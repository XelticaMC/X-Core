package work.xeltica.craft.core.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import work.xeltica.craft.core.models.CounterData;

/**
 * プレイヤーが時間計測を開始したイベント
 * @author Xeltica
 */
@SuppressWarnings("ALL")
public class PlayerCounterStart extends Event {
    /**
     * コンストラクタ
     * @param player カウントを停止したプレイヤー
     * @param counter 対象のカウントデータ
     */
    public PlayerCounterStart(Player player, CounterData counter) {
        this.player = player;
        this.counter = counter;
    }

    @Override
	public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
	}

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    private CounterData counter;

    private Player player;

    public CounterData getCounter() {
        return this.counter;
    }

    public Player getPlayer() {
        return this.player;
    }
}
