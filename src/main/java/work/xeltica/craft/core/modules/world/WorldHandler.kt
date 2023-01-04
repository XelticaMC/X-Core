package work.xeltica.craft.core.modules.world

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.Tag
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockFormEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerAdvancementDoneEvent
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.event.player.PlayerTeleportEvent
import work.xeltica.craft.core.XCorePlugin.Companion.instance
import work.xeltica.craft.core.api.playerStore.PlayerStore
import work.xeltica.craft.core.gui.Gui
import work.xeltica.craft.core.hooks.DiscordHook
import work.xeltica.craft.core.modules.hint.Hint
import work.xeltica.craft.core.modules.hint.HintModule
import work.xeltica.craft.core.utils.Ticks

/**
 * ワールド制御に関するハンドラーをまとめています。
 * TODO: 機能別に再編
 * @author Xeltica
 */
class WorldHandler : Listener {
    /*
     * 進捗を達成できるワールドを限定させるハンドラー
     */
    @EventHandler
    fun onAdvancementDone(e: PlayerAdvancementDoneEvent) {
        if (WorldModule.getWorldInfo(e.player.world).allowAdvancements) return

        for (criteria in e.advancement.criteria) {
            e.player.getAdvancementProgress(e.advancement).revokeCriteria(criteria)
        }
    }

    /*
     * サンドボックスでのエンダーチェスト設置を防止する
     */
    @EventHandler
    fun onBlockPlace(e: BlockPlaceEvent) {
        val p = e.player
        if (p.world.name == "sandbox") {
            val block = e.block.type
            // エンダーチェストはダメ
            if (block == Material.ENDER_CHEST) {
                e.isCancelled = true
            }
        }
    }

    /*
     * ワールドを移動する前に位置を保存する
     */
    @EventHandler
    fun onPlayerTeleportingWorld(e: PlayerTeleportEvent) {
        if (e.isCancelled) return
        if (e.from.world.uid == e.to.world.uid) return

        WorldModule.saveCurrentLocation(e.player)
    }

    /**
     * ワールド移動後の処理いろいろ。
     */
    @EventHandler
    fun onPlayerMoveWorld(e: PlayerChangedWorldEvent) {
        val info = WorldModule.getWorldInfo(e.player.world)
        val player = e.player

        // ワールド情報の表示
        e.player.showTitle(Title.title(Component.text(info.displayName).color(TextColor.color(0xFFB300)), Component.empty()))
        if (info.description.isNotEmpty()) {
            player.sendMessage(info.description)
        }

        // ナイトメア効果音再生
        if (info.name == "nightmare2") {
            player.playSound(player.location, Sound.BLOCK_END_PORTAL_SPAWN, SoundCategory.PLAYERS, 1f, 0.5f)
        }

        // 以前サーバーに来ている or FIRST_SPAWN フラグが立っている
        val isNotFirstTeleport = player.hasPlayedBefore() || PlayerStore.open(player).getBoolean(WorldModule.PS_KEY_FIRST_SPAWN)
        if (info.name == "main" && !HintModule.hasAchieved(player, Hint.GOTO_MAIN) && isNotFirstTeleport) {
            // はじめてメインワールドに入った場合、対象のスタッフに通知する
            try {
                DiscordHook.alertNewcomer(player)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }

        // ヒント
        val hint = when (info.name) {
            "main" -> Hint.GOTO_MAIN
            "hub2" -> Hint.GOTO_LOBBY
            "wildarea2" -> Hint.GOTO_WILDAREA
            "wildarea2_nether" -> Hint.GOTO_WILDNETHER
            "wildarea2_the_end" -> Hint.GOTO_WILDEND
            "wildareab" -> Hint.GOTO_WILDAREAB
            "sandbox2" -> Hint.GOTO_SANDBOX
            "art" -> Hint.GOTO_ART
            "nightmare2" -> Hint.GOTO_NIGHTMARE
            "shigen_nether" -> Hint.GOTO_WILDNETHERB
            "shigen_end" -> Hint.GOTO_WILDENDB
            else -> null
        }
        if (hint != null && isNotFirstTeleport) {
            HintModule.achieve(player, hint)
        }

        // FIRST_SPAWN フラグの設定
        Bukkit.getScheduler().runTaskLater(instance, Runnable {
            PlayerStore.open(e.player)[WorldModule.PS_KEY_FIRST_SPAWN] = true
        }, Ticks.from(5.0).toLong())
    }

    /**
     * 禁止された場所では睡眠を取れないようガードする
     */
    @EventHandler
    fun onGuardBedInDenyWorld(e: PlayerInteractEvent) {
        val p = e.player
        val block = e.clickedBlock
        if (e.action != Action.RIGHT_CLICK_BLOCK || block == null) return

        val worldInfo = WorldModule.getWorldInfo(p.world.name)
        if (!worldInfo.canSleep && Tag.BEDS.isTagged(block.type)) {
            Gui.getInstance().error(p, "ベッドはこの世界では使えない…")
            e.isCancelled = true
        }
    }

    /**
     * 焼石製造機を丸石製造機にする
     * 採掘EPボーナスの自動化対策
     */
    @EventHandler
    fun onGuardCobbleStoneGenerator(e: BlockFormEvent) {
        if (e.newState.type == Material.STONE) {
            e.newState.type = Material.COBBLESTONE
        }
    }

    /**
     * プレイヤーが資源ワールドで死んだときにメインワールドに転送する
     */
    @EventHandler
    fun onPlayerDeath(e: PlayerRespawnEvent) {
        val p = e.player
        if (p.world.name.startsWith("wildareab")) {
            var respawnLocation = p.bedSpawnLocation
            if (respawnLocation == null) {
                respawnLocation = Bukkit.getWorld("main")?.spawnLocation!!
            }
            e.respawnLocation = respawnLocation
        }
    }
}