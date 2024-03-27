package download

interface Downloader {
    suspend fun download(url: String, targetFilePath: String): Boolean
}