package work.xeltica.craft.core.models;

public enum PlayerDataKey {
    CAT_MODE("cat"),
    NEWCOMER_TIME("newcomer_time"),
    BEDROCK_ACCEPT_DISCLAIMER("accept_disclaimer"),
    FIRST_SPAWN("first_spawn"),
    LAST_JOINED("last_joined"),
    ;

    PlayerDataKey(String physicalKey) {
        this.physicalKey = physicalKey;
    }

    public String getPhysicalKey() {
        return this.physicalKey;
    }

    private String physicalKey;
}
