package bot.telegram.processor.intermediate.social

import bot.support.TelegramPhotoSupport
import bot.telegram.processor.intermediate.IntermediateProcessor
import bot.telegram.state.machine.State
import bot.telegram.state.machine.context.BotContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class SocialProblemPhotosInputIntermediateProcessor(private val botContext: BotContext) : IntermediateProcessor {

    @Value("\${telegram.bot.token}")
    private lateinit var botToken: String

    override fun invoke(update: Update) {
        val userContext = botContext.getOrCreate(update.message.chatId)
        val photo = TelegramPhotoSupport.findPhotoWithMaxResolution(update.message.photo)
        val photoByteArray = TelegramPhotoSupport.download(botToken, photo.fileId)
        userContext.socialProblem = userContext.socialProblem.copy(photo = photoByteArray)
    }

    override fun getType(): State = State.SocialProblemPhotosInput
}
