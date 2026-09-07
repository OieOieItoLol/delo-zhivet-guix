package bot.telegram.state.machine.context

import bot.domain.tag.Tag
import org.locationtech.jts.geom.Geometry

open class SocialProblem(
    open val contact: String? = null,
    open val coordinates: Geometry? = null,
    open val address: String? = null,
    open val photo: ByteArray? = null,
    open val tags: List<Tag> = listOf(),
    open val commentary: String? = null
) {
    fun copy(
        contact: String? = this.contact,
        coordinates: Geometry? = this.coordinates,
        address: String? = this.address,
        photo: ByteArray? = this.photo,
        tags: List<Tag> = this.tags,
        commentary: String? = this.commentary
    ): SocialProblem = SocialProblem(contact, coordinates, address, photo, tags, commentary)

    override fun toString(): String = "SocialProblem(" +
        "contact=$contact, " +
        "coordinates=$coordinates, " +
        "address=$address" +
        "photo=${photo?.let { "loaded" } ?: "null"}, " +
        "tags=$tags, " +
        "commentary=$commentary" +
        ")"
}

data class SocialProblemValid(
    override val contact: String?,
    override val coordinates: Geometry,
    override val address: String,
    override val photo: ByteArray,
    override val tags: List<Tag> = listOf(),
    override val commentary: String?
) : SocialProblem(contact, coordinates, address, photo, tags, commentary) {

    override fun toString(): String = "SocialProblem(" +
        "contact=$contact, " +
        "coordinates=$coordinates, " +
        "address=$address" +
        "tags=$tags, " +
        "commentary=$commentary" +
        ")"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SocialProblemValid

        if (contact != other.contact) return false
        if (coordinates != other.coordinates) return false
        if (address != other.address) return false
        if (!photo.contentEquals(other.photo)) return false
        if (tags != other.tags) return false
        if (commentary != other.commentary) return false

        return true
    }

    override fun hashCode(): Int {
        var result = contact?.hashCode() ?: 0
        result = 31 * result + coordinates.hashCode()
        result = 31 * result + address.hashCode()
        result = 31 * result + photo.contentHashCode()
        result = 31 * result + tags.hashCode()
        result = 31 * result + (commentary?.hashCode() ?: 0)
        return result
    }
}

val socialProblemValidator: (SocialProblem) -> SocialProblem = { socialProblem ->
    if (socialProblem.coordinates != null &&
        socialProblem.address != null &&
        socialProblem.photo != null
    ) {
        SocialProblemValid(
            socialProblem.contact,
            socialProblem.coordinates!!,
            socialProblem.address!!,
            socialProblem.photo!!,
            socialProblem.tags,
            socialProblem.commentary
        )
    } else {
        socialProblem
    }
}
