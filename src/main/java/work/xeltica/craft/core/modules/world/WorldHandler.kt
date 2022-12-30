package work.xeltica.craft.core.modules.world

import io.papermc.paper.event.block.BlockPreDispenseEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.title.Title
import org.bukkit.*
import org.bukkit.block.Dispenser
import org.bukkit.entity.GlowItemFrame
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerAdvancementDoneEvent
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ShapelessRecipe
import org.bukkit.material.Directional
import work.xeltica.craft.core.XCorePlugin.Companion.instance
import work.xeltica.craft.core.api.playerStore.PlayerStore
import work.xeltica.craft.core.gui.Gui
import work.xeltica.craft.core.hooks.DiscordHook
import work.xeltica.craft.core.models.CraftRecipe
import work.xeltica.craft.core.modules.hint.Hint
import work.xeltica.craft.core.modules.hint.HintModule
import work.xeltica.craft.core.modules.player.PlayerDataKey
import work.xeltica.craft.core.utils.CollectionHelper.sum
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
        val isNotFirstTeleport = player.hasPlayedBefore() || PlayerStore.open(player).getBoolean(PlayerDataKey.FIRST_SPAWN)
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
            PlayerStore.open(e.player)[PlayerDataKey.FIRST_SPAWN] = true
        }, Ticks.from(5.0).toLong())
    }

    @EventHandler
    fun onPlayerTryBed(e: PlayerInteractEvent) {
        val p = e.player
        val block = e.clickedBlock
        if (e.action != Action.RIGHT_CLICK_BLOCK || block == null) return

        val worldInfo = WorldModule.getWorldInfo(p.world.name)
        if (!worldInfo.canSleep && Tag.BEDS.isTagged(block.type)) {
            Gui.getInstance().error(p, "ベッドはこの世界では使えない…")
            e.isCancelled = true
        }
    }

    /*
     * 自動クラフト機能
     */
    @EventHandler
    fun onDispenser(event: BlockPreDispenseEvent) {
        val block = event.block
        val blockData = block.blockData
        if (blockData.material != Material.DISPENSER) return
        val frames = block.location.toCenterLocation().getNearbyEntitiesByType(GlowItemFrame::class.java, 1.0)
        val itemFrame = frames.firstOrNull {
            it.location.clone()
                .add(it.attachedFace.direction).block.location.toBlockLocation() == block.location.toBlockLocation()
        } ?: return

        event.isCancelled = true
        val item = itemFrame.item

        val recipes = ArrayList<CraftRecipe>()
        for (recipe in Bukkit.getServer().getRecipesFor(item)) {
            if (recipe is ShapedRecipe) {
                val ingredients = ArrayList<ItemStack>()
                for (i in recipe.ingredientMap.values) {
                    if (i == null) continue
                    ingredients.add(ItemStack(i.type, 1))
                }
                recipes.add(CraftRecipe(ingredients, recipe.getResult()))
            }
            if (recipe is ShapelessRecipe) {
                val ingredients = ArrayList<ItemStack>()
                for (i in recipe.ingredientList) {
                    if (i == null) continue
                    ingredients.add(ItemStack(i.type, 1))
                }
                recipes.add(CraftRecipe(ingredients, recipe.getResult()))
            }
        }
        val state = block.state
        if (state is Dispenser) {
            val inventory = state.inventory
            val contents = inventory.contents.filterNotNull()

            for (recipe in recipes) {
                val fixedInventory = contents.groupingBy { it.type }.sum { it.amount }.toMutableMap()
                var flag = true
                for (ingredient in recipe.fixedRecipe.keys) {
                    if (fixedInventory.containsKey(ingredient) && fixedInventory[ingredient]!! >= recipe.fixedRecipe[ingredient]!!) {
                        fixedInventory.replace(
                            ingredient,
                            fixedInventory[ingredient]!! - recipe.fixedRecipe[ingredient]!!
                        )
                        continue
                    }
                    flag = false
                }
                if (flag) {
                    for (itemStack in contents) {
                        inventory.remove(itemStack)
                    }
                    for ((key, value) in fixedInventory) {
                        val itemStack = ItemStack(key)
                        itemStack.amount = value
                        inventory.addItem(itemStack)
                    }
                    if (blockData is Directional) {
                        val location: Location = block.location.toBlockLocation().add(blockData.facing.direction)
                        val result: HashMap<Int, ItemStack> = state.inventory.addItem(recipe.result)
                        if (result.isNotEmpty()) {
                            for (i in result.values) {
                                block.world.dropItem(location, i)
                            }
                        }
                        return
                    }
                }
            }
        }
    }
}