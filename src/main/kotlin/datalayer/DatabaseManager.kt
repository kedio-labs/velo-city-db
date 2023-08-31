package datalayer

import CityTrafficMeasurement
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.sql.Connection

private val logger = KotlinLogging.logger {}

/**
 * Tested at the integration layer
 */
class DatabaseManager(
    private val databaseName: String, private val directoryAbsoluteFilePath: String
) {
    private fun connect(databaseAbsoluteFilePath: String) {

        logger.info { "Connecting to DB" }

        // see https://github.com/JetBrains/Exposed/wiki/DataBase-and-DataSource
        Database.connect(
            "jdbc:sqlite:${databaseAbsoluteFilePath}",
            "org.sqlite.JDBC"
        )
        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
    }

    private fun getDatabaseAbsoluteFilePath() =
        "${directoryAbsoluteFilePath}/${databaseName}.${SQLITE3_DATABASE_FILE_EXTENSION}"

    fun createDatabase() {
        logger.info { "Creating DB if it does not already exist" }

        val databaseAbsoluteFilePath = getDatabaseAbsoluteFilePath()
        connect(databaseAbsoluteFilePath)
        createTable(CityTrafficMeasurementTable)
    }

    fun deleteDatabase() {
        logger.info { "Deleting DB" }
        val databaseAbsoluteFilePath = getDatabaseAbsoluteFilePath()
        val database = File(databaseAbsoluteFilePath)
        if (database.exists()) {
            val deleted = database.delete();
            if (!deleted) {
                throw IllegalStateException("Could not delete the database located at: $databaseAbsoluteFilePath")
            }
        }
    }

    private fun <T : Table> createTable(table: T) {
        logger.info { "Creating table if it does not already exist" }
        transaction {
            SchemaUtils.create(table)
        }
    }

    fun <T : Table> deleteTable(table: T) {
        logger.info { "Deleting table" }
        transaction {
            SchemaUtils.drop(table)
        }
    }

    fun ingest(cityTrafficMeasurements: List<CityTrafficMeasurement>) {
        DataIngester().ingest(cityTrafficMeasurements)
    }

    companion object {
        private const val SQLITE3_DATABASE_FILE_EXTENSION = "sqlite3"
    }
}
