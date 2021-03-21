package work.xeltica.craft.otanoshimiplugin.handlers;

import java.util.HashSet;
import java.util.Set;

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.Tag;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;

import work.xeltica.craft.otanoshimiplugin.PlayerFlagsManager;

public class VisitorHandler implements Listener {
    public VisitorHandler() {
        // 右クリックできるブロックのホワイトリスト
        rightInteractWhitelist.add(Material.JUKEBOX);
        rightInteractWhitelist.add(Material.LECTERN);
        rightInteractWhitelist.addAll(Tag.SIGNS.getValues());
        rightInteractWhitelist.addAll(Tag.WALL_SIGNS.getValues());
        rightInteractWhitelist.addAll(Tag.DOORS.getValues());
        rightInteractWhitelist.addAll(Tag.BEDS.getValues());
        rightInteractWhitelist.addAll(Tag.TRAPDOORS.getValues());
        rightInteractWhitelist.addAll(Tag.FENCE_GATES.getValues());

        // 右クリックできるエンティティのホワイトリスト
        rightInteractEntitiesWhitelist.add(EntityType.MINECART);
        rightInteractEntitiesWhitelist.add(EntityType.BOAT);
        // 左クリックできるエンティティのホワイトリスト
        leftInteractEntitiesWhitelist.add(EntityType.MINECART);
        leftInteractEntitiesWhitelist.add(EntityType.BOAT);

        rightItemBlacklist.add(Material.BOW);
        rightItemBlacklist.add(Material.TRIDENT);
        rightItemBlacklist.add(Material.CROSSBOW);
    }

    // 敵が観光客をターゲットにするのを防ぐ
    @EventHandler
    public void onEntityTargetVisitor(EntityTargetLivingEntityEvent e) {
        cancelIfVisitor(e, e.getTarget());
    }

    // 観光客のダメージを防ぐ
    @EventHandler
    public void onVisitorDamage(EntityDamageEvent e) {
        var ent = e.getEntity();
        cancelIfVisitor(e, e.getEntity(), () -> {
            // 奈落であれば初期スポーンに飛ばす
            if (e.getCause() == DamageCause.VOID) {
                var respawn = ((Player)ent).getWorld().getSpawnLocation();
                ent.teleport(respawn);
            }
        });
    }

    // 観光客の空腹ゲージを下げないしなんなら全回復する
    @EventHandler
    public void onVisitorDamage(FoodLevelChangeEvent e) {
        var ent = e.getEntity();
        cancelIfVisitor(e, ent, () -> {
            ent.setFoodLevel(20);
        });
    }

    // 観光客がポーション効果を受けないようにする
    @EventHandler
    public void onVisitorDamage(EntityPotionEffectEvent e) {
        cancelIfVisitor(e, e.getEntity());
    }

    // 観光客がブロックを破壊できないようにする
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        var p = e.getPlayer();
        cancelIfVisitor(e, p, () -> showError(p, "そのブロックを破壊できません"));
    }

    // 観光客がブロックを設置できないようにする
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        var p = e.getPlayer();
        cancelIfVisitor(e, p, () -> showError(p, "そのブロックを設置できません"));
    }

    // 観光客がブロックに干渉できないようにする
    @EventHandler
    public void onVisitorInteract(PlayerInteractEvent e) {
        var a = e.getAction();
        if (a != Action.LEFT_CLICK_BLOCK && a != Action.RIGHT_CLICK_BLOCK) return;
        var type = e.getClickedBlock().getType();
        var whitelist = a == Action.LEFT_CLICK_BLOCK ? leftInteractWhitelist : rightInteractWhitelist;
        
        if (!whitelist.contains(type)) {
            cancelIfVisitor(e, e.getPlayer(), () -> showError(e.getPlayer(), "そのブロックに干渉できません"));
        }
    }

    // 観光客が指定アイテムを使えないようにする
    @EventHandler
    public void onVisitorUseBlacklistedItem(PlayerInteractEvent e) {
        var item = e.getItem();
        if (item == null) return;
        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (rightItemBlacklist.contains(item.getType())) {
                cancelIfVisitor(e, e.getPlayer(), () -> showError(e.getPlayer(), "そのアイテムは使用できません"));
            }
        }
    }

    // 観光客がエンティティに干渉できないようにする
    @EventHandler
    public void onVisitorInteractEntity(PlayerInteractEntityEvent e) {
        var type = e.getRightClicked().getType();
        if (!rightInteractEntitiesWhitelist.contains(type)) {
            cancelIfVisitor(e, e.getPlayer(), () -> showError(e.getPlayer(), "そのエンティティに干渉できません"));
        }
    }

    // 観光客がエンティティを攻撃できないようにする
    @EventHandler
    public void onVisitorAttackEntity(EntityDamageByEntityEvent e) {
        var maybeVisitor = e.getDamager();
        var type = e.getEntityType();
        if (!leftInteractEntitiesWhitelist.contains(type)) {
            cancelIfVisitor(e, maybeVisitor, () -> {
                var p = (Player)maybeVisitor;
                showError(p, "そのエンティティを攻撃できません");
            });
        }
    }

    // 観光客がアイテムを拾えないようにする
    @EventHandler
    public void onVisitorPickupItem(EntityPickupItemEvent e) {
        cancelIfVisitor(e, e.getEntity());
    }

    // 観光客がアイテムを捨てられないようにする
    @EventHandler
    public void onEntityDropItem(PlayerDropItemEvent e) {
        cancelIfVisitor(e, e.getPlayer(), () -> {
            var p = (Player)e.getPlayer();
            showError(p, "アイテムを捨てることはできません");
        });
    }

    // 観光客が経験値オーブを拾えないようにする
    @EventHandler
    public void onVisitorPickupExperience(PlayerPickupExperienceEvent e) {
        cancelIfVisitor(e, e.getPlayer());
    }

    // 観光客が矢・トライデントを拾えないようにする
    @EventHandler
    public void onVisitorPickupItem(PlayerPickupArrowEvent e) {
        cancelIfVisitor(e, e.getPlayer());
    }

    // 観光客であれば指定したイベントを無効化する
    private void cancelIfVisitor(Cancellable e, Entity entity) {
        cancelIfVisitor(e, entity, null);
    }

    // 観光客であれば指定したイベントを無効化する
    // 第３引数を指定すると、観光客だった場合に呼び出される
    private void cancelIfVisitor(Cancellable e, Entity entity, Runnable callback) {
        // キャンセルされていれば何もしない
        if (e.isCancelled()) return;
        if (!(entity instanceof Player))
            return;
        var p = (Player) entity;
        if (isVisitor(p)) {
            e.setCancelled(true);
            if (callback != null) callback.run();
        }
    }

    private void showError(Player p, String action) {
        p.playSound(p.getLocation(), Sound.ITEM_SHIELD_BLOCK, SoundCategory.PLAYERS, 1, 1);
        p.sendActionBar("§c現在「観光モード」のため、" + action + "。§r");
    }

    // 観光客かどうかを取得する
    private boolean isVisitor(Player p) {
        return PlayerFlagsManager.getInstance().getVisitorMode(p);
    }

    private Set<Material> leftInteractWhitelist = new HashSet<>();
    private Set<Material> rightInteractWhitelist = new HashSet<>();

    private Set<EntityType> leftInteractEntitiesWhitelist = new HashSet<>();
    private Set<EntityType> rightInteractEntitiesWhitelist = new HashSet<>();

    private Set<Material> rightItemBlacklist = new HashSet<>();
}
