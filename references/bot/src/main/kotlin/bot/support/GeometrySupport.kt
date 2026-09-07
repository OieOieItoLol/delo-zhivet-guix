package bot.support

import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.io.WKTReader
import org.locationtech.jts.io.WKTWriter
import org.postgresql.util.PGobject

private val wktReader = WKTReader()
private val wktWriter = WKTWriter()
fun Geometry.toPgObject(): PGobject = this.let { geometry ->
    PGobject().apply {
        type = "geometry"
        value = wktWriter.write(geometry)
    }
}

object GeometrySupport {

    fun createFromString(geometryStr: String): Geometry = wktReader.read(geometryStr)
}
