package bot.telegram.command.ecological

import bot.telegram.command.BotView
import bot.telegram.state.machine.CommandType
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow

abstract class CommentaryInputView : BotView {
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
                },
                KeyboardRow().apply {
                    add(KeyboardButton(CommandType.ACCEPT_WITHOUT_COMMENT.commandText))
                }
            )
            resizeKeyboard = true
        }

        private val message = "Отправьте комментарий или нажмите Отправить без комментария."
    }
}
