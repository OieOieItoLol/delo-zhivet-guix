package bot.domain.volunteer

import bot.support.toPgObject
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class VolunteerRepository(private val jdbcTemplate: NamedParameterJdbcTemplate, private val rowMapper: VolunteerRowMapper) {

    fun save(volunteer: Volunteer) {
        val params = MapSqlParameterSource()
        params.addValue("id", volunteer.id)
        params.addValue("tgName", volunteer.tgName)
        params.addValue("tgChannelId", volunteer.tgChannelId)
        params.addValue("registerDate", volunteer.registerDate)
        params.addValue("status", volunteer.status.dbValue)
        params.addValue("statusDate", volunteer.statusDate)
        params.addValue("isBanned", volunteer.isBanned)
        params.addValue("note", volunteer.note)
        params.addValue("coordinates", volunteer.coordinates?.toPgObject())
        params.addValue("radius", volunteer.radius)

        jdbcTemplate.update(saveQuery, params)
    }

    fun findByTgChannelId(tgChannelId: Long): Volunteer? {
        val params = MapSqlParameterSource()
        params.addValue("tgChannelId", tgChannelId)

        return jdbcTemplate.query(findByTgChannelIdQuery, params, rowMapper).singleOrNull()
    }

    fun findByTgName(tgName: String): Volunteer? {
        val params = MapSqlParameterSource()
        params.addValue("tgName", tgName)

        return jdbcTemplate.query(findByTgNameQuery, params, rowMapper).singleOrNull()
    }

    companion object {
        val saveQuery = """
            MERGE INTO volunteer AS v
            USING (
                VALUES (:id::bigint, :tgName, :tgChannelId, :registerDate, :status::volunteer_status, :statusDate, :isBanned, :note, :coordinates::geometry, :radius)
            ) AS vals(id, tg_name, tg_channel_id, register_date, status, status_date, is_banned, note, coordinates, radius)
            ON (v.id = vals.id)
            WHEN MATCHED THEN
                UPDATE SET
                    tg_name = vals.tg_name,
                    tg_channel_id = vals.tg_channel_id,
                    register_date = vals.register_date,
                    status = vals.status,
                    status_date = vals.status_date,
                    is_banned = vals.is_banned,
                    note = vals.note,
                    coordinates = vals.coordinates,
                    radius = vals.radius
            WHEN NOT MATCHED THEN
                INSERT (tg_name, tg_channel_id, register_date, status, status_date, is_banned, note, coordinates, radius)
                VALUES (vals.tg_name, vals.tg_channel_id, vals.register_date, vals.status, vals.status_date, vals.is_banned, vals.note, vals.coordinates, vals.radius)
        """.trimIndent()

        val findByTgChannelIdQuery = """
            SELECT id,
                   tg_name,
                   tg_channel_id,
                   register_date,
                   status,
                   status_date,
                   is_banned,
                   note,
                   ST_AsText(coordinates) AS coordinates,
                   radius
            FROM volunteer
            WHERE tg_channel_id = :tgChannelId
        """.trimIndent()

        val findByTgNameQuery = """
            SELECT id,
                   tg_name,
                   tg_channel_id,
                   register_date,
                   status,
                   status_date,
                   is_banned,
                   note,
                   ST_AsText(coordinates) AS coordinates,
                   radius
            FROM volunteer
            WHERE tg_name = :tgName
        """.trimIndent()
    }
}
