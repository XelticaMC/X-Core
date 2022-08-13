package work.xeltica.craft.core.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import work.xeltica.craft.core.XCorePlugin;
import work.xeltica.craft.core.api.commands.CommandPlayerOnlyBase;
import work.xeltica.craft.core.gui.Gui;
import work.xeltica.craft.core.models.MenuItem;
import work.xeltica.craft.core.models.EbiPowerEffect;
import work.xeltica.craft.core.models.Hint;
import work.xeltica.craft.core.modules.EbipowerModule;
import work.xeltica.craft.core.modules.HintModule;

/**
 * エビパワードラッグストアを開くコマンド
 * @author Xeltica
 */
public class CommandEpEffectShop extends CommandPlayerOnlyBase {

    @Override
    public boolean execute(Player player, Command command, String label, String[] args) {
        final var subCommand = args.length > 0 ? args[0] : null;

        // サブコマンドがなければお店UIを開く
        if (subCommand == null || !player.hasPermission("otanoshimi.command.epeffectshop." + subCommand.toLowerCase())) {
            openShop(player);
            return true;
        }

        switch (subCommand.toLowerCase()) {
            // エビパワーストアに、手に持っている商品を追加
            case "add" -> {
                if (args.length != 5) {
                    player.sendMessage("/epeffectshop add <type> <power> <time> <cost>");
                    return true;
                }

                final var type = PotionEffectType.getByName(args[1]);
                if (type == null) {
                    player.sendMessage("/epeffectshop add <type> <power> <time> <cost>");
                    return true;
                }

                final var power = Integer.parseInt(args[2]);
                final var time = Integer.parseInt(args[3]);
                final var cost = Integer.parseInt(args[4]);

                EbipowerModule.addItem(new EbiPowerEffect(type, power, time, cost));
                player.sendMessage("追加しました。");
            }

            // エビパワーストアから商品を削除
            case "delete" -> openShopMenu(player, "削除するアイテムを選んでください", (item) -> {
                EbipowerModule.deleteItem(item);
                player.sendMessage("削除しました。");
            });
        }
        return true;
    }

    /**
     * 購入用のUIを開きます。
     * @param player UIを開くプレイヤー
     */
    private void openShop(Player player) {
        openShopMenu(player, "購入するステータス効果を選んでください", (item) -> {
            final var result = EbipowerModule.tryBuyItem(player, item);

            switch (result) {
                case NO_ENOUGH_POWER -> {
                    player.sendMessage("エビパワー不足のため、購入に失敗しました。");
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1, 0.5f);
                }
                case SUCCESS -> {
                    player.sendMessage(String.format(
                            "§b%s%s§rを§6%d秒間§r付与しました。",
                            toJapanese(item.effectType()),
                            item.level() > 1 ? Integer.toString(item.level()) : "",
                            item.time()
                    ));
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1, 1);
                    HintModule.achieve(player, Hint.EPEFFECTSHOP);
                }
                default -> {
                    player.sendMessage("不明なエラーが発生しました。");
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1, 0.5f);
                }
            }
            Bukkit.getScheduler().runTask(XCorePlugin.getInstance(), () -> openShop(player));
        });
    }

    /**
     * お店のメニューを開きます。
     * @param player メニューを開くプレイヤー
     * @param title メニューのタイトル
     * @param onChosen お店のメニューの一覧
     */
    private void openShopMenu(Player player, String title, Consumer<EbiPowerEffect> onChosen) {
        final var ui = Gui.getInstance();
        final var items = EbipowerModule.getEffectShopItems()
                .stream()
                .map(m -> {
                    final var stack = new ItemStack(Material.POTION);
                    stack.editMeta(meta -> {
                        if (meta instanceof PotionMeta potion) {
                            potion.addCustomEffect(m.toPotionEffect(), true);
                            potion.setColor(m.effectType().getColor());
                        }
                    });
                    final var displayName = String.format(
                            "%s%s %d秒 (%dEP)",
                            toJapanese(m.effectType()),
                            m.level() > 1 ? Integer.toString(m.level()) : "",
                            m.time(),
                            m.cost()
                    );

                    return new MenuItem(displayName, (a) -> {
                        if (onChosen != null) {
                            onChosen.accept(m);
                        }
                    }, stack);
                })
                .toList();
        ui.openMenu(player, title, items);
    }

    private String toJapanese(PotionEffectType type) {
        // TODO マップから変換
        return switch (type.getName()) {
            case "SPEED" -> "移動速度上昇";
            case "SLOW" -> "移動速度低下";
            case "FAST_DIGGING" -> "採掘速度上昇";
            case "SLOW_DIGGING" -> "採掘速度低下";
            case "INCREASE_DAMAGE" -> "攻撃力上昇";
            case "HEAL" -> "即時回復";
            case "HARM" -> "即時ダメージ";
            case "JUMP" -> "跳躍力上昇";
            case "CONFUSION" -> "吐き気";
            case "REGENERATION" -> "再生能力";
            case "DAMAGE_RESISTANCE" -> "耐性";
            case "FIRE_RESISTANCE" -> "火炎耐性";
            case "WATER_BREATHING" -> "水中呼吸";
            case "INVISIBILITY" -> "透明化";
            case "BLINDNESS" -> "盲目";
            case "NIGHT_VISION" -> "暗視";
            case "HUNGER" -> "空腹";
            case "WEAKNESS" -> "弱体化";
            case "POISON" -> "毒";
            case "WITHER" -> "衰弱";
            case "HEALTH_BOOST" -> "体力増強";
            case "ABSORPTION" -> "衝撃吸収";
            case "SATURATION" -> "満腹度回復";
            case "GLOWING" -> "発光";
            case "LEVITATION" -> "浮遊";
            case "LUCK" -> "幸運";
            case "UNLUCK" -> "不運";
            case "SLOW_FALLING" -> "落下速度低下";
            case "CONDUIT_POWER" -> "コンジットパワー";
            case "DOLPHINS_GRACE" -> "イルカの好意";
            case "BAD_OMEN" -> "不吉な予感";
            case "HERO_OF_THE_VILLAGE" -> "村の英雄";
            default -> "不明";
        };
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 1) {
            final var commands = Arrays.asList("add", "delete");
            final var completions = new ArrayList<String>();
            StringUtil.copyPartialMatches(args[0], commands, completions);
            Collections.sort(completions);
            return completions;
        } else if (args.length == 2) {
            return Arrays.stream(PotionEffectType.values()).map(PotionEffectType::getName).toList();
        }
        return COMPLETE_LIST_EMPTY;
    }
}
