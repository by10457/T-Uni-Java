package t.uni.domain.common.enums;


import lombok.Getter;

@Getter
public enum LoginEnums {
    // 默认登录请求
    default_STRATEGY("default"),
    ;

    private final String value;

    LoginEnums(String value) {
        this.value = value;
    }
}
