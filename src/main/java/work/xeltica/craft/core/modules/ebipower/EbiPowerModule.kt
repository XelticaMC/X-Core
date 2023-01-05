package work.xeltica.craft.core.modules.ebipower

import org.bukkit.Material
import org.bukkit.entity.Creeper
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Monster
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.inventory.ItemStack
import work.xeltica.craft.core.XCorePlugin
import work.xeltica.craft.core.api.Config
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.hooks.VaultHook
import work.xeltica.craft.core.utils.Ticks
import java.util.Locale

/**
 * エビパワーを入手する手段や、受け渡しのAPIを提供するモジュールです。
 */
object EbiPowerModule : ModuleBase() {
    const val PS_KEY_LAST_JOINED = "last_joined"
    const val PS_KEY_BROKEN_BLOCKS_COUNT = "broken_blocks_count"

    private lateinit var config: Config

    override fun onEnable() {
        registerHandler(EbiPowerHandler())
        EbipowerObserver().runTaskTimer(XCorePlugin.instance, 0, Ticks.from(1.0).toLong())
        XCorePlugin.instance.saveResource("mobEP.yml", false)
        config = Config("mobEP")
    }

    /**
     * [player] の所有エビパワーを取得します。
     */
    fun get(player: Player): Int {
        return VaultHook.getBalance(player).toInt()
    }

    /**
     * [player] に [amount] EPを付与します。
     */
    fun tryGive(player: Player, amount: Int): Boolean {
        require(amount > 0) { "amountを0以下の数にはできない" }
        return VaultHook.tryDepositPlayer(player, amount.toDouble())
    }

    /**
     * [player] から [amount] EPを剥奪します。
     */
    fun tryTake(player: Player, amount: Int): Boolean {
        require(amount > 0) { "amountを0以下の数にはできない" }
        return VaultHook.tryWithdrawPlayer(player, amount.toDouble())
    }

    /**
     * [entity] が倒された時に手に入るEPを取得します。
     */
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