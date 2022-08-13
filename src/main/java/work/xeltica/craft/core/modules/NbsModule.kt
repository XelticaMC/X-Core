package work.xeltica.craft.core.modules

import work.xeltica.craft.core.models.NbsModel
import work.xeltica.craft.core.models.NbsModel.PlaybackMode
import java.lang.IllegalArgumentException
import java.io.File
import com.xxmicloxx.NoteBlockAPI.model.RepeatMode
import com.xxmicloxx.NoteBlockAPI.model.SoundCategory
import com.xxmicloxx.NoteBlockAPI.songplayer.PositionSongPlayer
import com.xxmicloxx.NoteBlockAPI.songplayer.RadioSongPlayer
import java.util.UUID
import com.xxmicloxx.NoteBlockAPI.songplayer.RangeSongPlayer
import com.xxmicloxx.NoteBlockAPI.utils.NBSDecoder
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.configuration.serialization.ConfigurationSerialization
import org.bukkit.entity.Player
import work.xeltica.craft.core.api.Config
import kotlin.jvm.JvmOverloads
import java.io.IOException
import java.util.ArrayList
import java.util.HashMap
import java.util.function.Consumer

/**
 * NoteBlock Systemに関するデータを保持します。
 * @author Xeltica
 */
object NbsModule: ModuleBase() {
    override fun onEnable() {
        ConfigurationSerialization.registerClass(NbsModel::class.java, "NbsModel")
        nbs = Config("nbs")
        loadModels()
        playAll()
    }

    override fun onDisable() {
        stopAll()
    }

    /**
     * 保存されたNBSモデルを元に再生を開始します。
     */
    @JvmStatic
    fun playAll() {
        models!!.forEach(Consumer { model: NbsModel? -> playModel(model) })
    }

    /**
     * 音を全部停止します。
     */
    @JvmStatic
    fun stopAll() {
        playerCache.keys.stream().toList()
            .forEach(Consumer { location: Location -> playerCache[location]!!.isPlaying = false })
    }

    /**
     * 指定した位置で音楽を再生します。
     * @param location 再生位置
     * @param songId 音楽ID
     * @param distance 長さ
     * @param playbackMode 再生モード
     * @throws IllegalArgumentException 曲IDが存在しない
     */
    @JvmStatic
    fun play(location: Location?, songId: String, distance: Int, playbackMode: PlaybackMode?) {
        // ディレクトリトラバーサル対策
        require(!songId.startsWith("."))
        val file = getFile(songId)
        require(file.exists())
        val model = NbsModel(location, songId, distance, playbackMode)
        playModel(model)
        addModel(location, model)
    }

    /**
     * 指定したプレイヤーを対象に、音楽を再生します。
     * @param player プレイヤー
     * @param songId 音楽ID
     * @param playbackMode 再生モード
     * @throws IllegalArgumentException 曲IDが存在しない
     */
    @JvmStatic
    fun playRadio(player: Player, songId: String, playbackMode: PlaybackMode) {
        // ディレクトリトラバーサル対策
        require(!songId.startsWith("."))
        val file = getFile(songId)
        require(file.exists())
        val song = NBSDecoder.parse(getFile(songId))
        val radio = RadioSongPlayer(song, SoundCategory.VOICE)
        if (playbackMode == PlaybackMode.LOOP) {
            radio.repeatMode = RepeatMode.ALL
        }
        radio.addPlayer(player)
        radio.isPlaying = true
        radioCache[player.uniqueId] = radio
    }

    /**
     * 指定した位置での曲のdistanceをいじります。
     * @param location 再生位置
     * @param distance 長さ
     */
    @JvmStatic
    fun changeDistance(location: Location, distance: Int) {
        if (distance == 0) {
            stop(location)
        } else {
            playerCache[location]!!.distance = distance
            modelCache[location]!!.distance = distance
            saveModels()
        }
    }

    /**
     * 指定した位置にある音楽を停止します。
     * @param location 位置
     */
    @JvmStatic
    fun stop(location: Location) {
        playerCache[location]!!.isPlaying = false
        playerCache.remove(location)
        removeModel(location)
    }

    /**
     * 指定した位置にある音楽を停止します。
     * @param player プレイヤー
     */
    @JvmStatic
    fun stopRadio(player: Player) {
        val id = player.uniqueId
        if (!radioCache.containsKey(id)) return
        radioCache[id]!!.isPlaying = false
        radioCache.remove(id)
    }

    /**
     * 指定した位置に音楽があるかどうかを取得します。
     * @param location 位置
     */
    @JvmStatic
    fun has(location: Location): Boolean {
        return getPlayer(location) != null
    }

    /**
     * 指定した位置の音楽プレイヤーを取得します。
     * @param location 位置
     */
    @JvmStatic
    fun getPlayer(location: Location): RangeSongPlayer? {
        return playerCache[location]
    }

    /**
     * 指定した位置の音楽モデルを取得します。
     * @param location 位置
     */
    @JvmStatic
    fun getModel(location: Location?): NbsModel? {
        return modelCache[location]
    }

    /**
     * 指定した位置の音楽プレイヤーを取得します。
     * @param location 位置
     */
    @JvmStatic
    fun getDistance(location: Location): Int {
        return playerCache[location]!!.distance
    }

    /**
     * プレイヤーを聴衆に加えます。
     * @param p プレイヤー
     */
    @JvmStatic
    fun addAudience(p: Player?) {
        playerCache.values.forEach(Consumer { player: RangeSongPlayer -> player.addPlayer(p) })
    }

    /**
     * プレイヤーを聴衆から外します。
     * @param p プレイヤー
     */
    @JvmStatic
    fun removeAudience(p: Player?) {
        playerCache.values.forEach(Consumer { player: RangeSongPlayer -> player.removePlayer(p) })
    }
    /**
     * モデルをシステムに追加します。
     * @param location 位置
     * @param model モデル
     * @param save 保存するかどうか
     */
    /**
     * モデルをシステムに追加します。
     * @param location 位置
     * @param model モデル
     */
    @JvmOverloads
    fun addModel(location: Location?, model: NbsModel?, save: Boolean = true) {
        modelCache[location] = model
        models!!.add(model)
        saveModels()
    }

    /**
     * モデルをシステムから削除します。
     * @param location 位置
     */
    @JvmStatic
    fun removeModel(location: Location) {
        removeModel(location, true)
    }

    /**
     * モデルをシステムから削除します。
     * @param location 位置
     * @param save 保存するかどうか
     */
    private fun removeModel(location: Location, save: Boolean) {
        val model = modelCache[location]
        playerCache.remove(location)
        modelCache.remove(location)
        models!!.remove(model)
        if (save) {
            saveModels()
        }
    }

    /**
     * ディスクからモデルを読み込みます。
     */
    private fun loadModels() {
        models = nbs.conf.getList("models", ArrayList<NbsModel>()) as MutableList<NbsModel?>?
        for (model in models!!) {
            modelCache[model!!.location] = model
        }
    }

    /**
     * ディスクにモデルを書き込みます。
     */
    private fun saveModels() {
        nbs.conf["models"] = models
        try {
            nbs.save()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun getFile(songId: String): File {
        return File("$NBS_PATH$songId.nbs")
    }

    private fun playModel(model: NbsModel?) {
        val song = NBSDecoder.parse(
            getFile(
                model!!.songId
            )
        )
        val player = PositionSongPlayer(song, SoundCategory.VOICE)
        player.targetLocation = model.location
        player.distance = model.distance
        if (model.playbackMode == PlaybackMode.LOOP) {
            player.repeatMode = RepeatMode.ALL
        }
        player.isPlaying = true
        Bukkit.getOnlinePlayers().forEach { player.addPlayer(it) }
        playerCache[model.location] = player
    }

    private val playerCache: MutableMap<Location, RangeSongPlayer> = HashMap()
    private val radioCache: MutableMap<UUID, RadioSongPlayer> = HashMap()
    private var models: MutableList<NbsModel?>? = null
    private val modelCache: MutableMap<Location?, NbsModel?> = HashMap()
    private lateinit var nbs: Config
    private const val NBS_PATH = "/srv/nbs/"
}