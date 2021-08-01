package work.xeltica.craft.core.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * スタッフがサーバーから退出したときに発生するイベント
 * @author Xeltica
 */
public class StaffLeaveEvent extends Event {
	@Override
	public HandlerList getHandlers() {
        return HANDLERS_LIST;
	}

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    private static final HandlerList HANDLERS_LIST = new HandlerList();
}
