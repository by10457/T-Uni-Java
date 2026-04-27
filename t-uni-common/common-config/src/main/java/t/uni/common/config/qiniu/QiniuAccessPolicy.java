package t.uni.common.config.qiniu;

/**
 * 七牛空间访问策略。
 * <p>
 * PUBLIC 直接拼接公开访问地址；PRIVATE 需要生成带有效期的签名地址。
 * </p>
 */
public enum QiniuAccessPolicy {
    PUBLIC,
    PRIVATE
}
