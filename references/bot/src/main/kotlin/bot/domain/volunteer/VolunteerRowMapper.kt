package bot.domain.volunteer

import bot.support.GeometrySupport
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Component
import java.sql.ResultSet

@Component
class VolunteerRowMapper : RowMapper<Volunteer> {

    override fun mapRow(rs: ResultSet, rowNum: Int): Volunteer? = Volunteer(
        id = rs.getInt("id"),
        tgName = rs.getString("tg_name"),
        tgChannelId = rs.getLong("tg_channel_id"),
        registerDate = rs.getTimestamp("register_date").toLocalDateTime(),
        status = VolunteerStatus.fromDbValue(rs.getString("status")),
        statusDate = rs.getTimestamp("status_date").toLocalDateTime(),
        isBanned = rs.getBoolean("is_banned"),
        note = rs.getString("note"),
        coordinates = rs.getString("coordinates")?.let { GeometrySupport.createFromString(it) },
        radius = rs.getObject("radius") as? Int
    )
}
