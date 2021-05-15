package work.xeltica.craft.otanoshimiplugin.handlers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import work.xeltica.craft.otanoshimiplugin.events.NewMorningEvent;
import work.xeltica.craft.otanoshimiplugin.stores.OmikujiStore;

public class NewMorningHandler implements Listener {
    @EventHandler
    public void onNewMorning(NewMorningEvent e) {
        OmikujiStore.getInstance().reset();
    }
}
