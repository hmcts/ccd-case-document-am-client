package uk.gov.hmcts.reform.ccd.document.am.feign;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import uk.gov.hmcts.reform.ccd.document.am.healthcheck.CaseDocumentManagementHealthIndicator;
import uk.gov.hmcts.reform.ccd.document.am.healthcheck.InternalHealth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class CaseDocumentHealthClientApiTest {

    @Mock
    private CaseDocumentHealthClientApi caseDocumentHealthClientApi;

    private HealthIndicator indicator;

    @BeforeEach
    public void setUp() {
        indicator = new CaseDocumentManagementHealthIndicator(caseDocumentHealthClientApi);
    }

    @DisplayName("Should respond with UP status")
    @Test
    public void healthUp() {
        given(caseDocumentHealthClientApi.health()).willReturn(new InternalHealth("UP"));

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
    }

    @DisplayName("Should respond with DOWN status")
    @Test
    public void healthDown() {
        given(caseDocumentHealthClientApi.health()).willReturn(new InternalHealth("DOWN"));

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    }

    @DisplayName("Should respond with DOWN status when exception is thrown")
    @Test
    public void healthHandleException() {
        given(caseDocumentHealthClientApi.health()).willThrow(new RuntimeException("Service Unavailable"));

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails())
            .containsKey("error")
            .containsValue(RuntimeException.class.getCanonicalName() + ": Service Unavailable");
    }
}
