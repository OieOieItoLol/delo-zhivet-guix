package bot.telegram.command

import bot.telegram.state.machine.CommandType
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow

val mainMenu = ReplyKeyboardMarkup().apply {
    keyboard = listOf(
        KeyboardRow().apply {
            add(KeyboardButton(CommandType.ECOLOGICAL_PROBLEM_MENU.commandText))
        },
        KeyboardRow().apply {
            add(KeyboardButton(CommandType.SOCIAL_PROBLEM_MENU.commandText))
        },
        KeyboardRow().apply {
            add(KeyboardButton(CommandType.PROFILE_MENU.commandText))
        },
        KeyboardRow().apply {
            add(KeyboardButton(CommandType.VOCATION_MENU.commandText))
        },
        KeyboardRow().apply {
            add(KeyboardButton(CommandType.SUPPORT_MENU.commandText))
        }
    )
    resizeKeyboard = true
}

val featureNotReadyMessage = "На текущий момент функция недоступна."
