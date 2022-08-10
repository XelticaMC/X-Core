package work.xeltica.craft.core.api.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * スタッフがサーバーから退出したときに発生するイベント
 * @author Xeltica
 */
@SuppressWarnings("ALL")
public class StaffLeaveEvent extends Event {
	@Override
	public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
	}

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    private static final HandlerList HANDLERS_LIST = new HandlerList();
}
