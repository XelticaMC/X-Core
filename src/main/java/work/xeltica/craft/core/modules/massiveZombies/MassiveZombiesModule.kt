package work.xeltica.craft.core.modules.massiveZombies

import org.bukkit.Bukkit
import org.bukkit.EntityEffect
import work.xeltica.craft.core.XCorePlugin.Companion.instance
import work.xeltica.craft.core.utils.Ticks
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.scheduler.BukkitRunnable
import work.xeltica.craft.core.api.ModuleBase

object MassiveZombiesModule :ModuleBase(){
    var zombie
    override fun onEnable() {
        Bukkit.getLogger().info("モジュールが読み込まれました")
    }
    fun spawnZombie(chk: Boolean,player: Player) {
            player.sendMessage("強力なゾンビが召喚されます…")

        object : BukkitRunnable(){
            val dest = player.location.clone()
            override fun run() {
                    player.sendMessage("強力なゾンビが目覚めた！")
                    zombie = player.world.spawnEntity(dest, EntityType.ZOMBIE)
                zombie.

                }
        }.runTaskLater(instance, Ticks.from(10.0).toLong())
    }
@EventHandler
    fun onMobDeath(e: EntityDeathEvent){
        if(e.entityType == zombie){

        }
    }
}