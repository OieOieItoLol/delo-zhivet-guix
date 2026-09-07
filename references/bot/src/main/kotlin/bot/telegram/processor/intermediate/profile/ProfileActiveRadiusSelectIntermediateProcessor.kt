package bot.telegram.processor.intermediate.profile

import bot.telegram.command.profile.ActiveRadius
import bot.telegram.processor.intermediate.IntermediateProcessor
import bot.telegram.state.machine.State
import bot.telegram.state.machine.context.BotContext
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class ProfileActiveRadiusSelectIntermediateProcessor(private val botContext: BotContext) : IntermediateProcessor {

    override fun invoke(update: Update) {
        val userContext = botContext.getOrCreate(update.message.chatId)
        val activeRadius = ActiveRadius.findByText(update.message.text)
        userContext.profile = userContext.profile.copy(activeRadius = activeRadius)
    }

    override fun getType(): State = State.ProfileActiveRadiusSelect
}
