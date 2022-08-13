package work.xeltica.craft.core.stores;

import org.bukkit.Material;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import work.xeltica.craft.core.XCorePlugin;
import work.xeltica.craft.core.api.Config;

public class MobEPStore {
    public MobEPStore() {
        instance = this;
        XCorePlugin.getInstance().saveResource("mobEP.yml", false);
        config = new Config("mobEP");
    }

    public static MobEPStore getInstance() { return instance; }

    public int getMobDropEP(Entity entity, EntityDeathEvent event) {
        final var conf = config.getConf();
        if (entity.getType() == EntityType.ENDERMAN) {
            if (event.getDrops().stream().map(ItemStack::getType).toList().contains(Material.ENDER_PEARL)) {
                return conf.getInt("pearl_enderman");
            }
        }
        if (entity.getType() == EntityType.CREEPER) {
            if (((Creeper) entity).isPowered()) {
                return conf.getInt("charged_creeper");
            }
        }
        if (conf.contains(entity.getType().name().toLowerCase())) {
            return conf.getInt(entity.getType().name().toLowerCase());
        }

        if (entity instanceof Monster) {
            return conf.getInt("other_enemy");
        } else {
            return conf.getInt("friendly_mob");
        }
    }

    private static MobEPStore instance;
    private static Config config;
}
