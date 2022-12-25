package work.xeltica.craft.core.modules.digitalClock

import org.bukkit.Sound
import org.bukkit.command.Command
import org.bukkit.entity.Player
import work.xeltica.craft.core.api.commands.CommandPlayerOnlyBase
import work.xeltica.craft.core.gui.Gui
import work.xeltica.craft.core.models.SoundPitch

class PlaceDigitalClockCommand : CommandPlayerOnlyBase() {
    override fun execute(player: Player, command: Command, label: String, args: Array<String>): Boolean {
        if (args.isNotEmpty()) {
            if (args[0].lowercase() != "reset") return false
            DigitalClockModule.updateLocation(null)
            player.sendMessage("Deleted the digital clock.")
            return true
        }
        DigitalClockModule.updateLocation(player.location)
        Gui.getInstance().playSound(player, Sound.BLOCK_ANVIL_PLACE, 1f, SoundPitch.F_1)
        player.sendMessage("Placed a digital clock.")
        return true
    }
}