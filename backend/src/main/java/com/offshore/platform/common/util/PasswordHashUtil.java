package com.offshore.platform.common.util;

import com.offshore.platform.common.enums.ErrorCode;
import com.offshore.platform.common.exception.BusinessException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class PasswordHashUtil {
    private static final String PREFIX = "{pbkdf2}";
    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH = 256;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public String hash(String rawPassword) {
        if (!StringUtils.hasText(rawPassword)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "密码不能为空");
        }
        try {
            byte[] salt = new byte[16];
            SECURE_RANDOM.nextBytes(salt);
            byte[] hash = pbkdf2(rawPassword.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            return PREFIX + ITERATIONS + "$"
                    + Base64.getEncoder().encodeToString(salt) + "$"
                    + Base64.getEncoder().encodeToString(hash);
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "密码加密失败");
        }
    }

    public boolean matches(String rawPassword, String storedHash) {
        if (!StringUtils.hasText(rawPassword) || !StringUtils.hasText(storedHash)) {
            return false;
        }
        if (storedHash.startsWith(PREFIX)) {
            return matchesPbkdf2(rawPassword, storedHash);
        }
        if (storedHash.startsWith("{noop}")) {
            return storedHash.substring("{noop}".length()).equals(rawPassword);
        }
        // TODO: 正式环境接入 Spring Security PasswordEncoder/BCrypt 后移除初始化占位密码兼容逻辑。
        if (storedHash.startsWith("PLACEHOLDER_PASSWORD_HASH_USE_PROJECT_ENCODER_")) {
            return "123456".equals(rawPassword);
        }
        return storedHash.equals(rawPassword);
    }

    private boolean matchesPbkdf2(String rawPassword, String storedHash) {
        try {
            String text = storedHash.substring(PREFIX.length());
            String[] parts = text.split("\\$");
            if (parts.length != 3) {
                return false;
            }
            int iterations = Integer.parseInt(parts[0]);
            byte[] salt = Base64.getDecoder().decode(parts[1]);
            byte[] expected = Base64.getDecoder().decode(parts[2]);
            byte[] actual = pbkdf2(rawPassword.toCharArray(), salt, iterations, expected.length * 8);
            return constantTimeEquals(expected, actual);
        } catch (Exception ex) {
            return false;
        }
    }

    private byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLength) throws Exception {
        KeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
    }

    private boolean constantTimeEquals(byte[] left, byte[] right) {
        if (left.length != right.length) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < left.length; i++) {
            result |= left[i] ^ right[i];
        }
        return result == 0;
    }
}
