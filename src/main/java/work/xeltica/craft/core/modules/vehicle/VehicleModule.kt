package work.xeltica.craft.core.modules.vehicle

import com.destroystokyo.paper.block.TargetBlockInfo
import org.bukkit.*
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Rail
import org.bukkit.block.data.type.RedstoneRail
import org.bukkit.entity.Boat
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.Vehicle
import org.bukkit.entity.minecart.RideableMinecart
import org.bukkit.event.entity.CreatureSpawnEvent
import work.xeltica.craft.core.XCorePlugin
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.gui.Gui
import work.xeltica.craft.core.modules.hint.Hint
import work.xeltica.craft.core.modules.hint.HintModule
import work.xeltica.craft.core.modules.world.WorldModule
import work.xeltica.craft.core.utils.Config

object VehicleModule: ModuleBase() {
    private lateinit var config: Config

    private val collisionArea = listOf(
        BlockFace.EAST.direction.multiply(0.7),
        BlockFace.WEST.direction.multiply(0.7),
        BlockFace.SOUTH.direction.multiply(0.7),
        BlockFace.NORTH.direction.multiply(0.7)
    )

    private val offsets = listOf(
        BlockFace.NORTH.direction.multiply(0.35),
        BlockFace.NORTH_EAST.direction.multiply(0.35),
        BlockFace.EAST.direction.multiply(0.35),
        BlockFace.SOUTH_EAST.direction.multiply(0.35),
        BlockFace.SOUTH.direction.multiply(0.35),
        BlockFace.SOUTH_WEST.direction.multiply(0.35),
        BlockFace.WEST.direction.multiply(0.35),
        BlockFace.NORTH_WEST.direction.multiply(0.35)
    )

    override fun onEnable() {
        config = Config("vehicles")
        config.useAutoSave = true

        registerHandler(VehicleHandler())
        registerCommand("boat", CommandBoat())
        registerCommand("cart", CommandCart())
        VehicleObserver().runTaskTimer(XCorePlugin.instance, 0L, 1L)
    }

    fun getVehicleIds(): Set<String> {
        return config.conf.getKeys(false)
    }

    fun getTick(vehicleId: String): Int {
        return config.conf.getInt(vehicleId)
    }

    fun setTick(vehicleId: String, tick: Int) {
        config.conf.set(vehicleId, tick)
    }

    fun registerVehicle(vehicle: Vehicle) {
        if (!isValidVehicle(vehicle)) return

        val id = vehicle.uniqueId.toString()

        //初期値を登録
        config.conf.set(id, 20 * 60 * 5)
    }

    fun unregisterVehicle(vehicle: Vehicle) {
        if (!isValidVehicle(vehicle)) return

        val id = vehicle.uniqueId.toString()

        unregisterVehicle(id)
    }

    fun isValidVehicle(v: Vehicle?): Boolean {
        return v is Boat || v is RideableMinecart
    }

    fun trySummonCart(player: Player): Boolean {
        if (!WorldModule.canSummonVehicles(player.world)) {
            return Gui.getInstance().error(player, "§cここには召喚できないようだ…。")
        }
        val lookBlock = player.getTargetBlock(5) ?: return true
        if (lookBlock.type.data == Rail::class.java || lookBlock.type.data == RedstoneRail::class.java) {
            val spawnLoc = lookBlock.location.add(0.0, 0.5, 0.0)
            spawnLoc.world.spawnEntity(spawnLoc, EntityType.MINECART, CreatureSpawnEvent.SpawnReason.CUSTOM)
            player.sendMessage("トロッコを召喚した。")
            player.playSound(player.location, Sound.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 1f, 2f)
            HintModule.achieve(player, Hint.MINECART)
        } else {
            return Gui.getInstance().error(player, "ここには召喚できない")
        }

        return true
    }

    fun trySummonBoat(player: Player): Boolean {
        if (!WorldModule.canSummonVehicles(player.world)) {
            return Gui.getInstance().error(player, "§cここには召喚できないようだ…。")
        }
        var spawnLoc = player.location
        val lookBlock = player.getTargetBlock(5, TargetBlockInfo.FluidMode.ALWAYS)
        if (lookBlock != null && lookBlock.type != Material.AIR) {
            val lookFace = player.getTargetBlockFace(5, TargetBlockInfo.FluidMode.ALWAYS)
            spawnLoc = lookBlock.location
            if (lookFace != null) {
                spawnLoc.add(lookFace.direction)
                spawnLoc.add(0.5, 0.0, 0.5)
            }
        }
        if (collidesAt(spawnLoc.clone())) {
            var flag = false
            for (offset in offsets) {
                if (!collidesAt(spawnLoc.clone().add(offset))) {
                    spawnLoc = spawnLoc.add(offset)
                    flag = true
                    break
                }
            }
            if (!flag) return Gui.getInstance().error(player, "狭すぎて置けない…")
        }
        val boat = spawnLoc.world.spawnEntity(spawnLoc, EntityType.BOAT, CreatureSpawnEvent.SpawnReason.CUSTOM)
        boat.setRotation(player.location.yaw, boat.location.pitch)
        player.sendMessage("ボートを足元に召喚した。")
        player.playSound(player.location, Sound.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 1f, 2f)
        HintModule.achieve(player, Hint.BOAT)
        return true
    }

    fun unregisterVehicle(id: String) {
        config.conf.set(id, null)
    }

    private fun collidesAt(loc: Location): Boolean {
        if (loc.block.isSolid) return true
        for (vec in collisionArea) {
            if (loc.clone().add(vec).block.isSolid) return true
        }
        return false
    }
}