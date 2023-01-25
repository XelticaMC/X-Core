package work.xeltica.craft.core.modules.setMarker

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import work.xeltica.craft.core.api.Config
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.modules.item.ItemModule
import java.io.IOException

object SetMarkerModule : ModuleBase() {
    lateinit var marker: Config

    /*
    ToDO マルチでの検証ができていないのでやる。
     */
    override fun onEnable() {
        Bukkit.getLogger().info("モジュールが読み込まれました")
        marker = Config("marker")
        registerCommand("marker", SetMarkerCommand())
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
    fun setMarker(p: Player, loc: Location, face: String? = null) {
        val dest: Location
        if (replaceable.contains(loc.block.type)) {
            dest = offset(loc, null)
        } else {
            dest = offset(loc, face)
            if (!replaceable.contains(dest.block.type)) {
                return
            }
        }
        var index = getLocationIndex(p)

        if (dest.block.type == Material.WATER) {
            p.sendMessage("水中には置けません。")
            return
        }
        var locationList: MutableList<Location>? = getLocationList(p)
        if (index < 0 || locationList == null) {
            locationList = mutableListOf(dest)//nullの時は.addだと動かない
            index = 0
        } else if (index >= (locationList.size - 1)) {
            locationList.add(dest)
            index = locationList.size - 1
        } else {
            index++
            locationList.add(index, dest)
        }
        dest.block.type = Material.SOUL_TORCH

        if (locationList.size >= 2 && index >= 1) {
            locationList[index - 1].block.type = Material.REDSTONE_TORCH
        }
        saveLocationAll(p, locationList, index)
    }

    /**
     * アクティブマーカーをプレイヤーの足元に移動
     */
    fun moveMarker(p: Player) {
        moveMarker(p, p.location, null)
    }

    /**
     * アクティブマーカ―を指定した場所に移動
     */
    fun moveMarker(p: Player, loc: Location, face: String?) {
        val index = getLocationIndex(p)
        moveMarker(p, index, loc, face)
    }

    /**
     * 座標[loc1]にあるマーカ―を指定した座標[loc2]に移動
     */
    fun moveMarker(p: Player, loc1: Location, loc2: Location) {
        val index = isMarkerIndex(p, loc1)
        if (index < 0) {
            return
        }
        moveMarker(p, index, loc2, null)
    }

    /**
     * 番号指定したマーカーを指定した座標に移動
     */
    fun moveMarker(p: Player, index: Int, loc: Location, face: String?) {
        if (index < 0) return
        val locationList = getLocationList(p) ?: return
        val dest: Location
        if (replaceable.contains(loc.block.type)) {
            dest = offset(loc, null)
        } else {
            dest = offset(loc, face)
            if (!replaceable.contains(dest.block.type)) {
                return
            }
        }
        if (dest.block.type == Material.WATER) {
            p.sendMessage("水中には移動できません")
            return
        }
        locationList[index].block.type = Material.AIR
        locationList[index] = dest
        locationList[index].block.type = Material.SOUL_TORCH
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
        if (loc.block.type != Material.REDSTONE_TORCH && loc.block.type != Material.SOUL_TORCH) return 0
        val thisMarker = searchLocationPid(loc, p.world.name) ?: return 0
        return if (thisMarker != pid) 1 else 2
    }

    /**
     * プレイヤーに紐づいているLocationListの中に[loc]があるかどうか
     */
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
        val index = locationList.indexOf(loc.toBlockLocation())
        return dellMarker(p, index)
    }

    /**
     * LocationListから指定したインデックスの座標とマーカーを削除（自分のマーカーでない場合は失敗する）
     * @return 削除できたかどうか（成功：true 失敗:false）
     */
    fun dellMarker(p: Player, index_: Int): Boolean {
        var index = index_
        val confIndex = getLocationIndex(p)
        val locationList = getLocationList(p) ?: return false
        if (index < 0) index = 0
        if (index >= locationList.size) index = locationList.size - 1
        val loc = locationList[index]
        if (isMarker(p, loc) != 2) return false
        loc.block.type = Material.AIR
        locationList.removeAt(index)
        if (index == confIndex) { //アクティブマーカーをずらす処理
            index = (confIndex - 1)
            if (index < 0) {
                index = 0
            }
            if (locationList.size > 0) {
                locationList[index].block.type = Material.SOUL_TORCH
            }
            saveLocationAll(p, locationList, index)
        }
        saveLocationList(p, locationList)
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
        // ToDo チャット欄に座標リストをページを分けて出力できるようにする。
        val locationList = getLocationList(p)
        if (locationList == null) {
            p.sendMessage("このワールドに所有マーカーはありません")
            return
        }


        Bukkit.getLogger().info("----------------------------------------")
        for (i in locationList.indices) {
            Bukkit.getLogger().info("" + i + "：" + locationList[i])
        }
        Bukkit.getLogger().info("合計：" + locationList.size)
        Bukkit.getLogger().info("----------------------------------------")

        p.sendMessage("合計：" + locationList.size)
    }

    /**
     * マーカーを再設置します。
     */
    fun reposition(p: Player) {
        val locationList = getLocationList(p)
        val index = getLocationIndex(p)
        if (locationList == null) {
            p.sendMessage("このワールドに所有マーカーはありません")
            return
        }
        for (i in locationList.indices) {
            locationList[i].block.type = Material.REDSTONE_TORCH
            if (i == index) locationList[i].block.type = Material.SOUL_TORCH
        }
    }

    //コンフィグ関係

    /**
     * [player] の [locationList] を保存します。
     */
    fun saveLocationList(p: Player, locationList: MutableList<Location>) {
        val conf: YamlConfiguration = marker.conf
        val pid = p.uniqueId.toString()
        var playerSection = conf.getConfigurationSection(pid)
        if (playerSection == null) {
            playerSection = conf.createSection(pid)
        }
        playerSection.set(p.world.name, locationList)
        try {
            marker.save()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * [player] のマーカー番号 [index] を保存します。
     */
    fun saveLocationIndex(p: Player, index: Int) {
        val conf: YamlConfiguration = marker.conf
        val pid = p.uniqueId.toString()
        var playerSection = conf.getConfigurationSection(pid)
        if (playerSection == null) {
            playerSection = conf.createSection(pid)
        }
        playerSection.set(p.world.name + " index", index)
        try {
            marker.save()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * 座標リストとアクティブマーカーのインデックスを保存
     */
    fun saveLocationAll(p: Player, locationList: MutableList<Location>?, index: Int) {
        val conf: YamlConfiguration = marker.conf
        val pid = p.uniqueId.toString()
        var playerSection = conf.getConfigurationSection(pid)
        if (playerSection == null) {
            playerSection = conf.createSection(pid)
        }
        playerSection.set(p.world.name, locationList)
        playerSection.set(p.world.name + " index", index)
        try {
            marker.save()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * [player]と紐づいている、プレイヤーが今いるワールドの座標リストを返します。
     */
    fun getLocationList(p: Player): MutableList<Location>? {
        val conf: YamlConfiguration = marker.conf
        val pid = p.uniqueId.toString()
        val playerSection = conf.getConfigurationSection(pid) ?: return mutableListOf()
        return playerSection.getList(p.world.name) as MutableList<Location>?
    }

    /**
     * [pid]と[worldName]から座標リストを返します。
     */
    fun getLocationList(pid: String, worldName: String): MutableList<Location>? {
        val conf: YamlConfiguration = marker.conf
        val playerSection = conf.getConfigurationSection(pid) ?: return mutableListOf()
        return playerSection.getList(worldName) as MutableList<Location>?
    }

    /**
     * [player]に紐づいているインデックスを返します。
     * setMarkerでは最後に追加された点として利用
     */
    fun getLocationIndex(p: Player): Int {
        val conf: YamlConfiguration = marker.conf
        val pid = p.uniqueId.toString()
        val playerSection = conf.getConfigurationSection(pid) ?: return -1
        return playerSection.getInt(p.world.name + " index", -1)//戻り値のデフォルト = -1
    }

    /**
     * [player]のいるワールドのLocationListを削除
     */
    fun deleteLocationList(p: Player) {
        val conf: YamlConfiguration = marker.conf
        val pid = p.uniqueId.toString()
        val playerSection = conf.getConfigurationSection(pid) ?: return
        playerSection.set(p.world.name, null)
        try {
            marker.save()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * [player]に紐づいているインデックスを削除
     */
    fun deleteLocationIndex(p: Player) {
        val conf: YamlConfiguration = marker.conf
        val pid = p.uniqueId.toString()
        val playerSection = conf.getConfigurationSection(pid) ?: return
        playerSection.set(p.world.name + " index", null)
        try {
            marker.save()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * すべての座標のなかに座標があるか検索します。
     * @return その座標があったpidを返します。なかった場合はnullが返ります
     */
    fun searchLocationPid(location: Location, worldName: String): String? {
        val loc = Location(location.world, location.x, location.y, location.z).toBlockLocation()
        val keyList = marker.conf.getKeys(false).toList()
        for (pid in keyList) {
            val locationList = getLocationList(pid, worldName)
            if (locationList != null && locationList.contains(loc)) {
                return pid
            }
        }
        return null
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

    /**
     * ツールを交換
     */
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
     * クリックしたブロックの面に応じて、返す座標を1マスずらします。
     */
    fun offset(loc: Location, blockFace: String?): Location {
        var reLoc = loc
        if (blockFace == null) return reLoc
        when (blockFace) {
            "NORTH" -> reLoc = Location(loc.world, loc.x, loc.y, loc.z - 1).toBlockLocation()
            "EAST" -> reLoc = Location(loc.world, loc.x + 1, loc.y, loc.z).toBlockLocation()
            "SOUTH" -> reLoc = Location(loc.world, loc.x, loc.y, loc.z + 1).toBlockLocation()
            "WEST" -> reLoc = Location(loc.world, loc.x - 1, loc.y, loc.z).toBlockLocation()
            "UP" -> reLoc = Location(loc.world, loc.x, loc.y + 1, loc.z).toBlockLocation()
            "DOWN" -> reLoc = Location(loc.world, loc.x, loc.y - 1, loc.z).toBlockLocation()
        }
        return reLoc
    }

    fun tildaToLocation(player: Player, x_: String, y_: String, z_: String): Location? {
        var x = 0.0
        var y = 0.0
        var z = 0.0
        if (x_.equals("~")) {
            x = player.location.x
        } else {
            try {
                x = x_.toDouble()
            } catch (e: NumberFormatException) {
                return null
            }

        }
        if (y_.equals("~")) {
            y = player.location.y
        } else {
            try {
                x = x_.toDouble()
            } catch (e: NumberFormatException) {
                return null
            }
        }
        if (z_.equals("~")) {
            z = player.location.z
        } else {
            try {
                x = x_.toDouble()
            } catch (e: NumberFormatException) {
                return null
            }
        }
        return Location(player.world, x, y, z)
    }

    /**
     * 置換可能ブロックリスト
     */
    private val replaceable = listOf(
            Material.AIR,
            Material.GRASS,
            Material.FERN,
            Material.TALL_GRASS,
            Material.LARGE_FERN,
            Material.VINE,
    )
}