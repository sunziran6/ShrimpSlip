package com.shrimpslip.app.user.controller;

import com.shrimpslip.app.user.dto.LoginRequest;
import com.shrimpslip.app.user.dto.LoginResponse;
import com.shrimpslip.app.user.dto.RegisterRequest;
import com.shrimpslip.app.user.dto.SendCodeRequest;
import com.shrimpslip.app.user.entity.User;
import com.shrimpslip.app.user.service.SmsService;
import com.shrimpslip.app.user.service.UserService;
import com.shrimpslip.common.core.Result;
import com.shrimpslip.common.security.JwtUtil;
import com.shrimpslip.common.security.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final SmsService smsService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @PostMapping("/send-code")
    public Result<Void> sendCode(@Valid @RequestBody SendCodeRequest req) {
        smsService.sendCode(req.getPhone());
        return Result.ok();
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        boolean hasPassword = req.getPassword() != null && !req.getPassword().isBlank();
        boolean hasCode = req.getCode() != null && !req.getCode().isBlank();

        if (!hasPassword && !hasCode) {
            return Result.fail(400, "请提供密码或验证码");
        }

        if (hasCode) {
            if (!smsService.verifyCode(req.getPhone(), req.getCode())) {
                return Result.fail(400, "验证码错误或已过期");
            }
        }

        return Result.ok(userService.login(req.getPhone(),
                hasPassword ? req.getPassword() : null,
                hasCode ? req.getCode() : null));
    }

    @PostMapping("/register")
    public Result<LoginResponse> register(@Valid @RequestBody RegisterRequest req) {
        return Result.ok(userService.register(req));
    }

    @PostMapping("/refresh")
    public Result<Map<String, String>> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        if (refreshToken == null) {
            return Result.fail(400, "缺少 refreshToken");
        }
        try {
            Long userId = jwtUtil.getUserId(refreshToken);
            String newAccessToken = jwtUtil.generateAccessToken(userId);
            return Result.ok(Map.of("accessToken", newAccessToken));
        } catch (Exception e) {
            return Result.fail(401, "Token 无效或已过期");
        }
    }

    @GetMapping("/profile")
    public Result<User> profile() {
        Long userId = UserContext.get();
        return Result.ok(userService.getById(userId));
    }

    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody Map<String, String> body) {
        Long userId = UserContext.get();
        userService.updateProfile(userId, body.get("nickname"), body.get("avatar"));
        return Result.ok();
    }
}
