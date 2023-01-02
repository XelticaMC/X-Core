package work.xeltica.craft.core.api.playerStore

import org.bukkit.OfflinePlayer
import work.xeltica.craft.core.api.Config
import java.util.UUID

object PlayerStore {
    private lateinit var config: Config
    fun onEnable() {
        config = Config("playerStores")
        config.useAutoSave = true
    }

    fun onDisable() {
        config.save()
    }

    fun open(player: OfflinePlayer): PlayerRecord {
        return open(player.uniqueId)
    }

    fun open(uuid: UUID): PlayerRecord {
        var section = config.conf.getConfigurationSection(uuid.toString())
        if (section == null) {
            section = config.conf.createSection(uuid.toString())
        }
        return PlayerRecord(section)
    }

    fun openAll(): List<PlayerRecord> {
        return config.conf
            .getKeys(false)
            .map { open(UUID.fromString(it)) }
            .toList()
    }
}