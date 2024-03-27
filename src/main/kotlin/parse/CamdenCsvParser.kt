package parse

import CityTrafficMeasurement
import SocrataCityInfo
import java.time.ZoneId

class CamdenCsvParser(override val ingest: (List<CityTrafficMeasurement>) -> Unit) : CsvParser {
    override val batch = mutableListOf<CityTrafficMeasurement>()

    override fun parseAndIngest(absoluteFilePath: String) =
        SocrataCsvParser(ingest, SocrataCityInfo("Camden", ZoneId.of("Europe/London"))).parseAndIngest(absoluteFilePath)

}