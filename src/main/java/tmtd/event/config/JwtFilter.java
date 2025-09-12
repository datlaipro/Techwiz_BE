package tmtd.event.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import tmtd.event.auth.JwtUtil;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            chain.doFilter(req, res);
            return;
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String email = jwtUtil.extractEmail(token);
        Integer userIdInt = jwtUtil.extractUserId(token);
        Long userId = userIdInt != null ? userIdInt.longValue() : null;

        String rolesStr = jwtUtil.extractRoles(token); // ví dụ: "ROLE_USER,ROLE_ORGANIZER"
        List<SimpleGrantedAuthority> authorities =
                (rolesStr == null || rolesStr.isBlank())
                        ? Collections.emptyList()
                        : Arrays.stream(rolesStr.split(","))
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .map(SimpleGrantedAuthority::new)
                                .collect(Collectors.toList());

        var principal = new UserPrincipal(userId, email, rolesStr);
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(principal, null, authorities);
        ((UsernamePasswordAuthenticationToken) authentication)
                .setDetails(new WebAuthenticationDetailsSource().buildDetails(req));

        org.springframework.security.core.context.SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        chain.doFilter(req, res);
    }

    /**
     * Bỏ qua filter cho:
     * - Preflight CORS (OPTIONS /**)
     * - Endpoint public đồng nhất với SecurityConfig:
     *   POST /api/auth/login
     *   POST /api/user
     *   GET  /api/events/**
     *   GET  /api/feedback/**
     *   GET  /api/review/**
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) {
        final String path = req.getRequestURI();
        final String method = req.getMethod();

        // Preflight
        if (HttpMethod.OPTIONS.matches(method)) return true;

        // Public: khớp với SecurityConfig
        if (HttpMethod.POST.matches(method) && "/api/auth/login".equals(path)) return true;
        if (HttpMethod.POST.matches(method) && "/api/user".equals(path)) return true;
   if (HttpMethod.POST.matches(method) && "/api/user/register".equals(path)) return true;  // ✅ THÊM DÒNG NÀY
        if (HttpMethod.GET.matches(method)) {
            if (path.startsWith("/api/events")) return true;
            if (path.startsWith("/api/feedback")) return true;
            if (path.startsWith("/api/review")) return true;
        }

        return false;
    }

    // Principal tối giản để Controller/Service dùng khi cần
    public record UserPrincipal(Long userId, String email, String roles) {}
}
