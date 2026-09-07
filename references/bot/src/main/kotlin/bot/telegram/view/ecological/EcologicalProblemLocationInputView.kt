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
class EcologicalProblemLocationInputView : BotView {

    override fun invoke(update: Update): SendMessage = SendMessage(update.message.chatId.toString(), message)
        .apply { replyMarkup = menu }

    override fun getType(): State = State.EcologicalProblemLocationInput

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
            Отправьте геопозицию, для этого:
            1.) Нажмите значок в виде скрепки справа от поля ввода сообщения;
            2.) В открывшемся меню выберите «Геопозиция»;
            3.) Выберете место на карте и нажмите «Отправить геопозицию»
        """.trimIndent()
    }
}
