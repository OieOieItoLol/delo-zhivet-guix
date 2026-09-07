package bot.telegram.state.machine.context

import bot.telegram.command.profile.ActiveRadius
import org.locationtech.jts.geom.Geometry

open class Profile(
    open val userName: String? = null,
    open val address: String? = null,
    open val coordinates: Geometry? = null,
    open val activeRadius: ActiveRadius? = null
) {
    fun copy(
        userName: String? = this.userName,
        address: String? = this.address,
        coordinates: Geometry? = this.coordinates,
        activeRadius: ActiveRadius? = this.activeRadius
    ): Profile = Profile(userName, address, coordinates, activeRadius)

    override fun toString(): String = "Profile(userName=$userName, address=$address, coordinates=$coordinates, activeRadius=$activeRadius)"
}

data class ProfileValid(
    override val userName: String,
    override val address: String,
    override val coordinates: Geometry,
    override val activeRadius: ActiveRadius
) : Profile(userName, address, coordinates)

val profileValidator: (Profile) -> Profile = { profile ->
    if (profile.userName != null && profile.address != null && profile.coordinates != null && profile.activeRadius != null) {
        ProfileValid(profile.userName!!, profile.address!!, profile.coordinates!!, profile.activeRadius!!)
    } else {
        profile
    }
}
