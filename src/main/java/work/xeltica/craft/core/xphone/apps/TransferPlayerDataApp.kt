package work.xeltica.craft.core.xphone.apps

import org.bukkit.Material
import org.bukkit.entity.Player
import work.xeltica.craft.core.gui.Gui
import work.xeltica.craft.core.gui.MenuItem
import work.xeltica.craft.core.models.TransferPlayerData

/**
 * プレイヤーデータ引っ越しアプリ
 * @author raink1208
 */
class TransferPlayerDataApp : AppBase() {
    override fun getName(player: Player): String {
        val data = TransferPlayerData.getInstance(player) ?: return "引っ越し"
        return if (data.getType(player) == TransferPlayerData.TransferPlayerType.FROM_PLAYER) {
            "引っ越しを取り消す"
        } else {
            "引っ越し（申請が来ています！）"
        }
    }

    override fun getIcon(player: Player): Material = Material.CHEST_MINECART

    override fun onLaunch(player: Player) {
        val data = TransferPlayerData.getInstance(player)
        val ui = Gui.getInstance()
        if (data == null) {
            openTransferPlayerDataApp(player)
        } else if (data.getType(player) == TransferPlayerData.TransferPlayerType.TO_PLAYER) {
            ui.openMenu(player, "引っ越しを受け入れますか？", listOf(
                MenuItem("引っ越しを受け入れる", { acceptTransferPlayerData(player) }, Material.RED_DYE),
                MenuItem("引っ越しを受け入れない", { cancelTransferPlayerData(player) }, Material.GRAY_DYE),
            ))
        } else {
            cancelTransferPlayerData(player)
        }
    }

    private fun openTransferPlayerDataApp(player: Player) {
        val transferPlayerDataWarning: String = """
            §r引っ越しにより、次の情報が新しいプレイヤーに§l上書き§rされます。元のデータは削除されます。
            > エビパワー
            > ヒント解禁状況
            > 各種設定項目
        """.trimIndent()
        val ui = Gui.getInstance()
            ui.openDialog(player, "§4注意！§r", transferPlayerDataWarning) {
                ui.openTextInput(player, "引っ越し先のアカウント名を入力してください。") { name: String? ->
                    if (name == null) {
                        return@openTextInput
                    }
                    val to = player.server.getPlayer(name)
                    if (to == null) {
                        ui.error(player, "指定したアカウント名「$name」は現在いないようです。名前が合っていることと、そのプレイヤーがサーバーにいることをご確認の上、もう一度お試しください！")
                        return@openTextInput
                    }
                    TransferPlayerData(player, to)
                }
            }
    }

    private fun acceptTransferPlayerData(player: Player) {
        val playerData = TransferPlayerData.getInstance(player)
        val fromName = playerData.from.name
        val ui = Gui.getInstance()
        ui.openDialog(player, "引っ越し", """
                §3${fromName}§rをお使いのアカウントに移行します。
                
                問題がなければ[OK]ボタンを押下して次に進んでください。
            """.trimIndent()
        ) {
            ui.openTextInput(player, "$fromName と入力してください。" ) { name: String ->
                if (!fromName.equals(name.trim { it <= ' ' }, ignoreCase = true)) {
                    ui.error(player, "入力した名前は間違っています。")
                    return@openTextInput
                }
                playerData.process()
            }
        }
    }

    private fun cancelTransferPlayerData(player: Player) {
        TransferPlayerData.getInstance(player).cancel()
    }

    override fun isShiny(player: Player): Boolean = TransferPlayerData.getInstance(player) != null

    override fun isVisible(player: Player): Boolean {
        return player.world.name != "event"
    }
}
