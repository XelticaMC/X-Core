package work.xeltica.craft.core.models;

/**
 * ヒントを定義しています。
 * @author Xeltica
 */
public enum Hint {
    WELCOME("XelticaMCへようこそ！", "XelticaMCに初めてログインする", 500),
    GOTO_MAIN("いざ居住区へ！", "メインワールドへ行く。メインワールドは建築・生活に適したワールド。採掘目的で地下を掘ってはいけない。", 100),
    GOTO_LOBBY("ロビーへ戻る", "別世界からロビーに移動する", 100),
    GOTO_WILDAREA("資源調達に行くぞ！", "ロビーからワイルドエリアへ行く。ワイルドエリアは普通のサバイバル・資源採取に自由に使えるワールド。", 100),
    GOTO_WILDNETHER("怖い地底世界", "ワイルドエリアでネザーポータルを作り、行く。作り方は知ってるよね？", 100),
    GOTO_WILDEND("はて？ここは果て？", "ワイルドエリアでエンド要塞を見つけ、行く。要塞の意味は知ってるよね？", 100),
    GOTO_SANDBOX("好きなだけ実験しよう", "サンドボックスへ行く。クリエイティブモードで好きなだけ実験できる。爆発物も思いのママ。あ、鯖は落とすなよ？", 100),
    GOTO_ART("アーティストへの第一歩", "アートワールドへ行く。サンドボックスに近いが、こちらはクリエイティブ建築用ワールド。", 100),
    GOTO_NIGHTMARE("悪い夢", "ナイトメアワールドへ行く。怖い敵がうじゃうじゃ、天候は最悪。だけど貰えるエビパワーは2倍！", 100),
    
    GOTO_CLASSIC_LOBBY("先代の歩んだ歴史を探して Part.1", "クラシックロビーへ行く。かつてのXelticaMCのロビー。", 100),
    GOTO_CLASSIC_WORLD("先代の歩んだ歴史を探して Part.2", "クラシックワールドへ行く。発展しきった過去のメインワールドを刮目せよ。", 100),
    GOTO_CLASSIC_WILDAREA("そう、クラシックで、ワイルドです", "クラシックワイルドエリアへ行く。クラシックワールドで建築したければここで資材を集めるが良い。", 100),
    
    // USE_POINT("力こそパワー", "エビパワーを使用する", 100),
    
    CAT_MODE("にゃんか猫ににゃった", "ネコ語モードをオンにする。チャットの「な」が「にゃ」になる。", 50),
    BOAT("ボートを出す", "/boat コマンドを使うか、X Phoneを用いてボートを出現させる", 50),
    MINECART("トロッコを出す", "/cart コマンドを使うか、X Phoneを用いてトロッコを出現させる", 50),
    KUSA("草", "草ブロックの上で「草」「www」などと発言して、ガチで草を生やす", 50),
    BE_CITIZEN("晴れて市民になった！", "市民へ昇格する。詳しくは「市民システム」アプリを見てね。", 500),

    VIOLENCE_CHILD("Don't touch the child!", "子どもモブを攻撃するとエビパワーが10減ります。殴っちゃダメよ。", 10),
    VIOLENCE_PET("動物虐待、ダメ、ゼッタイ。", "誰かに飼いならされたモブを攻撃するとエビパワーが10減ります。殴っちゃダメよ。", 10),
    VIOLENCE_CAT("猫を殴るなんて！！(-100)", "ネコちゃんを殴るなんて言語道断ですよね。エビパワーが100減ります。", 100),

    OMIKUJI_TOKUDAIKICHI("運勢「特大吉」", "おみくじを引いて、特大吉を当てよう！0.001%の確率だ！特大吉になると、20分間「幸運」ポーション効果がつく上に、5%の確率で死亡時に「不死のトーテム」効果がつくぞ！", 1000),
    OMIKUJI_DAIKYOU("運勢「大凶」", "おみくじを引いて、大凶を当てよう！0.001%の確率だ！大凶になると、「不運」「不吉の予感」「毒」効果がついてしまう…。", 1000),

    EBIPOWER("エビパワーとは", "海老パワーは、この世界で遊ぶほど貯まるポイントです。戦闘、進捗達成、ヒント達成、ログインボーナスなどで貰え、アイテム購入、飛行、おみくじなど様々なことに使えます。クラシックワールドでは貯まりません。"),
    WEB("公式サイト", "公式サイトには様々な情報が書かれています。規約も書いてあるので、一度読んでください。\n\nXelticaMCで検索。"),
    WHAT_TO_DO("やることがない", "やることに困ったら、まずはワイルドエリアで資源集めをしましょう。その後、メインワールドに戻って、トロッコを使って電車に乗り、好きな場所にお家を建てましょう。"),
    ;

    Hint(String name, String description) {
        this(name, description, 0);
    }

    Hint(String name, String description, int power) {
        this.name = name;
        this.description = description;
        this.power = power;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getPower() { return power; }

    private String name;
    private String description;
    private int power;
}
