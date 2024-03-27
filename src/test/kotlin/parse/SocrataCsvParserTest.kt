package parse

import CityTrafficMeasurement
import HasResourcePathGetter.Companion.getResourcePath
import SocrataCityInfo
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.time.LocalDateTime
import java.time.ZoneId

class SocrataCsvParserTest {

    @Test
    fun parseAndIngestThrowsIllegalArgumentExceptionOnEmptyString() {
        // given
        val ingestMock = mockk<(List<CityTrafficMeasurement>) -> Unit>()

        val actualException = assertThrows<IllegalArgumentException>(
            "Expected parse() to throw an illegal argument exception"
        ) {
            // when
            SocrataCsvParser(ingestMock, socrataCityInfo).parseAndIngest("")
        }

        // then
        verify { ingestMock wasNot Called }
        Assertions.assertEquals("File path must not be empty", actualException.message)
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
        SocrataCsvParser(ingestMock, socrataCityInfo).parseAndIngest(
            getResourcePath("/data/socrata/socrata-test-empty-epoch.csv")
        )

        verify { ingestMock(any()) }
    }

    @ParameterizedTest
    @ValueSource(strings = ["/data/socrata/socrata-test-invalid-epoch.csv", "/data/socrata/socrata-test-invalid-hourly-traffic-count.csv"])
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
        SocrataCsvParser(ingestMock, socrataCityInfo).parseAndIngest(getResourcePath(resourcePath))

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
        SocrataCsvParser(ingestMock, socrataCityInfo).parseAndIngest(
            getResourcePath("/data/socrata/socrata-test-empty-hourly-traffic-count.csv"),
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
            Assertions.assertEquals(2, actual.size)

            Assertions.assertEquals(
                arrayListOf(
                    CityTrafficMeasurement(
                        "SomeCity",
                        "S46_YorkWayRd_path_LHS_cam",
                        1,
                        LocalDateTime.parse("2024-02-04T22:00:00.000").atZone(socrataCityInfo.zoneId)
                            .toInstant()
                    ),
                    CityTrafficMeasurement(
                        "SomeCity",
                        "s57_CamdenParkRd_road_cam",
                        33,
                        LocalDateTime.parse("2024-02-04T19:00:00.000").atZone(socrataCityInfo.zoneId)
                            .toInstant()
                    )
                ),
                actual
            )
        }

        // when
        SocrataCsvParser(ingestMock, socrataCityInfo).parseAndIngest(
            getResourcePath("/data/socrata/socrata-test.csv"),
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
        SocrataCsvParser(ingestMock, socrataCityInfo).parseAndIngest(
            getResourcePath("/data/socrata/socrata-test-empty-location.csv"),
        )

        verify { ingestMock(any()) }
    }

    companion object {
        private val socrataCityInfo = SocrataCityInfo("SomeCity", ZoneId.of("Europe/London"))
    }
}