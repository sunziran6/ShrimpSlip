package com.shrimpslip.app.user.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.shrimpslip.app.user.entity.SmsCode;
import com.shrimpslip.app.user.mapper.SmsCodeMapper;
import com.shrimpslip.app.user.service.SmsService;
import com.shrimpslip.common.core.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsServiceImpl implements SmsService {

    private final StringRedisTemplate redisTemplate;
    private final SmsCodeMapper smsCodeMapper;

    private static final String REDIS_KEY_PREFIX = "sms:code:";
    private static final long CODE_EXPIRE_MINUTES = 5;
    private static final long SEND_INTERVAL_SECONDS = 60;

    @Override
    public void sendCode(String phone) {
        // 60s 内不能重复发送
        String rateKey = REDIS_KEY_PREFIX + "rate:" + phone;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(rateKey))) {
            throw new BizException(429, "请60秒后再试");
        }

        String code = RandomUtil.randomNumbers(6);
        log.info("【验证码】手机号: {}, 验证码: {}", phone, code);

        // 存 Redis，5 分钟过期
        redisTemplate.opsForValue().set(REDIS_KEY_PREFIX + phone, code,
                CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(rateKey, "1",
                SEND_INTERVAL_SECONDS, TimeUnit.SECONDS);

        // 存 DB
        SmsCode smsCode = new SmsCode();
        smsCode.setPhone(phone);
        smsCode.setCode(code);
        smsCode.setType(1); // 登录/注册通用
        smsCode.setUsed(0);
        smsCode.setExpiresAt(LocalDateTime.now().plusMinutes(CODE_EXPIRE_MINUTES));
        smsCode.setCreatedAt(LocalDateTime.now());
        smsCodeMapper.insert(smsCode);
    }

    @Override
    public boolean verifyCode(String phone, String code) {
        String cached = redisTemplate.opsForValue().get(REDIS_KEY_PREFIX + phone);
        return code.equals(cached);
    }
}
