package work.xeltica.craft.core.modules.xMusicDisc

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import work.xeltica.craft.core.modules.item.ItemModule
import work.xeltica.craft.core.modules.nbs.NbsModule
import work.xeltica.craft.core.modules.xMusicDisc.XMusicDiscModule.getXMusicDiscSongId

class XMusicDiscHandler : Listener {
    @EventHandler
    fun onPlay(e: PlayerInteractEvent) {
        val logger = Bukkit.getLogger()
        if (e.hand != EquipmentSlot.HAND) return
        if (e.action != Action.RIGHT_CLICK_BLOCK) return
        val block = e.clickedBlock ?: return
        if (block.type != Material.JUKEBOX) return
        val item = e.item
        val songId = item?.getXMusicDiscSongId()

        val music = NbsModule.getModel(block.location)
        if (music != null) {
            // 音楽の取り出し
            if (!music.isXMusicDisc) {
                logger.warning("バグ：ジュークボックスなのに、Xレコードでない音が鳴ってます…。${block.location}")
                return
            }
            NbsModule.stop(block.location)
            block.world.dropItem(block.location, ItemModule.getItem("${XMusicDiscModule.ITEM_NAME_X_MUSIC_DISC}.${music.songId}"))
            e.setUseInteractedBlock(Event.Result.DENY)
        } else if (songId != null) {
            // 音楽の再生
            NbsModule.playXRecord(block.location, songId)
            e.setUseInteractedBlock(Event.Result.DENY)
            e.player.inventory.remove(item)
        }
    }
}