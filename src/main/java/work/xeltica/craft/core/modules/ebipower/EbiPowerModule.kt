package work.xeltica.craft.core.modules.ebipower

import org.bukkit.Material
import org.bukkit.entity.*
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.inventory.ItemStack
import work.xeltica.craft.core.XCorePlugin
import work.xeltica.craft.core.api.Config
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.hooks.VaultHook
import work.xeltica.craft.core.utils.Ticks
import java.util.*

object EbiPowerModule: ModuleBase() {
    const val PS_KEY_LAST_JOINED = "last_joined"
    const val PS_KEY_BROKEN_BLOCKS_COUNT = "broken_blocks_count"

    private lateinit var config: Config

    override fun onEnable() {
        registerHandler(EbiPowerHandler())
        EbipowerObserver().runTaskTimer(XCorePlugin.instance, 0, Ticks.from(1.0).toLong())
        XCorePlugin.instance.saveResource("mobEP.yml", false)
        config = Config("mobEP")
    }

    fun get(p: Player): Int {
        return VaultHook.getBalance(p).toInt()
    }

    fun tryGive(p: Player, amount: Int): Boolean {
        require(amount > 0) { "amountを0以下の数にはできない" }
        return VaultHook.tryDepositPlayer(p, amount.toDouble())
    }

    fun tryTake(p: Player, amount: Int): Boolean {
        require(amount > 0) { "amountを0以下の数にはできない" }
        return VaultHook.tryWithdrawPlayer(p, amount.toDouble())
    }

    fun getMobDropEP(entity: Entity, event: EntityDeathEvent): Int {
        if (entity.type == EntityType.ENDERMAN) {
            if (event.drops.stream().map { obj: ItemStack -> obj.type }.toList().contains(Material.ENDER_PEARL)) {
                return config.conf.getInt("pearl_enderman")
            }
        }
        if (entity.type == EntityType.CREEPER) {
            if ((entity as Creeper).isPowered) {
                return config.conf.getInt("charged_creeper")
            }
        }
        if (config.conf.contains(entity.type.name.lowercase(Locale.getDefault()))) {
            return config.conf.getInt(entity.type.name.lowercase(Locale.getDefault()))
        }
        return if (entity is Monster) {
            config.conf.getInt("other_enemy")
        } else {
            config.conf.getInt("friendly_mob")
        }
    }
}