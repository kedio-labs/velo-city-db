package parse

import CityTrafficMeasurement
import HasResourcePathGetter.Companion.getResourcePath
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.time.ZonedDateTime

class RennesCsvParserTest {

    @Test
    fun parseAndIngestThrowsIllegalArgumentExceptionOnEmptyString() {
        // given
        val ingestMock = mockk<(List<CityTrafficMeasurement>) -> Unit>()

        val actualException =
            assertThrows<IllegalArgumentException>("Expected parse() to throw an illegal argument exception") {
                // when
                RennesCsvParser(ingestMock).parseAndIngest("")
            }

        // then
        verify { ingestMock wasNot Called }
        Assertions.assertEquals("Relative file path must not be empty", actualException.message)
    }

    @Test
    fun parseAndIngestDoesNothingWhenCommaCsvDelimiter() {
        // given
        val ingestMock = mockk<(List<CityTrafficMeasurement>) -> Unit>()

        // when
        RennesCsvParser(ingestMock).parseAndIngest(
            getResourcePath("/data/rennes/rennes-test-comma.csv")
        )

        // then
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
            Assertions.assertEquals(1, actual.size)
        }

        // when
        RennesCsvParser(ingestMock).parseAndIngest(
            getResourcePath("/data/rennes/rennes-test-empty-date.csv")
        )

        verify { ingestMock(any()) }
    }

    @ParameterizedTest
    @ValueSource(strings = ["/data/rennes/rennes-test-invalid-hourly-traffic-count.csv", "/data/rennes/rennes-test-invalid-date.csv"])
    fun parseAndIngestSkipsRowWithInvalidField(resourcePath: String) {
        // given
        val ingestMock = mockk<(List<CityTrafficMeasurement>) -> Unit>()

        every { ingestMock(any()) } answers {
            // then
            @Suppress("UNCHECKED_CAST")
            val actual = it.invocation.args.first() as List<CityTrafficMeasurement>
            Assertions.assertEquals(1, actual.size)
        }

        // when
        RennesCsvParser(ingestMock).parseAndIngest(getResourcePath(resourcePath))

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
            Assertions.assertEquals(2, actual.size)
            actual.forEach { cityTrafficMeasurement ->
                Assertions.assertEquals(
                    0,
                    cityTrafficMeasurement.hourlyTrafficCount
                )
            }
        }

        // when
        RennesCsvParser(ingestMock).parseAndIngest(
            getResourcePath("/data/rennes/rennes-test-empty-hourly-traffic-count.csv"),
        )

        verify { ingestMock(any()) }
    }

    @Test
    fun parseAndIngestIngestsListOfMeasurements() {
        // given
        val ingestMock = mockk<(List<CityTrafficMeasurement>) -> Unit>()

        every { ingestMock(any()) } answers {
            // then
            @Suppress("UNCHECKED_CAST")
            val actual = it.invocation.args.first() as List<CityTrafficMeasurement>
            Assertions.assertEquals(5, actual.size)

            Assertions.assertEquals(
                arrayListOf(
                    CityTrafficMeasurement(
                        "Rennes",
                        "Rennes Rue d'Isly V1",
                        4,
                        ZonedDateTime.parse("2020-11-27T01:00:00+01:00").toInstant()
                    ),
                    CityTrafficMeasurement(
                        "Rennes",
                        "Rennes Rue d'Isly V1",
                        0,
                        ZonedDateTime.parse("2020-11-27T02:00:00+01:00").toInstant()
                    ),
                    CityTrafficMeasurement(
                        "Rennes",
                        "Eco-Display Place de Bretagne",
                        178,
                        ZonedDateTime.parse("2021-10-27T22:00:00+02:00").toInstant()
                    ),
                    CityTrafficMeasurement(
                        "Rennes",
                        "Eco-Display Place de Bretagne",
                        184,
                        ZonedDateTime.parse("2021-10-28T11:00:00+02:00").toInstant()
                    ),
                    CityTrafficMeasurement(
                        "Rennes",
                        "Eco-Display Place de Bretagne",
                        175,
                        ZonedDateTime.parse("2021-10-28T15:00:00+02:00").toInstant()
                    )
                ),
                actual
            )
        }

        // when
        RennesCsvParser(ingestMock).parseAndIngest(
            getResourcePath("/data/rennes/rennes-test.csv"),
        )

        verify { ingestMock(any()) }
    }

    @Test
    fun parseAndIngestSuccessfullyProcessesRowWithEmptyLocation() {
        // given
        val ingestMock = mockk<(List<CityTrafficMeasurement>) -> Unit>()

        every { ingestMock(any()) } answers {
            // then
            @Suppress("UNCHECKED_CAST")
            val actual = it.invocation.args.first() as List<CityTrafficMeasurement>
            Assertions.assertEquals(2, actual.size)
        }

        // when
        RennesCsvParser(ingestMock).parseAndIngest(
            getResourcePath("/data/rennes/rennes-test-empty-location.csv"),
        )

        verify { ingestMock(any()) }
    }
}