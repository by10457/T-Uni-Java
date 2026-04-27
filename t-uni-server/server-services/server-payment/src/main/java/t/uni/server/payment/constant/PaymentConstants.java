package t.uni.server.payment.constant;

/**
 * 支付模板常量。
 *
 * <p>状态值会落库并参与条件更新，新增或调整时需要同步数据库字典和业务判断。</p>
 */
public final class PaymentConstants {

    private PaymentConstants() {
    }

    public static final int PAY_CHANNEL_WECHAT = 1;

    public static final String CURRENCY_CNY = "CNY";
    public static final String TRADE_TYPE_JSAPI = "JSAPI";

    /**
     * 支付单状态。
     *
     * <p>主流程：CREATED -> PREPAY_SUCCESS -> PAID -> REFUNDING -> PARTIAL_REFUNDED/REFUNDED；
     * 未支付超时可从 CREATED/PREPAY_SUCCESS -> CLOSED，查单兜底允许 CLOSED -> PAID 修复微信已支付结果。</p>
     */
    public static final class OrderStatus {
        /** 已创建本地支付单，尚未完成微信预下单。 */
        public static final int CREATED = 0;
        /** 微信预下单成功，可调起收银台。 */
        public static final int PREPAY_SUCCESS = 1;
        /** 微信确认支付成功，业务侧可履约。 */
        public static final int PAID = 2;
        /** 未支付订单已关闭，业务侧应释放锁定资源。 */
        public static final int CLOSED = 3;
        /** 正在退款，作为并发退款申请的本地锁。 */
        public static final int REFUNDING = 4;
        /** 已全额退款。 */
        public static final int REFUNDED = 5;
        /** 支付失败保留状态，当前主流程不主动写入。 */
        public static final int PAY_FAILED = 6;
        /** 已部分退款，仍可能继续申请剩余金额退款。 */
        public static final int PARTIAL_REFUNDED = 7;

        private OrderStatus() {
        }
    }

    /**
     * 微信交易流水状态。
     */
    public static final class TransactionStatus {
        public static final int INIT = 0;
        public static final int PREPAY_SUCCESS = 1;
        public static final int PAY_SUCCESS = 2;
        public static final int PAY_FAILED = 3;
        public static final int CLOSED = 4;
        public static final int REFUNDED = 5;

        private TransactionStatus() {
        }
    }

    /**
     * 退款单状态。
     *
     * <p>APPLYING/PROCESSING 是可被微信回调继续推进的中间态；SUCCESS/FAILED/CLOSED 是终态。</p>
     */
    public static final class RefundStatus {
        public static final int APPLYING = 0;
        public static final int PROCESSING = 1;
        public static final int SUCCESS = 2;
        public static final int FAILED = 3;
        public static final int CLOSED = 4;

        private RefundStatus() {
        }
    }

    /**
     * 微信回调类型。
     */
    public static final class NotifyType {
        public static final int PAYMENT = 1;
        public static final int REFUND = 2;

        private NotifyType() {
        }
    }

    /**
     * 回调日志处理状态。
     *
     * <p>SUCCESS 和 IGNORED 都表示该通知不需要微信继续重试。</p>
     */
    public static final class NotifyProcessStatus {
        public static final int RECEIVED = 0;
        public static final int SUCCESS = 1;
        public static final int IGNORED = 2;
        public static final int FAILED = 3;

        private NotifyProcessStatus() {
        }
    }
}
