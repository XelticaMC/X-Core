package work.xeltica.craft.core.models;

import org.bukkit.entity.Player;
import work.xeltica.craft.core.stores.EbiPowerStore;
import work.xeltica.craft.core.stores.HintStore;
import work.xeltica.craft.core.stores.PlayerStore;

import java.io.IOException;

public class TransferPlayerData {
    public TransferPlayerData(Player from, Player to) {
        this.from = from;
        this.to = to;
    }

    private void process() {
        transferEbiPower();
        transferHint();
        transferPlayerStoreData();
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
                    hintStore.achieve(to, hint);
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

    private final Player from;
    private final Player to;
}
