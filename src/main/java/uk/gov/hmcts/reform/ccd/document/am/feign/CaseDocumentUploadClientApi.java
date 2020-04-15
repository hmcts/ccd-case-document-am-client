package uk.gov.hmcts.reform.ccd.document.am.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@FeignClient(name = "case-document-am-upload-api",
    url = "${case_document_am.url}")
public interface CaseDocumentUploadClientApi {

    @RequestMapping(method = RequestMethod.POST, value = "cases/documents")
    ResponseEntity<Resource> uploadDocument(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                                            @RequestHeader("ServiceAuthorization") String serviceAuth,
                                            @PathVariable("files") List<MultipartFile> files,
                                            @PathVariable("classification") String classification,
                                            @PathVariable("caseTypeId") String caseTypeId,
                                            @PathVariable("jurisdictionId") String jurisdictionId);
}
