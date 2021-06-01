package uk.gov.hmcts.reform.ccd.document.am.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class DocumentTTLResponse {

    private LocalDateTime createdOn;

    private LocalDateTime modifiedOn;

    private LocalDateTime ttl;

    public DocumentTTLResponse(LocalDateTime ttl) {
        this.ttl = ttl;
    }

}
