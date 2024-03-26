package download

import DataSourceConfig
import DownloadType
import HasResourcePathGetter.Companion.getResourcePath
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@WireMockTest
class CsvParallelDownloaderTest {

    @Test
    fun downloadCompletesSuccessfully(wmRuntimeInfo: WireMockRuntimeInfo) = runTest {

        stubFor(get("/url1").willReturn(ok().withFixedDelay(150)))
        stubFor(get("/url2").willReturn(ok().withFixedDelay(150)))
        stubFor(get("/url3").willReturn(ok().withFixedDelay(150)))

        val dataSourceConfigs = arrayListOf(
            DataSourceConfig(
                "does_not_matter",
                "http://localhost:${wmRuntimeInfo.httpPort}/url1",
                DownloadType.SINGLE_FILE,
                "does_not_matter",
                Class.forName("java.lang.String") // does not matter
            ),
            DataSourceConfig(
                "does_not_matter",
                "http://localhost:${wmRuntimeInfo.httpPort}/url2",
                DownloadType.SINGLE_FILE,
                "does_not_matter",
                Class.forName("java.lang.String") // does not matter
            ),
            DataSourceConfig(
                "does_not_matter",
                "http://localhost:${wmRuntimeInfo.httpPort}/url3",
                DownloadType.SINGLE_FILE,
                "does_not_matter",
                Class.forName("java.lang.String") // does not matter
            ),
        )

        val result = CsvParallelDownloader().downloadParallel(dataSourceConfigs)

        verify(1, getRequestedFor(urlEqualTo("/url1")))
        verify(1, getRequestedFor(urlEqualTo("/url2")))
        verify(1, getRequestedFor(urlEqualTo("/url3")))

        assertTrue { result }
    }

    @Test
    fun downloadReturnsFalseOnIndividualFailure(wmRuntimeInfo: WireMockRuntimeInfo) = runTest {

        stubFor(get("/url1").willReturn(ok().withFixedDelay(150)))
        stubFor(get("/url2").willReturn(serverError().withFixedDelay(150)))
        stubFor(get("/url3").willReturn(ok().withFixedDelay(150)))

        val dataSourceConfigs = arrayListOf(
            DataSourceConfig(
                "does_not_matter",
                "http://localhost:${wmRuntimeInfo.httpPort}/url1",
                DownloadType.SINGLE_FILE,
                "does_not_matter",
                Class.forName("java.lang.String") // does not matter
            ),
            DataSourceConfig(
                "does_not_matter",
                "http://localhost:${wmRuntimeInfo.httpPort}/url2",
                DownloadType.SINGLE_FILE,
                "does_not_matter",
                Class.forName("java.lang.String") // does not matter
            ),
            DataSourceConfig(
                "does_not_matter",
                "http://localhost:${wmRuntimeInfo.httpPort}/url3",
                DownloadType.SINGLE_FILE,
                "does_not_matter",
                Class.forName("java.lang.String") // does not matter
            ),
        )

        val result = CsvParallelDownloader().downloadParallel(dataSourceConfigs)

        verify(1, getRequestedFor(urlEqualTo("/url1")))
        verify(1, getRequestedFor(urlEqualTo("/url2")))
        verify(1, getRequestedFor(urlEqualTo("/url3")))

        assertFalse { result }
    }

    companion object {
        private val temporaryDirectory = "${getResourcePath("/")}csv_download_parallel_test_temp"

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