package net.adipappi.transport.integration.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(2) // CustomFilter a une priorité plus faible
public class CustomFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestURI = httpRequest.getRequestURI();

        // Ignorer les URI publiques
        if (requestURI.startsWith("/api/geolocation/")) {
            chain.doFilter(request, response);
        }

        // Logique du filtre pour les autres URI
        chain.doFilter(request, response); // Assurez-vous d'appeler chain.doFilter() pour toutes les requêtes
    }
}
