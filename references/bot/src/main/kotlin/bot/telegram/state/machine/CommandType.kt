package bot.telegram.state.machine

import org.telegram.telegrambots.meta.api.objects.Update

enum class CommandType(val commandText: String, val eventFactory: (Update) -> Event) {
    START(
        "/start",
        { event -> Event.OnStart(event) }
    ),
    PROFILE_MENU(
        "Стать волонтером/изменить данные.",
        { event -> Event.OnProfileMenuSelected(event) }
    ),
    PROFILE_ENTER_ADDRESS(
        "Перейти к указанию адреса.",
        { event -> Event.OnProfileEnterAddress(event) }
    ),
    ECOLOGICAL_PROBLEM_MENU(
        "Сообщить об экологической проблеме.",
        { event -> Event.OnEcologicalProblemMenuSelected(event) }
    ),
    SEND_ECOLOGICAL_PROBLEM(
        "Отправить информацию об экопроблеме.",
        { event -> Event.OnEcologicalProblemSendSelected(event) }
    ),
    SOCIAL_PROBLEM_MENU(
        "Сообщить о социальной проблеме",
        { event -> Event.OnSocialProblemMenuSelected(event) }
    ),
    SEND_SOCIAL_PROBLEM(
        "Отправить информацию о соцпроблеме.",
        { event -> Event.OnSocialProblemSendSelected(event) }
    ),
    ACCEPT_WITHOUT_COMMENT(
        "Отправить без комментария.",
        { event -> Event.OnAcceptWithoutCommentary(event) }
    ),
    SUPPORT_MENU(
        "Оказать поддержку.",
        { event -> Event.OnSupportMenuSelected(event) }
    ),
    VOCATION_MENU(
        "Режим отпуска.",
        { event -> Event.OnVocationMenuSelected(event) }
    ),
    VOCATION_ENABLE(
        "Включить режим отпуска.",
        { event -> Event.OnVocationChanged(event) }
    ),
    VOCATION_DISABLE(
        "Выключить режим отпуска.",
        { event -> Event.OnVocationChanged(event) }
    ),
    RETURN_MAIN_MENU(
        "Возврат в главное меню.",
        { event -> Event.OnReturnMainMenu(event) }
    ),
    RETURN_BACK(
        "Назад.",
        { event -> Event.OnReturnBack(event) }
    ),
    ACCEPT(
        "Да.",
        { event -> Event.OnAccept(event) }
    ),
    REJECT(
        "Нет.",
        { event -> Event.OnReject(event) }
    ),
    NEXT(
        "Далее.",
        { event -> Event.OnNext(event) }
    );

    companion object {
        fun findByCommandTextOrNull(value: String): CommandType? = entries
            .firstOrNull { it.commandText == value }
    }
}
