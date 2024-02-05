package parse

import CityTrafficMeasurement
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.ZonedDateTime
import java.time.format.DateTimeParseException

private val logger = KotlinLogging.logger {}

class ParisCsvParser(override val ingest: (List<CityTrafficMeasurement>) -> Unit) : CsvParser {

    override val batch = mutableListOf<CityTrafficMeasurement>()

    override fun parseAndIngest(absoluteFilePath: String) {
        validateFilePath(absoluteFilePath)

        csvReader {
            delimiter = ';'
        }.open(absoluteFilePath) {
            readAllWithHeaderAsSequence().forEach { row: Map<String, String> ->

                ingestBatch(batch)

                try {
                    val zonedDateTime = ZonedDateTime.parse(row["Date et heure de comptage"]!!)
                    val instant = zonedDateTime.toInstant()

                    val hourlyTrafficCountAsString: String? = row["Comptage horaire"]
                    val hourlyTrafficCount =
                        if (hourlyTrafficCountAsString.isNullOrEmpty()) 0 else hourlyTrafficCountAsString.toFloat()
                            .toInt()

                    val cityTrafficMeasurement = CityTrafficMeasurement(
                        "Paris",
                        row["Nom du compteur"]!!,
                        hourlyTrafficCount,
                        instant
                    )

                    batch += cityTrafficMeasurement
                } catch (e: DateTimeParseException) {
                    logger.info { "Could not parse date for row $row" }
                } catch (e: NumberFormatException) {
                    logger.info { "Could not parse hourly traffic count for row $row" }
                }
            }

            ingestBatch(batch, force = true)
        }
    }
}