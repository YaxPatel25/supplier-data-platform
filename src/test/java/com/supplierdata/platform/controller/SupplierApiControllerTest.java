package com.supplierdata.platform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.supplierdata.platform.service.IngestionService;
import java.util.List;

@WebMvcTest(SupplierApiController.class)
class SupplierApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IngestionService ingestionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void acceptsSupplierSubmissionAndReturnsOk() throws Exception {
        when(ingestionService.ingest("SUP-001,Royal Seas,Ocean Star,2026-11-02,PROMO10", "CSV"))
                .thenReturn(List.of());

        var submission = new SupplierApiController.SupplierSubmission(
                "CSV", "SUP-001,Royal Seas,Ocean Star,2026-11-02,PROMO10");

        mockMvc.perform(post("/api/v1/suppliers/submissions")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(submission)))
                .andExpect(status().isOk());
    }
}
