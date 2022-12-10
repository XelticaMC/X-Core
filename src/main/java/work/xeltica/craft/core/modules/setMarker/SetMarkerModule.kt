package work.xeltica.craft.core.modules.setMarker

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import work.xeltica.craft.core.XCorePlugin

import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.utils.Config
import java.io.IOException
import java.util.*
import kotlin.math.floor

object SetMarkerModule : ModuleBase(){
    lateinit var marker: Config
    val locIndex = mutableMapOf<String ,Int?>()
    val locationTemp = mutableMapOf<String ,Location>()
    val isMarkerClick = mutableMapOf<String ,Boolean>()

    /*
    ToDo 1 他プレイヤーのマーカーかどうかも判定しなければいけない
    ToDO 2 マルチでの検証ができていないのでやる。
     */

    override fun onEnable() {
        Bukkit.getLogger().info( "モジュールが読み込まれました")
        marker = Config("marker")
        registerHandler(SetMarkerHandler())
    }

    /**
     * マーカーを置きます。
     */
    fun setMarker(p: Player, loc: Location? = null) {
        loadLocIndex(p)
        var dest = Location(p.world, p.getLocation().x,p.getLocation().y,p.getLocation().z)
        if(loc != null) dest = loc
        val pid = p.getUniqueId().toString()
        var index = locIndex[pid]
        val list = getLocationList(p,p.getWorld().getName())
        if(index == null || index == -1) index = 0
        else index += 1
        addLocationList(p,dest,index)
        dest.block.type = Material.SOUL_TORCH
        dest.block.setMetadata("maker", FixedMetadataValue(XCorePlugin.instance, pid))
        locIndex[pid] = index
        if(list != null){
            if(list.size > 1 && index > 0){
                list[index-1].block.type = Material.REDSTONE_TORCH
            }
        }
        saveLocIndex(p)
    }

    /**
    マーカーコンフィグに保存されているkeyを返します。
     */
    fun getAllPrefix(): Set<String> {
        return marker.conf.getKeys(true)
    }

    /**
     * 指定した座標をブロック座標にし、マーカーコンフィグに保存します。
     * インデックスを指定した場合はリストの指定箇所に追加されます。
     * また、インデックスがリスト長を超えた場合は最後に追加されます。
     */
    fun addLocationList(p: Player, loc: Location, index: Int? = null){
        val conf = marker.getConf()
        val pid = p.getUniqueId().toString()
        var locationList : MutableList<Location>? = mutableListOf()
        var playerSection = conf?.getConfigurationSection(pid)
        if (playerSection == null) {
            playerSection = conf?.createSection(pid)
        }else{
            if(getLocationList(p, p.getWorld().getName()) != null) {
                locationList = getLocationList(p, p.getWorld().getName())
            }
        }
        if(index == null || locationList == null) locationList?.add(toBlockLocation(loc))
        else if(index >= locationList.size) locationList.add(toBlockLocation(loc))
        else locationList.add(index,toBlockLocation(loc))
        playerSection?.set(p.getWorld().getName(), locationList)
        try {
            marker.save()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * クリック対象がマーカーかどうかを判定する。
     * 0 マーカーではない
     * 1 マーカーだが自分のではない
     * 2 自分のマーカー
     */
    fun isClickMarker(p: Player, loc: Location) :Int {
        Bukkit.getLogger().info("isClick load")
        loadLocIndex(p)
        val locationList = getLocationList(p, p.getWorld().getName())
        val pid = p.getUniqueId().toString()
        val loc2 = toBlockLocation(loc)
        if(loc.block.hasMetadata("marker")){
            if(!loc.block.getMetadata("marker").equals(pid)) return 1
        }
        //isMarkerClick[pid] = false
        locationTemp[pid] = loc2
        if(locationList == null) {
            return 0
        }
        Bukkit.getLogger().info("-----------------------------------")
        for(i in locationList.indices){
            Bukkit.getLogger().info(""+i+"："+locationList[i]+"")
        }
        Bukkit.getLogger().info("-----------------------------------")
        Bukkit.getLogger().info(""+loc2)
        val index = locationList.indexOf(loc2)
        val index2 = locIndex[pid]
        if(index == -1) {
            return 0
        }
        if(index2 != null){
            locationList[index2].block.type = Material.REDSTONE_TORCH
            locationList[index].block.type = Material.SOUL_TORCH
        }
        locIndex[pid] = index
        saveLocIndex(p)
        isMarkerClick[pid] = true
        return 2
    }

    /**
     * マーカーを移動します。
     */
    fun moveMaker(p: Player, loc: Location){
        Bukkit.getLogger().info("move load")
        val conf = marker.getConf()
        val pid = p.getUniqueId().toString()
        val playerSection = conf.getConfigurationSection(pid) ?: return
        val locationList = getLocationList(p, p.getWorld().getName()) ?: return

        Bukkit.getLogger().info(locIndex.toString())
        val index = locIndex[pid] ?: return
        if(isMarkerClick[pid] == true){
            loc.set(loc.x,loc.y+1,loc.z)
            locationList[index].block.type = Material.AIR
            locationList[index] = loc
            locationList[index].block.type = Material.SOUL_TORCH
            locationList[index].block.setMetadata("maker", FixedMetadataValue(XCorePlugin.instance, pid))
        }
        playerSection.set(p.getWorld().getName(), locationList)
        try {
            marker.save()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        //isMarkerClick[pid] = false
    }

    /**
     * 今いるワールドの自身が作成したマーカーを削除します。
     */
    fun dellAllMarker(p: Player){
        val conf = marker.getConf()
        val pid = p.getUniqueId().toString()
        val world = p.getWorld().getName()
        conf?.getConfigurationSection(pid) ?: return
        val locationList : MutableList<Location> = getLocationList(p, p.getWorld().getName()) ?: return
        for(i in locationList.indices){
            val dest = Location(p.world, locationList[i].x,locationList[i].y,locationList[i].z)
            dest.block.type = Material.AIR
        }
        locIndex[pid] = null
        Bukkit.getLogger().info( "-1 : "+locIndex[pid])
        saveLocIndex(p)
        marker.conf.set("$pid.$world",null)
        try {
            marker.save()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * 今いるワールドの自身が作成したマーカーを削除します。
     */
    fun dellMarker(p: Player, loc: Location){
        val conf = marker.getConf()
        val pid = p.getUniqueId().toString()
        val world = p.getWorld().getName()
        conf?.getConfigurationSection(pid) ?: return
        val locationList : MutableList<Location> = getLocationList(p, p.getWorld().getName()) ?: return
        if(loc.block.hasMetadata("marker")){
            if(!loc.block.getMetadata("marker").equals(pid)){
                p.sendMessage("自分のマーカーではないため削除できません。")
                return
            }
            return
        }
        val i = locationList.indexOf(loc)
        val dest = Location(p.world, locationList[i].x,locationList[i].y,locationList[i].z)
        locationList.removeAt(i)
        dest.block.type = Material.AIR
        if(locIndex[pid] == 0){
            locIndex[pid] = 0
        }else if(locIndex[pid] != 0 && locIndex[pid] == i){
            locIndex[pid] = i-1
        }
        Bukkit.getLogger().info( "-2 : "+locIndex[pid])
        saveLocIndex(p)
        marker.conf.set("$pid.$world",locationList)
        try {
            marker.save()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * マーカーコンフィグから、今いるワールドの座標リストをコンソールに出力します。
     */
    fun infoMarker(p: Player){
        val locationList = getLocationList(p, p.getWorld().getName())
        if(locationList == null) {
            Bukkit.getLogger().info("List is null")
            return
        }
        for(i in locationList.indices){
            Bukkit.getLogger().info(""+i+"："+locationList[i])
        }
        Bukkit.getLogger().info("合計："+locationList.size)
    }

    /**
     * マーカーコンフィグから指定したプレイヤーとワールドの座標リストを返します。
     */
    fun getLocationList(p: Player, worldName: String): MutableList<Location>? {
        val conf = marker.getConf()
        val pid = p.getUniqueId().toString()
        val playerSection = conf?.getConfigurationSection(pid) ?: return null
        return playerSection.getList(worldName) as MutableList<Location>?
    }

    private fun saveLocIndex(p: Player){
        val conf = marker.getConf()
        val pid = p.getUniqueId().toString()
        var playerSection = conf?.getConfigurationSection(pid)
        if (playerSection == null) {
            playerSection = conf?.createSection(pid)
        }
        playerSection?.set(pid, locIndex[pid])
        try {
            marker.save()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun loadLocIndex(p: Player){
        val conf = marker.getConf()
        val pid = p.getUniqueId().toString()
        val playerSection = conf?.getConfigurationSection(pid) ?: return
        locIndex[pid] = playerSection.getInt(pid,-1)
        Bukkit.getLogger().info( ""+locIndex[pid])
    }

    /**
     * Block座標に変換します。
     * ピッチとヨーは0.0に設定されます。
     */
    fun toBlockLocation(location: Location): Location{
        location.set(floor(location.x),floor(location.y),floor(location.z))
        location.setPitch(0.0F)
        location.setYaw(0.0F)

        return location
    }

    //-----------------------------------------
    //以上マーカー座標関係　以下ツール関係
    //-----------------------------------------

    /**
     * マーカーツールADかどうか判定
     */
    fun isMarkerToolAD(item: ItemStack): Boolean{
        return isEqualItem(item, createMarkerToolAD(1))
    }

    /**
     * マーカーツールMかどうか判定
     */
    fun isMarkerToolM(item: ItemStack): Boolean{
        return isEqualItem(item, createMarkerToolM(1))
    }


    fun toolSwitching(p: Player, item: ItemStack){
        if(isMarkerToolAD(item)){
            p.getInventory().setItemInMainHand(createMarkerToolM())
        }
        if(isMarkerToolM(item)){
            p.getInventory().setItemInMainHand(createMarkerToolAD())
        }
    }

    /**
     * マーカーツール　設置・破壊専用アイテムの作成。
     */
    fun createMarkerToolAD(amount: Int = 1) : ItemStack{
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
    fun createMarkerToolM(amount: Int = 1) : ItemStack{
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

    /**
     * アイテムが同じか比較
     */
    fun isEqualItem(item1: ItemStack, item2: ItemStack) : Boolean{
        if (item1.type != item2.type) return false
        val lore1 = item1.lore() ?: return false
        val lore2 = item2.lore() ?: return false
        if (!Objects.equals(lore1[0], lore2[0])) return false
        return true
    }
}