package t.uni.server.auth.util;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import t.uni.common.core.enums.TrueFalseEnum;
import t.uni.server.auth.mapper.CoreUserMapper;
import t.uni.server.domain.entity.CoreUser;

import java.security.SecureRandom;

/**
 * 用户唯一ID生成器
 * <p>
 * 生成8位数字的唯一ID，带数据库冲突检测
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UniqueIdGenerator {

    private static final String DIGITS = "0123456789";
    private static final int ID_LENGTH = 8;
    private static final int MAX_ATTEMPTS = 10;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final CoreUserMapper coreUserMapper;

    /**
     * 生成唯一ID
     *
     * @return 8位数字唯一ID
     * @throws IllegalStateException 如果多次尝试后仍无法生成唯一ID
     */
    public String generate() {
        for (var attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            var uniqueId = generateRandomCode();
            if (!isCodeExists(uniqueId)) {
                return uniqueId;
            }
            log.warn("生成唯一ID重复：{}，尝试第{}次", uniqueId, attempt + 1);
        }
        throw new IllegalStateException("无法生成唯一ID");
    }

    /**
     * 生成8位随机数字
     */
    private String generateRandomCode() {
        var sb = new StringBuilder(ID_LENGTH);
        for (int i = 0; i < ID_LENGTH; i++) {
            sb.append(DIGITS.charAt(RANDOM.nextInt(DIGITS.length())));
        }
        return sb.toString();
    }

    /**
     * 检查唯一ID是否已存在
     */
    private boolean isCodeExists(String uniqueId) {
        return coreUserMapper.exists(Wrappers.<CoreUser>lambdaQuery()
                .eq(CoreUser::getUniqueId, uniqueId)
                .eq(CoreUser::getIsDestroy, TrueFalseEnum.FALSE.getStatus()));
    }
}
