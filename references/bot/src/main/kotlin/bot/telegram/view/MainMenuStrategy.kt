package bot.telegram.command

import bot.telegram.state.machine.State
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class MainMenuStrategy(@Value("\${app.project.name}") private val projectName: String) : BotView {
    override fun invoke(update: Update): SendMessage = SendMessage(
        update.message.chatId.toString(),
        "Привет ${update.message.from.userName}, я бот проекта $projectName.\n" +
            "Я могу принять заявку на помощь или зарегистрировать тебя волонтером. " +
            "Выбери, что ты хочешь сделать: "
    ).apply { replyMarkup = mainMenu }

    override fun getType(): State = State.MainMenu
}
