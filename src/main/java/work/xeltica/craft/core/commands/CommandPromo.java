package work.xeltica.craft.core.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.query.QueryOptions;
import work.xeltica.craft.core.models.PlayerDataKey;
import work.xeltica.craft.core.stores.PlayerStore;
import work.xeltica.craft.core.utils.Ticks;

/**
 * 市民システムの情報表示コマンド
 * @author Xeltica
 */
public class CommandPromo extends CommandPlayerOnlyBase {
    @Override
    public boolean execute(Player player, Command command, String label, String[] args) {
        final var provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        final var luckPerms = provider.getProvider();
        final var lpUser = luckPerms.getPlayerAdapter(Player.class).getUser(player);
        final var store = PlayerStore.getInstance();
        final var record = store.open(player);
        final var isManualCitizen = lpUser.getInheritedGroups(QueryOptions.defaultContextualOptions()).stream().anyMatch(g -> g.getName().equals("citizen"));

        if (!store.isCitizen(player)) {
            player.sendMessage("本サーバーでは、プレイヤーさんを§aわかば§r、§b市民§rという大きく2つのロールに分類しています。");
            player.sendMessage("§b市民§rにならなくても基本的なプレイはできますが、");
            player.sendMessage("・§c一部ブロックが使えない§r");
            player.sendMessage("・§c一部のオリジナル機能が使えない§r");
            player.sendMessage("という欠点があります。§b市民§rに昇格することで全ての機能が開放されます。§b市民§rに昇格する方法の一つに、クイック認証があります。");
        }

        if (isManualCitizen) {
            player.sendMessage("既に手動認証されているため、あなたは市民です！");
        } else {
            final var ctx = luckPerms.getContextManager().getContext(player);
            final var linked = ctx.contains("discordsrv:linked", "true");
            final var crafterRole = ctx.contains("discordsrv:role", "クラフター");
            final var citizenRole = ctx.contains("discordsrv:role", "市民");
            final var tick = record.getInt(PlayerDataKey.NEWCOMER_TIME);

            player.sendMessage("§b§lクイック認証に必要な条件: ");
            sendMessage(player, "Discord 連携済み", linked);
            sendMessage(player, "クラフターロール付与済み", crafterRole);
            sendMessage(player, "市民ロール付与済み", citizenRole);
            sendMessage(player, "初参加から30分経過(残り" + tickToString(tick) + ")", tick == 0);
            player.sendMessage(
                linked && crafterRole && citizenRole
                    ? "§b全ての条件を満たしているため、あなたは市民です！"
                    : "§cあなたはまだいくつかの条件を満たしていないため、市民ではありません。"
            );
            if (!(linked && crafterRole && citizenRole)) {
                player.sendMessage("クイック認証が面倒であれば§6§l手動認証§rもできます。");
            }
        }
        player.sendMessage("詳しくは https://wiki.craft.xeltica.work/citizen を確認してください！");
        return true;
    }

    private void sendMessage(Player player, String str, boolean isSuccess) {
        player.sendMessage((isSuccess ? "§a✔ " : "§c✘ ") + str + "§r");
    }

    private String tickToString(int tick) {
        final var elapsedTime = (int) Ticks.toTime(tick);
        final var elapsedTimeMinutes = elapsedTime / 60;
        final var elapsedTimeSeconds = elapsedTime % 60;
        return elapsedTimeMinutes > 0
            ? elapsedTimeMinutes + "分" + elapsedTimeSeconds + "秒"
            : elapsedTimeSeconds + "秒";
    }
}
