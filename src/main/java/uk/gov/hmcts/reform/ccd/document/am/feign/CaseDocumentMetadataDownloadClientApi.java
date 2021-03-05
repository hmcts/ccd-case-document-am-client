package uk.gov.hmcts.reform.ccd.document.am.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.ccd.document.am.config.DownloadConfiguration;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;

import java.util.UUID;

@FeignClient(name = "case-document-am-metadata-client-api", url = "${case_document_am.url}",
    configuration = DownloadConfiguration.class)
public interface CaseDocumentMetadataDownloadClientApi {

    @GetMapping(value = "/cases/documents/{documentId}")
    Document getMetadataForDocument(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                                    @RequestHeader("ServiceAuthorization") String serviceAuth,
                                    @PathVariable("documentId") UUID documentId);
}
