package uk.gov.hmcts.reform.ccd.document.am.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UploadResponse {

    private List<Document> documents;

    @JsonCreator
    public UploadResponse(List<Document> documents) {
        this.documents = documents;
    }

    public List<Document> getDocuments() {
        return documents;
    }
}
