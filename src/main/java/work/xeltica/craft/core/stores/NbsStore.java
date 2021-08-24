package work.xeltica.craft.core.stores;

import com.xxmicloxx.NoteBlockAPI.model.RepeatMode;
import com.xxmicloxx.NoteBlockAPI.model.SoundCategory;
import com.xxmicloxx.NoteBlockAPI.songplayer.PositionSongPlayer;
import com.xxmicloxx.NoteBlockAPI.songplayer.RangeSongPlayer;
import com.xxmicloxx.NoteBlockAPI.utils.NBSDecoder;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import work.xeltica.craft.core.models.EbiPowerItem;
import work.xeltica.craft.core.models.NbsModel;
import work.xeltica.craft.core.utils.Config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * NoteBlock Systemに関するデータを保持します。
 * @author Xeltica
 */
public class NbsStore {
    public NbsStore() {
        ConfigurationSerialization.registerClass(EbiPowerItem.class, "EbiPowerItem");
        loadModels();
        playAll();
        instance = this;
    }

    /**
     * 保存されたNBSモデルを元に再生を開始します。
     */
    public void playAll() {
        models.forEach(this::playModel);
    }

    /**
     * 音を全部停止します。
     */
    public void stopAll() {
        playerCache.keySet().forEach(location -> {
            playerCache.get(location).setPlaying(false);
        });
    }

    /**
     * 指定した位置で音楽を再生します。
     * @param location 再生位置
     * @param songId 音楽ID
     * @param distance 長さ
     * @param playbackMode 再生モード
     * @throws IllegalArgumentException 曲IDが存在しない
     */
    public void play(Location location, String songId, int distance, NbsModel.PlaybackMode playbackMode) {
        // ディレクトリトラバーサル対策
        if (songId.startsWith(".")) throw new IllegalArgumentException();
        final var file = getFile(songId);
        if (!file.exists()) throw new IllegalArgumentException();

        final var model = new NbsModel(location, songId, distance, playbackMode);
        playModel(model);
        addModel(location, model);
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
            modelCache.get(location).setDistance(distance);
            saveModels();
        }
    }

    /**
     * 指定した位置にある音楽を停止します。
     * @param location 位置
     */
    public void stop(Location location) {
        playerCache.get(location).setPlaying(false);
        playerCache.remove(location);
        removeModel(location);
    }

    /**
     * 指定した位置に音楽があるかどうかを取得します。
     * @param location 位置
     */
    public boolean has(Location location) {
        return getPlayer(location) != null;
    }

    /**
     * 指定した位置の音楽プレイヤーを取得します。
     * @param location 位置
     */
    public RangeSongPlayer getPlayer(Location location) {
        return playerCache.get(location);
    }

    /**
     * 指定した位置の音楽モデルを取得します。
     * @param location 位置
     */
    public NbsModel getModel(Location location) {
        return modelCache.get(location);
    }

    /**
     * 指定した位置の音楽プレイヤーを取得します。
     * @param location 位置
     */
    public int getDistance(Location location) {
        return playerCache.get(location).getDistance();
    }

    /**
     * プレイヤーを聴衆に加えます。
     * @param p プレイヤー
     */
    public void addAudience(Player p) {
        playerCache.values().forEach(player -> player.addPlayer(p));
    }

    /**
     * プレイヤーを聴衆から外します。
     * @param p プレイヤー
     */
    public void removeAudience(Player p) {
        playerCache.values().forEach(player -> player.removePlayer(p));
    }

    /**
     * モデルをシステムに追加します。
     * @param location 位置
     * @param model モデル
     */
    private void addModel(Location location, NbsModel model) {
        addModel(location, model, true);
    }

    /**
     * モデルをシステムに追加します。
     * @param location 位置
     * @param model モデル
     * @param save 保存するかどうか
     */
    private void addModel(Location location, NbsModel model, boolean save) {
        modelCache.put(location, model);
        models.add(model);
        saveModels();
    }

    /**
     * モデルをシステムから削除します。
     * @param location 位置
     */
    private void removeModel(Location location) {
        removeModel(location, true);
    }

    /**
     * モデルをシステムから削除します。
     * @param location 位置
     * @param save 保存するかどうか
     */
    private void removeModel(Location location, boolean save) {
        final var model = modelCache.get(location);
        modelCache.remove(location);
        models.remove(model);
        saveModels();
    }

    /**
     * ディスクからモデルを読み込みます。
     */
    private void loadModels() {
        models = (List<NbsModel>)nbs.getConf().getList("models", new ArrayList<NbsModel>());
    }

    /**
     * ディスクにモデルを書き込みます。
     */
    private void saveModels() {
        nbs.getConf().set("models", models);
        try {
            nbs.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File getFile(String songId) {
        return new File(NBS_PATH + songId + ".nbs");
    }

    private void playModel(NbsModel model) {
        final var song = NBSDecoder.parse(getFile(model.getSongId()));
        final var player = new PositionSongPlayer(song, SoundCategory.VOICE);
        player.setTargetLocation(model.getLocation());
        player.setDistance(model.getDistance());

        if (model.getPlaybackMode() == NbsModel.PlaybackMode.LOOP) {
            player.setRepeatMode(RepeatMode.ALL);
        }
        player.setPlaying(true);
        Bukkit.getOnlinePlayers().forEach(player::addPlayer);

        playerCache.put(model.getLocation(), player);
    }


    private final Map<Location, RangeSongPlayer> playerCache = new HashMap<>();
    private List<NbsModel> models;
    private final Map<Location, NbsModel> modelCache = new HashMap<>();
    private final Config nbs = new Config("nbs");

    @Getter
    private static NbsStore instance;

    private static final String NBS_PATH = "/srv/nbs/";
}
