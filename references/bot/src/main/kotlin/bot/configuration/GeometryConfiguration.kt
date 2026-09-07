package bot.configuration

import org.locationtech.jts.geom.GeometryFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GeometryConfiguration {

    @Bean
    fun geometryFactory(): GeometryFactory = GeometryFactory()
}
