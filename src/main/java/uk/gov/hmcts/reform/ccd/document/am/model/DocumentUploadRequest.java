package uk.gov.hmcts.reform.ccd.document.am.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;

import java.util.List;

@AllArgsConstructor
@Getter
public class DocumentUploadRequest {

    private String classification;
    private String caseTypeId;
    private String jurisdictionId;
    private List<HttpEntity<Resource>> files;
}
