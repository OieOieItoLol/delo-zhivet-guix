package bot.telegram.command.ecological

import bot.telegram.command.BotView
import bot.telegram.state.machine.CommandType
import bot.telegram.state.machine.State
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow

@Component
class EcologicalProblemMenuView : BotView {
    override fun invoke(update: Update): SendMessage = SendMessage(update.message.chatId.toString(), message)
        .apply { replyMarkup = menu }

    override fun getType(): State = State.EcologicalProblemMenu

    companion object {
        private val menu = ReplyKeyboardMarkup().apply {
            keyboard = listOf(
                KeyboardRow().apply {
                    add(KeyboardButton(CommandType.SEND_ECOLOGICAL_PROBLEM.commandText))
                },
                KeyboardRow().apply {
                    add(KeyboardButton(CommandType.RETURN_BACK.commandText))
                }
            )
            resizeKeyboard = true
        }

        private val message = """
            Для того, чтобы отправить информацию о социальной проблеме нужно отправить фото, адрес, выбрать тип проблемы и, ри желании, написать комментарии с другой важной информацией.
        """.trimIndent()
    }
}
