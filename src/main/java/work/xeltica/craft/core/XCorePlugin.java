package work.xeltica.craft.core;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import net.kyori.adventure.text.Component;
import net.luckperms.api.LuckPerms;
import work.xeltica.craft.core.commands.CommandBase;
import work.xeltica.craft.core.commands.CommandBoat;
import work.xeltica.craft.core.commands.CommandCart;
import work.xeltica.craft.core.commands.CommandCat;
import work.xeltica.craft.core.commands.CommandGiveCustomItem;
import work.xeltica.craft.core.commands.CommandGiveTravelTicket;
import work.xeltica.craft.core.commands.CommandHint;
import work.xeltica.craft.core.commands.CommandHub;
import work.xeltica.craft.core.commands.CommandLive;
import work.xeltica.craft.core.commands.CommandLocalTime;
import work.xeltica.craft.core.commands.CommandOmikuji;
import work.xeltica.craft.core.commands.CommandXCoreGuiEvent;
import work.xeltica.craft.core.commands.CommandXPhone;
import work.xeltica.craft.core.commands.CommandXtp;
import work.xeltica.craft.core.commands.CommandPromo;
import work.xeltica.craft.core.commands.CommandPvp;
import work.xeltica.craft.core.commands.CommandReport;
import work.xeltica.craft.core.commands.CommandRespawn;
import work.xeltica.craft.core.commands.CommandSignEdit;
import work.xeltica.craft.core.gui.Gui;
import work.xeltica.craft.core.handlers.XphoneHandler;
import work.xeltica.craft.core.models.PlayerDataKey;
import work.xeltica.craft.core.handlers.EbiPowerHandler;
import work.xeltica.craft.core.handlers.HubHandler;
import work.xeltica.craft.core.handlers.NewMorningHandler;
import work.xeltica.craft.core.handlers.NightmareHandler;
import work.xeltica.craft.core.handlers.PlayerHandler;
import work.xeltica.craft.core.handlers.VehicleHandler;
import work.xeltica.craft.core.handlers.WakabaHandler;
import work.xeltica.craft.core.handlers.WorldHandler;
import work.xeltica.craft.core.plugins.CitizenTimerCalculator;
import work.xeltica.craft.core.plugins.VaultPlugin;
import work.xeltica.craft.core.runnables.DaylightObserver;
import work.xeltica.craft.core.runnables.NightmareRandomEvent;
import work.xeltica.craft.core.stores.HubStore;
import work.xeltica.craft.core.stores.ItemStore;
import work.xeltica.craft.core.stores.MetaStore;
import work.xeltica.craft.core.stores.OmikujiStore;
import work.xeltica.craft.core.stores.PlayerStore;
import work.xeltica.craft.core.stores.VehicleStore;
import work.xeltica.craft.core.stores.WorldStore;
import work.xeltica.craft.core.utils.Ticks;
import work.xeltica.craft.core.commands.CommandEpShop;
import work.xeltica.craft.core.stores.BossBarStore;
import work.xeltica.craft.core.stores.CloverStore;
import work.xeltica.craft.core.stores.EbiPowerStore;
import work.xeltica.craft.core.stores.HintStore;

/**
 * X-Core のメインクラスであり、構成する要素を初期化・管理しています。
 * @author Xeltica
 */
public class XCorePlugin extends JavaPlugin {
    public static XCorePlugin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        logger = getLogger();
        loadPlugins();
        loadStores();
        loadCommands();
        loadHandlers();

        new DaylightObserver(this).runTaskTimer(this, 0, Ticks.from(1));

        new NightmareRandomEvent(this).runTaskTimer(this, 0, Ticks.from(15));
        // 4tickに1回
        // new FlyingObserver().runTaskTimer(this, 0, 4);

        final var tick = 10;
        new BukkitRunnable(){
            @Override
            public void run() {
                VehicleStore.getInstance().tick(tick);

                var store = PlayerStore.getInstance();
                store.openAll().forEach(record -> {
                    // オフラインなら処理しない
                    if (Bukkit.getPlayer(record.getPlayerId()) == null) return;
                    var time = record.getInt(PlayerDataKey.NEWCOMER_TIME, 0);
                    time -= tick;
                    if (time <= 0) {
                        record.delete(PlayerDataKey.NEWCOMER_TIME, false);
                    } else {
                        record.set(PlayerDataKey.NEWCOMER_TIME, time, false);
                    }
                });
                try {
                    store.save();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskTimer(this, 0, tick);


        calculator = new CitizenTimerCalculator();

        var provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        var luckPerms = provider.getProvider();
        luckPerms.getContextManager().registerCalculator(calculator);

        var meta = MetaStore.getInstance();

        if (MetaStore.getInstance().isUpdated()) {
            Bukkit.getServer()
            .audiences()
            .forEach(a -> {
                var prev = meta.getPreviousVersion();
                if (prev == null) prev = "unknown";
                var current = meta.getCurrentVersion();
                var text = String.format("§aCore Systemが更新されました。%s -> %s", prev, current);
                a.sendMessage(Component.text(text));
                for (var log : meta.getChangeLog()) {
                    a.sendMessage(Component.text("・" + log));                    
                }
            });
        }

        logger.info("Booted XelticaMC Core System.");
    }

    @Override
    public void onDisable() {
        commands.clear();
        Gui.resetInstance();
        unloadPlugins();
        var provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        var luckPerms = provider.getProvider();
        luckPerms.getContextManager().unregisterCalculator(calculator);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        var name = command.getName().toLowerCase();

        var com = commands.get(name);
        if (com == null) return false;

        return com.execute(sender, command, label, args);
    }

    private void loadStores() {
        new OmikujiStore();
        new VehicleStore();
        new PlayerStore();
        new HubStore();
        new WorldStore();
        new ItemStore();
        new CloverStore();
        new EbiPowerStore();
        new HintStore();
        new MetaStore();
        new BossBarStore();
    }

    private void loadCommands() {
        commands.clear();

        commands.put("omikuji", new CommandOmikuji());
        commands.put("respawn", new CommandRespawn());
        commands.put("pvp", new CommandPvp());
        commands.put("signedit", new CommandSignEdit());
        commands.put("givetravelticket", new CommandGiveTravelTicket());
        commands.put("givecustomitem", new CommandGiveCustomItem());
        commands.put("report", new CommandReport());
        commands.put("localtime", new CommandLocalTime());
        commands.put("boat", new CommandBoat());
        commands.put("cart", new CommandCart());
        commands.put("promo", new CommandPromo());
        commands.put("cat", new CommandCat());
        commands.put("hub", new CommandHub());
        commands.put("xtp", new CommandXtp());
        commands.put("epshop", new CommandEpShop());
        commands.put("hint", new CommandHint());
        commands.put("__core_gui_event__", new CommandXCoreGuiEvent());
        commands.put("xphone", new CommandXPhone());
        commands.put("live", new CommandLive());
    }

    private void loadHandlers() {
        var pm = getServer().getPluginManager();

        pm.registerEvents(new NewMorningHandler(), this);
        pm.registerEvents(new PlayerHandler(this), this);
        pm.registerEvents(new VehicleHandler(), this);
        pm.registerEvents(new WakabaHandler(), this);
        pm.registerEvents(new HubHandler(), this);
        pm.registerEvents(new WorldHandler(), this);
        pm.registerEvents(new NightmareHandler(), this);
        pm.registerEvents(new XphoneHandler(), this);
        pm.registerEvents(new EbiPowerHandler(), this);
        pm.registerEvents(Gui.getInstance(), this);
    }

    private void loadPlugins() {
        VaultPlugin.getInstance().onEnable(this);
    }

    private void unloadPlugins() {
        VaultPlugin.getInstance().onDisable(this);
    }

    private Logger logger;
    private final HashMap<String, CommandBase> commands = new HashMap<>();

    private CitizenTimerCalculator calculator;

    private static XCorePlugin instance;
}