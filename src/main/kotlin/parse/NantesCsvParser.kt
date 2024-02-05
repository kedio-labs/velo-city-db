package parse

import CityTrafficMeasurement
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeParseException

private val logger = KotlinLogging.logger {}

class NantesCsvParser(override val ingest: (List<CityTrafficMeasurement>) -> Unit) : CsvParser {

    override val batch = mutableListOf<CityTrafficMeasurement>()

    override fun parseAndIngest(absoluteFilePath: String) {
        validateFilePath(absoluteFilePath)

        val zoneId = ZoneId.of("Europe/Paris")

        csvReader {
            delimiter = ';'
        }.open(absoluteFilePath) {
            readAllWithHeaderAsSequence().forEach { row: Map<String, String> ->

                ingestBatch(batch)

                val locationName = row["Libellé"]!!

                val date: String? = row["Jour"]

                if (!date.isNullOrEmpty()) {
                    for (hour in 0..23) {
                        val paddedHour = if (hour < 10) "0$hour" else "$hour"

                        try {
                            val instant = LocalDateTime.parse("${date}T${paddedHour}:00:00").atZone(zoneId).toInstant()

                            val hourlyTrafficCountAsString: String? = row[paddedHour]
                            val hourlyTrafficCount =
                                if (hourlyTrafficCountAsString.isNullOrEmpty()) 0 else hourlyTrafficCountAsString.toInt()

                            val cityTrafficMeasurement = CityTrafficMeasurement(
                                "Nantes",
                                locationName,
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
                }
            }

            ingestBatch(batch, force = true)
        }
    }
}