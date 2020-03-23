package uk.gov.hmcts.reform.ccd.document.am;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentMetadataDownloadClientApi;
import uk.gov.hmcts.reform.ccd.document.am.healthcheck.CaseDocumentManagementHealthIndicator;


@Configuration
@ConditionalOnProperty(prefix = "case_document_management", name = "url")
@EnableFeignClients(basePackages = "uk.gov.hmcts.reform.ccd.document.am")
public class CaseDocumentManagementClientAutoConfiguration {

    @Bean
    public CaseDocumentManagementHealthIndicator documentManagement(
        CaseDocumentMetadataDownloadClientApi caseDocumentMetadataDownloadClientApi
    ) {
        return new CaseDocumentManagementHealthIndicator(caseDocumentMetadataDownloadClientApi);
    }
}
