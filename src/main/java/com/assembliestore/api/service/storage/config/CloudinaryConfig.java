package com.assembliestore.api.service.storage.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@ConfigurationProperties(prefix = "cloudinary")
public class CloudinaryConfig {

    private static final Logger logger = LoggerFactory.getLogger(CloudinaryConfig.class);

    private String cloudName;
    private String apiKey;
    private String apiSecret;
    private boolean secure = true;

    public String getCloudName() {
        return cloudName;
    }

    public void setCloudName(String cloudName) {
        this.cloudName = cloudName;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiSecret() {
        return apiSecret;
    }

    public void setApiSecret(String apiSecret) {
        this.apiSecret = apiSecret;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    @Bean
    public Cloudinary cloudinary() {
        logger.info("Configurando Cloudinary - Cloud Name: {}", cloudName);
        
        return new Cloudinary(ObjectUtils.asMap(
            "cloud_name", cloudName,
            "api_key", apiKey,
            "api_secret", apiSecret,
            "secure", secure
        ));
    }
}
