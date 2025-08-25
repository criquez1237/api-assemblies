package com.assembliestore.api.common.response;

import java.time.Instant;

public class TechnicalDetails {
    private String timestamp;
    private String path;
    private String method;
    private Long durationMs;
    private String debug; // only present in non-production when enabled

    public TechnicalDetails() {}

    public TechnicalDetails(String path, String method, Long durationMs, String debug) {
        this.timestamp = Instant.now().toString();
        this.path = path;
        this.method = method;
        this.durationMs = durationMs;
        this.debug = debug;
    }

    public String getTimestamp() { return timestamp; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }
    public String getDebug() { return debug; }
    public void setDebug(String debug) { this.debug = debug; }
}
