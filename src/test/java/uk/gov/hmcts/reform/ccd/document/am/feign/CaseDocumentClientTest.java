package uk.gov.hmcts.reform.ccd.document.am.feign;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import uk.gov.hmcts.reform.ccd.document.am.model.CaseDocumentsMetadata;
import uk.gov.hmcts.reform.ccd.document.am.model.Classification;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.ccd.document.am.model.DocumentHashToken;
import uk.gov.hmcts.reform.ccd.document.am.model.DocumentTTLRequest;
import uk.gov.hmcts.reform.ccd.document.am.model.DocumentTTLResponse;
import uk.gov.hmcts.reform.ccd.document.am.model.DocumentUploadRequest;
import uk.gov.hmcts.reform.ccd.document.am.model.PatchDocumentMetaDataResponse;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
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
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@SpringBootTest(classes = {CaseDocumentClient.class, CaseDocumentClientApi.class})
@TestPropertySource(properties = "case_document_am.url=http://localhost:5170")
@EnableAutoConfiguration
@AutoConfigureWireMock(port = 5170)
public class CaseDocumentClientTest {

    private static final Classification PUBLIC = Classification.PUBLIC;

    private static final String SELF_LINK = "http://dm-store:8080/documents/80e9471e-0f67-42ef-8739-170aa1942363";
    private static final String BINARY_LINK = "http://dm-store:8080/documents/80e9471e-0f67-42ef-8739-170aa1942363/binary";

    private static final UUID DOCUMENT_ID = UUID.fromString(SELF_LINK.substring(SELF_LINK.length() - 36));

    private static final String URL = "/cases/documents";

    private static final String CASE_TYPE_ID = "aCaseTypeId";
    private static final String JURISDICTION = "aJurisdictionId";

    private static final String SERVICE_AUTHORISATION_KEY = "ServiceAuthorization";
    private static final String SERVICE_AUTHORISATION_VALUE = "a Bearer s2s token";
    private static final String AUTHORISATION_VALUE = "a Bearer idam token";

    private static final boolean PERMANENT = false;

    private static final String PERMANENT_QUERY_PARAM = "permanent";
    public static final String HASH_TOKEN = "aHashToken";
    public static final String MIME_TYPE = "application/octet-stream";
    public static final String ORIGINAL_DOCUMENT_NAME = "test.png";
    private static final String ATTACH_TO_CASE = "attachToCase";
    public static final String TEST_USER = "aTestUser";
    public static final Date NOW = new Date();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private CaseDocumentClient caseDocumentClient;

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new ParameterNamesModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    void shouldSuccessfullyUploadDocumentsUsingMockMultiPartFile() throws IOException {

        MockMultipartFile multipartFile = new MockMultipartFile("file1",
                                                                "test.png",
                                                                "application/octet-stream",
                                                                "someBytes".getBytes());

        assertDocumentUpload(multipartFile);
    }

    @Test
    void shouldSuccessfullyUploadDocumentsUsingMultipartFileImplementation() throws IOException {
        String url = "src/test/resources/validation.txt";
        File file = new File(url);
        FileItemFactory factory = new DiskFileItemFactory(16, file.getParentFile());
        FileItem item = factory.createItem(file.getName(),"text/plain",true, file.getName());

        Path path = Paths.get(url);
        Files.copy(path, item.getOutputStream());
        MultipartFile multipartFile = new CommonsMultipartFile(item);

        assertDocumentUpload(multipartFile);
    }

    @Test
    void shouldSuccessfullyGetDocumentBinary() throws IOException {
        ResponseEntity response = new ResponseEntity(HttpStatus.OK);

        stubForDocumentBinary(response);

        ResponseEntity finalResponse = caseDocumentClient.getDocumentBinary(
            AUTHORISATION_VALUE,
            SERVICE_AUTHORISATION_VALUE,
            DOCUMENT_ID
        );

        assertEquals(HttpStatus.OK, finalResponse.getStatusCode());
    }

    @Test
    void shouldGetDocumentBinaryWithBinaryHref() throws IOException {
        ResponseEntity response = new ResponseEntity(HttpStatus.OK);

        stubForDocumentBinary(response);

        ResponseEntity finalResponse = caseDocumentClient.getDocumentBinary(
            AUTHORISATION_VALUE,
            SERVICE_AUTHORISATION_VALUE,
            BINARY_LINK
        );

        assertEquals(HttpStatus.OK, finalResponse.getStatusCode());
    }

    @Test
    void shouldSuccessfullyGetMetadataForDocument() throws IOException {

        Document response = createDocument();

        stubForDocumentMetaData(response);

        Document finalResponse = caseDocumentClient.getMetadataForDocument(
            AUTHORISATION_VALUE,
            SERVICE_AUTHORISATION_VALUE,
            DOCUMENT_ID
        );

        assertEquals(finalResponse.createdOn, response.createdOn);
        assertEquals(finalResponse.classification, response.classification);
        assertEquals(finalResponse.size, response.size);
        assertEquals(finalResponse.mimeType, response.mimeType);
        assertEquals(finalResponse.links.self.href, response.links.self.href);
        assertEquals(finalResponse.links.binary.href, response.links.binary.href);
    }

    @Test
    void shouldGetMetadataForDocumentWithSelfLink() throws IOException {

        Document document = createDocument();

        stubForDocumentMetaData(document);

        Document finalResponse = caseDocumentClient.getMetadataForDocument(
            AUTHORISATION_VALUE,
            SERVICE_AUTHORISATION_VALUE,
            SELF_LINK
        );

        assertEquals(finalResponse.createdOn, document.createdOn);
        assertEquals(finalResponse.classification, document.classification);
        assertEquals(finalResponse.size, document.size);
        assertEquals(finalResponse.mimeType, document.mimeType);
        assertEquals(finalResponse.links.self.href, document.links.self.href);
        assertEquals(finalResponse.links.binary.href, document.links.binary.href);
    }

    @Test
    void shouldSuccessfullyDeleteDocument() {

        stubForDeleteDocument(DOCUMENT_ID, PERMANENT);

        caseDocumentClient.deleteDocument(
            AUTHORISATION_VALUE,
            SERVICE_AUTHORISATION_VALUE,
            DOCUMENT_ID,
            PERMANENT
        );

        WireMock.verify(deleteRequestedFor(
            urlPathEqualTo(URL + "/" + DOCUMENT_ID))
                            .withQueryParam(PERMANENT_QUERY_PARAM, equalTo(String.valueOf(PERMANENT))));
    }

    @Test
    void shouldSuccessfullyPatchDocument() throws IOException {
        LocalDateTime createdOn = LocalDateTime.now().plusDays(1);
        LocalDateTime modifiedOn = LocalDateTime.now().plusDays(2);
        LocalDateTime ttl = LocalDateTime.now();

        DocumentTTLRequest request = new DocumentTTLRequest(ttl);
        DocumentTTLResponse response = new DocumentTTLResponse(createdOn, modifiedOn, ttl);

        stubForPatch(request, response);

        DocumentTTLResponse finalResponse = caseDocumentClient.patchDocument(
            AUTHORISATION_VALUE,
            SERVICE_AUTHORISATION_VALUE,
            DOCUMENT_ID,
            request
        );

        assertEquals(finalResponse.getCreatedOn(), createdOn);
        assertEquals(finalResponse.getModifiedOn(), modifiedOn);
        assertEquals(finalResponse.getTtl(), ttl);
    }

    @Test
    void shouldPatchDocumentWithSelfLink() throws IOException {
        LocalDateTime createdOn = LocalDateTime.now().plusDays(1);
        LocalDateTime modifiedOn = LocalDateTime.now().plusDays(2);
        LocalDateTime ttl = LocalDateTime.now();

        DocumentTTLRequest request = new DocumentTTLRequest(ttl);
        DocumentTTLResponse response = new DocumentTTLResponse(createdOn, modifiedOn, ttl);

        stubForPatch(request, response);

        DocumentTTLResponse finalResponse = caseDocumentClient.patchDocument(
            AUTHORISATION_VALUE,
            SERVICE_AUTHORISATION_VALUE,
            SELF_LINK,
            request
        );

        assertEquals(finalResponse.getCreatedOn(), createdOn);
        assertEquals(finalResponse.getModifiedOn(), modifiedOn);
        assertEquals(finalResponse.getTtl(), ttl);
    }

    @Test
    void shouldSuccessfullyPatchDocumentWithCaseDocumentsMetadata() throws IOException {

        CaseDocumentsMetadata caseDocumentsMetadata = CaseDocumentsMetadata.builder()
                .caseId("caseId")
                .caseTypeId("caseTypeId")
                .jurisdictionId("jurisdictionId")
                .documentHashTokens(List.of(DocumentHashToken.builder().build()))
                .build();

        PatchDocumentMetaDataResponse patchDocumentMetaDataResponse = new PatchDocumentMetaDataResponse("Success");

        stubForPatch(caseDocumentsMetadata, patchDocumentMetaDataResponse);

        PatchDocumentMetaDataResponse finalResponse = caseDocumentClient.patchDocument(
                AUTHORISATION_VALUE,
                SERVICE_AUTHORISATION_VALUE,
                caseDocumentsMetadata
        );

        assertEquals(finalResponse.getResult(), "Success");
    }

    @Test
    void shouldFailWhenPatchDocumentWithCaseDocumentsMetadataReturnsBadRequest() throws IOException {

        CaseDocumentsMetadata caseDocumentsMetadata = CaseDocumentsMetadata.builder()
                .build();

        stubForPatch(caseDocumentsMetadata, HttpStatus.BAD_REQUEST);

        assertThrows(FeignException.BadRequest.class, () ->
            caseDocumentClient.patchDocument(
                    AUTHORISATION_VALUE,
                    SERVICE_AUTHORISATION_VALUE,
                    caseDocumentsMetadata)
        );
    }

    private void stubForUpload(DocumentUploadRequest request, UploadResponse mockResponse)
        throws JsonProcessingException {
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
                    .willReturn(aResponse()
                                    .withStatus(HttpStatus.OK.value())
                                    .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                                    .withBody(objectMapper.writeValueAsString(mockResponse))
                    )
        );
    }

    private void stubForDocumentBinary(ResponseEntity response) throws JsonProcessingException {
        stubFor(WireMock.get(WireMock.urlMatching(URL
                                                      + "/" + DOCUMENT_ID
                                                      + "/binary"))
                    .withHeader(SERVICE_AUTHORISATION_KEY, equalTo(SERVICE_AUTHORISATION_VALUE))
                    .withHeader(AUTHORIZATION, equalTo(AUTHORISATION_VALUE))
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
                    .withHeader(AUTHORIZATION, equalTo(AUTHORISATION_VALUE))
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
                    .withHeader(AUTHORIZATION, equalTo(AUTHORISATION_VALUE))
                    .willReturn(aResponse().withStatus(HttpStatus.NO_CONTENT.value()))
        );
    }

    private void stubForPatch(DocumentTTLRequest request, DocumentTTLResponse response)
        throws JsonProcessingException {
        stubFor(WireMock.patch(WireMock.urlMatching(URL
                                                        + "/" + DOCUMENT_ID))
                    .withHeader(AUTHORIZATION, equalTo(AUTHORISATION_VALUE))
                    .withHeader(SERVICE_AUTHORISATION_KEY, equalTo(SERVICE_AUTHORISATION_VALUE))
                    .withRequestBody(equalToJson(objectMapper.writeValueAsString(request)))
                    .willReturn(aResponse()
                                    .withStatus(HttpStatus.OK.value())
                                    .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                                    .withBody(objectMapper.writeValueAsString(response))
                    )
        );
    }

    private void stubForPatch(CaseDocumentsMetadata requestBody, PatchDocumentMetaDataResponse response)
            throws JsonProcessingException {
        stubFor(WireMock.patch(WireMock.urlMatching(URL
                + "/" + ATTACH_TO_CASE))
                .withHeader(AUTHORIZATION, equalTo(AUTHORISATION_VALUE))
                .withHeader(SERVICE_AUTHORISATION_KEY, equalTo(SERVICE_AUTHORISATION_VALUE))
                .withRequestBody(equalToJson(objectMapper.writeValueAsString(requestBody)))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(response))
                )
        );
    }

    private void stubForPatch(CaseDocumentsMetadata requestBody, HttpStatus httpStatus)
            throws JsonProcessingException {
        stubFor(WireMock.patch(WireMock.urlMatching(URL
                + "/" + ATTACH_TO_CASE))
                .withHeader(AUTHORIZATION, equalTo(AUTHORISATION_VALUE))
                .withHeader(SERVICE_AUTHORISATION_KEY, equalTo(SERVICE_AUTHORISATION_VALUE))
                .withRequestBody(equalToJson(objectMapper.writeValueAsString(requestBody)))
                .willReturn(aResponse()
                        .withStatus(httpStatus.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                )
        );
    }

    private void assertDocumentUpload(MultipartFile multipartFile) throws JsonProcessingException {
        DocumentUploadRequest request = new DocumentUploadRequest(Classification.RESTRICTED.name(),
                                                                  CASE_TYPE_ID, JURISDICTION, List.of(multipartFile));

        Date ttl = new Date();

        Document.Links links = getLinks();

        Document mockDocument = Document.builder()
            .classification(PUBLIC)
            .hashToken(HASH_TOKEN)
            .mimeType(MIME_TYPE)
            .createdBy(TEST_USER)
            .lastModifiedBy(TEST_USER)
            .createdOn(NOW)
            .modifiedOn(NOW)
            .size(1000)
            .originalDocumentName(ORIGINAL_DOCUMENT_NAME)
            .ttl(ttl)
            .links(links)
            .build();

        UploadResponse mockResponse = new UploadResponse(List.of(mockDocument));

        stubForUpload(request, mockResponse);

        UploadResponse uploadResponse = caseDocumentClient.uploadDocuments(
            AUTHORISATION_VALUE,
            SERVICE_AUTHORISATION_VALUE,
            CASE_TYPE_ID,
            JURISDICTION, List.of(multipartFile));

        List<Document> documents = uploadResponse.getDocuments();

        assertThat(documents)
            .hasSize(1)
            .first()
            .satisfies(document -> {
                assertThat(document.classification).isEqualTo(Classification.PUBLIC);
                assertThat(document.size).isEqualTo(1000);
                assertThat(document.mimeType).isEqualTo(MIME_TYPE);
                assertThat(document.createdBy).isEqualTo(TEST_USER);
                assertThat(document.lastModifiedBy).isEqualTo(TEST_USER);
                assertThat(document.createdOn).isEqualTo(NOW);
                assertThat(document.modifiedOn).isEqualTo(NOW);
                assertThat(document.originalDocumentName).isEqualTo(ORIGINAL_DOCUMENT_NAME);
                assertThat(document.hashToken).isEqualTo(HASH_TOKEN);
                assertThat(document.links.binary.href).isEqualTo(BINARY_LINK);
                assertThat(document.links.self.href).isEqualTo(SELF_LINK);
                assertThat(document.ttl).isEqualTo(ttl);
            });
    }

    private Document createDocument() {
        Document.Links links = new Document.Links();
        links.self = new Document.Link();
        links.self.href = "link:1000";
        links.binary = new Document.Link();
        links.binary.href = "link:1000/binary";

        Date date = Date.from(Instant.now());

        return Document.builder()
            .createdOn(date)
            .classification(Classification.PUBLIC)
            .size(10)
            .mimeType("mimeType")
            .links(links).build();
    }

    private Document.Links getLinks() {

        Document.Links links = new Document.Links();
        Document.Link self = new Document.Link();
        self.href = SELF_LINK;
        Document.Link binary = new Document.Link();
        binary.href = BINARY_LINK;
        links.self = self;
        links.binary = binary;

        return links;
    }

}
