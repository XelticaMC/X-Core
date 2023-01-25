package work.xeltica.craft.core.modules.setMarker

import org.bukkit.Location
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.util.StringUtil
import work.xeltica.craft.core.api.commands.CommandPlayerOnlyBase
import java.util.*

class SetMarkerCommand : CommandPlayerOnlyBase() {

    ///marker <set | move | delete> <location | index>

    override fun execute(player: Player, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) return false
        val subCommand = args[0].lowercase(Locale.getDefault())
        when (subCommand) {
            // /marker set [location]
            "set" -> {
                when (args.size) {
                    1 -> {
                        SetMarkerModule.setMarker(player)
                        return true
                    }

                    2 -> {
                        player.sendMessage("§c座標を入力してください！")
                    }

                    3 -> {
                        player.sendMessage("§c座標を入力してください！")
                    }

                    4 -> {
                        val x = args[1]
                        val y = args[2]
                        val z = args[3]

                        val location = SetMarkerModule.tildaToLocation(player, x, y, z) ?: return false
                        SetMarkerModule.setMarker(player, location.toBlockLocation())
                    }

                    else -> player.sendMessage("§c引数が多すぎます！")
                }
            }
            // /marker delete <index | location>
            "delete" -> {
                when (args.size) {
                    1 -> {
                        SetMarkerModule.dellAll(player)
                        return true
                    }

                    2 -> {
                        val index: Int
                        try {
                            index = args[1].toInt()
                        } catch (e: NumberFormatException) {
                            player.sendMessage("§cマーカー番号には整数を入力してください！")
                            return false
                        }
                        SetMarkerModule.dellMarker(player, index)
                    }

                    3 -> {
                        player.sendMessage("§c正しい座標を入力してください！")
                    }

                    4 -> {
                        val x = args[1]
                        val y = args[2]
                        val z = args[3]

                        val location = SetMarkerModule.tildaToLocation(player, x, y, z) ?: return false
                        SetMarkerModule.dellMarker(player, location.toBlockLocation())
                    }

                    else -> player.sendMessage("§c引数が多すぎます！")
                }
            }

            "move" -> {
                when (args.size) {
                    1 -> {
                        player.sendMessage("§cマーカー番号もしくは座標を入力してください！")
                        return true
                    }

                    2 -> {
                        val index: Int
                        try {
                            index = args[1].toInt()
                        } catch (e: NumberFormatException) {
                            player.sendMessage("§cマーカー番号には整数を入力してください！")
                            return false
                        }
                        var loc = player.location
                        loc = Location(player.world, loc.x, loc.y, loc.z).toBlockLocation()
                        SetMarkerModule.moveMarker(player, index, loc, null)
                    }

                    3 -> {
                        player.sendMessage("§c正しい座標を入力してください！")
                    }

                    4 -> {
                        val x = args[1]
                        val y = args[2]
                        val z = args[3]
                        var loc2 = player.location
                        loc2 = Location(player.world, loc2.x, loc2.y, loc2.z).toBlockLocation()

                        val loc1 = SetMarkerModule.tildaToLocation(player, x, y, z) ?: return false
                        SetMarkerModule.moveMarker(player, loc1.toBlockLocation(), loc2)
                    }

                    5 -> {
                        val index: Int
                        try {
                            index = args[1].toInt()
                        } catch (e: NumberFormatException) {
                            player.sendMessage("§cマーカー番号には整数を入力してください！")
                            return false
                        }
                        val x = args[2]
                        val y = args[3]
                        val z = args[4]

                        val loc = SetMarkerModule.tildaToLocation(player, x, y, z) ?: return false
                        SetMarkerModule.moveMarker(player, index, loc.toBlockLocation(), null)
                    }

                    6 -> {
                        player.sendMessage("§c座標を入力してください！")
                    }

                    7 -> {
                        val x = args[1]
                        val y = args[2]
                        val z = args[3]
                        val x2 = args[4]
                        val y2 = args[5]
                        val z2 = args[6]

                        val location = SetMarkerModule.tildaToLocation(player, x, y, z) ?: return false
                        val endlocation = SetMarkerModule.tildaToLocation(player, x2, y2, z2) ?: return false
                        SetMarkerModule.moveMarker(player, location.toBlockLocation(), endlocation.toBlockLocation())
                    }

                    else -> player.sendMessage("§c引数が多すぎます！")
                }
            }

            "reload" -> {
                SetMarkerModule.reload(player)
            }

            else -> player.sendMessage("< delete | move | reload | set >のどれかを指定して下さい")
            // /marker move <index | location> [location]
        }

        return true
    }

    override fun onTabComplete(commandSender: CommandSender, command: Command, label: String, args: Array<String>): List<String>? {
        if (args.isEmpty()) return COMPLETE_LIST_EMPTY
        val subcommand = args[0].lowercase(Locale.getDefault())
        when (args.size) {
            1 -> {
                val commands = listOf("delete", "set", "move", "reload")
                val completions = ArrayList<String>()
                StringUtil.copyPartialMatches(subcommand, commands, completions)
                completions.sort()
                return completions
            }

            2 -> {
                when (args[0]) {
                    "set" -> {
                        val subargument = args[1].lowercase(Locale.getDefault())
                        val argument = listOf("~", "~ ~", "~ ~ ~")
                        val completions = ArrayList<String>()
                        StringUtil.copyPartialMatches(subargument, argument, completions)
                        //completions.addAll(argument)
                        completions.sort()
                        return completions
                    }

                    "delete", "move" -> {
                        val subargument = args[1].lowercase(Locale.getDefault())
                        val argument = listOf("マーカー番号", "~", "~ ~", "~ ~ ~")
                        val completions = ArrayList<String>()
                        StringUtil.copyPartialMatches(subargument, argument, completions)
                        completions.sort()
                        return completions
                    }
                }
            }

            3, 5 -> {
                when (args[0]) {
                    "move" -> {
                        val subargument = args[2].lowercase(Locale.getDefault())
                        val argument = listOf("~", "~ ~", "~ ~ ~")
                        val completions = ArrayList<String>()
                        StringUtil.copyPartialMatches(subargument, argument, completions)
                        completions.sort()
                        return completions
                    }
                }
            }
        }
        return COMPLETE_LIST_EMPTY
    }
}