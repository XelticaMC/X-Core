package work.xeltica.craft.core.modules.player

import com.github.ucchyocean.lc3.bukkit.event.LunaChatBukkitChannelMessageEvent
import com.github.ucchyocean.lc3.member.ChannelMemberPlayer
import io.papermc.paper.event.player.ChatEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.scheduler.BukkitRunnable
import work.xeltica.craft.core.XCorePlugin
import work.xeltica.craft.core.api.playerStore.PlayerStore
import work.xeltica.craft.core.modules.hint.Hint
import work.xeltica.craft.core.modules.hint.HintModule
import work.xeltica.craft.core.modules.hub.HubModule
import work.xeltica.craft.core.modules.hub.HubType
import java.util.*

class PlayerHandler: Listener {

    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
        val p = e.player
        val name = PlainTextComponentSerializer.plainText().serialize(p.displayName())

        e.joinMessage(Component.text("§a$name§bさんがやってきました"))
        if (!p.hasPlayedBefore()) {
            e.joinMessage(Component.text("§a$name§bさんが§6§l初参加§rです"))
            PlayerStore.open(p)[PlayerDataKey.NEWCOMER_TIME] = DEFAULT_NEW_COMER_TIME
            HubModule.teleport(p, HubType.NewComer, true)
        }

        p.showTitle(Title.title(
            Component.text("§aXelticaMCへ§6ようこそ！"),
            Component.text("§f詳しくは §b§nhttps://craft.xeltica.work§fを見てね！")
        ))
        HintModule.achieve(p, Hint.WELCOME)
    }

    @EventHandler
    fun onPlayerQuit(e: PlayerQuitEvent) {
        val name = PlainTextComponentSerializer.plainText().serialize(e.player.displayName())
        e.quitMessage(Component.text("§a$name§bさんがかえりました"))
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerChatForCat(e: ChatEvent) {
        if (PlayerStore.open(e.player).getBoolean(PlayerDataKey.CAT_MODE)) {
            val text = e.message() as TextComponent
            e.message(Component.text(nyaize(text.content())))
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerChatForCat(e: LunaChatBukkitChannelMessageEvent) {
        val member = e.member
        if (member !is ChannelMemberPlayer) return
        if (PlayerStore.open(member.player).getBoolean(PlayerDataKey.CAT_MODE))
            e.message = nyaize(e.message)
    }

    private fun nyaize(m: String): String {
        var mes = m
        mes = mes.replace("な", "にゃ")
        mes = mes.replace("ナ", "ニャ")
        mes = mes.replace("ﾅ", "ﾆｬ")
        mes = mes.replace("everyone", "everynyan")
        mes = mes.replace("morning", "mornyan")
        mes = mes.replace("na", "nya")
        mes = mes.replace("EVERYONE", "EVERYNYAN")
        mes = mes.replace("MORNING", "MORNYAN")
        mes = mes.replace("NA", "NYA")
        return mes
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onPlayer草ed(e: ChatEvent) {
        val message = PlainTextComponentSerializer.plainText().serialize(e.message())
        handle草(message, e.player)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onPlayer草edOnChannel(e: LunaChatBukkitChannelMessageEvent) {
        val member = e.member
        if (member !is ChannelMemberPlayer) return
        handle草(e.originalMessage, member.player)
    }

    private fun handle草(message: String, player: Player) {
        if (message == "草" || message.equals("kusa", true) || message.equals("w", true)) {
            object : BukkitRunnable() {
                override fun run() {
                    val block = player.location.subtract(0.0, 1.0, 0.0).block
                    if (block.type != Material.GRASS_BLOCK) {
                        return
                    }
                    val id = player.uniqueId
                    val lastTime = last草edTimeMap[id] ?: Int.MIN_VALUE
                    val nowTime = Bukkit.getCurrentTick()
                    if (lastTime == Int.MIN_VALUE || nowTime - lastTime > 20 * 60) {
                        block.applyBoneMeal(BlockFace.UP)
                    }
                    last草edTimeMap[id] = nowTime
                    HintModule.achieve(player, Hint.KUSA)
                }
            }.runTask(XCorePlugin.instance)
        }
    }

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

    private val last草edTimeMap: HashMap<UUID, Int> = HashMap()

    // 30分
    private val DEFAULT_NEW_COMER_TIME = 20 * 60 * 30
}