package bot.support

import com.fasterxml.jackson.annotation.JsonProperty

data class TelegramPhotoInfo(val result: TelegramPhotoInfoResult)

data class TelegramPhotoInfoResult(
    @JsonProperty("file_id")
    val fileId: String,

    @JsonProperty("file_unique_id")
    val fileUniqueId: String,

    @JsonProperty("file_size")
    val fileSize: Int,

    @JsonProperty("file_path")
    val filePath: String
)
