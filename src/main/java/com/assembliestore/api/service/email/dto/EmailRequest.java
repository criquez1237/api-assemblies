package com.assembliestore.api.service.email.dto;

import java.util.Map;

public class EmailRequest {
    private String to;
    private String subject;
    private String templateName;
    private Map<String, Object> variables;
    private String from;

    public EmailRequest() {
        this.from = "Assemblies Store <noreply@assembliesstore.com>";
    }

    public EmailRequest(String to, String subject, String templateName, Map<String, Object> variables) {
        this();
        this.to = to;
        this.subject = subject;
        this.templateName = templateName;
        this.variables = variables;
    }

    // Getters y Setters
    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
}
