package t.uni.server.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import t.uni.server.payment.entity.CorePaymentRefund;

/**
 * 退款单数据访问。
 */
@Mapper
public interface CorePaymentRefundMapper extends BaseMapper<CorePaymentRefund> {
}
