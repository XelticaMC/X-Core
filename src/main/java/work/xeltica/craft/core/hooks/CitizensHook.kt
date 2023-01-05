package work.xeltica.craft.core.hooks

import net.citizensnpcs.api.CitizensAPI
import org.bukkit.Bukkit
import org.bukkit.entity.Entity
import work.xeltica.craft.core.api.HookBase

/**
 * Citizens NPC プラグインとのAPI連携フック。
 */
object CitizensHook : HookBase() {
    override val isEnabled = Bukkit.getPluginManager().getPlugin("Citizens") != null

    /**
     * Citizensプラグイン由来のNPCであるかどうかを取得します。
     */
    fun Entity.isCitizensNpc(): Boolean {
        // Citizens が読み込まれていなければ常にfalse
        if (!isEnabled) return false
        return CitizensAPI.getNPCRegistry().isNPC(this)
    }
}