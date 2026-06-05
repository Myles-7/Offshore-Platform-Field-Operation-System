package com.offshore.platform.common.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.offshore.platform.common.enums.ErrorCode;
import com.offshore.platform.common.exception.BusinessException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenUtil {
    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();

    private final ObjectMapper objectMapper;
    private final String secret;
    private final long expirationSeconds;

    public JwtTokenUtil(ObjectMapper objectMapper,
            @Value("${app.jwt.secret:offshore-platform-dev-secret-change-me}") String secret,
            @Value("${app.jwt.expiration-seconds:86400}") long expirationSeconds) {
        this.objectMapper = objectMapper;
        this.secret = secret;
        this.expirationSeconds = expirationSeconds;
    }

    public String generateToken(Long userId, String username) {
        try {
            Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
            long now = Instant.now().getEpochSecond();
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("sub", String.valueOf(userId));
            payload.put("username", username);
            payload.put("iat", now);
            payload.put("exp", now + expirationSeconds);

            String headerText = base64Url(objectMapper.writeValueAsBytes(header));
            String payloadText = base64Url(objectMapper.writeValueAsBytes(payload));
            String signingInput = headerText + "." + payloadText;
            return signingInput + "." + sign(signingInput);
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成登录令牌失败");
        }
    }

    public Long parseUserId(String token) {
        Map<String, Object> payload = parsePayload(token);
        Object subject = payload.get("sub");
        if (subject == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return Long.valueOf(String.valueOf(subject));
    }

    private Map<String, Object> parsePayload(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED);
            }
            String signingInput = parts[0] + "." + parts[1];
            if (!constantTimeEquals(sign(signingInput), parts[2])) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED, "token签名无效");
            }
            Map<String, Object> payload = objectMapper.readValue(URL_DECODER.decode(parts[1]),
                    new TypeReference<Map<String, Object>>() {
                    });
            long exp = Long.parseLong(String.valueOf(payload.get("exp")));
            if (Instant.now().getEpochSecond() > exp) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED, "token已过期");
            }
            return payload;
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "token解析失败");
        }
    }

    private String sign(String text) throws Exception {
        Mac mac = Mac.getInstance(HMAC_SHA256);
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
        return base64Url(mac.doFinal(text.getBytes(StandardCharsets.UTF_8)));
    }

    private String base64Url(byte[] bytes) {
        return URL_ENCODER.encodeToString(bytes);
    }

    private boolean constantTimeEquals(String left, String right) {
        byte[] leftBytes = left.getBytes(StandardCharsets.UTF_8);
        byte[] rightBytes = right.getBytes(StandardCharsets.UTF_8);
        if (leftBytes.length != rightBytes.length) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < leftBytes.length; i++) {
            result |= leftBytes[i] ^ rightBytes[i];
        }
        return result == 0;
    }
}
