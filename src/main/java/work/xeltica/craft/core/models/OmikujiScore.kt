package work.xeltica.craft.core.models

/**
 * おみくじの結果を定義しています。
 * @author Xeltica
 */
enum class OmikujiScore(val displayName: String) {
    /** 特大吉 */
    Tokudaikichi("特大吉"),
    /** 大吉 */
    Daikichi("大吉"),
    /** 吉 */
    Kichi("吉"),
    /** 中吉 */
    Chukichi("中吉"),
    /** 小吉 */
    Shokichi("小吉"),
    /** 凶 */
    Kyou("凶"),
    /** 大凶 */
    Daikyou("大凶"),
    /** 無し */
    None("無し"),
    ;
}