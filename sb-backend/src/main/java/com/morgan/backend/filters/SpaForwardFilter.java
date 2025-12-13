package com.morgan.backend.filters;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class SpaForwardFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
                         throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String path = req.getRequestURI();
        log.debug("SpaForwardFilter path [{}]", path);

        // 1. Let API and static resources pass through untouched
        if (path.startsWith("/api")
            || path.startsWith("/h2")
            || path.startsWith("/swagger") // swagger-ui/...
            || path.startsWith("/v3/api-docs") // OpenAPI JSON/YAML/config
            || path.startsWith("/assets")
            || path.startsWith("/css")
            || path.startsWith("/js")
            || path.startsWith("/images")
            || path.contains(".")) {
            chain.doFilter(request, response);
            return;
        }

        // 2. Everything else → SPA (Angular)
        // Examples:
        //   /employees
        //   /employees/123
        //   /notExist ==> Angular 404 page
        RequestDispatcher dispatcher = req.getRequestDispatcher("/index.html");
        dispatcher.forward(req, res);
    }
}

