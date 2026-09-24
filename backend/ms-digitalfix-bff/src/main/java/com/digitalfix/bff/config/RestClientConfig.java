package com.digitalfix.bff.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Value("${digitalfix.services.workorders.base-url}")
    private String workOrdersBaseUrl;

    @Value("${digitalfix.services.catalog.base-url}")
    private String catalogBaseUrl;

    @Value("${digitalfix.services.audit.base-url}")
    private String auditBaseUrl;

    @Value("${digitalfix.services.report.base-url}")
    private String reportBaseUrl;

    @Bean
    public RestClient workOrdersRestClient(RestClient.Builder builder) {
        return builder.baseUrl(workOrdersBaseUrl).build();
    }

    @Bean
    public RestClient catalogRestClient(RestClient.Builder builder) {
        return builder.baseUrl(catalogBaseUrl).build();
    }

    @Bean
    public RestClient auditRestClient(RestClient.Builder builder) {
        return builder.baseUrl(auditBaseUrl).build();
    }

    @Bean
    public RestClient reportRestClient(RestClient.Builder builder) {
        return builder.baseUrl(reportBaseUrl).build();
    }
}
