package bot.telegram.command.profile

import bot.telegram.command.BotView
import bot.telegram.state.machine.CommandType
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow

abstract class AddressValidationView : BotView {
    override fun invoke(update: Update): SendMessage {
        val address = this.getAddress(update)
        if (address != null) {
            val acceptMessage = "Адрес корректный: $address?"
            return SendMessage(update.message.chatId.toString(), acceptMessage)
                .apply { replyMarkup = acceptMenu }
        } else {
            return SendMessage(update.message.chatId.toString(), rejectMessage)
                .apply { replyMarkup = rejectMenu }
        }
    }

    abstract fun getAddress(update: Update): String?

    companion object {
        private val acceptMenu = ReplyKeyboardMarkup().apply {
            keyboard = listOf(
                KeyboardRow().apply {
                    add(KeyboardButton(CommandType.ACCEPT.commandText))
                },
                KeyboardRow().apply {
                    add(KeyboardButton(CommandType.REJECT.commandText))
                },
                KeyboardRow().apply {
                    add(KeyboardButton(CommandType.RETURN_BACK.commandText))
                },
                KeyboardRow().apply {
                    add(KeyboardButton(CommandType.RETURN_MAIN_MENU.commandText))
                }
            )
            resizeKeyboard = true
        }

        private val rejectMenu = ReplyKeyboardMarkup().apply {
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
        private val rejectMessage = "Введите адрес более подробно, например с указанием района."
    }
}
