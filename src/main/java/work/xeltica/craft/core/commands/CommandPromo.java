package work.xeltica.craft.core.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.query.QueryOptions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import work.xeltica.craft.core.api.commands.CommandPlayerOnlyBase;
import work.xeltica.craft.core.models.PlayerDataKey;
import work.xeltica.craft.core.modules.PlayerStoreModule;
import work.xeltica.craft.core.api.Ticks;

import java.util.List;

/**
 * 市民システムの情報表示コマンド
 * @author Xeltica
 */
public class CommandPromo extends CommandPlayerOnlyBase {
    @Override
    public boolean execute(Player sender, Command command, String label, String[] args) {
        // 情報表示対象のプレイヤー。デフォルトではコマンド送信者自身を指す
        Player player = sender;

        if (args.length > 0) {
            final var name = args[0];
            if (!sender.hasPermission("otanoshimi.command.promo.other")) {
                sender.sendMessage("§c権限がありません。");
                return true;
            }

            final var p = Bukkit.getPlayer(name);
            if (p == null) {
                sender.sendMessage("§cプレイヤーが見つかりませんでした。");
                return true;
            }
            player = p;
        }
        final var provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        final var luckPerms = provider.getProvider();
        final var lpUser = luckPerms.getPlayerAdapter(Player.class).getUser(player);
        final var record = PlayerStoreModule.open(player);
        final var isManualCitizen = lpUser.getInheritedGroups(QueryOptions.defaultContextualOptions()).stream().anyMatch(g -> g.getName().equals("citizen"));

        if (!PlayerStoreModule.isCitizen(player)) {
            sender.sendMessage("本サーバーでは、プレイヤーさんを§aわかば§r、§b市民§rという大きく2つのロールに分類しています。");
            sender.sendMessage("§b市民§rにならなくても基本的なプレイはできますが、");
            sender.sendMessage("・§c一部ブロックが使えない§r");
            sender.sendMessage("・§c一部のオリジナル機能が使えない§r");
            sender.sendMessage("という欠点があります。§b市民§rに昇格することで全ての機能が開放されます。");
        }

        if (isManualCitizen) {
            sender.sendMessage("既に手動認証されているため、あなたは市民です！");
        } else {
            final var ctx = luckPerms.getContextManager().getContext(player);
            final var linked = ctx.contains("discordsrv:linked", "true");
            final var crafterRole = ctx.contains("discordsrv:role", "クラフター");
            final var tick = record.getInt(PlayerDataKey.NEWCOMER_TIME);

            sender.sendMessage("§b§lクイック認証に必要な条件: ");
            sendMessage(sender, "Discord 連携済み", linked);
            sendMessage(sender, "クラフターロール付与済み", crafterRole);
            sendMessage(sender, "初参加から30分経過(残り" + tickToString(tick) + ")", tick == 0);
            sender.sendMessage(
                linked && crafterRole
                    ? "§b全ての条件を満たしているため、あなたは市民です！"
                    : "§cあなたはまだいくつかの条件を満たしていないため、市民ではありません。"
            );
        }
        sender.sendMessage("詳しくは https://wiki.craft.xeltica.work/citizen を確認してください！");
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

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, String[] args) {
        return null;
    }
}
