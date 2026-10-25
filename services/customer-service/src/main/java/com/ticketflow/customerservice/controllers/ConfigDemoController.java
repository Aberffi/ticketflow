package com.ticketflow.customerservice.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Shows a value coming from the Config Server. {@code @RefreshScope} makes Spring rebuild this
 * bean when {@code POST /actuator/refresh} is called, so a changed value is visible without restart.
 */
@RestController
@RefreshScope
@RequestMapping("/api/customers")
public class ConfigDemoController {

    private final String message;

    public ConfigDemoController(@Value("${ticketflow.demo.message:default value (Config Server not used)}") String message) {
        this.message = message;
    }

    @GetMapping("/config-demo")
    public Map<String, String> configDemo() {
        return Map.of("message", message);
    }
}
