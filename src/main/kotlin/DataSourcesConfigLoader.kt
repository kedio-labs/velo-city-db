import io.github.oshai.kotlinlogging.KotlinLogging
import org.yaml.snakeyaml.Yaml

private val logger = KotlinLogging.logger {}

class DataSourcesConfigLoader {

    fun load(
        dataSourceConfigRelativePath: String,
        csvTargetFilesDirectoryAbsolutePath: String
    ): List<DataSourceConfig> {

        if (dataSourceConfigRelativePath.isEmpty()) {
            throw IllegalArgumentException("Please provide a path relative to the resources directory for the data source config file")
        }

        if (csvTargetFilesDirectoryAbsolutePath.isEmpty()) {
            throw IllegalArgumentException("Please provide an absolute path for the CSV target files directory")
        }

        val result = arrayListOf<DataSourceConfig>()

        logger.info { "Loading data sources config file" }

        val dataSourceConfigStream = this::class.java.getResourceAsStream(dataSourceConfigRelativePath)
            ?: throw IllegalStateException("File not found: $dataSourceConfigRelativePath")

        val yaml = Yaml()
        val dataSources = yaml.load<Map<String, String>>(dataSourceConfigStream)

        if (!dataSources.isNullOrEmpty()) {
            // check if all cities have a corresponding CSV parser
            val citiesWithoutCsvParser = dataSources.entries.filter { (cityName, cityUrl) ->
                try {
                    // first, look for a CSV parser class that city name. The line below will throw a ClassNotFoundException
                    // if no parser found. This is expected
                    val csvParserClass = Class.forName("parse.${cityName}CsvParser", false, javaClass.classLoader)

                    result += DataSourceConfig(
                        cityName,
                        cityUrl,
                        "${csvTargetFilesDirectoryAbsolutePath}/${cityName}.csv",
                        csvParserClass
                    )
                    false
                } catch (e: ClassNotFoundException) {
                    true
                }
            }

            if (citiesWithoutCsvParser.isNotEmpty()) {
                logger.warn { "The following cities listed in file $dataSourceConfigRelativePath will be ignored as they have no CSV parser class: $citiesWithoutCsvParser" }
            }
        }

        return result
    }
}
