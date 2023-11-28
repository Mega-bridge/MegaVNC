package kr.co.megabridge.megavnc.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class LoginSuccessHandler implements AuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        List<String> roles = new ArrayList<>();
        authentication.getAuthorities().forEach(auth -> roles.add(auth.getAuthority()));

        if (roles.contains("ROLE_ADMIN")) {
            response.sendRedirect("/admin");
            return;
        }

        if (roles.contains("ROLE_USER")) {
            response.sendRedirect("/remote-pcs");
            return;
        }

        response.sendRedirect("/");
    }
}
