package work.xeltica.craft.core.modules.nbs

import com.xxmicloxx.NoteBlockAPI.model.RepeatMode
import com.xxmicloxx.NoteBlockAPI.model.SoundCategory
import com.xxmicloxx.NoteBlockAPI.songplayer.PositionSongPlayer
import com.xxmicloxx.NoteBlockAPI.songplayer.RadioSongPlayer
import com.xxmicloxx.NoteBlockAPI.songplayer.RangeSongPlayer
import com.xxmicloxx.NoteBlockAPI.utils.NBSDecoder
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Tag
import org.bukkit.block.Block
import org.bukkit.block.Sign
import org.bukkit.configuration.serialization.ConfigurationSerialization
import org.bukkit.entity.Player
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.modules.nbs.NbsModel.PlaybackMode
import work.xeltica.craft.core.utils.CastHelper
import work.xeltica.craft.core.utils.Config
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.collections.HashMap

object NbsModule: ModuleBase() {
    override fun onEnable() {
        ConfigurationSerialization.registerClass(NbsModel::class.java, "NbsModel")
        nbs = Config("nbs")
        loadModels()
        playAll()

        registerHandler(NbsHandler())
    }

    /**
     * 保存されたNBSモデルを元に再生を開始します。
     */
    fun playAll() {
        models.forEach(this::playModel)
    }

    /**
     * 音を全部停止します。
     */
    fun stopAll() {
        playerCache.keys.forEach {
            playerCache[it]?.isPlaying = false
        }
    }

    /**
     * 指定した位置で音楽を再生します。
     * @param location 再生位置
     * @param songId 音楽ID
     * @param distance 長さ
     * @param playbackMode 再生モード
     * @throws IllegalArgumentException 曲IDが存在しない
     */
    fun play(location: Location, songId: String, distance: Int, playbackMode: PlaybackMode) {
        // ディレクトリトラバーサル対策
        if (songId.startsWith(".")) throw IllegalArgumentException()
        val file = getFile(songId)
        if (!file.exists()) throw IllegalArgumentException()

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
    fun playRadio(player: Player, songId: String, playbackMode: PlaybackMode) {
        // ディレクトリトラバーサル対策
        if (songId.startsWith(".")) throw IllegalArgumentException()
        val file = getFile(songId)
        if (!file.exists()) throw IllegalArgumentException()

        val song = NBSDecoder.parse(getFile(songId))
        val radio = RadioSongPlayer(song, SoundCategory.VOICE)

        if (playbackMode == PlaybackMode.LOOP) {
            radio.repeatMode = RepeatMode.ALL
        }
        radio.addPlayer(player)
        radio.isPlaying = true
    }

    /**
     * 指定した位置での曲のdistanceをいじります。
     * @param location 再生位置
     * @param distance 長さ
     */
    fun changeDistance(location: Location, distance: Int) {
        if (distance == 0) {
            stop(location)
        } else {
            playerCache[location]?.distance = distance
            modelCache[location]?.distance = distance
            saveModels()
        }
    }

    /**
     * 指定した位置にある音楽を停止します。
     * @param location 位置
     */
    fun stop(location: Location) {
        playerCache[location]?.isPlaying = false
        playerCache.remove(location)
        removeModel(location)
    }

    /**
     * 指定した位置にある音楽を停止します。
     * @param player プレイヤー
     */
    fun stopRadio(player: Player) {
        val id = player.uniqueId
        if (!radioCache.containsKey(id)) return
        radioCache[id]?.isPlaying = false
        radioCache.remove(id)
    }

    /**
     * 指定した位置に音楽があるかどうかを取得します。
     * @param location 位置
     */
    fun has(location: Location): Boolean {
        return getPlayer(location) != null
    }

    /**
     * 指定した位置の音楽プレイヤーを取得します。
     * @param location 位置
     */
    fun getPlayer(location: Location): RangeSongPlayer? {
        return playerCache[location]
    }

    /**
     * 指定した位置の音楽モデルを取得します。
     * @param location 位置
     */
    fun getModel(location: Location): NbsModel? {
        return modelCache[location]
    }

    /**
     * 指定した位置の音楽プレイヤーを取得します。
     * @param location 位置
     */
    fun getDistance(location: Location): Int? {
        return playerCache[location]?.distance
    }

    /**
     * プレイヤーを聴衆に加えます。
     * @param p プレイヤー
     */
    fun addAudience(p: Player) {
        playerCache.values.forEach {
            it.addPlayer(p)
        }
    }

    /**
     * プレイヤーを聴衆から外します。
     * @param p プレイヤー
     */
    fun removeAudience(p: Player) {
        playerCache.values.forEach {
            it.removePlayer(p)
        }
    }

    /**
     * モデルをシステムに追加します。
     * @param location 位置
     * @param model モデル
     */
    fun addModel(location: Location, model: NbsModel) {
        modelCache[location] = model
        models.add(model)
        saveModels()
    }
    /**
     * モデルをシステムから削除します。
     * @param location 位置
     */
    fun removeModel(location: Location) {
        val model = modelCache[location]
        playerCache.remove(location)
        modelCache.remove(location)
        models.remove(model)
        saveModels()
    }

    private fun playModel(model: NbsModel) {
        val song = NBSDecoder.parse(getFile(model.songId))
        val player = PositionSongPlayer(song, SoundCategory.VOICE)
        player.targetLocation = model.location
        player.distance = model.distance

        if (model.playbackMode == PlaybackMode.LOOP) {
            player.repeatMode = RepeatMode.ALL
        }
        player.isPlaying = true
        Bukkit.getOnlinePlayers().forEach(player::addPlayer)

        playerCache[model.location] = player
    }

    private fun getFile(songId: String): File {
        return File("$NBS_PATH$songId.nbs")
    }

    /**
     * ディスクからモデルを読み込みます。
     */
    private fun loadModels() {
        models = CastHelper.checkList<NbsModel>(nbs.conf.getList("models", listOf<NbsModel>()) as List<*>).toMutableList()
        models.forEach {
            modelCache[it.location] = it
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

    fun getSong(location: Location): NBSSignModel? {
        val block1 = location.clone().add(-1.0, 0.0, 0.0).block
        val block2 = location.clone().add(1.0, 0.0, 0.0).block
        val block3 = location.clone().add(0.0, 0.0, -1.0).block
        val block4 = location.clone().add(0.0, 0.0, 1.0).block

        var songId = getSongFromSign(block1)
        if (songId == null) songId = getSongFromSign(block2)
        if (songId == null) songId = getSongFromSign(block3)
        if (songId == null) songId = getSongFromSign(block4)

        return songId
    }

    fun getSongFromSign(block: Block): NBSSignModel? {
        if (Tag.WALL_SIGNS.isTagged(block.type)) {
            val state = block.state
            if (state is Sign) {
                val id = PlainTextComponentSerializer.plainText().serialize(state.line(0))
                val distance = PlainTextComponentSerializer.plainText().serialize(state.line(1)).toInt()
                val modeString = PlainTextComponentSerializer.plainText().serialize(state.line(2))
                val mode = when (modeString.lowercase()) {
                    "loop" -> PlaybackMode.LOOP
                    "oneshot" -> PlaybackMode.ONESHOT
                    else -> PlaybackMode.NORMAL
                }

                return NBSSignModel(id, distance, mode)
            }
        }
        return null
    }

    private val playerCache: HashMap<Location, RangeSongPlayer> = HashMap()
    private val radioCache: HashMap<UUID, RadioSongPlayer> = HashMap()
    private val modelCache: HashMap<Location, NbsModel> = HashMap()
    private lateinit var nbs: Config
    private lateinit var models: MutableList<NbsModel>

    private const val NBS_PATH = "/srv/nbs/"

    data class NBSSignModel(val songId: String, val distance: Int, val mode: PlaybackMode)
}