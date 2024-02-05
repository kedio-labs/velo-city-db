package parse

import CityTrafficMeasurement
import HasResourcePathGetter.Companion.getResourcePath
import io.mockk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.time.ZonedDateTime

class StrasbourgCsvParserTest {

    @Test
    fun parseAndIngestThrowsIllegalArgumentExceptionOnEmptyString() {
        // given
        val ingestMock = mockk<(List<CityTrafficMeasurement>) -> Unit>()

        val actualException = assertThrows<IllegalArgumentException>(
            "Expected parse() to throw an illegal argument exception"
        ) {
            // when
            StrasbourgCsvParser(ingestMock).parseAndIngest("")
        }

        // then
        verify { ingestMock wasNot Called }
        assertEquals("File path must not be empty", actualException.message)
    }

    @Test
    fun parseAndIngestReturnsEmptyResultWithCommaCsvDelimiter() {
        // given
        val ingestMock = mockk<(List<CityTrafficMeasurement>) -> Unit>()

        // when
        StrasbourgCsvParser(ingestMock).parseAndIngest(
            getResourcePath("/data/strasbourg/strasbourg-test-comma.csv")
        )

        verify { ingestMock wasNot called }
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
        StrasbourgCsvParser(ingestMock).parseAndIngest(
            getResourcePath("/data/strasbourg/strasbourg-test-empty-date.csv")
        )

        verify { ingestMock(any()) }
    }

    @ParameterizedTest
    @ValueSource(strings = ["/data/strasbourg/strasbourg-test-invalid-sum-counts.csv", "/data/strasbourg/strasbourg-test-invalid-date.csv"])
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
        StrasbourgCsvParser(ingestMock).parseAndIngest(getResourcePath(resourcePath))

        verify { ingestMock(any()) }
    }

    @Test
    fun parseAndIngestSetsEmptySumCountToZero() {
        // given
        val ingestMock = mockk<(List<CityTrafficMeasurement>) -> Unit>()

        every { ingestMock(any()) } answers {
            // then
            @Suppress("UNCHECKED_CAST")
            val actual = it.invocation.args.first() as List<CityTrafficMeasurement>

            assertEquals(2, actual.size)
            actual.forEach { cityTrafficMeasurement -> assertEquals(0, cityTrafficMeasurement.hourlyTrafficCount) }
        }

        // when
        StrasbourgCsvParser(ingestMock).parseAndIngest(
            getResourcePath("/data/strasbourg/strasbourg-test-empty-sum-counts.csv")
        )

        verify { ingestMock(any()) }
    }

    @Test
    fun parseAndIngestReturnsListOfMeasurements() {
        // given
        val ingestMock = mockk<(List<CityTrafficMeasurement>) -> Unit>()

        every { ingestMock(any()) } answers {
            // then
            @Suppress("UNCHECKED_CAST")
            val actual = it.invocation.args.first() as List<CityTrafficMeasurement>

            assertEquals(5, actual.size)

            assertEquals(
                arrayListOf(
                    CityTrafficMeasurement(
                        "Strasbourg",
                        "200000390",
                        0,
                        ZonedDateTime.parse("2022-01-01T00:00:00+00:00").toInstant()
                    ),
                    CityTrafficMeasurement(
                        "Strasbourg",
                        "200000399",
                        6,
                        ZonedDateTime.parse("2022-01-01T00:00:00+00:00").toInstant()
                    ),
                    CityTrafficMeasurement(
                        "Strasbourg",
                        "1145",
                        31,
                        ZonedDateTime.parse("2023-08-18T14:00:00+00:00").toInstant()
                    ),
                    CityTrafficMeasurement(
                        "Strasbourg",
                        "1148",
                        3,
                        ZonedDateTime.parse("2023-08-18T14:00:00+00:00").toInstant()
                    ),
                    CityTrafficMeasurement(
                        "Strasbourg",
                        "1149",
                        5,
                        ZonedDateTime.parse("2023-08-18T14:00:00+00:00").toInstant()
                    )
                ),
                actual
            )
        }

        // when
        StrasbourgCsvParser(ingestMock).parseAndIngest(
            getResourcePath("/data/strasbourg/strasbourg-test.csv")
        )

        verify { ingestMock(any()) }
    }

    @Test
    fun parseAndIngestSuccessfullyProcessesRowWithEmptyLocation() {// given
        val ingestMock = mockk<(List<CityTrafficMeasurement>) -> Unit>()

        every { ingestMock(any()) } answers {
            // then
            @Suppress("UNCHECKED_CAST")
            val actual = it.invocation.args.first() as List<CityTrafficMeasurement>

            assertEquals(2, actual.size)
        }

        // when
        StrasbourgCsvParser(ingestMock).parseAndIngest(
            getResourcePath("/data/strasbourg/strasbourg-test-empty-location.csv")
        )

        verify { ingestMock(any()) }
    }
}