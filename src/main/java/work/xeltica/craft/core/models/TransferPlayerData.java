package work.xeltica.craft.core.models;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import work.xeltica.craft.core.modules.clover.CloverModule;
import work.xeltica.craft.core.modules.ebipower.EbiPowerModule;
import work.xeltica.craft.core.modules.hint.HintModule;
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

    public Player getFrom() {
        return from;
    }

    public Player getTo() {
        return to;
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
        final var ebiPowerModule = EbiPowerModule.INSTANCE;
        final var hasMoney = ebiPowerModule.get(from);
        ebiPowerModule.tryTake(from, hasMoney);
        ebiPowerModule.tryGive(to, hasMoney);
    }

    private void transferHint() {
        final var hintModule = HintModule.INSTANCE;
        for (String hintName: hintModule.getArchived(from)) {
            for (Hint hint: Hint.values()) {
                if (hint.getHintName().equals(hintName)) {
                    hintModule.achieve(to, hint, false);
                    break;
                }
            }
        }
        hintModule.deleteArchiveData(from);
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
        final var cloverStore = CloverModule.INSTANCE;
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
