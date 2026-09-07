package bot.telegram.processor.terminating.social

import bot.domain.photo.PhotoService
import bot.domain.tag.TagService
import bot.domain.task.TaskService
import bot.telegram.processor.terminating.TerminatingProcessor
import bot.telegram.state.machine.State
import bot.telegram.state.machine.context.BotContext
import bot.telegram.state.machine.context.SocialProblemMapper
import bot.telegram.state.machine.context.SocialProblemValid
import bot.telegram.state.machine.context.socialProblemValidator
import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class SocialProblemCreatedTerminatingProcessor(
    private val botContext: BotContext,
    private val socialProblemMapper: SocialProblemMapper,
    private val taskService: TaskService,
    private val tagService: TagService,
    private val photoService: PhotoService
) : TerminatingProcessor {

    override fun invoke(update: Update) {
        val userContext = botContext.getOrCreate(update.message.chatId)
        val userName = update.message.from.userName
        val validatedSocialProblem = socialProblemValidator.invoke(userContext.socialProblem)
        if (validatedSocialProblem is SocialProblemValid) {
            val previousTaskId = taskService.getLastId() ?: 0
            val name = "Социальная проблема ${previousTaskId + 1}"
            val task = socialProblemMapper.invoke(validatedSocialProblem, name, userName)
            val savedTask = taskService.save(task)

            val tags = validatedSocialProblem.tags
            tagService.saveTaskTags(savedTask, tags)

            val fileName = "Social_problem_${savedTask.id}.jpg"
            photoService.uploadPhoto(validatedSocialProblem.photo, fileName, savedTask.id!!)
        }

        log.info("User data: $validatedSocialProblem")
        userContext.clear()
    }

    override fun getType(): State = State.SocialProblemCreated

    companion object {
        private val log = KotlinLogging.logger {}
    }
}
