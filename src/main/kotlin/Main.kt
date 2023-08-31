import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.output.MordantHelpFormatter
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.boolean
import datalayer.DatabaseManager
import download.CsvParallelDownloader
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import parse.CsvParser
import java.io.File

private val logger = KotlinLogging.logger {}
private const val APPLICATION_NAME = "VéloCityDB"
private const val DATA_SOURCES_CONFIG_FILEPATH = "/data-sources.yml"

class Main : CliktCommand(printHelpOnEmptyArgs = true) {
    init {
        context {
            helpFormatter = { MordantHelpFormatter(it, showRequiredTag = true, showDefaultValues = true) }
        }
    }

    private val dataDirectoryPath: String by option()
        .help("Target data directory where CSV files will be downloaded and the resulting SQLite database created. Must be an absolute path.")
        .required()

    private val overrideCsvFiles: Boolean by option()
        .boolean()
        .help("Override a city CSV file if it already exists in the data directory.")
        .default(false)

    private val deleteExistingDatabase: Boolean by option()
        .boolean()
        .help("Delete the database if it already exists before starting the data ingestion. If set to false, this program will ingest data in the existing database")
        .default(true)

    override fun run() {
        logger.info { "Started $APPLICATION_NAME" }
        logger.info {
            "\n" +
                    """
        ██╗   ██╗███████╗██╗      ██████╗  ██████╗██╗████████╗██╗   ██╗██████╗ ██████╗ 
        ██║   ██║██╔════╝██║     ██╔═══██╗██╔════╝██║╚══██╔══╝╚██╗ ██╔╝██╔══██╗██╔══██╗
        ██║   ██║█████╗  ██║     ██║   ██║██║     ██║   ██║    ╚████╔╝ ██║  ██║██████╔╝
        ╚██╗ ██╔╝██╔══╝  ██║     ██║   ██║██║     ██║   ██║     ╚██╔╝  ██║  ██║██╔══██╗
         ╚████╔╝ ███████╗███████╗╚██████╔╝╚██████╗██║   ██║      ██║   ██████╔╝██████╔╝
          ╚═══╝  ╚══════╝╚══════╝ ╚═════╝  ╚═════╝╚═╝   ╚═╝      ╚═╝   ╚═════╝ ╚═════╝ 
    """.trimIndent()
        }

        val dataSourceConfigs =
            DataSourcesConfigLoader().load(DATA_SOURCES_CONFIG_FILEPATH, dataDirectoryPath)

        val toDownload = dataSourceConfigs.filter {
            overrideCsvFiles || !File(it.targetFilePath).exists()
        }

        if (toDownload.isNotEmpty()) {
            logger.info { "Will download CSV files for cities: ${toDownload.map { it.city }}" }

            runBlocking {
                logger.info { "Downloading CSV files in parallel" }
                CsvParallelDownloader().downloadParallel(toDownload)
            }
        } else {
            logger.info { "Skipping CSV file download" }
        }

        val databaseManager = DatabaseManager("velocitydb", dataDirectoryPath)
        if (deleteExistingDatabase) {
            databaseManager.deleteDatabase()
        }
        databaseManager.createDatabase()

        dataSourceConfigs.forEachIndexed { index, dataSourceConfig ->
            logger.info { "Parsing and ingesting CSV data for city ${index + 1}/${dataSourceConfigs.size}: ${dataSourceConfig.city}" }
            // TODO - replace with more robust call to CSV parser
            val csvParser = dataSourceConfig.className.getDeclaredConstructors()[0]
                .newInstance(databaseManager::ingest) as CsvParser
            csvParser.parseAndIngest(dataSourceConfig.targetFilePath)
        }

        logger.info { "Process completed. The SQLite database is located in directory $dataDirectoryPath" }
    }
}

fun main(args: Array<String>) = Main().main(args)
