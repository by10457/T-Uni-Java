package t.uni.common.core.redis;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis工具类
 * <p>
 * 提供常用的Redis操作方法，包括String、Hash、List、Set、ZSet等数据结构的操作
 * 以及分布式锁、批量操作等高级功能。
 * </p>
 * <p>
 * 设计原则：
 * <ul>
 *     <li>统一异常处理：所有方法都包含try-catch，异常时返回默认值或false</li>
 *     <li>空值安全：对null参数进行校验，避免NPE</li>
 *     <li>返回值明确：所有方法都有明确的返回值，避免void方法忽略返回值</li>
 *     <li>批量操作优化：使用Pipeline减少网络往返</li>
 * </ul>
 * </p>
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
            log.error("Redis设置缓存失败，key: {}", key, e);
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
            log.error("Redis设置带过期时间缓存失败，key: {}, 过期时间: {}", key, time, e);
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
            log.error("Redis设置带过期时间缓存失败，key: {}, 过期时间: {}, 时间单位: {}", key, time, timeUnit, e);
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
            log.error("Redis获取缓存失败，key: {}, 类型: {}", key, clazz.getName(), e);
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
     * @param key 键，不能为null
     * @return true存在 false不存在或异常
     */
    public boolean hasKey(String key) {
        if (key == null) {
            return false;
        }
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error("Redis检查key是否存在失败，key: {}", key, e);
            return false;
        }
    }

    /**
     * 设置过期时间
     *
     * @param key  键
     * @param time 时间(秒)，必须大于0
     * @return true成功 false失败
     */
    public boolean expire(String key, long time) {
        if (key == null || time <= 0) {
            return false;
        }
        try {
            return redisTemplate.expire(key, time, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Redis设置过期时间失败，key: {}, 过期时间: {}", key, time, e);
            return false;
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
            log.error("Redis设置过期时间失败，key: {}, 过期时间: {}, 时间单位: {}", key, time, timeUnit, e);
            return false;
        }
    }

    /**
     * 获取过期时间
     *
     * @param key 键，不能为null
     * @return 时间(秒)，返回0代表为永久有效，返回-1代表key不存在，返回-2代表key存在但没有设置过期时间
     */
    public Long getExpire(String key) {
        if (key == null) {
            return -1L;
        }
        try {
            return redisTemplate.getExpire(key, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Redis获取过期时间失败，key: {}", key, e);
            return -1L;
        }
    }

    /**
     * 移除过期时间，使key永久有效
     *
     * @param key 键，不能为null
     * @return true成功 false失败
     */
    public boolean persist(String key) {
        if (key == null) {
            return false;
        }
        try {
            return redisTemplate.persist(key);
        } catch (Exception e) {
            log.error("Redis移除过期时间失败，key: {}", key, e);
            return false;
        }
    }

    /**
     * 递增
     *
     * @param key   键，不能为null
     * @param delta 要增加的值，必须大于等于0
     */
    public void increment(String key, long delta) {
        if (key == null) {
            return;
        }
        if (delta < 0) {
            throw new IllegalArgumentException("递增因子必须大于等于0");
        }
        try {
            redisTemplate.opsForValue().increment(key, delta);
        } catch (Exception e) {
            log.error("Redis递增失败，key: {}, 增量: {}", key, delta, e);
        }
    }

    /**
     * 仅当key不存在时设置值（SETNX）
     * <p>
     * 原子操作，常用于分布式锁的实现
     * </p>
     *
     * @param key   键，不能为null
     * @param value 值
     * @return true设置成功（key不存在） false设置失败（key已存在）
     */
    public boolean setIfAbsent(String key, Object value) {
        if (key == null) {
            return false;
        }
        try {
            Boolean result = redisTemplate.opsForValue().setIfAbsent(key, value);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("Redis SETNX失败，key: {}", key, e);
            return false;
        }
    }

    /**
     * 仅当key不存在时设置值，并设置过期时间（SETNX + EXPIRE）
     * <p>
     * 原子操作，常用于分布式锁的实现
     * </p>
     *
     * @param key      键，不能为null
     * @param value    值
     * @param timeout  过期时间
     * @param timeUnit 时间单位
     * @return true设置成功（key不存在） false设置失败（key已存在）
     */
    public boolean setIfAbsent(String key, Object value, long timeout, TimeUnit timeUnit) {
        if (key == null || timeout <= 0) {
            return false;
        }
        try {
            Boolean result = redisTemplate.opsForValue().setIfAbsent(key, value, timeout, timeUnit);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("Redis SETNX带过期时间失败，key: {}, 过期时间: {}", key, timeout, e);
            return false;
        }
    }

    /**
     * 获取并设置新值（GETSET）
     * <p>
     * 原子操作，返回旧值并设置新值
     * </p>
     *
     * @param key   键，不能为null
     * @param value 新值
     * @return 旧值，key不存在返回null
     */
    @SuppressWarnings("unchecked")
    public <T> T getAndSet(String key, Object value) {
        if (key == null) {
            return null;
        }
        try {
            return (T) redisTemplate.opsForValue().getAndSet(key, value);
        } catch (Exception e) {
            log.error("Redis GETSET失败，key: {}", key, e);
            return null;
        }
    }

    /**
     * 递减
     *
     * @param key   键，不能为null
     * @param delta 要减少的值，必须大于等于0
     */
    public void decrement(String key, long delta) {
        if (key == null) {
            return;
        }
        if (delta < 0) {
            throw new IllegalArgumentException("递减因子必须大于等于0");
        }
        try {
            redisTemplate.opsForValue().decrement(key, delta);
        } catch (Exception e) {
            log.error("Redis递减失败，key: {}, 减量: {}", key, delta, e);
        }
    }

    // ================================Hash操作=================================

    /**
     * HashGet - 获取Hash表中指定field的值
     *
     * @param key  键，不能为null
     * @param item 项（field），不能为null
     * @return 值，不存在返回null
     */
    public Object hGet(String key, String item) {
        if (key == null || item == null) {
            return null;
        }
        try {
            return redisTemplate.opsForHash().get(key, item);
        } catch (Exception e) {
            log.error("Redis获取Hash表项失败，key: {}, item: {}", key, item, e);
            return null;
        }
    }

    /**
     * 获取hashKey对应的所有键值对
     *
     * @param key 键，不能为null
     * @return 对应的多个键值对，不存在返回空Map
     */
    public Map<Object, Object> hGetAll(String key) {
        if (key == null) {
            return Collections.emptyMap();
        }
        try {
            return redisTemplate.opsForHash().entries(key);
        } catch (Exception e) {
            log.error("Redis获取Hash表所有键值失败，key: {}", key, e);
            return Collections.emptyMap();
        }
    }

    /**
     * HashSet - 批量设置Hash表的多个键值对
     *
     * @param key 键，不能为null
     * @param map 对应多个键值对，不能为null或空
     */
    public void hSetAll(String key, Map<String, Object> map) {
        if (key == null || map == null || map.isEmpty()) {
            return;
        }
        try {
            redisTemplate.opsForHash().putAll(key, map);
        } catch (Exception e) {
            log.error("Redis设置Hash表失败，key: {}", key, e);
        }
    }

    /**
     * HashSet 并设置时间
     *
     * @param key  键
     * @param map  对应多个键值
     * @param time 时间(秒)，必须大于0
     * @return true成功 false失败
     */
    public boolean hSetAll(String key, Map<String, Object> map, long time) {
        if (key == null || map == null || map.isEmpty()) {
            return false;
        }
        try {
            redisTemplate.opsForHash().putAll(key, map);
            if (time > 0) {
                Boolean expireResult = redisTemplate.expire(key, time, TimeUnit.SECONDS);
                if (!expireResult) {
                    log.warn("Redis设置Hash表过期时间失败，key: {}, 过期时间: {}", key, time);
                }
            }
            return true;
        } catch (Exception e) {
            log.error("Redis设置带过期时间Hash表失败，key: {}, 过期时间: {}", key, time, e);
            return false;
        }
    }

    /**
     * 向一张hash表中放入数据，如果不存在将创建
     *
     * @param key   键，不能为null
     * @param item  项（field），不能为null
     * @param value 值，可以为null
     */
    public void hSet(String key, String item, Object value) {
        if (key == null || item == null) {
            return;
        }
        try {
            redisTemplate.opsForHash().put(key, item, value);
        } catch (Exception e) {
            log.error("Redis设置Hash表项失败，key: {}, 项: {}", key, item, e);
        }
    }

    /**
     * 向一张hash表中放入数据，如果不存在将创建，并设置过期时间
     * <p>
     * 注意：如果已存在的hash表有时间，这里将会替换原有的时间
     * </p>
     *
     * @param key   键，不能为null
     * @param item  项（field），不能为null
     * @param value 值，可以为null
     * @param time  时间(秒)，必须大于0
     * @return true成功 false失败
     */
    public boolean hSet(String key, String item, Object value, long time) {
        if (key == null || item == null || time <= 0) {
            return false;
        }
        try {
            redisTemplate.opsForHash().put(key, item, value);
            Boolean expireResult = redisTemplate.expire(key, time, TimeUnit.SECONDS);
            if (!expireResult) {
                log.warn("Redis设置Hash表项过期时间失败，key: {}, 项: {}, 过期时间: {}", key, item, time);
            }
            return true;
        } catch (Exception e) {
            log.error("Redis设置带过期时间Hash表项失败，key: {}, 项: {}, 过期时间: {}", key, item, time, e);
            return false;
        }
    }

    /**
     * 删除hash表中的值
     *
     * @param key  键，不能为null
     * @param item 项（field），可以是多个，不能为null
     */
    public void hDelete(String key, Object... item) {
        if (key == null || item == null || item.length == 0) {
            return;
        }
        try {
            redisTemplate.opsForHash().delete(key, item);
        } catch (Exception e) {
            log.error("Redis删除Hash表项失败，key: {}, 项数量: {}", key, item.length, e);
        }
    }

    /**
     * 判断hash表中是否有该项的值
     *
     * @param key  键，不能为null
     * @param item 项（field），不能为null
     * @return true存在 false不存在或异常
     */
    public boolean hHasKey(String key, String item) {
        if (key == null || item == null) {
            return false;
        }
        try {
            return redisTemplate.opsForHash().hasKey(key, item);
        } catch (Exception e) {
            log.error("Redis检查Hash表项是否存在失败，key: {}, item: {}", key, item, e);
            return false;
        }
    }

    /**
     * hash递增，如果不存在就会创建一个并把新增后的值返回
     *
     * @param key  键，不能为null
     * @param item 项（field），不能为null
     * @param by   要增加的值，可以为负数（表示递减）
     * @return 递增后的值，异常时返回null
     */
    public Double hIncrement(String key, String item, double by) {
        if (key == null || item == null) {
            return null;
        }
        try {
            return redisTemplate.opsForHash().increment(key, item, by);
        } catch (Exception e) {
            log.error("Redis Hash递增失败，key: {}, item: {}, 增量: {}", key, item, by, e);
            return null;
        }
    }

    /**
     * hash递减
     *
     * @param key  键，不能为null
     * @param item 项（field），不能为null
     * @param by   要减少的值，必须大于0
     * @return 递减后的值，异常时返回null
     */
    public Double hDecrement(String key, String item, double by) {
        if (key == null || item == null) {
            return null;
        }
        if (by <= 0) {
            throw new IllegalArgumentException("递减因子必须大于0");
        }
        try {
            return redisTemplate.opsForHash().increment(key, item, -by);
        } catch (Exception e) {
            log.error("Redis Hash递减失败，key: {}, item: {}, 减量: {}", key, item, by, e);
            return null;
        }
    }

    /**
     * 获取hash表的长度（即field的数量）
     *
     * @param key 键，不能为null
     * @return hash表中field的数量，不存在返回0，异常时返回0
     */
    public Long hLen(String key) {
        if (key == null) {
            return 0L;
        }
        try {
            return redisTemplate.opsForHash().size(key);
        } catch (Exception e) {
            log.error("Redis获取Hash表长度失败，key: {}", key, e);
            return 0L;
        }
    }

    /**
     * 判断hash表中是否存在指定的field
     *
     * @param key   键，不能为null
     * @param field 字段（field），不能为null
     * @return true存在 false不存在或异常
     */
    public boolean hExists(String key, String field) {
        if (key == null || field == null) {
            return false;
        }
        try {
            return redisTemplate.opsForHash().hasKey(key, field);
        } catch (Exception e) {
            log.error("Redis检查Hash表字段是否存在失败，key: {}, field: {}", key, field, e);
            return false;
        }
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
            log.error("Redis获取列表范围失败，key: {}, 起始位置: {}, 结束位置: {}", key, start, end, e);
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
            log.error("Redis获取列表大小失败，key: {}", key, e);
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
            log.error("Redis获取列表元素失败，key: {}, 索引: {}", key, index, e);
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
            log.error("Redis向列表添加元素失败，key: {}", key, e);
            return false;
        }
    }

    /**
     * 将值放入list缓存，并设置过期时间
     *
     * @param key   键，不能为null
     * @param value 值
     * @param time  时间(秒)，必须大于0
     * @return true成功 false失败
     */
    public boolean lPush(String key, Object value, long time) {
        if (key == null || time <= 0) {
            return false;
        }
        try {
            redisTemplate.opsForList().rightPush(key, value);
            Boolean expireResult = redisTemplate.expire(key, time, TimeUnit.SECONDS);
            if (!expireResult) {
                log.warn("Redis设置列表过期时间失败，key: {}, 过期时间: {}", key, time);
            }
            return true;
        } catch (Exception e) {
            log.error("Redis向列表添加带过期时间元素失败，key: {}, 过期时间: {}", key, time, e);
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
            log.error("Redis向列表批量添加元素失败，key: {}", key, e);
            return false;
        }
    }

    /**
     * 将list放入缓存，并设置过期时间
     *
     * @param key   键，不能为null
     * @param value 值列表，不能为null或空
     * @param time  时间(秒)，必须大于0
     * @return true成功 false失败
     */
    public boolean lPushAll(String key, List<Object> value, long time) {
        if (key == null || value == null || value.isEmpty() || time <= 0) {
            return false;
        }
        try {
            redisTemplate.opsForList().rightPushAll(key, value);
            Boolean expireResult = redisTemplate.expire(key, time, TimeUnit.SECONDS);
            if (!expireResult) {
                log.warn("Redis设置列表过期时间失败，key: {}, 过期时间: {}", key, time);
            }
            return true;
        } catch (Exception e) {
            log.error("Redis向列表批量添加带过期时间元素失败，key: {}, 过期时间: {}", key, time, e);
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
            log.error("Redis更新列表元素失败，key: {}, 索引: {}", key, index, e);
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
            log.error("Redis移除列表元素失败，key: {}, 数量: {}", key, count, e);
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
            log.error("Redis获取集合成员失败，key: {}", key, e);
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
            log.error("Redis检查集合成员是否存在失败，key: {}", key, e);
            return false;
        }
    }

    /**
     * 将数据放入set缓存
     *
     * @param key    键
     * @param values 值 可以是多个
     */
    public void sAdd(String key, Object... values) {
        try {
            redisTemplate.opsForSet().add(key, values);
        } catch (Exception e) {
            log.error("Redis向集合添加元素失败，key: {}", key, e);
        }
    }

    /**
     * 将set数据放入缓存，并设置过期时间
     *
     * @param key    键，不能为null
     * @param time   时间(秒)，必须大于0
     * @param values 值，可以是多个，不能为null或空
     * @return 成功个数，异常时返回0
     */
    public Long sAdd(String key, long time, Object... values) {
        if (key == null || values == null || values.length == 0 || time <= 0) {
            return 0L;
        }
        try {
            Long count = redisTemplate.opsForSet().add(key, values);
            Boolean expireResult = redisTemplate.expire(key, time, TimeUnit.SECONDS);
            if (!expireResult) {
                log.warn("Redis设置集合过期时间失败，key: {}, 过期时间: {}", key, time);
            }
            return count != null ? count : 0L;
        } catch (Exception e) {
            log.error("Redis向集合添加带过期时间元素失败，key: {}, 过期时间: {}", key, time, e);
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
            log.error("Redis获取集合大小失败，key: {}", key, e);
            return 0L;
        }
    }

    /**
     * 移除值为value的元素
     *
     * @param key    键
     * @param values 值 可以是多个
     */
    public void sRemove(String key, Object... values) {
        try {
            redisTemplate.opsForSet().remove(key, values);
        } catch (Exception e) {
            log.error("Redis从集合移除元素失败，key: {}", key, e);
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
            log.error("Redis向有序集合添加元素失败，key: {}, 分数: {}", key, score, e);
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
            log.error("Redis批量添加有序集合元素失败，key: {}", key, e);
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
            log.error("Redis获取有序集合大小失败，key: {}", key, e);
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
            log.error("Redis获取有序集合范围失败，key: {}, 起始位置: {}, 结束位置: {}", key, start, end, e);
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
            log.error("Redis获取有序集合反向范围失败，key: {}, 起始位置: {}, 结束位置: {}", key, start, end, e);
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
            log.error("Redis从有序集合移除元素失败，key: {}", key, e);
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
            log.error("Redis增加有序集合元素分数失败，key: {}, 增量: {}", key, delta, e);
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
            log.error("Redis获取有序集合元素排名失败，key: {}", key, e);
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
            log.error("Redis获取有序集合元素反向排名失败，key: {}", key, e);
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
            log.error("Redis尝试获取分布式锁失败，key: {}, 过期时间: {}", key, expireTime, e);
            return false;
        }
    }

    /**
     * 释放分布式锁
     * <p>
     * 注意：只有当前value与锁的value匹配时才会释放，避免误释放其他线程的锁
     * </p>
     *
     * @param key   锁的key，不能为null
     * @param value 锁的value，不能为null
     * @return true释放成功 false释放失败（value不匹配或异常）
     */
    public boolean releaseLock(String key, String value) {
        if (key == null || value == null) {
            return false;
        }
        try {
            Object currentValue = redisTemplate.opsForValue().get(key);
            if (currentValue != null && currentValue.equals(value)) {
                return redisTemplate.delete(key);
            }
            return false;
        } catch (Exception e) {
            log.error("Redis释放分布式锁失败，key: {}", key, e);
            return false;
        }
    }

    // ===============================发布/订阅=================================

    /**
     * 发布消息到指定频道
     * <p>
     * 用于Redis发布/订阅模式，发布者将消息发送到指定频道，订阅者可以接收消息
     * </p>
     *
     * @param channel 频道名称，不能为null
     * @param message 消息内容
     * @return 接收到消息的订阅者数量，异常时返回0
     */
    public Long publish(String channel, Object message) {
        if (channel == null) {
            return 0L;
        }
        try {
            redisTemplate.convertAndSend(channel, message);
            // convertAndSend 不返回订阅者数量，这里返回1表示成功
            return 1L;
        } catch (Exception e) {
            log.error("Redis发布消息失败，channel: {}, message: {}", channel, message, e);
            return 0L;
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
            log.error("Redis获取键列表失败，模式: {}", pattern, e);
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
            if (!keys.isEmpty()) {
                return redisTemplate.delete(keys);
            }
            return 0L;
        } catch (Exception e) {
            log.error("Redis按模式删除键失败，模式: {}", pattern, e);
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
            log.error("Redis重命名键失败，旧键: {}, 新键: {}", oldKey, newKey, e);
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
            log.error("Redis条件重命名键失败，旧键: {}, 新键: {}", oldKey, newKey, e);
            return false;
        }
    }

    // ===============================批量操作=================================

    /**
     * 批量获取多个 key 的值（MGET）
     * <p>
     * 使用 Redis MGET 命令批量获取多个 key 的值，减少网络往返次数。
     * 适用于批量获取缓存数据的场景。
     * </p>
     *
     * @param keys 键集合，不能为null或空
     * @return 值列表（与 keys 顺序对应，不存在的 key 返回 null），异常时返回与 keys 等长的 null 列表
     */
    public List<Object> mGet(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            List<Object> results = redisTemplate.opsForValue().multiGet(keys);
            if (results != null && results.size() == keys.size()) {
                return results;
            }
            log.warn("Redis批量获取值结果数量不匹配，期望: {}, 实际: {}", keys.size(), results != null ? results.size() : 0);
            return new ArrayList<>(Collections.nCopies(keys.size(), null));
        } catch (Exception e) {
            log.error("Redis批量获取值失败，keys数量: {}", keys.size(), e);
            return new ArrayList<>(Collections.nCopies(keys.size(), null));
        }
    }

    /**
     * 批量设置多个 key-value（MSET）
     * <p>
     * 使用 Redis MSET 命令批量设置多个 key-value，原子操作，减少网络往返次数。
     * </p>
     *
     * @param map key-value映射，不能为null或空
     * @return true成功 false失败
     */
    public boolean mSet(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return false;
        }
        try {
            redisTemplate.opsForValue().multiSet(map);
            return true;
        } catch (Exception e) {
            log.error("Redis批量设置值失败，keys数量: {}", map.size(), e);
            return false;
        }
    }

    /**
     * 批量设置多个 key-value，仅当所有key都不存在时设置（MSETNX）
     * <p>
     * 原子操作，只有当所有key都不存在时才会设置，否则不设置任何key。
     * </p>
     *
     * @param map key-value映射，不能为null或空
     * @return true成功（所有key都不存在） false失败（至少一个key已存在）
     */
    public boolean mSetIfAbsent(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return false;
        }
        try {
            Boolean result = redisTemplate.opsForValue().multiSetIfAbsent(map);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("Redis批量设置值（MSETNX）失败，keys数量: {}", map.size(), e);
            return false;
        }
    }

    /**
     * 批量获取多个 HASH 的长度（Pipeline 执行 HLEN）
     * <p>
     * 使用 Redis Pipeline 批量执行 HLEN 命令，一次网络往返获取多个 HASH 的元素数量。
     * </p>
     *
     * @param keys HASH 键列表，不能为null或空
     * @return 长度列表（与 keys 顺序对应，不存在的 key 返回 0），异常时返回全0列表
     */
    public List<Long> batchHLen(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            List<Object> results = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                for (String key : keys) {
                    if (key != null) {
                        connection.hashCommands().hLen(key.getBytes());
                    } else {
                        connection.hashCommands().hLen(new byte[0]);
                    }
                }
                return null;
            });
            if (results.size() != keys.size()) {
                log.warn("Redis批量获取HASH长度结果数量不匹配，期望: {}, 实际: {}", keys.size(), results.size());
                return keys.stream().map(k -> 0L).toList();
            }
            return results.stream().map(o -> o == null ? 0L : (Long) o).toList();
        } catch (Exception e) {
            log.error("Redis批量获取HASH长度失败，keys数量: {}", keys.size(), e);
            return keys.stream().map(k -> 0L).toList();
        }
    }

    /**
     * 批量检查多个 HASH 中是否存在指定 field（Pipeline 执行 HEXISTS）
     * <p>
     * 使用 Redis Pipeline 批量执行 HEXISTS 命令，一次网络往返检查多个状态。
     * </p>
     *
     * @param keys  HASH 键列表，不能为null或空
     * @param field 要检查的字段（如用户ID），不能为null
     * @return 存在状态列表（与 keys 顺序对应），异常时返回全false列表
     */
    public List<Boolean> batchHExists(List<String> keys, String field) {
        if (keys == null || keys.isEmpty() || field == null) {
            return keys != null && !keys.isEmpty() ? keys.stream().map(k -> false).toList() : Collections.emptyList();
        }
        try {
            byte[] fieldBytes = field.getBytes();
            List<Object> results = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                for (String key : keys) {
                    if (key != null) {
                        connection.hashCommands().hExists(key.getBytes(), fieldBytes);
                    } else {
                        connection.hashCommands().hExists(new byte[0], fieldBytes);
                    }
                }
                return null;
            });
            if (results.size() != keys.size()) {
                log.warn("Redis批量检查HASH字段是否存在结果数量不匹配，期望: {}, 实际: {}", keys.size(), results.size());
                return keys.stream().map(k -> false).toList();
            }
            return results.stream().map(o -> o != null && (Boolean) o).toList();
        } catch (Exception e) {
            log.error("Redis批量检查HASH字段是否存在失败，keys数量: {}, field: {}", keys.size(), field, e);
            return keys.stream().map(k -> false).toList();
        }
    }

    // ===============================位操作=================================

    /**
     * 设置位值（SETBIT）
     *
     * @param key    键，不能为null
     * @param offset 偏移量，从0开始
     * @param value  位值，true表示1，false表示0
     * @return 指定偏移量原来存储的位值，异常时返回false
     */
    public Boolean setBit(String key, long offset, boolean value) {
        if (key == null) {
            return false;
        }
        try {
            Boolean result = redisTemplate.opsForValue().setBit(key, offset, value);
            return result != null ? result : false;
        } catch (Exception e) {
            log.error("Redis设置位值失败，key: {}, offset: {}, value: {}", key, offset, value, e);
            return false;
        }
    }

    /**
     * 获取位值（GETBIT）
     *
     * @param key    键，不能为null
     * @param offset 偏移量，从0开始
     * @return 位值，true表示1，false表示0，key不存在返回false
     */
    public Boolean getBit(String key, long offset) {
        if (key == null) {
            return false;
        }
        try {
            Boolean result = redisTemplate.opsForValue().getBit(key, offset);
            return result != null ? result : false;
        } catch (Exception e) {
            log.error("Redis获取位值失败，key: {}, offset: {}", key, offset, e);
            return false;
        }
    }

    // ===============================Lua脚本执行=================================

    /**
     * 执行Lua脚本
     * <p>
     * Lua脚本在Redis服务器端原子执行，适用于复杂业务逻辑，如限流、计数器、分布式锁等场景。
     * </p>
     *
     * @param script     Lua脚本内容，不能为null
     * @param resultType 返回值类型
     * @param keys       key列表，对应脚本中的KEYS数组
     * @param args       参数列表，对应脚本中的ARGV数组
     * @param <T>        返回值类型
     * @return 脚本执行结果，异常时返回null
     */
    public <T> T executeScript(String script, Class<T> resultType, List<String> keys, Object... args) {
        if (script == null || script.isEmpty()) {
            return null;
        }
        try {
            DefaultRedisScript<T> redisScript = new DefaultRedisScript<>();
            redisScript.setScriptText(script);
            redisScript.setResultType(resultType);
            return redisTemplate.execute(redisScript, keys != null ? keys : Collections.emptyList(), args);
        } catch (Exception e) {
            log.error("Redis执行Lua脚本失败，script长度: {}, keys数量: {}", script.length(), keys != null ? keys.size() : 0, e);
            return null;
        }
    }

    /**
     * 执行Lua脚本（从资源文件加载）
     * <p>
     * 从classpath加载Lua脚本文件并执行
     * </p>
     *
     * @param scriptPath 脚本文件路径（相对于classpath），不能为null
     * @param resultType 返回值类型
     * @param keys       key列表，对应脚本中的KEYS数组
     * @param args       参数列表，对应脚本中的ARGV数组
     * @param <T>        返回值类型
     * @return 脚本执行结果，异常时返回null
     */
    public <T> T executeScriptFromResource(String scriptPath, Class<T> resultType, List<String> keys, Object... args) {
        if (scriptPath == null || scriptPath.isEmpty()) {
            return null;
        }
        try {
            DefaultRedisScript<T> redisScript = new DefaultRedisScript<>();
            redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource(scriptPath)));
            redisScript.setResultType(resultType);
            return redisTemplate.execute(redisScript, keys != null ? keys : Collections.emptyList(), args);
        } catch (Exception e) {
            log.error("Redis执行Lua脚本失败，scriptPath: {}, keys数量: {}", scriptPath, keys != null ? keys.size() : 0, e);
            return null;
        }
    }

    // ===============================事务支持=================================

    /**
     * 执行事务
     * <p>
     * 使用Redis事务（MULTI/EXEC）执行多个命令，保证原子性。
     * 注意：Redis事务不支持回滚，如果某个命令失败，其他命令仍会执行。
     * </p>
     *
     * @param callback 事务回调，不能为null
     * @return 事务执行结果列表
     */
    public List<Object> executeTransaction(SessionCallback<?> callback) {
        if (callback == null) {
            return Collections.emptyList();
        }
        try {
            return Collections.singletonList(redisTemplate.execute(callback));
        } catch (Exception e) {
            log.error("Redis执行事务失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 执行Pipeline操作
     * <p>
     * Pipeline可以将多个命令打包发送，减少网络往返次数，提高性能。
     * 注意：Pipeline不是事务，不保证原子性。
     * </p>
     *
     * @param callback Pipeline回调，不能为null
     * @return Pipeline执行结果列表
     */
    public List<Object> executePipeline(RedisCallback<?> callback) {
        if (callback == null) {
            return Collections.emptyList();
        }
        try {
            return redisTemplate.executePipelined(callback);
        } catch (Exception e) {
            log.error("Redis执行Pipeline失败", e);
            return Collections.emptyList();
        }
    }
}
