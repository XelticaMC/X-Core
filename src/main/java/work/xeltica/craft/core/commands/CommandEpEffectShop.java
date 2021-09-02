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
import work.xeltica.craft.core.gui.Gui;
import work.xeltica.craft.core.gui.MenuItem;
import work.xeltica.craft.core.models.EbiPowerEffect;
import work.xeltica.craft.core.models.Hint;
import work.xeltica.craft.core.stores.EbiPowerStore;
import work.xeltica.craft.core.stores.HintStore;

/**
 * エビパワードラッグストアを開くコマンド
 * @author Xeltica
 */
public class CommandEpEffectShop extends CommandPlayerOnlyBase {

    @Override
    public boolean execute(Player player, Command command, String label, String[] args) {
        final var subCommand = args.length > 0 ? args[0] : null;
        final var store = EbiPowerStore.getInstance();

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

                store.addItem(new EbiPowerEffect(type, power, time, cost));
                player.sendMessage("追加しました。");
            }

            // エビパワーストアから商品を削除
            case "delete" -> openShopMenu(player, "削除するアイテムを選んでください", (item) -> {
                store.deleteItem(item);
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
            final var result = EbiPowerStore.getInstance().tryBuyItem(player, item);

            switch (result) {
                case NO_ENOUGH_POWER -> {
                    player.sendMessage("エビパワー不足のため、購入に失敗しました。");
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1, 0.5f);
                }
                case SUCCESS -> {
                    player.sendMessage(String.format(
                            "ポーション効果「&b%s&r」&aレベル%d&rを&6%d秒間&r付与しました。",
                            toJapanese(item.effectType()),
                            item.level(),
                            item.time()
                    ));
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1, 1);
                    HintStore.getInstance().achieve(player, Hint.EPSHOP);
                }
                default -> {
                    player.sendMessage("謎のエラーだゾ");
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
        final var store = EbiPowerStore.getInstance();
        final var items = store.getEffectShopItems()
                .stream()
                .map(m -> {
                    final var stack = new ItemStack(Material.POTION);
                    stack.editMeta(meta -> {
                        if (meta instanceof PotionMeta potion) {
                            potion.addCustomEffect(m.toPotionEffect(), true);
                        }
                    });
                    final var displayName = String.format(
                            "%s レベル%d × %d (%dEP)",
                            toJapanese(m.effectType()),
                            m.level(),
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
        return type.getName();
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, String label, String[] args) {
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
