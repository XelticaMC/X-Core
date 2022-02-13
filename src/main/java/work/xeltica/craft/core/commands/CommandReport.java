package work.xeltica.craft.core.commands;

import java.util.Arrays;
import java.util.HashSet;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import work.xeltica.craft.core.gui.Gui;
import work.xeltica.craft.core.gui.MenuItem;
import work.xeltica.craft.core.utils.DiscordService;

/**
 * 処罰コマンド
 * TODO: punish コマンドに名称変更
 * @author Xeltica
 */
public class CommandReport extends CommandPlayerOnlyBase {
    @Override
    public boolean execute(Player reporter, Command command, String label, String[] args) {
        if (args.length != 1) {
            return false;
        }
        final var playerName = args[0];
        final var reportee = Bukkit.getOfflinePlayer(Bukkit.getPlayerUniqueId(playerName));
        if (reportee == null) {
            reporter.sendMessage("そのような名前のプレイヤーはこのサーバーにはいないようです。");
            return true;
        }
        choosePunishmentType(reporter, reportee);
        return true;
    }

    /** 処罰の種類を選ぶUIを表示します */
    private void choosePunishmentType(Player reporter, OfflinePlayer reportee) {
        final Consumer<MenuItem> cb = (m) -> chooseReason(reporter, reportee, (String)m.getCustomData(), null);
        Gui.getInstance().openMenu(reporter, "処罰の種類"
            , new MenuItem("BAN", cb, Material.BARRIER, "ban")
            , new MenuItem("警告", cb, Material.BELL, "warn")
            , new MenuItem("キック", cb, Material.RABBIT_FOOT, "kick")
            , new MenuItem("ミュート", cb, Material.MUSIC_DISC_11, "mute")
        );
    }


    /** 処罰の理由を選ぶUIを表示します */
    private void chooseReason(Player reporter, OfflinePlayer reportee, String command, HashSet<AbuseType> state) {
        final var types = AbuseType.values();
        final HashSet<AbuseType> currentState = state == null ? new HashSet<>() : state;
        final var menuItems = Arrays.stream(types).map(t -> {
            return new MenuItem(t.shortName, _null -> {
                if (currentState.contains(t)) {
                    currentState.remove(t);
                } else {
                    currentState.add(t);
                }
                chooseReason(reporter, reportee, command, currentState);
            }, t.icon, null, currentState.contains(t));
        }).collect(Collectors.toList());

        menuItems.add(new MenuItem("戻る", _null -> {
            choosePunishmentType(reporter, reportee);
        }, Material.RED_WOOL));

        menuItems.add(
            new MenuItem("決定", _null -> {
                if (currentState.size() == 0) {
                    reporter.sendMessage(ChatColor.RED + "理由が指定されなかったため、何もしません。");
                    return;
                }
                if (command.equals("ban") || command.equals("mute")) {
                    chooseTime(reporter, reportee, command, currentState);
                } else {
                    takeDown(reporter, reportee, command, currentState, null);
                }
            }, Material.GREEN_WOOL)
        );

        Gui.getInstance().openMenu(reporter, command + "すべき理由（複数選択可）", menuItems.toArray(MenuItem[]::new));
    }

    /** 処罰期間を選ぶUIを表示します */
    private void chooseTime(Player reporter, OfflinePlayer reportee, String command, HashSet<AbuseType> state) {
        final Consumer<MenuItem> cb = (m) -> takeDown(reporter, reportee, command, state, (String)m.getCustomData());

        final String[] times = {
            "1d", "3d", "5d", "7d", "14d", "1mo", "3mo", "6mo", "12mo", null,
        };

        Gui.getInstance().openMenu(reporter, "期間を指定してください", Arrays.stream(times).map(
            t -> new MenuItem(convertTimeToLocaleString(t), cb, Material.LIGHT_GRAY_WOOL, t)
        ).toArray(MenuItem[]::new));
    }

    /** 処罰を下します */
    private void takeDown(Player moderator, OfflinePlayer badGuy, String command, HashSet<AbuseType> state, String time) {
        final var abuses = String.join(",", state.stream().map(s -> s.shortName).toArray(String[]::new));
        final var timeString = convertTimeToLocaleString(time);
        final var name = badGuy.getName();
        String message;
        if (command.equals("warn")) {
            if (!(badGuy instanceof Player badPlayer)) {
                moderator.sendMessage("オフラインのため、警告を送信できません。");
                return;
            }
            for (var s : state) {
                message = s.instruction == null
                    ? String.format(warnTemplateWithoutAfterDoing, s.shortName, s.punishment)
                    : String.format(warnTemplate, s.shortName, s.instruction, s.punishment);
                badPlayer.sendMessage("§c§l警告: §r§c" + message);
            }
            // 警告時は画面を暗くしたりして目立たせる
            badPlayer.showTitle(Title.title(Component.text("§e⚠警告"), Component.text("§cチャット欄を確認してください。")));
            badPlayer.playSound(badPlayer.getLocation(), Sound.BLOCK_ANVIL_PLACE, SoundCategory.PLAYERS, 1, 0.5f);
            badPlayer.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 15, 1));
            badPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 15, 30));
            return;
        } else if (command.equals("ban")) {
            message = String.format(punishLogTemplate, abuses);
            DiscordService.getInstance().reportDiscord(badGuy, abuses, timeString, command);
        } else if (command.equals("kick")) {
            message = String.format(punishLogTemplate, abuses);
        } else if (command.equals("mute")) {
            message = String.format(punishLogTemplate, abuses);
            DiscordService.getInstance().reportDiscord(badGuy, abuses, timeString, command);
        } else {
            moderator.sendMessage(ChatColor.RED + "無効なコマンド: " + command);
            return;
        }

        final var commandString = switch (command) {
            case "ban" -> "BAN";
            case "mute" -> "ミュート";
            case "kick" -> "キック";
            default -> "何か";
        };

        Bukkit.getServer().sendMessage(
            Component.text(String.format(broadcastTemplate, name, abuses, timeString + commandString))
        );

        final var cmd = time != null ? String.format("temp%s %s %s %s", command, name, time, message) : String.format("%s %s %s", command, name, message);
        moderator.performCommand(cmd);
    }

    /** 時間文字列を日本語表記に変換します */
    private String convertTimeToLocaleString(String time) {
        return time == null ? "無期限" : time.replace("d", "日間").replace("mo", "ヶ月");
    }

    private final String warnTemplateWithoutAfterDoing = "利用規約の「%s」に違反しています。今すぐ停止してください。本警告を無視した場合、%s。";
    private final String warnTemplate = "%sは規約違反です。今すぐ停止し、%s。本警告を無視した場合、%s。";
    private final String punishLogTemplate = "利用規約で禁止されている「%s」を行った";
    private final String broadcastTemplate = "§c§l[報告] §r§b%s§cは「§a%s§c」により、%sされました。";

    private static final String WILL_MUTE = "あなたの発言を今後ミュートします";
    private static final String WILL_BAN = "あなたを本サーバーから追放します";
    private static final String WILL_KICK = "あなたを本サーバーからキックします";

    /**
     * 現時点で存在する処罰一覧です。
     * TODO: enumではなくデータクラスにした上で、設定ファイルに移す
     */
    enum AbuseType {
        GRIEFING("グリーフィング（建築物の毀損）", Material.DIAMOND_PICKAXE, WILL_BAN, "直ちに本来の形に修復するか、意図的でない場合はその旨を返信してください"),
        STEALING("窃盗", Material.ENDER_CHEST, WILL_BAN, "盗んだアイテムを直ちに元の場所、持ち主に返却してください"),
        MONOPOLY_SHARED_ITEMS("共有資産独占", Material.OAK_SIGN, WILL_BAN, "直ちに元の状態に戻すことで独占状態を解いてください"),
        FORCED_PVP("取り決め無きPvP", Material.DIAMOND_SWORD, WILL_BAN),
        PRIVATE_INVADING("無許可での私有地侵入", Material.OAK_DOOR, WILL_BAN),
        OBSCENE_BUILDING("わいせつ物建築", Material.RED_MUSHROOM, "強制撤去かつ悪質であれば" + WILL_BAN, "撤去してください"),
        LAW_VIOLATION_BUILDING("国内法違反建築", Material.TNT, "強制撤去かつ悪質であれば" + WILL_BAN, "撤去してください"),
        INVALID_CHAT("秩序を乱すチャット", Material.PLAYER_HEAD, WILL_MUTE),
        REAL_TRADING("資産の現実での取引", Material.GOLD_BLOCK, WILL_BAN),
        BLACKMAIL("恐喝", Material.CROSSBOW, WILL_BAN),
        COLLUSION("共謀", Material.CAMPFIRE, WILL_BAN),
        DOS("意図的に負荷をかける行為", Material.CAMPFIRE, WILL_BAN),
        GLITCH("不具合悪用", Material.COMMAND_BLOCK, WILL_BAN),
        AVOID_PANISHMENT("処罰回避", Material.TRIPWIRE_HOOK, WILL_BAN),
        FAKE_REPORT("虚偽通報", Material.PUFFERFISH, WILL_BAN),
        IGNORE_WARN("運営からのPMの無視", Material.PUFFERFISH, WILL_KICK),
        EXPOSE_PM("PMの晒し上げ行為", Material.PUFFERFISH, WILL_BAN),
        INVALID_MOD("不正MODの使用", Material.COMPARATOR, WILL_BAN, "該当するMODをアンインストールしてから参加してください（該当するMODについてわからなければ質問してください）"),
        INVALID_MINING("禁止場所での資源採掘", Material.NETHERITE_PICKAXE, WILL_BAN, "破壊箇所を可能な限り修復してください"),
        SPOOF("運営になりすます行為", Material.BEDROCK, WILL_BAN),
        FAKE_AGE("年齢詐称行為", Material.CAKE, WILL_BAN),
        ACCOUNT_SHARING("アカウント共有", Material.LEAD, WILL_BAN),
        OTHER("トラブルの原因となる行為", Material.CAMPFIRE, WILL_BAN, "この後に続く指示に従ってください"),
        ;

        AbuseType(String shortName, Material icon, String punishment) {
            this(shortName, icon, punishment, null);
        }

		AbuseType(String shortName, Material icon, String punishment, String whatToDoToAvoidPunishment) {
            this.shortName = shortName;
            this.icon = icon;
            this.instruction = whatToDoToAvoidPunishment;
            this.punishment = punishment;
        }

        private final String shortName;
        private final Material icon;
        private final String instruction;
        private final String punishment;
    }
}
