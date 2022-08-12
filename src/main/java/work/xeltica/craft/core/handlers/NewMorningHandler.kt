package work.xeltica.craft.core.handlers

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import work.xeltica.craft.core.api.events.NewMorningEvent
import work.xeltica.craft.core.modules.OmikujiModule

/**
 * 日付が変わった際のイベントハンドラーをまとめています。
 * @author Xeltica
 */
class NewMorningHandler : Listener {
    @EventHandler
    fun onNewMorning(e: NewMorningEvent?) {
        OmikujiModule.reset()
    }
}