package com.techlooper.repository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

/**
 * Created by khoa-nd on 12/5/14.
 */
@Repository
public class JobSearchAPIConfigurationRepository {

    @Value("${vnw.api.configuration.url}")
    private String configurationUrl;

    @Value("${vnw.api.job.search.url}")
    private String searchUrl;

    @Value("${vnw.api.key.name}")
    private String apiKeyName;

    @Value("${vnw.api.key.value}")
    private String apiKeyValue;

    public String getConfigurationUrl() {
        return configurationUrl;
    }

    public void setConfigurationUrl(String configurationUrl) {
        this.configurationUrl = configurationUrl;
    }

    public String getSearchUrl() {
        return searchUrl;
    }

    public void setSearchUrl(String searchUrl) {
        this.searchUrl = searchUrl;
    }

    public String getApiKeyName() {
        return apiKeyName;
    }

    public void setApiKeyName(String apiKeyName) {
        this.apiKeyName = apiKeyName;
    }

    public String getApiKeyValue() {
        return apiKeyValue;
    }

    public void setApiKeyValue(String apiKeyValue) {
        this.apiKeyValue = apiKeyValue;
    }
}
