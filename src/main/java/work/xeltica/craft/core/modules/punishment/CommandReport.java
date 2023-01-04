package work.xeltica.craft.core.modules.punishment;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import work.xeltica.craft.core.api.commands.CommandPlayerOnlyBase;
import work.xeltica.craft.core.gui.Gui;
import work.xeltica.craft.core.gui.MenuItem;
import work.xeltica.craft.core.hooks.DiscordHook;

import java.util.Arrays;
import java.util.HashSet;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 処罰コマンド
 * TODO: punish コマンドに名称変更
 *
 * @author Xeltica
 */
public class CommandReport extends CommandPlayerOnlyBase {
    private static final String WILL_MUTE = "あなたの発言を今後ミュートします";
    private static final String WILL_BAN = "あなたを本サーバーからBANします";
    private static final String WILL_KICK = "あなたを本サーバーからキックします";
    private static final String FORCE_REMOVE = "強制撤去かつ悪質であれば" + WILL_BAN;
    private final String warnTemplateWithoutAfterDoing = "%sは規約違反です。今すぐ停止してください。本警告を無視した場合、%s。";
    private final String warnTemplate = "%sは規約違反です。今すぐ停止し、%s。本警告を無視した場合、%s。";
    private final String punishLogTemplate = "利用規約で禁止されている「%s」を行った";
    private final String broadcastTemplate = "§c§l[報告] §r§b%s§c：「§a%s§c」による規約違反で%sされました。";

    @Override
    public boolean execute(Player reporter, Command command, String label, String[] args) {
        if (args.length != 1) {
            Gui.getInstance().openTextInput(reporter, "処罰対象のプレイヤー名", name -> {
                choosePunishmentType(reporter, name);
            });
        } else {
            choosePunishmentType(reporter, args[0]);
        }
        return true;
    }

    /**
     * 処罰の種類を選ぶUIを表示します
     */
    private void choosePunishmentType(Player reporter, String badPlayerName) {
        final Consumer<MenuItem> cb = (m) -> chooseReason(reporter, badPlayerName, (String) m.getCustomData(), null);
        Gui.getInstance().openMenu(reporter, "処罰の種類"
                , new MenuItem("BAN", cb, Material.BARRIER, "ban")
                , new MenuItem("警告", cb, Material.BELL, "warn")
                , new MenuItem("キック", cb, Material.RABBIT_FOOT, "kick")
                , new MenuItem("ミュート", cb, Material.MUSIC_DISC_11, "mute")
        );
    }

    /**
     * 処罰の理由を選ぶUIを表示します
     */
    private void chooseReason(Player reporter, String badPlayerName, String command, HashSet<AbuseType> state) {
        final var types = AbuseType.values();
        final HashSet<AbuseType> currentState = state == null ? new HashSet<>() : state;
        final var menuItems = Arrays.stream(types).map(t -> new MenuItem(t.shortName, _null -> {
            if (currentState.contains(t)) {
                currentState.remove(t);
            } else {
                currentState.add(t);
            }
            chooseReason(reporter, badPlayerName, command, currentState);
        }, t.icon, null, currentState.contains(t))).collect(Collectors.toList());

        menuItems.add(new MenuItem("戻る", _null -> {
            choosePunishmentType(reporter, badPlayerName);
        }, Material.RED_WOOL));

        menuItems.add(
                new MenuItem("決定", _null -> {
                    if (currentState.size() == 0) {
                        reporter.sendMessage(ChatColor.RED + "理由が指定されなかったため、何もしません。");
                        return;
                    }
                    if (command.equals("ban") || command.equals("mute")) {
                        chooseTime(reporter, badPlayerName, command, currentState);
                    } else {
                        takeDown(reporter, badPlayerName, command, currentState, null);
                    }
                }, Material.GREEN_WOOL)
        );

        Gui.getInstance().openMenu(reporter, command + "すべき理由（複数選択可）", menuItems.toArray(MenuItem[]::new));
    }

    /**
     * 処罰期間を選ぶUIを表示します
     */
    private void chooseTime(Player reporter, String badPlayerName, String command, HashSet<AbuseType> state) {
        final Consumer<MenuItem> cb = (m) -> takeDown(reporter, badPlayerName, command, state, (String) m.getCustomData());

        final String[] times = {
                "1d", "3d", "5d", "7d", "14d", "1mo", "3mo", "6mo", "12mo", null,
        };

        Gui.getInstance().openMenu(reporter, "期間を指定してください", Arrays.stream(times).map(
                t -> new MenuItem(convertTimeToLocaleString(t), cb, Material.LIGHT_GRAY_WOOL, t)
        ).toArray(MenuItem[]::new));
    }

    /**
     * 処罰を下します
     */
    private void takeDown(Player moderator, String badPlayerName, String command, HashSet<AbuseType> state, String time) {
        final var abuses = String.join(",", state.stream().map(s -> s.shortName).toArray(String[]::new));
        final var timeString = convertTimeToLocaleString(time);
        String message;
        if (command.equals("warn")) {
            final var badPlayer = Bukkit.getPlayer(badPlayerName);
            if (badPlayer == null) {
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
            DiscordHook.reportDiscord(badPlayerName, abuses, timeString, command);
        } else if (command.equals("kick")) {
            message = String.format(punishLogTemplate, abuses);
        } else if (command.equals("mute")) {
            message = String.format(punishLogTemplate, abuses);
            DiscordHook.reportDiscord(badPlayerName, abuses, timeString, command);
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

        Bukkit.getServer().sendMessage(Component.text(String.format(broadcastTemplate, badPlayerName, abuses, timeString + commandString)));

        final var cmd = time != null ? String.format("temp%s %s %s %s", command, badPlayerName, time, message) : String.format("%s %s %s", command, badPlayerName, message);
        moderator.performCommand(cmd);
    }

    /**
     * 時間文字列を日本語表記に変換します
     */
    private String convertTimeToLocaleString(String time) {
        return time == null ? "無期限" : time.replace("d", "日間").replace("mo", "ヶ月");
    }

    /**
     * 現時点で存在する処罰一覧です。
     * TODO: enumではなくデータクラスにした上で、設定ファイルに移す
     */
    enum AbuseType {
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
        OTHER("トラブルの原因となる行為", Material.CAMPFIRE, WILL_BAN, "この後に続く指示に従ってください"),
        ;

        private final String shortName;
        private final Material icon;
        private final String instruction;
        private final String punishment;

        AbuseType(String shortName, Material icon, String punishment) {
            this(shortName, icon, punishment, null);
        }

        AbuseType(String shortName, Material icon, String punishment, String whatToDoToAvoidPunishment) {
            this.shortName = shortName;
            this.icon = icon;
            this.instruction = whatToDoToAvoidPunishment;
            this.punishment = punishment;
        }
    }
}
