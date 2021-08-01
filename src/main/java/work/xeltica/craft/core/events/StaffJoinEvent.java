package work.xeltica.craft.core.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * スタッフがサーバーに参加したときに発生するイベント
 * @author Xeltica
 */
public class StaffJoinEvent extends Event {
	@Override
	public HandlerList getHandlers() {
        return HANDLERS_LIST;
	}

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
    
    private static final HandlerList HANDLERS_LIST = new HandlerList();
}
