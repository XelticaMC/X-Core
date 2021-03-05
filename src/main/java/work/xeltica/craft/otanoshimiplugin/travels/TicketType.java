package work.xeltica.craft.otanoshimiplugin.travels;

public enum TicketType {
    WILDAREA("ワイルドエリア"),
    MEGAWILD("メガワイルド"),
    MOON("月"),
    ;

    public String getDisplayName() {
        return this.displayName;
    }

    private TicketType(String displayName) {
        this.displayName = displayName;
    }

    private String displayName;
}
