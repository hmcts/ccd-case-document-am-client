package uk.gov.hmcts.reform.ccd.document.am.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;
import java.util.Date;
import java.util.List;
import java.util.Map;



/**
 * StoredDocumentHalResource.
 */
@Data
@NoArgsConstructor
@Validated
@JsonIgnoreProperties(value = { "_links,_embedded" })
public class Document  {

    @JsonProperty("classification")
    private ClassificationEnum classification = ClassificationEnum.PRIVATE;
    @JsonProperty("createdBy")
    private String createdBy = null;
    @JsonProperty("createdOn")
    private Date createdOn = null;
    @JsonProperty("lastModifiedBy")
    private String lastModifiedBy = null;
    @JsonProperty("metadata")
    private Map<String, String> metadata = null;
    @JsonProperty("mimeType")
    private String mimeType = null;
    @JsonProperty("modifiedOn")
    private Date modifiedOn = null;
    @JsonProperty("originalDocumentName")
    private String originalDocumentName = null;
    @JsonProperty("roles")
    private List<String> roles = null;
    @JsonProperty("size")
    private Long size = null;
    @JsonProperty("ttl")
    private Date ttl = null;
    private String hashCode;



    /**
     * Gets or Sets classification.
     */
    public enum ClassificationEnum {
        PUBLIC("PUBLIC"),

        PRIVATE("PRIVATE"),

        RESTRICTED("RESTRICTED");

        private String value;

        ClassificationEnum(String value) {
            this.value = value;
        }

        @JsonCreator
        public static ClassificationEnum fromValue(String text) {
            for (ClassificationEnum b : ClassificationEnum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }
    }
}
