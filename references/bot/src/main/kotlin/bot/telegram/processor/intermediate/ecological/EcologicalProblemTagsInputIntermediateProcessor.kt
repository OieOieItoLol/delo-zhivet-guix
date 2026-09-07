package bot.telegram.processor.intermediate.ecological

import bot.domain.tag.Tag
import bot.domain.tag.TagService
import bot.domain.task.TaskType
import bot.telegram.processor.intermediate.IntermediateProcessor
import bot.telegram.state.machine.State
import bot.telegram.state.machine.context.BotContext
import bot.telegram.state.machine.context.UserContext
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class EcologicalProblemTagsInputIntermediateProcessor(private val botContext: BotContext, private val tagsService: TagService) :
    IntermediateProcessor {

    override fun invoke(update: Update) {
        val userContext = botContext.getOrCreate(update.message.chatId)
        val input = update.message.text
            .replace(tagSelectedEmoji, "")
            .replace(tagNotSelectedEmoji, "")
            .trimIndent()
        val updatedTags = tagsService.findAllByTaskType(TaskType.ECOLOGICAL)
            .firstOrNull { it.name == input }
            ?.let { tag -> toggleTags(userContext, tag) }
        userContext.ecologicalProblem = userContext.ecologicalProblem.copy(
            tags = updatedTags ?: userContext.ecologicalProblem.tags
        )
    }

    override fun getType(): State = State.EcologicalProblemTagsInput

    private fun toggleTags(userContext: UserContext, tag: Tag): List<Tag> {
        val selectedTags = userContext.ecologicalProblem.tags
        return if (selectedTags.contains(tag)) {
            selectedTags.filter { it != tag }
        } else {
            selectedTags + tag
        }
    }

    companion object {
        private val tagSelectedEmoji = "\u2705"
        private val tagNotSelectedEmoji = "\u274C"
    }
}
