package work.xeltica.craft.core.modules.kusa

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import work.xeltica.craft.core.XCorePlugin
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.modules.hint.Hint
import work.xeltica.craft.core.modules.hint.HintModule
import work.xeltica.craft.core.utils.Ticks
import java.util.UUID

/**
 * 「草」などとチャットすると、足元に草が生える機能を提供するモジュールです。
 */
object KusaModule : ModuleBase() {
    private val kusaTimeMap: HashMap<UUID, Int> = HashMap()
    private val kusaRegex: Regex = Regex("^w+$")

    override fun onEnable() {
        registerHandler(KusaHandler())
    }

    /**
     * [message] を検証し、[player] 周辺に草を生やします。
     */
    fun handleKusa(message: String, player: Player) {
        val messageLower = message.lowercase()
        if (message == "草" || messageLower == "kusa" || kusaRegex.matches(messageLower)) {
            object : BukkitRunnable() {
                override fun run() {
                    val block = player.location.subtract(0.0, 1.0, 0.0).block
                    if (block.type != Material.GRASS_BLOCK) return
                    val id = player.uniqueId
                    val lastTime = kusaTimeMap[id] ?: Int.MIN_VALUE
                    val nowTime = Bukkit.getCurrentTick()
                    if (lastTime == Int.MIN_VALUE || nowTime - lastTime > Ticks.from(20.0)) {
                        block.applyBoneMeal(BlockFace.UP)
                    }
                    kusaTimeMap[id] = nowTime
                    HintModule.achieve(player, Hint.KUSA)
                }
            }.runTask(XCorePlugin.instance)
        }
    }
}

