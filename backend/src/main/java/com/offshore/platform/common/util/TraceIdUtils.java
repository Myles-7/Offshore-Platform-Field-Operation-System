package com.offshore.platform.common.util;

import java.util.UUID;
import org.slf4j.MDC;

public final class TraceIdUtils {
    public static final String TRACE_ID_KEY = "traceId";
    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    private TraceIdUtils() {
    }

    public static String newTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String currentTraceId() {
        return MDC.get(TRACE_ID_KEY);
    }
}
