package bot.telegram.command.ecological

import bot.domain.tag.Tag
import bot.domain.tag.TagService
import bot.domain.task.TaskType
import bot.telegram.command.BotView
import bot.telegram.state.machine.CommandType
import bot.telegram.state.machine.context.BotContext
import bot.telegram.state.machine.context.UserContext
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow

abstract class TagsInputView(
    private val botContext: BotContext,
    private val tagService: TagService,
    private val taskTape: TaskType,
    private val getSelectedTags: (UserContext) -> List<Tag>
) : BotView {
    override fun invoke(update: Update): SendMessage {
        val userContext = botContext.getOrCreate(update.message.chatId)
        val tags = tagService.findAllByTaskType(taskTape).map { tag ->
            KeyboardRow().apply {
                add(
                    KeyboardButton(
                        formatTag(userContext, tag.name)
                    )
                )
            }
        }.toList()
        val menu = ReplyKeyboardMarkup().apply {
            keyboard = tags + menuKeyboards
            resizeKeyboard = true
        }
        return SendMessage(update.message.chatId.toString(), message)
            .apply { replyMarkup = menu }
    }

    private fun formatTag(userContext: UserContext, tag: String): String =
        if (getSelectedTags(userContext).any { it.name == tag }) "$tagSelectedEmoji $tag" else "$tagNotSelectedEmoji $tag"

    companion object {
        private val menuKeyboards = listOf(
            KeyboardRow().apply {
                add(KeyboardButton(CommandType.NEXT.commandText))
            },
            KeyboardRow().apply {
                add(KeyboardButton(CommandType.RETURN_BACK.commandText))
            }
        )

        private val message = """
            Выберите теги, которые относятся к проблеме. Если ничего не подходит просто нажмите Далее.
        """.trimIndent()

        private val tagSelectedEmoji = "\u2705"
        private val tagNotSelectedEmoji = "\u274C"
    }
}
