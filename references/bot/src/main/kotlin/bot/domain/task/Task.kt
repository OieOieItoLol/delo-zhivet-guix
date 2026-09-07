package bot.domain.task

import org.locationtech.jts.geom.Geometry
import java.time.LocalDateTime

data class Task(
    val id: Int? = null,
    val type: TaskType,
    val name: String,
    val createDate: LocalDateTime,
    val status: TaskStatus,
    val statusDate: LocalDateTime,
    val eventDatetimeUtc: LocalDateTime? = null,
    val leader: Int? = null,
    val chatLink: String?,
    val note: String?,
    val coordinates: Geometry?,
    val address: String? = null,
    val contact: String? = null
)

enum class TaskType(val dbValue: String) {
    SOCIAL("Social"),
    ECOLOGICAL("Ecological");

    companion object {
        fun fromDbValue(value: String): TaskType = entries.firstOrNull { it.dbValue.equals(value, ignoreCase = true) }
            ?: throw IllegalArgumentException("Unknown TaskType value: $value")
    }
}

enum class TaskStatus(val dbValue: String) {
    NEW("New"),
    IN_PROCESSING("InProcessing"),
    IN_PLAN("InPlan"),
    DONE("Done"),
    INACTIVE("Inactive"),
    ARCHIVE("Archive");

    companion object {
        fun fromDbValue(value: String): TaskStatus = entries.firstOrNull { it.dbValue.equals(value, ignoreCase = true) }
            ?: throw IllegalArgumentException("Unknown TaskStatus value: $value")
    }
}
