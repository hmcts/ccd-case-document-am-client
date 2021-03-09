package uk.gov.hmcts.reform.ccd.document.am.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.codec.Decoder;
import feign.jackson.JacksonDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ClientConfiguration {
    @Bean
    @Primary
    Decoder feignDecode(ObjectMapper objectMapper) {
        return new JacksonDecoder(objectMapper);
    }
}
