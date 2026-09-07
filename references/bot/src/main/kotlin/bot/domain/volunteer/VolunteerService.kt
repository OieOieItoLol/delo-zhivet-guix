package bot.domain.volunteer

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class VolunteerService(private val repository: VolunteerRepository) {

    @Transactional
    fun update(volunteer: Volunteer) {
        val volunteerToSave = repository.findByTgChannelId(volunteer.tgChannelId)?.let {
            volunteer.copy(
                id = it.id,
                registerDate = it.registerDate,
                isBanned = it.isBanned
            )
        } ?: volunteer

        repository.save(volunteerToSave)
    }

    @Transactional
    fun setStatus(chatId: Long, volunteerStatus: VolunteerStatus) {
        repository.findByTgChannelId(chatId)?.let { record ->
            record.copy(status = volunteerStatus).also { repository.save(it) }
        }
    }

    fun findByTgName(tgName: String): Volunteer? = repository.findByTgName(tgName)
}
