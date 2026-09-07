package bot.support

import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.telegram.telegrambots.meta.api.objects.PhotoSize

object TelegramPhotoSupport {

    private val webClient = WebClient.create()

    fun findPhotoWithMaxResolution(photos: List<PhotoSize>): PhotoSize = photos.maxByOrNull { it.fileSize } ?: photos.last()

    fun download(botToken: String, fileId: String): ByteArray? {
        val photoInfoUrl = "https://api.telegram.org/bot$botToken/getFile?file_id=$fileId"
        val photoInfo = webClient.get()
            .uri(photoInfoUrl)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(TelegramPhotoInfo::class.java)
            .block()

        val downloadPhotoUrl = "https://api.telegram.org/file/bot$botToken/${photoInfo?.result?.filePath}"
        return webClient.get()
            .uri(downloadPhotoUrl)
            .retrieve()
            .bodyToMono(ByteArray::class.java)
            .block()
    }
}
