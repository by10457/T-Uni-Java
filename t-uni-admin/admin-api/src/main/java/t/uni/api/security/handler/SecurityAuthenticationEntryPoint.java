package t.uni.api.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import t.uni.api.security.exception.CustomAuthenticationException;
import t.uni.common.core.result.Result;
import t.uni.common.core.result.ResultCodeEnum;
import t.uni.core.utils.ResponseUtil;

/**
 * 自定义请求未认证接口异常
 */
@Slf4j
public class SecurityAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) {
        Result<Object> result;

        if (authException instanceof CustomAuthenticationException customException) {
            result = Result.error(customException.getResultCodeEnum());
        } else {
            result = Result.error(ResultCodeEnum.FAIL_NO_ACCESS_DENIED);
        }

        ResponseUtil.out(response, result);
    }
}
