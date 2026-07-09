package com.shrimpslip.app.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shrimpslip.app.user.dto.LoginResponse;
import com.shrimpslip.app.user.dto.RegisterRequest;
import com.shrimpslip.app.user.entity.User;
import com.shrimpslip.app.user.mapper.UserMapper;
import com.shrimpslip.app.user.service.UserService;
import com.shrimpslip.common.core.BizException;
import com.shrimpslip.common.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginResponse login(String phone, String password, String code) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
        if (user == null) {
            throw new BizException(400, "用户不存在，请先注册");
        }

        if (password != null) {
            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new BizException(400, "密码错误");
            }
        } else if (code == null) {
            throw new BizException(400, "请提供密码或验证码");
        }

        user.setLastLoginAt(LocalDateTime.now());
        userMapper.updateById(user);

        String accessToken = jwtUtil.generateAccessToken(user.getId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        return new LoginResponse(accessToken, refreshToken, user.getId(),
                user.getNickname(), user.getAvatar(), user.getRole());
    }

    @Override
    public LoginResponse register(RegisterRequest req) {
        User existing = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getPhone, req.getPhone()));
        if (existing != null) {
            throw new BizException(400, "该手机号已注册");
        }

        User user = new User();
        user.setPhone(req.getPhone());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setNickname(req.getNickname());
        user.setRole("USER");
        user.setStatus(1);
        user.setCreatedAt(LocalDateTime.now());
        user.setLastLoginAt(LocalDateTime.now());
        userMapper.insert(user);

        String accessToken = jwtUtil.generateAccessToken(user.getId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        return new LoginResponse(accessToken, refreshToken, user.getId(),
                user.getNickname(), user.getAvatar(), user.getRole());
    }

    @Override
    public User getById(Long userId) {
        return userMapper.selectById(userId);
    }

    @Override
    public void updateProfile(Long userId, String nickname, String avatar) {
        User user = new User();
        user.setId(userId);
        user.setNickname(nickname);
        user.setAvatar(avatar);
        userMapper.updateById(user);
    }
}
