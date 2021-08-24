package work.xeltica.craft.core.handlers;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.NotePlayEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.Nullable;
import work.xeltica.craft.core.models.NbsModel;
import work.xeltica.craft.core.stores.NbsStore;

public class NbsHandler implements Listener {
    /**
     * ブロック破壊したときに音を止める
     */
    @EventHandler
    public void onNoteBreak(BlockBreakEvent e) {
        final var store = NbsStore.getInstance();
        final var loc = e.getBlock().getLocation();

        if (store.has(loc)) {
            store.stop(loc);
        }
    }

    /**
     * 元々の音符ブロックの挙動をブロックする
     */
    @EventHandler
    public void onNotePlay(NotePlayEvent e) {
        final var store = NbsStore.getInstance();
        final var location = e.getBlock().getLocation();

        final var song = getSong(location);

        if (store.has(location) && store.getModel(location).getPlaybackMode() != NbsModel.PlaybackMode.ONESHOT) {
            store.stop(location);
            e.setCancelled(true);
        } else if (song != null) {
            store.play(location, song.songId, song.distance, song.mode);
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        final var store = NbsStore.getInstance();
        store.addAudience(e.getPlayer());
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        final var store = NbsStore.getInstance();
        store.removeAudience(e.getPlayer());
    }

    @Nullable
    public NBSSignModel getSong(Location location) {
        final var block1 = location.add(-1, 0, 0).getBlock();
        final var block2 = location.add(1, 0, 0).getBlock();
        final var block3 = location.add(0, 0, -1).getBlock();
        final var block4 = location.add(0, 0, 1).getBlock();

        var songId = getSongFromSign(block1);
        if (songId == null) songId = getSongFromSign(block2);
        if (songId == null) songId = getSongFromSign(block3);
        if (songId == null) songId = getSongFromSign(block4);

        return songId;
    }

    @Nullable
    private NBSSignModel getSongFromSign(Block block) {
        if (Tag.WALL_SIGNS.isTagged(block.getType())) {
            if (block.getState() instanceof Sign s) {
                final var id = PlainTextComponentSerializer.plainText().serialize(s.line(0));
                final var distance = Integer.parseInt(PlainTextComponentSerializer.plainText().serialize(s.line(1)));
                final var modeString = PlainTextComponentSerializer.plainText().serialize(s.line(2));
                final var mode = switch (modeString.toLowerCase()) {
                    case "loop" -> NbsModel.PlaybackMode.LOOP;
                    case "oneshot" -> NbsModel.PlaybackMode.ONESHOT;
                    default -> NbsModel.PlaybackMode.NORMAL;
                };

                return new NBSSignModel(id, distance, mode);
            }
        }
        return null;
    }

    record NBSSignModel(String songId, int distance, NbsModel.PlaybackMode mode) {}
}
