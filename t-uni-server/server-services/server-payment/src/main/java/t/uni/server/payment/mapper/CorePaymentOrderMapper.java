package t.uni.server.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import t.uni.server.payment.entity.CorePaymentOrder;

/**
 * 支付主订单数据访问。
 */
@Mapper
public interface CorePaymentOrderMapper extends BaseMapper<CorePaymentOrder> {
}
