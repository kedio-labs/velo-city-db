package parse

import CityTrafficMeasurement
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

interface CsvParser {

    val ingest: (List<CityTrafficMeasurement>) -> Unit
    val batch: MutableList<CityTrafficMeasurement>

    /**
     * Ingests given batch of CityTrafficMeasurement into the database
     * @param batch batch to ingest
     */
    fun ingestBatch(
        batch: MutableList<CityTrafficMeasurement>, force: Boolean = false
    ) {
        if (batch.size > 0) {
            if (force || batch.size >= INGESTION_BATCH_SIZE) {
                logger.debug { "Ingesting batch" }
                ingest(batch)
                batch.clear()
            }
        }
    }

    /**
     * Parses a given CSV file and ingests into DB.
     * Implementation code would typically ingest in batches. See BATCH_SIZE.
     *
     * @param absoluteFilePath CSV file path relative to the "resources" directory.
     */
    fun parseAndIngest(absoluteFilePath: String)

    /**
     * Validates filepath.
     *
     * @param absoluteFilePath filepath to validate
     */
    fun validateRelativeFilePath(absoluteFilePath: String) {
        if (absoluteFilePath.trim() == "") {
            throw IllegalArgumentException("Relative file path must not be empty")
        }
    }

    companion object {
        private const val INGESTION_BATCH_SIZE = 5000
    }
}