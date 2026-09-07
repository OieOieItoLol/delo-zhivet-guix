package bot.telegram.command.common

import bot.telegram.command.BotView
import bot.telegram.state.machine.CommandType
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow

@Service
abstract class ProfileClosedView : BotView {

    override fun invoke(update: Update): SendMessage = SendMessage(update.message.chatId.toString(), message)
        .apply { replyMarkup = menu }

    companion object {
        private val menu = ReplyKeyboardMarkup().apply {
            keyboard = listOf(
                KeyboardRow().apply {
                    add(KeyboardButton(CommandType.RETURN_BACK.commandText))
                },
                KeyboardRow().apply {
                    add(KeyboardButton(CommandType.RETURN_MAIN_MENU.commandText))
                }
            )
            resizeKeyboard = true
        }

        private val message = """
            Оставьте любые контактные данные сообщением, чтобы мы могли связаться с вами для
            уточнения деталей по проблеме.
        """.trimIndent()
    }
}
