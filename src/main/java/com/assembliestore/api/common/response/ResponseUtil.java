package com.assembliestore.api.common.response;

import jakarta.servlet.http.HttpServletRequest;
import com.assembliestore.api.config.AppEnvConfig;

public class ResponseUtil {

    public static TechnicalDetails buildTechnicalDetails(HttpServletRequest request, long durationMs, AppEnvConfig env) {
        String debug = null;
        if (env != null && !env.isProduction()) {
            // Provide a short debug snippet only when not production
            debug = "Request received and validated; durationMs=" + durationMs + "ms";
        }

        TechnicalDetails tech = new TechnicalDetails(
            request != null ? request.getRequestURI() : "-",
            request != null ? request.getMethod() : "-",
            durationMs,
            debug
        );

        return tech;
    }
}
