package work.xeltica.craft.core.modules.ebipower

import org.bukkit.entity.Player
import work.xeltica.craft.core.XCorePlugin
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.plugins.VaultPlugin
import work.xeltica.craft.core.utils.Ticks

object EbiPowerModule: ModuleBase() {
    override fun onEnable() {
        registerHandler(EbiPowerHandler())
        EbipowerObserver().runTaskTimer(XCorePlugin.instance, 0, Ticks.from(1.0).toLong())
    }

    fun get(p: Player): Int {
        val vault = VaultPlugin.getInstance()
        return vault.getBalance(p).toInt()
    }

    fun tryGive(p: Player, amount: Int): Boolean {
        require(amount > 0) { "amountを0以下の数にはできない" }
        val vault = VaultPlugin.getInstance()
        return vault.tryDepositPlayer(p, amount.toDouble())
    }

    fun tryTake(p: Player, amount: Int): Boolean {
        require(amount > 0) { "amountを0以下の数にはできない" }
        val vault = VaultPlugin.getInstance()
        return vault.tryWithdrawPlayer(p, amount.toDouble())
    }
}