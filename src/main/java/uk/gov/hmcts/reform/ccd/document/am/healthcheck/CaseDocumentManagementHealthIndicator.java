package uk.gov.hmcts.reform.ccd.document.am.healthcheck;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentHealthClientApi;

public class CaseDocumentManagementHealthIndicator implements HealthIndicator {

    private static final Logger LOGGER = LoggerFactory.getLogger(CaseDocumentManagementHealthIndicator.class);

    private final CaseDocumentHealthClientApi caseDocumentHealthClientApi;

    public CaseDocumentManagementHealthIndicator(
        final CaseDocumentHealthClientApi caseDocumentHealthClientApi) {
        this.caseDocumentHealthClientApi = caseDocumentHealthClientApi;
    }

    @Override
    public Health health() {
        try {
            InternalHealth internalHealth = this.caseDocumentHealthClientApi.health();
            return new Health.Builder(internalHealth.getStatus()).build();
        } catch (Exception ex) {
            LOGGER.error("Error on document management app healthcheck", ex);
            return Health.down(ex).build();
        }
    }
}
