package work.xeltica.craft.core.modules.world

import org.bukkit.*
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import work.xeltica.craft.core.XCorePlugin
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.gui.Gui.Companion.getInstance
import work.xeltica.craft.core.utils.Config
import java.io.IOException
import java.util.function.Consumer

object WorldModule: ModuleBase() {
    private lateinit var locationConfig: Config
    private lateinit var worldsConfig: Config
    private val worldsMap = HashMap<String, WorldInfo>()

    override fun onEnable() {
        locationConfig = Config("location")

        if (!Config.exists("worlds")) {
            XCorePlugin.instance.saveResource("worlds.yml", false)
        }
        worldsConfig = Config("worlds")

        loadWorldInfomations()
        initializeWorlds()
    }

    fun getWorldInfo(world: World) = getWorldInfo(world.name)

    fun getWorldInfo(worldName: String) = worldsMap[worldName] ?: WorldInfo(
        worldName,
        worldName,
    )

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
            getInstance().error(player, "既に" + getWorldInfo(worldName).displayName + "にいます。")
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

    private fun loadWorldInfomations() {
        worldsMap.clear()
        worldsConfig.conf.getKeys(false).forEach {
            val section = worldsConfig.conf.getConfigurationSection(it) ?: return@forEach
            val displayName = section.getString("displayName", "")!!
            val isCitizenOnly = section.getBoolean("isCitizenOnly", false)
            val isStaffOnly = section.getBoolean("isStaffOnly", false)
            val isCreativeWorld = section.getBoolean("isCreativeWorld", false)
            val canSleep = section.getBoolean("canSleep", false)
            val canEarnEbiPower = section.getBoolean("canEarnEbiPower", false)
            val canRespawn = section.getBoolean("canRespawn", true)
            val allowVehicleSpawn = section.getBoolean("allowVehicleSpawn", false)
            val allowAdvancements = section.getBoolean("allowAdvancements", true)
            val respawnWorld = section.getString("respawnWorld", it)!!
            val description = section.getString("description", "")!!
            worldsMap[it] = WorldInfo(
                it,
                displayName,
                isCitizenOnly,
                isStaffOnly,
                isCreativeWorld,
                canSleep,
                canEarnEbiPower,
                canRespawn,
                allowVehicleSpawn,
                allowAdvancements,
                respawnWorld,
                description
            )
        }
    }

    private fun initializeWorlds() {
        getWorldInfo("nightmare2").apply {
            world.difficulty = Difficulty.HARD
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
            world.setGameRule(GameRule.DO_WEATHER_CYCLE, false)
            world.setGameRule(GameRule.MOB_GRIEFING, false)
            world.time = 18000
            world.setStorm(true)
            world.weatherDuration = 20000
            world.isThundering = true
            world.thunderDuration = 20000
        }
        worldsMap.values.forEach {
            it.world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, it.allowAdvancements)
        }
    }
}