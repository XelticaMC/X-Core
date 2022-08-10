package work.xeltica.craft.core.plugins

import net.luckperms.api.context.ContextCalculator
import net.luckperms.api.context.ContextConsumer
import work.xeltica.craft.core.models.PlayerDataKey
import net.luckperms.api.context.ContextSet
import net.luckperms.api.context.ImmutableContextSet
import org.bukkit.entity.Player
import work.xeltica.craft.core.stores.PlayerStore

/**
 * 市民になるまでの時間を経過しているかどうかの情報を提供するLuckPermsのアドオンクラスです。
 * LuckPermsの設定で、この値がtrueになっているかどうかを、
 * 市民ロールであるかどうかの条件式の一つとするために用いています。
 * @author Xeltica
 */
class CitizenTimerCalculator : ContextCalculator<Player> {
    override fun calculate(target: Player, contextConsumer: ContextConsumer) {
        contextConsumer.accept(KEY, if (PlayerStore.instance.open(target).has(PlayerDataKey.NEWCOMER_TIME)) "false" else "true")
    }

    override fun estimatePotentialContexts(): ContextSet {
        val builder = ImmutableContextSet.builder()
        builder.add(KEY, "false")
        builder.add(KEY, "true")
        return builder.build()
    }

    companion object {
        private const val KEY = "otanoshimi:citizentimerelapsed"
    }
}