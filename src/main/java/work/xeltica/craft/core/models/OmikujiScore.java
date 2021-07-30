package work.xeltica.craft.core.models;

public enum OmikujiScore {
    Tokudaikichi("特大吉"),
    Daikichi("大吉"),
    Kichi("吉"),
    Chukichi("中吉"),
    Shokichi("小吉"),
    Kyou("凶"),
    Daikyou("大凶"),
    None("無し"),
    ;

    OmikujiScore(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    private String displayName;
}