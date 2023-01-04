package work.xeltica.craft.core.modules.counter

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
import org.geysermc.floodgate.util.DeviceOs
import work.xeltica.craft.core.api.events.RealTimeNewDayEvent
import work.xeltica.craft.core.api.playerStore.PlayerStore
import work.xeltica.craft.core.gui.Gui
import work.xeltica.craft.core.hooks.DiscordHook
import work.xeltica.craft.core.hooks.FloodgateHook.isFloodgatePlayer
import work.xeltica.craft.core.hooks.FloodgateHook.toFloodgatePlayer
import work.xeltica.craft.core.modules.ranking.RankingModule
import work.xeltica.craft.core.utils.Time
import java.io.IOException

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
        val ui = Gui.getInstance()
        val player = e.player
        val block = e.clickedBlock
        val record = PlayerStore.open(player)
        val isCounterRegisterMode = record.getBoolean(CounterModule.PS_KEY_MODE)
        val isPlate = Tag.PRESSURE_PLATES.isTagged(block!!.type)

        // カウンター登録モードでなければ無視
        if (!isCounterRegisterMode) return

        // 感圧板でなければ無視
        if (!isPlate) return
        val name = record.getString(CounterModule.PS_KEY_NAME)
        val loc = record.getLocation(CounterModule.PS_KEY_LOCATION)
        val daily = record.getBoolean(CounterModule.PS_KEY_IS_DAILY)
        e.isCancelled = true
        try {
            // 始点登録
            if (loc == null) {
                record[CounterModule.PS_KEY_LOCATION] = block.location
                player.sendMessage("始点を登録しました。続いて終点を登録します。")
                player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.PLAYERS, 1f, 2f)
            } else {
                CounterModule.add(
                    CounterData(
                        name,
                        loc,
                        block.location,
                        daily,
                        null,
                        null,
                        null,
                        null
                    )
                )
                player.sendMessage("カウンター " + name + "を登録しました。")
                player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1f, 1f)
                record.delete(CounterModule.PS_KEY_MODE)
                record.delete(CounterModule.PS_KEY_NAME)
                record.delete(CounterModule.PS_KEY_LOCATION)
                record.delete(CounterModule.PS_KEY_IS_DAILY)
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
        val ui = Gui.getInstance()
        val player = e.player
        val block = e.clickedBlock ?: return

        // ブロックがnullなら無視 

        // 感圧板でなければ無視
        if (!Tag.PRESSURE_PLATES.isTagged(block.type)) return
        val first = CounterModule.getByLocation1(block.location)
        val last = CounterModule.getByLocation2(block.location)
        val record = PlayerStore.open(player)
        val counterId = record.getString(CounterModule.PS_KEY_ID)
        val startedAt = record.getString(CounterModule.PS_KEY_TIME, "0").toLong()
        val counter = CounterModule[counterId]
        val isUsingCounter = counter != null

        // カウンター開始する
        if (first != null) {
            if (isUsingCounter) {
                ui.error(player, "既にタイムアタックが始まっています！")
                return
            }
            val ts = System.currentTimeMillis().toString()
            record[CounterModule.PS_KEY_ID] = first.name
            record[CounterModule.PS_KEY_TIME] = ts
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
            record.delete(CounterModule.PS_KEY_ID)
            record.delete(CounterModule.PS_KEY_TIME)
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
            val count = record.getInt(CounterModule.PS_KEY_COUNT, 0)
            if (last.isDaily && count >= 2) {
                player.sendMessage(ChatColor.RED + "既にチャレンジ済みのため、ランキングは更新されません。")
            } else if (last.bedrockRankingId != null || last.javaRankingId != null || last.uwpRankingId != null || last.phoneRankingId != null) {
                val playerName = PlainTextComponentSerializer.plainText().serialize(player.displayName())
                Bukkit.getOnlinePlayers()
                    .filter { it.uniqueId != player.uniqueId }
                    .forEach {
                        it.sendMessage("${ChatColor.GREEN}${playerName}さん${ChatColor.RESET}がタイムアタックで${ChatColor.AQUA}${timeString}${ChatColor.RESET}を達成しました！")
                    }
                DiscordHook.broadcast("${playerName}さんがタイムアタックで${timeString}を達成しました！")
                handleRanking(player, last, diff)
                val message = if (count == 0) "あと1回チャレンジできます！" else "本日はもうチャレンジできません。"
                player.sendMessage(ChatColor.GREEN + message)
            }
            record[CounterModule.PS_KEY_COUNT] = count + 1
            Bukkit.getPluginManager().callEvent(PlayerCounterFinish(player, last, diff.toLong()))
        }
    }

    @EventHandler
    fun onDailyReset(e: RealTimeNewDayEvent) {
        try {
            CounterModule.resetAllPlayersPlayedLog()
            Bukkit.getLogger().info("タイムアタックのプレイ済み履歴をリセットしました。")
        } catch (e1: IOException) {
            e1.printStackTrace()
        }
    }

    /**
     * ランキングに投稿するやつ
     */
    private fun handleRanking(player: Player, counter: CounterData, diff: Int) {
        if (player.isFloodgatePlayer()) {
            // bedrock
            val fplayer = player.toFloodgatePlayer() ?: return
            val type = fplayer.deviceOs
            addRanking(if (type == DeviceOs.UWP) counter.uwpRankingId else counter.phoneRankingId, player, diff)
            addRanking(counter.bedrockRankingId, player, diff)
        } else {
            // java
            addRanking(counter.javaRankingId, player, diff)
        }
    }

    private fun addRanking(id: String?, player: Player, diff: Int) {
        if (id == null) return
        val ranking = RankingModule[id] ?: return
        val uniqueId = player.uniqueId.toString()
        val prev = ranking[uniqueId]
        if (prev == 0 || prev > diff) {
            ranking.add(uniqueId, diff)
            player.sendMessage("§a§l新記録達成！§cおめでとう！")
            player.playSound(player.location, Sound.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 1f, 1f)
        } else {
            player.sendMessage("§7新記録達成ならず…。")
            player.playSound(player.location, Sound.ENTITY_CAT_AMBIENT, SoundCategory.PLAYERS, 1f, 1f)
        }
        player.sendMessage("§dまたチャレンジしてね！")
    }
}