package com.eric.apple;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("hook")
@Data
public class HookConfig {

    private String make;

    private String bark;
}
