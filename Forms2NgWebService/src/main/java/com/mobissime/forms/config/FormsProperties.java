package com.mobissime.forms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FormsProperties {

    @Value("${forms2ng.path:/opt/forms2ng/}")
    private String formsPath;

    @Value("${forms2ng.cors.allowed-origins:*}")
    private String corsAllowedOrigins;

    public String getFormsPath() {
        return formsPath;
    }

    public void setFormsPath(String formsPath) {
        this.formsPath = formsPath;
    }

    public String getCorsAllowedOrigins() {
        return corsAllowedOrigins;
    }
}
