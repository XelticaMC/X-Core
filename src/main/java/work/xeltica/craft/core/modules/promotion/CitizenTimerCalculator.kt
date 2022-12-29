package work.xeltica.craft.core.modules.promotion;

import org.bukkit.entity.Player;

import net.luckperms.api.context.ContextCalculator;
import net.luckperms.api.context.ContextConsumer;
import net.luckperms.api.context.ContextSet;
import net.luckperms.api.context.ImmutableContextSet;
import org.jetbrains.annotations.NotNull;
import work.xeltica.craft.core.modules.player.PlayerDataKey;
import work.xeltica.craft.core.modules.player.PlayerModule;

/**
 * 市民になるまでの時間を経過しているかどうかの情報を提供するLuckPermsのアドオンクラスです。
 * LuckPermsの設定で、この値がtrueになっているかどうかを、
 * 市民ロールであるかどうかの条件式の一つとするために用いています。
 * @author Xeltica
 */
public class CitizenTimerCalculator implements ContextCalculator<Player> {

    @Override
    public void calculate(@NotNull Player target, ContextConsumer contextConsumer) {
        contextConsumer.accept(KEY, PlayerModule.INSTANCE.open(target).has(PlayerDataKey.NEWCOMER_TIME) ? "false" : "true");
    }

    @Override
    public @NotNull ContextSet estimatePotentialContexts() {
        final ImmutableContextSet.Builder builder = ImmutableContextSet.builder();
        builder.add(KEY, "false");
        builder.add(KEY, "true");
        return builder.build();
    }

    private static final String KEY = "otanoshimi:citizentimerelapsed";

}
