package work.xeltica.craft.core.modules

import org.bukkit.configuration.serialization.ConfigurationSerialization
import org.bukkit.entity.Player
import work.xeltica.craft.core.api.Config
import work.xeltica.craft.core.models.EbiPowerItem
import java.io.IOException
import work.xeltica.craft.core.models.EbiPowerEffect
import java.util.ArrayList

/**
 * エビパワーショップの販売品管理などを行います。
 * @author Xeltica
 */
object EbipowerModule : ModuleBase() {
    override fun onEnable() {
        ConfigurationSerialization.registerClass(EbiPowerItem::class.java, "EbiPowerItem")
        ConfigurationSerialization.registerClass(EbiPowerEffect::class.java, "EbiPowerEffect")

        // エビパワー保存データを読み込む
        ep = Config("ep") { conf: Config ->
            val c = conf.conf
            shopItems = c.getList(CONFIG_KEY_SHOP_ITEMS, ArrayList<EbiPowerItem>()) as MutableList<EbiPowerItem>
            effectShopItems = c.getList(
                CONFIG_KEY_EFFECT_SHOP_ITEMS, ArrayList<EbiPowerEffect>()
            ) as MutableList<EbiPowerEffect>
        }
    }

    @JvmStatic
    fun deleteItem(item: EbiPowerItem) {
        shopItems.remove(item)
        ep.conf[CONFIG_KEY_SHOP_ITEMS] = shopItems
        try {
            ep.save()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun addItem(item: EbiPowerItem) {
        shopItems.add(item)
        ep.conf[CONFIG_KEY_SHOP_ITEMS] = shopItems
        try {
            ep.save()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun deleteItem(item: EbiPowerEffect) {
        effectShopItems.remove(item)
        ep.conf[CONFIG_KEY_EFFECT_SHOP_ITEMS] = effectShopItems
        try {
            ep.save()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun addItem(item: EbiPowerEffect) {
        effectShopItems.add(item)
        ep.conf[CONFIG_KEY_EFFECT_SHOP_ITEMS] = effectShopItems
        try {
            ep.save()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun tryBuyItem(p: Player, item: EbiPowerItem): Result {
        val isFree = item.cost() == 0
        if (!isFree && !tryTake(p, item.cost())) {
            return Result.NO_ENOUGH_POWER
        }
        val res = p.inventory.addItem(item.item().clone())
        return if (res.size != 0) {
            // 購入失敗なので返金
            if (!isFree) tryGive(p, item.cost())
            val partiallyAddedItemsCount = item.item().amount - res[0]!!.amount
            if (partiallyAddedItemsCount > 0) {
                // 部分的に追加されてしまったアイテムを剥奪
                val partial = item.item().clone()
                partial.amount = partiallyAddedItemsCount
                p.inventory.removeItemAnySlot(partial)
            }
            Result.NO_ENOUGH_INVENTORY
        } else {
            Result.SUCCESS
        }
    }

    @JvmStatic
    fun tryBuyItem(p: Player, item: EbiPowerEffect): Result {
        val isFree = item.cost() == 0
        if (!isFree && !tryTake(p, item.cost())) {
            return Result.NO_ENOUGH_POWER
        }
        p.addPotionEffect(item.toPotionEffect())
        return Result.SUCCESS
    }

    @JvmStatic
    fun tryGive(p: Player?, amount: Int): Boolean {
        require(amount > 0) { "amountを0以下の数にはできない" }
        return VaultModule.tryDepositPlayer(p, amount.toDouble())
    }

    @JvmStatic
    fun tryTake(p: Player?, amount: Int): Boolean {
        require(amount > 0) { "amountを0以下の数にはできない" }
        return VaultModule.tryWithdrawPlayer(p, amount.toDouble())
    }

    @JvmStatic
    fun getShopItems(): List<EbiPowerItem> {
        return shopItems
    }

    @JvmStatic
    operator fun get(p: Player?): Int {
        return VaultModule.getBalance(p).toInt()
    }

    @JvmStatic
    fun getEffectShopItems(): List<EbiPowerEffect> {
        return effectShopItems
    }

    private var shopItems: MutableList<EbiPowerItem> = ArrayList()
    private var effectShopItems: MutableList<EbiPowerEffect> = ArrayList()
    private lateinit var ep: Config

    private const val CONFIG_KEY_SHOP_ITEMS = "shopItems"
    private const val CONFIG_KEY_EFFECT_SHOP_ITEMS = "effectShopItems"

    enum class Result {
        SUCCESS, NO_ENOUGH_POWER, NO_ENOUGH_INVENTORY
    }
}