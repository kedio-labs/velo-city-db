package ingest

import io.github.oshai.kotlinlogging.KotlinLogging
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Statement

private val logger = KotlinLogging.logger {}

class DataLayer(private val sqliteDatabaseAbsoluteFilepath: String) {

    private var connection: Connection? = null

    private fun getStatement(): Statement {
        val statement = connection!!.createStatement()
        statement.queryTimeout = QUERY_TIMEOUT_SEC

        return statement
    }

    fun openConnection() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:$sqliteDatabaseAbsoluteFilepath")
        } catch (e: SQLException) {
            logger.error { "Could not connect to SQLite database located at $sqliteDatabaseAbsoluteFilepath" }
            throw e
        }
    }

    fun closeConnection() {
        try {
            connection?.close()
        } catch (e: SQLException) {
            logger.error { "Could not close connection to SQLite database located at $sqliteDatabaseAbsoluteFilepath" }
            throw e
        }
    }


    fun getNumberOfMeasurementsForCity(city: String): Int {
        try {
            val rs = getStatement().executeQuery("select COUNT(1) from ${TABLE_NAME} where city=\"$city\"")
            rs.next()
            return rs.getInt(1)

        } catch (e: SQLException) {
            logger.error { "Error while running query on SQLite DB" }
            throw e
        }
    }

    companion object {
        const val TABLE_NAME = "CityTrafficMeasurement"
        const val QUERY_TIMEOUT_SEC = 3
    }
}