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
import org.jetbrains.annotations.NotNull;
import work.xeltica.craft.core.commands.CommandBase;
import work.xeltica.craft.core.commands.CommandBoat;
import work.xeltica.craft.core.commands.CommandCart;
import work.xeltica.craft.core.commands.CommandCat;
import work.xeltica.craft.core.commands.CommandCountdown;
import work.xeltica.craft.core.commands.CommandCounter;
import work.xeltica.craft.core.commands.CommandGiveCustomItem;
import work.xeltica.craft.core.commands.CommandGiveTravelTicket;
import work.xeltica.craft.core.commands.CommandHint;
import work.xeltica.craft.core.commands.CommandHub;
import work.xeltica.craft.core.commands.CommandLive;
import work.xeltica.craft.core.commands.CommandLocalTime;
import work.xeltica.craft.core.commands.CommandNickName;
import work.xeltica.craft.core.commands.CommandOmikuji;
import work.xeltica.craft.core.commands.CommandXCoreGuiEvent;
import work.xeltica.craft.core.commands.CommandXPhone;
import work.xeltica.craft.core.commands.CommandXtp;
import work.xeltica.craft.core.commands.CommandPromo;
import work.xeltica.craft.core.commands.CommandPvp;
import work.xeltica.craft.core.commands.CommandRanking;
import work.xeltica.craft.core.commands.CommandReport;
import work.xeltica.craft.core.commands.CommandRespawn;
import work.xeltica.craft.core.commands.CommandSignEdit;
import work.xeltica.craft.core.gui.Gui;
import work.xeltica.craft.core.handlers.LiveModeHandler;
import work.xeltica.craft.core.handlers.LoginBonusHandler;
import work.xeltica.craft.core.handlers.NbsHandler;
import work.xeltica.craft.core.handlers.PlayerTntHandler;
import work.xeltica.craft.core.handlers.MiscHandler;
import work.xeltica.craft.core.handlers.XphoneHandler;
import work.xeltica.craft.core.models.PlayerDataKey;
import work.xeltica.craft.core.handlers.CounterHandler;
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
import work.xeltica.craft.core.runnables.RealTimeObserver;
import work.xeltica.craft.core.stores.HubStore;
import work.xeltica.craft.core.stores.ItemStore;
import work.xeltica.craft.core.stores.MetaStore;
import work.xeltica.craft.core.stores.NbsStore;
import work.xeltica.craft.core.stores.NickNameStore;
import work.xeltica.craft.core.stores.OmikujiStore;
import work.xeltica.craft.core.stores.PlayerStore;
import work.xeltica.craft.core.stores.QuickChatStore;
import work.xeltica.craft.core.stores.RankingStore;
import work.xeltica.craft.core.stores.VehicleStore;
import work.xeltica.craft.core.stores.WorldStore;
import work.xeltica.craft.core.utils.Ticks;
import work.xeltica.craft.core.commands.CommandEpShop;
import work.xeltica.craft.core.stores.BossBarStore;
import work.xeltica.craft.core.stores.CloverStore;
import work.xeltica.craft.core.stores.CounterStore;
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

        // new FlyingObserver().runTaskTimer(this, 0, 4);

        new RealTimeObserver().runTaskTimer(this, 0, Ticks.from(1));

        final var tick = 10;
        new BukkitRunnable(){
            @Override
            public void run() {
                VehicleStore.getInstance().tick(tick);

                final var store = PlayerStore.getInstance();
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

        final var provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider == null) {
            Bukkit.getLogger().severe("X-CoreはLuckPermsを必要とします。X-Coreを終了します。");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        final var luckPerms = provider.getProvider();
        luckPerms.getContextManager().registerCalculator(calculator);

        final var meta = MetaStore.getInstance();

        if (MetaStore.getInstance().isUpdated()) {
            Bukkit.getServer()
            .audiences()
            .forEach(a -> {
                var prev = meta.getPreviousVersion();
                if (prev == null) prev = "unknown";
                final var current = meta.getCurrentVersion();
                final var text = String.format("§aコアシステムを更新しました。%s -> %s", prev, current);
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
        NbsStore.getInstance().stopAll();
        final var provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            final var luckPerms = provider.getProvider();
            luckPerms.getContextManager().unregisterCalculator(calculator);
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String label, String[] args) {
        final var name = command.getName().toLowerCase();

        final var com = commands.get(name);
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
        new NickNameStore();
        new CounterStore();
        new RankingStore();
        new NbsStore();
        new QuickChatStore();
    }

    private void loadCommands() {
        commands.clear();

        addCommand("omikuji", new CommandOmikuji());
        addCommand("respawn", new CommandRespawn());
        addCommand("pvp", new CommandPvp());
        addCommand("signedit", new CommandSignEdit());
        addCommand("givetravelticket", new CommandGiveTravelTicket());
        addCommand("givecustomitem", new CommandGiveCustomItem());
        addCommand("report", new CommandReport());
        addCommand("localtime", new CommandLocalTime());
        addCommand("boat", new CommandBoat());
        addCommand("cart", new CommandCart());
        addCommand("promo", new CommandPromo());
        addCommand("cat", new CommandCat());
        addCommand("hub", new CommandHub());
        addCommand("xtp", new CommandXtp());
        addCommand("epshop", new CommandEpShop());
        addCommand("hint", new CommandHint());
        addCommand("__core_gui_event__", new CommandXCoreGuiEvent());
        addCommand("xphone", new CommandXPhone());
        addCommand("live", new CommandLive());
        addCommand("nick", new CommandNickName());
        addCommand("counter", new CommandCounter());
        addCommand("ranking", new CommandRanking());
        addCommand("countdown", new CommandCountdown());
    }

    private void loadHandlers() {
        final var pm = getServer().getPluginManager();

        pm.registerEvents(new NewMorningHandler(), this);
        logger.info("Loaded NewMorningHandler");
        pm.registerEvents(new PlayerHandler(this), this);
        logger.info("Loaded PlayerHandler");
        pm.registerEvents(new VehicleHandler(), this);
        logger.info("Loaded VehicleHandler");
        pm.registerEvents(new WakabaHandler(), this);
        logger.info("Loaded WakabaHandler");
        pm.registerEvents(new HubHandler(), this);
        logger.info("Loaded HubHandler");
        pm.registerEvents(new WorldHandler(), this);
        logger.info("Loaded WorldHandler");
        pm.registerEvents(new NightmareHandler(), this);
        logger.info("Loaded NightmareHandler");
        pm.registerEvents(new XphoneHandler(), this);
        logger.info("Loaded XphoneHandler");
        pm.registerEvents(new EbiPowerHandler(), this);
        logger.info("Loaded EbiPowerHandler");
        pm.registerEvents(new LiveModeHandler(), this);
        logger.info("Loaded LiveModeHandler");
        pm.registerEvents(new CounterHandler(), this);
        logger.info("Loaded CounterHandler");
        pm.registerEvents(new NbsHandler(), this);
        logger.info("Loaded NbsHandler");
        pm.registerEvents(new PlayerTntHandler(), this);
        logger.info("Loaded PlayTntHandler");
        pm.registerEvents(new MiscHandler(), this);
        logger.info("Loaded MiscHandler");
        pm.registerEvents(new LoginBonusHandler(), this);
        logger.info("Loaded LoginBonusHandler");
        pm.registerEvents(Gui.getInstance(), this);
        logger.info("Loaded Gui");
    }

    private void loadPlugins() {
        VaultPlugin.getInstance().onEnable(this);
    }

    private void unloadPlugins() {
        VaultPlugin.getInstance().onDisable(this);
    }

    /**
     * コマンドをコアシステムに登録します。
     * @param commandName コマンド名
     * @param command コマンドのインスタンス
     */
    private void addCommand(String commandName, CommandBase command) {
        commands.put(commandName, command);
        final var cmd = getCommand(commandName);
        if (cmd == null) {
            logger.warning("Command " + commandName + " is not defined at the plugin.yml");
            return;
        }
        cmd.setTabCompleter(command);
        logger.info("Command " + commandName + " is registered");
    }

    private Logger logger;
    private final HashMap<String, CommandBase> commands = new HashMap<>();

    private CitizenTimerCalculator calculator;

    private static XCorePlugin instance;
}
