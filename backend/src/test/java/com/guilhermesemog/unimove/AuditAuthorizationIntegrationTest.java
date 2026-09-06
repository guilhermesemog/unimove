package com.guilhermesemog.unimove;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@SpringBootTest
@AutoConfigureMockMvc
class AuditAuthorizationIntegrationTest {
    @Autowired private MockMvc mockMvc;

    @Test
    void shouldRejectNonAdminAuditAccess() throws Exception {
        mockMvc.perform(get("/admin/audit-events").with(user("student").roles("STUDENT")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/admin/analytics").with(user("student").roles("STUDENT")))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowAdminAuditAccess() throws Exception {
        mockMvc.perform(get("/admin/audit-events").param("size", "1").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/admin/analytics").param("days", "7").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/admin/analytics").param("days", "8").with(user("admin").roles("ADMIN")))
                .andExpect(status().isBadRequest());
    }
}
