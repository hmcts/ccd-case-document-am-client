package uk.gov.hmcts.reform.ccd.document.am.feign;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.cloud.contract.wiremock.WireMockConfigurationCustomizer;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.ccd.document.am.config.CaseDocumentManagementClientAutoConfiguration;
import uk.gov.hmcts.reform.ccd.document.am.healthcheck.InternalHealth;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.ccd.document.am"})
@SpringBootTest(classes = {CaseDocumentManagementClientAutoConfiguration.class, CaseDocumentHealthClientApi.class})
@PropertySource(value = "classpath:application.properties")
@EnableAutoConfiguration
@AutoConfigureWireMock(port = 4455)
public class CaseDocumentClientApiTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String HEALTH_ENDPOINT = "/health";

    @Autowired
    private CaseDocumentHealthClientApi caseDocumentHealthClientApi;

    @Configuration
    static class WireMockTestConfiguration {
        @Bean
        public WireMockConfigurationCustomizer wireMockConfigurationCustomizer() {
            return config -> config.extensions(new ConnectionClosedTransformer());
        }
    }

    public static void stubForHealth() throws JsonProcessingException {
        Health healthResponse = new Health.Builder(new Status("UP")).build();

        stubFor(WireMock.get(HEALTH_ENDPOINT)
                    .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(healthResponse))
                    )
        );
    }

    @Test
    public void testHealth() {
        InternalHealth response = caseDocumentHealthClientApi.health();
    }

    public static class ConnectionClosedTransformer extends ResponseDefinitionTransformer {

        @Override
        public String getName() {
            return "keep-alive-disabler";
        }

        @Override
        public ResponseDefinition transform(Request request, ResponseDefinition responseDefinition,
                                            FileSource files, Parameters parameters) {
            return ResponseDefinitionBuilder.like(responseDefinition)
                .withHeader(HttpHeaders.CONNECTION, "close")
                .build();
        }
    }

}
