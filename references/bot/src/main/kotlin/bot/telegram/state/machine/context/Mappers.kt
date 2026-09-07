package bot.telegram.state.machine.context

import bot.domain.task.Task
import bot.domain.task.TaskStatus
import bot.domain.task.TaskType
import bot.domain.volunteer.Volunteer
import bot.domain.volunteer.VolunteerStatus
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.LocalDateTime

@Component
class ProfileMapper(private val clock: Clock) : (ProfileValid, Long) -> Volunteer {

    override fun invoke(profile: ProfileValid, chatId: Long): Volunteer = with(profile) {
        Volunteer(
            tgName = "@$userName",
            tgChannelId = chatId,
            registerDate = LocalDateTime.now(clock),
            status = VolunteerStatus.ACTIVE,
            statusDate = LocalDateTime.now(clock),
            isBanned = false,
            coordinates = profile.coordinates,
            radius = activeRadius.value
        )
    }
}

@Component
class EcologicalProblemMapper(private val clock: Clock) : (EcologicalProblemValid, String, String?) -> Task {

    override fun invoke(ecologicalProblem: EcologicalProblemValid, name: String, userName: String?): Task = with(ecologicalProblem) {
        Task(
            type = TaskType.ECOLOGICAL,
            name = name,
            createDate = LocalDateTime.now(clock),
            status = TaskStatus.NEW,
            statusDate = LocalDateTime.now(clock),
            chatLink = userName?.let { "https://t.me/$it" },
            note = commentary,
            coordinates = coordinates,
            contact = contact
        )
    }
}

@Component
class SocialProblemMapper(private val clock: Clock) : (SocialProblemValid, String, String?) -> Task {

    override fun invoke(socialProblem: SocialProblemValid, name: String, userName: String?): Task = with(socialProblem) {
        Task(
            type = TaskType.SOCIAL,
            name = name,
            createDate = LocalDateTime.now(clock),
            status = TaskStatus.NEW,
            statusDate = LocalDateTime.now(clock),
            chatLink = userName?.let { "https://t.me/$it" },
            note = commentary,
            coordinates = coordinates,
            address = address,
            contact = contact
        )
    }
}
