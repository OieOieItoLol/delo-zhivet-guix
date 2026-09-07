package bot.telegram.processor.terminating.ecological

import bot.domain.photo.PhotoService
import bot.domain.tag.TagService
import bot.domain.task.TaskService
import bot.telegram.processor.terminating.TerminatingProcessor
import bot.telegram.state.machine.State
import bot.telegram.state.machine.context.BotContext
import bot.telegram.state.machine.context.EcologicalProblemMapper
import bot.telegram.state.machine.context.EcologicalProblemValid
import bot.telegram.state.machine.context.ecologicalProblemValidator
import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class EcologicalProblemCreatedTerminatingProcessor(
    private val botContext: BotContext,
    private val ecologicalProblemMapper: EcologicalProblemMapper,
    private val taskService: TaskService,
    private val tagService: TagService,
    private val photoService: PhotoService
) : TerminatingProcessor {

    override fun invoke(update: Update) {
        val userContext = botContext.getOrCreate(update.message.chatId)
        val userName = update.message.from.userName
        val validatedEcologicalProblem = ecologicalProblemValidator.invoke(userContext.ecologicalProblem)
        if (validatedEcologicalProblem is EcologicalProblemValid) {
            val previousTaskId = taskService.getLastId() ?: 0
            val name = "Экологическая проблема ${previousTaskId + 1}"
            val task = ecologicalProblemMapper.invoke(validatedEcologicalProblem, name, userName)
            val savedTask = taskService.save(task)

            val tags = validatedEcologicalProblem.tags
            tagService.saveTaskTags(savedTask, tags)

            val fileName = "Social_problem_${savedTask.id}.jpg"
            photoService.uploadPhoto(validatedEcologicalProblem.photo, fileName, savedTask.id!!)
        }

        log.info("User data: $validatedEcologicalProblem")
        userContext.clear()
    }

    override fun getType(): State = State.EcologicalProblemCreated

    companion object {
        private val log = KotlinLogging.logger {}
    }
}
