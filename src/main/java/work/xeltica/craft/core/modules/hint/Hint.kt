package work.xeltica.craft.core.modules.hint

import work.xeltica.craft.core.modules.ebipower.EbiPowerHandler

/**
 * ヒントを定義しています。
 * @author Lutica
 */
enum class Hint constructor(
    val hintName: String,
    val description: String,
    val power: Int = 0,
    val type: HintType = HintType.NORMAL,
) {
    // ようこそ
    WELCOME(
        "XelticaMCへようこそ！",
        "XelticaMCに初めてログインする",
        500
    ),
    GOTO_MAIN(
        "いざ居住区へ！",
        "メインワールドへ行く。メインワールドは建築・生活に適したワールド。採掘目的で地下を掘ってはいけない。",
        100
    ),
    GOTO_LOBBY(
        "ロビーへ戻る",
        "別世界からロビーに移動する",
        100
    ),
    GOTO_WILDAREA(
        "資源を分けてもらおう",
        "ロビーから共有ワールドへ行く。共有ワールドにある資材は、誰でも持っていって良い。余裕ができたら、共有ワールドに資材を提供してあげよう。",
        100
    ),
    GOTO_WILDAREAB(
        "広大な自然へ…",
        "X Phoneのテレポートアプリを用いて、メインワールドから資源ワールドに行く。資源ワールドは、月に1回再生成があるぞ。",
        100
    ),
    GOTO_WILDNETHER(
        "怖い地底世界",
        "共有ワールドの共有拠点から、ネザーに行く。",
        100
    ),
    GOTO_WILDEND(
        "はて？ここは果て？",
        "共有ワールドで共有拠点から、エンドポータルを伝ってエンドへ行く。",
        100
    ),
    GOTO_WILDNETHERB(
        "地獄の果てまで採掘だ！",
        "X Phoneのテレポートアプリを用いて、資源ネザーに行く。資源ネザーは、月に1回再生成があるぞ。",
        100
    ),
    GOTO_WILDENDB(
        "地の果てには何がある？？",
        "X Phoneのテレポートアプリを用いて、資源エンドに行く。資源エンドは、月に1回再生成があるぞ。",
        100
    ),
    GOTO_SANDBOX(
        "好きなだけ実験しよう",
        "サンドボックスへ行く。クリエイティブモードで好きなだけ実験できる。爆発物も思いのママ。あ、鯖は落とすなよ？",
        100
    ),
    GOTO_ART(
        "アーティストへの第一歩",
        "アートワールドへ行く。サンドボックスに近いが、こちらはクリエイティブ建築用ワールド。",
        100
    ),
    GOTO_NIGHTMARE(
        "悪い夢",
        "ナイトメアワールドへ行く。怖い敵がうじゃうじゃ、天候は最悪。だけど貰えるエビパワーは2倍！",
        100
    ),  // 機能
    CAT_MODE(
        "にゃんか猫ににゃった",
        "ネコ語モードをオンにする。チャットの「な」が「にゃ」になる。",
        50
    ),
    BOAT(
        "ボートを出す",
        "/boat コマンドを使うか、X Phoneを用いてボートを出現させる",
        50
    ),
    MINECART(
        "トロッコを出す",
        "/cart コマンドを使うか、X Phoneを用いてトロッコを出現させる",
        50
    ),
    KUSA(
        "草",
        "草ブロックの上で「草」「www」などと発言して、ガチで草を生やす",
        50
    ),
    BE_CITIZEN(
        "晴れて市民になった！",
        "市民へ昇格する。詳しくは「市民システム」アプリを見てね。",
        500
    ),
    EPSHOP(
        "エビパワーストアデビュー",
        "エビパワーストアで商品を購入しよう。",
        200
    ),
    EPEFFECTSHOP(
        "困ったときのお薬屋さん",
        "エビパワードラッグストアで強力なポーション効果を購入しよう。",
        200
    ),
    TWIN_XPHONE(
        "X Phoneデビュー",
        "X Phoneを右クリック/タップで使用するか、 /p コマンドを実行するとゲームメニューが表示される。",
        100
    ),
    COUNTDOWN(
        "カウントダウン",
        "/countdown コマンドは、仲間とかけっこしたり様々なことに使えるカウントダウンを表示できます。",
        100
    ),
    QUICKCHAT_APP(
        "クイックチャット",
        "X Phoneアプリ「クイックチャット」を使って簡単にメッセージを送ろう！",
        100
    ),
    QUICKCHAT(
        "点から始まるコミュニケーション",
        "X Phoneアプリ「クイックチャット」のメニューにかかれている英文字をチャットに打ち込もう。例: .k → こんにちは",
        100
    ),
    GET_BALL(
        "めざせモブマスター！",
        "モブボールを入手する。エビパワーストアで購入できる。",
        200
    ),
    FAILED_TO_CATCH_MOB(
        "いつもいつでもうまくゆくなんて保証はどこにもないけど",
        "体力が有り余っている、貴重である、強い、などの条件を持つモブは捕獲しづらいです。根気強くボールを投げましょう。",
        200
    ),
    SUCCEEDED_TO_CATCH_MOB(
        "モブ、ゲットだぜ！",
        "モブボールをモブに投げると捕獲することができます。捕まえる前に弱らせましょう。なお、自分のペットであれば100%捕獲できます。",
        500
    ),

    // エビパワー稼ぎ
    KILL_MOB_AND_EARN_MONEY(
        "狩人のチカラ",
        "モブを倒して、エビパワーを稼ごう。ただし、飼いならされたモブ、敵対でないこどもモブ、ネコへの攻撃では稼ぐことができない。",
        200
    ),
    HARVEST_AND_EARN_MONEY(
        "農家の一日",
        "完全に成熟した作物を回収すると、1つにつき1EP貰える。こつこつ働こう。",
        200
    ),
    BREED_AND_EARN_MONEY(
        "生命が芽吹く瞬間",
        "動物を繁殖させると、1度に2EP手に入る。新たな生命の誕生を感じよう。",
        200
    ),
    MINERS_NEWBIE(
        "エビパワーマイニング",
        "石や鉱石、土などのブロックを採掘することでエビパワーを稼ぐことができる。さらに、幸運付きツルハシで鉱石などを掘るとボーナスが手に入る。",
        200
    ),
    MINERS_DREAM(
        "マイナーズ・ドリーム",
        "ブロックをひたすら掘り続けて${EbiPowerHandler.BREAK_BLOCK_BONUS_LIMIT}EP稼ごう。",
        2000,
        HintType.CHALLENGE
    ),

    // 暴力反対
    VIOLENCE_CHILD(
        "Don't touch the child!",
        "子どもモブを攻撃するとエビパワーが10減ります。殴っちゃダメよ。",
        10
    ),
    VIOLENCE_PET(
        "動物虐待、ダメ、ゼッタイ。",
        "誰かに飼いならされたモブを攻撃するとエビパワーが10減ります。殴っちゃダメよ。",
        10
    ),
    VIOLENCE_CAT(
        "猫を殴るなんて！！(-100)",
        "ネコちゃんを殴るなんて言語道断ですよね。エビパワーが100減ります。",
        100
    ),

    // おみくじ
    OMIKUJI_TOKUDAIKICHI(
        "運勢「特大吉」",
        "おみくじを引いて、特大吉を当てよう！0.001%の確率だ！特大吉になると、20分間「幸運」ポーション効果がつく上に、5%の確率で死亡時に「不死のトーテム」効果がつくぞ！",
        1000,
        HintType.CHALLENGE
    ),
    OMIKUJI_DAIKYOU(
        "運勢「大凶」",
        "おみくじを引いて、大凶を当てよう！0.001%の確率だ！大凶になると、「不運」「不吉の予感」「毒」効果がついてしまう…。",
        1000,
        HintType.CHALLENGE
    ),

    // 億万長者
    EBIPOWER_1000000(
        "100万あったら何したい？",
        "100万EPを溜める",
        0,  // 100万いった瞬間にボーナスがはいっちゃうとちょっと余韻がないので…
        HintType.CHALLENGE
    ),
    EBIPOWER_5000000(
        "ファイブ・ミリオン",
        "500万EPを溜める",
        0,  // 同上
        HintType.CHALLENGE
    ),
    EBIPOWER_10000000(
        "名実ともに億万長者。",
        "1000万EPを溜める",
        0,  // 同上
        HintType.CHALLENGE
    ),

    // イベント限定
    TWO_YEARS_EVENT_NO_MISS(
        "コードレス・パルクールの達人",
        "（2周年記念イベント限定ヒント）チェックポイントを置かずにパルクールを制覇する。",
        50000,
        HintType.EVENT
    ),

    // イベント実績
    TWO_YEARS_EVENT_PARKOUR_JOINED(
        "2周年記念イベントパルクール参加",
        "パルクールに参加する",
        1000,
        HintType.EVENT
    ),

    TWO_YEARS_EVENT_PARKOUR_1ST(
        "2周年記念イベントパルクール・1位",
        "パルクールで1位を獲得する！",
        5000,
        HintType.EVENT
    ),

    TWO_YEARS_EVENT_PARKOUR_2ND(
        "2周年記念イベントパルクール・2位",
        "パルクールで2位を獲得する！",
        3000,
        HintType.EVENT
    ),

    TWO_YEARS_EVENT_PARKOUR_3RD(
        "2周年記念イベントパルクール・3位",
        "パルクールで3位を獲得する！",
        1000,
        HintType.EVENT
    ),

    // ヘルプ
    EBIPOWER(
        "エビパワーとは",
        "エビパワーは、この世界で遊ぶほど貯まるポイントです。戦闘、採掘、ヒント達成、ログインボーナスなどで貰え、アイテム購入、飛行、おみくじなど様々なことに使えます。クラシックワールドでは貯まりません。",
        0,
        HintType.HELP
    ),
    WEB(
        "公式サイト",
        "公式サイトには様々な情報が書かれています。規約も書いてあるので、一度読んでください。\n\nXelticaMCで検索。",
        0,
        HintType.HELP
    ),
    WHAT_TO_DO(
        "やることがない",
        "やることに困ったら、「XelticaMCの歩き方」で検索しよう。本サーバーの遊び方がわかるぞ。",
        0,
        HintType.HELP
    ),
    TRANSFER(
        "アカウント移行したい",
        "もしアカウントを新しく作って、エビパワーなどを新しいアカウントに引き継ぎたいなと思ったら、X Phoneの[引っ越し]アプリを使いましょう。",
        0,
        HintType.HELP
    );

    enum class HintType {
        NORMAL, CHALLENGE, HELP, EVENT,
    }
}