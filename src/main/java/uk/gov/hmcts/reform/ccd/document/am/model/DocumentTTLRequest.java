package uk.gov.hmcts.reform.ccd.document.am.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class DocumentTTLRequest {

    private LocalDateTime ttl;

}
