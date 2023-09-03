package parse

import CityTrafficMeasurement
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.ZonedDateTime
import java.time.format.DateTimeParseException

private val logger = KotlinLogging.logger {}

class RennesCsvParser(override val ingest: (List<CityTrafficMeasurement>) -> Unit) : CsvParser {

    override val batch = mutableListOf<CityTrafficMeasurement>()

    override fun parseAndIngest(absoluteFilePath: String) {
        validateRelativeFilePath(absoluteFilePath)

        csvReader {
            delimiter = ';'
        }.open(absoluteFilePath) {
            readAllWithHeaderAsSequence().forEach { row: Map<String, String> ->

                ingestBatch(batch)

                val date: String? = row["date"]

                if (!date.isNullOrEmpty()) {
                    try {
                        val hourlyTrafficCountAsString: String? = row["counts"]
                        val hourlyTrafficCount =
                            if (hourlyTrafficCountAsString.isNullOrEmpty()) 0 else hourlyTrafficCountAsString.toInt()

                        val zonedDateTime = ZonedDateTime.parse(date)
                        val instant = zonedDateTime.toInstant()

                        val cityTrafficMeasurement = CityTrafficMeasurement(
                            "Rennes",
                            row["name"]!!,
                            hourlyTrafficCount,
                            instant
                        )

                        batch += cityTrafficMeasurement
                    } catch (e: DateTimeParseException) {
                        logger.info { "Could not parse date in row $row" }
                    } catch (e: NumberFormatException) {
                        logger.info { "Could not parse sum count in row $row" }
                    }
                }
            }

            ingestBatch(batch, force = true)
        }
    }
}