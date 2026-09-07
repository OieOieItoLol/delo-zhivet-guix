package bot.telegram.state.machine.context

import bot.domain.tag.Tag
import org.locationtech.jts.geom.Geometry

open class EcologicalProblem(
    open val contact: String? = null,
    open val coordinates: Geometry? = null,
    open val photo: ByteArray? = null,
    open val tags: List<Tag> = listOf(),
    open val commentary: String? = null
) {
    fun copy(
        contact: String? = this.contact,
        coordinates: Geometry? = this.coordinates,
        photo: ByteArray? = this.photo,
        tags: List<Tag> = this.tags,
        commentary: String? = this.commentary
    ): EcologicalProblem = EcologicalProblem(contact, coordinates, photo, tags, commentary)

    override fun toString(): String = "EcologicalProblem(" +
        "contact=$contact, " +
        "coordinates=$coordinates, " +
        "photo=${photo?.let { "loaded" } ?: "null"}, " +
        "tags=$tags, " +
        "commentary=$commentary" +
        ")"
}

data class EcologicalProblemValid(
    override val contact: String?,
    override val coordinates: Geometry,
    override val photo: ByteArray,
    override val tags: List<Tag> = listOf(),
    override val commentary: String?
) : EcologicalProblem(contact, coordinates, photo, tags, commentary) {

    override fun toString(): String = "EcologicalProblem(" +
        "contact=$contact, " +
        "coordinates=$coordinates, " +
        "tags=$tags, " +
        "commentary=$commentary" +
        ")"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EcologicalProblemValid

        if (contact != other.contact) return false
        if (coordinates != other.coordinates) return false
        if (!photo.contentEquals(other.photo)) return false
        if (tags != other.tags) return false
        if (commentary != other.commentary) return false

        return true
    }

    override fun hashCode(): Int {
        var result = contact?.hashCode() ?: 0
        result = 31 * result + coordinates.hashCode()
        result = 31 * result + photo.contentHashCode()
        result = 31 * result + tags.hashCode()
        result = 31 * result + (commentary?.hashCode() ?: 0)
        return result
    }
}

val ecologicalProblemValidator: (EcologicalProblem) -> EcologicalProblem = { ecologicalProblem ->
    if (ecologicalProblem.coordinates != null && ecologicalProblem.photo != null) {
        EcologicalProblemValid(
            ecologicalProblem.contact,
            ecologicalProblem.coordinates!!,
            ecologicalProblem.photo!!,
            ecologicalProblem.tags,
            ecologicalProblem.commentary
        )
    } else {
        ecologicalProblem
    }
}
