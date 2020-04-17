package uk.gov.hmcts.reform.ccd.document.am.feign;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.codec.Decoder;
import feign.jackson.JacksonDecoder;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import uk.gov.hmcts.reform.ccd.document.am.healthcheck.InternalHealth;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;

import java.util.UUID;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@FeignClient(name = "case-document-am-metadata-client-api", url = "${case_document_am.url}",
    configuration = CaseDocumentMetadataDownloadClientApi.DownloadConfiguration.class)
public interface CaseDocumentMetadataDownloadClientApi {

    @RequestMapping(method = RequestMethod.GET, value = "/cases/documents/{documentId}")
    Document getMetadataForDocument(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                                    @RequestHeader("ServiceAuthorization") String serviceAuth,
                                    @PathVariable("documentId") UUID documentId);


    @RequestMapping(
        method = RequestMethod.GET,
        value = "/health",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE
    )
    InternalHealth health();

    @Configuration
    class DownloadConfiguration {
        @Bean
        @Primary
        Decoder feignDecoder(ObjectMapper objectMapper) {
            return new JacksonDecoder(objectMapper);
        }
    }
}
