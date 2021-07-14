package uk.gov.hmcts.reform.ccd.document.am.config;

import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentHealthClientApi;
import uk.gov.hmcts.reform.ccd.document.am.healthcheck.CaseDocumentManagementHealthIndicator;


@Configuration
@ConditionalOnProperty(prefix = "case_document_am", name = "url")
@EnableFeignClients(basePackages = "uk.gov.hmcts.reform.ccd.document.am")
public class CaseDocumentManagementClientAutoConfiguration {

    @Bean
    @ConditionalOnEnabledHealthIndicator(value = "case-document-am-api")
    public CaseDocumentManagementHealthIndicator caseDocumentManagementHealthIndicator(
        CaseDocumentHealthClientApi caseDocumentHealthClientApi) {
        return new CaseDocumentManagementHealthIndicator(caseDocumentHealthClientApi);
    }
}
