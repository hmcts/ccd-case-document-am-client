package uk.gov.hmcts.reform.ccd.document.am.feign;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.ccd.document.am.model.CaseDocumentsMetadata;
import uk.gov.hmcts.reform.ccd.document.am.model.Classification;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.ccd.document.am.model.DocumentTTLRequest;
import uk.gov.hmcts.reform.ccd.document.am.model.DocumentTTLResponse;
import uk.gov.hmcts.reform.ccd.document.am.model.DocumentUploadRequest;
import uk.gov.hmcts.reform.ccd.document.am.model.PatchDocumentMetaDataResponse;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;

import java.util.List;
import java.util.UUID;

@Service
public class CaseDocumentClient {

    private static final int DOC_UUID_LENGTH = 36;
    private static final String BINARY_SUFFIX = "/binary";

    private CaseDocumentClientApi caseDocumentClientApi;

    @Autowired
    public CaseDocumentClient(CaseDocumentClientApi caseDocumentClientApi) {
        this.caseDocumentClientApi = caseDocumentClientApi;
    }

    public UploadResponse uploadDocuments(String authorisation, String serviceAuth, String caseTypeId,
                                          String jurisdictionId, List<MultipartFile> files) {
        return uploadDocuments(authorisation, serviceAuth, caseTypeId, jurisdictionId, files,
                               Classification.RESTRICTED);
    }

    public UploadResponse uploadDocuments(String authorisation, String serviceAuth,
                                           String caseTypeId,
                                           String jurisdictionId,
                                           List<MultipartFile> files,
                                           Classification classification) {

        DocumentUploadRequest documentUploadRequest = new DocumentUploadRequest(classification.toString(),
                                                                                caseTypeId,
                                                                                jurisdictionId,
                                                                                files);

        return caseDocumentClientApi.uploadDocuments(authorisation, serviceAuth, documentUploadRequest);
    }

    public ResponseEntity<Resource> getDocumentBinary(String authorisation, String serviceAuth, UUID documentId) {
        return caseDocumentClientApi.getDocumentBinary(authorisation, serviceAuth, documentId);
    }

    public ResponseEntity<Resource> getDocumentBinary(String authorisation, String serviceAuth, String binaryHref) {
        String selfHref = binaryHref.replace(BINARY_SUFFIX, "");
        UUID documentId = getDocumentIdFromSelfHref(selfHref);
        return caseDocumentClientApi.getDocumentBinary(authorisation, serviceAuth, documentId);
    }

    public Document getMetadataForDocument(String authorisation, String serviceAuth, UUID documentId) {
        return caseDocumentClientApi.getMetadataForDocument(authorisation, serviceAuth, documentId);
    }

    public Document getMetadataForDocument(String authorisation, String serviceAuth, String selfHref) {
        UUID documentId = getDocumentIdFromSelfHref(selfHref);
        return caseDocumentClientApi.getMetadataForDocument(authorisation, serviceAuth, documentId);
    }

    public DocumentTTLResponse patchDocument(String authorisation, String serviceAuth, UUID documentId,
                                             DocumentTTLRequest ttl) {
        return caseDocumentClientApi.patchDocument(authorisation, serviceAuth, documentId, ttl);
    }

    public DocumentTTLResponse patchDocument(String authorisation, String serviceAuth, String selfHref,
                                             DocumentTTLRequest ttl) {
        UUID documentId = getDocumentIdFromSelfHref(selfHref);
        return caseDocumentClientApi.patchDocument(authorisation, serviceAuth, documentId, ttl);
    }

    public PatchDocumentMetaDataResponse patchDocument(String authorisation, String serviceAuth,
                                                       CaseDocumentsMetadata caseDocumentsMetadata) {
        return caseDocumentClientApi.patchDocument(authorisation, serviceAuth, caseDocumentsMetadata);
    }

    public void deleteDocument(String authorisation, String serviceAuth, UUID documentId, boolean permanent) {
        caseDocumentClientApi.deleteDocument(authorisation, serviceAuth, documentId, permanent);
    }

    private UUID getDocumentIdFromSelfHref(String selfHref) {
        return UUID.fromString(selfHref.substring(selfHref.length() - DOC_UUID_LENGTH));
    }

}
