package ingest

import HasResourcePathGetter.Companion.getResourcePath
import io.github.oshai.kotlinlogging.KotlinLogging
import main
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes

private val logger = KotlinLogging.logger {}

class MainIntegrationTest {

    private var dataLayer: DataLayer? = null

    @AfterEach
    fun afterEach() {
        if (dataLayer != null) {
            dataLayer!!.closeConnection()
        }
    }

    @Test
    fun ingestsCsvFilesSuccessfully() {
        // the CSV data files have been crafted so that their row size is kept small while still bigger than the ingestion batch size
        // see INGESTION_BATCH_SIZE in CsvParser

        // given
        val testDataAbsolutePath = getResourcePath("/test-data")

        // when
        main(
            arrayOf(
                "--data-directory-path",
                testDataAbsolutePath,
                "--override-csv-files",
                "false",
                "--delete-existing-database",
                "true"
            )
        )

        dataLayer = DataLayer("$testDataAbsolutePath/velocitydb.sqlite3")
        dataLayer!!.openConnection()

        // then
        assertEquals(5999, dataLayer!!.getNumberOfMeasurementsForCity("Bordeaux"))
        assertEquals(143976, dataLayer!!.getNumberOfMeasurementsForCity("Nantes"))
        assertEquals(5999, dataLayer!!.getNumberOfMeasurementsForCity("Paris"))
        assertEquals(5999, dataLayer!!.getNumberOfMeasurementsForCity("Rennes"))
        assertEquals(5999, dataLayer!!.getNumberOfMeasurementsForCity("Strasbourg"))
    }

    @Test
    fun deletesExistingDatabaseFile() {

        // given
        val testDataAbsolutePath = getResourcePath("/test-data")

        val existingDb = File("$testDataAbsolutePath/velocitydb.sqlite3")
        existingDb.createNewFile()
        Files.write(existingDb.toPath(), "Some content that does not matter".toByteArray())
        val fileAttributesBeforeIngestion = Files.readAttributes(existingDb.toPath(), BasicFileAttributes::class.java)

        // when
        main(
            arrayOf(
                "--data-directory-path",
                testDataAbsolutePath,
                "--override-csv-files",
                "false",
                "--delete-existing-database",
                "true"
            )
        )

        // then
        val fileAttributesAfterIngestion = Files.readAttributes(existingDb.toPath(), BasicFileAttributes::class.java)

        assertNotEquals(fileAttributesBeforeIngestion.creationTime(), fileAttributesAfterIngestion.creationTime())
        assertNotEquals(
            fileAttributesBeforeIngestion.lastModifiedTime(),
            fileAttributesAfterIngestion.lastModifiedTime()
        )
        assertNotEquals(fileAttributesBeforeIngestion.size(), fileAttributesAfterIngestion.size())
    }
}