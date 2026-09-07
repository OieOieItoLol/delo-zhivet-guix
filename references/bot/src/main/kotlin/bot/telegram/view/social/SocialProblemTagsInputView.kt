package bot.telegram.command.ecological

import bot.domain.tag.TagService
import bot.domain.task.TaskType
import bot.telegram.state.machine.State
import bot.telegram.state.machine.context.BotContext
import org.springframework.stereotype.Component

@Component
class SocialProblemTagsInputView(botContext: BotContext, tagService: TagService) :
    TagsInputView(
        botContext,
        tagService,
        TaskType.SOCIAL,
        { userContext -> userContext.socialProblem.tags }
    ) {
    override fun getType(): State = State.SocialProblemTagsInput
}
