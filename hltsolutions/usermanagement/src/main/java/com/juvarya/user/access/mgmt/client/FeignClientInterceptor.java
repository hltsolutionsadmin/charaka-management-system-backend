package com.juvarya.user.access.mgmt.client;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;

@Component
public class FeignClientInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate template) {
        template.header("X-juvarya-Internal-Call", "true");
    }
}
