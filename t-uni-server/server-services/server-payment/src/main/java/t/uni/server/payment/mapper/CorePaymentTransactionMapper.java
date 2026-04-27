package t.uni.server.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import t.uni.server.payment.entity.CorePaymentTransaction;

/**
 * 支付交易流水数据访问。
 */
@Mapper
public interface CorePaymentTransactionMapper extends BaseMapper<CorePaymentTransaction> {
}
