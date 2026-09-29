package uk.gov.hmcts.reform.ccd.document.am.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldDeserializeDocument() throws Exception {
        String json = """
            {
              "originalDocumentName": "test.pdf",
              "size": 123
            }
            """;

        Document document = objectMapper.readValue(json, Document.class);

        assertThat(document.originalDocumentName).isEqualTo("test.pdf");
        assertThat(document.size).isEqualTo(123);
    }
}
