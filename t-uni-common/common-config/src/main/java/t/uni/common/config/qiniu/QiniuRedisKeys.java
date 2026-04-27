package t.uni.common.config.qiniu;

/**
 * 七牛下载地址缓存 Key。
 * <p>
 * 仅生成命名空间和缓存 key，不负责缓存读写。
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
