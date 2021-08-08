package work.xeltica.craft.core.stores;

import java.io.IOException;

import org.jetbrains.annotations.Nullable;

import lombok.Getter;
import work.xeltica.craft.core.models.Ranking;
import work.xeltica.craft.core.utils.Config;

/**
 * 時間計測カウンターの情報を管理します。
 */
public class RankingStore {
    public RankingStore() {
        instance = this;
        rankingConfig = new Config("ranking");
    }

    /**
     * ランキングを新規作成します。
     * @throws IllegalArgumentException 既にランキングが存在する
     * @throws IOException 保存に失敗
     */
    public Ranking create(String name, String displayName) throws IOException {
        if (has(name)) throw new IllegalArgumentException();

        final var ranking = new Ranking(name, rankingConfig);
        ranking.setDisplayName(displayName);
        return ranking;
    }

    /**
     * ランキングが存在するかどうかを取得します。
     * @param name ランキング名
     * @return ランキングが存在すればtrue、しなければfalse
     */
    public boolean has(String name) {
        return !rankingConfig.getConf().contains(name);
    }

    /**
     * 指定した名前のランキングを取得します。
     * @param name ランキング名
     * @return ランキング。存在しなければnull
     */
    @Nullable
    public Ranking get(String name) {
        if (!has(name)) return null;

        return new Ranking(name, rankingConfig);
    }

    @Getter
    private static RankingStore instance;

    /** ranking.yml */
    private Config rankingConfig;
}
