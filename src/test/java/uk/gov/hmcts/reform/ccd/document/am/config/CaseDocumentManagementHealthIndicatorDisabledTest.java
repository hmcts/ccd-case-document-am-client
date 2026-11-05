package uk.gov.hmcts.reform.ccd.document.am.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClientApi;
import uk.gov.hmcts.reform.ccd.document.am.healthcheck.CaseDocumentManagementHealthIndicator;
import uk.gov.hmcts.reform.ccd.document.testsupport.IsolatedAutoConfigurationTestApplication;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
    classes = IsolatedAutoConfigurationTestApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@TestPropertySource(properties = {
    "case_document_am.url=http://localhost:5171",
    "management.health.case-document-am-api.enabled=false"
})
class CaseDocumentManagementHealthIndicatorDisabledTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void shouldKeepFeignClientsAndSkipHealthIndicatorWhenHealthIsDisabled() {
        assertThat(context.getBean(CaseDocumentClientApi.class)).isNotNull();
        assertThat(context.getBeanNamesForType(CaseDocumentManagementHealthIndicator.class)).isEmpty();
    }
}
