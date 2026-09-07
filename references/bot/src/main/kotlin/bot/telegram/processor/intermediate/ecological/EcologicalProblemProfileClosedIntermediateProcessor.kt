package bot.telegram.processor.intermediate.ecological

import bot.telegram.processor.intermediate.IntermediateProcessor
import bot.telegram.state.machine.State
import bot.telegram.state.machine.context.BotContext
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class EcologicalProblemProfileClosedIntermediateProcessor(private val botContext: BotContext) : IntermediateProcessor {

    override fun invoke(update: Update) {
        val userContext = botContext.getOrCreate(update.message.chatId)
        userContext.ecologicalProblem = userContext.ecologicalProblem.copy(contact = update.message.text)
    }

    override fun getType(): State = State.EcologicalProblemProfileClosed
}
