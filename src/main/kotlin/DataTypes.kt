import java.time.Instant

data class CityTrafficMeasurement(
    val city: String,
    val locationName: String,
    val hourlyTrafficCount: Int,
    val measurementTimestamp: Instant
)

enum class DownloadType(val type: String) {
    SINGLE_FILE("single_file"),
    PAGINATED_SOCRATA_API("paginated_socrata_api"),
    REMOVE_ME("REMOVE_ME")
}

data class DataSourceConfig(
    val city: String,
    val url: String,
    val downloadType: DownloadType,
    val targetFilePath: String,
    val className: Class<*>
)