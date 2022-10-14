package work.xeltica.craft.core.xphone.apps

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import work.xeltica.craft.core.gui.Gui
import work.xeltica.craft.core.gui.MenuItem
import work.xeltica.craft.core.modules.halloween.HalloweenModule
import work.xeltica.craft.core.stores.WorldStore
import java.util.Calendar

/**
 * テレポートアプリ
 * @author Ebise Lutica
 */
class TeleportApp : AppBase() {
    override fun getName(player: Player): String = if (isShigen(player)) "メインワールドに帰る" else "テレポート"

    override fun getIcon(player: Player): Material = if (isShigen(player)) Material.CREEPER_HEAD else Material.COMPASS

    override fun onLaunch(player: Player) {
        if (isShigen(player)) {
            WorldStore.getInstance().teleportToSavedLocation(player, "main")
            return
        }

        showMainMenu(player)
    }

    override fun isVisible(player: Player): Boolean {
        return !worldsBlackList.contains(player.world.name)
    }

    private fun showMainMenu(player: Player) {
        Gui.getInstance().openMenu(player, "テレポート", listOf(
            MenuItem("ワールド…", { showWorldsMenu(player) }, Material.GRASS_BLOCK),
            MenuItem("初期スポーン", { player.performCommand("respawn") }, Material.FIREWORK_ROCKET),
            MenuItem("ベッド", { player.performCommand("respawn bed") }, Material.RED_BED),
        ))
    }

    private fun showWorldsMenu(player: Player) {
        val list = ArrayList<MenuItem>()
        val worldName = player.world.name

        list.add(MenuItem("戻る…", { showMainMenu(player) }, Material.REDSTONE_TORCH))

        list.add(MenuItem("ロビー", { player.performCommand("hub") }, Material.NETHERITE_BLOCK))
        list.add(MenuItem("メインワールド", { WorldStore.getInstance().teleportToSavedLocation(player, "main") }, Material.CRAFTING_TABLE))
        list.add(MenuItem("共有ワールド…", { showSharedWorldsMenu(player) }, Material.CHEST))
        if (player.hasPermission("hub.teleport.sandbox2")) {
            list.add(MenuItem("サンドボックス", { WorldStore.getInstance().teleportToSavedLocation(player, "sandbox2") }, Material.RED_CONCRETE))
        }
        if (player.hasPermission("hub.teleport.art")) {
            list.add(MenuItem("アートワールド", { WorldStore.getInstance().teleportToSavedLocation(player, "art") }, Material.PAINTING))
        }

        if (worldName == "main") {
            list.add(MenuItem("資源ワールド…", { showShigenWorldsMenu(player) }, Material.DIAMOND_PICKAXE))

            val calendar = Calendar.getInstance()
            val month = calendar.get(Calendar.MONTH) + 1
            // 夏イベント用テレポート。
            // TODO イベント機能をどっかにうつす
            if (month == 8 || player.isOp) {
                list.add(
                    MenuItem("イベント", {
                        val eventWorldLocation = Bukkit.getWorld("event")?.spawnLocation
                        if (eventWorldLocation == null) {
                            player.sendMessage("No such world")
                            return@MenuItem
                        }
                        player.teleportAsync(eventWorldLocation)
                    }, Material.TROPICAL_FISH)
                )
            }
            // TODO イベント機能をどっかにうつす
            if (HalloweenModule.isEventMode || player.isOp) {
                list.add(
                    MenuItem("イベントワールドへ（4アメが必要）", {
                        val eventWorldLocation = Bukkit.getWorld("event2")?.spawnLocation
                        if (eventWorldLocation == null) {
                            player.sendMessage("No such world")
                            return@MenuItem
                        }
                        if (!HalloweenModule.tryTakeCandy(player, 4)) {
                            Gui.getInstance().error(player, "アメが足りません！イベントワールドへの移動にはアメを4つ消費します…。")
                            return@MenuItem
                        }
                        player.teleportAsync(eventWorldLocation)
                    }, Material.JACK_O_LANTERN)
                )
            }
        }

        Gui.getInstance().openMenu(player, "テレポート", list)
    }

    private fun showSharedWorldsMenu(player: Player) {
        Gui.getInstance().openMenu(player, "共有ワールド…", listOf(
            MenuItem("戻る…", { showWorldsMenu(player) }, Material.REDSTONE_TORCH),

            MenuItem("共有ワールド", {
                WorldStore.getInstance().teleportToSavedLocation(player, "wildarea2")
            }, Material.GRASS_BLOCK),

            MenuItem("共有ネザー", {
                WorldStore.getInstance().teleportToSavedLocation(player, "wildarea2_nether")
            }, Material.NETHERRACK),

            MenuItem("共有エンド", {
                WorldStore.getInstance().teleportToSavedLocation(player, "wildarea2_the_end")
            }, Material.END_STONE)
        ))
    }

    private fun showShigenWorldsMenu(player: Player) {
        Gui.getInstance().openMenu(player, "資源ワールド…", listOf(
            MenuItem("戻る…", { showWorldsMenu(player) }, Material.REDSTONE_TORCH),
            MenuItem("資源ワールド", {
                val loc: Location = player.location
                val x = loc.blockX * 16
                val z = loc.blockZ * 16
                player.sendMessage("ワールドを準備中です…。そのまましばらくお待ちください。")
                player.world.getChunkAtAsync(x, z)
                    .thenAccept {
                        val wildareab = Bukkit.getWorld("wildareab")
                        if (wildareab == null) {
                            Gui.getInstance().error(player, "テレポートに失敗しました。ワールドが作成されていないようです。")
                            return@thenAccept
                        }
                        val y = wildareab.getHighestBlockYAt(x, z) + 1
                        val land =
                            Location(wildareab, x.toDouble(), (y - 1).toDouble(), z.toDouble())
                        if (land.block.type == Material.WATER) {
                            land.block.type = Material.STONE
                        }
                        player.teleportAsync(Location(wildareab, x.toDouble(), y.toDouble(), z.toDouble()))
                    }
            }, Material.GRASS_BLOCK),

            MenuItem("資源ネザー", {
                WorldStore.getInstance().teleportToSavedLocation(player, "shigen_nether")
            }, Material.NETHERRACK),

            MenuItem("資源エンド", {
                WorldStore.getInstance().teleportToSavedLocation(player, "shigen_end")
            }, Material.END_STONE)
        ))
    }

    private fun isShigen(player: Player) = shigenWorldsList.contains(player.world.name)

    private val shigenWorldsList = listOf(
        "wildareab",
        "shigen_nether",
        "shigen_end",
    )

    private val worldsBlackList = listOf(
        "hub2",
        "event",
    )
}