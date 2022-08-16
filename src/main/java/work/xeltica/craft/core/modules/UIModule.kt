package work.xeltica.craft.core.modules

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import work.xeltica.craft.core.modules.CustomItemModule.getPlayerHead
import kotlin.jvm.JvmOverloads
import work.xeltica.craft.core.models.DialogEventArgs
import work.xeltica.craft.core.models.SoundPitch
import work.xeltica.craft.core.XCorePlugin
import java.lang.Runnable
import org.geysermc.cumulus.SimpleForm
import org.geysermc.floodgate.api.FloodgateApi
import net.wesjd.anvilgui.AnvilGUI
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerEditBookEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BookMeta
import org.geysermc.connector.common.ChatColor
import org.geysermc.cumulus.CustomForm
import work.xeltica.craft.core.models.MenuItem
import java.util.*
import java.util.function.Consumer
import java.util.function.Predicate

/**
 * UI システムのメインクラス。
 * メニューやダイアログといったプレイヤーにUIを表示し、入力を受け取るAPIを持ちます。
 * @author Xeltica
 */
class UIModule : Listener {
    /**
     * メニューを開きます。
     * @param player メニューを開くプレイヤー
     * @param title メニューのタイトル
     * @param items メニューのアイテム
     */
    fun openMenu(player: Player, title: String?, vararg items: MenuItem) {
        openMenu(player, title, listOf(*items))
    }

    /**
     * メニューを開きます。
     * @param player メニューを開くプレイヤー
     * @param title メニューのタイトル
     * @param items メニューのアイテム
     */
    fun openMenu(player: Player, title: String?, items: Collection<MenuItem>) {
        if (isBedrock(player)) {
            openMenuBedrockImpl(player, title, items.toTypedArray())
        } else {
            openMenuJavaImpl(player, title, items.toTypedArray())
        }
    }
    /**
     * ダイアログを開きます。
     * @param player ダイアログを開くプレイヤー
     * @param title ダイアログのタイトル
     * @param content ダイアログに記載する文字列
     * @param callback UIダイアログのボタンを押したときに発火するイベント
     * @param okButtonText OKボタンのテキスト
     */
    @JvmOverloads
    fun openDialog(
        player: Player,
        title: String,
        content: String,
        callback: Consumer<DialogEventArgs>? = null,
        okButtonText: String? = null
    ) {
        val okText = okButtonText ?: "OK"
        if (isBedrock(player)) {
            openDialogBedrockImpl(player, title, content, callback, okText)
        } else {
            openDialogJavaImpl(player, title, content, callback, okText)
        }
    }

    /**
     * 現在参加中のプレイヤーを選択するメニューを開きます。
     * @param player メニューを開くプレイヤー
     * @param onSelect 選択肢に入るプレイヤーを指定
     */
    fun openPlayersMenu(player: Player, onSelect: Consumer<Player?>?) {
        openPlayersMenu(player, "プレイヤーを選んでください", onSelect)
    }
    /**
     * 現在参加中のプレイヤーを選択するメニューを開きます。
     * @param player メニューを開くプレイヤー
     * @param onSelect 選択肢に入るプレイヤーを指定
     * @param title メニューのタイトルを指定
     * @param filter プレイヤーのフィルターを指定
     */
    @JvmOverloads
    fun openPlayersMenu(
        player: Player,
        title: String?,
        onSelect: Consumer<Player?>?,
        filter: Predicate<Player>? = null
    ) {
        var stream = Bukkit.getOnlinePlayers().stream()
        if (filter != null) {
            stream = stream.filter(filter)
        }
        val list = stream.map { p: Player ->
            val head = getPlayerHead(p)
            p.displayName()
            val name = PlainTextComponentSerializer.plainText().serialize(p.displayName())
            MenuItem(name, { a: MenuItem? -> onSelect?.accept(p) }, head, p)
        }
            .toList()
        openMenu(player, title, list)
    }

    /**
     * Java Editionにてボタンを押下したときに実行される内部コマンドの処理を行います。
     * 直接呼び出さないこと。
     * @param id ID
     */
    fun handleCommand(id: String) {
        if (!bookHandlersMap.containsKey(id)) return
        val t = bookHandlersMap[id]
        t!!.handler.accept(t.eventArgs)
        bookHandlersMap.remove(id)
    }

    /**
     * ブーリアン値に対応するアイコンを取得します
     * @param flag アイコンとなる値
     * @return 対応するアイコン
     */
    fun getIconOfFlag(flag: Boolean): Material {
        return if (flag) Material.LIME_DYE else Material.GRAY_DYE
    }

    /**
     * エラーをプレイヤーに表示します。
     * @param p エラーを表示させるプレイヤー
     * @param message エラー内容
     * @return 常にtrue。コマンドの返り値に使うことを想定。
     */
    fun error(p: Player, message: String?): Boolean {
        p.sendMessage(message!!)
        p.playSound(p.location, Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1f, 0.5f)
        return true
    }

    /**
     * JavaでインベントリをメニューUIとして使うため、そのハンドリングを行います。
     * @param e ハンドリングに使用するイベント
     */
    @EventHandler
    fun onInventoryClick(e: InventoryClickEvent) {
        val inv = e.inventory
        val p = e.whoClicked

        // 管理インベントリでなければ無視
        if (!invMap.containsKey(inv)) return
        e.isCancelled = true
        val menuItems = invMap[inv]!!
        val id = e.rawSlot
        if (menuItems.size <= id || id < 0) return
        p.closeInventory()
        val handler = menuItems[id].onClick
        handler?.accept(menuItems[id])
    }

    /**
     * JavaでインベントリをメニューUIとして使うため、そのハンドリングを行います。
     * @param e ハンドリングに使用するイベント
     */
    @EventHandler
    fun onInventoryClose(e: InventoryCloseEvent) {
        val inv = e.inventory

        // 管理インベントリでなければ無視
        if (!invMap.containsKey(inv)) return

        // GC
        invMap.remove(inv)
    }

    /**
     * Javaで本をダイアログUIとして使うため、そのハンドリングを行います。
     * @param e ハンドリングするイベントを指定
     */
    @EventHandler
    fun onPlayerEditBook(e: PlayerEditBookEvent) {
        if (bookSet.contains(e.previousBookMeta)) {
            e.isCancelled = true
            e.player.sendMessage("てすてすてすとですとよ")
            Bukkit.getLogger().info("テスト")
        }
    }

    /**
     * 指定したプレイヤーの位置でサウンドを再生します。
     * @param player プレイヤー
     * @param sound 効果音
     * @param volume ボリューム
     * @param pitch ピッチ
     */
    fun playSound(player: Player, sound: Sound?, volume: Float, pitch: SoundPitch) {
        player.world.playSound(player.location, sound!!, SoundCategory.PLAYERS, volume, pitch.pitch)
    }

    /**
     * 指定したプレイヤーの位置で指定Tick後にサウンドを再生します。
     * @param player プレイヤー
     * @param sound 効果音
     * @param volume ボリューム
     * @param pitch ピッチ
     * @param delay Tick
     */
    fun playSoundAfter(player: Player, sound: Sound?, volume: Float, pitch: SoundPitch, delay: Int) {
        Bukkit.getScheduler()
            .runTaskLater(XCorePlugin.instance, Runnable { playSound(player, sound, volume, pitch) }, delay.toLong())
    }

    /**
     * 指定したプレイヤーにのみサウンドを再生します。
     * @param player プレイヤー
     * @param sound 効果音
     * @param volume ボリューム
     * @param pitch ピッチ
     */
    fun playSoundLocally(player: Player, sound: Sound?, volume: Float, pitch: SoundPitch) {
        player.playSound(player.location, sound!!, SoundCategory.PLAYERS, volume, pitch.pitch)
    }

    /**
     * 指定したプレイヤーにのみ指定Tick後にサウンドを再生します。
     * @param player プレイヤー
     * @param sound 効果音
     * @param volume ボリューム
     * @param pitch ピッチ
     * @param delay Tick
     */
    fun playSoundLocallyAfter(player: Player, sound: Sound?, volume: Float, pitch: SoundPitch, delay: Int) {
        Bukkit.getScheduler().runTaskLater(
            XCorePlugin.instance,
            Runnable { playSoundLocally(player, sound, volume, pitch) },
            delay.toLong()
        )
    }

    private fun openMenuJavaImpl(player: Player, title: String?, items: Array<MenuItem>) {
        val inv = Bukkit.createInventory(
            null, (1 + items.size / 9) * 9, Component.text(
                title!!
            )
        )
        Arrays.stream(items).map { i: MenuItem ->
            val item = i.icon
            if (i.isShiny) {
                item.addUnsafeEnchantment(Enchantment.DURABILITY, 1)
            }
            val meta = item.itemMeta
            meta.displayName(Component.text(i.name!!))
            item.itemMeta = meta
            item
        }.forEach { items: ItemStack? ->
            inv.addItem(
                items!!
            )
        }
        invMap[inv] = items
        player.openInventory(inv)
    }

    private fun openMenuBedrockImpl(player: Player, title: String?, items: Array<MenuItem>) {
        val builder = SimpleForm.builder()
            .title(title!!)
        for (item in items) {
            var text = item.name
            if (item.isShiny) {
                text = ChatColor.DARK_BLUE + text
            }
            builder.button(text!!)
        }
        builder.responseHandler { form: SimpleForm, data: String? ->
            val res = form.parseResponse(data)
            if (!res.isCorrect) {
                return@responseHandler
            }
            val id = res.clickedButtonId
            val callback = items[id].onClick
            callback?.accept(items[id])
        }
        val fPlayer = FloodgateApi.getInstance().getPlayer(player.uniqueId)
        fPlayer.sendForm(builder)
    }

    private fun openDialogJavaImpl(
        player: Player,
        title: String,
        content: String,
        callback: Consumer<DialogEventArgs>?,
        okButtonText: String
    ) {
        val book = ItemStack(Material.WRITTEN_BOOK)
        val meta = book.itemMeta as BookMeta
        val handleString = UUID.randomUUID().toString().replace("-", "")
        val comTitle = Component.text("$title\n\n")
        val comContent = Component.text("$content\n\n")
        val comOkButton = Component.text(okButtonText, Style.style(TextColor.color(0, 0, 0), TextDecoration.BOLD, TextDecoration.UNDERLINED))
            .clickEvent(ClickEvent.runCommand("/__core_gui_event__ $handleString"))
        val component = comTitle.append(comContent).append(comOkButton)
        meta.addPages(component)
        meta.author = "XelticaMC"
        meta.title = title
        book.itemMeta = meta
        bookSet.add(meta)
        player.openBook(book)
        if (callback != null) {
            bookHandlersMap[handleString] = HandlerTuple(callback, DialogEventArgs(player), meta)
        }
    }

    private fun openDialogBedrockImpl(
        player: Player, title: String, content: String,
        callback: Consumer<DialogEventArgs>?, okButtonText: String
    ) {
        val api = FloodgateApi.getInstance()
        val form = SimpleForm.builder()
            .title(title)
            .content(content)
            .button(okButtonText)
            .responseHandler { _ -> callback?.accept(DialogEventArgs(player)) }
        api.getPlayer(player.uniqueId).sendForm(form)
    }

    fun openTextInput(player: Player, title: String, responseHandler: Consumer<String?>) {
        if (isBedrock(player)) {
            openTextInputBedrockImpl(player, title, responseHandler)
        } else {
            openTextInputJavaImpl(player, title, responseHandler)
        }
    }

    private fun openTextInputJavaImpl(player: Player, title: String, responseHandler: Consumer<String?>) {
        AnvilGUI.Builder().title(title).onComplete { p: Player?, text: String? ->
            responseHandler.accept(text)
            AnvilGUI.Response.close()
        }.itemLeft(ItemStack(Material.GRAY_STAINED_GLASS_PANE)).plugin(XCorePlugin.instance).open(player)
    }

    private fun openTextInputBedrockImpl(player: Player, title: String, responseHandler: Consumer<String?>) {
        val fPlayer = FloodgateApi.getInstance().getPlayer(player.uniqueId)
        val form = CustomForm.builder()
            .title(title)
            .input("")
            .responseHandler { form: CustomForm, res: String? ->
                responseHandler.accept(
                    form.parseResponse(res).getInput(0)
                )
            }
            .build()
        fPlayer.sendForm(form)
    }

    private val invMap = HashMap<Inventory, Array<MenuItem>>()
    private val bookHandlersMap = HashMap<String, HandlerTuple>()
    private val bookSet = HashSet<BookMeta>()

    internal class HandlerTuple(
        var handler: Consumer<DialogEventArgs>,
        var eventArgs: DialogEventArgs,
        var meta: BookMeta
    )

    companion object {
        /**
         * インスタンスを取得します。
         * @return インスタンス
         */
        @JvmStatic
        fun getInstance(): UIModule {
            return if (instance == null) UIModule().also { instance = it } else instance!!
        }

        /**
         * 内部的に使用するものです。
         */
        fun resetInstance() {
            instance = null
        }

        private fun isBedrock(player: Player): Boolean {
            return FloodgateApi.getInstance().isFloodgateId(player.uniqueId)
        }

        private var instance: UIModule? = null
    }
}