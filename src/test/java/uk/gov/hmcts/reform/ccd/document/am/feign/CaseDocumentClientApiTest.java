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
import uk.gov.hmcts.reform.ccd.document.am.model.DocumentUploadRequest;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aMultipart;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@SpringBootTest(classes = {CaseDocumentClientApi.class})
@PropertySource(value = "classpath:application.yml")
@EnableAutoConfiguration
@AutoConfigureWireMock(port = 5050)
class CaseDocumentClientApiTest {

    private static final UUID DOCUMENT_ID = UUID.randomUUID();

    private static final String URL = "/cases/documents";

    private static final String CLASSIFICATION = "aClassification";
    private static final String CASE_TYPE_ID = "aCaseTypeId";
    private static final String JURISDICTION = "aJurisdictionId";

    private static final String SERVICE_AUTHORISATION_KEY = "ServiceAuthorization";
    private static final String BEARER = "Bearer ";
    private static final String TOKEN = "user1";
    private static final String SERVICE_AUTHORISATION_VALUE = BEARER + TOKEN;
    private static final String AUTHORISATION_VALUE = "a Bearer idam token";

    private static final boolean PERMANENT = false;
    public static final String PERMANENT_QUERY_PARAM = "permanent";

    private MockMultipartFile multipartFile = new MockMultipartFile("testFile1", "content".getBytes());

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private CaseDocumentClientApi caseDocumentClientApi;

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    void shouldUploadDocuments() {
        List<MultipartFile> files = new ArrayList<>();
        files.add(multipartFile);

        DocumentUploadRequest request = new DocumentUploadRequest(CLASSIFICATION,
                                                                  CASE_TYPE_ID, JURISDICTION, files);
        stubForUpload(request);

        ResponseEntity responseEntity = caseDocumentClientApi.uploadDocuments(
            AUTHORISATION_VALUE, SERVICE_AUTHORISATION_VALUE, request);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
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
    void shouldDeleteDocument() {

        stubForDeleteDocument(DOCUMENT_ID, PERMANENT);

        caseDocumentClientApi.deleteDocument(
            TOKEN,
            SERVICE_AUTHORISATION_VALUE,
            DOCUMENT_ID,
            PERMANENT
        );

        WireMock.verify(deleteRequestedFor(
            urlPathEqualTo(URL + "/" + DOCUMENT_ID))
               .withQueryParam(PERMANENT_QUERY_PARAM, equalTo(String.valueOf(PERMANENT))));
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

    private void stubForUpload(DocumentUploadRequest request) {
        stubFor(WireMock.post(urlPathEqualTo(URL))
                    .withHeader(SERVICE_AUTHORISATION_KEY, equalTo(SERVICE_AUTHORISATION_VALUE))
                    .withHeader(AUTHORIZATION, equalTo(AUTHORISATION_VALUE))
                    .withHeader(CONTENT_TYPE, containing(MULTIPART_FORM_DATA_VALUE))
                    .withMultipartRequestBody(
                        aMultipart()
                          .withName("jurisdictionId")
                          .withBody(containing(request.getJurisdictionId())))
                    .withMultipartRequestBody(
                        aMultipart()
                            .withName("caseTypeId")
                            .withBody(containing(request.getCaseTypeId())))
                    .withMultipartRequestBody(
                        aMultipart()
                            .withName("classification")
                            .withBody(containing(request.getClassification())))
                    .willReturn(aResponse().withStatus(HttpStatus.OK.value())
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

    private void stubForDeleteDocument(UUID documentId, boolean permanent) {
        stubFor(WireMock.delete(urlPathEqualTo(URL + "/" + documentId))
                    .withQueryParam(PERMANENT_QUERY_PARAM, equalTo(String.valueOf(permanent)))
                    .withHeader(SERVICE_AUTHORISATION_KEY, equalTo(SERVICE_AUTHORISATION_VALUE))
                    .withHeader(AUTHORIZATION, equalTo(TOKEN))
                    .willReturn(aResponse().withStatus(HttpStatus.NO_CONTENT.value()))
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
