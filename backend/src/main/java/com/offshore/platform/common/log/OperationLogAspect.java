package com.offshore.platform.common.log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.offshore.platform.common.context.CurrentUser;
import com.offshore.platform.common.context.CurrentUserContext;
import com.offshore.platform.common.util.TraceIdUtils;
import com.offshore.platform.mapper.OperationLogMapper;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Locale;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

@Aspect
@Component
public class OperationLogAspect {
    private static final Logger log = LoggerFactory.getLogger(OperationLogAspect.class);
    private static final int MAX_BODY_LENGTH = 4000;

    private final OperationLogMapper operationLogMapper;
    private final ObjectMapper objectMapper;

    public OperationLogAspect(OperationLogMapper operationLogMapper, ObjectMapper objectMapper) {
        this.operationLogMapper = operationLogMapper;
        this.objectMapper = objectMapper;
    }

    @Around("@annotation(annotation)")
    public Object around(ProceedingJoinPoint joinPoint, OperationLog annotation) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            write(joinPoint, annotation, "SUCCESS", null, null, start);
            return result;
        } catch (Throwable ex) {
            write(joinPoint, annotation, "FAILED", ex.getClass().getSimpleName(), ex.getMessage(), start);
            throw ex;
        }
    }

    private void write(ProceedingJoinPoint joinPoint, OperationLog annotation, String status, String errorCode,
            String errorMessage, long start) {
        try {
            HttpServletRequest request = currentRequest();
            com.offshore.platform.entity.OperationLog entity = new com.offshore.platform.entity.OperationLog();
            CurrentUser user = CurrentUserContext.get();
            entity.setTraceId(TraceIdUtils.currentTraceId());
            if (user != null) {
                entity.setOperatorId(user.getUserId());
                entity.setOperatorName(user.getRealName());
                entity.setRoleCode(String.join(",", user.getRoleCodes()));
                entity.setCreatedBy(user.getUserId());
                entity.setUpdatedBy(user.getUserId());
            }
            entity.setPlatform(resolvePlatform(annotation, request));
            entity.setModuleName(annotation.module());
            entity.setOperationType(annotation.operation());
            entity.setBusinessType(annotation.businessType());
            entity.setBusinessId(resolveBusinessId(request));
            entity.setProjectId(resolveProjectId(request));
            entity.setRequestMethod(request == null ? null : request.getMethod());
            entity.setRequestPath(request == null ? null : request.getRequestURI());
            entity.setRequestIp(request == null ? null : request.getRemoteAddr());
            entity.setUserAgent(request == null ? null : request.getHeader("User-Agent"));
            entity.setRequestBody(sanitizeArgs(joinPoint.getArgs()));
            entity.setResultStatus(status);
            entity.setErrorCode(errorCode);
            entity.setErrorMessage(truncate(errorMessage, 500));
            entity.setOperationTime(LocalDateTime.now());
            entity.setCreatedAt(LocalDateTime.now());
            entity.setUpdatedAt(LocalDateTime.now());
            entity.setDeletedFlag(0);
            entity.setRemark("costMs=" + (System.currentTimeMillis() - start));
            operationLogMapper.insert(entity);
        } catch (Exception logException) {
            log.warn("operation log write failed: {}", logException.getMessage());
        }
    }

    private HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }
        return null;
    }

    private String resolvePlatform(OperationLog annotation, HttpServletRequest request) {
        if (annotation.platform() != null && !annotation.platform().isBlank()) {
            return annotation.platform();
        }
        if (request != null && request.getRequestURI() != null && request.getRequestURI().contains("/mobile/")) {
            return "MOBILE";
        }
        return "PC";
    }

    private String resolveBusinessId(HttpServletRequest request) {
        if (request == null || request.getRequestURI() == null) {
            return null;
        }
        String uri = request.getRequestURI();
        String[] tokens = uri.split("/");
        for (int i = 0; i < tokens.length; i++) {
            if ("work-orders".equals(tokens[i]) && i + 1 < tokens.length && tokens[i + 1].matches("\\d+")) {
                return tokens[i + 1];
            }
        }
        for (int i = tokens.length - 1; i >= 0; i--) {
            if (tokens[i].matches("\\d+")) {
                return tokens[i];
            }
        }
        return null;
    }

    private Long resolveProjectId(HttpServletRequest request) {
        return null;
    }

    private String sanitizeArgs(Object[] args) {
        try {
            ArrayNode array = objectMapper.createArrayNode();
            for (Object arg : args) {
                if (arg == null || arg instanceof ServletRequest || arg instanceof ServletResponse
                        || arg instanceof MultipartFile || arg instanceof Resource || arg instanceof byte[]) {
                    continue;
                }
                array.add(sanitize(objectMapper.valueToTree(arg)));
            }
            return truncate(objectMapper.writeValueAsString(array), MAX_BODY_LENGTH);
        } catch (Exception ex) {
            return "[unserializable]";
        }
    }

    private JsonNode sanitize(JsonNode node) {
        if (node == null) {
            return null;
        }
        if (node.isObject()) {
            ObjectNode object = (ObjectNode) node;
            Iterator<String> names = object.fieldNames();
            while (names.hasNext()) {
                String name = names.next();
                String lowered = name.toLowerCase(Locale.ROOT);
                if (lowered.contains("password") || lowered.contains("token") || lowered.contains("secret")
                        || lowered.contains("base64") || lowered.contains("filecontent") || lowered.contains("binary")) {
                    object.put(name, "***");
                } else {
                    sanitize(object.get(name));
                }
            }
        } else if (node.isArray()) {
            for (JsonNode child : node) {
                sanitize(child);
            }
        }
        return node;
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
