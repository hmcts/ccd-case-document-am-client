package uk.gov.hmcts.reform.ccd.document.am.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class DocumentTTLResponse {

    private LocalDateTime createdOn;

    private LocalDateTime modifiedOn;

    private LocalDateTime ttl;
}
