package uk.gov.hmcts.reform.ccd.document.am.feign;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.ccd.document.am.model.DocumentTTLRequest;
import uk.gov.hmcts.reform.ccd.document.am.model.DocumentTTLResponse;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.DataTruncation;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
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

    public static final UUID DOCUMENT_ID = UUID.randomUUID();

    public static final String URL = "/cases/documents";

    public static final String CLASSIFICATION = "classification";
    public static final String CASE_TYPE_ID = "CaseTypeID";

    public static final boolean PERMANENT = false;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private CaseDocumentClientApi caseDocumentClientApi;

    @BeforeAll
    static void beforeAll(){
    }

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);


    }

    @AfterAll
    static void tearDown() {
    }

    @Test
    void uploadSingleDocumentTest() throws IOException {
        List<MultipartFile> files = new ArrayList<>();
        files.add(getMultiPartFile("testFile1"));

        ResponseEntity response = new ResponseEntity(HttpStatus.OK);

        stubForUpload(response);

        ResponseEntity finalResponse = caseDocumentClientApi.uploadDocuments(
            files,
            "classification",
            "CaseTypeID",
            "Bearer user1"
        );

        assertEquals(HttpStatus.OK, finalResponse.getStatusCode());
        assertEquals(1, files.size());
    }

    @Test
    void uploadMultipleDocumentTest() throws IOException {
        List<MultipartFile> files = new ArrayList<>();
        files.add(getMultiPartFile("testFile1"));
        files.add(getMultiPartFile("testFile2"));
        files.add(getMultiPartFile("testFile3"));

        ResponseEntity response = new ResponseEntity(HttpStatus.OK);

        stubForUpload(response);

        ResponseEntity finalResponse = caseDocumentClientApi.uploadDocuments(
            files,
            "classification",
            "CaseTypeID",
            "Bearer user1"
        );

        System.out.println(finalResponse.getBody());

        assertEquals(HttpStatus.OK, finalResponse.getStatusCode());
        //assertEquals(3, finalResponse.getBody());
    }

    @Test
    void getDocumentBinaryTest() throws IOException {
        ResponseEntity response = new ResponseEntity(HttpStatus.OK);

        stubForDocumentBinary(response);

        ResponseEntity finalResponse = caseDocumentClientApi.getDocumentBinary(
            "user1",
            "Bearer user1",
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
            "user1",
            "Bearer user1",
            DOCUMENT_ID
        );

        System.out.println(response.createdOn);
        System.out.println(finalResponse.createdOn);

        assertEquals(finalResponse.createdOn, response.createdOn);
    }

    @Test
    void deleteDocument() throws IOException {
        ResponseEntity response = new ResponseEntity(HttpStatus.OK);

        stubForDeleteDocument(response);

        ResponseEntity finalResponse = caseDocumentClientApi.deleteDocument(
            "user1",
            "Bearer user1",
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
            "user1",
            "Bearer user1",
            DOCUMENT_ID,
            request
        );

        assertEquals(finalResponse.getTtl(), localDateTime);
    }

    private void stubForUpload(ResponseEntity response) throws JsonProcessingException {
        stubFor(WireMock.post(WireMock.urlPathEqualTo(URL))
                    .withHeader("ServiceAuthorization", equalTo("Bearer user1"))
                    .willReturn(aResponse()
                                    .withStatus(HttpStatus.OK.value())
                                    .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                                    .withBody(objectMapper.writeValueAsString(response))
                    )
        );
    }

    private void stubForDocumentBinary(ResponseEntity response) throws JsonProcessingException {
        stubFor(WireMock.get(WireMock.urlMatching(URL
                                                      + "/" + DOCUMENT_ID
                                                      + "/binary"))
                    .withHeader("ServiceAuthorization", equalTo("Bearer user1"))
                    .withHeader(AUTHORIZATION, equalTo("user1"))
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
                    .withHeader("ServiceAuthorization", equalTo("Bearer user1"))
                    .withHeader(AUTHORIZATION, equalTo("user1"))
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
                    .withHeader("ServiceAuthorization", equalTo("Bearer user1"))
                    .withHeader(AUTHORIZATION, equalTo("user1"))
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
                    .withHeader(AUTHORIZATION, equalTo("user1"))
                    .withHeader("ServiceAuthorization", equalTo("Bearer user1"))
                    .withRequestBody(equalToJson(objectMapper.writeValueAsString(request)))
                    .willReturn(aResponse()
                                    .withStatus(HttpStatus.OK.value())
                                    .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                                    .withBody(objectMapper.writeValueAsString(response))
                    )
        );
    }



    private MultipartFile getMultiPartFile(String name){
        MultipartFile multipartFile = new MultipartFile() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getOriginalFilename() {
                return name;
            }

            @Override
            public String getContentType() {
                return "Application";
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public long getSize() {
                return 10;
            }

            @Override
            public byte[] getBytes() throws IOException {
                return new byte[0];
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return null;
            }

            @Override
            public void transferTo(File dest) throws IOException, IllegalStateException {

            }
        };
        return multipartFile;
    }

}
