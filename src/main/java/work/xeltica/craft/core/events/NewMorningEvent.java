package work.xeltica.craft.core.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * マイクラ内で朝が来たときに発生するイベント
 * @author Xeltica
 */
@SuppressWarnings("ALL")
public class NewMorningEvent extends Event {
    private static final HandlerList HANDLERS_LIST = new HandlerList();

    public NewMorningEvent(long time) {
        this.time = time;
    }

	@Override
	public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
	}

    public long getTime() {
        return this.time;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    private final long time;
}
