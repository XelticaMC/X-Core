package work.xeltica.craft.core.modules.ebipowerShop

import org.bukkit.configuration.serialization.ConfigurationSerialization
import org.bukkit.entity.Player
import work.xeltica.craft.core.api.Config
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.modules.ebipower.EbiPowerModule
import work.xeltica.craft.core.utils.CastHelper
import java.io.IOException

/**
 * エビパワーストアおよびエビパワードラッグストアの機能を提供するモジュールです。
 */
object EbiPowerShopModule : ModuleBase() {
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
    }

    /**
     * エビパワーストアに商品を追加します。
     */
    fun addItem(item: EbiPowerItem) {
        shopItems.add(item)
        ep.conf.set(CONFIG_KEY_SHOP_ITEMS, shopItems)
        try {
            ep.save()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * エビパワーストアから商品を削除します。
     */
    fun deleteItem(item: EbiPowerItem) {
        shopItems.remove(item)
        ep.conf.set(CONFIG_KEY_SHOP_ITEMS, shopItems)
        try {
            ep.save()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * エビパワードラッグストアに商品を追加します。
     */
    fun addEffectItem(item: EbiPowerEffect) {
        effectShopItems.add(item)
        ep.conf.set(CONFIG_KEY_EFFECT_SHOP_ITEMS, effectShopItems)
        try {
            ep.save()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * エビパワードラッグストアから商品を削除します。
     */
    fun deleteEffectItem(item: EbiPowerEffect) {
        effectShopItems.remove(item)
        ep.conf.set(CONFIG_KEY_EFFECT_SHOP_ITEMS, effectShopItems)
        try {
            ep.save()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * エビパワーストアで [player] が [item] を購入するのを試みます。
     */
    fun tryBuyItem(player: Player, item: EbiPowerItem): Result {
        val isFree = item.cost == 0
        if (!isFree && !EbiPowerModule.tryTake(player, item.cost)) {
            return Result.NO_ENOUGH_POWER
        }
        val res = player.inventory.addItem(item.item.clone())
        return if (res.size != 0) {
            // 購入失敗なので返金
            if (!isFree) EbiPowerModule.tryGive(player, item.cost)
            val partiallyAddedItemsCount = item.item.amount - res[0]!!.amount
            if (partiallyAddedItemsCount > 0) {
                // 部分的に追加されてしまったアイテムを剥奪
                val partial = item.item.clone()
                partial.amount = partiallyAddedItemsCount
                player.inventory.removeItemAnySlot(partial)
            }
            Result.NO_ENOUGH_INVENTORY
        } else {
            Result.SUCCESS
        }
    }

    /**
     * エビパワードラッグストアで [player] が [item] を購入するのを試みます。
     */
    fun tryBuyEffectItem(player: Player, item: EbiPowerEffect): Result {
        val isFree = item.cost == 0
        if (!isFree && !EbiPowerModule.tryTake(player, item.cost)) {
            return Result.NO_ENOUGH_POWER
        }
        player.addPotionEffect(item.toPotionEffect())
        return Result.SUCCESS
    }

    enum class Result {
        SUCCESS,
        NO_ENOUGH_POWER,
        NO_ENOUGH_INVENTORY,
    }
}