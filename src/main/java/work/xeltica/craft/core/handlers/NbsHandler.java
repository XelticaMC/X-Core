package work.xeltica.craft.core.handlers;

import com.xxmicloxx.NoteBlockAPI.model.Song;
import com.xxmicloxx.NoteBlockAPI.songplayer.NoteBlockSongPlayer;
import com.xxmicloxx.NoteBlockAPI.utils.NBSDecoder;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.NotePlayEvent;
import org.jetbrains.annotations.Nullable;
import work.xeltica.craft.core.stores.NbsStore;

public class NbsHandler implements Listener {
    @EventHandler
    /**
     * 音を出したりなんやら
     */
    public void onNoteRedstone(BlockRedstoneEvent e) {
        final var store = NbsStore.getInstance();
        final var rs = e.getNewCurrent();
        if (rs == e.getOldCurrent()) return;

        final var distance = rs * 2;
        final var loc = e.getBlock().getLocation();

        if (store.has(loc)) {
            store.changeDistance(loc, distance);
        } else if (rs > 0) {
            final var songId = getSongId(loc);
            if (songId == null) {
                loc.getWorld().playSound(loc, Sound.BLOCK_DISPENSER_FAIL, SoundCategory.BLOCKS, 1, 1);
                return;
            }
            store.play(loc, songId, distance);
        }
    }

    @EventHandler
    /**
     * ブロック破壊したときに音を止める
     */
    public void onNoteBreak(BlockBreakEvent e) {
        final var store = NbsStore.getInstance();

        final var loc = e.getBlock().getLocation();
        if (store.has(loc)) {
            store.stop(loc);
        }
    }

    @EventHandler
    /**
     * 元々の音符ブロックの挙動をブロックする
     */
    public void onNotePlay(NotePlayEvent e) {
        final var store = NbsStore.getInstance();
        final var location = e.getBlock().getLocation();

        if (store.has(location)) e.setCancelled(true);
        if (getSongId(location) != null) e.setCancelled(true);
    }

    @Nullable
    public String getSongId(Location location) {
        final var block1 = location.add(-1, 0, 0).getBlock();
        final var block2 = location.add(1, 0, 0).getBlock();
        final var block3 = location.add(0, 0, -1).getBlock();
        final var block4 = location.add(0, 0, 1).getBlock();

        var songId = getSongIdFromSign(block1);
        if (songId == null) songId = getSongIdFromSign(block2);
        if (songId == null) songId = getSongIdFromSign(block3);
        if (songId == null) songId = getSongIdFromSign(block4);

        return songId;
    }

    @Nullable
    private String getSongIdFromSign(Block block) {
        if (Tag.WALL_SIGNS.isTagged(block.getType())) {
            if (block.getBlockData() instanceof Sign s) {
                return PlainTextComponentSerializer.plainText().serialize(s.line(0));
            }
        }
        return null;
    }
}
