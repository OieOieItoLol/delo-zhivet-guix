package bot.domain.volunteer

import org.locationtech.jts.geom.Geometry
import java.time.LocalDateTime

data class Volunteer(
    val id: Int? = null,
    val tgName: String,
    val tgChannelId: Long,
    val registerDate: LocalDateTime,
    val status: VolunteerStatus,
    val statusDate: LocalDateTime,
    val isBanned: Boolean,
    val note: String? = null,
    val coordinates: Geometry?,
    val radius: Int?
)

enum class VolunteerStatus(val dbValue: String) {
    ACTIVE("Active"),
    VACATION("Vacation");

    companion object {
        fun fromDbValue(value: String): VolunteerStatus = entries.firstOrNull { it.dbValue.equals(value, ignoreCase = true) }
            ?: throw IllegalArgumentException("Unknown enum value: $value")
    }
}
