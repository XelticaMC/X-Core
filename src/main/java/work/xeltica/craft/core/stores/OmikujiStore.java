package work.xeltica.craft.core.stores;

import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

import org.bukkit.entity.Player;

import work.xeltica.craft.core.models.OmikujiScore;
import work.xeltica.craft.core.utils.Config;

/**
 * プレイヤーのおみくじ記録を保存・読み出しします。
 * @author Xeltica
 */
public class OmikujiStore {
    public OmikujiStore() {
        OmikujiStore.instance = this;
        this.cm = new Config("omikujistore");
    }

    public static OmikujiStore getInstance () {
        return OmikujiStore.instance;
    }

    @Deprecated
    public String getScoreName(OmikujiScore score) {
        return score.getDisplayName();
    }

    public String getScoreName(Player player) {
        return getScoreName(get(player));
    }

    public boolean isDrawnBy(Player player) {
        return get(player) != OmikujiScore.None;
    }

    public OmikujiScore get(Player player) {
        var str = cm.getConf().getString(player.getUniqueId().toString(), OmikujiScore.None.name());
        return OmikujiScore.valueOf(str);
    }

    public void set(Player player, OmikujiScore score) {
        cm.getConf().set(player.getUniqueId().toString(), score.name());
        try {
            cm.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reset() {
        cm.getConf().getKeys(false).forEach((key) -> {
            cm.getConf().set(key, null);
        });
        try {
            cm.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public OmikujiScore generateScore() {
        var dice = random.nextInt(100000);

        if (dice == 777) return OmikujiScore.Tokudaikichi;
        if (dice == 666) return OmikujiScore.Daikyou;
        if (dice < 10000) return OmikujiScore.Daikichi;
        if (dice < 30000) return OmikujiScore.Kichi;
        if (dice < 55000) return OmikujiScore.Chukichi;
        if (dice < 80000) return OmikujiScore.Shokichi;
        return OmikujiScore.Kyou;
    }

    private Config cm;
    private static OmikujiStore instance;
    private Random random = new Random();
}