package com.shrimpslip.app.user.service;

import com.shrimpslip.app.user.dto.LoginResponse;
import com.shrimpslip.app.user.dto.RegisterRequest;
import com.shrimpslip.app.user.entity.User;

public interface UserService {
    LoginResponse login(String phone, String password, String code);
    LoginResponse register(RegisterRequest req);
    User getById(Long userId);
    void updateProfile(Long userId, String nickname, String avatar);
}
