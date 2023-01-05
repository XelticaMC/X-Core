package work.xeltica.craft.core.modules.vehicle

import org.bukkit.FluidCollisionMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.SoundCategory
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
import work.xeltica.craft.core.api.Config
import work.xeltica.craft.core.api.ModuleBase
import work.xeltica.craft.core.gui.Gui
import work.xeltica.craft.core.modules.hint.Hint
import work.xeltica.craft.core.modules.hint.HintModule
import work.xeltica.craft.core.modules.world.WorldModule

/**
 * トロッコやボートの射出機能、放置された乗り物の自動削除機能などを提供するモジュールです。
 */
object VehicleModule : ModuleBase() {
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

    /**
     * 乗り物のID一覧を取得します。
     */
    fun getVehicleIds(): Set<String> {
        return config.conf.getKeys(false)
    }

    /**
     * [vehicleId] の寿命を取得します。
     */
    fun getTick(vehicleId: String): Int {
        return config.conf.getInt(vehicleId)
    }

    /**
     * [vehicleId] の寿命を設定します。
     */
    fun setTick(vehicleId: String, tick: Int) {
        config.conf.set(vehicleId, tick)
    }

    /**
     * [vehicle] を管理対象に追加します。
     */
    fun registerVehicle(vehicle: Vehicle) {
        if (!isValidVehicle(vehicle)) return

        val id = vehicle.uniqueId.toString()

        //初期値を登録
        config.conf.set(id, 20 * 60 * 5)
    }

    /**
     * [vehicle] を管理対象から外します。
     */
    fun unregisterVehicle(vehicle: Vehicle) {
        if (!isValidVehicle(vehicle)) return

        val id = vehicle.uniqueId.toString()

        unregisterVehicle(id)
    }

    /**
     * [id] に対応する乗り物を管理対象から外します。
     */
    fun unregisterVehicle(id: String) {
        config.conf.set(id, null)
    }

    /**
     * この乗り物が管理対象であるかどうかを取得します。
     */
    fun isValidVehicle(v: Vehicle?): Boolean {
        return v is Boat || v is RideableMinecart
    }

    /**
     * トロッコの射出を試みます。
     */
    fun trySummonCart(player: Player): Boolean {
        if (!WorldModule.getWorldInfo(player.world).allowVehicleSpawn) {
            return Gui.getInstance().error(player, "§cここには召喚できないようだ…。")
        }
        val lookBlock = player.getTargetBlockExact(5) ?: return true
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

    /**
     * ボートの射出を試みます。
     */
    fun trySummonBoat(player: Player): Boolean {
        if (!WorldModule.getWorldInfo(player.world).allowVehicleSpawn) {
            return Gui.getInstance().error(player, "§cここには召喚できないようだ…。")
        }
        var spawnLoc = player.location
        val lookBlock = player.getTargetBlockExact(5, FluidCollisionMode.ALWAYS)
        if (lookBlock != null && lookBlock.type != Material.AIR) {
            val lookFace = player.getTargetBlockFace(5, FluidCollisionMode.ALWAYS)
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

    private fun collidesAt(loc: Location): Boolean {
        if (loc.block.isSolid) return true
        for (vec in collisionArea) {
            if (loc.clone().add(vec).block.isSolid) return true
        }
        return false
    }
}