package uk.gov.hmcts.reform.ccd.document.am.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import uk.gov.hmcts.reform.ccd.document.am.healthcheck.InternalHealth;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@FeignClient(name = "case-document-am-health-client-api", url = "${case_document_am.url}")
public interface CaseDocumentHealthClientApi {

    @GetMapping(value = "/health", headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE)
    InternalHealth health();
}
