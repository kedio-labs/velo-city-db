package download

import HasResourcePathGetter.Companion.getResourcePath
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.http.Fault
import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.http.HttpHeaders
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.channels.FileChannel
import java.nio.channels.ReadableByteChannel
import java.nio.file.Files
import kotlin.test.assertContentEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@WireMockTest
class UrlContentDownloaderTest {

    /**
     * Gets a named private field of a given class instance.
     */
    private fun <T : Any, V> getPrivateClassField(classInstance: T, fieldName: String): V {
        val field = (classInstance).javaClass.getDeclaredField(fieldName)
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        return field.get(classInstance) as V
    }

    @Test
    fun downloadSavesFileToTargetLocation(wmRuntimeInfo: WireMockRuntimeInfo) = runTest {

        val sourceCsvFilename = "expected-bicycle-traffic-data.csv"

        // WireMock expects files served by its underlying server to be under the directory "/resources/__files"
        // see https://github.com/wiremock/wiremock/blob/master/src/main/java/com/github/tomakehurst/wiremock/core/WireMockApp.java#L47
        val sourceCsvFilePath = getResourcePath("/__files/csv/${sourceCsvFilename}")
        val targetCsvFilePath = "${temporaryDirectory}/downloaded-bicycle-traffic-data.csv"

        // This test case includes a redirect to the actual file
        stubFor(get("/redirect").willReturn(permanentRedirect("/${sourceCsvFilename}")))

        stubFor(
            get("/${sourceCsvFilename}")
                .willReturn(
                    ok()
                        .withBodyFile("csv/${sourceCsvFilename}")
                        .withHeaders(
                            HttpHeaders(
                                HttpHeader("Content-Type", "text/csv; charset=utf-8"),
                                HttpHeader(
                                    "content-disposition",
                                    "attachment; filename=\"${sourceCsvFilename}\""
                                )
                            )
                        )
                )
        )

        val result = UrlContentDownloader().download(
            "http://localhost:${wmRuntimeInfo.httpPort}/redirect",
            targetCsvFilePath
        )


        assertTrue("Expected download to be successful") { result }

        assertTrue("Expected target file to be created") { File(targetCsvFilePath).exists() }

        val expectedFileContent = Files.lines(File(sourceCsvFilePath).toPath()).toArray()
        val actualFileContent = Files.lines(File(targetCsvFilePath).toPath()).toArray()

        assertContentEquals(expectedFileContent, actualFileContent)
    }

    @Test
    fun downloadReturnsFailsOnNotFound(wmRuntimeInfo: WireMockRuntimeInfo) = runTest {
        val notFound = "not-found.csv"

        val targetCsvFilePath = "${temporaryDirectory}/actual-not-found.csv"
        stubFor(get("/${notFound}").willReturn(notFound()))

        val result = UrlContentDownloader().download(
            "http://localhost:${wmRuntimeInfo.httpPort}/${notFound}",
            targetCsvFilePath // should not be created
        )

        assertFalse("Expected download to fail") { result }

        assertFalse("Expected target file to not be created") { File(targetCsvFilePath).exists() }
    }

    @Test
    fun downloadFailsOnServerError(wmRuntimeInfo: WireMockRuntimeInfo) = runTest {
        val serverError = "server-error.csv"

        val targetCsvFilePath = "${temporaryDirectory}/actual-server-error.csv"
        stubFor(get("/${serverError}").willReturn(serverError()))

        val result = UrlContentDownloader().download(
            "http://localhost:${wmRuntimeInfo.httpPort}/${serverError}",
            targetCsvFilePath // should not be created
        )

        assertFalse("Expected download to fail") { result }

        assertFalse("Expected target file to not be created") { File(targetCsvFilePath).exists() }
    }

    @Test
    // Warning: this test is likely not working on Windows, as per the WireMock documentation: https://wiremock.org/docs/simulating-faults/#bad-responses
    fun downloadReturnsFailsOnConnectionResetByPeer(wmRuntimeInfo: WireMockRuntimeInfo) = runTest {
        val connectionReset = "connection-reset.csv"

        val targetCsvFilePath = "${temporaryDirectory}/connection-reset.csv"
        stubFor(
            get("/${connectionReset}")
                .willReturn(ok().withFault(Fault.MALFORMED_RESPONSE_CHUNK))
        )

        val result = UrlContentDownloader().download(
            "http://localhost:${wmRuntimeInfo.httpPort}/${connectionReset}",
            targetCsvFilePath // should not be created
        )

        assertFalse("Expected download to fail") { result }

        assertFalse("Expected target file to not be created") { File(targetCsvFilePath).exists() }
    }

    @Test
    fun downloadClosesChannelsAndStreamsOnSuccess(wmRuntimeInfo: WireMockRuntimeInfo) = runTest {
        /**
         * This test unfortunately has to hard-codedly know the names of private fields in CsvFileDownloader.
         * This is far from ideal but still provides a practical way that streams and channels are properly closed after use.
         */

        val sourceCsvFilename = "expected-bicycle-traffic-data.csv"

        // WireMock expects files served by its underlying server to be under the directory "/resources/__files"
        // see https://github.com/wiremock/wiremock/blob/master/src/main/java/com/github/tomakehurst/wiremock/core/WireMockApp.java#L47
        val targetCsvFilePath = "${temporaryDirectory}/downloaded-bicycle-traffic-data.csv"

        stubFor(
            get("/${sourceCsvFilename}")
                .willReturn(
                    ok()
                        .withBodyFile("csv/${sourceCsvFilename}")
                        .withHeaders(
                            HttpHeaders(
                                HttpHeader("Content-Type", "text/csv; charset=utf-8"),
                                HttpHeader(
                                    "content-disposition",
                                    "attachment; filename=\"${sourceCsvFilename}\""
                                )
                            )
                        )
                )
        )


        val urlContentDownloader = UrlContentDownloader()
        val result = urlContentDownloader.download(
            "http://localhost:${wmRuntimeInfo.httpPort}/${sourceCsvFilename}",
            targetCsvFilePath
        )

        assertTrue("Expected download to be successful") { result }

        assertThrows<IOException> {
            val stream = getPrivateClassField<UrlContentDownloader, InputStream>(urlContentDownloader, "urlInputStream")
            stream.available()
        }

        assertFalse {
            val channel =
                getPrivateClassField<UrlContentDownloader, ReadableByteChannel>(
                    urlContentDownloader,
                    "urlReadableByteChannel"
                )
            channel.isOpen
        }

        assertThrows<IOException> {
            val stream =
                getPrivateClassField<UrlContentDownloader, FileOutputStream>(urlContentDownloader, "fileOutputStream")

            stream.write(0)
        }

        assertFalse {
            val channel =
                getPrivateClassField<UrlContentDownloader, FileChannel>(urlContentDownloader, "targetFileChannel")

            channel.isOpen
        }
    }

    companion object {
        private val temporaryDirectory = "${getResourcePath("/")}csv_download_test_temp"

        private fun createTemporaryDirectory() = File(temporaryDirectory).mkdir()
        private fun deleteTemporaryDirectory() = File(temporaryDirectory).deleteRecursively()

        @JvmStatic
        @BeforeAll
        fun beforeAll() {

            deleteTemporaryDirectory()
            val temporaryDirectoryCreated = createTemporaryDirectory()
            assertTrue("Could not create temporary directory for the CSV downloader tests") { temporaryDirectoryCreated }
        }

        @JvmStatic
        @AfterAll
        fun afterAll() {
            deleteTemporaryDirectory()
        }
    }
}