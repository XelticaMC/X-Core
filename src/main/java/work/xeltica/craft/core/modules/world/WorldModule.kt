package work.xeltica.craft.core.modules.world

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.gui.Gui.Companion.getInstance
import work.xeltica.craft.core.utils.Config
import java.io.IOException
import java.util.function.Consumer

object WorldModule: ModuleBase() {
    private lateinit var locationConfig: Config
    private val worldNameMap = HashMap<String, String>()
    private val worldDescMap = HashMap<String, String>()
    private val lockedWorldNames = HashSet<String>()
    private val creativeWorldNames = HashSet<String>()

    private val summonVehicleWhiteList = listOf(
        "wildarea2",
        "wildarea2_nether",
        "wildarea2_the_end",
        "main",
        "nightmare2",
        "wildareab",
    )

    override fun onEnable() {
        loadWorldName()
        loadWorldDescription()
        loadLockedWorldNames()
        loadCreativeWorldNames()
        locationConfig = Config("location")
    }

    fun getWorldDisplayName(world: World): String {
        return getWorldDisplayName(world.name)
    }

    fun getWorldDisplayName(name: String): String {
        if (!worldNameMap.containsKey(name)) return name
        return worldNameMap[name]!!
    }

    fun getWorldDescription(world: World): String? {
        return getWorldDescription(world.name)
    }

    fun getWorldDescription(name: String): String? {
        if (!worldDescMap.containsKey(name)) return null
        return worldDescMap[name]
    }

    fun isCreativeWorld(world: World): Boolean {
        return isCreativeWorld(world.name)
    }

    fun isCreativeWorld(name: String): Boolean {
        return creativeWorldNames.contains(name)
    }

    fun isLockedWorld(world: World): Boolean {
        return isLockedWorld(world.name)
    }

    fun isLockedWorld(name: String): Boolean {
        return lockedWorldNames.contains(name)
    }

    fun canSummonVehicles(world: World): Boolean {
        return canSummonVehicles(world.name)
    }

    fun canSummonVehicles(name: String): Boolean {
        return summonVehicleWhiteList.contains(name)
    }

    fun saveCurrentLocation(player: Player) {
        val conf: YamlConfiguration = locationConfig.conf
        val pid = player.uniqueId.toString()
        var playerSection = conf.getConfigurationSection(pid)
        if (playerSection == null) {
            playerSection = conf.createSection(pid)
        }
        playerSection[player.world.name] = player.location
        try {
            locationConfig.save()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun getLocation(player: Player, name: String): Location? {
        val conf: YamlConfiguration = locationConfig.conf
        val pid = player.uniqueId.toString()
        val playerSection = conf.getConfigurationSection(pid) ?: return null
        return playerSection.getLocation(name)
    }

    fun teleport(player: Player, worldName: String) {
        val world = Bukkit.getWorld(worldName)
        if (world == null) {
            player.sendMessage("§bテレポートに失敗しました。ワールドが存在しないようです。")
            return
        }
        player.teleportAsync(world.spawnLocation)
    }

    fun teleportToSavedLocation(player: Player, worldName: String) {
        if (player.world.name == worldName) {
            getInstance().error(player, "既に" + getWorldDisplayName(worldName) + "にいます。")
            return
        }
        val loc = getLocation(player, worldName)
        if (loc == null) {
            // 保存されていなければ普通にTP
            teleport(player, worldName)
            return
        }
        player.teleportAsync(loc)
    }

    fun deleteSavedLocation(worldName: String) {
        val conf: YamlConfiguration = locationConfig.conf
        conf.getKeys(false).forEach(Consumer { pid: String ->
            var playerSection = conf.getConfigurationSection(pid)
            if (playerSection == null) {
                playerSection = conf.createSection(pid)
            }
            playerSection[worldName] = null
        })
        try {
            locationConfig.save()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun deleteSavedLocation(worldName: String, player: Player) {
        val conf: YamlConfiguration = locationConfig.conf
        val pid = player.uniqueId.toString()
        var playerSection = conf.getConfigurationSection(pid)
        if (playerSection == null) {
            playerSection = conf.createSection(pid)
        }
        playerSection[worldName] = null
        try {
            locationConfig.save()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun getRespawnWorld(world: World): String? {
        return getRespawnWorld(world.name)
    }

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
        worldNameMap["event2"] = "イベントワールド"
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
        lockedWorldNames.add("sandbox2")
        lockedWorldNames.add("art")
        lockedWorldNames.add("nightmare2")
    }

    private fun loadCreativeWorldNames() {
        creativeWorldNames.add("art")
        creativeWorldNames.add("sandbox2")
        creativeWorldNames.add("test")
    }
}