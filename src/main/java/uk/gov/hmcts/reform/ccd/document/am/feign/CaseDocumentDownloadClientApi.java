package uk.gov.hmcts.reform.ccd.document.am.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.UUID;

@FeignClient(name = "case-document-am-download-api",
             url = "${case_document_am.url}"
             )
public interface CaseDocumentDownloadClientApi {

    @RequestMapping(method = RequestMethod.GET, value = "/cases/documents/{documentId}")
    ResponseEntity<Object> getMetadataForDocument(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                                     @RequestHeader("ServiceAuthorization") String serviceAuth,
                                     @PathVariable("documentId") UUID documentId);

    @RequestMapping(method = RequestMethod.GET, value = "cases/documents/{documentId}/binary")
    ResponseEntity<Object> getDocumentBinary(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                                               @RequestHeader("ServiceAuthorization") String serviceAuth,
                                               @PathVariable("documentId") UUID documentId);

}
