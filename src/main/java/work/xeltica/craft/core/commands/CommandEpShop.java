package work.xeltica.craft.core.commands;

import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import work.xeltica.craft.core.XCorePlugin;
import work.xeltica.craft.core.gui.Gui;
import work.xeltica.craft.core.gui.MenuItem;
import work.xeltica.craft.core.models.EbiPowerItem;
import work.xeltica.craft.core.stores.EbiPowerStore;

/**
 * エビパワーストアを開くコマンド
 * @author Xeltica
 */
public class CommandEpShop extends CommandPlayerOnlyBase {

    @Override
    public boolean execute(Player player, Command command, String label, String[] args) {
        final var subCommand = args.length > 0 ? args[0] : null;
        final var store = EbiPowerStore.getInstance();

        // サブコマンドがなければお店UIを開く
        if (subCommand == null || !player.hasPermission("otanoshimi.command.epshop." + subCommand.toLowerCase())) {
            openShop(player);
            return true;
        }

        switch (subCommand.toLowerCase()) {
            // エビパワーストアに、手に持っている商品を追加
            case "add" -> {
                if (args.length != 2) {
                    player.sendMessage("/epshop add <cost>");
                    return true;
                }
                final var cost = Integer.parseInt(args[1]);
                final var handheld = player.getInventory().getItemInMainHand();
                if (handheld == null || handheld.getType() == Material.AIR) {
                    player.sendMessage("アイテムを手に持っていないため追加できません。");
                    return true;
                }
                store.addItem(new EbiPowerItem(handheld, cost));
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
     */
    private void openShop(Player player) {
        openShopMenu(player, "購入するアイテムを選んでください", (item) -> {
            final var result = EbiPowerStore.getInstance().tryBuyItem(player, item);

            switch (result) {
                case NO_ENOUGH_INVENTORY:
                    player.sendMessage("インベントリがいっぱいなため、購入に失敗しました。");
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1, 0.5f);
                    break;
                case NO_ENOUGH_POWER:
                    player.sendMessage("エビパワー不足のため、購入に失敗しました。");
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1, 0.5f);
                    break;
                case SUCCESS:
                    player.sendMessage(Component.text("§a" + getItemName(item.item()) + "§rを購入しました！"));
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1, 1);
                    break;
                default:
                    player.sendMessage("謎のエラーだゾ");
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1, 0.5f);
                    break;
            }
            Bukkit.getScheduler().runTask(XCorePlugin.getInstance(), () -> openShop(player));
        });
    }

    /**
     * お店のメニューを開きます。
     */
    private void openShopMenu(Player player, String title, Consumer<EbiPowerItem> onChosen) {
        final var ui = Gui.getInstance();
        final var store = EbiPowerStore.getInstance();
        final var items = store.getShopItems()
            .stream()
            .map(m -> {
                final var item = m.item();
                final var name = getItemName(item);
                final var displayName = name + "×" + item.getAmount() +  " (" + m.cost() + "EP)";
                return new MenuItem(displayName, (a) -> {
                    if (onChosen != null) {
                        onChosen.accept(m);
                    }
                }, item.getType(), null, item.getAmount());
            })
            .toList();
        ui.openMenu(player, title, items);
    }

    /**
     * 指定したアイテムスタックから名前を取得します。
     */
    private String getItemName(ItemStack item) {
        final var dn = item.getItemMeta().displayName();
        final var name = dn != null ? PlainTextComponentSerializer.plainText().serialize(dn) : item.getI18NDisplayName();
        return name;
    }
}
