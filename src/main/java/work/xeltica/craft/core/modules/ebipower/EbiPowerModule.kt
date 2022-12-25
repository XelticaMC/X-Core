package work.xeltica.craft.core.modules.ebipower

import org.bukkit.configuration.serialization.ConfigurationSerialization
import org.bukkit.entity.Player
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.plugins.VaultPlugin
import work.xeltica.craft.core.utils.CastHelper
import work.xeltica.craft.core.utils.Config
import java.io.IOException

object EbiPowerModule: ModuleBase() {
    private const val CONFIG_KEY_SHOP_ITEMS = "shopItems"
    private const val CONFIG_KEY_EFFECT_SHOP_ITEMS = "effectShopItems"

    lateinit var ep: Config

    lateinit var shopItems: ArrayList<EbiPowerItem>
    lateinit var effectShopItems: ArrayList<EbiPowerEffect>

    override fun onEnable() {
        ConfigurationSerialization.registerClass(EbiPowerItem::class.java, "EbiPowerItem")
        ConfigurationSerialization.registerClass(EbiPowerEffect::class.java, "EbiPowerEffect")

        ep = Config("ep") { conf ->
            val c = conf.conf
            val items = c.getList(CONFIG_KEY_SHOP_ITEMS)
            val effects = c.getList(CONFIG_KEY_EFFECT_SHOP_ITEMS)
            shopItems = if (items != null) {
                CastHelper.checkList<EbiPowerItem>(items) as ArrayList<EbiPowerItem>
            } else {
                ArrayList()
            }
            effectShopItems = if (effects != null) {
                CastHelper.checkList<EbiPowerEffect>(effects) as ArrayList<EbiPowerEffect>
            } else {
                ArrayList()
            }
        }
        registerCommand("epshop", EpShopCommand())
        registerCommand("epeffectshop", EpEffectShopCommand())
        registerHandler(EbiPowerHandler())
    }

    fun addItem(item: EbiPowerItem) {
        shopItems.add(item)
        ep.conf.set(CONFIG_KEY_SHOP_ITEMS, shopItems)
        try {
            ep.save()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun deleteItem(item: EbiPowerItem) {
        shopItems.remove(item)
        ep.conf.set(CONFIG_KEY_SHOP_ITEMS, shopItems)
        try {
            ep.save()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun addEffectItem(item: EbiPowerEffect) {
        effectShopItems.add(item)
        ep.conf.set(CONFIG_KEY_EFFECT_SHOP_ITEMS, effectShopItems)
        try {
            ep.save()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun deleteEffectItem(item: EbiPowerEffect) {
        effectShopItems.remove(item)
        ep.conf.set(CONFIG_KEY_EFFECT_SHOP_ITEMS, effectShopItems)
        try {
            ep.save()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun tryBuyItem(p: Player, item: EbiPowerItem): Result {
        val isFree = item.cost == 0
        if (!isFree && !tryTake(p, item.cost)) {
            return Result.NO_ENOUGH_POWER
        }
        val res = p.inventory.addItem(item.item.clone())
        return if (res.size != 0) {
            // 購入失敗なので返金
            if (!isFree) tryGive(p, item.cost)
            val partiallyAddedItemsCount = item.item.amount - res[0]!!.amount
            if (partiallyAddedItemsCount > 0) {
                // 部分的に追加されてしまったアイテムを剥奪
                val partial = item.item.clone()
                partial.amount = partiallyAddedItemsCount
                p.inventory.removeItemAnySlot(partial)
            }
            Result.NO_ENOUGH_INVENTORY
        } else {
            Result.SUCCESS
        }
    }

    fun tryBuyEffectItem(p: Player, item: EbiPowerEffect): Result {
        val isFree = item.cost == 0
        if (!isFree && !tryTake(p, item.cost)) {
            return Result.NO_ENOUGH_POWER
        }
        p.addPotionEffect(item.toPotionEffect())
        return Result.SUCCESS
    }

    fun get(p: Player): Int {
        val vault = VaultPlugin.getInstance()
        return vault.getBalance(p).toInt()
    }

    fun tryGive(p: Player, amount: Int): Boolean {
        require(amount > 0) { "amountを0以下の数にはできない" }
        val vault = VaultPlugin.getInstance()
        return vault.tryDepositPlayer(p, amount.toDouble())
    }

    fun tryTake(p: Player, amount: Int): Boolean {
        require(amount > 0) { "amountを0以下の数にはできない" }
        val vault = VaultPlugin.getInstance()
        return vault.tryWithdrawPlayer(p, amount.toDouble())
    }

    enum class Result {
        SUCCESS,
        NO_ENOUGH_POWER,
        NO_ENOUGH_INVENTORY,
    }
}