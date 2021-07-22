package work.xeltica.craft.core.handlers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;
import work.xeltica.craft.core.models.PlayerDataKey;
import work.xeltica.craft.core.stores.EbiPowerStore;
import work.xeltica.craft.core.stores.PlayerStore;

public class EbiPowerHandler implements Listener{
    public EbiPowerHandler() {
        epBlackList.add("hub");
        epBlackList.add("hub2");
        epBlackList.add("sandbox");
        epBlackList.add("art");
        epBlackList.add("sandbox2");
        epBlackList.add("pvp");
        epBlackList.add("hub_dev");
        epBlackList.add("world");
        epBlackList.add("world_nether");
        epBlackList.add("world_the_end");
        epBlackList.add("wildarea");
        epBlackList.add("travel_wildarea");
        epBlackList.add("travel_megawild");
        epBlackList.add("travel_maikura_city");
    }

    @EventHandler
    public void onCombat(EntityDamageByEntityEvent e) {
        var victim = e.getEntity();
        if (e.getDamager() instanceof Player killer) {
            if (playerIsInBlacklisted(killer)) return;
            // スポナーは対象外
            if (victim.fromMobSpawner()) return;
            // TODO: TT登録可能にしてその中のキルは対象外にする

            var buff = getDropBonus(killer.getInventory().getItemInMainHand()) * 4;
            
            var power = 3 + (buff > 0 ? random.nextInt(buff) : 0);
            var mes = "モブにダメージを与えた！" + power + "EPを獲得。";

            if ("nightmare2".equals(killer.getWorld().getName())) {
                power *= 2;
                mes = "モブにダメージを与えた！" + power + "EPを獲得。(ナイトメアボーナス)";
            }
            if (power > 0) {
                store().tryGive(killer, power);
                notification(killer, mes);
            }
        }
    }

    @EventHandler
    public void on(PlayerAdvancementDoneEvent e) {
        var p = e.getPlayer();
        if (playerIsInBlacklisted(p)) return;
        store().tryGive(p, ADVANCEMENT_POWER);
        var mes = "進捗達成！" + ADVANCEMENT_POWER + "EPを獲得。";
        notification(p, mes);
    }

    @EventHandler
    public void on(PlayerJoinEvent e) {
        var now = new Date();
        var ps = PlayerStore.getInstance();
        var record = ps.open(e.getPlayer());
        var prev = new Date(record.getLong(PlayerDataKey.LAST_JOINED, now.getTime()));
        if (prev.getYear() != now.getYear() && prev.getMonth() != now.getMonth() && prev.getDay() != now.getDay()) {
            store().tryGive(e.getPlayer(), LOGIN_BONUS_POWER);
            notification(e.getPlayer(), "ログボ達成！" + LOGIN_BONUS_POWER + "EPを獲得。");
        }
        record.set(PlayerDataKey.LAST_JOINED, now.getTime());
    }

    private void notification(Player p, String mes) {
        p.sendActionBar(Component.text(mes));
        // p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1, 2);
    }

    private boolean playerIsInBlacklisted(Player p) {
        var wName = p.getWorld().getName();
        return epBlackList.contains(wName);
    }

    private EbiPowerStore store() {
        return EbiPowerStore.getInstance();
    }

    private int getDropBonus(ItemStack stack) {
        if (stack == null) return 0;
        return stack.getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS);
    }

    private List<String> epBlackList = new ArrayList<>();

    private static final int ADVANCEMENT_POWER = 30;
    private static final int LOGIN_BONUS_POWER = 50;

    private static final Random random = new Random();
}
