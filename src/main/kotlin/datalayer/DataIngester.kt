package datalayer

import CityTrafficMeasurement
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Tested at the integration layer
 */
class DataIngester {
    private fun insertToCityTrafficMeasurementTable(cityTrafficMeasurement: CityTrafficMeasurement) {
        CityTrafficMeasurementTable.insert {
            it[city] = cityTrafficMeasurement.city
            it[locationName] = cityTrafficMeasurement.locationName
            it[hourlyTrafficCount] = cityTrafficMeasurement.hourlyTrafficCount
            it[measurementTimestamp] = cityTrafficMeasurement.measurementTimestamp
        }
    }

    fun ingest(cityTrafficMeasurements: List<CityTrafficMeasurement>) {
        transaction {
            SchemaUtils.create(CityTrafficMeasurementTable)

            cityTrafficMeasurements.forEach(::insertToCityTrafficMeasurementTable)
        }
    }
}