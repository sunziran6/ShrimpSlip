package com.shrimpslip.common.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "shrimp.jwt")
public class JwtProperties {
    private String secret = "ShrimpSlip-JWT-Secret-Key-Must-Be-At-Least-256-Bits-Long!!!";
    private long accessTokenExpire = 7200;   // 2 hours
    private long refreshTokenExpire = 604800; // 7 days
}
