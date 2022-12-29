package work.xeltica.craft.core.handlers

import io.papermc.paper.event.block.BlockPreDispenseEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.title.Title
import org.bukkit.*
import org.bukkit.block.Dispenser
import org.bukkit.entity.GlowItemFrame
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerAdvancementDoneEvent
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ShapelessRecipe
import org.bukkit.material.Directional
import work.xeltica.craft.core.XCorePlugin.Companion.instance
import work.xeltica.craft.core.api.playerStore.PlayerStore
import work.xeltica.craft.core.hooks.DiscordHook
import work.xeltica.craft.core.models.CraftRecipe
import work.xeltica.craft.core.modules.hint.Hint
import work.xeltica.craft.core.modules.hint.HintModule.achieve
import work.xeltica.craft.core.modules.hint.HintModule.hasAchieved
import work.xeltica.craft.core.modules.player.PlayerDataKey
import work.xeltica.craft.core.modules.world.WorldModule
import work.xeltica.craft.core.modules.world.WorldModule.getWorldDisplayName

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
        val p = e.player
        if (!advancementWhitelist.contains(p.world.name)) {
            val advancement = e.advancement
            for (criteria in advancement.criteria) {
                p.getAdvancementProgress(advancement).revokeCriteria(criteria!!)
            }
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
     * テレポート時のいろいろなガード機能など
     * TODO: 分割する
     */
    @EventHandler
    fun onPlayerTeleportGuard(e: PlayerTeleportEvent) {
        val p = e.player
        val world = e.to.world
        val name = world.name
        val worldModule = WorldModule
        val isLockedWorld = worldModule.isLockedWorld(name)
        val isCreativeWorld = worldModule.isCreativeWorld(name)
        val displayName = worldModule.getWorldDisplayName(name)
        val desc = worldModule.getWorldDescription(name)
        val from = e.from.world
        val to = e.to.world
        val fromId = from.uid
        val toId = to.uid
        if (fromId == toId) return
        if (isLockedWorld && !p.hasPermission("hub.teleport.$name")) {
            p.playSound(p.location, Sound.BLOCK_ANVIL_PLACE, 1f, 0.5f)
            p.sendMessage("§aわかば§rプレイヤーは§6$displayName§rに行くことができません！\n§b/promo§rコマンドを実行して、昇格方法を確認してください！")
            e.isCancelled = true
            return
        }
        worldModule.saveCurrentLocation(p)
        val hint = when (to.name) {
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

        // 以前サーバーに来ている or FIRST_SPAWN フラグが立っている
        val isNotFirstTeleport = p.hasPlayedBefore() || PlayerStore.open(p).getBoolean(PlayerDataKey.FIRST_SPAWN)
        if (to.name == "main" && !hasAchieved(p, Hint.GOTO_MAIN)) {
            // はじめてメインワールドに入った場合、対象のスタッフに通知する
            try {
                DiscordHook.alertNewcomer(p)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
        if (hint != null && isNotFirstTeleport) {
            achieve(p, hint)
        }
        if (isCreativeWorld) {
            p.gameMode = GameMode.CREATIVE
        }
        if (name == "nightmare2") {
            world.difficulty = Difficulty.HARD
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
            world.setGameRule(GameRule.DO_WEATHER_CYCLE, false)
            world.setGameRule(GameRule.MOB_GRIEFING, false)
            world.time = 18000
            world.setStorm(true)
            world.weatherDuration = 20000
            world.isThundering = true
            world.thunderDuration = 20000
            p.playSound(p.location, Sound.BLOCK_END_PORTAL_SPAWN, SoundCategory.PLAYERS, 1f, 0.5f)
        }
        if (desc != null) {
            p.sendMessage(desc)
        }
        Bukkit.getScheduler()
            .runTaskLater(instance, Runnable { PlayerStore.open(p)[PlayerDataKey.FIRST_SPAWN] = true }, 20 * 5)
    }

    /**
     * ワールドを移動した時に、ワールド名を表示する機能
     */
    @EventHandler
    fun onPlayerMoveWorld(e: PlayerChangedWorldEvent) {
        val name = getWorldDisplayName(e.player.world)
        e.player.showTitle(Title.title(Component.text(name).color(TextColor.color(0xFFB300)), Component.empty()))
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
                val fixedInventory = contents
                    .groupingBy { it.type }
                    .fold(0) { acc, i -> acc + i.amount }
                    .toMutableMap()
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

    private val advancementWhitelist: Set<String> = setOf(
        "wildarea2",
        "wildarea2_nether",
        "wildarea2_the_end",
        "wildareab",
        "shigen_nether",
        "shigen_end",
        "main",
        "nightmare2"
    )
}