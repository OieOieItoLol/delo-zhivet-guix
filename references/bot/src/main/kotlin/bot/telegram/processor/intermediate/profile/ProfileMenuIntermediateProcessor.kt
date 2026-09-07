package bot.telegram.processor.intermediate.profile

import bot.telegram.processor.intermediate.IntermediateProcessor
import bot.telegram.state.machine.State
import bot.telegram.state.machine.context.BotContext
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class ProfileMenuIntermediateProcessor(private val botContext: BotContext) : IntermediateProcessor {

    override fun invoke(update: Update) {
        val userContext = botContext.getOrCreate(update.message.chatId)
        userContext.profile = userContext.profile.copy(userName = update.message.from.userName)
    }

    override fun getType(): State = State.ProfileMenu
}
