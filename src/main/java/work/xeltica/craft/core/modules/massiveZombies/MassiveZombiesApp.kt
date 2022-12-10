package work.xeltica.craft.core.modules.massiveZombies

import org.bukkit.Material
import org.bukkit.entity.Player
import work.xeltica.craft.core.xphone.apps.AppBase

class MassiveZombiesApp : AppBase() {
    var chkSpawn = false;
    override fun getName(player: Player): String {
        return "宝を持った強力なゾンビを召喚"
    }

    override fun getIcon(player: Player): Material {
        return Material.ZOMBIE_HEAD
    }

    override fun onLaunch(player: Player) {
        chkSpawn = MassiveZombiesModule.spawnZombie(chkSpawn)

    }
}