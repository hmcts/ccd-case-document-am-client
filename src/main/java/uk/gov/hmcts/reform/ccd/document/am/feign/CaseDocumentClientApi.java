package uk.gov.hmcts.reform.ccd.document.am.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;

import java.util.UUID;

@FeignClient(name = "case-document-am-client-api", url = "${case_document_am.url}/cases/documents")
public interface CaseDocumentClientApi {

    String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    String DOCUMENT_ID = "documentId";

    @GetMapping(value = "/{documentId}/binary")
    ResponseEntity<Resource> getDocumentBinary(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                                               @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuth,
                                               @PathVariable(DOCUMENT_ID) UUID documentId);

    @GetMapping(value = "/{documentId}")
    Document getMetadataForDocument(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                                    @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuth,
                                    @PathVariable(DOCUMENT_ID) UUID documentId);

    @DeleteMapping(value = "/{documentId}")
    ResponseEntity deleteDocument(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                                  @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuth,
                                  @RequestHeader("user-roles") String userRoles,
                                  @PathVariable(DOCUMENT_ID) UUID documentId,
                                  @RequestParam("permanent") boolean permanent);
}
