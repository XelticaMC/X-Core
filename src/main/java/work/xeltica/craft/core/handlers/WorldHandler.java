package work.xeltica.craft.core.handlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import io.papermc.paper.event.block.BlockPreDispenseEvent;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Entity;
import org.bukkit.entity.GlowItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.material.Directional;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;

import work.xeltica.craft.core.XCorePlugin;
import work.xeltica.craft.core.models.CraftRecipe;
import work.xeltica.craft.core.modules.hint.Hint;
import work.xeltica.craft.core.models.PlayerDataKey;
import work.xeltica.craft.core.modules.hint.HintModule;
import work.xeltica.craft.core.stores.PlayerStore;
import work.xeltica.craft.core.stores.WorldStore;
import work.xeltica.craft.core.utils.DiscordService;

/**
 * ワールド制御に関するハンドラーをまとめています。
 * TODO: 機能別に再編
 * @author Xeltica
 */
public class WorldHandler implements Listener {
    public WorldHandler() {
    }

    @EventHandler
    /**
     * 進捗を達成できるワールドを限定させるハンドラー
     */
    public void onAdvancementDone(PlayerAdvancementDoneEvent e) {
        final var p = e.getPlayer();
        if (!advancementWhitelist.contains(p.getWorld().getName())) {
            final var advancement = e.getAdvancement();

            for (var criteria : advancement.getCriteria()) {
                p.getAdvancementProgress(advancement).revokeCriteria(criteria);
            }
        }
    }

    @EventHandler
    /**
     * サンドボックスでのエンダーチェスト設置を防止する
     */
    public void onBlockPlace(BlockPlaceEvent e) {
        final var p = e.getPlayer();
        if (p.getWorld().getName().equals("sandbox")) {
            final var block = e.getBlock().getType();
            // エンダーチェストはダメ
            if (block == Material.ENDER_CHEST) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    /**
     * テレポート時のいろいろなガード機能など
     * TODO: 分割する
     */
    public void onPlayerTeleportGuard(PlayerTeleportEvent e) {
        final var p = e.getPlayer();
        final var world = e.getTo().getWorld();
        final var name = world.getName();
        final var store = WorldStore.getInstance();

        final var isLockedWorld = store.isLockedWorld(name);
        final var isCreativeWorld = store.isCreativeWorld(name);
        final var displayName = store.getWorldDisplayName(name);
        final var desc = store.getWorldDescription(name);

        final var from = e.getFrom().getWorld();
        final var to = e.getTo().getWorld();

        final var fromId = from.getUID();
        final var toId = to.getUID();

        if (fromId.equals(toId)) return;

        if (isLockedWorld && !p.hasPermission("hub.teleport." + name)) {
            p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1, 0.5f);
            p.sendMessage("§aわかば§rプレイヤーは§6" + displayName + "§rに行くことができません！\n§b/promo§rコマンドを実行して、昇格方法を確認してください！");
            e.setCancelled(true);
            return;
        }

        WorldStore.getInstance().saveCurrentLocation(p);

        final var hint = switch (to.getName()) {
            case "main" -> Hint.GOTO_MAIN;
            case "hub2" -> Hint.GOTO_LOBBY;
            case "wildarea2" -> Hint.GOTO_WILDAREA;
            case "wildarea2_nether" -> Hint.GOTO_WILDNETHER;
            case "wildarea2_the_end" -> Hint.GOTO_WILDEND;
            case "wildareab" -> Hint.GOTO_WILDAREAB;
            case "sandbox2" -> Hint.GOTO_SANDBOX;
            case "art" -> Hint.GOTO_ART;
            case "nightmare2" -> Hint.GOTO_NIGHTMARE;
            case "shigen_nether" -> Hint.GOTO_WILDNETHERB;
            case "shigen_end" -> Hint.GOTO_WILDENDB;
            default -> null;
        };

        // 以前サーバーに来ている or FIRST_SPAWN フラグが立っている
        final var isNotFirstTeleport = p.hasPlayedBefore() || PlayerStore.getInstance().open(p).getBoolean(PlayerDataKey.FIRST_SPAWN);

        if (to.getName().equals("main") && isNotFirstTeleport && !HintModule.INSTANCE.hasAchieved(p, Hint.GOTO_MAIN)) {
            // はじめてメインワールドに入った場合、対象のスタッフに通知する
            try {
                DiscordService.getInstance().alertNewcomer(p);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        if (hint != null && isNotFirstTeleport) {
            HintModule.INSTANCE.achieve(p, hint);
        }

        if (isCreativeWorld) {
            p.setGameMode(GameMode.CREATIVE);
        }
        if (name.equals("nightmare2")) {
            world.setDifficulty(Difficulty.HARD);
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
            world.setGameRule(GameRule.MOB_GRIEFING, false);
            world.setTime(18000);
            world.setStorm(true);
            world.setWeatherDuration(20000);
            world.setThundering(true);
            world.setThunderDuration(20000);
            p.playSound(p.getLocation(), Sound.BLOCK_END_PORTAL_SPAWN, SoundCategory.PLAYERS, 1, 0.5f);
        }
        if (desc != null) {
            p.sendMessage(desc);
        }

        Bukkit.getScheduler().runTaskLater(XCorePlugin.getInstance(), () -> {
            PlayerStore.getInstance().open(p).set(PlayerDataKey.FIRST_SPAWN, true);
        }, 20 * 5);
    }

    @EventHandler
    /**
     * ワールドを移動した時に、ワールド名を表示する機能
     */
    public void onPlayerMoveWorld(PlayerChangedWorldEvent e) {
        final var name = WorldStore.getInstance().getWorldDisplayName(e.getPlayer().getWorld());
        e.getPlayer().showTitle(Title.title(Component.text(name).color(TextColor.color(0xFFB300)), Component.empty()));
    }

    @EventHandler
    /**
     * 自動クラフト機能
     */
    public void onDispenser(BlockPreDispenseEvent event) {
        final var block = event.getBlock();
        if (block.getBlockData().getMaterial() != Material.DISPENSER) return;
        GlowItemFrame itemFlame = null;

        for (Entity entity: block.getLocation().toCenterLocation().getNearbyEntitiesByType(GlowItemFrame.class, 1)) {
            if (entity instanceof GlowItemFrame flame) {
                if (flame.getLocation().add(flame.getAttachedFace().getDirection()).getBlock().getLocation().toBlockLocation().equals(block.getLocation().toBlockLocation())) {
                    itemFlame = flame;
                    break;
                }
            }
        }
        if (itemFlame == null) return;
        event.setCancelled(true);

        final var item = itemFlame.getItem();
        final var recipes = new ArrayList<CraftRecipe>();
        for (Recipe recipe: Bukkit.getServer().getRecipesFor(item)) {
            if (recipe instanceof ShapedRecipe shapedRecipe) {
                final var ingredients = new ArrayList<ItemStack>();
                for (ItemStack i: shapedRecipe.getIngredientMap().values()) {
                    if (i == null) continue;
                    ingredients.add(new ItemStack(i.getType(),1));
                }
                recipes.add(new CraftRecipe(ingredients, recipe.getResult()));
            }
            if (recipe instanceof ShapelessRecipe shapelessRecipe) {
                final var ingredients = new ArrayList<ItemStack>();
                for (ItemStack i: shapelessRecipe.getIngredientList()) {
                    if (i == null) continue;
                    ingredients.add(new ItemStack(i.getType(), 1));
                }
                recipes.add(new CraftRecipe(ingredients, recipe.getResult()));
            }
        }

        if (block.getState() instanceof Dispenser dispenser) {
            final var inventory = dispenser.getInventory();

            for (CraftRecipe recipe: recipes) {
                final var fixedInventory = Arrays.stream(inventory.getContents()).filter(Objects::nonNull).collect(
                        Collectors.groupingBy(ItemStack::getType, Collectors.summingInt(ItemStack::getAmount))
                );
                var flag = true;
                for (Material ingredient: recipe.getFixedRecipe().keySet()) {
                    if (fixedInventory.containsKey(ingredient) && fixedInventory.get(ingredient) >= recipe.getFixedRecipe().get(ingredient)) {
                        fixedInventory.replace(ingredient, fixedInventory.get(ingredient) - recipe.getFixedRecipe().get(ingredient));
                        continue;
                    }
                    flag = false;
                }

                if (flag) {
                    for (ItemStack itemStack: inventory.getContents()) {
                        if (itemStack == null) continue;
                        inventory.remove(itemStack);
                    }
                    for (Map.Entry<Material, Integer> entry: fixedInventory.entrySet()) {
                        final var itemStack = new ItemStack(entry.getKey());
                        itemStack.setAmount(entry.getValue());
                        inventory.addItem(itemStack);
                    }

                    if (event.getBlock().getState().getData() instanceof Directional directional) {
                        final var location = block.getLocation().toBlockLocation().add(directional.getFacing().getDirection());
                        if (location.getBlock().getState() instanceof BlockInventoryHolder inventoryHolder) {
                            final var result = inventoryHolder.getInventory().addItem(recipe.result());
                            if (!result.isEmpty()) {
                                for (ItemStack i: result.values()) {
                                    block.getWorld().dropItem(location, i);
                                }
                            }
                            return;
                        }
                        block.getWorld().dropItem(location, recipe.result());
                        return;
                    }
                }
            }
        }
    }

    private final Set<String> advancementWhitelist = new HashSet<>(List.of(
        "wildarea2",
        "wildarea2_nether",
        "wildarea2_the_end",
        "wildareab",
        "shigen_nether",
        "shigen_end",
        "main",
        "nightmare2"
    ));
}
