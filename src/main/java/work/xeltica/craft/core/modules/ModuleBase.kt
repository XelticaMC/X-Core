package work.xeltica.craft.core.modules

abstract class ModuleBase {
    /** X-Core が起動した時に実行されます。 */
    open fun onEnable() { }
    /**
     * X-Core が起動し、各サービスの初期化が終わった後に実行されます。
     * サービス間の連携はこちらから。
     */
    open fun onPostEnable() { }

    /** X-Core が停止する時に実行されます。 */
    open fun onDisable() {
        isInitialized = false
    }


    var isInitialized: Boolean = false
        private set
}