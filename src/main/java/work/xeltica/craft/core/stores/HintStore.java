package work.xeltica.craft.core.stores;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEvent.Action;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
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
        var component = p.displayName().color(TextColor.color(0x4CAF50))
            .append(Component.text("さんがヒント "))
            .append(Component
                .text(hint.getName())
                .color(TextColor.color(0x03A9F4))
                .hoverEvent(HoverEvent.hoverEvent(Action.SHOW_TEXT, Component.text(hint.getDescription())))
                .clickEvent(ClickEvent.clickEvent(net.kyori.adventure.text.event.ClickEvent.Action.RUN_COMMAND, "/hint " + hint.name())))
            .append(Component.text("を達成した！"))
            .asComponent();
        Bukkit.getServer().audiences().forEach(a -> a.sendMessage(component));
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