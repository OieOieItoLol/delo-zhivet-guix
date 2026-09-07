package bot.telegram.processor.intermediate.social

import bot.dadata.DadataService
import bot.telegram.processor.intermediate.IntermediateProcessor
import bot.telegram.state.machine.State
import bot.telegram.state.machine.context.BotContext
import mu.KotlinLogging
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class SocialProblemAddressInputIntermediateProcessor(
    private val dadataService: DadataService,
    private val botContext: BotContext,
    private val geometryFactory: GeometryFactory
) : IntermediateProcessor {

    override fun invoke(update: Update) {
        try {
            val validatedAddress = dadataService.invoke(update.message.text)
            val coordinates = geometryFactory.createPoint(Coordinate(validatedAddress.data.geoLon, validatedAddress.data.geoLat))

            val userContext = botContext.getOrCreate(update.message.chatId)
            userContext.socialProblem = userContext.socialProblem.copy(coordinates = coordinates, address = validatedAddress.value)
        } catch (ex: NullPointerException) {
            val userContext = botContext.getOrCreate(update.message.chatId)
            userContext.socialProblem = userContext.socialProblem.copy(coordinates = null, address = null)
            log.error("Error resolving address ", ex)
        }
    }

    override fun getType(): State = State.SocialProblemAddressInput

    companion object {
        private val log = KotlinLogging.logger {}
    }
}
