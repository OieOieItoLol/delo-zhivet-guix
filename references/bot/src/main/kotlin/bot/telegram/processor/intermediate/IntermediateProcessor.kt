package bot.telegram.processor.intermediate

import bot.telegram.state.machine.State
import org.telegram.telegrambots.meta.api.objects.Update

interface IntermediateProcessor {
    fun invoke(update: Update)
    fun getType(): State
}
