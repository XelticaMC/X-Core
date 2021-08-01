package work.xeltica.craft.core.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * マイクラ内で朝が来たときに発生するイベント
 * @author Xeltica
 */
public class NewMorningEvent extends Event {
    private static final HandlerList HANDLERS_LIST = new HandlerList();

    public NewMorningEvent(long time) {
        this.time = time;
    }

	@Override
	public HandlerList getHandlers() {
        return HANDLERS_LIST;
	}

    public long getTime() {
        return this.time;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    private long time;
}
