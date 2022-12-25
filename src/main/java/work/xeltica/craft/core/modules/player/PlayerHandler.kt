package work.xeltica.craft.core.modules.player

import com.github.ucchyocean.lc3.bukkit.event.LunaChatBukkitChannelMessageEvent
import com.github.ucchyocean.lc3.member.ChannelMemberPlayer
import io.papermc.paper.event.player.ChatEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.kyori.adventure.title.Title
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.event.node.NodeAddEvent
import net.luckperms.api.model.user.User
import net.luckperms.api.node.types.InheritanceNode
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.*
import org.bukkit.scheduler.BukkitRunnable
import work.xeltica.craft.core.XCorePlugin
import work.xeltica.craft.core.gui.Gui.Companion.getInstance
import work.xeltica.craft.core.modules.hint.Hint
import work.xeltica.craft.core.modules.hint.HintModule
import work.xeltica.craft.core.modules.hub.HubModule
import work.xeltica.craft.core.modules.hub.HubType
import work.xeltica.craft.core.modules.item.ItemModule
import work.xeltica.craft.core.utils.BedrockDisclaimerUtil
import java.util.*

class PlayerHandler: Listener {
    init {
        LuckPermsProvider
            .get()
            .eventBus.
            subscribe(XCorePlugin.instance, NodeAddEvent::class.java, this::onNodeAdd)
    }

    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
        val p = e.player

        val name = PlainTextComponentSerializer.plainText().serialize(p.displayName())
        val playerModule = PlayerModule
        e.joinMessage(Component.text("§a$name§bさんがやってきました"))
        if (!p.hasPlayedBefore()) {
            e.joinMessage(Component.text("§a$name§bが§6§l初参加§rです"))
            playerModule.open(p).set(PlayerDataKey.NEWCOMER_TIME, DEFAULT_NEW_COMER_TIME)
            HubModule.teleport(p, HubType.NewComer, true)
        }

        val record = playerModule.open(p)
        if (!record.getBoolean(PlayerDataKey.GIVEN_PHONE)) {
            p.inventory.addItem(ItemModule.getItem(ItemModule.ITEM_NAME_XPHONE))
            record[PlayerDataKey.GIVEN_PHONE] = true
        }

        HintModule.achieve(p, Hint.WELCOME)

        if (playerModule.isCitizen(p)) {
            HintModule.achieve(p, Hint.BE_CITIZEN)
        }

        if (!record.getBoolean(PlayerDataKey.BEDROCK_ACCEPT_DISCLAIMER)) {
            BedrockDisclaimerUtil.showDisclaimerAsync(p)
        }

        p.showTitle(Title.title(
            Component.text("§aXelticaMCへ§6ようこそ！"),
            Component.text("§f詳しくは §b§nhttps://craft.xeltica.work§fを見てね！")
        ))

        if (playerModule.isCitizen(p)) return
        if (!record.has(PlayerDataKey.NEWCOMER_TIME)) {
            p.sendMessage("総プレイ時間が30分を超えたため、§b市民§rへの昇格ができます！")
            p.sendMessage("詳しくは §b/promo§rコマンドを実行してください。")
        }
    }

    @EventHandler
    fun onPlayerQuit(e: PlayerQuitEvent) {
        val name = PlainTextComponentSerializer.plainText().serialize(e.player.displayName())
        e.quitMessage(Component.text("§a$name§bさんがかえりました"))
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerChatForCat(e: ChatEvent) {
        if (PlayerModule.open(e.player).getBoolean(PlayerDataKey.CAT_MODE)) {
            val text = e.message() as TextComponent
            e.message(Component.text(nyaize(text.content())))
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerChatForCat(e: LunaChatBukkitChannelMessageEvent) {
        val member = e.member
        if (member !is ChannelMemberPlayer) return
        if (PlayerModule.open(member.player).getBoolean(PlayerDataKey.CAT_MODE))
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

    @EventHandler
    fun onPlayerTryBed(e: PlayerInteractEvent) {
        val p = e.player
        val isSneaking = p.isSneaking
        if (e.action == Action.RIGHT_CLICK_BLOCK) {
            val worldName = p.world.name
            // TODO WorldStoreで管理する
            val isBedDisabledWorld =
                worldName == "hub2" || worldName == "sandbox" || worldName == "wildareab" || worldName == "event" || worldName == "event2"
            if (isBedDisabledWorld && Tag.BEDS.isTagged(e.clickedBlock?.type!!)) {
                getInstance().error(p, "ベッドはこの世界では使えない…")
                e.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onPlayerGameModeChange(e: PlayerGameModeChangeEvent) {
        if (e.newGameMode == GameMode.SPECTATOR) {
            e.player.performCommand("dynmap hide")
        } else {
            e.player.performCommand("dynmap show")
        }
    }

    private fun onNodeAdd(e: NodeAddEvent) {
        if (!e.isUser) return
        val target = e.target as User
        val node = e.node

        val task = object: Runnable {
            override fun run() {
                val player = Bukkit.getPlayer(target.uniqueId) ?: return
                if (node is InheritanceNode && "citizen" == node.groupName) {
                    HintModule.achieve(player, Hint.BE_CITIZEN)
                }
            }
        }

        Bukkit.getScheduler().runTask(XCorePlugin.instance, task)
    }

    private val last草edTimeMap: HashMap<UUID, Int> = HashMap()

    // 30分
    private val DEFAULT_NEW_COMER_TIME = 20 * 60 * 30
}