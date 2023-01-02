package work.xeltica.craft.core.modules.nbs

import org.bukkit.Location
import org.bukkit.configuration.serialization.ConfigurationSerializable
import work.xeltica.craft.core.utils.CastHelper

class NbsModel(var location: Location, var songId: String, var distance: Int, var playbackMode: PlaybackMode) : Cloneable, ConfigurationSerializable {
    companion object {
        @JvmStatic
        fun deserialize(args: Map<String, Any>): NbsModel {
            require(args.containsKey("location")) { "location is null" }
            require(args.containsKey("songId")) { "songId is null" }
            require(args.containsKey("distance")) { "distance is null" }

            val location = Location.deserialize(CastHelper.checkMap(args["location"] as Map<*, *>))
            val songId = args["songId"] as String
            val distance = args["distance"] as Int
            val playbackMode = if (!args.containsKey("playbackMode")) {
                PlaybackMode.NORMAL
            } else {
                PlaybackMode.valueOf(args["playbackMode"] as String)
            }

            return NbsModel(location, songId, distance, playbackMode)
        }
    }

    override fun serialize(): MutableMap<String, Any> {
        val result = HashMap<String, Any>()
        result["location"] = location.serialize()
        result["songId"] = songId
        result["distance"] = distance
        result["playbackMode"] = playbackMode.toString()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (other == this) return true
        if (other !is NbsModel) return false
        if (this.location != other.location) return false
        if (this.songId != other.songId) return false
        if (this.playbackMode != other.playbackMode) return false
        return true
    }

    override fun toString(): String {
        return "NbsModel(location=$location, songId=$songId, distance=$distance, playbackMode=$playbackMode)"
    }

    override fun hashCode(): Int {
        var result = location.hashCode()
        result = 31 * result + songId.hashCode()
        result = 31 * result + distance
        result = 31 * result + playbackMode.hashCode()
        return result
    }

    enum class PlaybackMode {
        /** 普通  */
        NORMAL,

        /** ループする  */
        LOOP,

        /** トグルで停止せずに鳴り続ける  */
        ONESHOT
    }
}