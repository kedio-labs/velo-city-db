package download.socrata

import download.SingleFileDownloader
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedWriter
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.Path

private val logger = KotlinLogging.logger {}

class SocrataDownloader {
    // these properties are declared here to easy unit testing of their successful closure
    // those unit tests unfortunately have hard-coded knowledge of the property names
    // see tests in SocrataDownloaderTest for more details
    private lateinit var targetFileWriter: BufferedWriter

    private suspend fun downloadInternal(
        recordsBatchSize: Long,
        baseUrl: String,
        targetPath: String,
        pageNumber: Int
    ): Pair<Boolean, Int> {
        val offset = pageNumber * recordsBatchSize

        val url = "${baseUrl}?\$order=:id&\$limit=${recordsBatchSize}&\$offset=${offset}"
        val batchTargetPath = getBatchResultsFilePath(targetPath, pageNumber)

        val result = SingleFileDownloader().download(url, batchTargetPath)
        if (!result) {
            return Pair(false, pageNumber)
        }

        // number of records = number of lines minus the csv header
        val numberOfRecords = Files.lines(Path(batchTargetPath)).count() - 1
        if (numberOfRecords < recordsBatchSize) {
            return Pair(true, pageNumber)
        }

        return downloadInternal(recordsBatchSize, baseUrl, targetPath, pageNumber + 1)
    }


    suspend fun download(
        baseUrl: String,
        targetFilePath: String,
        recordsBatchSize: Long = RECORDS_BATCH_SIZE
    ): Boolean =
        withContext(Dispatchers.IO) {
            val result = downloadInternal(recordsBatchSize, baseUrl, targetFilePath, 0)
            if (!result.first) {
                return@withContext false
            }

            try {
                targetFileWriter = Files.newBufferedWriter(Paths.get(targetFilePath))

                for (pageNumber in 0..result.second) {
                    val batchSourceFilePath = getBatchResultsFilePath(targetFilePath, pageNumber)
                    val isIncludeHeaderRow = pageNumber == 0

                    SocrataBatchResultsWriter().writeBatchResults(
                        targetFileWriter,
                        batchSourceFilePath,
                        isIncludeHeaderRow
                    )
                }
            }
            // all non-runtime exceptions thrown in the try block above are subclasses of IOException
            catch (e: IOException) {
                logger.warn(e) { "Exception while downloading file at URL $baseUrl" }
                File(targetFilePath).delete()
                return@withContext false
            } finally {
                if (::targetFileWriter.isInitialized) {
                    targetFileWriter.close()
                }
            }

            return@withContext true
        }

    companion object {
        private const val RECORDS_BATCH_SIZE = 500000L

        private fun getBatchResultsFilePath(targetPath: String, pageNumber: Int) = "${targetPath}_${pageNumber}"
    }
}