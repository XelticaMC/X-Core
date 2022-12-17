package work.xeltica.craft.core.modules.transferGuide.enums

enum class JapaneseColumns(val firstChar: String, val chars: Array<String>) {
    A("あ", arrayOf("あ", "い", "う", "え", "お")),
    KA("か", arrayOf("か", "き", "く", "け", "こ", "が", "ぎ", "ぐ", "げ", "ご")),
    SA("さ", arrayOf("さ", "し", "す", "せ", "そ", "ざ", "じ", "ず", "ぜ", "ぞ")),
    TA("た", arrayOf("た", "ち", "つ", "て", "と", "だ", "ぢ", "づ", "で", "ど")),
    NA("な", arrayOf("な", "に", "ぬ", "ね", "の")),
    HA("は", arrayOf("は", "ひ", "ふ", "へ", "ほ", "ば", "び", "ぶ", "べ", "ぼ", "ぱ", "ぴ", "ぷ", "ぺ", "ぽ")),
    MA("ま", arrayOf("ま", "み", "む", "め", "も")),
    YA("や", arrayOf("や", "ゆ", "よ")),
    RA("ら", arrayOf("ら", "り", "る", "れ", "ろ")),
    WA("わ", arrayOf("わ", "ゐ", "ゑ", "を", "ん")),
}