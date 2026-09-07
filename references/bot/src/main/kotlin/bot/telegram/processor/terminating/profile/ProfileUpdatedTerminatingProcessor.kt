package bot.telegram.processor.terminating.profile

import bot.domain.volunteer.VolunteerService
import bot.telegram.processor.terminating.TerminatingProcessor
import bot.telegram.state.machine.State
import bot.telegram.state.machine.context.BotContext
import bot.telegram.state.machine.context.ProfileMapper
import bot.telegram.state.machine.context.ProfileValid
import bot.telegram.state.machine.context.profileValidator
import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class ProfileUpdatedTerminatingProcessor(
    private val botContext: BotContext,
    private val profileMapper: ProfileMapper,
    private val volunteerService: VolunteerService
) : TerminatingProcessor {
    override fun invoke(update: Update) {
        val chatId = update.message.chatId
        val userContext = botContext.getOrCreate(chatId)
        val validatedProfile = profileValidator.invoke(userContext.profile)
        if (validatedProfile is ProfileValid) {
            profileMapper.invoke(validatedProfile, chatId)
                .let(volunteerService::update)
        }

        log.info("User data: $validatedProfile")
        userContext.clear()
    }

    override fun getType(): State = State.ProfileUpdated

    companion object {
        private val log = KotlinLogging.logger {}
    }
}
