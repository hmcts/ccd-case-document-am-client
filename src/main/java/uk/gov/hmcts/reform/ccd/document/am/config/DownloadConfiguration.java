package uk.gov.hmcts.reform.ccd.document.am.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.codec.Decoder;
import feign.jackson.JacksonDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

public class DownloadConfiguration {
    @Bean
    @Primary
    Decoder feignDecode(ObjectMapper objectMapper) {
        return new JacksonDecoder(objectMapper);
    }
}
