package work.xeltica.craft.core.modules.punishment

import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.command.Command
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import work.xeltica.craft.core.api.commands.CommandPlayerOnlyBase
import work.xeltica.craft.core.gui.Gui
import work.xeltica.craft.core.gui.MenuItem
import work.xeltica.craft.core.hooks.DiscordHook

/**
 * 処罰コマンド
 * TODO: punish コマンドに名称変更
 *
 * @author Lutica
 */
class CommandReport : CommandPlayerOnlyBase() {
    private val warnTemplateWithoutAfterDoing = "%sは規約違反です。今すぐ停止してください。本警告を無視した場合、%s。"
    private val warnTemplate = "%sは規約違反です。今すぐ停止し、%s。本警告を無視した場合、%s。"
    private val punishLogTemplate = "利用規約で禁止されている「%s」を行った"
    private val broadcastTemplate = "${ChatColor.RED}${ChatColor.BOLD}[報告] ${ChatColor.RESET}${ChatColor.AQUA}%s${ChatColor.RED}：「${ChatColor.GREEN}%s${ChatColor.RED}」による規約違反で%sされました。"

    private val ui get() = Gui.getInstance()

    override fun execute(player: Player, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.size != 1) {
            ui.openTextInput(player, "処罰対象のプレイヤー名") { name: String -> choosePunishmentType(player, name) }
        } else {
            choosePunishmentType(player, args[0])
        }
        return true
    }

    /**
     * 処罰の種類を選ぶUIを表示します
     */
    private fun choosePunishmentType(reporter: Player, badPlayerName: String) {
        val onChosen: (MenuItem) -> Unit = {
            chooseReason(reporter, badPlayerName, it.customData as String?, null)
        }
        ui.openMenu(
            reporter, "処罰の種類",
            MenuItem("BAN", onChosen, Material.BARRIER, "ban"),
            MenuItem("警告", onChosen, Material.BELL, "warn"),
            MenuItem("キック", onChosen, Material.RABBIT_FOOT, "kick"),
            MenuItem("ミュート", onChosen, Material.MUSIC_DISC_11, "mute")
        )
    }

    /**
     * 処罰の理由を選ぶUIを表示します
     */
    private fun chooseReason(reporter: Player, badPlayerName: String, command: String?, state: HashSet<AbuseType>?) {
        val types = AbuseType.values()
        val currentState = state ?: HashSet()
        val menuItems = types.map { type ->
            MenuItem(type.shortName, {
                if (currentState.contains(type)) {
                    currentState.remove(type)
                } else {
                    currentState.add(type)
                }
                chooseReason(reporter, badPlayerName, command, currentState)
            }, type.icon, null, currentState.contains(type))
        }.toMutableList()
        menuItems.add(MenuItem("戻る", { choosePunishmentType(reporter, badPlayerName) }, Material.RED_WOOL))
        menuItems.add(
            MenuItem("決定", {
                if (currentState.size == 0) {
                    reporter.sendMessage(ChatColor.RED.toString() + "理由が指定されなかったため、何もしません。")
                    return@MenuItem
                }
                if (command == "ban" || command == "mute") {
                    chooseTime(reporter, badPlayerName, command, currentState)
                } else {
                    takeDown(reporter, badPlayerName, command, currentState, null)
                }
            }, Material.GREEN_WOOL)
        )
        ui.openMenu(reporter, "${command}すべき理由（複数選択可）", menuItems)
    }

    /**
     * 処罰期間を選ぶUIを表示します
     */
    private fun chooseTime(reporter: Player, badPlayerName: String, command: String, state: HashSet<AbuseType>) {
        val callback: (MenuItem) -> Unit = { takeDown(reporter, badPlayerName, command, state, it.customData as String?) }
        val times = arrayOf("1d", "3d", "5d", "7d", "14d", "1mo", "3mo", "6mo", "12mo", null)
        ui.openMenu(reporter, "期間を指定してください", times.map { MenuItem(convertTimeToLocaleString(it), callback, Material.LIGHT_GRAY_WOOL, it) })
    }

    /**
     * 処罰を下します
     */
    private fun takeDown(moderator: Player, badPlayerName: String, command: String?, state: HashSet<AbuseType>, time: String?) {
        val abuses = state.joinToString(",") { it.shortName }
        val timeString = convertTimeToLocaleString(time)
        var message: String
        when (command) {
            "warn" -> {
                val badPlayer = Bukkit.getPlayer(badPlayerName)
                if (badPlayer == null) {
                    moderator.sendMessage("オフラインのため、警告を送信できません。")
                    return
                }
                for (s in state) {
                    message = if (s.instruction == null) String.format(warnTemplateWithoutAfterDoing, s.shortName, s.punishment) else String.format(warnTemplate, s.shortName, s.instruction, s.punishment)
                    badPlayer.sendMessage("${ChatColor.RED}${ChatColor.BOLD}警告: ${ChatColor.RESET}${ChatColor.RED}${message}")
                }
                // 警告時は画面を暗くしたりして目立たせる
                badPlayer.showTitle(Title.title(Component.text("${ChatColor.YELLOW}⚠警告"), Component.text("${ChatColor.RED}チャット欄を確認してください。")))
                badPlayer.playSound(badPlayer.location, Sound.BLOCK_ANVIL_PLACE, SoundCategory.PLAYERS, 1f, 0.5f)
                badPlayer.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 20 * 15, 1))
                badPlayer.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 20 * 15, 30))
                return
            }

            "ban" -> {
                message = String.format(punishLogTemplate, abuses)
                DiscordHook.reportDiscord(badPlayerName, abuses, timeString, command)
            }

            "kick" -> {
                message = String.format(punishLogTemplate, abuses)
            }

            "mute" -> {
                message = String.format(punishLogTemplate, abuses)
                DiscordHook.reportDiscord(badPlayerName, abuses, timeString, command)
            }

            else -> {
                moderator.sendMessage(ChatColor.RED.toString() + "無効なコマンド: " + command)
                return
            }
        }
        val commandString = when (command) {
            "ban" -> "BAN"
            "mute" -> "ミュート"
            "kick" -> "キック"
            else -> "何か"
        }
        Bukkit.getServer().sendMessage(Component.text(String.format(broadcastTemplate, badPlayerName, abuses, timeString + commandString)))
        val cmd = if (time != null) String.format("temp%s %s %s %s", command, badPlayerName, time, message) else String.format("%s %s %s", command, badPlayerName, message)
        moderator.performCommand(cmd)
    }

    /**
     * 時間文字列を日本語表記に変換します
     */
    private fun convertTimeToLocaleString(time: String?): String {
        return time?.replace("d", "日間")?.replace("mo", "ヶ月") ?: "無期限"
    }

    /**
     * 現時点で存在する処罰一覧です。
     * TODO: enumではなくデータクラスにした上で、設定ファイルに移す
     */
    internal enum class AbuseType constructor(val shortName: String, val icon: Material, val punishment: String, val instruction: String? = null) {
        // 財産について
        GRIEFING("グリーフィング（建築物の毀損）", Material.DIAMOND_PICKAXE, WILL_BAN, "直ちに本来の形に修復するか、意図的でない場合はその旨を返信してください"),
        STEALING("窃盗", Material.ENDER_CHEST, WILL_BAN, "盗んだアイテムを直ちに元の場所、持ち主に返却してください"),
        MONOPOLY_SHARED_ITEMS("共有資産独占", Material.OAK_SIGN, WILL_BAN, "直ちに元の状態に戻すことで独占状態を解いてください"),
        PRIVATE_INVADING("無許可での私有地侵入", Material.OAK_DOOR, WILL_BAN),

        // 鉄道について
        ILLEGAL_TRAIN("鉄道敷設", Material.POWERED_RAIL, FORCE_REMOVE, "撤去してください"),

        // コミュニティについて
        COMMUNITY_GUIDELINE("コミュニティガイドラインに反する行為", Material.KNOWLEDGE_BOOK, WILL_BAN),
        FORCED_PVP("取り決め無きPvP", Material.DIAMOND_SWORD, WILL_BAN),
        OBSCENE_BUILDING("公序良俗に反するコンテンツの作成", Material.RED_MUSHROOM, FORCE_REMOVE, "撤去してください"),
        INVALID_CHAT("秩序を乱すチャット", Material.PLAYER_HEAD, WILL_MUTE),
        BLACKMAIL("恐喝", Material.CROSSBOW, WILL_BAN),
        EXPOSE_PM("PMの晒し上げ行為", Material.PUFFERFISH, WILL_BAN),

        // 不正行為について
        REAL_TRADING("資産の現実での取引", Material.GOLD_BLOCK, WILL_BAN),
        COLLUSION("共謀", Material.CAMPFIRE, WILL_BAN),
        DOS("意図的に負荷をかける行為", Material.CAMPFIRE, WILL_BAN),
        SMURFING("利益目的でのサブアカウント運用", Material.TOTEM_OF_UNDYING, "関連する全てのアカウントをBANします"),
        ILLEGAL_TT("無許可のTT作成", Material.SPAWNER, FORCE_REMOVE, "撤去してください"),
        GLITCH("不具合悪用", Material.COMMAND_BLOCK, WILL_BAN),
        AVOID_PANISHMENT("処罰回避", Material.TRIPWIRE_HOOK, WILL_BAN),
        FAKE_REPORT("虚偽通報", Material.PUFFERFISH, WILL_BAN),
        IGNORE_WARN("運営からのPMの無視", Material.PUFFERFISH, WILL_KICK),
        INVALID_MOD("不正MODの使用", Material.COMPARATOR, WILL_BAN, "該当するMODをアンインストールしてから参加してください（該当するMODについてわからなければ質問してください）"),
        ACCOUNT_SHARING("アカウント共有", Material.LEAD, WILL_BAN),
        INVALID_MINING("禁止場所での資源採掘", Material.NETHERITE_PICKAXE, WILL_BAN, "破壊箇所を可能な限り修復してください"),
        SPOOF("運営を妨害する行為", Material.BEDROCK, WILL_BAN),
        FAKE_AGE("年齢詐称行為", Material.CAKE, WILL_BAN),
        OTHER("トラブルの原因となる行為", Material.CAMPFIRE, WILL_BAN, "この後に続く指示に従ってください")
    }

    companion object {
        private const val WILL_MUTE = "あなたの発言を今後ミュートします"
        private const val WILL_BAN = "あなたを本サーバーからBANします"
        private const val WILL_KICK = "あなたを本サーバーからキックします"
        private const val FORCE_REMOVE = "強制撤去かつ悪質であれば$WILL_BAN"
    }
}