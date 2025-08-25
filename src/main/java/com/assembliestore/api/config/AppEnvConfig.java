package com.assembliestore.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.env")
public class AppEnvConfig {
    private boolean production = false;
    private boolean test = true;
    private boolean development = true;

    public boolean isProduction() { return production; }
    public void setProduction(boolean production) { this.production = production; }
    public boolean isTest() { return test; }
    public void setTest(boolean test) { this.test = test; }
    public boolean isDevelopment() { return development; }
    public void setDevelopment(boolean development) { this.development = development; }
}
