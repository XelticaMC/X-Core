package work.xeltica.craft.core.modules.stamprally

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

class StampRallyHandler: Listener {
    @EventHandler
    fun createStamp(event: SignChangeEvent) {
        val player = event.player

        val line0 = event.line(0) as? TextComponent
        if (line0?.content()!! != "[stamp]") return
        if (!player.hasPermission(StampRallyModule.CREATE_PERMISSION)) {
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
            if (StampRallyModule.contains(name)) {
                player.sendMessage("スタンプ名が既に存在します")
                return
            }

            event.line(0, Component.text("［§aスタンプ§r］"))
            event.line(1, Component.text("§b"+line1.content()))

            StampRallyModule.create(name, event.block.location)
            player.sendMessage("スタンプ: " + name + "を作成しました")
        }
    }

    @EventHandler
    fun destroyStamp(event: BlockBreakEvent) {
        val stampRallyStore = StampRallyModule
        val player = event.player
        val state = event.block.state
        if (state !is Sign) return

        val line0 = state.line(0) as? TextComponent
        if (line0?.content()!! != "［§aスタンプ§r］") return

        val line1 = state.line(1)
        if (line1 is TextComponent) {
            val name = line1.content()
            val stampName = name.removePrefix("§b")
            if (!stampRallyStore.contains(stampName)) return
            if (!player.hasPermission(StampRallyModule.DESTROY_PERMISSION)) {
                player.sendMessage("スタンプを破壊する権限がありません")
                event.isCancelled = true
                return
            }
            stampRallyStore.destroy(stampName)
            player.sendMessage("スタンプ: " + stampName + "を破壊しました")
        }
    }

    @EventHandler
    fun breakBlock(event: BlockBreakEvent) {
        val block = event.block
        if (getNearStampSign(block.location) != null) {
            event.isCancelled = true
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
                val stampName = line1.content().removePrefix("§b")
                StampRallyModule.activate(event.player, stampName)
            }
        }
    }

    private fun getNearStampSign(loc: Location): Sign? {
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