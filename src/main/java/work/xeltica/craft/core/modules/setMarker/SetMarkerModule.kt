package work.xeltica.craft.core.modules.setMarker

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import work.xeltica.craft.core.XCorePlugin
import work.xeltica.craft.core.api.Config
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.modules.item.ItemModule
import java.io.IOException

object SetMarkerModule : ModuleBase() {
    lateinit var marker: Config

    /*
    ToDo 1 他プレイヤーのマーカーかどうかも判定しなければいけない
    ToDO 2 マルチでの検証ができていないのでやる。
     */
    override fun onEnable() {
        Bukkit.getLogger().info("モジュールが読み込まれました")
        marker = Config("marker")
        registerHandler(SetMarkerHandler())
    }

    //マーカー本体操作
    /**
     * プレイヤーが立っている座標にマーカーを設置する
     */
    fun setMarker(p: Player) {
        setMarker(p, p.location)
    }

    /**
     * 代入された位置にマーカーを設置する
     */
    fun setMarker(p: Player, loc: Location) {
        val pid = p.uniqueId.toString()
        var index = getLocationIndex(p)
        val dest = Location(p.world, loc.x, loc.y, loc.z).toBlockLocation()
        var locationList: MutableList<Location>? = getLocationList(p)
        if (index < 0 || locationList == null) {
            Bukkit.getLogger().info("追加座標 : $dest")
            locationList?.add(dest)
            infoList(locationList) //---------------------デバック！！！！！
            index = 0
            Bukkit.getLogger().info("リスト作成")
        } else if (index >= (locationList.size - 1)) {
            locationList.add(dest)
            index = locationList.size - 1
            Bukkit.getLogger().info("リスト末尾追加")
        } else {
            index++
            locationList.add(index, dest)
            Bukkit.getLogger().info("リスト途中追加")
        }

        dest.block.type = Material.SOUL_TORCH
        dest.block.setMetadata("maker", FixedMetadataValue(XCorePlugin.instance, pid))
        if (locationList != null && locationList.size >= 2 && index >= 1) {
            locationList[index - 1].block.type = Material.REDSTONE_TORCH
        }
        saveLocationAll(p, locationList, index)
    }

    /**
     * マーカーをプレイヤーの足元に移動
     */
    fun moveMarker(p: Player) {
        moveMarker(p, p.location)
    }

    /**
     * マーカーを指定した座標に移動
     */
    fun moveMarker(p: Player, loc: Location) {
        val pid = p.uniqueId.toString()
        val index = getLocationIndex(p)
        if (index < 0) return
        val locationList = getLocationList(p) ?: return
        val dest = Location(p.world, loc.x, loc.y, loc.z).toBlockLocation()
        locationList[index].block.type = Material.AIR
        locationList[index] = dest
        locationList[index].block.type = Material.SOUL_TORCH
        locationList[index].block.setMetadata("maker", FixedMetadataValue(XCorePlugin.instance, pid))
        saveLocationList(p, locationList)
    }

    /**
     * 新しく指定したマーカーをアクティブマーカーに変更
     */
    fun changeActiveMarker(p: Player, loc: Location) {
        val newIndex = isMarkerIndex(p, loc)
        val oldIndex = getLocationIndex(p)
        changeActiveMarker(p, oldIndex, newIndex)
    }

    /**
     * アクティブマーカー[index1]を[index2]へ移動
     */
    fun changeActiveMarker(p: Player, index1: Int, index2: Int) {
        val locationList = getLocationList(p) ?: return
        locationList[index1].block.type = Material.REDSTONE_TORCH
        locationList[index2].block.type = Material.SOUL_TORCH
        saveLocationIndex(p, index2)
    }

    /**
     * 座標にあるマーカーが自分の物かそうじゃないかの判定
     * @return
     * 0 マーカーでない
     * 1 マーカーだが自分のではない
     * 2 自分のマーカー
     */
    fun isMarker(p: Player, loc: Location): Int {
        val pid = p.uniqueId.toString()
        if (loc.block.type != Material.REDSTONE_TORCH) return 0
        if (!loc.block.hasMetadata("marker")) return 0
        if (!loc.block.getMetadata("marker").equals(pid)) {
            return 1
        }
        return 2
    }

    fun isMarkerIndex(p: Player, loc: Location): Int {
        val locationList = getLocationList(p) ?: return -1
        return locationList.indexOf(loc)
    }

    /**
     * LocationListから指定した座標とマーカーを削除（マーカーでない場合は削除されたものとする）
     * @return 削除できたかどうか（成功：true 失敗:false）
     */
    fun dellMarker(p: Player, loc: Location): Boolean {
        val locationList = getLocationList(p) ?: return false
        val index = locationList.indexOf(loc)
        return dellMarker(p, index)
    }

    /**
     * LocationListから指定したインデックスの座標とマーカーを削除（マーカーでない場合は削除されたものとする）
     * @return 削除できたかどうか（成功：true 失敗:false）
     */
    fun dellMarker(p: Player, index_: Int): Boolean {
        var index = index_
        val pid = p.uniqueId.toString()
        val confIndex = getLocationIndex(p)
        val locationList = getLocationList(p) ?: return false
        if (index < 0) return false
        if (index > locationList.size - 1) return false
        val loc = locationList[index]
        if (loc.block.type != Material.REDSTONE_TORCH) return true
        if (!loc.block.hasMetadata("marker")) return true
        if (!loc.block.getMetadata("marker").equals(pid)) {
            p.sendMessage("自分のマーカーではないため削除できません。")
            return false
        }
        loc.block.type = Material.AIR
        locationList.removeAt(index)
        if (index == confIndex) { //アクティブマーカーをずらす処理
            index = (confIndex - 1)
            if (index < 0) {
                index = 0
            }
            locationList[index].block.type = Material.SOUL_TORCH
        }
        saveLocationAll(p, locationList, index)
        return true
    }

    /**
     * プレイヤーが今いるワールドでの、全マーカーとLocationListを削除
     */
    fun dellAll(p: Player) {
        val locationList = getLocationList(p) ?: return
        for (i in locationList.indices) {
            val dest = Location(p.world, locationList[i].x, locationList[i].y, locationList[i].z)
            dest.block.type = Material.AIR
        }
        deleteLocationList(p)
        deleteLocationIndex(p)
    }

    /**
     * マーカーコンフィグから、今いるワールドの座標リストをコンソールに出力します。
     */
    fun infoMarker(p: Player) {
        val locationList = getLocationList(p)
        if (locationList == null) {
            Bukkit.getLogger().info("List is null")
            return
        }
        for (i in locationList.indices) {
            Bukkit.getLogger().info("" + i + "：" + locationList[i])
        }
        Bukkit.getLogger().info("合計：" + locationList.size)
    }

    //コンフィグ関係

    /**
     * [player] の [locationList] を保存します。
     */
    fun saveLocationList(player: Player, locationList: MutableList<Location>) {
        val conf: YamlConfiguration = marker.conf
        val pid = player.uniqueId.toString()
        var playerSection = conf.getConfigurationSection(pid)
        if (playerSection == null) {
            playerSection = conf.createSection(pid)
        }
        playerSection.set(player.world.name, locationList)
        try {
            marker.save()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * [player] のマーカー番号 [index] を保存します。
     */
    fun saveLocationIndex(player: Player, index: Int) {
        val conf: YamlConfiguration = marker.conf
        val pid = player.uniqueId.toString()
        var playerSection = conf.getConfigurationSection(pid)
        if (playerSection == null) {
            playerSection = conf.createSection(pid)
        }
        playerSection.set(pid, index)
        try {
            marker.save()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun saveLocationAll(player: Player, locationList: MutableList<Location>?, index: Int) {
        val conf: YamlConfiguration = marker.conf
        val pid = player.uniqueId.toString()
        var playerSection = conf.getConfigurationSection(pid)
        if (playerSection == null) {
            playerSection = conf.createSection(pid)
        }
        playerSection.set(player.world.name, locationList)
        playerSection.set(pid, index)
        try {
            marker.save()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * [player]と紐づいている、プレイヤーが今いるワールドの座標リストを返します。
     */
    fun getLocationList(player: Player): MutableList<Location>? {
        val conf: YamlConfiguration = marker.conf
        val pid = player.uniqueId.toString()
        val playerSection = conf.getConfigurationSection(pid) ?: return null
        return playerSection.getList(player.world.name) as MutableList<Location>?
    }

    /**
     * [player]に紐づいているインデックスを返します。
     * setMarkerでは最後に追加された点として利用
     */
    fun getLocationIndex(p: Player): Int {
        val conf: YamlConfiguration = marker.conf
        val pid = p.uniqueId.toString()
        val playerSection = conf.getConfigurationSection(pid) ?: return -1
        return playerSection.getInt(pid, -1)//戻り値のデフォルト = -1
    }

    /**
     * [player]のいるワールドのLocationListを削除
     */
    fun deleteLocationList(player: Player) {
        val conf: YamlConfiguration = marker.conf
        val pid = player.uniqueId.toString()
        var playerSection = conf.getConfigurationSection(pid) ?: return
        playerSection.set(player.world.name, null)
        try {
            marker.save()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * [player]に紐づいているインデックスを削除
     */
    fun deleteLocationIndex(player: Player) {
        val conf: YamlConfiguration = marker.conf
        val pid = player.uniqueId.toString()
        val playerSection = conf.getConfigurationSection(pid) ?: return
        playerSection.set(pid, null)
        try {
            marker.save()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun getAllPrefix(): Set<String> {
        return marker.conf.getKeys(true)
    }

    //ツール関係
    /**
     * マーカーツールADかどうか判定
     */
    fun isMarkerToolAD(item: ItemStack): Boolean {
        return ItemModule.compareCustomItem(item, createMarkerToolAD())
    }

    /**
     * マーカーツールMかどうか判定
     */
    fun isMarkerToolM(item: ItemStack): Boolean {
        return ItemModule.compareCustomItem(item, createMarkerToolM())
    }

    /**
     * マーカーツールかどうか判定
     */
    fun isMarkerTool(item: ItemStack): Boolean {
        if (ItemModule.compareCustomItem(item, createMarkerToolM())) return true
        if (ItemModule.compareCustomItem(item, createMarkerToolAD())) return true
        return false
    }


    fun toolSwitching(p: Player, item: ItemStack) {
        if (isMarkerToolAD(item)) {
            p.inventory.setItemInMainHand(createMarkerToolM())
        }
        if (isMarkerToolM(item)) {
            p.inventory.setItemInMainHand(createMarkerToolAD())
        }
    }

    /**
     * マーカーツール　設置・破壊専用アイテムの作成。
     */
    fun createMarkerToolAD(amount: Int = 1): ItemStack {
        val item = ItemStack(Material.CARROT_ON_A_STICK, amount)
        item.addUnsafeEnchantment(Enchantment.DURABILITY, 1)
        item.editMeta {
            it.displayName(Component.text("マーカーツール  設置・破壊モード"))
            it.lore(listOf(
                    Component.text("マーカーを操作するツール。空中に向かって使用すると移動モードに変更。")
            ))
        }
        return item
    }

    /**
     * マーカーツール　移動専用アイテムの作成。
     */
    fun createMarkerToolM(amount: Int = 1): ItemStack {
        val item = ItemStack(Material.WARPED_FUNGUS_ON_A_STICK, amount)
        item.addUnsafeEnchantment(Enchantment.DURABILITY, 1)
        item.editMeta {
            it.displayName(Component.text("マーカーツール  移動モード"))
            it.lore(listOf(
                    Component.text("マーカーを操作するツール。空中に向かって使用すると設置・破壊モードに変更。")
            ))
        }
        return item
    }

    //その他

    /**
     * 引数からチャット欄に返すコード。主にマーカーの状態表示
     */
    fun reply(p: Player, int: Int) {
        when (int) {
            0 -> p.sendMessage("マーカー以外のブロックです")
            1 -> p.sendMessage("あなたのマーカーではありません。移動もしくは破壊したい場合は、設置した本人にお願いしてください")
            2 -> p.sendMessage("あなたのマーカーです。")
            else -> p.sendMessage("-1,0,1,2以外の引数が渡されました。")
        }
    }

    /**
     * クリックしたブロックの面に応じて、返す座標を1マスずらします。
     */
    fun offset(loc: Location, blockFace: String): Location {
        var reLoc = loc
        when (blockFace) {
            "NORTH" -> reLoc = Location(loc.world, loc.x, loc.y, loc.z - 1)
            "EAST" -> reLoc = Location(loc.world, loc.x + 1, loc.y, loc.z)
            "SOUTH" -> reLoc = Location(loc.world, loc.x, loc.y, loc.z + 1)
            "WEST" -> reLoc = Location(loc.world, loc.x - 1, loc.y, loc.z)
            "UP" -> reLoc = Location(loc.world, loc.x, loc.y + 1, loc.z)
            "DOWN" -> reLoc = Location(loc.world, loc.x, loc.y - 1, loc.z)
        }
        return reLoc
    }

    fun infoList(list: MutableList<Location>?) {
        if (list == null) {
            Bukkit.getLogger().info("list is null")
            return
        }
        for (i in list) {
            Bukkit.getLogger().info("" + i)
        }
    }
}