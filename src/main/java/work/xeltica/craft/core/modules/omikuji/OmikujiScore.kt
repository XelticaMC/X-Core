package work.xeltica.craft.core.modules.omikuji

enum class OmikujiScore(val displayName: String) {
    TOKUDAIKICHI("特大吉"),
    DAIKICHI("大吉"),
    KICHI("吉"),
    CHUKICHI("中吉"),
    SHOKICHI("小吉"),
    KYOU("凶"),
    DAIKYOU("大凶"),
    NONE("無し");

    companion object {
        fun getByDisplayName(name: String): OmikujiScore {
            return values().firstOrNull { it.displayName == name } ?: throw IllegalArgumentException()
        }
    }
}