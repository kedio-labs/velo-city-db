import java.time.Instant
import java.time.ZoneId

data class CityTrafficMeasurement(
    val city: String,
    val locationName: String,
    val hourlyTrafficCount: Int,
    val measurementTimestamp: Instant
)

enum class DownloadType(val type: String) {
    SINGLE_FILE("single_file"),
    PAGINATED_SOCRATA_API("paginated_socrata_api")
}

data class DataSourceConfig(
    val city: String,
    val url: String,
    val downloadType: DownloadType,
    val targetFilePath: String,
    val className: Class<*>
)

data class SocrataCityInfo(
    val cityName: String,
    val zoneId: ZoneId
)
