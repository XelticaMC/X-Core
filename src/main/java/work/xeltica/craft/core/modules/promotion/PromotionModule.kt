package work.xeltica.craft.core.modules.promotion

import net.luckperms.api.LuckPerms
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.event.node.NodeAddEvent
import net.luckperms.api.model.user.User
import net.luckperms.api.node.types.InheritanceNode
import net.luckperms.api.query.QueryOptions
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import work.xeltica.craft.core.XCorePlugin
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.api.playerStore.PlayerStore
import work.xeltica.craft.core.modules.hint.Hint
import work.xeltica.craft.core.modules.hint.HintModule
import work.xeltica.craft.core.modules.player.PlayerDataKey
import work.xeltica.craft.core.utils.Ticks

object PromotionModule : ModuleBase() {
    private lateinit var calculator: CitizenTimerCalculator
    override fun onEnable() {
        val luckPerms = Bukkit.getServicesManager().getRegistration(LuckPerms::class.java)?.provider
        if (luckPerms == null) {
            Bukkit.getLogger().severe("LuckPermsが見つかりません。")
            return
        }
        calculator = CitizenTimerCalculator()
        luckPerms.contextManager.registerCalculator(calculator)
        registerHandler(WakabaLimitHandler())
        LuckPermsProvider.get().eventBus.subscribe(XCorePlugin.instance, NodeAddEvent::class.java, this::onPlayerGotCitizen)
    }

    override fun onDisable() {
        val luckPerms = Bukkit.getServicesManager().getRegistration(LuckPerms::class.java)?.provider ?: return

        luckPerms.contextManager.unregisterCalculator(calculator)
    }

    fun isCitizen(player: Player): Boolean {
        return player.hasPermission("otanoshimi.citizen")
    }

    fun getPromoInfo(player: Player): String {
        val builder = StringBuilder()
        val luckPerms = Bukkit.getServicesManager().getRegistration(LuckPerms::class.java)?.provider ?: throw Exception()
        val lpUser = luckPerms.getPlayerAdapter(Player::class.java).getUser(player)
        val record = PlayerStore.open(player)
        val isManualCitizen = lpUser.getInheritedGroups(QueryOptions.defaultContextualOptions()).any { it.name == "citizen" }
        if (!isCitizen(player)) {
            builder.appendLine("本サーバーでは、プレイヤーさんを§aわかば§r、§b市民§rという大きく2つのロールに分類しています。")
            builder.appendLine("§b市民§rにならなくても基本的なプレイはできますが、")
            builder.appendLine("・§c一部ブロックが使えない§r")
            builder.appendLine("・§c一部のオリジナル機能が使えない§r")
            builder.appendLine("という欠点があります。§b市民§rに昇格することで全ての機能が開放されます。")
        }
        if (isManualCitizen) {
            builder.appendLine("既に手動認証されているため、あなたは市民です！")
        } else {
            val ctx = luckPerms.contextManager.getContext(player)
            val linked = ctx.contains("discordsrv:linked", "true")
            val crafterRole = ctx.contains("discordsrv:role", "クラフター")
            val tick = record.getInt(PlayerDataKey.NEWCOMER_TIME)
            builder.appendLine("§b§lクイック認証に必要な条件: ")
            builder.appendLine(getSuccessListItem("Discord 連携済み", linked))
            builder.appendLine(getSuccessListItem("クラフターロール付与済み", crafterRole))
            builder.appendLine(getSuccessListItem("初参加から30分経過(残り${tickToString(tick)})", tick == 0))
            if (linked && crafterRole) {
                builder.appendLine("§b全ての条件を満たしているため、あなたは市民です！")
            } else {
                builder.appendLine("§cあなたはまだいくつかの条件を満たしていないため、市民ではありません。")
            }
        }
        builder.appendLine("詳しくは https://wiki.craft.xeltica.work/citizen を確認してください！")

        return builder.toString()
    }

    private fun getSuccessListItem(label: String, isSuccess: Boolean): String {
        return (if (isSuccess) "§a✔ " else "§c✘ ") + label + "§r"
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

        val task = object: Runnable {
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