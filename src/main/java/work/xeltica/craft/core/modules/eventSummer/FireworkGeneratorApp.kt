package work.xeltica.craft.core.modules.eventSummer

import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.entity.EntityType
import org.bukkit.entity.Firework
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.FireworkMeta
import org.bukkit.scheduler.BukkitRunnable
import work.xeltica.craft.core.XCorePlugin
import work.xeltica.craft.core.gui.Gui
import work.xeltica.craft.core.gui.MenuItem
import work.xeltica.craft.core.xphone.apps.AppBase
import javax.annotation.Nonnull

/**
 * 夏祭りスタッフ用花火作成アプリ
 * @author raink1208
 */
class FireworkGeneratorApp : AppBase() {
    override fun getName(player: Player): String = "花火を打ち上げる"

    override fun getIcon(player: Player): Material = Material.FIREWORK_STAR

    override fun onLaunch(player: Player) {
        openFireworkLaunchApp(player)
    }

    private fun openFireworkLaunchApp(player: Player) {
        val list = FireworkType.values()
            .map { f ->
                MenuItem(f.typeName, { chooseFireworkColor(player, f.type) }, f.material)
            }
            .toList()

        Gui.getInstance().openMenu(player, "花火の形状を選んでください", list)
    }

    private fun chooseFireworkColor(player: Player, type: FireworkEffect.Type) {
        val list = FireworkColor.values()
            .map { f ->
                MenuItem(f.colorName, { chooseFireworkColor2(player, type, f.color) }, f.material)
            }
            .toList()
        Gui.getInstance().openMenu(player, "花火の色を選んでください", list)
    }

    private fun chooseFireworkColor2(player: Player, type: FireworkEffect.Type, color: Color) {
        val list = FireworkColor.values()
            .map { f ->
                MenuItem(f.colorName, {
                    chooseFireworkPower(player, type, color, f.color)
                }, f.material)
            }
            .toList()
        val list2 = ArrayList(list)
        list2.add(0, MenuItem("なし", { chooseFireworkPower(player, type, color, null) }, Material.BARRIER))
        Gui.getInstance().openMenu(player, "花火のフェード色を選んでください", list2)
    }

    private fun chooseFireworkPower(player: Player, type: FireworkEffect.Type, color: Color, color2: Color?) {
        Gui.getInstance().openMenu(
            player, "花火の飛翔時間を選んでください", listOf(
                MenuItem("1", {
                    chooseFireworkAttributes(player, type, color, color2, 1, null)
                }, Material.REDSTONE_TORCH),
                MenuItem("2", {
                    chooseFireworkAttributes(player, type, color, color2, 2, null)
                }, Material.REPEATER),
                MenuItem("3", {
                    chooseFireworkAttributes(player, type, color, color2, 3, null)
                }, Material.COMPARATOR)
            )
        )
    }

    private fun chooseFireworkAttributes(
        player: Player,
        type: FireworkEffect.Type,
        color: Color,
        color2: Color?,
        power: Int,
        attribute: FireworkAttribute?,
    ) {
        val attr = attribute ?: FireworkAttribute()
        val ui = Gui.getInstance()
        ui.openMenu(
            player, "花火の属性を選んでください", listOf(
                MenuItem("点滅エフェクト (現在: " + (if (attr.flicker) "オン" else "オフ") + ")", {
                    attr.flicker = attr.flicker xor true
                    chooseFireworkAttributes(player, type, color, color2, power, attr)
                }, ui.getIconOfFlag(attr.flicker), null, attr.flicker),
                MenuItem("軌跡エフェクト (現在: " + (if (attr.trail) "オン" else "オフ") + ")", {
                    attr.trail = attr.trail xor true
                    chooseFireworkAttributes(player, type, color, color2, power, attr)
                }, ui.getIconOfFlag(attr.trail), null, attr.trail),
                MenuItem("射出", { spawnFirework(player, type, color, color2, power, attr) }, Material.DISPENSER),
                MenuItem("連射", {
                    val runnable: BukkitRunnable = object : BukkitRunnable() {
                        var count = 0
                        override fun run() {
                            spawnFirework(player, type, color, color2, power, attr)
                            if (count > 4 * 5) {
                                cancel()
                            }
                            count++
                        }
                    }
                    runnable.runTaskTimer(XCorePlugin.instance, 0, 4)
                }, Material.COMPARATOR),
                MenuItem("ダウンロード", { downloadFirework(player, type, color, color2, power, attr) }, Material.FIREWORK_ROCKET)
            )
        )
    }

    private fun spawnFirework(player: Player, type: FireworkEffect.Type, color: Color, color2: Color?, power: Int, attribute: FireworkAttribute) {
        val entity = player.world.spawnEntity(player.location, EntityType.FIREWORK)
        if (entity is Firework) {
            val meta = entity.fireworkMeta
            meta.power = power
            meta.addEffect(getEffect(type, color, color2, attribute))
            entity.fireworkMeta = meta
        }
    }

    private fun downloadFirework(player: Player, type: FireworkEffect.Type, color: Color, color2: Color?, power: Int, attribute: FireworkAttribute) {
        val stack = ItemStack(Material.FIREWORK_ROCKET)
        stack.editMeta {
            if (it is FireworkMeta) {
                it.power = power
                it.addEffect(getEffect(type, color, color2, attribute))
            }
        }
        player.inventory.addItem(stack)
        player.playSound(player.location, Sound.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 1f, 1f)
        player.sendMessage("花火をダウンロードしました")
    }

    private fun getEffect(type: FireworkEffect.Type, color: Color, color2: Color?, @Nonnull attribute: FireworkAttribute): FireworkEffect {
        val effect = FireworkEffect.builder()
            .with(type)
            .withColor(color)
            .flicker(attribute.flicker)
            .trail(attribute.trail)
        if (color2 != null) {
            effect.withFade(color2)
        }
        return effect.build()
    }

    internal enum class FireworkType(
        val type: FireworkEffect.Type,
        val material: Material,
        val typeName: String,
    ) {
        SMALL(FireworkEffect.Type.BALL, Material.FIRE_CHARGE, "小さい花火"),
        LARGE(FireworkEffect.Type.BALL_LARGE, Material.FIRE_CHARGE, "大きい花火"),
        STAR(FireworkEffect.Type.STAR, Material.NETHER_STAR, "星型"),
        BURST(FireworkEffect.Type.BURST, Material.TNT, "爆発型"),
        CREEPER(FireworkEffect.Type.CREEPER, Material.CREEPER_HEAD, "クリーパー型");
    }

    internal enum class FireworkColor(
        val material: Material,
        val color: Color,
        val colorName: String,
    ) {
        ORANGE(Material.ORANGE_WOOL, Color.ORANGE, "オレンジ"),
        FUCHSIA(Material.MAGENTA_WOOL, Color.FUCHSIA, "マゼンタ"),
        LIGHTBLUE(Material.LIGHT_BLUE_WOOL, Color.AQUA, "ライトブルー"),
        YELLOW(Material.YELLOW_WOOL, Color.YELLOW, "イエロー"),
        LIME(Material.LIME_WOOL, Color.LIME, "ライム"),

        // PINK(Material.PINK_WOOL, Color., "ピンク"),
        GRAY(Material.GRAY_WOOL, Color.GRAY, "グレー"),
        LIGHTGRAY(Material.LIGHT_GRAY_WOOL, Color.SILVER, "ライトグレー"),
        CYAN(Material.CYAN_WOOL, Color.TEAL, "シアン"),
        PURPLE(Material.PURPLE_WOOL, Color.PURPLE, "パープル"),
        BLUE(Material.BLUE_WOOL, Color.BLUE, "ブルー"),
        BROWN(Material.BROWN_WOOL, Color.MAROON, "ブラウン"),
        GREEN(Material.GREEN_WOOL, Color.GREEN, "グリーン"),
        RED(Material.RED_WOOL, Color.RED, "レッド"),
        BLACK(Material.BLACK_WOOL, Color.BLACK, "ブラック");
    }

    internal class FireworkAttribute {
        var flicker = false
        var trail = false
    }
}