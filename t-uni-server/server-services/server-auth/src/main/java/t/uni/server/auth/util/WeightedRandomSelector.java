package t.uni.server.auth.util;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.ToIntFunction;

/**
 * 加权随机选择器。
 * <p>
 * 用于默认头像、昵称等候选池选择。权重越大，被选中的概率越高。
 * 总权重小于等于 0 时退化为均匀随机，避免配置错误导致无法分配默认值。
 * </p>
 */
public final class WeightedRandomSelector {

    private WeightedRandomSelector() {
        // 工具类禁止实例化
    }

    /**
     * 从列表中根据权重随机选择一个元素。
     *
     * @param items        元素列表
     * @param weightGetter 获取权重的函数
     * @param <T>          元素类型
     * @return 随机选中的元素，如果列表为空返回 null
     */
    public static <T> T select(List<T> items, ToIntFunction<T> weightGetter) {
        if (items == null || items.isEmpty()) {
            return null;
        }

        var totalWeight = items.stream()
                .mapToInt(weightGetter)
                .sum();

        if (totalWeight <= 0) {
            // 权重配置不可用时仍返回候选值，避免登录建档失败。
            return items.get(ThreadLocalRandom.current().nextInt(items.size()));
        }

        var random = ThreadLocalRandom.current().nextInt(totalWeight);

        var cumulative = 0;
        for (var item : items) {
            cumulative += weightGetter.applyAsInt(item);
            if (random < cumulative) {
                return item;
            }
        }

        // 理论兜底：应只在权重函数返回异常值时触达。
        return items.get(items.size() - 1);
    }
}
