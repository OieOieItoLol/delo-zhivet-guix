package bot.telegram.command

import bot.telegram.state.machine.State
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update

interface BotView {
    fun invoke(update: Update): SendMessage
    fun getType(): State
}
