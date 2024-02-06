import io.github.oshai.kotlinlogging.KotlinLogging
import org.yaml.snakeyaml.Yaml

private val logger = KotlinLogging.logger {}

private const val DATA_SOURCE_CITY_NAME_KEY = "name"
private const val DATA_SOURCE_CITY_URL_KEY = "url"
private const val DATA_SOURCE_CITY_DOWNLOAD_TYPE_KEY = "download_type"

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
        val dataSources = yaml.load<List<Map<String, String>>>(dataSourceConfigStream)

        if (!dataSources.isNullOrEmpty()) {
            // check if all cities have a corresponding CSV parser
            val citiesWithoutCsvParser = dataSources.filter { map ->
                // use getValue() as we know the keys for each city's key-value pairs
                // this will ensure we get "String" as opposed to "String?"
                val cityName = map.getValue(DATA_SOURCE_CITY_NAME_KEY)
                val cityUrl = map.getValue(DATA_SOURCE_CITY_URL_KEY)
                val downloadTypeAsString = map.getValue(DATA_SOURCE_CITY_DOWNLOAD_TYPE_KEY)

                val downloadType = DownloadType.entries.find { it.type == downloadTypeAsString }
                    ?: throw IllegalArgumentException("Unknown download type: $downloadTypeAsString")


                try {
                    // look for a CSV parser class for that city name
                    // the line below will throw a ClassNotFoundException if no parser found. This is expected :)
                    val csvParserClass = Class.forName("parse.${cityName}CsvParser", false, javaClass.classLoader)

                    result += DataSourceConfig(
                        cityName,
                        cityUrl,
                        downloadType,
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
