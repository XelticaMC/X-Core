package work.xeltica.craft.core.stores;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import work.xeltica.craft.core.XCorePlugin;
import work.xeltica.craft.core.models.HubType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

/**
 * クラシックロビーのコマンド看板の保存・読み出しを行います。
 * @author Xeltica
 */
public class HubStore {
    public HubStore() {
        HubStore.instance = this;
    }

    public static HubStore getInstance() {
        return HubStore.instance;
    }

    public void teleport(Player player, HubType type) {
        teleport(player, type, false);
    }

    public void teleport(Player player, HubType hub, boolean bulk) {
        final var playerWorld = player.getWorld();
        final var world = Bukkit.getWorld(hub.getWorldName());

        if (world == null) {
            player.sendMessage("未生成");
            return;
        }

        final var isWarping = isWarpingMap.get(player.getUniqueId());
        if (isWarping != null && isWarping) {
            player.sendMessage("移動中です！");
            return;
        }

        if (playerWorld.getUID().equals(world.getUID())) {
            player.sendMessage("既にロビーです！");
            return;
        }

        final var currentWorldName = playerWorld.getName();
        final var requireCooldown = bulk || Arrays.stream(noCooldownWorldNames)
                .anyMatch(name -> name.equalsIgnoreCase(currentWorldName));

        Bukkit.getScheduler().runTaskLater(XCorePlugin.getInstance(), () -> {
            if (hub.getLocation() != null) {
                player.teleportAsync(hub.getSpigotLocation(), TeleportCause.PLUGIN);
            } else {
                WorldStore.getInstance().teleport(player, hub.getWorldName());
            }
            isWarpingMap.put(player.getUniqueId(), false);
        }, requireCooldown ? 1 : 20 * 5);
        if (!requireCooldown) {
            player.sendMessage("5秒後にロビーに移動します...");
            isWarpingMap.put(player.getUniqueId(), true);
        }
    }

    private final String[] noCooldownWorldNames = {
        "art",
        "pvp",
        "test",
        "hub2",
        "hub_dev",
        "main",
        "sandbox2",
    };

    private static HubStore instance;

    private final HashMap<UUID, Boolean> isWarpingMap = new HashMap<>();
}
