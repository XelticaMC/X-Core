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

    ;

    PlayerDataKey(String physicalKey) {
        this.physicalKey = physicalKey;
    }

    @Getter
    private final String physicalKey;
}
