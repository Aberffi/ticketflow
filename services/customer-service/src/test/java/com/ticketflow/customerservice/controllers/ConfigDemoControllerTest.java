package com.ticketflow.customerservice.controllers;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ConfigDemoControllerTest {

    @Test
    void returnsTheConfiguredMessage() {
        ConfigDemoController controller = new ConfigDemoController("hello");

        assertThat(controller.configDemo()).containsEntry("message", "hello");
    }
}
