package bot.configuration

import bot.telegram.TelegramBot
import bot.telegram.command.BotView
import bot.telegram.processor.intermediate.IntermediateProcessor
import bot.telegram.processor.terminating.TerminatingProcessor
import bot.telegram.state.machine.State
import bot.telegram.state.machine.context.BotContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TelegramConfiguration {

    @Bean
    fun views(botViews: List<BotView>): Map<State, BotView> = botViews.associateBy { it.getType() }

    @Bean
    fun intermediateProcessors(intermediateProcessors: List<IntermediateProcessor>): Map<State, IntermediateProcessor> =
        intermediateProcessors.associateBy { it.getType() }

    @Bean
    fun terminatingProcessors(terminatingProcessors: List<TerminatingProcessor>): Map<State, TerminatingProcessor> =
        terminatingProcessors.associateBy { it.getType() }

    @Bean
    fun telegramBotController(
        @Value("\${telegram.bot.token}") botToken: String,
        @Value("\${telegram.bot.username}") botUsername: String,
        botViewMap: Map<State, BotView>,
        botContext: BotContext
    ): TelegramBot = TelegramBot(
        botToken,
        botUsername,
        botViewMap,
        botContext
    )
}
