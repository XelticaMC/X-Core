package work.xeltica.craft.core.api.commands

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import java.util.Locale

/**
 * X-Core のコマンドを管理し、呼び出すレジストリです。
 */
object CommandRegistry : CommandExecutor {

    /**
     * システムにコマンド [command] を [name] という名前で登録します。
     */
    fun register(name: String, command: CommandBase) {
        val logger = Bukkit.getLogger()
        if (commandsMap.containsKey(name)) {
            logger.warning("コマンド '${name}' は既に登録されているため、スキップします。")
            return
        }
        val cmd = Bukkit.getPluginCommand(name)
        if (cmd == null) {
            logger.warning("コマンド '$name' は plugin.yml で定義されていないため、スキップします。")
            return
        }
        cmd.setExecutor(this)
        cmd.tabCompleter = command
        commandsMap[name] = command
        logger.info("コマンド '$name' を登録しました。")
    }

    /**
     * コマンド一覧を消去します。
     */
    fun clearMap() {
        commandsMap.clear()
    }

    /**
     * コマンドが実行されるときに呼び出されます。
     */
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val name = command.name.lowercase(Locale.getDefault())
        val com = commandsMap[name] ?: return false
        return com.execute(sender, command, label, args)
    }

    private val commandsMap = HashMap<String, CommandBase>()
}
