package com.forum.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ForumCorsFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) res;
        HttpServletRequest request = (HttpServletRequest) req;
        System.out.println("Access-Control-Allow-Origin" + request.getHeader("Origin"));
        response.setHeader("Access-Control-Allow-Origin", "*");//request.getHeader("Origin"));
        response.setHeader("Access-Control-Allow-Headers",
                ////https://stackoverflow.com/questions/69478852/firefox-cors-missing-allow-header
                "Origin, X-Requested-With, Content-Type, Accept, Access-Control-Allow-Origin, Cache-Control, Authorization");

        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE, HEAD, TRACE, PATCH");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Credentials", "true");

        chain.doFilter(req, res);
    }

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void destroy() {
    }
}
