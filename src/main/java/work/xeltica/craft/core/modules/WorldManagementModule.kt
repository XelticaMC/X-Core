package work.xeltica.craft.core.modules

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import work.xeltica.craft.core.api.Config
import java.io.IOException
import java.util.HashSet
import java.util.HashMap
import java.util.List
import java.util.function.Consumer

/**
 * ワールドを管理するストアです。
 * @author Xeltica
 */
object WorldManagementModule : ModuleBase() {
    override fun onEnable() {
        loadWorldName()
        loadWorldDescription()
        loadLockedWorldNames()
        loadCreativeWorldNames()
        location = Config("location")
    }

    @JvmStatic
    fun getWorldDisplayName(w: World): String? {
        return getWorldDisplayName(w.name)
    }

    @JvmStatic
    fun getWorldDisplayName(n: String): String? {
        return worldNameMap[n]
    }

    @JvmStatic
    fun getWorldDescription(w: World): String? {
        return getWorldDescription(w.name)
    }

    @JvmStatic
    fun getWorldDescription(n: String): String? {
        return worldDescMap[n]
    }

    @JvmStatic
    fun isCreativeWorld(w: World): Boolean {
        return isCreativeWorld(w.name)
    }

    @JvmStatic
    fun isCreativeWorld(n: String): Boolean {
        return creativeWorldNames.contains(n)
    }

    @JvmStatic
    fun isLockedWorld(w: World): Boolean {
        return isLockedWorld(w.name)
    }

    @JvmStatic
    fun isLockedWorld(n: String): Boolean {
        return lockedWorldNames.contains(n)
    }

    @JvmStatic
    fun canSummonVehicles(w: World): Boolean {
        return canSummonVehicles(w.name)
    }

    @JvmStatic
    fun canSummonVehicles(worldName: String): Boolean {
        return List.of(*summonVehicleWhiteList).contains(worldName)
    }

    @JvmStatic
    fun saveCurrentLocation(p: Player) {
        val conf = location.conf
        val pid = p.uniqueId.toString()
        var playerSection = conf.getConfigurationSection(pid)
        if (playerSection == null) {
            playerSection = conf.createSection(pid)
        }
        playerSection[p.world.name] = p.location
        try {
            location.save()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun getLocation(p: Player, name: String?): Location? {
        val conf = location.conf
        val pid = p.uniqueId.toString()
        val playerSection = conf.getConfigurationSection(pid) ?: return null
        return playerSection.getLocation(name!!)
    }

    @JvmStatic
    fun teleport(player: Player, worldName: String?) {
        val world = Bukkit.getWorld(worldName!!)
        if (world == null) {
            player.sendMessage("§bテレポートに失敗しました。ワールドが存在しないようです。")
            return
        }
        player.teleportAsync(world.spawnLocation)
    }

    @JvmStatic
    fun teleportToSavedLocation(player: Player, worldName: String?) {
        val loc = getLocation(player, worldName)
        if (loc == null) {
            // 保存されていなければ普通にTP
            teleport(player, worldName)
            return
        }
        player.teleportAsync(loc)
    }

    @JvmStatic
    fun deleteSavedLocation(worldName: String?) {
        val conf = location.conf
        conf.getKeys(false).forEach(Consumer { pid: String? ->
            var playerSection = conf.getConfigurationSection(
                pid!!
            )
            if (playerSection == null) {
                playerSection = conf.createSection(pid)
            }
            playerSection[worldName!!] = null
        })
        try {
            location.save()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun deleteSavedLocation(worldName: String?, player: Player) {
        val conf = location.conf
        val pid = player.uniqueId.toString()
        var playerSection = conf.getConfigurationSection(pid)
        if (playerSection == null) {
            playerSection = conf.createSection(pid)
        }
        playerSection[worldName!!] = null
        try {
            location.save()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun getRespawnWorld(w: World): String? {
        return getRespawnWorld(w.name)
    }

    @JvmStatic
    fun getRespawnWorld(worldName: String): String? {
        return when (worldName) {
            "wildarea2_nether", "wildarea2_the_end" -> "wildarea2"
            "pvp", "wildareab", "hub2" -> null
            else -> worldName
        }
    }

    private fun loadWorldName() {
        worldNameMap["main"] = "メインワールド"
        worldNameMap["sandbox2"] = "サンドボックス"
        worldNameMap["art"] = "アートワールド"
        worldNameMap["nightmare2"] = "ナイトメア"
        worldNameMap["pvp"] = "PvPアリーナ"
        worldNameMap["test"] = "実験ワールド"
        worldNameMap["wildarea2"] = "共有ワールド"
        worldNameMap["wildarea2_nether"] = "共有ネザー"
        worldNameMap["wildarea2_the_end"] = "共有エンド"
        worldNameMap["wildareab"] = "資源ワールド"
        worldNameMap["shigen_nether"] = "資源ネザー"
        worldNameMap["shigen_end"] = "資源エンド"
        worldNameMap["hub2"] = "ロビー"
        worldNameMap["event"] = "イベントワールド"
    }

    private fun loadWorldDescription() {
        worldDescMap["sandbox2"] = """
            ここは、§bクリエイティブモード§rで好きなだけ遊べる§cサンドボックスワールド§r。
            元の世界の道具や経験値はお預かりしているので、好きなだけあそんでね！§7(あ、でも他の人の建築物を壊したりしないでね)
            """.trimIndent()
        worldDescMap["nightmare2"] = """
            ここは怖い敵がうじゃうじゃいる§cナイトメアワールド§r。
            手に入れたアイテムは持ち帰れます。
            """.trimIndent()
        worldDescMap["art"] = """
            ここは、§b地上絵§rに特化した§cアートワールド§r。
            元の世界の道具や経験値はお預かりしているので、安心して地上絵を作成・観覧できます！
            §7(他の人の作った地上絵を壊さないようお願いします。)
            """.trimIndent()
        worldDescMap["wildarea2"] = """
            ここは、§c共有ワールド§r。
            誰かが寄付してくれた資源を共有拠点にしまってあるので、有効にご活用ください。（独り占めはダメです）
            """.trimIndent()
        worldDescMap["wildareab"] = """
            ここは、§c資源ワールド§r。
            メインワールドで生活するための資源を探そう。
            """.trimIndent()
    }

    private fun loadLockedWorldNames() {
        lockedWorldNames.add("art")
        lockedWorldNames.add("nightmare2")
    }

    private fun loadCreativeWorldNames() {
        creativeWorldNames.add("art")
        creativeWorldNames.add("sandbox2")
        creativeWorldNames.add("test")
    }

    private lateinit var location: Config
    private val worldNameMap: MutableMap<String, String> = HashMap()
    private val worldDescMap: MutableMap<String, String> = HashMap()
    private val lockedWorldNames: MutableSet<String> = HashSet()
    private val creativeWorldNames: MutableSet<String> = HashSet()
    private val summonVehicleWhiteList = arrayOf(
        "wildarea2",
        "wildarea2_nether",
        "wildarea2_the_end",
        "main",
        "nightmare2",
        "wildareab"
    )
}