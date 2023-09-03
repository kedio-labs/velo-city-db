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

class BordeauxCsvParserTest {

    @Test
    fun parseAndIngestThrowsIllegalArgumentExceptionOnEmptyString() {
        // given
        val ingestMock = mockk<(List<CityTrafficMeasurement>) -> Unit>()

        val actualException =
            assertThrows<IllegalArgumentException>("Expected parse() to throw an illegal argument exception") {
                // when
                BordeauxCsvParser(ingestMock).parseAndIngest("")
            }

        // then
        verify { ingestMock wasNot Called }
        assertEquals("Relative file path must not be empty", actualException.message)
    }

    @Test
    fun parseAndIngestDoesNothingWhenCommaCsvDelimiter() {
        // given
        val ingestMock = mockk<(List<CityTrafficMeasurement>) -> Unit>()

        // when
        BordeauxCsvParser(ingestMock).parseAndIngest(
            getResourcePath("/data/bordeaux/bordeaux-test-comma.csv")
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
            assertEquals(1, actual.size)
        }

        // when
        BordeauxCsvParser(ingestMock).parseAndIngest(
            getResourcePath("/data/bordeaux/bordeaux-test-empty-date.csv")
        )

        verify { ingestMock(any()) }
    }

    @ParameterizedTest
    @ValueSource(strings = ["/data/bordeaux/bordeaux-test-invalid-hourly-traffic-count.csv", "/data/bordeaux/bordeaux-test-invalid-date.csv"])
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
        BordeauxCsvParser(ingestMock).parseAndIngest(getResourcePath(resourcePath))

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
        BordeauxCsvParser(ingestMock).parseAndIngest(
            getResourcePath("/data/bordeaux/bordeaux-test-empty-hourly-traffic-count.csv"),
        )

        verify { ingestMock(any()) }
    }

    @Test
    fun parseAndIngestSkipsRowsNotOfTypeBoucle() {
        // given
        val ingestMock = mockk<(List<CityTrafficMeasurement>) -> Unit>()

        every { ingestMock(any()) } answers {
            // then
            @Suppress("UNCHECKED_CAST")
            val actual = it.invocation.args.first() as List<CityTrafficMeasurement>
            assertEquals(1, actual.size)
        }

        // when
        BordeauxCsvParser(ingestMock).parseAndIngest(
            getResourcePath("/data/bordeaux/bordeaux-test-has-ponctuel.csv")
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
            assertEquals(5, actual.size)

            assertEquals(
                arrayListOf(
                    CityTrafficMeasurement(
                        "Bordeaux",
                        "Boulevard G. Pompidou vers Barrière d'Ornano",
                        9,
                        ZonedDateTime.parse("2023-02-18T07:00:00+01:00").toInstant()
                    ),
                    CityTrafficMeasurement(
                        "Bordeaux",
                        "Boulevard G. Pompidou vers Barrière d'Ornano",
                        45,
                        ZonedDateTime.parse("2023-02-18T09:00:00+01:00").toInstant()
                    ),
                    CityTrafficMeasurement(
                        "Bordeaux",
                        "Boulevard Antoine Gautier vers Cité des Pêcheurs",
                        43,
                        ZonedDateTime.parse("2023-05-09T20:00:00+02:00").toInstant()
                    ),
                    CityTrafficMeasurement(
                        "Bordeaux",
                        "49 Rue des Griffons vers Allende",
                        0,
                        ZonedDateTime.parse("2022-08-15T03:00:00+02:00").toInstant()
                    ),
                    CityTrafficMeasurement(
                        "Bordeaux",
                        "27 Cours Clémenceau vers rue Buffon",
                        124,
                        ZonedDateTime.parse("2023-07-07T12:00:00+02:00").toInstant()
                    )
                ),
                actual
            )
        }

        // when
        BordeauxCsvParser(ingestMock).parseAndIngest(
            getResourcePath("/data/bordeaux/bordeaux-test.csv"),
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
            assertEquals(2, actual.size)
        }

        // when
        BordeauxCsvParser(ingestMock).parseAndIngest(
            getResourcePath("/data/bordeaux/bordeaux-test-empty-location.csv"),
        )

        verify { ingestMock(any()) }
    }
}