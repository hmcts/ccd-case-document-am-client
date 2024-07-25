package uk.gov.hmcts.reform.ccd.document.am.feign;

import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import feign.okhttp.OkHttpClient;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.HttpMessageConverterCustomizer;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;

public class FeignSupportConfig {

    @Bean
    public Encoder multipartFormEncoder(ObjectFactory<HttpMessageConverters> messageConverters) {
        return new SpringFormEncoder(new SpringEncoder(messageConverters));
    }

    @Bean
    public Decoder customDecoder(ObjectFactory<HttpMessageConverters> messageConverters,
                                 ObjectProvider<HttpMessageConverterCustomizer> customizers) {
        Decoder decoder = (response, type) -> new SpringDecoder(messageConverters, customizers).decode(response, type);
        return new ResponseEntityDecoder(decoder);
    }

    @Bean
    public OkHttpClient client() {
        return new OkHttpClient();
    }
}
