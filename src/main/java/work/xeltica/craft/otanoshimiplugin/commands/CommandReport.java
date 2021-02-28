package work.xeltica.craft.otanoshimiplugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import work.xeltica.craft.otanoshimiplugin.gui.Gui;
import work.xeltica.craft.otanoshimiplugin.gui.MenuItem;

public class CommandReport extends CommandPlayerOnlyBase {
    @Override
    public boolean execute(Player reporter, Command command, String label, String[] args) {
        if (args.length != 1) {
            return false;
        }
        var playerName = args[0];
        var reportee = Bukkit.getOfflinePlayer(Bukkit.getPlayerUniqueId(playerName));
        if (reportee == null) {
            reporter.sendMessage("そのような名前のプレイヤーはこのサーバーにはいないようです。");
            return true;
        }
        var ui = Gui.getInstance();
        ui.openMenu(reporter, "処罰の種類を選んでください。"
            , new MenuItem("BAN", (m) -> reporter.sendMessage("BANされる"), Material.BARRIER)
            , new MenuItem("警告", (m) -> reporter.sendMessage("警告される"), Material.BELL)
            , new MenuItem("キック", (m) -> reporter.sendMessage("キックされる"), Material.RABBIT_FOOT)
            , new MenuItem("ミュート", (m) -> reporter.sendMessage("ミュートされる"), Material.MUSIC_DISC_11)
        );
        return true;
    }
}
