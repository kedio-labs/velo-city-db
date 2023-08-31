import java.time.Instant

data class CityTrafficMeasurement(
    val city: String,
    val locationName: String,
    val hourlyTrafficCount: Int,
    val measurementTimestamp: Instant
)

data class DataSourceConfig(
    val city: String,
    val url: String,
    val targetFilePath: String,
    val className: Class<*>
)