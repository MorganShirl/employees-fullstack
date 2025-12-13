package com.morgan.backend.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.function.Supplier;

import static org.apache.commons.lang3.StringUtils.isEmpty;

@Component
@RequiredArgsConstructor
public class MdcRequestIdFilter extends OncePerRequestFilter {

    private static final String REQUEST_ID = "requestId";
    private static final String HEADER_REQUEST_ID = "X-Request-Id";

    private final Supplier<String> uuidSupplier;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
                                    throws ServletException, IOException {
        try {
            String requestId = request.getHeader(HEADER_REQUEST_ID);

            if (isEmpty(requestId)) {
                requestId = this.uuidSupplier.get();
            }

            MDC.put(REQUEST_ID, requestId);

            chain.doFilter(request, response);
        } finally {
            MDC.remove(REQUEST_ID); // avoid MDC leaks on thread local reuse
        }
    }
}
