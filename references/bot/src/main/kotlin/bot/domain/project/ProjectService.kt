package bot.domain.project

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class ProjectService(
    @Value("\${app.project.name}")
    private val projectName: String
) {

    fun getName() = projectName
}
