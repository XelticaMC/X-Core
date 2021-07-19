package work.xeltica.craft.core.stores;

public enum PlayerDataKey {
    CAT_MODE("cat"),
    NEWCOMER_TIME("newcomer_time"),
    ;

    PlayerDataKey(String physicalKey) {
        this.physicalKey = physicalKey;
    }

    public String getPhysicalKey() {
        return this.physicalKey;
    }

    private String physicalKey;
}
