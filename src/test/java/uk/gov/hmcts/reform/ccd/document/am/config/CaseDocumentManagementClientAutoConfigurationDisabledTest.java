package uk.gov.hmcts.reform.ccd.document.am.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClientApi;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentHealthClientApi;
import uk.gov.hmcts.reform.ccd.document.am.healthcheck.CaseDocumentManagementHealthIndicator;
import uk.gov.hmcts.reform.ccd.document.testsupport.IsolatedAutoConfigurationTestApplication;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
    classes = IsolatedAutoConfigurationTestApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = "case_document_am.url=false"
)
class CaseDocumentManagementClientAutoConfigurationDisabledTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void shouldNotRegisterClientBeansWhenUrlIsMissing() {
        assertThat(context.getBeanNamesForType(CaseDocumentClientApi.class)).isEmpty();
        assertThat(context.getBeanNamesForType(CaseDocumentHealthClientApi.class)).isEmpty();
        assertThat(context.getBeanNamesForType(CaseDocumentManagementHealthIndicator.class)).isEmpty();
    }
}
