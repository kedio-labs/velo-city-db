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
import java.time.ZonedDateTime

class NantesCsvParserTest {

    @Test
    fun parseAndIngestThrowsIllegalArgumentExceptionOnEmptyString() {
        // given
        val ingestMock = mockk<(List<CityTrafficMeasurement>) -> Unit>()

        val actualException = assertThrows<IllegalArgumentException>(
            "Expected parse() to throw an illegal argument exception"
        ) {
            // when
            NantesCsvParser(ingestMock).parseAndIngest("")
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
            NantesCsvParser(ingestMock).parseAndIngest(getResourcePath("/data/nantes/nantes-test-comma.csv"))
        }

        verify { ingestMock wasNot Called }
    }

    @Test
    fun parseAndIngestSkipsRowWithEmptyDay() {
        // given
        val ingestMock = mockk<(List<CityTrafficMeasurement>) -> Unit>()

        every { ingestMock(any()) } answers {
            // then
            @Suppress("UNCHECKED_CAST")
            val actual = it.invocation.args.first() as List<CityTrafficMeasurement>
            // the test CSV file has two rows: (1) a row with an empty day field and (2) a valid row
            // we therefore expect only one row to be actually processed, which means having a resulting list with 24 hourly measurements

            assertEquals(24, actual.size)
        }

        // when
        NantesCsvParser(ingestMock).parseAndIngest(
            getResourcePath("/data/nantes/nantes-test-empty-day.csv")
        )
    }

    @Test
    fun parseAndIngestSkipsFieldWithInvalidHourlyTrafficCount() {
        // given
        val ingestMock = mockk<(List<CityTrafficMeasurement>) -> Unit>()

        every { ingestMock(any()) } answers {
            // then
            @Suppress("UNCHECKED_CAST")
            val actual = it.invocation.args.first() as List<CityTrafficMeasurement>

            // out of the two rows in the test data, we expect one field to be skipped. That means 2x24 - 1 = 47
            assertEquals(47, actual.size)
        }

        // when
        NantesCsvParser(ingestMock).parseAndIngest(
            getResourcePath("/data/nantes/nantes-test-invalid-hourly-traffic-count.csv")
        )
    }

    @Test
    fun parseAndIngestSkipsRowWithInvalidDay() {
        // given
        val ingestMock = mockk<(List<CityTrafficMeasurement>) -> Unit>()

        every { ingestMock(any()) } answers {
            // then
            @Suppress("UNCHECKED_CAST")
            val actual = it.invocation.args.first() as List<CityTrafficMeasurement>

            // out of the two rows in the test data, we expect the one with an invalid day to be skipped
            assertEquals(24, actual.size)
        }

        // when
        NantesCsvParser(ingestMock).parseAndIngest(getResourcePath("/data/nantes/nantes-test-invalid-day.csv"))
    }

    @Test
    fun parseAndIngestSetsEmptyHourlyTrafficCountToZero() {// given
        val ingestMock = mockk<(List<CityTrafficMeasurement>) -> Unit>()

        every { ingestMock(any()) } answers {
            // then
            @Suppress("UNCHECKED_CAST")
            val actual = it.invocation.args.first() as List<CityTrafficMeasurement>

            assertEquals(24, actual.size)
            actual.forEach { cityTrafficMeasurement -> assertEquals(0, cityTrafficMeasurement.hourlyTrafficCount) }
        }

        // when
        NantesCsvParser(ingestMock).parseAndIngest(
            getResourcePath("/data/nantes/nantes-test-empty-hourly-traffic-count.csv")
        )
    }

    @Test
    fun parseAndIngestReturnsListOfMeasurements() {
        val ingestMock = mockk<(List<CityTrafficMeasurement>) -> Unit>()

        every { ingestMock(any()) } answers {
            // then
            @Suppress("UNCHECKED_CAST")
            val actual = it.invocation.args.first() as List<CityTrafficMeasurement>

            assertEquals(48, actual.size)

            //@formatter:off
            val expectedResultFromFirstCsvRow = arrayListOf(2,1,0,0,0,0,3,5,17,18,7,16,13,14,12,9,15,18,10,12,3,2,3,0).mapIndexed { index: Int, hourlyTrafficCount: Int ->
                val paddedHour = if (index < 10) "0$index" else "$index"

                CityTrafficMeasurement(
                    "Nantes",
                    "Magellan vers Est",
                    hourlyTrafficCount,
                    ZonedDateTime.parse("2023-08-11T${paddedHour}:00:00+02:00").toInstant()
                )
            }

            val expectedResultFromSecondCsvRow = arrayListOf(20,7,12,5,5,11,42,135,411,213,92,111,214,168,139,147,158,288,334,249,198,95,51,131).mapIndexed { index: Int, hourlyTrafficCount: Int ->
                val paddedHour = if (index < 10) "0$index" else "$index"

                CityTrafficMeasurement(
                    "Nantes",
                    "50 Otages Vers Sud",
                    hourlyTrafficCount,
                    ZonedDateTime.parse("2021-06-23T${paddedHour}:00:00+02:00").toInstant()
                )
            }
            //@formatter:on

            assertEquals(
                expectedResultFromFirstCsvRow + expectedResultFromSecondCsvRow, actual
            )
        }

        // when
        NantesCsvParser(ingestMock).parseAndIngest(getResourcePath("/data/nantes/nantes-test.csv"))
    }

    @Test
    fun parseAndIngestSuccessfullyProcessesRowWithEmptyLocation() {
        val ingestMock = mockk<(List<CityTrafficMeasurement>) -> Unit>()

        every { ingestMock(any()) } answers {
            // then
            @Suppress("UNCHECKED_CAST")
            val actual = it.invocation.args.first() as List<CityTrafficMeasurement>

            assertEquals(48, actual.size)
        }

        // when
        NantesCsvParser(ingestMock).parseAndIngest(
            getResourcePath("/data/nantes/nantes-test-empty-location.csv")
        )
    }
}
