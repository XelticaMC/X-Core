package work.xeltica.craft.core.handlers

import de.tr7zw.nbtapi.NBTEntity
import de.tr7zw.nbtapi.NBTItem
import org.bukkit.*
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerEggThrowEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitRunnable
import work.xeltica.craft.core.XCorePlugin
import work.xeltica.craft.core.gui.Gui
import work.xeltica.craft.core.stores.MobBallStore
import java.util.*
import kotlin.random.Random

class MobBallHandler : Listener {
    @EventHandler
    fun onPlayerThrowMobBall(e: PlayerEggThrowEvent) {
        if (!MobBallStore.getInstance().isMobBall(e.egg.item)) return

        e.isHatching = false
    }

    @EventHandler
    fun onMobBallHitEntity(e: ProjectileHitEvent) {
        val egg = e.entity as? Egg ?: return
        val player = egg.shooter as? Player ?: return
        if (!MobBallStore.getInstance().isMobBall(egg.item)) return
        e.isCancelled = true

        val target = e.hitEntity as? Mob
        if (target == null) {
            egg.world.dropItem(egg.location, egg.item)
            egg.world.playSound(egg.location, Sound.ITEM_SHIELD_BLOCK, SoundCategory.PLAYERS, 1f, 0.5f)
            return
        }
        val ownerId = if (target is Tameable) target.ownerUniqueId else null
        // 飼育可能かつ飼育済みかつ親IDがあり親が自分でなければ弾く
        if (ownerId is UUID && ownerId != player.uniqueId) {
            egg.world.dropItem(egg.location, egg.item)
            egg.world.playSound(egg.location, Sound.ITEM_SHIELD_BLOCK, SoundCategory.PLAYERS, 1f, 0.5f)
            return
        }
        val material = getSpawnEggMaterial(target.type)
        if (material == null) {
            egg.world.dropItem(egg.location, egg.item)
            egg.world.playSound(egg.location, Sound.ITEM_SHIELD_BLOCK, SoundCategory.PLAYERS, 1f, 0.5f)
            return
        }
        if (target.persistentDataContainer.has(NamespacedKey(XCorePlugin.getInstance(), "isCaptured"), PersistentDataType.INTEGER)) {
            egg.world.dropItem(egg.location, egg.item)
            egg.world.playSound(egg.location, Sound.ITEM_SHIELD_BLOCK, SoundCategory.PLAYERS, 1f, 0.5f)
            Gui.getInstance().error(player, "そのモブは既に捕獲されています。人のモブを獲ったら泥棒！")
            return
        }

        egg.remove()

        var i = 1
        val spawnEgg = ItemStack(material, 1)
        val eggNbt = restoreMob(target, spawnEgg)
        val eggEntity = egg.world.dropItem(egg.location, spawnEgg)
        val calculated = MobBallStore.getInstance().calculate(target)
        var isGotcha = false
        eggEntity.setCanMobPickup(false)
        eggEntity.setCanPlayerPickup(false)
        object: BukkitRunnable() {
            override fun run() {
                val particleLoc = eggEntity.location.add(0.0, 0.2, 0.0)
                if (i % 20 == 0) {
                    val randNum = Random.nextInt(100)
                    isGotcha = randNum < calculated
                    i = if (isGotcha) i else 80
                    eggEntity.world.playSound(eggEntity.location, Sound.ENTITY_BLAZE_SHOOT, SoundCategory.PLAYERS, 1f, 1.5f)
                }
                eggEntity.world.spawnParticle(Particle.SMOKE_NORMAL, particleLoc, 1)
                i++
                if (i > 80) {
                    this.cancel()
                    if (isGotcha) {
                        eggEntity.world.spawnParticle(Particle.COMPOSTER, particleLoc, 10)
                        player.playSound(egg.location, Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1f, 0.5f)
                        player.sendMessage("§a§lおめでとう！§r${eggNbt.getString("mobCase")}を捕まえた！")
                        eggEntity.setCanPlayerPickup(true)
                    } else {
                        egg.world.playSound(egg.location, Sound.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 1f, 1f)
                        player.sendMessage("残念！ボールから出てきてしまった…。")
                        eggEntity.remove()
                        val entityTag = eggNbt.getCompound("EntityTag")
                        entityTag.removeKey("Pos")
                        val type = EntityType.fromName(eggNbt.getString("mobType"))!!
                        eggEntity.world.spawnEntity(eggEntity.location, type, CreatureSpawnEvent.SpawnReason.CUSTOM) {
                            NBTEntity(it).mergeCompound(entityTag)
                            it.world.playSound(it.location, Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1f, 1f)
                        }
                    }
                }
            }
        }.runTaskTimer(XCorePlugin.getInstance(), 0, 1L)
    }

    @EventHandler
    fun onReleaseMob(e: PlayerInteractEvent) {
        val item = e.item ?: return
        val block = e.clickedBlock ?: return
        val nbt = NBTItem(item)
        if (!nbt.hasKey("mobCase")) return
        e.isCancelled = true

        if (!nbt.getBoolean("isActive")) {
            return
        }
        val entityTag = nbt.getCompound("EntityTag")
        entityTag.removeKey("Pos")
        nbt.applyNBT(item)
        val type = EntityType.fromName(nbt.getString("mobType")) ?: return
        e.player.world.spawnEntity(block.location, type, CreatureSpawnEvent.SpawnReason.CUSTOM) {
            NBTEntity(it).mergeCompound(entityTag)
            it.teleport(block.location.add(0.0, 1.0, 0.0))
            it.world.playSound(it.location, Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1f, 1f)
            it.persistentDataContainer.set(NamespacedKey(XCorePlugin.getInstance(), "isCaptured"), PersistentDataType.INTEGER, 1)
        }
        nbt.setBoolean("isActive", false)
        nbt.applyNBT(item)
        MobBallStore.getInstance().setMobCaseMeta(item)
        e.isCancelled = true
    }

    @EventHandler
    fun onRestoreMob(e: PlayerInteractEntityEvent) {
        val p = e.player
        val entity = e.rightClicked as? Mob ?: return
        val item = p.inventory.itemInMainHand
        val nbt = NBTItem(item)
        if (!nbt.hasKey("mobCase")) return
        e.isCancelled = true

        if (nbt.getBoolean("isActive")) {
            return
        }

        val entityTag = nbt.getCompound("EntityTag")
        if (!entityTag.getUUID("UUID").equals(entity.uniqueId)) {
            Gui.getInstance().error(p, "そのモブはモブケースに適合していません。")
            return
        }

        restoreMob(entity, item)
    }

    private fun restoreMob(target: Mob, spawnEgg: ItemStack): NBTItem {
        val targetNbt = NBTEntity(target)
        val eggNbt = NBTItem(spawnEgg)
        eggNbt.removeKey("EntityTag")
        val entityTag = eggNbt.addCompound("EntityTag")
        entityTag.mergeCompound(targetNbt)
        entityTag.removeKey("Pos")
        eggNbt.setBoolean("isActive", true)
        eggNbt.setString("mobType", target.type.name)
        eggNbt.setString("mobCase", target.customName ?: target.name)
        eggNbt.applyNBT(spawnEgg)
        MobBallStore.getInstance().setMobCaseMeta(spawnEgg)
        target.remove()
        target.world.playSound(target.location, Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1f, 1f)
        return eggNbt
    }

    private fun getSpawnEggMaterial(type: EntityType): Material? {
        return when (type) {
            EntityType.ELDER_GUARDIAN -> Material.ELDER_GUARDIAN_SPAWN_EGG
            EntityType.WITHER_SKELETON -> Material.WITHER_SKELETON_SPAWN_EGG
            EntityType.STRAY -> Material.STRAY_SPAWN_EGG
            EntityType.HUSK -> Material.HUSK_SPAWN_EGG
            EntityType.ZOMBIE_VILLAGER -> Material.ZOMBIE_VILLAGER_SPAWN_EGG
            EntityType.SKELETON_HORSE -> Material.SKELETON_HORSE_SPAWN_EGG
            EntityType.ZOMBIE_HORSE -> Material.ZOMBIE_VILLAGER_SPAWN_EGG
            EntityType.DONKEY -> Material.DONKEY_SPAWN_EGG
            EntityType.MULE -> Material.MULE_SPAWN_EGG
            EntityType.EVOKER -> Material.EVOKER_SPAWN_EGG
            EntityType.VEX -> Material.VEX_SPAWN_EGG
            EntityType.VINDICATOR -> Material.VINDICATOR_SPAWN_EGG
            EntityType.ILLUSIONER -> Material.VINDICATOR_SPAWN_EGG
            EntityType.CREEPER -> Material.CREEPER_SPAWN_EGG
            EntityType.SKELETON -> Material.SKELETON_SPAWN_EGG
            EntityType.SPIDER -> Material.SPIDER_SPAWN_EGG
            EntityType.ZOMBIE -> Material.ZOMBIE_SPAWN_EGG
            EntityType.SLIME -> Material.SLIME_SPAWN_EGG
            EntityType.GHAST -> Material.GHAST_SPAWN_EGG
            EntityType.ZOMBIFIED_PIGLIN -> Material.ZOMBIFIED_PIGLIN_SPAWN_EGG
            EntityType.ENDERMAN -> Material.ENDERMAN_SPAWN_EGG
            EntityType.CAVE_SPIDER -> Material.CAVE_SPIDER_SPAWN_EGG
            EntityType.SILVERFISH -> Material.SILVERFISH_SPAWN_EGG
            EntityType.BLAZE -> Material.BLAZE_SPAWN_EGG
            EntityType.MAGMA_CUBE -> Material.MAGMA_CUBE_SPAWN_EGG
            EntityType.BAT -> Material.BAT_SPAWN_EGG
            EntityType.WITCH -> Material.WITCH_SPAWN_EGG
            EntityType.ENDERMITE -> Material.ENDERMITE_SPAWN_EGG
            EntityType.GUARDIAN -> Material.GUARDIAN_SPAWN_EGG
            EntityType.SHULKER -> Material.SHULKER_SPAWN_EGG
            EntityType.PIG -> Material.PIG_SPAWN_EGG
            EntityType.SHEEP -> Material.SHEEP_SPAWN_EGG
            EntityType.COW -> Material.COW_SPAWN_EGG
            EntityType.CHICKEN -> Material.CHICKEN_SPAWN_EGG
            EntityType.SQUID -> Material.SQUID_SPAWN_EGG
            EntityType.WOLF -> Material.WOLF_SPAWN_EGG
            EntityType.MUSHROOM_COW -> Material.MOOSHROOM_SPAWN_EGG
            EntityType.OCELOT -> Material.OCELOT_SPAWN_EGG
            EntityType.HORSE -> Material.HORSE_SPAWN_EGG
            EntityType.RABBIT -> Material.RABBIT_SPAWN_EGG
            EntityType.POLAR_BEAR -> Material.POLAR_BEAR_SPAWN_EGG
            EntityType.LLAMA -> Material.LLAMA_SPAWN_EGG
            EntityType.PARROT -> Material.PARROT_SPAWN_EGG
            EntityType.VILLAGER -> Material.VILLAGER_SPAWN_EGG
            EntityType.TURTLE -> Material.TURTLE_SPAWN_EGG
            EntityType.PHANTOM -> Material.PHANTOM_SPAWN_EGG
            EntityType.COD -> Material.COD_SPAWN_EGG
            EntityType.SALMON -> Material.SALMON_SPAWN_EGG
            EntityType.PUFFERFISH -> Material.PUFFERFISH_SPAWN_EGG
            EntityType.TROPICAL_FISH -> Material.TROPICAL_FISH_SPAWN_EGG
            EntityType.DROWNED -> Material.DROWNED_SPAWN_EGG
            EntityType.DOLPHIN -> Material.DOLPHIN_SPAWN_EGG
            EntityType.CAT -> Material.CAT_SPAWN_EGG
            EntityType.PANDA -> Material.PANDA_SPAWN_EGG
            EntityType.PILLAGER -> Material.PILLAGER_SPAWN_EGG
            EntityType.RAVAGER -> Material.RAVAGER_SPAWN_EGG
            EntityType.TRADER_LLAMA -> Material.TRADER_LLAMA_SPAWN_EGG
            EntityType.WANDERING_TRADER -> Material.WANDERING_TRADER_SPAWN_EGG
            EntityType.FOX -> Material.FOX_SPAWN_EGG
            EntityType.BEE -> Material.BEE_SPAWN_EGG
            EntityType.HOGLIN -> Material.HOGLIN_SPAWN_EGG
            EntityType.PIGLIN -> Material.PIGLIN_SPAWN_EGG
            EntityType.STRIDER -> Material.STRIDER_SPAWN_EGG
            EntityType.ZOGLIN -> Material.ZOGLIN_SPAWN_EGG
            EntityType.PIGLIN_BRUTE -> Material.PIGLIN_BRUTE_SPAWN_EGG
            EntityType.AXOLOTL -> Material.AXOLOTL_SPAWN_EGG
            EntityType.GLOW_SQUID -> Material.GLOW_SQUID_SPAWN_EGG
            EntityType.GOAT -> Material.GOAT_SPAWN_EGG
            else -> null
        }
    }
}