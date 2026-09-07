package bot.telegram.command.ecological

import bot.telegram.command.BotView
import bot.telegram.state.machine.CommandType
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow

abstract class ProblemCreatedView : BotView {

    override fun invoke(update: Update): SendMessage = SendMessage(update.message.chatId.toString(), message)
        .apply { replyMarkup = menu }

    companion object {
        private val menu = ReplyKeyboardMarkup().apply {
            keyboard = listOf(
                KeyboardRow().apply {
                    add(KeyboardButton(CommandType.RETURN_MAIN_MENU.commandText))
                }
            )
            resizeKeyboard = true
        }

        private val message = """
            Сообщение о проблеме успешно отправлено.
            Мы свяжемся с вами через телеграмм если понадобятся подробности.
            Вы также можете зарегистрироваться в качестве волонтера нажав Стать волонтером/изменить данные.
        """.trimIndent()
    }
}
