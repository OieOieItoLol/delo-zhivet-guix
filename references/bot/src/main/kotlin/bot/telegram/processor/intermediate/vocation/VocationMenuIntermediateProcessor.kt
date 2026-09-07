package bot.telegram.processor.intermediate.vocation

import bot.telegram.processor.intermediate.IntermediateProcessor
import bot.telegram.state.machine.CommandType
import bot.telegram.state.machine.State
import bot.telegram.state.machine.context.BotContext
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class VocationMenuIntermediateProcessor(private val botContext: BotContext) : IntermediateProcessor {

    override fun invoke(update: Update) {
        val userContext = botContext.getOrCreate(update.message.chatId)
        val command = CommandType.findByCommandTextOrNull(update.message.text)
        val isVocation = when (command) {
            CommandType.VOCATION_ENABLE -> true
            CommandType.VOCATION_DISABLE -> false
            else -> throw IllegalStateException("Unexpected command")
        }
        userContext.isVocation = isVocation
    }

    override fun getType(): State = State.VocationMenu
}
