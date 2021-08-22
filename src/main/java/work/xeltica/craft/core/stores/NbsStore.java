package work.xeltica.craft.core.stores;

import com.xxmicloxx.NoteBlockAPI.model.RepeatMode;
import com.xxmicloxx.NoteBlockAPI.model.SoundCategory;
import com.xxmicloxx.NoteBlockAPI.songplayer.PositionSongPlayer;
import com.xxmicloxx.NoteBlockAPI.songplayer.RangeSongPlayer;
import com.xxmicloxx.NoteBlockAPI.utils.NBSDecoder;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * NoteBlock Systemに関するデータを保持します。
 * @author Xeltica
 */
public class NbsStore {
    public NbsStore() {
        instance = this;
    }

    /**
     * 保存されたNBSモデルを元に再生を開始します。
     */
    public void playAll() {
        // TODO: 実装する
    }

    /**
     * 音を全部停止します。
     */
    public void stopAll() {
        playerCache.keySet().forEach(loc -> stop(loc));
    }

    /**
     * 指定した位置で音楽を再生します。
     * @param location 再生位置
     * @param songId 音楽ID
     * @param distance 長さ
     * @throws IllegalArgumentException 曲IDが存在しない
     */
    public void play(Location location, String songId, int distance) {
        // ディレクトリトラバーサル対策
        if (songId.startsWith(".")) {
            throw new IllegalArgumentException();
        }
        final var file = new File(NBS_PATH + songId + ".nbs");
        if (!file.exists()) throw new IllegalArgumentException();

        final var song = NBSDecoder.parse(file);
        final var player = new PositionSongPlayer(song, SoundCategory.VOICE);
        player.setTargetLocation(location);
        player.setDistance(distance);
        player.setRepeatMode(RepeatMode.ALL);
        player.setPlaying(true);
        Bukkit.getOnlinePlayers().forEach(p -> player.addPlayer(p));
        playerCache.put(location, player);
    }

    /**
     * 指定した位置での曲のdistanceをいじります。
     * @param location 再生位置
     * @param distance 長さ
     */
    public void changeDistance(Location location, int distance) {
        if (distance == 0) {
            stop(location);
        } else {
            playerCache.get(location).setDistance(distance);
        }
    }

    /**
     * 指定した位置にある音楽を停止します。
     * @param location 位置
     */
    public void stop(Location location) {
        playerCache.get(location).setPlaying(false);
        playerCache.remove(location);
    }

    /**
     * 指定した位置に音楽があるかどうかを取得します。
     * @param location 位置
     */
    public boolean has(Location location) {
        return get(location) != null;
    }

    /**
     * 指定した位置の音楽を取得します。
     * @param location 位置
     */
    public RangeSongPlayer get(Location location) {
        return playerCache.get(location);
    }

    /**
     * 指定した位置に音楽があるかどうかを取得します。
     * @param location 位置
     */
    public int getDistance(Location location) {
        return playerCache.get(location).getDistance();
    }

    public void addAudience(Player p) {
        playerCache.values().forEach(player -> player.addPlayer(p));
    }

    public void removeAudience(Player p) {
        playerCache.values().forEach(player -> player.removePlayer(p));
    }

    private final Map<Location, RangeSongPlayer> playerCache = new HashMap<>();

    @Getter
    private static NbsStore instance;

    private final String NBS_PATH = "/srv/nbs/";
}
