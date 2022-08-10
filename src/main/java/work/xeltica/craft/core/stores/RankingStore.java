package work.xeltica.craft.core.stores;

import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import org.jetbrains.annotations.Nullable;
import work.xeltica.craft.core.XCorePlugin;
import work.xeltica.craft.core.api.Config;
import work.xeltica.craft.core.models.Ranking;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 時間計測カウンターの情報を管理します。
 */
public class RankingStore {
    public RankingStore() {
        instance = this;
        rankingConfig = new Config("ranking");
        renderAll();
    }

    public static RankingStore getInstance() {
        return RankingStore.instance;
    }

    /**
     * ランキングを新規作成します。
     * @throws IllegalArgumentException 既にランキングが存在する
     * @throws IOException 保存に失敗
     */
    public Ranking create(String name, String displayName, boolean isPlayerMode) throws IOException {
        if (has(name)) throw new IllegalArgumentException();

        final var ranking = new Ranking(name, rankingConfig);
        ranking.setDisplayName(displayName, false);
        ranking.setIsPlayerMode(isPlayerMode, false);
        ranking.save();
        renderAll();
        return ranking;
    }

    /**
     * ランキングを削除します。
     * @return ランキング削除に成功すればtrue、そうでなければfalse。
     * @throws IOException 保存に失敗
     */
    public boolean delete(String name) throws IOException {
        if (!has(name)) return false;

        rankingConfig.getConf().set(name, null);
        rankingConfig.save();
        renderAll();
        return true;
    }

    /**
     * ランキングが存在するかどうかを取得します。
     * @param name ランキング名
     * @return ランキングが存在すればtrue、しなければfalse
     */
    public boolean has(String name) {
        return rankingConfig.getConf().contains(name);
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

    /**
     * ランキングを全て取得します。
     * @return ランキング一覧。
     */
    public Set<Ranking> getAll() {
        return rankingConfig.getConf().getKeys(false).stream()
            .map(name -> new Ranking(name, rankingConfig))
            .collect(Collectors.toSet());
    }

    public void renderAll() {
        HologramsAPI.getHolograms(XCorePlugin.getInstance()).forEach(h -> h.delete());
        getAll().forEach(ranks -> {
            final var loc = ranks.getHologramLocation();
            if (loc == null) return;
            final var isHidden = ranks.getHologramHidden();
            final var holo = HologramsAPI.createHologram(XCorePlugin.getInstance(), loc);

            holo.appendTextLine("§a§l" + ranks.getDisplayName());
            final var ranking = ranks.queryRanking();
            for (var i = 0; i < 10; i++) {
                final var name = isHidden ? "??????????" : ranking.length <= i ? "--------" : ranking[i].getId();
                final var value = isHidden ? "??????????" : ranking.length <= i ? "------" : ranking[i].getScore();
                final var prefix = i == 0 ? "§e" : i == 1 ? "§f" : i == 2 ? "§6" : "§d";
                holo.appendTextLine(String.format("%s§l%d位: %s (%s)", prefix, i + 1, name, value));
            }
        });
    }

    private static RankingStore instance;

    /** ranking.yml */
    private Config rankingConfig;
}
