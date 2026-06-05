package com.offshore.platform.config;

import com.offshore.platform.common.util.TraceIdUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class TraceIdFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String traceId = request.getHeader(TraceIdUtils.TRACE_ID_HEADER);
        if (!StringUtils.hasText(traceId)) {
            traceId = TraceIdUtils.newTraceId();
        }
        MDC.put(TraceIdUtils.TRACE_ID_KEY, traceId);
        response.setHeader(TraceIdUtils.TRACE_ID_HEADER, traceId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(TraceIdUtils.TRACE_ID_KEY);
        }
    }
}
