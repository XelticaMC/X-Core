package work.xeltica.craft.core.modules.ebipowerShop

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.StringUtil
import work.xeltica.craft.core.XCorePlugin
import work.xeltica.craft.core.api.commands.CommandPlayerOnlyBase
import work.xeltica.craft.core.gui.Gui
import work.xeltica.craft.core.gui.MenuItem
import work.xeltica.craft.core.modules.hint.Hint
import work.xeltica.craft.core.modules.hint.HintModule
import java.util.function.Consumer

class EpEffectShopCommand : CommandPlayerOnlyBase() {
    override fun execute(player: Player, command: Command, label: String, args: Array<out String>): Boolean {
        val subCommand = if (args.isNotEmpty()) args[0] else null

        if (subCommand == null || !player.hasPermission("otanoshimi.command.epeffectshop." + subCommand.lowercase())) {
            openShop(player)
            return true
        }
        when (subCommand.lowercase()) {
            "add" -> {
                if (args.size != 5) {
                    player.sendMessage("/epeffectshop add <type> <power> <time> <cost>")
                    return true
                }
                val type = PotionEffectType.getByName(args[1])
                if (type == null) {
                    player.sendMessage("/epeffectshop add <type> <power> <time> <cost>")
                    return true
                }
                val power = args[2].toInt()
                val time = args[3].toInt()
                val cost = args[4].toInt()
                EbiPowerShopModule.addEffectItem(EbiPowerEffect(type, power, time, cost))
                player.sendMessage("追加しました。")
            }

            "delete" -> openShopMenu(player, "削除するアイテムを選んでください") { item ->
                EbiPowerShopModule.deleteEffectItem(item)
                player.sendMessage("削除しました。")
            }
        }
        return true
    }

    private fun openShop(player: Player) {
        openShopMenu(player, "購入するステータス効果を選んでください") { item ->

            when (EbiPowerShopModule.tryBuyEffectItem(player, item)) {
                EbiPowerShopModule.Result.NO_ENOUGH_POWER -> {
                    player.sendMessage("エビパワー不足のため、購入に失敗しました。")
                    player.playSound(player.location, Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1f, 0.5f)
                }

                EbiPowerShopModule.Result.SUCCESS -> {
                    player.sendMessage(
                        String.format(
                            "§b%s%s§rを§6%d秒間§r付与しました。",
                            toJapanese(item.effectType),
                            if (item.level > 1) item.level.toString() else "",
                            item.time
                        )
                    )
                    player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1f, 1f)
                    HintModule.achieve(player, Hint.EPEFFECTSHOP)
                }

                else -> {
                    player.sendMessage("不明なエラーが発生しました。")
                    player.playSound(player.location, Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1f, 0.5f)
                }
            }
            Bukkit.getScheduler().runTask(XCorePlugin.instance, Runnable { openShop(player) })
        }
    }

    private fun openShopMenu(player: Player, title: String, onChosen: Consumer<EbiPowerEffect>?) {
        val ui = Gui.getInstance()
        val items = EbiPowerShopModule.effectShopItems.map { m ->
            val stack = ItemStack(Material.POTION)
            stack.editMeta { meta ->
                if (meta is PotionMeta) {
                    meta.addCustomEffect(m.toPotionEffect(), true)
                    meta.color = m.effectType.color
                }
            }
            val displayName = String.format(
                "%s%s %d秒 (%dEP)", toJapanese(m.effectType),
                if (m.level > 1) m.level.toString() else "",
                m.time, m.cost
            )
            MenuItem(displayName, { onChosen?.accept(m) }, stack)
        }.toList()
        ui.openMenu(player, title, items)
    }

    private fun toJapanese(type: PotionEffectType): String {
        // TODO マップから変換
        return when (type.name) {
            "SPEED" -> "移動速度上昇"
            "SLOW" -> "移動速度低下"
            "FAST_DIGGING" -> "採掘速度上昇"
            "SLOW_DIGGING" -> "採掘速度低下"
            "INCREASE_DAMAGE" -> "攻撃力上昇"
            "HEAL" -> "即時回復"
            "HARM" -> "即時ダメージ"
            "JUMP" -> "跳躍力上昇"
            "CONFUSION" -> "吐き気"
            "REGENERATION" -> "再生能力"
            "DAMAGE_RESISTANCE" -> "耐性"
            "FIRE_RESISTANCE" -> "火炎耐性"
            "WATER_BREATHING" -> "水中呼吸"
            "INVISIBILITY" -> "透明化"
            "BLINDNESS" -> "盲目"
            "NIGHT_VISION" -> "暗視"
            "HUNGER" -> "空腹"
            "WEAKNESS" -> "弱体化"
            "POISON" -> "毒"
            "WITHER" -> "衰弱"
            "HEALTH_BOOST" -> "体力増強"
            "ABSORPTION" -> "衝撃吸収"
            "SATURATION" -> "満腹度回復"
            "GLOWING" -> "発光"
            "LEVITATION" -> "浮遊"
            "LUCK" -> "幸運"
            "UNLUCK" -> "不運"
            "SLOW_FALLING" -> "落下速度低下"
            "CONDUIT_POWER" -> "コンジットパワー"
            "DOLPHINS_GRACE" -> "イルカの好意"
            "BAD_OMEN" -> "不吉な予感"
            "HERO_OF_THE_VILLAGE" -> "村の英雄"
            else -> "不明"
        }
    }

    override fun onTabComplete(commandSender: CommandSender, command: Command, label: String, args: Array<String>): List<String> {
        if (args.size == 1) {
            val commands = listOf("add", "delete")
            val completions = ArrayList<String>()
            StringUtil.copyPartialMatches(args[0], commands, completions)
            completions.sort()
            return completions
        }
        return COMPLETE_LIST_EMPTY
    }
}