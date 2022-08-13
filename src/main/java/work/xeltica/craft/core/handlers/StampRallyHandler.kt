package work.xeltica.craft.core.handlers

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.SignChangeEvent
import org.bukkit.event.player.PlayerInteractEvent
import work.xeltica.craft.core.stores.StampRallyStore

class StampRallyHandler: Listener {
    @EventHandler
    fun createStamp(event: SignChangeEvent) {
        val stampRallyStore = StampRallyStore.getInstance()
        val player = event.player

        val line0 = event.line(0) as? TextComponent
        if (line0?.content()!! != "[stamp]") return
        if (!player.hasPermission(StampRallyStore.CREATE_PERMISSION)) {
            player.sendMessage("スタンプを作成する権限がありません")
            return
        }
        val line1 = event.line(1)
        if (line1 is TextComponent) {
            val name = line1.content()
            if (name.isEmpty()) {
                player.sendMessage("スタンプ名が入力されていません")
                return
            }
            if (stampRallyStore.contains(name)) {
                player.sendMessage("スタンプ名が既に存在します")
                return
            }


            event.line(0, Component.text("［§aスタンプ§r］"))

            stampRallyStore.create(name, event.block.location)
            player.sendMessage("スタンプ: " + name + "を作成しました")
        }
    }

    @EventHandler
    fun destroyStamp(event: BlockBreakEvent) {
        val stampRallyStore = StampRallyStore.getInstance()
        val player = event.player
        val state = event.block.state
        if (state !is Sign) return

        val line0 = state.line(0) as? TextComponent
        if (line0?.content()!! != "［§aスタンプ§r］") return

        val line1 = state.line(1)
        if (line1 is TextComponent) {
            val name = line1.content()
            if (!stampRallyStore.contains(name)) return
            if (!player.hasPermission(StampRallyStore.DESTROY_PERMISSION)) {
                player.sendMessage("スタンプを破壊する権限がありません")
                return
            }
            stampRallyStore.destroy(name)
            player.sendMessage("スタンプ: " + name + "を破壊しました")
        }
    }

    @EventHandler
    fun activateStamp(event: PlayerInteractEvent) {
        if (event.action == Action.RIGHT_CLICK_BLOCK) {
            val block = event.clickedBlock ?: return

            if (block.type.name.endsWith("_BUTTON")) {
                val location = block.location.add(0.0, -1.0, 0.0)
                val sign = getNearStampSign(location) ?: return
                val line1 = sign.line(1) as? TextComponent ?: return
                StampRallyStore.getInstance().activate(event.player, line1.content())
            }
        }
    }

    fun getNearStampSign(loc: Location): Sign? {
        val directions = arrayOf(
            BlockFace.EAST.direction,
            BlockFace.NORTH.direction,
            BlockFace.SOUTH.direction,
            BlockFace.WEST.direction
        )
        for (direction in directions) {
            val newLoc = loc.clone().add(direction)
            val state = newLoc.block.state
            if (state is Sign) {
                val line0 = state.line(0) as? TextComponent ?: continue
                if (line0.content() == "［§aスタンプ§r］") {
                    return state
                }
            }
        }
        return null
    }
}