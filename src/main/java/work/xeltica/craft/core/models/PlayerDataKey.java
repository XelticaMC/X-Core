package work.xeltica.craft.core.models;

import lombok.Getter;

/**
 * プレイヤーごとに保存するデータのキーを定義しています。
 * PlayerStore で使用します。
 * @author Xeltica
 */
public enum PlayerDataKey {
    CAT_MODE("cat"),
    NEWCOMER_TIME("newcomer_time"),
    BEDROCK_ACCEPT_DISCLAIMER("accept_disclaimer"),
    FIRST_SPAWN("first_spawn"),
    LAST_JOINED("last_joined"),
    GIVEN_PHONE("given_phone"),
    COUNTER_REGISTER_MODE("counter_register_mode"),
    COUNTER_REGISTER_NAME("counter_register_name"),
    COUNTER_REGISTER_IS_DAILY("counter_register_is_daily"),
    COUNTER_REGISTER_LOCATION("counter_register_location"),
    PLAYING_COUNTER_ID("counter_id"),
    PLAYING_COUNTER_TIMESTAMP("counter_time"),
    PLAYED_COUNTER("played_counter"),
    RECEIVED_LOGIN_BONUS("login_bonus"),
    RECEIVED_LOGIN_BONUS_SUMMER("login_bonus_summer"),

    ;

    PlayerDataKey(String physicalKey) {
        this.physicalKey = physicalKey;
    }

    @Getter
    private final String physicalKey;
}
