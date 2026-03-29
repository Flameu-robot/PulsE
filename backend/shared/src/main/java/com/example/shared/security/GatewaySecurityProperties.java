package com.example.shared.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "gateway.security")
public class GatewaySecurityProperties {

    private String headerName = "X-Internal-Secret";

    private String token;
}