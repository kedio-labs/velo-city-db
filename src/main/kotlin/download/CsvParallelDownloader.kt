package download

import DataSourceConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.util.concurrent.atomic.AtomicBoolean

private val logger = KotlinLogging.logger {}

class CsvParallelDownloader {

    suspend fun downloadParallel(dataSourceConfigs: List<DataSourceConfig>): Boolean = coroutineScope {
        val overallResult = AtomicBoolean(true)

        dataSourceConfigs.map {
            async {
                val result = UrlContentDownloader().download(it.url, it.targetFilePath)
                if (result) {
                    logger.info { "Successfully downloaded CSV file for city ${it.city} from URL ${it.url} to target path ${it.targetFilePath}" }
                } else {
                    logger.warn { "Could not download CSV file for city ${it.city} from URL ${it.url} to target path ${it.targetFilePath}" }
                    overallResult.set(false)
                }
            }
        }.awaitAll()

        return@coroutineScope overallResult.get()
    }
}