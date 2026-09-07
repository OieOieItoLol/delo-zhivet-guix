package bot.domain.photo

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient

@Service
class PhotoService(@Value("\${app.backend.url}") backendUrl: String, @Value("\${app.backend.token}") backendToken: String) {

    private val webClient = WebClient.builder()
        .baseUrl(backendUrl)
        .defaultHeader("Authorization", backendToken)
        .build()

    fun uploadPhoto(fileContent: ByteArray, fileName: String, taskId: Int) {
        val builder = MultipartBodyBuilder()
        builder.part("file", ByteArrayResource(fileContent))
            .header("Content-Disposition", "form-data; name=file; filename=$fileName.jpg")
            .header("Content-Type", "image/jpeg")
        val body = BodyInserters.fromMultipartData(builder.build())

        webClient.post()
            .uri { uriBuilder ->
                uriBuilder.path("/api/bot/photo/upload")
                    .queryParam("taskId", taskId)
                    .build()
            }
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(body)
            .retrieve()
            .toBodilessEntity()
            .block()
    }
}
