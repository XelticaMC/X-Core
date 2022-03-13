package work.xeltica.craft.core.commands

import org.bukkit.ChatColor
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import work.xeltica.craft.core.COMPLETE_LIST_EMPTY
import work.xeltica.craft.core.stores.OmikujiStore
import work.xeltica.craft.core.plugins.VaultPlugin
import work.xeltica.craft.core.models.OmikujiScore
import work.xeltica.craft.core.stores.HintStore
import work.xeltica.craft.core.XCorePlugin
import work.xeltica.craft.core.models.Hint
import work.xeltica.craft.core.utils.Ticks

/**
 * おみくじを引くコマンド
 * @author Xeltica
 */
class CommandOmikuji : CommandPlayerOnlyBase() {
    override fun execute(player: Player, command: Command, label: String, args: Array<String>): Boolean {
        val s = OmikujiStore.getInstance()
        if (s.isDrawnBy(player)) {
            val score = s.getScoreName(player)
            player.sendMessage(
                ChatColor.RED.toString() + "既に引いています！" +
                ChatColor.GOLD + "あなたの運勢は" +
                ChatColor.RESET + "「" +
                ChatColor.GREEN + score +
                ChatColor.RESET + "」" +
                ChatColor.AQUA + "です！"
            )
            player.sendMessage(ChatColor.GOLD.toString() + "また次の朝、引いてください！")
            return true
        }
        val vault = VaultPlugin.getInstance()
        if (vault.isEconomyEnabled && !vault.tryWithdrawPlayer(player, 100.0)) {
            player.sendMessage(ChatColor.RED.toString() + "パワーが足りません！おみくじは1回100エビパワーが必要です。")
            return true
        }
        player.sendMessage("何が出るかな...?")
        object : BukkitRunnable() {
            override fun run() {
                val score = s.generateScore()
                s[player] = score
                player.sendMessage(
                    ChatColor.GOLD.toString() + "あなたの運勢は" +
                    ChatColor.RESET + "「" +
                    ChatColor.GREEN + score.displayName +
                    ChatColor.RESET + "」" +
                    ChatColor.AQUA + "です！"
                )
                when (score) {
                    OmikujiScore.Daikichi -> {
                        // 大吉。幸運が20分つく
                        player.addPotionEffect(PotionEffect(PotionEffectType.LUCK, Ticks.from(20, 0.0), 1))
                        player.playSound(
                            player.location,
                            Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
                            SoundCategory.PLAYERS,
                            1f,
                            0.7f
                        )
                    }
                    OmikujiScore.Daikyou -> {
                        // 大凶。不吉な予感10分、毒10秒、不運が20分つく
                        player.addPotionEffect(PotionEffect(PotionEffectType.BAD_OMEN, Ticks.from(10, 0.0), 1))
                        player.addPotionEffect(PotionEffect(PotionEffectType.POISON, Ticks.from(10.0), 2))
                        player.addPotionEffect(PotionEffect(PotionEffectType.UNLUCK, Ticks.from(20, 0.0), 1))
                        player.playSound(player.location, Sound.BLOCK_END_PORTAL_SPAWN, SoundCategory.PLAYERS, 1f, 0.5f)
                        HintStore.getInstance().achieve(player, Hint.OMIKUJI_DAIKYOU)
                    }
                    OmikujiScore.Kyou -> {
                        // 凶。不運が20分、吐き気が5秒つく
                        player.addPotionEffect(PotionEffect(PotionEffectType.UNLUCK, Ticks.from(20, 0.0), 1))
                        player.addPotionEffect(PotionEffect(PotionEffectType.CONFUSION, Ticks.from(5.0), 1))
                        player.playSound(player.location, Sound.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 1f, 0.5f)
                    }
                    OmikujiScore.Tokudaikichi -> {
                        // 特大吉。幸運が20分つく
                        player.addPotionEffect(PotionEffect(PotionEffectType.LUCK, 60 * 20 * 20, 1))
                        player.playSound(
                            player.location,
                            Sound.UI_TOAST_CHALLENGE_COMPLETE,
                            SoundCategory.PLAYERS,
                            1f,
                            1.4f
                        )
                        HintStore.getInstance().achieve(player, Hint.OMIKUJI_TOKUDAIKICHI)
                    }
                    else ->  // その他。特に何もなし
                        player.playSound(player.location, Sound.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 1f, 1.6f)
                }
            }
        }.runTaskLater(XCorePlugin.getInstance(), (20 * 3).toLong())
        return true
    }

    override fun onTabComplete(commandSender: CommandSender, command: Command, label: String, args: Array<String>): List<String> {
        return COMPLETE_LIST_EMPTY
    }
}