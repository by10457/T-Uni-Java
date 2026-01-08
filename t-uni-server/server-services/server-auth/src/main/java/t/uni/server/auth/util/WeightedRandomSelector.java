package t.uni.server.auth.util;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.ToIntFunction;

/**
 * 加权随机选择器
 * <p>
 * 根据权重进行随机选择，权重越大被选中的概率越高
 * </p>
 */
public final class WeightedRandomSelector {

    private WeightedRandomSelector() {
        // 工具类禁止实例化
    }

    /**
     * 从列表中根据权重随机选择一个元素
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

        // 计算总权重
        var totalWeight = items.stream()
                .mapToInt(weightGetter)
                .sum();

        if (totalWeight <= 0) {
            // 如果总权重为0，随机返回一个元素
            return items.get(ThreadLocalRandom.current().nextInt(items.size()));
        }

        // 生成随机数 [0, totalWeight)
        var random = ThreadLocalRandom.current().nextInt(totalWeight);

        // 累加权重直到超过随机数
        var cumulative = 0;
        for (var item : items) {
            cumulative += weightGetter.applyAsInt(item);
            if (random < cumulative) {
                return item;
            }
        }

        // 理论上不会执行到这里，返回最后一个元素作为兜底
        return items.get(items.size() - 1);
    }
}
