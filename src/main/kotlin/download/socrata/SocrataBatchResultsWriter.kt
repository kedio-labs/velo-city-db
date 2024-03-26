package download.socrata

import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths

private val logger = KotlinLogging.logger {}

class SocrataBatchResultsWriter {
    // these properties are declared here to easy unit testing of their successful closure
    // those unit tests unfortunately have hard-coded knowledge of the property names
    // see tests in SocrataDownloaderTest for more details
    private lateinit var bufferedBatchFileReader: BufferedReader

    fun writeBatchResults(
        targetFileWriter: BufferedWriter,
        batchSourceFilePath: String,
        isIncludeHeaderRow: Boolean
    ): Boolean {
        try {
            bufferedBatchFileReader = Files.newBufferedReader(Paths.get(batchSourceFilePath))

            bufferedBatchFileReader.useLines { lines ->
                val linesToProcess = if (!isIncludeHeaderRow) lines.drop(1) else lines

                linesToProcess.forEach { line ->
                    targetFileWriter.write(line)
                    targetFileWriter.newLine()
                }
            }
        }
        // all non-runtime exceptions thrown in the try block above are subclasses of IOException
        catch (e: IOException) {
            logger.warn(e) { "Exception while copying batch file $batchSourceFilePath" }
            return false
        } finally {
            if (::bufferedBatchFileReader.isInitialized) {
                bufferedBatchFileReader.close()
            }
        }

        return true
    }
}