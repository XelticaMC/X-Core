package work.xeltica.craft.core.modules.transferPlayerData

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import work.xeltica.craft.core.gui.Gui
import work.xeltica.craft.core.gui.MenuItem
import work.xeltica.craft.core.modules.transferPlayerData.TransferPlayerDataModule.cancel
import work.xeltica.craft.core.modules.transferPlayerData.TransferPlayerDataModule.getTransferPendingData
import work.xeltica.craft.core.modules.transferPlayerData.TransferPlayerDataModule.getTransferState
import work.xeltica.craft.core.modules.transferPlayerData.TransferPlayerDataModule.process
import work.xeltica.craft.core.modules.xphone.AppBase

/**
 * プレイヤーデータ引っ越しアプリ
 * @author raink1208
 */
class TransferPlayerDataApp : AppBase() {

    override fun getName(player: Player): String {
        return when (player.getTransferState()) {
            TransferState.NONE -> "引っ越し"
            TransferState.FROM -> "引っ越しを取り消す"
            TransferState.TO -> "引っ越し（申請が来ています！）"
        }
    }

    override fun getIcon(player: Player): Material = Material.CHEST_MINECART

    override fun onLaunch(player: Player) {
        val ui = Gui.getInstance()
        when (player.getTransferState()) {
            // 引っ越し待機中でなければメニューを開く
            TransferState.NONE -> openTransferPlayerDataApp(player)
            // 引っ越し待機中のプレイヤーならキャンセルする
            TransferState.FROM -> cancelTransferPlayerData(player)
            // 引っ越し先のプレイヤーなら確認メッセージを出す
            TransferState.TO -> {
                ui.openMenu(
                    player, "引っ越しを受け入れますか？", listOf(
                        MenuItem("引っ越しを受け入れる", { acceptTransferPlayerData(player) }, Material.RED_DYE),
                        MenuItem("引っ越しを受け入れない", { cancelTransferPlayerData(player) }, Material.GRAY_DYE),
                    )
                )
            }
        }
    }

    private fun openTransferPlayerDataApp(player: Player) {
        val ui = Gui.getInstance()
        ui.openDialog(player, "§4注意！§r", TransferPlayerDataModule.WARNING_MESSAGE) {
            ui.openTextInput(player, "引っ越し先のアカウント名を入力してください。") {
                if (it.isEmpty()) return@openTextInput
                val to = Bukkit.getPlayer(it)
                if (to == null) {
                    ui.error(player, "指定したアカウント名「$it」は現在いないようです。名前が合っていることと、そのプレイヤーがサーバーにいることをご確認の上、もう一度お試しください！")
                    return@openTextInput
                }
                TransferPlayerDataModule.requestTransfer(player, to)
            }
        }
    }

    private fun acceptTransferPlayerData(player: Player) {
        if (player.getTransferState() != TransferState.TO) throw IllegalArgumentException()
        val pendingData = player.getTransferPendingData() ?: throw IllegalArgumentException()
        val fromName = pendingData.from.name
        val ui = Gui.getInstance()
        ui.openDialog(
            player, "引っ越し", """
                §3${fromName}§rをお使いのアカウントに移行します。
                
                問題がなければ[OK]ボタンを押下して次に進んでください。
            """.trimIndent()
        ) {
            ui.openTextInput(player, "$fromName と入力してください。") { name: String ->
                if (fromName.lowercase() != name.trim().lowercase()) {
                    ui.error(player, "入力した名前は間違っています。")
                    return@openTextInput
                }
                pendingData.process()
            }
        }
    }

    private fun cancelTransferPlayerData(player: Player) {
        if (player.getTransferState() != TransferState.TO) throw IllegalArgumentException()
        val pendingData = player.getTransferPendingData() ?: throw IllegalArgumentException()
        pendingData.cancel()
    }

    override fun isShiny(player: Player): Boolean {
        return player.getTransferState() !== TransferState.NONE
    }

    override fun isVisible(player: Player): Boolean {
        return player.world.name != "event"
    }
}
