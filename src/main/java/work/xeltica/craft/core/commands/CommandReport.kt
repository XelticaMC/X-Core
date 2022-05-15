package work.xeltica.craft.core.commands

import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.util.StringUtil
import work.xeltica.craft.core.XCorePlugin
import kotlin.collections.ArrayList


class CommandReport: CommandBase() {
    override fun execute(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            return false
        }

        return true
    }

    override fun onTabComplete(
        commandSender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): MutableList<String> {
        if (args.last().contains(':')) {
            return complete(args)
        } else {
            return prefixComplete()
        }
    }

    private fun complete(args: Array<out String>): MutableList<String> {
        val completions = ArrayList<String>()
        if (args.last().startsWith("u:")) {
            val players = XCorePlugin.getInstance().server.onlinePlayers.map { "u:"+it.name }
            StringUtil.copyPartialMatches(args.last(), players, completions)
            completions.sort()
            return completions
        } else if (args.last().startsWith("t:")) {
            return mutableListOf("1d", "3d", "5d", "7d", "14d", "1mo", "3mo", "6mo", "12mo", "NL").map { "t:$it" }
                .toMutableList()
        }
        return COMPLETE_LIST_EMPTY
    }

    private fun prefixComplete(): MutableList<String> {
        return mutableListOf("u", "t", "r")
    }

    private val warnTemplateWithoutAfterDoing = "利用規約の「%s」に違反しています。今すぐ停止してください。本警告を無視した場合、%s。"
    private val warnTemplate = "%sは規約違反です。今すぐ停止し、%s。本警告を無視した場合、%s。"
    private val punishLogTemplate = "利用規約で禁止されている「%s」を行った"
    private val broadcastTemplate = "§c§l[報告] §r§b%s§cは「§a%s§c」により、%sされました。"

    companion object {
        private const val WILL_MUTE = "あなたの発言を今後ミュートします"
        private const val WILL_BAN = "あなたを本サーバーから追放します"
        private const val WILL_KICK = "あなたを本サーバーからキックします"
    }

    internal enum class AbuseType (
        private val shortName: String,
        private val icon: Material,
        private val punishment: String,
        private val instruction: String? = null
    ) {
        GRIEFING(
            "グリーフィング（建築物の毀損）",
            Material.DIAMOND_PICKAXE,
            WILL_BAN,
            "直ちに本来の形に修復するか、意図的でない場合はその旨を返信してください"
        ),
        STEALING("窃盗", Material.ENDER_CHEST, WILL_BAN, "盗んだアイテムを直ちに元の場所、持ち主に返却してください"),
        MONOPOLY_SHARED_ITEMS(
            "共有資産独占",
            Material.OAK_SIGN,
            WILL_BAN,
            "直ちに元の状態に戻すことで独占状態を解いてください"
        ),
        FORCED_PVP("取り決め無きPvP", Material.DIAMOND_SWORD, WILL_BAN),
        PRIVATE_INVADING(
            "無許可での私有地侵入",
            Material.OAK_DOOR,
            WILL_BAN
        ),
        OBSCENE_BUILDING(
            "わいせつ物建築", Material.RED_MUSHROOM,
            "強制撤去かつ悪質であれば$WILL_BAN", "撤去してください"
        ),
        LAW_VIOLATION_BUILDING(
            "国内法違反建築", Material.TNT,
            "強制撤去かつ悪質であれば$WILL_BAN", "撤去してください"
        ),
        INVALID_CHAT("秩序を乱すチャット", Material.PLAYER_HEAD, WILL_MUTE),
        REAL_TRADING(
            "資産の現実での取引",
            Material.GOLD_BLOCK,
            WILL_BAN
        ),
        BLACKMAIL("恐喝", Material.CROSSBOW, WILL_BAN),
        COLLUSION("共謀", Material.CAMPFIRE, WILL_BAN),
        DOS(
            "意図的に負荷をかける行為",
            Material.CAMPFIRE,
            WILL_BAN
        ),
        GLITCH("不具合悪用", Material.COMMAND_BLOCK, WILL_BAN),
        AVOID_PANISHMENT(
            "処罰回避",
            Material.TRIPWIRE_HOOK,
            WILL_BAN
        ),
        FAKE_REPORT("虚偽通報", Material.PUFFERFISH, WILL_BAN),
        IGNORE_WARN(
            "運営からのPMの無視",
            Material.PUFFERFISH,
            WILL_KICK
        ),
        EXPOSE_PM("PMの晒し上げ行為", Material.PUFFERFISH, WILL_BAN),
        INVALID_MOD(
            "不正MODの使用",
            Material.COMPARATOR,
            WILL_BAN,
            "該当するMODをアンインストールしてから参加してください（該当するMODについてわからなければ質問してください）"
        ),
        INVALID_MINING("禁止場所での資源採掘", Material.NETHERITE_PICKAXE, WILL_BAN, "破壊箇所を可能な限り修復してください"),
        SPOOF(
            "運営になりすます行為",
            Material.BEDROCK,
            WILL_BAN
        ),
        FAKE_AGE("年齢詐称行為", Material.CAKE, WILL_BAN),
        ACCOUNT_SHARING(
            "アカウント共有",
            Material.LEAD,
            WILL_BAN
        ),
        OTHER("トラブルの原因となる行為", Material.CAMPFIRE, WILL_BAN, "この後に続く指示に従ってください");
    }
}