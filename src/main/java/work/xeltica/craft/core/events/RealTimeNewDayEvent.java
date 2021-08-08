package work.xeltica.craft.core.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * 現実時間で次の日になったイベント
 * @author Xeltica
 */
@SuppressWarnings("ALL")
public class RealTimeNewDayEvent extends Event {
    /** コンストラクタ */
    public RealTimeNewDayEvent() {
    }

    @Override
	public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
	}

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    private static final HandlerList HANDLERS_LIST = new HandlerList();
}
