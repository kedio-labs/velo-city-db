package parse

import CityTrafficMeasurement
import SocrataCityInfo
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.LocalDateTime
import java.time.format.DateTimeParseException

private val logger = KotlinLogging.logger {}

class SocrataCsvParser(
    override val ingest: (List<CityTrafficMeasurement>) -> Unit,
    private val socrataCityInfo: SocrataCityInfo
) :
    CsvParser {

    override val batch = mutableListOf<CityTrafficMeasurement>()

    override fun parseAndIngest(absoluteFilePath: String) {
        validateFilePath(absoluteFilePath)

        csvReader().open(absoluteFilePath) {
            readAllWithHeaderAsSequence().forEach { row: Map<String, String> ->
                ingestBatch(batch)

                val epoch: String? = row["epoch"]

                if (!epoch.isNullOrEmpty()) {
                    try {
                        val instant = LocalDateTime.parse(epoch).atZone(socrataCityInfo.zoneId).toInstant()

                        val hourlyTrafficCountAsString: String? = row["hourly_count"]
                        val hourlyTrafficCount =
                            if (hourlyTrafficCountAsString.isNullOrEmpty()) 0 else hourlyTrafficCountAsString.toInt()

                        val cityTrafficMeasurement = CityTrafficMeasurement(
                            socrataCityInfo.cityName,
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