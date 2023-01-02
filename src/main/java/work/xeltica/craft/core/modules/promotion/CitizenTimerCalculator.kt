package work.xeltica.craft.core.modules.promotion

import net.luckperms.api.context.ContextCalculator
import net.luckperms.api.context.ContextConsumer
import net.luckperms.api.context.ContextSet
import net.luckperms.api.context.ImmutableContextSet
import org.bukkit.entity.Player
import work.xeltica.craft.core.api.playerStore.PlayerStore

/**
 * 市民になるまでの時間を経過しているかどうかの情報を提供するLuckPermsのアドオンクラスです。
 * LuckPermsの設定で、この値がtrueになっているかどうかを、
 * 市民ロールであるかどうかの条件式の一つとするために用いています。
 * @author Xeltica
 */
class CitizenTimerCalculator : ContextCalculator<Player?> {
    override fun calculate(target: Player, contextConsumer: ContextConsumer) {
        val value = if (PlayerStore.open(target).has(PromotionModule.PS_KEY_NEWCOMER_TIME)) "false" else "true"
        contextConsumer.accept(contextKeyCitizenTimerElapsed, value)
    }

    override fun estimatePotentialContexts(): ContextSet {
        val builder = ImmutableContextSet.builder()
        builder.add(contextKeyCitizenTimerElapsed, "false")
        builder.add(contextKeyCitizenTimerElapsed, "true")
        return builder.build()
    }

    private val contextKeyCitizenTimerElapsed = "otanoshimi:citizentimerelapsed"
}