package work.xeltica.craft.core.stores;

public enum PlayerDataKey {
    CAT_MODE("cat"),
    NEWCOMER_TIME("newcomer_time"),
    BEDROCK_ACCEPT_DISCLAIMER("accept_disclaimer"),
    ;

    PlayerDataKey(String physicalKey) {
        this.physicalKey = physicalKey;
    }

    public String getPhysicalKey() {
        return this.physicalKey;
    }

    private String physicalKey;
}
