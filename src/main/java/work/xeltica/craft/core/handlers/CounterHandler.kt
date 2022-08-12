package work.xeltica.craft.core.handlers

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.Tag
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.geysermc.connector.common.ChatColor
import work.xeltica.craft.core.stores.PlayerStore
import work.xeltica.craft.core.stores.CounterStore
import work.xeltica.craft.core.gui.Gui
import work.xeltica.craft.core.models.PlayerDataKey
import java.io.IOException
import work.xeltica.craft.core.api.events.PlayerCounterStart
import work.xeltica.craft.core.api.events.PlayerCounterFinish
import work.xeltica.craft.core.api.events.RealTimeNewDayEvent
import org.geysermc.floodgate.api.FloodgateApi
import org.geysermc.floodgate.util.DeviceOs
import work.xeltica.craft.core.models.CounterData
import work.xeltica.craft.core.stores.RankingStore
import work.xeltica.craft.core.services.DiscordService
import work.xeltica.craft.core.api.Time

/**
 * Counter API処理用ハンドラー
 */
class CounterHandler : Listener {
    /**
     * カウンター登録モードのときに感圧板を右クリックした
     */
    @EventHandler(priority = EventPriority.LOWEST)
    fun onClickPlate(e: PlayerInteractEvent) {
        val isBlockClick = listOf(
            Action.RIGHT_CLICK_BLOCK,
            Action.LEFT_CLICK_BLOCK
        ).contains(e.action)

        // ブロッククリックでなければ無視
        if (!isBlockClick) return
        val pstore = PlayerStore.instance
        val store = CounterStore.instance
        val ui = Gui.getInstance()
        val player = e.player
        val block = e.clickedBlock
        val record = pstore.open(player)
        val isCounterRegisterMode = record.getBoolean(PlayerDataKey.COUNTER_REGISTER_MODE)
        val isPlate = Tag.PRESSURE_PLATES.isTagged(block!!.type)

        // カウンター登録モードでなければ無視
        if (!isCounterRegisterMode) return

        // 感圧板でなければ無視
        if (!isPlate) return
        val name = record.getString(PlayerDataKey.COUNTER_REGISTER_NAME)
        val loc = record.getLocation(PlayerDataKey.COUNTER_REGISTER_LOCATION)
        val daily = record.getBoolean(PlayerDataKey.COUNTER_REGISTER_IS_DAILY)
        e.isCancelled = true
        try {
            // 始点登録
            if (loc == null) {
                record[PlayerDataKey.COUNTER_REGISTER_LOCATION] = block.location
                player.sendMessage("始点を登録しました。続いて終点を登録します。")
                player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.PLAYERS, 1f, 2f)
            } else {
                store.add(CounterData(name, loc, block.location, daily, null, null, null, null))
                player.sendMessage("カウンター " + name + "を登録しました。")
                player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1f, 1f)
                record.delete(PlayerDataKey.COUNTER_REGISTER_MODE)
                record.delete(PlayerDataKey.COUNTER_REGISTER_NAME)
                record.delete(PlayerDataKey.COUNTER_REGISTER_LOCATION)
                record.delete(PlayerDataKey.COUNTER_REGISTER_IS_DAILY)
                pstore.save()
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
            ui.error(player, "§cIO エラーが発生したために処理を続行できませんでした。")
        }
    }

    /**
     * カウンター感圧板を踏んだ
     */
    @EventHandler(priority = EventPriority.LOWEST)
    fun onUsePlate(e: PlayerInteractEvent) {
        // 踏んだわけじゃないのなら無視
        if (e.action != Action.PHYSICAL) return
        val pstore = PlayerStore.instance
        val store = CounterStore.instance
        val ui = Gui.getInstance()
        val player = e.player
        val block = e.clickedBlock ?: return

        // ブロックがnullなら無視 

        // 感圧板でなければ無視
        if (!Tag.PRESSURE_PLATES.isTagged(block.type)) return
        val first = store.getByLocation1(block.location)
        val last = store.getByLocation2(block.location)
        val record = pstore.open(player)
        val counterId = record.getString(PlayerDataKey.PLAYING_COUNTER_ID)
        val startedAt = record.getString(PlayerDataKey.PLAYING_COUNTER_TIMESTAMP, "0").toLong()
        val counter = if (counterId == null) null else store[counterId]
        val isUsingCounter = counter != null

        // カウンター開始する
        if (first != null) {
            if (isUsingCounter) {
                ui.error(player, "既にタイムアタックが始まっています！")
                return
            }
            val ts = System.currentTimeMillis().toString()
            record[PlayerDataKey.PLAYING_COUNTER_ID] = first.name
            record[PlayerDataKey.PLAYING_COUNTER_TIMESTAMP] = ts
            player.showTitle(Title.title(Component.text("§6スタート！"), Component.empty()))
            player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.PLAYERS, 1f, 2f)
            Bukkit.getPluginManager().callEvent(PlayerCounterStart(player, first))
        }

        // カウンター終了する
        if (last != null) {
            if (!isUsingCounter) {
                ui.error(player, "こちらはゴールです。スタート地点から開始してください。")
                return
            }
            if (last.name != counterId) {
                ui.error(player, "ゴールが異なります。")
                return
            }
            record.delete(PlayerDataKey.PLAYING_COUNTER_ID)
            record.delete(PlayerDataKey.PLAYING_COUNTER_TIMESTAMP)
            val endAt = System.currentTimeMillis()
            val diff = (endAt - startedAt).toInt()
            val timeString = Time.msToString(diff.toLong())
            player.sendMessage("ゴール！タイムは" + timeString + "でした。")
            player.showTitle(
                Title.title(
                    Component.text("§6ゴール！"),
                    Component.text("タイム $timeString")
                )
            )
            player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.PLAYERS, 1f, 2f)
            if (last.isDaily && record.getBoolean(PlayerDataKey.PLAYED_COUNTER)) {
                player.sendMessage(ChatColor.RED + "既にチャレンジ済みのため、ランキングは更新されません。")
            } else if (last.bedrockRankingId != null || last.javaRankingId != null || last.uwpRankingId != null || last.phoneRankingId != null) {
                val playerName = PlainTextComponentSerializer.plainText().serialize(player.displayName())
                Bukkit.getOnlinePlayers()
                    .filter { it.uniqueId != player.uniqueId }
                    .forEach {
                        it.sendMessage("${ChatColor.GREEN}${playerName}さん${ChatColor.RESET}がタイムアタックで${ChatColor.AQUA}${timeString}${ChatColor.RESET}を達成しました！")
                    }
                DiscordService.getInstance().broadcast("${playerName}さんがタイムアタックで${timeString}を達成しました！")

                handleRanking(player, last, diff)
            }
            record[PlayerDataKey.PLAYED_COUNTER] = true
            Bukkit.getPluginManager().callEvent(PlayerCounterFinish(player, last, diff.toLong()))
        }
    }

    @EventHandler
    fun onDailyReset(e: RealTimeNewDayEvent?) {
        try {
            CounterStore.instance.resetAllPlayersPlayedLog()
            Bukkit.getLogger().info("タイムアタックのプレイ済み履歴をリセットしました。")
        } catch (e1: IOException) {
            e1.printStackTrace()
        }
    }

    /**
     * ランキングに投稿するやつ
     */
    private fun handleRanking(player: Player, counter: CounterData, diff: Int) {
        val floodgate = FloodgateApi.getInstance()
        if (floodgate.isFloodgatePlayer(player.uniqueId)) {
            // bedrock
            val type = floodgate.getPlayer(player.uniqueId).deviceOs
            addRanking(if (type == DeviceOs.UWP) counter.uwpRankingId else counter.phoneRankingId, player, diff)
            addRanking(counter.bedrockRankingId, player, diff)
        } else {
            // java
            addRanking(counter.javaRankingId, player, diff)
        }
    }

    private fun addRanking(id: String?, player: Player, diff: Int) {
        val rankingApi = RankingStore.getInstance()
        if (id != null && rankingApi.has(id)) {
            val ranking = rankingApi[id]
            val prev = ranking!![player.uniqueId.toString()]
            if (prev == 0 || prev > diff) {
                ranking.add(player.uniqueId.toString(), diff)
                player.sendMessage("§a§l新記録達成！§cおめでとう！")
                player.playSound(player.location, Sound.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 1f, 1f)
            } else {
                player.sendMessage("§7新記録達成ならず…。")
                player.playSound(player.location, Sound.ENTITY_CAT_AMBIENT, SoundCategory.PLAYERS, 1f, 1f)
            }
            player.sendMessage("§dまたチャレンジしてね！")
        }
    }
}