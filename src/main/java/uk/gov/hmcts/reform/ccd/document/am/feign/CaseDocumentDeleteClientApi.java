package uk.gov.hmcts.reform.ccd.document.am.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "case-document-am-delete-client-api", url = "${case_document_am.url}")
public interface CaseDocumentDeleteClientApi {

    @DeleteMapping(value = "cases/documents/{documentId}")
    ResponseEntity deleteDocument(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                                  @RequestHeader("ServiceAuthorization") String serviceAuth,
                                  @RequestHeader("user-roles") String userRoles,
                                  @PathVariable("documentId") UUID documentId,
                                  @RequestParam("permanent") boolean permanent);
}
