package t.uni.common.config.qiniu;

/**
 * 七牛下载地址缓存 Key。
 */
public final class QiniuRedisKeys {

    private static final String QINIU_DOWNLOAD_URL = "qiniu:download:url:{}";

    private QiniuRedisKeys() {
    }

    public static String qiniuDownloadUrl(String cacheId) {
        return QINIU_DOWNLOAD_URL.replace("{}", cacheId);
    }
}
