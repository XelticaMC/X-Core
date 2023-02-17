package work.xeltica.craft.core.modules.eventTwoYears

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.api.playerStore.PlayerStore
import work.xeltica.craft.core.modules.counter.CounterModule
import work.xeltica.craft.core.modules.hint.Hint
import work.xeltica.craft.core.modules.hint.HintModule
import work.xeltica.craft.core.modules.item.ItemModule
import work.xeltica.craft.core.modules.ranking.Ranking
import work.xeltica.craft.core.modules.ranking.RankingModule
import work.xeltica.craft.core.modules.xMusicDisc.XMusicDiscModule

object EventTwoYearsModule : ModuleBase() {
    const val PS_KEY_CHECKPOINT = "2yearsParkourGameCheckpoint"
    const val PS_KEY_DEATH_COUNT = "2yearsParkourGameDeathCount"
    const val EVENT_COUNTER_ID = "2yearsparkour"

    override fun onEnable() {
        registerHandler(EventTwoYearsHandler())

        checkHintForAllOnlinePlayers()
    }

    fun setCheckpoint(player: Player, location: Location) {
        val store = PlayerStore.open(player)
        store[PS_KEY_CHECKPOINT] = location
    }

    fun getCheckPoint(player: Player): Location {
        val counterStartLocation = CounterModule[EVENT_COUNTER_ID]?.location1?.clone() ?: throw Exception("カウンター $EVENT_COUNTER_ID が見つからない")
        counterStartLocation.yaw = 180f
        return PlayerStore.open(player).getLocation(PS_KEY_CHECKPOINT, counterStartLocation) ?: throw Exception("BUG")
    }

    fun incrementDeathCount(player: Player) {
        val store = PlayerStore.open(player)
        store[PS_KEY_DEATH_COUNT] = store.getInt(PS_KEY_DEATH_COUNT, 0) + 1
    }

    fun resetPlayerStore(player: Player) {
        val store = PlayerStore.open(player)
        store.delete(PS_KEY_CHECKPOINT)
        store.delete(PS_KEY_DEATH_COUNT)
    }

    fun checkHintForAllOnlinePlayers() {
        val ranking = RankingModule["2ye"] ?: return
        Bukkit.getOnlinePlayers().forEach {
            checkHint(it, ranking)
        }

    }

    fun checkHintFor(player: Player) {
        val ranking = RankingModule["2ye"] ?: return
        checkHint(player, ranking)
    }

    private fun checkHint(player: Player, ranking: Ranking) {
        // 参加賞
        val playerId = player.uniqueId.toString()
        if (ranking.keys().contains(playerId)) {
            if (!HintModule.hasAchieved(player, Hint.TWO_YEARS_EVENT_PARKOUR_JOINED)) {
                HintModule.achieve(player, Hint.TWO_YEARS_EVENT_PARKOUR_JOINED)
                val item = ItemModule.getItem("${XMusicDiscModule.ITEM_NAME_X_MUSIC_DISC}.csikospost")
                player.world.dropItem(player.location, item) {
                    it.owner = player.uniqueId
                }
            }
        }

        // ランキング
        when (ranking.getRank(playerId)) {
            1 -> if (!HintModule.hasAchieved(player, Hint.TWO_YEARS_EVENT_PARKOUR_1ST)) {
                giveHook(player)
                giveTravelTicket(player)
                giveTrident(player)
                HintModule.achieve(player, Hint.TWO_YEARS_EVENT_PARKOUR_1ST)
            }

            2 -> if (!HintModule.hasAchieved(player, Hint.TWO_YEARS_EVENT_PARKOUR_2ND)) {
                giveTravelTicket(player)
                giveTrident(player)
                HintModule.achieve(player, Hint.TWO_YEARS_EVENT_PARKOUR_2ND)
            }

            3 -> if (!HintModule.hasAchieved(player, Hint.TWO_YEARS_EVENT_PARKOUR_3RD)) {
                giveTrident(player)
                HintModule.achieve(player, Hint.TWO_YEARS_EVENT_PARKOUR_3RD)
            }
        }
    }

    private fun giveTrident(player: Player) {
        player.world.dropItem(player.location, ItemStack(Material.TRIDENT)) {
            it.owner = player.uniqueId
        }
    }

    private fun giveHook(player: Player) {
        val hook = ItemStack(Material.FISHING_ROD)
        hook.editMeta {
            it.displayName(Component.text("wirerod"))
        }
        hook.addUnsafeEnchantment(Enchantment.OXYGEN, 5)
        player.world.dropItem(player.location, hook) {
            it.owner = player.uniqueId
        }
    }

    private fun giveTravelTicket(player: Player) {
        player.world.dropItem(player.location, ItemModule.getItem(ItemModule.ITEM_NAME_TICKET_WILDAREAB_OCEAN_MONUMENT)) {
            it.owner = player.uniqueId
        }
    }
}

