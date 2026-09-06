package com.guilhermesemog.unimove.audit;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CorrelationIdFilter Tests")
class CorrelationIdFilterTests {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @Test
    @DisplayName("should preserve a valid request correlation id")
    void shouldPreserveValidCorrelationId() throws ServletException, IOException {
        UUID correlationId = UUID.randomUUID();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CorrelationIdFilter.HEADER_NAME, correlationId.toString());
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getHeader(CorrelationIdFilter.HEADER_NAME)).isEqualTo(correlationId.toString());
        assertThat(CorrelationIdContext.currentOrCreate()).isNotEqualTo(correlationId);
    }

    @Test
    @DisplayName("should replace an invalid request correlation id")
    void shouldReplaceInvalidCorrelationId() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CorrelationIdFilter.HEADER_NAME, "not-a-uuid");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThatCodeIsUuid(response.getHeader(CorrelationIdFilter.HEADER_NAME));
    }

    private void assertThatCodeIsUuid(String value) {
        assertThat(UUID.fromString(value)).isNotNull();
    }
}
