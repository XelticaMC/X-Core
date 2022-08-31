package work.xeltica.craft.core.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import work.xeltica.craft.core.modules.counter.CounterData;

/**
 * プレイヤーが時間計測を停止したイベント
 * @author Xeltica
 */
@SuppressWarnings("ALL")
public class PlayerCounterFinish extends Event {

    /**
     * コンストラクタ
     * @param player カウントを停止したプレイヤー
     * @param counter 対象のカウントデータ
     * @param time カウント停止時間(ミリ秒)
     */
    public PlayerCounterFinish(Player player, CounterData counter, long time) {
        this.player = player;
        this.counter = counter;
        this.time = time;
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

    private long time;

    public CounterData getCounter() {
        return this.counter;
    }

    public Player getPlayer() {
        return this.player;
    }

    public long getTime() {
        return this.time;
    }
}
