package com.paypalclone.featheredoofbird.admin.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import java.util.Map;

class AdminControllerTest {

    private final AdminController controller = new AdminController();

    @Test
    void status_returnsMapWithOkStatus() {
        Map<String, Object> result = controller.status();

        assertThat(result).containsEntry("status", "ok");
    }

    @Test
    void status_returnsExactlyOneEntry() {
        Map<String, Object> result = controller.status();

        assertThat(result).hasSize(1);
    }
}
