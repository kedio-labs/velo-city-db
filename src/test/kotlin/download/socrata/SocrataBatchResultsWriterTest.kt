package download.socrata

import HasPrivateClassFieldGetter
import HasResourcePathGetter.Companion.getResourcePath
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.test.assertContentEquals
import kotlin.test.assertTrue

class SocrataBatchResultsWriterTest : HasPrivateClassFieldGetter {

    @Test
    fun writesToTargetFileWithHeaderRow() = runTest {
        val targetFilePath = "${temporaryDirectory}/target_file.csv"
        val targetFileWriter = Files.newBufferedWriter(Paths.get(targetFilePath))

        val batchSourceFilePath = getResourcePath("__files/csv/socrata-downloader-source-data-2.csv")

        val result = SocrataBatchResultsWriter().writeBatchResults(targetFileWriter, batchSourceFilePath, true)
        // make sure the target file writer is closed (and therefore flushed) before assertions are run
        targetFileWriter.close()

        assertTrue("Expected download to be successful") { result }

        assertTrue("Expected target file to be created") { File(targetFilePath).exists() }


        val actual = Files.lines(Paths.get(targetFilePath)).toArray()
        val expected = Files.lines(Paths.get(batchSourceFilePath)).toArray()

        assertContentEquals(expected, actual)
    }

    @Test
    fun writesToTargetFileWithoutHeaderRow() = runTest {
        val targetFilePath = "${temporaryDirectory}/target_file.csv"
        val targetFileWriter = Files.newBufferedWriter(Paths.get(targetFilePath))

        val batchSourceFilePath = getResourcePath("__files/csv/socrata-downloader-source-data-2.csv")

        val result = SocrataBatchResultsWriter().writeBatchResults(targetFileWriter, batchSourceFilePath, false)
        // make sure the target file writer is closed (and therefore flushed) before assertions are run
        targetFileWriter.close()

        assertTrue("Expected download to be successful") { result }

        assertTrue("Expected target file to be created") { File(targetFilePath).exists() }


        val actual = Files.lines(Paths.get(targetFilePath)).toArray()
        val expected = Files.lines(Paths.get(batchSourceFilePath)).skip(1).toArray()

        assertContentEquals(expected, actual)
    }

    @Test
    fun closesIOResourcesOnCompletion() = runTest {
        val targetFileWriter = Files.newBufferedWriter(Paths.get("$temporaryDirectory/target_file.csv"))
        val batchSourceFilePath = getResourcePath("__files/csv/socrata-downloader-source-data-2.csv")

        val writer = SocrataBatchResultsWriter()
        val result = writer.writeBatchResults(targetFileWriter, batchSourceFilePath, true)
        // make sure the target file writer is closed (and therefore flushed) before assertions are run
        targetFileWriter.close()

        assertTrue("Expected download to be successful") { result }

        assertThrows<IOException> {
            val reader =
                getPrivateClassField<SocrataBatchResultsWriter, BufferedReader>(writer, "bufferedBatchFileReader")
            reader.readLine()
        }
    }

    companion object {
        private val temporaryDirectory = "${getResourcePath("/")}socrata_batch_results_writer_test_temp"

        private fun createTemporaryDirectory() = File(temporaryDirectory).mkdir()
        private fun deleteTemporaryDirectory() = File(temporaryDirectory).deleteRecursively()

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            deleteTemporaryDirectory()
            val temporaryDirectoryCreated = createTemporaryDirectory()
            assertTrue("Could not create temporary directory for tests") { temporaryDirectoryCreated }
        }

        @JvmStatic
        @AfterAll
        fun afterAll() {
            deleteTemporaryDirectory()
        }
    }
}