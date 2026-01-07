package t.uni.api.security.service;

import com.alibaba.fastjson2.JSON;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import t.uni.api.security.exception.CustomAuthenticationException;
import t.uni.common.core.result.ResultCodeEnum;
import t.uni.core.utils.JwtTokenUtil;
import t.uni.domain.common.constant.RedisUserConstant;
import t.uni.domain.common.model.dto.security.TokenInfo;
import t.uni.domain.common.model.vo.LoginVo;

/**
 * 处理Token相关逻辑
 */
@Service
public class TokenValidationService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    public TokenInfo validateToken(HttpServletRequest request) {
        // 判断是否有 token
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new CustomAuthenticationException(ResultCodeEnum.LOGIN_AUTH);
        }

        String token = header.substring(7);
        // 判断 token 是否过期
        if (JwtTokenUtil.isExpired(token)) {
            throw new CustomAuthenticationException(ResultCodeEnum.AUTHENTICATION_EXPIRED);
        }

        // 解析JWT中的用户名
        String username = JwtTokenUtil.getUsername(token);
        Long userId = JwtTokenUtil.getUserId(token);

        // 查找 Redis
        Object loginVoObject = redisTemplate.opsForValue().get(RedisUserConstant.getUserLoginInfoPrefix(username));
        LoginVo loginVo = JSON.parseObject(JSON.toJSONString(loginVoObject), LoginVo.class);

        return TokenInfo.builder().userId(userId).username(username).token(token).loginVo(loginVo).build();
    }
}
