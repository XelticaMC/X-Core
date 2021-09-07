package work.xeltica.craft.core.stores;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEvent.Action;
import net.kyori.adventure.text.format.TextColor;
import work.xeltica.craft.core.models.Hint;
import work.xeltica.craft.core.utils.Config;

/**
 * プレイヤーのヒントを達成する処理や、ヒントを達成しているかどうかの取得などを行います。
 * @author Xeltica
 */
public class HintStore {
    public HintStore() {
        HintStore.instance = this;
        hints = new Config("hints");
    }

    public boolean hasAchieved(Player p, Hint hint) {
        return open(p).contains(hint.name());
    }

    public void achieve(Player p, Hint hint) {
        if (hasAchieved(p, hint)) return;
        final var list = open(p);
        list.add(hint.name());
        hints.getConf().set(p.getUniqueId().toString(), list);

        EbiPowerStore.getInstance().tryGive(p, hint.getPower());
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1, 1.4f);
        final var component = p.displayName().color(TextColor.color(0x4CAF50))
            .append(Component.text("さんがヒント "))
            .append(Component
                .text(hint.getName())
                .color(TextColor.color(0x03A9F4))
                .hoverEvent(HoverEvent.hoverEvent(Action.SHOW_TEXT, Component.text(hint.getDescription())))
                .clickEvent(ClickEvent.clickEvent(net.kyori.adventure.text.event.ClickEvent.Action.RUN_COMMAND, "/hint " + hint.name())))
            .append(Component.text("を達成した！"))
            .asComponent();
        Bukkit.getServer().audiences().forEach(a -> {
            a.sendMessage(component);
            if (hint.getType() == Hint.HintType.CHALLENGE) {
                a.playSound(net.kyori.adventure.sound.Sound.sound(Key.key("ui.toast.challenge_complete"), net.kyori.adventure.sound.Sound.Source.PLAYER, 1, 1));
            }
        });
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

    @Getter
    private static HintStore instance;
    private final Config hints;
}
