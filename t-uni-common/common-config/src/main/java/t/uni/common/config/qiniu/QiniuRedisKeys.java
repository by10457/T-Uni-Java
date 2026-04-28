package t.uni.common.config.qiniu;

/**
 * 七牛下载地址缓存逻辑 Key。
 * <p>
 * 仅生成 Redis 逻辑 key，物理 namespace 由 common Redis 配置统一处理。
 * </p>
 */
public final class QiniuRedisKeys {

    private static final String QINIU_DOWNLOAD_URL = "qiniu:download:url:{}";

    private QiniuRedisKeys() {
    }

    /**
     * 生成七牛下载链接缓存 key。
     *
     * @param cacheId 下载链接参数摘要
     * @return Redis key
     */
    public static String qiniuDownloadUrl(String cacheId) {
        return QINIU_DOWNLOAD_URL.replace("{}", cacheId);
    }
}
