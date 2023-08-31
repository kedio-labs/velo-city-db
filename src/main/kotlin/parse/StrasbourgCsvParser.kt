package parse

import CityTrafficMeasurement
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.ZonedDateTime
import java.time.format.DateTimeParseException

private val logger = KotlinLogging.logger {}

class StrasbourgCsvParser(override val ingest: (List<CityTrafficMeasurement>) -> Unit) : CsvParser {

    override val batch = mutableListOf<CityTrafficMeasurement>()

    override fun parseAndIngest(absoluteFilePath: String) {
        validateRelativeFilePath(absoluteFilePath)

        csvReader {
            delimiter = ';'
        }.open(absoluteFilePath) {
            readAllWithHeaderAsSequence().forEach { row: Map<String, String> ->

                ingestBatch(batch)

                val date: String? = row["date"]

                try {
                    val hourlyTrafficCountAsString: String? = row["sum_counts"]
                    val hourlyTrafficCount =
                        if (hourlyTrafficCountAsString.isNullOrEmpty()) 0 else hourlyTrafficCountAsString.toInt()

                    if (!date.isNullOrEmpty()) {

                        val zonedDateTime = ZonedDateTime.parse(date)
                        val instant = zonedDateTime.toInstant()

                        val cityTrafficMeasurement = CityTrafficMeasurement(
                            "Strasbourg",
                            row["id_compteur"]!!,
                            hourlyTrafficCount,
                            instant
                        )

                        batch += cityTrafficMeasurement
                    }
                } catch (e: DateTimeParseException) {
                    logger.info { "Could not parse date in row $row" }
                } catch (e: NumberFormatException) {
                    logger.info { "Could not parse sum count in row $row" }
                }
            }

            ingestBatch(batch, force = true)
        }
    }
}