package t.uni.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import t.uni.domain.system.entity.UserDept;
import t.uni.system.mapper.UserDeptMapper;
import t.uni.system.service.UserDeptService;

/**
 * <p>
 * 部门用户关系表 服务实现类
 * </p>
 *
 * @since 2024-10-04
 */
@Service
public class UserDeptServiceImpl extends ServiceImpl<UserDeptMapper, UserDept> implements UserDeptService {

}
