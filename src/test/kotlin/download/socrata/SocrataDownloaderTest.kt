package download.socrata

import HasPrivateClassFieldGetter
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
import java.io.BufferedWriter
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.Path
import kotlin.test.assertContentEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@WireMockTest
class SocrataDownloaderTest : HasPrivateClassFieldGetter {

    @Test
    fun downloadSavesFileToTargetLocation(wmRuntimeInfo: WireMockRuntimeInfo) = runTest {

        val recordsBatchSize = 3L

        val sourceCsvFilenames = arrayOf(
            "socrata-downloader-source-data-1.csv",
            "socrata-downloader-source-data-2.csv"
        )

        sourceCsvFilenames.mapIndexed { index, sourceCsvFilename ->
            val offset = index * recordsBatchSize
            // This test case includes a redirect to the actual file
            stubFor(
                get("/redirect.csv?\$order=:id&\$limit=${recordsBatchSize}&\$offset=${offset}")
                    .willReturn(
                        permanentRedirect("/${sourceCsvFilename}")
                    )
            )

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
        }

        val targetFilePath = "$temporaryDirectory/downloaded-bicycle-traffic-data.csv"
        val result = SocrataDownloader(recordsBatchSize).download(
            "http://localhost:${wmRuntimeInfo.httpPort}/redirect.csv",
            targetFilePath
        )

        assertTrue("Expected download to be successful") { result }

        assertTrue("Expected target file to be created") { File(targetFilePath).exists() }

        val expectedFileContent =
            sourceCsvFilenames
                .mapIndexed { index, filename ->
                    // WireMock expects files served by its underlying server to be under the directory "/resources/__files"
                    // see https://github.com/wiremock/wiremock/blob/master/src/main/java/com/github/tomakehurst/wiremock/core/WireMockApp.java#L48
                    val sourceFilePath = getResourcePath("/__files/csv/${filename}")

                    if (index == 0) {
                        // keep the csv header line for the first file in the array
                        Files.lines(Path(sourceFilePath)).toList()
                    } else {
                        // skip the csv header line for the subsequent files in the array
                        Files.lines(Path(sourceFilePath)).skip(1).toList()
                    }
                }.flatten()

        val actualFileContent = Files.lines(Paths.get(targetFilePath)).toList()

        assertContentEquals(expectedFileContent, actualFileContent)
    }

    @Test
    fun downloadFailsOnNotFound(wmRuntimeInfo: WireMockRuntimeInfo) = runTest {
        val notFound = "not-found.csv"

        val targetFilePath = "$temporaryDirectory/actual-not-found.csv"
        stubFor(get("/${notFound}").willReturn(notFound()))

        val result = SocrataDownloader().download(
            "http://localhost:${wmRuntimeInfo.httpPort}/${notFound}",
            targetFilePath // should not be created
        )

        assertFalse("Expected download to fail") { result }

        assertFalse("Expected target file to not be created") { File(targetFilePath).exists() }
    }

    @Test
    fun downloadFailsOnServerError(wmRuntimeInfo: WireMockRuntimeInfo) = runTest {
        val serverError = "server-error.csv"

        val targetFilePath = "$temporaryDirectory/actual-server-error.csv"
        stubFor(get("/${serverError}").willReturn(serverError()))

        val result = SocrataDownloader().download(
            "http://localhost:${wmRuntimeInfo.httpPort}/${serverError}",
            targetFilePath // should not be created
        )

        assertFalse("Expected download to fail") { result }

        assertFalse("Expected target file to not be created") { File(targetFilePath).exists() }
    }

    @Test
    // Warning: this test is likely not working on Windows, as per the WireMock documentation: https://wiremock.org/docs/simulating-faults/#bad-responses
    fun downloadFailsOnConnectionResetByPeer(wmRuntimeInfo: WireMockRuntimeInfo) = runTest {
        val connectionReset = "connection-reset.csv"

        val targetFilePath = "$temporaryDirectory/connection-reset.csv"
        stubFor(
            get("/${connectionReset}")
                .willReturn(ok().withFault(Fault.MALFORMED_RESPONSE_CHUNK))
        )

        val result = SocrataDownloader().download(
            "http://localhost:${wmRuntimeInfo.httpPort}/${connectionReset}",
            targetFilePath // should not be created
        )

        assertFalse("Expected download to fail") { result }

        assertFalse("Expected target file to not be created") { File(targetFilePath).exists() }
    }

    @Test
    fun downloadClosesIOResourcesOnCompletion(wmRuntimeInfo: WireMockRuntimeInfo) = runTest {
        /**
         * This test unfortunately has to hard-codedly know the names of private fields in CsvFileDownloader.
         * This is far from ideal but still provides a practical way that streams and channels are properly closed after use.
         */

        val sourceCsvFilename = "socrata-downloader-source-data-2.csv"

        // WireMock expects files served by its underlying server to be under the directory "/resources/__files"
        // see https://github.com/wiremock/wiremock/blob/master/src/main/java/com/github/tomakehurst/wiremock/core/WireMockApp.java#L48
        val targetFilePath = "$temporaryDirectory/downloaded-bicycle-traffic-data.csv"

        stubFor(
            get(urlMatching("/${sourceCsvFilename}.*"))
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


        val downloader = SocrataDownloader()
        val result = downloader.download(
            "http://localhost:${wmRuntimeInfo.httpPort}/${sourceCsvFilename}",
            targetFilePath
        )

        assertTrue("Expected download to be successful") { result }

        assertThrows<IOException> {
            val writer = getPrivateClassField<SocrataDownloader, BufferedWriter>(downloader, "targetFileWriter")
            writer.newLine()
        }
    }

    companion object {
        private val temporaryDirectory = "${getResourcePath("/")}socrata_downloader_test_temp"

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