package work.xeltica.craft.core.stores;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import work.xeltica.craft.core.models.Hint;
import work.xeltica.craft.core.utils.Config;

public class HintStore {
    public HintStore() {
        HintStore.instance = this;
        hints = new Config("hints");
    }

    public static HintStore getInstance() {
        return HintStore.instance;
    }

    public boolean hasAchieved(Player p, Hint hint) {
        return open(p).contains(hint.name());
    }

    public void achieve(Player p, Hint hint) {
        if (hasAchieved(p, hint)) return;
        var list = open(p);
        list.add(hint.name());
        hints.getConf().set(p.getUniqueId().toString(), list);

        EbiPowerStore.getInstance().tryGive(p, hint.getPower());
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1, 1.4f);

        try {
            save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> open(Player p) {
        return open(p.getUniqueId());
    }

    private List<String> open(UUID id) {
        return hints.getConf().getStringList(id.toString());
    }

    public void save() throws IOException {
        hints.save();
    }
    
    private static HintStore instance;
    private Config hints;
}