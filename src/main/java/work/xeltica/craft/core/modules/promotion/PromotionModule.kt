package work.xeltica.craft.core.modules.promotion

import net.luckperms.api.LuckPerms
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.event.node.NodeAddEvent
import net.luckperms.api.model.user.User
import net.luckperms.api.node.types.InheritanceNode
import net.luckperms.api.query.QueryOptions
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import work.xeltica.craft.core.XCorePlugin
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.api.playerStore.PlayerStore
import work.xeltica.craft.core.modules.hint.Hint
import work.xeltica.craft.core.modules.hint.HintModule
import work.xeltica.craft.core.utils.Ticks

/**
 * 市民昇格制度を管理するモジュールです。
 */
object PromotionModule : ModuleBase() {
    const val PS_KEY_NEWCOMER_TIME = "newcomer_time"

    private lateinit var calculator: CitizenTimerCalculator

    override fun onEnable() {
        hookLuckPerms()
        registerNewcomerTimer()

        registerHandler(PromotionHandler())
        registerCommand("promo", CommandPromo())
    }

    override fun onDisable() {
        val luckPerms = Bukkit.getServicesManager().getRegistration(LuckPerms::class.java)?.provider ?: return
        luckPerms.contextManager.unregisterCalculator(calculator)
    }

    /**
     * [player] が市民ロールかどうかを取得します。
     */
    fun isCitizen(player: Player): Boolean {
        return player.hasPermission("otanoshimi.citizen")
    }

    /**
     * [player] の市民昇格条件などを含むメッセージを取得します。
     */
    fun getPromoInfo(player: Player): String {
        val builder = StringBuilder()
        val luckPerms = Bukkit.getServicesManager().getRegistration(LuckPerms::class.java)?.provider ?: throw Exception()
        val lpUser = luckPerms.getPlayerAdapter(Player::class.java).getUser(player)
        val record = PlayerStore.open(player)
        val isManualCitizen = lpUser.getInheritedGroups(QueryOptions.defaultContextualOptions()).any { it.name == "citizen" }
        if (!isCitizen(player)) {
            builder.appendLine("本サーバーでは、プレイヤーさんを${ChatColor.GREEN}わかば${ChatColor.RESET}、${ChatColor.AQUA}市民${ChatColor.RESET}という大きく2つのロールに分類しています。")
            builder.appendLine("${ChatColor.AQUA}市民${ChatColor.RESET}にならなくても基本的なプレイはできますが、")
            builder.appendLine("・${ChatColor.RED}一部ブロックが使えない${ChatColor.RESET}")
            builder.appendLine("・${ChatColor.RED}一部のオリジナル機能が使えない${ChatColor.RESET}")
            builder.appendLine("という欠点があります。${ChatColor.AQUA}市民${ChatColor.RESET}に昇格することで全ての機能が開放されます。")
        }
        if (isManualCitizen) {
            builder.appendLine("既に手動認証されているため、あなたは市民です！")
        } else {
            val ctx = luckPerms.contextManager.getContext(player)
            val linked = ctx.contains("discordsrv:linked", "true")
            val crafterRole = ctx.contains("discordsrv:role", "クラフター")
            val tick = record.getInt(PS_KEY_NEWCOMER_TIME)
            builder.appendLine("${ChatColor.AQUA}${ChatColor.BOLD}クイック認証に必要な条件: ")
            builder.appendLine(getSuccessListItem("Discord 連携済み", linked))
            builder.appendLine(getSuccessListItem("クラフターロール付与済み", crafterRole))
            builder.appendLine(getSuccessListItem("初参加から30分経過(残り${tickToString(tick)})", tick == 0))
            if (linked && crafterRole) {
                builder.appendLine("${ChatColor.AQUA}全ての条件を満たしているため、あなたは市民です！")
            } else {
                builder.appendLine("${ChatColor.RED}あなたはまだいくつかの条件を満たしていないため、市民ではありません。")
            }
        }
        builder.appendLine("詳しくは https://wiki.craft.xeltica.work/citizen を確認してください！")

        return builder.toString()
    }

    private fun hookLuckPerms() {
        val luckPerms = Bukkit.getServicesManager().getRegistration(LuckPerms::class.java)?.provider
        if (luckPerms == null) {
            Bukkit.getLogger().severe("LuckPermsが見つかりません。")
            return
        }
        calculator = CitizenTimerCalculator()
        luckPerms.contextManager.registerCalculator(calculator)
        LuckPermsProvider.get().eventBus.subscribe(XCorePlugin.instance, NodeAddEvent::class.java, this::onPlayerGotCitizen)
    }

    private fun registerNewcomerTimer() {
        val tick = Ticks.from(1.0)
        object : BukkitRunnable() {
            override fun run() {
                Bukkit.getOnlinePlayers().forEach {
                    val record = PlayerStore.open(it)
                    var time = record.getInt(PS_KEY_NEWCOMER_TIME, 0)
                    time -= tick
                    if (time <= 0) {
                        record.delete(PS_KEY_NEWCOMER_TIME)
                    } else {
                        record[PS_KEY_NEWCOMER_TIME] = time
                    }
                }
            }
        }.runTaskTimer(XCorePlugin.instance, 0, tick.toLong())
    }

    private fun getSuccessListItem(label: String, isSuccess: Boolean): String {
        return (if (isSuccess) "${ChatColor.GREEN}✔ " else "${ChatColor.RED}✘ ") + label + "${ChatColor.RESET}"
    }

    private fun tickToString(tick: Int): String {
        val elapsedTime = Ticks.toTime(tick).toInt()
        val elapsedTimeMinutes = elapsedTime / 60
        val elapsedTimeSeconds = elapsedTime % 60
        return if (elapsedTimeMinutes > 0)
            "${elapsedTimeMinutes}分${elapsedTimeSeconds}秒" + elapsedTimeSeconds + "秒"
        else
            "${elapsedTimeSeconds}秒"
    }

    private fun onPlayerGotCitizen(e: NodeAddEvent) {
        if (!e.isUser) return
        val target = e.target as User
        val node = e.node

        val task = object : Runnable {
            override fun run() {
                val player = Bukkit.getPlayer(target.uniqueId) ?: return
                if (node is InheritanceNode && "citizen" == node.groupName) {
                    HintModule.achieve(player, Hint.BE_CITIZEN)
                }
            }
        }

        Bukkit.getScheduler().runTask(XCorePlugin.instance, task)
    }
}