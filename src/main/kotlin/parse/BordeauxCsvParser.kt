package parse

import CityTrafficMeasurement
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.ZonedDateTime
import java.time.format.DateTimeParseException

private val logger = KotlinLogging.logger {}

private const val ROW_TYPE_BOUCLE = "BOUCLE"

class BordeauxCsvParser(override val ingest: (List<CityTrafficMeasurement>) -> Unit) : CsvParser {

    override val batch = mutableListOf<CityTrafficMeasurement>()

    override fun parseAndIngest(absoluteFilePath: String) {
        validateRelativeFilePath(absoluteFilePath)

        csvReader {
            delimiter = ';'
        }.open(absoluteFilePath) {
            readAllWithHeaderAsSequence().forEach { row: Map<String, String> ->

                ingestBatch(batch)

                val date: String? = row["Date et heure de comptage"]

                if (!date.isNullOrEmpty() && row["type"]!! == ROW_TYPE_BOUCLE) {
                    try {
                        // despite what its name suggests, the field "comptage_5m" represents an hourly slot
                        val hourlyTrafficCountAsString: String? = row["comptage_5m"]
                        val hourlyTrafficCount =
                            if (hourlyTrafficCountAsString.isNullOrEmpty()) 0 else hourlyTrafficCountAsString.toInt()

                        val zonedDateTime = ZonedDateTime.parse(date)
                        val instant = zonedDateTime.toInstant()

                        val cityTrafficMeasurement = CityTrafficMeasurement(
                            "Bordeaux",
                            row["libelle"]!!,
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