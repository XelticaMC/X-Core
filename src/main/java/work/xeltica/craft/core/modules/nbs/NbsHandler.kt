package work.xeltica.craft.core.modules.nbs

import com.xxmicloxx.NoteBlockAPI.event.SongEndEvent
import com.xxmicloxx.NoteBlockAPI.songplayer.PositionSongPlayer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.NotePlayEvent
import org.bukkit.event.player.PlayerQuitEvent
import work.xeltica.craft.core.modules.item.ItemModule
import work.xeltica.craft.core.modules.xMusicDisc.XMusicDiscModule

class NbsHandler : Listener {
    /**
     * ブロック破壊したときに音を止める
     */
    @EventHandler
    fun onNoteBreak(e: BlockBreakEvent) {
        val loc = e.block.location
        val nbs = NbsModule.getModel(loc) ?: return
        NbsModule.stop(loc)
        if (nbs.isXMusicDisc) {
            loc.world.dropItem(loc, ItemModule.getItem("${XMusicDiscModule.ITEM_NAME_X_MUSIC_DISC}.${nbs.songId}"))
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
        val location = (e.songPlayer as PositionSongPlayer).targetLocation
        val model = NbsModule.getModel(location) ?: return
        // 音楽ディスクの場合は消去しない
        if (model.isXMusicDisc) return
        NbsModule.removeModel(location)
    }
}
