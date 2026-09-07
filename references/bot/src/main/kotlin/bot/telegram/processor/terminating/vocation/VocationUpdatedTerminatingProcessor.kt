package bot.telegram.processor.terminating.vocation

import bot.domain.volunteer.VolunteerService
import bot.domain.volunteer.VolunteerStatus
import bot.telegram.processor.terminating.TerminatingProcessor
import bot.telegram.state.machine.State
import bot.telegram.state.machine.context.BotContext
import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class VocationUpdatedTerminatingProcessor(private val botContext: BotContext, private val volunteerService: VolunteerService) :
    TerminatingProcessor {
    override fun invoke(update: Update) {
        val chatId = update.message.chatId
        val userContext = botContext.getOrCreate(chatId)
        userContext.isVocation?.let { isVocation ->
            val volunteerStatus = if (isVocation) VolunteerStatus.VACATION else VolunteerStatus.ACTIVE
            volunteerService.setStatus(chatId, volunteerStatus)
        }

        log.info("VACATION data: ${userContext.isVocation}.isVocation")
        userContext.clear()
    }

    override fun getType(): State = State.VocationUpdated

    companion object {
        private val log = KotlinLogging.logger {}
    }
}
