import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import parse.BordeauxCsvParser
import parse.NantesCsvParser
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DataSourcesConfigLoaderTest {

    @Test
    fun loadThrowsAnExceptionWhenFileDoesNotExist() {
        assertThrows<IllegalStateException> { DataSourcesConfigLoader().load("invalid_file_path", "does_not_matter") }
    }

    @Test
    fun loadThrowsAnExceptionWhenYamlFileInvalid() {
        assertThrows<RuntimeException> {
            DataSourcesConfigLoader().load(
                "/data-sources/data-sources-invalid.yml",
                "does_not_matter"
            )
        }
    }

    @Test
    fun loadThrowsAnExceptionWhenDataSourceConfigPathEmpty() {
        assertThrows<IllegalArgumentException> {
            DataSourcesConfigLoader().load("", "does_not_matter")
        }
    }

    @Test
    fun loadThrowsAnExceptionWhenTargetDirectoryEmpty() {
        assertThrows<IllegalArgumentException> {
            DataSourcesConfigLoader().load("does_not_matter", "")
        }
    }

    @Test
    fun loadThrowsAnExceptionWhenDownloadTypeInvalid() {
        assertThrows<IllegalArgumentException> {
            DataSourcesConfigLoader().load(
                "/data-sources/data-sources-invalid-download-type.yml",
                "does_not_matter"
            )
        }
    }

    @Test
    fun loadReturnsEmptyMapWhenYamlFileEmpty() {
        val actual = DataSourcesConfigLoader().load("/data-sources/data-sources-empty.yml", "does_not_matter")

        assertTrue { actual.isEmpty() }
    }

    @Test
    fun loadIgnoresCitiesThatHaveNoCsvParser() {
        val expectedCsvTargetFilesDirectoryAbsolutePath = "/some/absolute/directory/path"
        val actual =
            DataSourcesConfigLoader().load(
                "/data-sources/data-sources-with-cities-that-have-no-csv-parser.yml",
                expectedCsvTargetFilesDirectoryAbsolutePath
            )

        assertEquals(
            arrayListOf(
                DataSourceConfig(
                    "Bordeaux",
                    "https://bordeaux-url",
                    DownloadType.SINGLE_FILE,
                    "${expectedCsvTargetFilesDirectoryAbsolutePath}/Bordeaux.csv",
                    BordeauxCsvParser {}.javaClass
                ),
                DataSourceConfig(
                    "Nantes",
                    "https://nantes-url",
                    DownloadType.SINGLE_FILE,
                    "${expectedCsvTargetFilesDirectoryAbsolutePath}/Nantes.csv",
                    NantesCsvParser {}.javaClass
                )
            ), actual
        )
    }
}