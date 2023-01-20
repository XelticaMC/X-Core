package work.xeltica.craft.core.modules.countdown

import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.entity.Player
import work.xeltica.craft.core.XCorePlugin
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.modules.hint.Hint
import work.xeltica.craft.core.modules.hint.HintModule
import work.xeltica.craft.core.utils.Ticks

/**
 * カウントダウン機能を提供します。
 */
object CountdownModule : ModuleBase() {
    override fun onEnable() {
        registerCommand("countdown", CommandCountdown())
    }

    /**
     * [members] に [count] 秒のカウントダウンを表示します。
     * 発行者として [issuer] を指定します。
     * カウントダウン終了時は [goMessage] か、それがnullであれば「GO!」と表示します。
     * [onFinish] が指定されていれば、カウントダウン終了時に呼び出されます。
     */
    fun showCountdown(count: Int, members: Set<Player>, issuer: Player? = null, onFinish: (() -> Unit)? = null, goMessage: String? = null) {
        members.forEach {
            it.sendTitle(count.toString(), "", 0, 20, 0)
            it.playSound(it.location, Sound.BLOCK_NOTE_BLOCK_BELL, SoundCategory.PLAYERS, 1f, 0.6f)
        }
        countDown(count - 1, members, onFinish, goMessage)
        issuer?.let { HintModule.achieve(it, Hint.COUNTDOWN) }
    }

    private fun countDown(count: Int, members: Set<Player>, onFinish: (() -> Unit)?, goMessage: String?) {
        Bukkit.getScheduler().runTaskLater(XCorePlugin.instance, Runnable {
            if (count > 0) {
                members.forEach {
                    it.sendTitle(count.toString(), "", 0, 20, 0)
                    it.playSound(it.location, Sound.BLOCK_NOTE_BLOCK_BELL, SoundCategory.PLAYERS, 1f, 0.6f)
                }
                countDown(count - 1, members, onFinish, goMessage)
            } else {
                members.forEach {
                    it.sendTitle(goMessage ?: "GO!", "", 0, 20, 0)
                    it.playSound(it.location, Sound.BLOCK_NOTE_BLOCK_BELL, SoundCategory.PLAYERS, 1f, 1.2f)
                    onFinish?.let { it() }
                }
            }
        }, Ticks.from(1.0).toLong())
    }
}