package work.xeltica.craft.otanoshimiplugin;

import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import work.xeltica.craft.otanoshimiplugin.commands.CommandBase;
import work.xeltica.craft.otanoshimiplugin.commands.CommandOmikuji;
import work.xeltica.craft.otanoshimiplugin.commands.CommandRespawn;
import work.xeltica.craft.otanoshimiplugin.handlers.EntityHandler;
import work.xeltica.craft.otanoshimiplugin.handlers.NewMorningHandler;
import work.xeltica.craft.otanoshimiplugin.handlers.PlayerHandler;
import work.xeltica.craft.otanoshimiplugin.plugins.VaultPlugin;
import work.xeltica.craft.otanoshimiplugin.runnables.DaylightObserver;
import work.xeltica.craft.otanoshimiplugin.runnables.NightmareRandomEvent;

public class OtanoshimiPlugin extends JavaPlugin {
    public static OtanoshimiPlugin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        logger = getLogger();
        loadStores();
        loadCommands();
        loadHandlers();
        loadPlugins();
        instance = this;

        // 1秒に1回
        new DaylightObserver(this).runTaskTimer(this, 0, 20);
        // 1分に1回
        new NightmareRandomEvent(this).runTaskTimer(this, 0, 20 * 60);

        logger.info("Initialized XelticaMC Otanoshimi Plugin! Have fun!");
    }

    @Override
    public void onDisable() {
        commands.clear();
        unloadPlugins();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        var name = command.getName().toLowerCase();

        var com = commands.get(name);
        if (com == null) return false;

        return com.execute(sender, command, label, args);
    }

    private void loadStores() {
        new OmikujiStore(this);
        logger.info("Loaded Omikuji store");
    }

    private void loadCommands() {
        commands.put("omikuji", new CommandOmikuji());
        logger.info("Loaded /omikuji command");
        commands.put("respawn", new CommandRespawn());
        logger.info("Loaded /respawn command");
    }

    private void loadHandlers() {
        var pm = getServer().getPluginManager();
        pm.registerEvents(new NewMorningHandler(), this);
        logger.info("Loaded NewMorningHandler");
        pm.registerEvents(new PlayerHandler(this), this);
        logger.info("Loaded PlayerHandler");
        pm.registerEvents(new EntityHandler(), this);
        logger.info("Loaded EntityHandler");
    }

    private void loadPlugins() {
        VaultPlugin.getInstance().onEnable(this);
    }

    private void unloadPlugins() {
        VaultPlugin.getInstance().onDisable(this);
    }

    private Logger logger;
    private final HashMap<String, CommandBase> commands = new HashMap<>();

    private static OtanoshimiPlugin instance;
}