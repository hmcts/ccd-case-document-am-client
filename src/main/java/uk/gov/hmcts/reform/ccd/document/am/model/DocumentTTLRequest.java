package uk.gov.hmcts.reform.ccd.document.am.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class DocumentTTLRequest {

    private LocalDateTime ttl;

}
