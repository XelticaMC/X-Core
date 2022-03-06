package work.xeltica.craft.core.models;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import work.xeltica.craft.core.stores.CloverStore;
import work.xeltica.craft.core.stores.EbiPowerStore;
import work.xeltica.craft.core.stores.HintStore;
import work.xeltica.craft.core.stores.PlayerStore;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TransferPlayerData {
    public enum TransferPlayerType {
        FROM_PLAYER,
        TO_PLAYER
    }

    public TransferPlayerData(Player from, Player to) {
        this.from = from;
        this.to = to;

        standby.put(from.getUniqueId(), this);
        standby.put(to.getUniqueId(), this);

        from.sendMessage("引っ越しの申請をしました");
        to.sendMessage("引っ越しの受け取りができます");
    }

    public TransferPlayerType getType(Player player) {
        if (player.getUniqueId() == from.getUniqueId()) {
            return TransferPlayerType.FROM_PLAYER;
        } else if (player.getUniqueId() == to.getUniqueId()) {
            return TransferPlayerType.TO_PLAYER;
        } else {
            return null;
        }
    }
    public void process() {
        from.sendMessage("引っ越しを開始します");
        to.sendMessage("引っ越しを開始します");

        transferEbiPower();
        transferHint();
        transferPlayerStoreData();
        transferClover();

        to.sendMessage("引っ越しが完了しました");
        from.kick(Component.text("引っ越しが完了しました"));

        close();
    }

    public void cancel() {
        from.sendMessage("引っ越しがキャンセルされました");
        to.sendMessage("引っ越しがキャンセルされました");
        close();
    }

    public void close() {
        standby.remove(from.getUniqueId());
        standby.remove(to.getUniqueId());
    }

    private void transferEbiPower() {
        final var ebiPowerStore = EbiPowerStore.getInstance();
        final var hasMoney = ebiPowerStore.get(from);
        ebiPowerStore.tryTake(from, hasMoney);
        ebiPowerStore.tryGive(to, hasMoney);
    }

    private void transferHint() {
        final var hintStore = HintStore.getInstance();
        for (String hintName: hintStore.getArchived(from)) {
            for (Hint hint: Hint.values()) {
                if (hint.getName().equals(hintName)) {
                    hintStore.achieve(to, hint, false);
                    break;
                }
            }
        }
        hintStore.deleteArchiveData(from);
    }

    private void transferPlayerStoreData() {
        final var playerStore = PlayerStore.getInstance();
        final var fromPlayerRecord = playerStore.open(from.getUniqueId());
        final var toPlayerRecord = playerStore.open(to.getUniqueId());
        for (PlayerDataKey key: PlayerDataKey.values()) {
            toPlayerRecord.set(key,fromPlayerRecord.get(key));
            fromPlayerRecord.delete(key);
        }
        try {
            playerStore.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void transferClover() {
        final var cloverStore = CloverStore.getInstance();
        final var hasClover = cloverStore.getCloverOf(from);
        cloverStore.set(to, hasClover);
        cloverStore.delete(from);
    }

    private final Player from;

    private final Player to;

    public static TransferPlayerData getInstance(Player player) {
        return standby.get(player.getUniqueId());
    }

    private static final Map<UUID, TransferPlayerData> standby = new HashMap<>();
}
