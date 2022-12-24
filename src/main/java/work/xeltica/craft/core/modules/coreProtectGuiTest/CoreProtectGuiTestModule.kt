package work.xeltica.craft.core.modules.coreProtectGuiTest

import org.bukkit.Bukkit
import work.xeltica.craft.core.api.ModuleBase

/**
 * CoreProtectのコマンドを良い感じにXPhoneから叩くようにするアプリのモジュール
 */
object CoreProtectGuiTestModule : ModuleBase() {
    override fun onEnable() {
        // むかつくので消さない
        Bukkit.getLogger().info("モジュールをボンジュール～")
    }

}