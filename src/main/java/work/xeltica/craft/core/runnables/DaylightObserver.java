package work.xeltica.craft.core.runnables;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import work.xeltica.craft.core.events.NewMorningEvent;

import java.util.Objects;

/**
 * 朝になったタイミングで NewMorningEvent イベントを発行するための
 * バックグラウンドタスクです。
 * @author Xeltica
 */
public class DaylightObserver extends BukkitRunnable {
    public DaylightObserver(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        final var time = Objects.requireNonNull(plugin.getServer().getWorld("world")).getTime();
        if (time < this.prevTime) {
            // 時間が前よりも小さくなったのであれば、おそらく日をまたいだことになる
            final var event = new NewMorningEvent(time);
            Bukkit.getPluginManager().callEvent(event);
        }
        this.prevTime = time;
    }

    private final Plugin plugin;
    private long prevTime;
}
