package datalayer

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

const val VARCHAR_COLUMN_LENGTH = 80

object CityTrafficMeasurementTable : Table() {
    private val id = integer("id").autoIncrement()
    val city = varchar("city", VARCHAR_COLUMN_LENGTH)
    val locationName = varchar("location_name", VARCHAR_COLUMN_LENGTH)
    val hourlyTrafficCount = integer("hourly_traffic_count")
    val measurementTimestamp = timestamp("measurement_timestamp")

    override val primaryKey = PrimaryKey(id)
}