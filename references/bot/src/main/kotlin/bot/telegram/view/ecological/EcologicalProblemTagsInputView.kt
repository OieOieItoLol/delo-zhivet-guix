package bot.telegram.command.ecological

import bot.domain.tag.TagService
import bot.domain.task.TaskType
import bot.telegram.state.machine.State
import bot.telegram.state.machine.context.BotContext
import org.springframework.stereotype.Component

@Component
class EcologicalProblemTagsInputView(botContext: BotContext, tagService: TagService) :
    TagsInputView(
        botContext,
        tagService,
        TaskType.ECOLOGICAL,
        { userContext -> userContext.ecologicalProblem.tags }
    ) {
    override fun getType(): State = State.EcologicalProblemTagsInput
}
