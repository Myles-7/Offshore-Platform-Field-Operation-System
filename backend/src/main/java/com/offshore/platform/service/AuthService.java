package com.offshore.platform.service;

import com.offshore.platform.common.context.CurrentUser;
import com.offshore.platform.dto.auth.LoginRequest;
import com.offshore.platform.vo.auth.CurrentUserVO;
import com.offshore.platform.vo.auth.LoginResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    LoginResponse login(LoginRequest request, HttpServletRequest servletRequest);

    void logout(HttpServletRequest servletRequest);

    CurrentUserVO current();

    CurrentUser loadCurrentUserByToken(String token);
}
