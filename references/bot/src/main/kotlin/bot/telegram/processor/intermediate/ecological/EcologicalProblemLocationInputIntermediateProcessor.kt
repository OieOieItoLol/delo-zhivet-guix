package bot.telegram.processor.intermediate.ecological

import bot.telegram.processor.intermediate.IntermediateProcessor
import bot.telegram.state.machine.State
import bot.telegram.state.machine.context.BotContext
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class EcologicalProblemLocationInputIntermediateProcessor(
    private val botContext: BotContext,
    private val geometryFactory: GeometryFactory
) : IntermediateProcessor {

    override fun invoke(update: Update) {
        val userContext = botContext.getOrCreate(update.message.chatId)
        val coordinates = geometryFactory.createPoint(Coordinate(update.message.location.longitude, update.message.location.latitude))
        userContext.ecologicalProblem = userContext.ecologicalProblem.copy(coordinates = coordinates)
    }

    override fun getType(): State = State.EcologicalProblemLocationInput
}
