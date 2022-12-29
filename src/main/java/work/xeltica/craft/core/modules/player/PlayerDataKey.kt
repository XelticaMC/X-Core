package work.xeltica.craft.core.modules.player

/**
 * プレイヤーごとに保存するデータのキーを定義しています。
 * PlayerStore で使用します。
 * @author Xeltica
 */
object PlayerDataKey {
    val CAT_MODE = "cat"
    val NEWCOMER_TIME = "newcomer_time"
    val BEDROCK_ACCEPT_DISCLAIMER = "accept_disclaimer"
    val FIRST_SPAWN = "first_spawn"
    val LAST_JOINED = "last_joined"
    val GIVEN_PHONE = "given_phone"
    val COUNTER_REGISTER_MODE = "counter_register_mode"
    val COUNTER_REGISTER_NAME = "counter_register_name"
    val COUNTER_REGISTER_IS_DAILY = "counter_register_is_daily"
    val COUNTER_REGISTER_LOCATION = "counter_register_location"
    val PLAYING_COUNTER_ID = "counter_id"
    val PLAYING_COUNTER_TIMESTAMP = "counter_time"
    val PLAYED_COUNTER_COUNT = "counter_count"
    val RECEIVED_LOGIN_BONUS = "login_bonus"
    val RECEIVED_LOGIN_BONUS_SUMMER = "login_bonus_summer"
    val BROKEN_BLOCKS_COUNT = "broken_blocks_count"
    val MOB_DEX = "mob_dex"
}