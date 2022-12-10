package work.xeltica.craft.core.modules.ChangeGameMode

import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Player
import work.xeltica.craft.core.xphone.apps.AppBase

class ChangeGameModeApp : AppBase(){
    override fun getName(player: Player): String {
        if(player.getGameMode()==GameMode.CREATIVE) {
            return "サバイバルモードに変更します"
        }else{
            return "クリエイティブモードに変更します"
        }
    }

    override fun getIcon(player: Player): Material {
        if()
        return Material.COBBLESTONE
    }
}