package bot.telegram.processor.intermediate.ecological

import bot.telegram.processor.intermediate.IntermediateProcessor
import bot.telegram.state.machine.State
import bot.telegram.state.machine.context.BotContext
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class EcologicalProblemCommentaryInputIntermediateProcessor(private val botContext: BotContext) : IntermediateProcessor {

    override fun invoke(update: Update) {
        val userContext = botContext.getOrCreate(update.message.chatId)
        val commentary: String? = if (update.message.isCommand) null else update.message.text
        userContext.ecologicalProblem = userContext.ecologicalProblem.copy(commentary = commentary)
    }

    override fun getType(): State = State.EcologicalProblemCommentaryInput
}
