package work.xeltica.craft.core.modules.omikuji

import org.bukkit.ChatColor
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import work.xeltica.craft.core.XCorePlugin
import work.xeltica.craft.core.api.commands.CommandPlayerOnlyBase
import work.xeltica.craft.core.modules.hint.Hint
import work.xeltica.craft.core.modules.hint.HintModule
import work.xeltica.craft.core.utils.Ticks

class OmikujiCommand : CommandPlayerOnlyBase() {
    override fun execute(player: Player, command: Command, label: String, args: Array<out String>): Boolean {
        val module = OmikujiModule
        if (module.isDrawnBy(player)) {
            val score = module.getScoreName(player)
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
        player.sendMessage("何が出るかな")

        object : BukkitRunnable() {
            override fun run() {
                val score = module.generateScore()
                module.set(player, score)

                player.sendMessage(
                    ChatColor.GOLD.toString() + "あなたの運勢は" +
                            ChatColor.RESET + "「" +
                            ChatColor.GREEN + score.displayName +
                            ChatColor.RESET + "」" +
                            ChatColor.AQUA + "です！"
                )

                when (score) {
                    OmikujiScore.TOKUDAIKICHI -> {
                        player.addPotionEffect(PotionEffect(PotionEffectType.LUCK, Ticks.from(20, 0.0), 1))
                        player.playSound(player.location, Sound.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 1f, 1.6f)
                        HintModule.achieve(player, Hint.OMIKUJI_TOKUDAIKICHI)
                    }

                    OmikujiScore.DAIKICHI -> {
                        player.addPotionEffect(PotionEffect(PotionEffectType.LUCK, Ticks.from(20, 0.0), 1))
                        player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1f, 0.7f)
                    }

                    OmikujiScore.DAIKYOU -> {
                        player.addPotionEffect(PotionEffect(PotionEffectType.BAD_OMEN, Ticks.from(10, 0.0), 1))
                        player.addPotionEffect(PotionEffect(PotionEffectType.POISON, Ticks.from(10.0), 2))
                        player.addPotionEffect(PotionEffect(PotionEffectType.UNLUCK, Ticks.from(20, 0.0), 1))
                        player.playSound(player.location, Sound.BLOCK_END_PORTAL_SPAWN, SoundCategory.PLAYERS, 1f, 0.5f)
                        HintModule.achieve(player, Hint.OMIKUJI_DAIKYOU)
                    }

                    OmikujiScore.KYOU -> {

                        // 凶。不運が20分、吐き気が5秒つく
                        player.addPotionEffect(PotionEffect(PotionEffectType.UNLUCK, Ticks.from(20, 0.0), 1))
                        player.addPotionEffect(PotionEffect(PotionEffectType.CONFUSION, Ticks.from(5.0), 1))
                        player.playSound(player.location, Sound.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 1f, 0.5f)
                    }

                    else -> {
                        player.playSound(player.location, Sound.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 1f, 1.6f)
                    }
                }
            }
        }.runTaskLater(XCorePlugin.instance, Ticks.from(3.0).toLong())
        return true
    }

    override fun onTabComplete(commandSender: CommandSender, command: Command, label: String, args: Array<String>): List<String> {
        return COMPLETE_LIST_EMPTY
    }
}