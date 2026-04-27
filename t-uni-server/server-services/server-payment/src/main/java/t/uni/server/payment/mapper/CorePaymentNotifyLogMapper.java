package t.uni.server.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import t.uni.server.payment.entity.CorePaymentNotifyLog;

/**
 * 支付/退款回调日志数据访问。
 */
@Mapper
public interface CorePaymentNotifyLogMapper extends BaseMapper<CorePaymentNotifyLog> {
}
