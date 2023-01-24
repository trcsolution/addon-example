package com.trc.ccopromo;

public class PluginConfig {
    private String baseUrl;
    private String apiKey;
    private Boolean secure;
    private Boolean advreturn;
    // private String password;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getAPIKey() {
        return apiKey;

    }
    public void setAPIKey(String apiKey) {
        this.apiKey = apiKey;
    }


    public void setSecure(Boolean secure) {
        this.secure = secure;
    }
    public Boolean getSecure() {
        return secure;

    }
    public void setAdvreturn(Boolean advreturn) {
        this.advreturn = advreturn;
    }
    public Boolean getAdvreturn() {
        return advreturn;

    }

}
