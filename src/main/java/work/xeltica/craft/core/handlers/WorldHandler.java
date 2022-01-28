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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.ChunkPopulateEvent;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.material.Directional;
import work.xeltica.craft.core.XCorePlugin;
import work.xeltica.craft.core.models.CraftRecipe;
import work.xeltica.craft.core.models.Hint;
import work.xeltica.craft.core.models.PlayerDataKey;
import work.xeltica.craft.core.stores.HintStore;
import work.xeltica.craft.core.stores.PlayerStore;
import work.xeltica.craft.core.stores.WorldStore;

/**
 * ワールド制御に関するハンドラーをまとめています。
 * TODO: 機能別に再編
 * @author Xeltica
 */
public class WorldHandler implements Listener {
    public WorldHandler() {
    }

    @EventHandler
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

        if (fromId.equals(toId))
            return;

        if (isLockedWorld && !p.hasPermission("hub.teleport." + name)) {
            p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1, 0.5f);
            p.sendMessage(
                "§aわかば§rプレイヤーは§6" + displayName + "§rに行くことができません！\n" +
                "§b/promo§rコマンドを実行して、昇格方法を確認してください！"
            );
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
            case "hub" -> Hint.GOTO_CLASSIC_LOBBY;
            case "world" -> Hint.GOTO_CLASSIC_WORLD;
            case "wildarea" -> Hint.GOTO_CLASSIC_WILDAREA;
            default -> null;
        };

        final var isNotFirstTeleport = p.hasPlayedBefore() || PlayerStore.getInstance().open(p).getBoolean(PlayerDataKey.FIRST_SPAWN);

        if (hint != null && isNotFirstTeleport) {
            HintStore.getInstance().achieve(p, hint);
        }

        if (isCreativeWorld) {
            p.setGameMode(GameMode.CREATIVE);
        }
        if (name.equals("nightmare")) {
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
        if (name.equals("wildarea") && e.getFrom().getWorld().getName().equals("hub")) {
            // 最終ベッドがワイルドエリアにある場合、そこに飛ばす
            final var bed = p.getBedSpawnLocation();
            if (Objects.requireNonNull(bed).getWorld().getUID().equals(world.getUID())) {
                e.setTo(bed);
            }
        }
        if (desc != null) {
            p.sendMessage(desc);
        }

        Bukkit.getScheduler().runTaskLater(XCorePlugin.getInstance(), () -> {
            PlayerStore.getInstance().open(p).set(PlayerDataKey.FIRST_SPAWN, true);
        }, 20 * 5);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTeleportNotify(PlayerTeleportEvent e) {
        final var worldStore = WorldStore.getInstance();
        final var player = e.getPlayer();
        // スペクテイターはステルスっす
        if (player.getGameMode() == GameMode.SPECTATOR)
            return;
        final var from = e.getFrom().getWorld();
        final var to = e.getTo().getWorld();
        if (from.getName().equals(to.getName()))
            return;
        final var fromName = worldStore.getWorldDisplayName(from);
        final var toName = worldStore.getWorldDisplayName(to);

        if (fromName == null || toName == null) return;

        if (!player.hasPlayedBefore() && !PlayerStore.getInstance().open(player).getBoolean(PlayerDataKey.FIRST_SPAWN)) return;

        if (e.isCancelled()) return;

        final var toPlayers = to.getPlayers();
        final var allPlayersExceptInDestination = Bukkit.getOnlinePlayers().stream()
                // tpとマッチするUUIDがひとつも無いpのみを抽出
                .filter(p -> toPlayers.stream().allMatch(tp -> !tp.getUniqueId().equals(p.getUniqueId())))
                .collect(Collectors.toList());

        // fromにいる人宛に「toに行く旨」を伝える
        if (toName != null) {
            for (Player p : allPlayersExceptInDestination) {
                if (p.getUniqueId().equals(player.getUniqueId()))
                    continue;
                p.sendMessage(String.format("§a%s§bが§e%s§bに行きました", player.getDisplayName(), toName));
            }
        }

        // toにいる人宛に「fromから来た旨」を伝える
        if (fromName != null) {
            for (Player p : toPlayers) {
                if (p.getUniqueId().equals(player.getUniqueId()))
                    continue;
                p.sendMessage(String.format("§a%s§bが§e%s§bから来ました", player.getDisplayName(), fromName));
            }
        }
    }

    @EventHandler
    public void onPlayerMoveWorld(PlayerChangedWorldEvent e) {
        final var name = WorldStore.getInstance().getWorldDisplayName(e.getPlayer().getWorld());
        e.getPlayer().showTitle(Title.title(Component.text(name).color(TextColor.color(0xFFB300)), Component.empty()));
    }

    @EventHandler()
    public void onChunkPopulateEvent(ChunkPopulateEvent e) {
        // TODO ハードコードをやめる
        if (!e.getWorld().getName().equals("main")) return;
        final var c = e.getChunk();

        for (var z = 0; z < 16; z++) {
            for (var x = 0; x < 16; x++) {
                final var yMax = c.getWorld().getHighestBlockYAt(c.getX() + x, c.getZ() + z);
                for (int y = 1; y <= yMax; y++) {
                    final var block = c.getBlock(x, y, z);
                    final var replacer = replace(block.getType());
                    if (replacer != null) {
                        block.setType(replacer, false);
                    }
                }
            }
        }
    }

    @EventHandler
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
                    ingredients.add(new ItemStack(i.getType(),1));
                }
                recipes.add(new CraftRecipe(ingredients, recipe.getResult()));
            }
            if (recipe instanceof ShapelessRecipe shapelessRecipe) {
                final var ingredients = new ArrayList<ItemStack>();
                for (ItemStack i: shapelessRecipe.getIngredientList()) {
                    ingredients.add(new ItemStack(i.getType(), 1));
                }
                recipes.add(new CraftRecipe(ingredients, recipe.getResult()));
            }
        }

        if (block.getState() instanceof Dispenser dispenser) {
            final var inventory = dispenser.getInventory();

            for (CraftRecipe recipe: recipes) {
                final var fixedInventory = Arrays.stream(inventory.getContents()).filter(Objects::nonNull).collect(
                        Collectors.groupingBy(x -> x.getType(), Collectors.summingInt(ItemStack::getAmount))
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
                            inventoryHolder.getInventory().addItem(recipe.result());
                            return;
                        }
                        block.getWorld().dropItem(location, recipe.result());
                        return;
                    }
                }
            }
        }

    }

    private Material replace(Material mat) {
        return switch (mat) {
            case COAL_ORE -> Material.STONE;
            case IRON_ORE -> Material.STONE;
            case GOLD_ORE -> Material.STONE;
            case DIAMOND_ORE -> Material.STONE;
            case LAPIS_ORE -> Material.STONE;
            case REDSTONE_ORE -> Material.STONE;
            case EMERALD_ORE -> Material.STONE;
            case COPPER_ORE -> Material.STONE;
            case DEEPSLATE_COAL_ORE -> Material.DEEPSLATE;
            case DEEPSLATE_IRON_ORE -> Material.DEEPSLATE;
            case DEEPSLATE_GOLD_ORE -> Material.DEEPSLATE;
            case DEEPSLATE_DIAMOND_ORE -> Material.DEEPSLATE;
            case DEEPSLATE_LAPIS_ORE -> Material.DEEPSLATE;
            case DEEPSLATE_REDSTONE_ORE -> Material.DEEPSLATE;
            case DEEPSLATE_EMERALD_ORE -> Material.DEEPSLATE;
            case DEEPSLATE_COPPER_ORE -> Material.DEEPSLATE;
            default -> null;
        };
    }

    private final Set<String> advancementWhitelist = new HashSet<>(List.of(
        "wildarea2",
        "wildarea2_nether",
        "wildarea2_the_end",
        "main",
        "nightmare2",

        "world",
        "world_nether",
        "world_the_end",
        "wildarea",
        "travel_wildarea",
        "travel_megawild"
    ));
}
