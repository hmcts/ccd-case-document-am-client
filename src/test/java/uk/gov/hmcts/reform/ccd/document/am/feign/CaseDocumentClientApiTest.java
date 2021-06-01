package uk.gov.hmcts.reform.ccd.document.am.feign;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.ccd.document.am.model.DocumentTTLRequest;
import uk.gov.hmcts.reform.ccd.document.am.model.DocumentTTLResponse;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@SpringBootTest(classes = {CaseDocumentClientApi.class})
@PropertySource(value = "classpath:application.yml")
@EnableAutoConfiguration
@AutoConfigureWireMock(port = 5050)
class CaseDocumentClientApiTest {

    private static final UUID DOCUMENT_ID = UUID.randomUUID();

    private static final String URL = "/cases/documents";

    private static final String CLASSIFICATION = "classification";
    private static final String CASE_TYPE_ID = "CaseTypeID";

    private List<MultipartFile> files;

    private static final String SERVICE_AUTHORISATION_KEY = "ServiceAuthorization";
    private static final String BEARER = "Bearer ";
    private static final String TOKEN = "user1";
    private static final String SERVICE_AUTHORISATION_VALUE = BEARER + TOKEN;

    private static final boolean PERMANENT = false;

    private MockMultipartFile multipartFile = new MockMultipartFile("testFile1", "content".getBytes());

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private CaseDocumentClientApi caseDocumentClientApi;

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        files = new ArrayList<>();

    }

    @Test
    void uploadSingleDocumentTest() throws IOException {
        files.add(multipartFile);

        ResponseEntity response = new ResponseEntity(HttpStatus.OK);

        stubForUpload(response);

        ResponseEntity finalResponse = caseDocumentClientApi.uploadDocuments(
            files,
            CLASSIFICATION,
            CASE_TYPE_ID,
            SERVICE_AUTHORISATION_VALUE
        );

        assertEquals(HttpStatus.OK, finalResponse.getStatusCode());
    }

    @Test
    void uploadMultipleDocumentTest() throws IOException {
        files.add(multipartFile);
        files.add(multipartFile);
        files.add(multipartFile);

        ResponseEntity response = new ResponseEntity(HttpStatus.OK);

        stubForUpload(response);

        ResponseEntity finalResponse = caseDocumentClientApi.uploadDocuments(
            files,
            CLASSIFICATION,
            CASE_TYPE_ID,
            SERVICE_AUTHORISATION_VALUE
        );

        assertEquals(HttpStatus.OK, finalResponse.getStatusCode());
    }

    @Test
    void getDocumentBinaryTest() throws IOException {
        ResponseEntity response = new ResponseEntity(HttpStatus.OK);

        stubForDocumentBinary(response);

        ResponseEntity finalResponse = caseDocumentClientApi.getDocumentBinary(
            TOKEN,
            SERVICE_AUTHORISATION_VALUE,
            DOCUMENT_ID
        );

        assertEquals(HttpStatus.OK, finalResponse.getStatusCode());
    }

    @Test
    void getDocumentMetaData() throws IOException {
        Date createdOn = Date.from(Instant.now());

        Document response = new Document();
        response.createdOn = createdOn;

        stubForDocumentMetaData(response);

        Document finalResponse = caseDocumentClientApi.getMetadataForDocument(
            TOKEN,
            SERVICE_AUTHORISATION_VALUE,
            DOCUMENT_ID
        );

        assertEquals(finalResponse.createdOn, response.createdOn);
    }

    @Test
    void deleteDocument() throws IOException {
        ResponseEntity response = new ResponseEntity(HttpStatus.OK);

        stubForDeleteDocument(response);

        ResponseEntity finalResponse = caseDocumentClientApi.deleteDocument(
            TOKEN,
            SERVICE_AUTHORISATION_VALUE,
            "user-roles",
            DOCUMENT_ID,
            PERMANENT
        );

        assertEquals(HttpStatus.OK, finalResponse.getStatusCode());
    }

    @Test
    void patchDocumentTest() throws IOException {
        LocalDateTime localDateTime = LocalDateTime.now();

        DocumentTTLRequest request = new DocumentTTLRequest(localDateTime);
        DocumentTTLResponse response = new DocumentTTLResponse(localDateTime);

        stubForPatch(request, response);

        DocumentTTLResponse finalResponse = caseDocumentClientApi.patchDocument(
            TOKEN,
            SERVICE_AUTHORISATION_VALUE,
            DOCUMENT_ID,
            request
        );

        assertEquals(finalResponse.getTtl(), localDateTime);
    }

    private void stubForUpload(ResponseEntity response) throws JsonProcessingException {
        stubFor(WireMock.post(WireMock.urlPathEqualTo(URL))
                    .withHeader(SERVICE_AUTHORISATION_KEY, equalTo(SERVICE_AUTHORISATION_VALUE))
                    .withQueryParam("classification", equalTo(CLASSIFICATION))
                    .withQueryParam("caseTypeId", equalTo(CASE_TYPE_ID))
                    .willReturn(aResponse()
                                    .withStatus(HttpStatus.OK.value())
                    )
        );
    }

    private void stubForDocumentBinary(ResponseEntity response) throws JsonProcessingException {
        stubFor(WireMock.get(WireMock.urlMatching(URL
                                                      + "/" + DOCUMENT_ID
                                                      + "/binary"))
                    .withHeader(SERVICE_AUTHORISATION_KEY, equalTo(SERVICE_AUTHORISATION_VALUE))
                    .withHeader(AUTHORIZATION, equalTo(TOKEN))
                    .willReturn(aResponse()
                                    .withStatus(HttpStatus.OK.value())
                                    .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                                    .withBody(objectMapper.writeValueAsString(response))
                    )
        );
    }

    private void stubForDocumentMetaData(Document response) throws JsonProcessingException {
        stubFor(WireMock.get(WireMock.urlMatching(URL
                                                      + "/" + DOCUMENT_ID))
                    .withHeader(SERVICE_AUTHORISATION_KEY, equalTo(SERVICE_AUTHORISATION_VALUE))
                    .withHeader(AUTHORIZATION, equalTo(TOKEN))
                    .willReturn(aResponse()
                                    .withStatus(HttpStatus.OK.value())
                                    .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                                    .withBody(objectMapper.writeValueAsString(response))
                    )
        );
    }

    private void stubForDeleteDocument(ResponseEntity response) throws JsonProcessingException {
        stubFor(WireMock.delete(WireMock.urlMatching(URL
                                                         + "/" + DOCUMENT_ID
                                                         + "\\?permanent=" + PERMANENT))
                    .withHeader(SERVICE_AUTHORISATION_KEY, equalTo(SERVICE_AUTHORISATION_VALUE))
                    .withHeader(AUTHORIZATION, equalTo(TOKEN))
                    .willReturn(aResponse()
                                    .withStatus(HttpStatus.OK.value())
                                    .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                                    .withBody(objectMapper.writeValueAsString(response))
                    )
        );
    }

    private void stubForPatch(DocumentTTLRequest request, DocumentTTLResponse response)
        throws JsonProcessingException {
        stubFor(WireMock.patch(WireMock.urlMatching(URL
                                                        + "/" + DOCUMENT_ID))
                    .withHeader(AUTHORIZATION, equalTo(TOKEN))
                    .withHeader(SERVICE_AUTHORISATION_KEY, equalTo(SERVICE_AUTHORISATION_VALUE))
                    .withRequestBody(equalToJson(objectMapper.writeValueAsString(request)))
                    .willReturn(aResponse()
                                    .withStatus(HttpStatus.OK.value())
                                    .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                                    .withBody(objectMapper.writeValueAsString(response))
                    )
        );
    }
}
