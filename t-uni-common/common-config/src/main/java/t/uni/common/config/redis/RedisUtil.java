package t.uni.common.config.redis;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis工具类
 * 提供常用的Redis操作方法，包括String、Hash、List、Set、ZSet等数据结构的操作
 * 以及分布式锁、发布订阅等高级功能
 */
@Slf4j
@Component
public class RedisUtil {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    // =============================String操作=============================

    /**
     * 设置缓存
     *
     * @param key   键
     * @param value 值
     * @return true成功 false失败
     */
    public boolean set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            log.error("Redis set error, key: {}", key, e);
            return false;
        }
    }

    /**
     * 设置缓存并设置过期时间
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒) time要大于0 如果time小于等于0 将设置无限期
     * @return true成功 false失败
     */
    public boolean set(String key, Object value, long time) {
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            log.error("Redis set with expire error, key: {}, time: {}", key, time, e);
            return false;
        }
    }

    /**
     * 设置缓存并设置过期时间（自定义时间单位）
     *
     * @param key      键
     * @param value    值
     * @param time     时间
     * @param timeUnit 时间单位
     * @return true成功 false失败
     */
    public boolean set(String key, Object value, long time, TimeUnit timeUnit) {
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, timeUnit);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            log.error("Redis set with expire error, key: {}, time: {}, unit: {}", key, time, timeUnit, e);
            return false;
        }
    }

    /**
     * 获取缓存
     *
     * @param key 键
     * @return 值
     */
    public Object get(String key) {
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }

    /**
     * 获取缓存（泛型）
     *
     * @param key   键
     * @param clazz 返回类型
     * @param <T>   泛型
     * @return 值
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        Object value = get(key);
        if (value == null) {
            return null;
        }
        try {
            return (T) value;
        } catch (ClassCastException e) {
            log.error("Redis get error, key: {}, class: {}", key, clazz.getName(), e);
            return null;
        }
    }

    /**
     * 删除缓存
     *
     * @param key 键 可以传一个值 或多个
     * @return 删除的数量
     */
    public Long delete(String... key) {
        if (key != null && key.length > 0) {
            if (key.length == 1) {
                Boolean result = redisTemplate.delete(key[0]);
                return result ? 1L : 0L;
            } else {
                return redisTemplate.delete(Arrays.asList(key));
            }
        }
        return 0L;
    }

    /**
     * 判断key是否存在
     *
     * @param key 键
     * @return true 存在 false不存在
     */
    public boolean hasKey(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error("Redis hasKey error, key: {}", key, e);
            return false;
        }
    }

    /**
     * 设置过期时间
     *
     * @param key  键
     * @param time 时间(秒)
     */
    public void expire(String key, long time) {
        try {
            if (time > 0) {
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            log.error("Redis expire error, key: {}, time: {}", key, time, e);
        }
    }

    /**
     * 设置过期时间（自定义时间单位）
     *
     * @param key      键
     * @param time     时间
     * @param timeUnit 时间单位
     * @return true成功 false失败
     */
    public boolean expire(String key, long time, TimeUnit timeUnit) {
        try {
            if (time > 0) {
                redisTemplate.expire(key, time, timeUnit);
            }
            return true;
        } catch (Exception e) {
            log.error("Redis expire error, key: {}, time: {}, unit: {}", key, time, timeUnit, e);
            return false;
        }
    }

    /**
     * 获取过期时间
     *
     * @param key 键 不能为null
     * @return 时间(秒) 返回0代表为永久有效
     */
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 递增
     *
     * @param key   键
     * @param delta 要增加几(大于0)
     * @return 递增后的值
     */
    public Long increment(String key, long delta) {
        if (delta < 0) {
            throw new IllegalArgumentException("递增因子必须大于0");
        }
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 递减
     *
     * @param key   键
     * @param delta 要减少几(大于0)
     * @return 递减后的值
     */
    public Long decrement(String key, long delta) {
        if (delta < 0) {
            throw new IllegalArgumentException("递减因子必须大于0");
        }
        return redisTemplate.opsForValue().decrement(key, delta);
    }

    // ================================Hash操作=================================

    /**
     * HashGet
     *
     * @param key  键 不能为null
     * @param item 项 不能为null
     * @return 值
     */
    public Object hGet(String key, String item) {
        return redisTemplate.opsForHash().get(key, item);
    }

    /**
     * 获取hashKey对应的所有键值
     *
     * @param key 键
     * @return 对应的多个键值
     */
    public Map<Object, Object> hGetAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * HashSet
     *
     * @param key 键
     * @param map 对应多个键值
     * @return true 成功 false 失败
     */
    public boolean hSetAll(String key, Map<String, Object> map) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            return true;
        } catch (Exception e) {
            log.error("Redis hSetAll error, key: {}", key, e);
            return false;
        }
    }

    /**
     * HashSet 并设置时间
     *
     * @param key  键
     * @param map  对应多个键值
     * @param time 时间(秒)
     * @return true成功 false失败
     */
    public boolean hSetAll(String key, Map<String, Object> map, long time) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            log.error("Redis hSetAll with expire error, key: {}, time: {}", key, time, e);
            return false;
        }
    }

    /**
     * 向一张hash表中放入数据,如果不存在将创建
     *
     * @param key   键
     * @param item  项
     * @param value 值
     * @return true 成功 false失败
     */
    public boolean hSet(String key, String item, Object value) {
        try {
            redisTemplate.opsForHash().put(key, item, value);
            return true;
        } catch (Exception e) {
            log.error("Redis hSet error, key: {}, item: {}", key, item, e);
            return false;
        }
    }

    /**
     * 向一张hash表中放入数据,如果不存在将创建，并设置过期时间
     *
     * @param key   键
     * @param item  项
     * @param value 值
     * @param time  时间(秒) 注意:如果已存在的hash表有时间,这里将会替换原有的时间
     * @return true 成功 false失败
     */
    public boolean hSet(String key, String item, Object value, long time) {
        try {
            redisTemplate.opsForHash().put(key, item, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            log.error("Redis hSet with expire error, key: {}, item: {}, time: {}", key, item, time, e);
            return false;
        }
    }

    /**
     * 删除hash表中的值
     *
     * @param key  键 不能为null
     * @param item 项 可以使多个 不能为null
     * @return 删除的数量
     */
    public Long hDelete(String key, Object... item) {
        return redisTemplate.opsForHash().delete(key, item);
    }

    /**
     * 判断hash表中是否有该项的值
     *
     * @param key  键 不能为null
     * @param item 项 不能为null
     * @return true 存在 false不存在
     */
    public boolean hHasKey(String key, String item) {
        return redisTemplate.opsForHash().hasKey(key, item);
    }

    /**
     * hash递增 如果不存在,就会创建一个 并把新增后的值返回
     *
     * @param key  键
     * @param item 项
     * @param by   要增加几(大于0)
     * @return 递增后的值
     */
    public Double hIncrement(String key, String item, double by) {
        return redisTemplate.opsForHash().increment(key, item, by);
    }

    /**
     * hash递减
     *
     * @param key  键
     * @param item 项
     * @param by   要减少几(大于0)
     * @return 递减后的值
     */
    public Double hDecrement(String key, String item, double by) {
        return redisTemplate.opsForHash().increment(key, item, -by);
    }

    // ============================List操作=============================

    /**
     * 获取list缓存的内容
     *
     * @param key   键
     * @param start 开始
     * @param end   结束 0 到 -1代表所有值
     * @return list内容
     */
    public List<Object> lRange(String key, long start, long end) {
        try {
            return redisTemplate.opsForList().range(key, start, end);
        } catch (Exception e) {
            log.error("Redis lRange error, key: {}, start: {}, end: {}", key, start, end, e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取list缓存的长度
     *
     * @param key 键
     * @return 长度
     */
    public Long lSize(String key) {
        try {
            return redisTemplate.opsForList().size(key);
        } catch (Exception e) {
            log.error("Redis lSize error, key: {}", key, e);
            return 0L;
        }
    }

    /**
     * 通过索引 获取list中的值
     *
     * @param key   键
     * @param index 索引 index>=0时， 0 表头，1 第二个元素，依次类推；index<0时，-1，表尾，-2倒数第二个元素，依次类推
     * @return 值
     */
    public Object lIndex(String key, long index) {
        try {
            return redisTemplate.opsForList().index(key, index);
        } catch (Exception e) {
            log.error("Redis lIndex error, key: {}, index: {}", key, index, e);
            return null;
        }
    }

    /**
     * 将值放入list缓存
     *
     * @param key   键
     * @param value 值
     * @return true成功 false失败
     */
    public boolean lPush(String key, Object value) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            return true;
        } catch (Exception e) {
            log.error("Redis lPush error, key: {}", key, e);
            return false;
        }
    }

    /**
     * 将值放入list缓存，并设置过期时间
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒)
     * @return true成功 false失败
     */
    public boolean lPush(String key, Object value, long time) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            log.error("Redis lPush with expire error, key: {}, time: {}", key, time, e);
            return false;
        }
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @return true成功 false失败
     */
    public boolean lPushAll(String key, List<Object> value) {
        try {
            redisTemplate.opsForList().rightPushAll(key, value);
            return true;
        } catch (Exception e) {
            log.error("Redis lPushAll error, key: {}", key, e);
            return false;
        }
    }

    /**
     * 将list放入缓存，并设置过期时间
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒)
     * @return true成功 false失败
     */
    public boolean lPushAll(String key, List<Object> value, long time) {
        try {
            redisTemplate.opsForList().rightPushAll(key, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            log.error("Redis lPushAll with expire error, key: {}, time: {}", key, time, e);
            return false;
        }
    }

    /**
     * 根据索引修改list中的某条数据
     *
     * @param key   键
     * @param index 索引
     * @param value 值
     * @return true成功 false失败
     */
    public boolean lUpdateIndex(String key, long index, Object value) {
        try {
            redisTemplate.opsForList().set(key, index, value);
            return true;
        } catch (Exception e) {
            log.error("Redis lUpdateIndex error, key: {}, index: {}", key, index, e);
            return false;
        }
    }

    /**
     * 移除N个值为value的元素
     *
     * @param key   键
     * @param count 移除多少个
     * @param value 值
     * @return 移除的个数
     */
    public Long lRemove(String key, long count, Object value) {
        try {
            return redisTemplate.opsForList().remove(key, count, value);
        } catch (Exception e) {
            log.error("Redis lRemove error, key: {}, count: {}", key, count, e);
            return 0L;
        }
    }

    // ===============================Set操作=================================

    /**
     * 根据key获取Set中的所有值
     *
     * @param key 键
     * @return Set集合
     */
    public Set<Object> sMembers(String key) {
        try {
            return redisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            log.error("Redis sMembers error, key: {}", key, e);
            return Collections.emptySet();
        }
    }

    /**
     * 根据value从一个set中查询,是否存在
     *
     * @param key   键
     * @param value 值
     * @return true 存在 false不存在
     */
    public boolean sHasKey(String key, Object value) {
        try {
            Boolean result = redisTemplate.opsForSet().isMember(key, value);
            return result != null && result;
        } catch (Exception e) {
            log.error("Redis sHasKey error, key: {}", key, e);
            return false;
        }
    }

    /**
     * 将数据放入set缓存
     *
     * @param key    键
     * @param values 值 可以是多个
     * @return 成功个数
     */
    public Long sAdd(String key, Object... values) {
        try {
            return redisTemplate.opsForSet().add(key, values);
        } catch (Exception e) {
            log.error("Redis sAdd error, key: {}", key, e);
            return 0L;
        }
    }

    /**
     * 将set数据放入缓存，并设置过期时间
     *
     * @param key    键
     * @param time   时间(秒)
     * @param values 值 可以是多个
     * @return 成功个数
     */
    public Long sAdd(String key, long time, Object... values) {
        try {
            Long count = redisTemplate.opsForSet().add(key, values);
            if (time > 0) {
                expire(key, time);
            }
            return count;
        } catch (Exception e) {
            log.error("Redis sAdd with expire error, key: {}, time: {}", key, time, e);
            return 0L;
        }
    }

    /**
     * 获取set缓存的长度
     *
     * @param key 键
     * @return 长度
     */
    public Long sSize(String key) {
        try {
            return redisTemplate.opsForSet().size(key);
        } catch (Exception e) {
            log.error("Redis sSize error, key: {}", key, e);
            return 0L;
        }
    }

    /**
     * 移除值为value的元素
     *
     * @param key    键
     * @param values 值 可以是多个
     * @return 移除的个数
     */
    public Long sRemove(String key, Object... values) {
        try {
            return redisTemplate.opsForSet().remove(key, values);
        } catch (Exception e) {
            log.error("Redis sRemove error, key: {}", key, e);
            return 0L;
        }
    }

    // ===============================ZSet操作=================================

    /**
     * 添加元素到有序集合
     *
     * @param key   键
     * @param value 值
     * @param score 分数
     * @return true成功 false失败
     */
    public boolean zAdd(String key, Object value, double score) {
        try {
            Boolean result = redisTemplate.opsForZSet().add(key, value, score);
            return result != null && result;
        } catch (Exception e) {
            log.error("Redis zAdd error, key: {}, score: {}", key, score, e);
            return false;
        }
    }

    /**
     * 批量添加到有序集合
     *
     * @param key    键
     * @param values 值和分数的集合
     * @return 成功个数
     */
    public Long zAdd(String key, Set<ZSetOperations.TypedTuple<Object>> values) {
        try {
            return redisTemplate.opsForZSet().add(key, values);
        } catch (Exception e) {
            log.error("Redis zAdd batch error, key: {}", key, e);
            return 0L;
        }
    }

    /**
     * 获取有序集合的成员数
     *
     * @param key 键
     * @return 成员数
     */
    public Long zSize(String key) {
        try {
            return redisTemplate.opsForZSet().size(key);
        } catch (Exception e) {
            log.error("Redis zSize error, key: {}", key, e);
            return 0L;
        }
    }

    /**
     * 获取指定范围的元素（升序）
     *
     * @param key   键
     * @param start 开始位置
     * @param end   结束位置
     * @return 元素集合
     */
    public Set<Object> zRange(String key, long start, long end) {
        try {
            return redisTemplate.opsForZSet().range(key, start, end);
        } catch (Exception e) {
            log.error("Redis zRange error, key: {}, start: {}, end: {}", key, start, end, e);
            return Collections.emptySet();
        }
    }

    /**
     * 获取指定范围的元素（降序）
     *
     * @param key   键
     * @param start 开始位置
     * @param end   结束位置
     * @return 元素集合
     */
    public Set<Object> zReverseRange(String key, long start, long end) {
        try {
            return redisTemplate.opsForZSet().reverseRange(key, start, end);
        } catch (Exception e) {
            log.error("Redis zReverseRange error, key: {}, start: {}, end: {}", key, start, end, e);
            return Collections.emptySet();
        }
    }

    /**
     * 移除有序集合中的元素
     *
     * @param key    键
     * @param values 值
     * @return 移除的个数
     */
    public Long zRemove(String key, Object... values) {
        try {
            return redisTemplate.opsForZSet().remove(key, values);
        } catch (Exception e) {
            log.error("Redis zRemove error, key: {}", key, e);
            return 0L;
        }
    }

    /**
     * 增加元素的score值，并返回增加后的值
     *
     * @param key   键
     * @param value 值
     * @param delta 增加的分数
     * @return 增加后的分数
     */
    public Double zIncrementScore(String key, Object value, double delta) {
        try {
            return redisTemplate.opsForZSet().incrementScore(key, value, delta);
        } catch (Exception e) {
            log.error("Redis zIncrementScore error, key: {}, delta: {}", key, delta, e);
            return null;
        }
    }

    /**
     * 获取元素的排名（升序，从0开始）
     *
     * @param key   键
     * @param value 值
     * @return 排名
     */
    public Long zRank(String key, Object value) {
        try {
            return redisTemplate.opsForZSet().rank(key, value);
        } catch (Exception e) {
            log.error("Redis zRank error, key: {}", key, e);
            return null;
        }
    }

    /**
     * 获取元素的排名（降序，从0开始）
     *
     * @param key   键
     * @param value 值
     * @return 排名
     */
    public Long zReverseRank(String key, Object value) {
        try {
            return redisTemplate.opsForZSet().reverseRank(key, value);
        } catch (Exception e) {
            log.error("Redis zReverseRank error, key: {}", key, e);
            return null;
        }
    }

    // ===============================分布式锁=================================

    /**
     * 尝试获取分布式锁
     *
     * @param key        锁的key
     * @param value      锁的value（建议使用UUID）
     * @param expireTime 过期时间（秒）
     * @return true获取成功 false获取失败
     */
    public boolean tryLock(String key, String value, long expireTime) {
        try {
            Boolean result = redisTemplate.opsForValue().setIfAbsent(key, value, expireTime, TimeUnit.SECONDS);
            return result != null && result;
        } catch (Exception e) {
            log.error("Redis tryLock error, key: {}, expireTime: {}", key, expireTime, e);
            return false;
        }
    }

    /**
     * 释放分布式锁
     *
     * @param key   锁的key
     * @param value 锁的value
     * @return true释放成功 false释放失败
     */
    public boolean releaseLock(String key, String value) {
        try {
            Object currentValue = redisTemplate.opsForValue().get(key);
            if (currentValue != null && currentValue.equals(value)) {
                Boolean result = redisTemplate.delete(key);
                return result != null && result;
            }
            return false;
        } catch (Exception e) {
            log.error("Redis releaseLock error, key: {}", key, e);
            return false;
        }
    }

    // ===============================其他常用操作=================================

    /**
     * 获取匹配的key列表
     *
     * @param pattern 匹配模式，如：user:*
     * @return key列表
     */
    public Set<String> keys(String pattern) {
        try {
            return redisTemplate.keys(pattern);
        } catch (Exception e) {
            log.error("Redis keys error, pattern: {}", pattern, e);
            return Collections.emptySet();
        }
    }

    /**
     * 批量删除key
     *
     * @param pattern 匹配模式，如：user:*
     * @return 删除的数量
     */
    public Long deleteByPattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                return redisTemplate.delete(keys);
            }
            return 0L;
        } catch (Exception e) {
            log.error("Redis deleteByPattern error, pattern: {}", pattern, e);
            return 0L;
        }
    }

    /**
     * 重命名key
     *
     * @param oldKey 旧key
     * @param newKey 新key
     * @return true成功 false失败
     */
    public boolean rename(String oldKey, String newKey) {
        try {
            redisTemplate.rename(oldKey, newKey);
            return true;
        } catch (Exception e) {
            log.error("Redis rename error, oldKey: {}, newKey: {}", oldKey, newKey, e);
            return false;
        }
    }

    /**
     * 仅当newKey不存在时，将oldKey改名为newKey
     *
     * @param oldKey 旧key
     * @param newKey 新key
     * @return true成功 false失败
     */
    public boolean renameIfAbsent(String oldKey, String newKey) {
        try {
            return redisTemplate.renameIfAbsent(oldKey, newKey);
        } catch (Exception e) {
            log.error("Redis renameIfAbsent error, oldKey: {}, newKey: {}", oldKey, newKey, e);
            return false;
        }
    }
}
