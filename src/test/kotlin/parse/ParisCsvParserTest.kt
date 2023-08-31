package parse

import CityTrafficMeasurement
import HasResourcePathGetter.Companion.getResourcePath
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.time.ZonedDateTime

class ParisCsvParserTest {

    @Test
    fun parseAndIngestThrowsIllegalArgumentExceptionOnEmptyString() {
        // given
        val ingestMock = mockk<(List<CityTrafficMeasurement>) -> Unit>()

        val actualException = assertThrows<IllegalArgumentException>(
            "Expected parse() to throw an illegal argument exception"
        ) {
            // when
            ParisCsvParser(ingestMock).parseAndIngest("")
        }

        // then
        verify { ingestMock wasNot Called }
        assertEquals("Relative file path must not be empty", actualException.message)
    }

    @Test
    fun parseAndIngestFailsWithCommaCsvDelimiter() {
        // given
        val ingestMock = mockk<(List<CityTrafficMeasurement>) -> Unit>()

        // then
        assertThrows<RuntimeException> {
            // when
            ParisCsvParser(ingestMock).parseAndIngest(getResourcePath("/data/paris/paris-test-comma.csv"))
        }

        verify { ingestMock wasNot Called }
    }

    @Test
    fun parseAndIngestSkipsRowWithEmptyDate() {
        // given
        val ingestMock = mockk<(List<CityTrafficMeasurement>) -> Unit>()

        every { ingestMock(any()) } answers {
            // then
            @Suppress("UNCHECKED_CAST")
            val actual = it.invocation.args.first() as List<CityTrafficMeasurement>

            assertEquals(1, actual.size)
        }

        // when
        ParisCsvParser(ingestMock).parseAndIngest(getResourcePath("/data/paris/paris-test-empty-date.csv"))
    }

    @ParameterizedTest
    @ValueSource(strings = ["/data/paris/paris-test-invalid-hourly-traffic-count.csv", "/data/paris/paris-test-invalid-date.csv"])
    fun parseAndIngestSkipsRowWithInvalidField(resourcePath: String) {
        // given
        val ingestMock = mockk<(List<CityTrafficMeasurement>) -> Unit>()

        every { ingestMock(any()) } answers {
            // then
            @Suppress("UNCHECKED_CAST")
            val actual = it.invocation.args.first() as List<CityTrafficMeasurement>

            assertEquals(1, actual.size)
        }

        // when
        ParisCsvParser(ingestMock).parseAndIngest(getResourcePath(resourcePath))
    }

    @Test
    fun parseAndIngestSetsEmptyTrafficCountToZero() {
        val ingestMock = mockk<(List<CityTrafficMeasurement>) -> Unit>()

        every { ingestMock(any()) } answers {
            // then
            @Suppress("UNCHECKED_CAST")
            val actual = it.invocation.args.first() as List<CityTrafficMeasurement>

            assertEquals(2, actual.size)

            actual.forEach { cityTrafficMeasurement -> assertEquals(0, cityTrafficMeasurement.hourlyTrafficCount) }
        }

        // when
        ParisCsvParser(ingestMock).parseAndIngest(
            getResourcePath("/data/paris/paris-test-empty-hourly-traffic-count.csv")
        )
    }

    @Test
    fun parseAndIngestReturnsListOfMeasurements() {
        val ingestMock = mockk<(List<CityTrafficMeasurement>) -> Unit>()

        every { ingestMock(any()) } answers {
            // then
            @Suppress("UNCHECKED_CAST")
            val actual = it.invocation.args.first() as List<CityTrafficMeasurement>

            assertEquals(5, actual.size)

            assertEquals(
                arrayListOf(
                    CityTrafficMeasurement(
                        "Paris",
                        "97 avenue Denfert Rochereau SO-NE",
                        68,
                        ZonedDateTime.parse("2022-07-01T07:00:00+02:00").toInstant()
                    ),
                    CityTrafficMeasurement(
                        "Paris",
                        "97 avenue Denfert Rochereau SO-NE",
                        224,
                        ZonedDateTime.parse("2022-07-01T08:00:00+02:00").toInstant()
                    ),
                    CityTrafficMeasurement(
                        "Paris",
                        "Face au 49 boulevard du Général Martial Valin NO-SE",
                        26,
                        ZonedDateTime.parse("2023-08-12T14:00:00+02:00").toInstant()
                    ),
                    CityTrafficMeasurement(
                        "Paris",
                        "Face au 49 boulevard du Général Martial Valin NO-SE",
                        30,
                        ZonedDateTime.parse("2023-08-12T17:00:00+02:00").toInstant()
                    ),
                    CityTrafficMeasurement(
                        "Paris",
                        "Face au 49 boulevard du Général Martial Valin NO-SE",
                        26,
                        ZonedDateTime.parse("2023-08-12T19:00:00+02:00").toInstant()
                    )
                ),
                actual
            )
        }

        // when
        ParisCsvParser(ingestMock).parseAndIngest(getResourcePath("/data/paris/paris-test.csv"))
    }

    @Test
    fun parseAndIngestSuccessfullyProcessesRowWithEmptyLocation() {
        val ingestMock = mockk<(List<CityTrafficMeasurement>) -> Unit>()

        every { ingestMock(any()) } answers {
            // then
            @Suppress("UNCHECKED_CAST")
            val actual = it.invocation.args.first() as List<CityTrafficMeasurement>

            assertEquals(2, actual.size)
        }

        // when
        ParisCsvParser(ingestMock).parseAndIngest(
            getResourcePath("/data/paris/paris-test-empty-location.csv")
        )
    }
}
