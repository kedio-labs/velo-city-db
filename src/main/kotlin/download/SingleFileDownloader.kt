package download

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.nio.channels.Channels
import java.nio.channels.FileChannel
import java.nio.channels.ReadableByteChannel

private val logger = KotlinLogging.logger {}

class SingleFileDownloader {
    // these properties are declared here to easy unit testing of their successful closure
    // those unit tests unfortunately have hard-coded knowledge of the property names
    // see tests in SingleFileDownloaderTest for more details
    private lateinit var urlInputStream: InputStream
    private lateinit var urlReadableByteChannel: ReadableByteChannel
    private lateinit var fileOutputStream: FileOutputStream
    private lateinit var targetFileChannel: FileChannel

    suspend fun download(url: String, targetPath: String): Boolean = withContext(Dispatchers.IO) {

        try {
            logger.info { "Downloading content from URL $url" }

            val urlInstance = URI(url).toURL()

            urlInputStream = urlInstance.openStream()
            urlReadableByteChannel = Channels.newChannel(urlInputStream)

            fileOutputStream = FileOutputStream(targetPath)
            targetFileChannel = fileOutputStream.getChannel()

            logger.info { "Transferring data from $url" }

            targetFileChannel.transferFrom(urlReadableByteChannel, TRANSFER_POSITION, Long.MAX_VALUE)

            logger.info { "Download completed" }

            return@withContext true
        }
        // all non-runtime exceptions thrown in the try block above are subclasses of IOException
        catch (e: IOException) {
            logger.warn(e) { "Exception while downloading file at URL $url" }
            File(targetPath).delete()
            return@withContext false
        } finally {
            if (::urlInputStream.isInitialized) {
                urlInputStream.close()
            }
            if (::urlReadableByteChannel.isInitialized) {
                urlReadableByteChannel.close()
            }
            if (::fileOutputStream.isInitialized) {
                fileOutputStream.close()
            }
            if (::targetFileChannel.isInitialized) {
                targetFileChannel.close()
            }
        }
    }

    companion object {
        private const val TRANSFER_POSITION = 0L
    }
}
