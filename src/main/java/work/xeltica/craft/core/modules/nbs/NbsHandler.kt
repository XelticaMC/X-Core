package work.xeltica.craft.core.modules.nbs

import com.xxmicloxx.NoteBlockAPI.event.SongEndEvent
import com.xxmicloxx.NoteBlockAPI.songplayer.PositionSongPlayer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.NotePlayEvent
import org.bukkit.event.player.PlayerQuitEvent

class NbsHandler : Listener {
    /**
     * ブロック破壊したときに音を止める
     */
    @EventHandler
    fun onNoteBreak(e: BlockBreakEvent) {
        val loc = e.block.location
        if (NbsModule.has(loc)) {
            NbsModule.stop(loc)
        }
    }

    @EventHandler
    fun onNotePlay(e: NotePlayEvent) {
        val location = e.block.location
        val song = NbsModule.getSong(location)

        if (NbsModule.has(location) && NbsModule.getModel(location)?.playbackMode != NbsModel.PlaybackMode.ONESHOT) {
            NbsModule.stop(location)
            e.isCancelled = true
        } else if (song != null) {
            NbsModule.play(location, song.songId, song.distance, song.mode)
            e.isCancelled = true
        }
    }

    @EventHandler
    fun onPlayerLeave(e: PlayerQuitEvent) {
        NbsModule.removeAudience(e.player)
    }

    @EventHandler
    fun onSongFinished(e: SongEndEvent) {
        NbsModule.removeModel((e.songPlayer as PositionSongPlayer).targetLocation)
    }
}
