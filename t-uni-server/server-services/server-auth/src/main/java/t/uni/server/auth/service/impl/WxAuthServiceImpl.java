package t.uni.server.auth.service.impl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import t.uni.common.core.exception.BaseException;
import t.uni.common.core.result.ResultCodeEnum;
import t.uni.server.auth.mapper.ServerUserMapper;
import t.uni.server.auth.service.WxAuthService;
import t.uni.server.domain.dto.auth.WxLoginDTO;
import t.uni.server.domain.entity.ServerUser;
import t.uni.server.domain.vo.auth.WxLoginVO;
import t.uni.common.core.utils.JwtTokenUtil;

/**
 * 微信认证服务实现
 */
@Slf4j
@Service
public class WxAuthServiceImpl implements WxAuthService {

    @Resource
    private WxMaService wxMaService;

    @Resource
    private ServerUserMapper serverUserMapper;

    /**
     * 微信小程序登录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public WxLoginVO wxLogin(WxLoginDTO dto) {
        String code = dto.getCode();

        try {
            // 1. 调用微信API获取session信息
            WxMaJscode2SessionResult sessionInfo = wxMaService.getUserService().getSessionInfo(code);
            String openid = sessionInfo.getOpenid();
            String sessionKey = sessionInfo.getSessionKey();
            String unionid = sessionInfo.getUnionid();

            log.info("微信登录成功，openid: {}", openid);

            // 2. 查询用户是否存在
            LambdaQueryWrapper<ServerUser> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ServerUser::getOpenid, openid);
            ServerUser user = serverUserMapper.selectOne(queryWrapper);

            boolean isNewUser = false;
            if (user == null) {
                // 3. 创建新用户
                user = new ServerUser();
                user.setOpenid(openid);
                user.setUnionid(unionid);
                user.setSessionKey(sessionKey);
                user.setStatus(1);
                serverUserMapper.insert(user);
                isNewUser = true;
                log.info("创建新用户，openid: {}, userId: {}", openid, user.getId());
            } else {
                // 4. 更新session_key
                user.setSessionKey(sessionKey);
                if (StrUtil.isNotBlank(unionid)) {
                    user.setUnionid(unionid);
                }
                serverUserMapper.updateById(user);
                log.info("更新用户session_key，userId: {}", user.getId());
            }

            // 5. 生成token（这里简化处理，实际应该使用JWT）
            String token = generateToken(user.getId(), openid);

            // 6. 返回登录结果
            return WxLoginVO.builder()
                    .userId(user.getId())
                    .token(token)
                    .openid(openid)
                    .nickname(user.getNickname())
                    .avatar(user.getAvatar())
                    .isNewUser(isNewUser)
                    .build();

        } catch (WxErrorException e) {
            log.error("微信登录失败，code: {}, error: {}", code, e.getMessage(), e);
            throw new BaseException(ResultCodeEnum.SERVICE_ERROR.getCode(), "微信登录失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户手机号
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String getPhoneNumber(String code, Long userId) {
        try {
            // 1. 调用微信API获取手机号
            WxMaPhoneNumberInfo phoneNumberInfo = wxMaService.getUserService().getPhoneNumber(code);
            String phoneNumber = phoneNumberInfo.getPhoneNumber();

            log.info("获取手机号成功，userId: {}, phone: {}", userId, phoneNumber);

            // 2. 更新用户手机号
            ServerUser user = serverUserMapper.selectById(userId);
            if (user != null) {
                user.setPhone(phoneNumber);
                serverUserMapper.updateById(user);
            }

            return phoneNumber;

        } catch (WxErrorException e) {
            log.error("获取手机号失败，userId: {}, error: {}", userId, e.getMessage(), e);
            throw new BaseException(ResultCodeEnum.SERVICE_ERROR.getCode(), "获取手机号失败：" + e.getMessage());
        }
    }

    /**
     * 生成JWT token
     */
    private String generateToken(Long userId, String openid) {
        return JwtTokenUtil.createToken(userId, openid);
    }
}
