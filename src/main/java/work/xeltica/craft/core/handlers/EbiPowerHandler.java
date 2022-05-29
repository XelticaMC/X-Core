package work.xeltica.craft.core.handlers;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.Tag;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Hoglin;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Player;
import org.bukkit.entity.SkeletonHorse;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import net.kyori.adventure.text.Component;
import work.xeltica.craft.core.events.RealTimeNewDayEvent;
import work.xeltica.craft.core.models.Hint;
import work.xeltica.craft.core.models.PlayerDataKey;
import work.xeltica.craft.core.stores.EbiPowerStore;
import work.xeltica.craft.core.stores.HintStore;
import work.xeltica.craft.core.stores.MobEPStore;
import work.xeltica.craft.core.stores.PlayerStore;

/**
 * エビパワー関連のイベントハンドラをまとめています。
 * @author Xeltica
 */
public class EbiPowerHandler implements Listener{
    public EbiPowerHandler() {
        // エビパワーが貯まらないワールドのリストを構築
        // TODO: 設定ファイルに移す
        epBlackList.add("hub2");
        epBlackList.add("art");
        epBlackList.add("sandbox2");
        epBlackList.add("pvp");
        epBlackList.add("hub_dev");

        crops.addAll(Tag.CROPS.getValues());

        breakBonusList.addAll(Tag.BASE_STONE_OVERWORLD.getValues());
        breakBonusList.addAll(Tag.BASE_STONE_NETHER.getValues());
        breakBonusList.addAll(Tag.ICE.getValues());
        breakBonusList.addAll(Tag.DIRT.getValues());
        breakBonusList.addAll(Tag.SAND.getValues());

        breakBonusList.addAll(Tag.COAL_ORES.getValues());
        breakBonusList.addAll(Tag.IRON_ORES.getValues());
        breakBonusList.addAll(Tag.COPPER_ORES.getValues());
        breakBonusList.addAll(Tag.GOLD_ORES.getValues());
        breakBonusList.addAll(Tag.REDSTONE_ORES.getValues());
        breakBonusList.addAll(Tag.EMERALD_ORES.getValues());
        breakBonusList.addAll(Tag.LAPIS_ORES.getValues());
        breakBonusList.addAll(Tag.DIAMOND_ORES.getValues());
        breakBonusList.addAll(Tag.LOGS.getValues());

        breakBonusList.add(Material.NETHER_QUARTZ_ORE);
        breakBonusList.add(Material.OBSIDIAN);
        breakBonusList.add(Material.ANCIENT_DEBRIS);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDamageFrailCreatures(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player killer && e.getEntity() instanceof LivingEntity victim) {
            if (playerIsInBlacklisted(killer)) return;
            // スポナーは対象外
            if (victim.fromMobSpawner()) return;
            // TODO: TT登録可能にしてその中のキルは対象外にする

            if (victim instanceof Cat || victim instanceof Ocelot) {
                store().tryTake(killer, 100);
                notification(killer, "可愛い可愛いネコちゃんを殴るなんて！100EPを失った。");
                killer.playSound(killer.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CURE, SoundCategory.PLAYERS, 0.7f, 0.5f);
                HintStore.getInstance().achieve(killer, Hint.VIOLENCE_CAT);
            } else if (victim instanceof Tameable pet && pet.isTamed() && !(victim instanceof SkeletonHorse)) {
                store().tryTake(killer, 10);
                notification(killer, "ペットを殴るなんて！10EPを失った。");
                killer.playSound(killer.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CURE, SoundCategory.PLAYERS, 0.7f, 0.5f);
                HintStore.getInstance().achieve(killer, Hint.VIOLENCE_PET);
            } else if (victim instanceof Ageable man && !(victim instanceof Monster) && !(victim instanceof Hoglin) && !man.isAdult()) {
                store().tryTake(killer, 10);
                notification(killer, "子供を殴るなんて！10EPを失った。");
                killer.playSound(killer.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CURE, SoundCategory.PLAYERS, 0.7f, 0.5f);
                HintStore.getInstance().achieve(killer, Hint.VIOLENCE_CHILD);
            }
        }
    }
    
    @EventHandler
    public void onPlayerKillMobs(EntityDeathEvent e) {
        final var victim = e.getEntity();
        final var killer = e.getEntity().getKiller();
        if (killer != null) {
            if (playerIsInBlacklisted(killer)) return;
            // don't kill cats
            if (victim instanceof Cat || victim instanceof Ocelot) return;
            // don't kill tamed pets
            if (victim instanceof Tameable pet && pet.isTamed() && !(victim instanceof SkeletonHorse)) return;
            // don't kill non-monster children
            if (victim instanceof Ageable man && !(victim instanceof Monster) && !(victim instanceof Hoglin) && !man.isAdult()) return;
            // ignore creatures from spawner
            if (victim.fromMobSpawner()) return;
            var ep = "nightmare2".equals(killer.getWorld().getName()) ? MobEPStore.getInstance().getMobDropEP(victim, e) : 6;
            final var buff = getMobDropBonus(killer.getInventory().getItemInMainHand()) * 4;
            ep += (buff > 0 ? random.nextInt(buff) : 0);
            if (ep > 0) {
                store().tryGive(killer, ep);
                HintStore.getInstance().achieve(killer, Hint.KILL_MOB_AND_EARN_MONEY);
            }
        }
    }

    @EventHandler
    public void onPlayerLoggedIn(PlayerJoinEvent e) {
        final var now = new Date();
        final var ps = PlayerStore.getInstance();
        final var record = ps.open(e.getPlayer());
        final var prev = new Date(record.getLong(PlayerDataKey.LAST_JOINED, now.getTime()));
        if (prev.getYear() != now.getYear() && prev.getMonth() != now.getMonth() && prev.getDate() != now.getDate()) {
            store().tryGive(e.getPlayer(), LOGIN_BONUS_POWER);
            notification(e.getPlayer(), "ログボ達成！" + LOGIN_BONUS_POWER + "EPを獲得。");
        }
        record.set(PlayerDataKey.LAST_JOINED, now.getTime());
    }

    @EventHandler
    public void onHarvestCrops(BlockBreakEvent e) {
        final var p = e.getPlayer();
        if (playerIsInBlacklisted(p)) return;
        if (e.getBlock().getBlockData() instanceof org.bukkit.block.data.Ageable a && a.getAge() == a.getMaximumAge()) {
            final var tool = p.getInventory().getItemInMainHand();
            final var bonus = getBlockDropBonus(tool);
            final var power = (1 + bonus) * HARVEST_POWER_MULTIPLIER;
            store().tryGive(p, power);
            HintStore.getInstance().achieve(p, Hint.HARVEST_AND_EARN_MONEY);
            // もし幸運ボーナスがあれば30%の確率で耐久が減っていく
            if (bonus > 0 && random.nextInt(100) < 30) {
                tool.editMeta(meta -> {
                    if (meta instanceof Damageable toolMeta) {
                        toolMeta.setDamage(toolMeta.getDamage() + 1);
                    }
                });
            }
        }
    }

    @EventHandler
    public void onBreedEntities(EntityBreedEvent e) {
        if (e.getBreeder() instanceof Player player) {
            if (playerIsInBlacklisted(player)) return;
            EbiPowerStore.getInstance().tryGive(player, 2);
            HintStore.getInstance().achieve(player, Hint.BREED_AND_EARN_MONEY);
        }
    }

    @EventHandler
    public void onMineBlocks(BlockBreakEvent e) {
        if (!breakBonusList.contains(e.getBlock().getType())) return;
        if (playerIsInBlacklisted(e.getPlayer())) return;

        final var record = PlayerStore.getInstance().open(e.getPlayer());
        final var brokenBlocksCount = record.getInt(PlayerDataKey.BROKEN_BLOCKS_COUNT);
        // リミット超えていたら対象外
        if (brokenBlocksCount > BREAK_BLOCK_BONUS_LIMIT) return;

        // 回収できなかったら対象外
        if (!e.isDropItems()) return;

        // シルクタッチは対象外
        final var item = e.getPlayer().getInventory().getItemInMainHand();
        if (item.containsEnchantment(Enchantment.SILK_TOUCH)) return;

        record.set(PlayerDataKey.BROKEN_BLOCKS_COUNT, brokenBlocksCount + 1);

        if (brokenBlocksCount + 1 >= BREAK_BLOCK_BONUS_LIMIT) {
            HintStore.getInstance().achieve(e.getPlayer(), Hint.MINERS_DREAM);
        }
        store().tryGive(e.getPlayer(), 1);
    }

    @EventHandler
    public void onNewDayToResetBrokenBlocksCount(RealTimeNewDayEvent e) {
        PlayerStore.getInstance().openAll().forEach(record -> record.set(PlayerDataKey.BROKEN_BLOCKS_COUNT, 0));
    }

    private void notification(Player p, String mes) {
        p.sendActionBar(Component.text(mes));
        // p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1, 2);
    }

    private boolean playerIsInBlacklisted(Player p) {
        final var wName = p.getWorld().getName();
        return epBlackList.contains(wName);
    }

    private EbiPowerStore store() {
        return EbiPowerStore.getInstance();
    }

    private int getMobDropBonus(ItemStack stack) {
        if (stack == null) return 0;
        return stack.getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS);
    }

    private int getBlockDropBonus(ItemStack stack) {
        if (stack == null) return 0;
        return stack.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);
    }

    private final HashSet<String> epBlackList = new HashSet<>();

    private final HashSet<Material> crops = new HashSet<>();
    private final HashSet<Material> breakBonusList = new HashSet<>();
    private final HashSet<Material> placeBonusList = new HashSet<>();

    private static final int HARVEST_POWER_MULTIPLIER = 1;
    private static final int LOGIN_BONUS_POWER = 50;
    public static final int BREAK_BLOCK_BONUS_LIMIT = 4000;

    private static final Random random = new Random();
}
