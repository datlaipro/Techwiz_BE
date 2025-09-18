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
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JwtFilter.class);

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        final String path = req.getServletPath(); // quan trọng nếu có context-path
        log.debug("[JwtFilter] {} {}", req.getMethod(), path);

        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("[JwtFilter] No Authorization header, pass through");
            chain.doFilter(req, res);
            return;
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            log.warn("[JwtFilter] validateToken=false for {}", path);
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String email = jwtUtil.extractEmail(token);
        Integer userIdInt = jwtUtil.extractUserId(token);
        String rolesStr = jwtUtil.extractRoles(token);

        List<SimpleGrantedAuthority> authorities = (rolesStr == null || rolesStr.isBlank())
                ? java.util.Collections.emptyList()
                : java.util.Arrays.stream(rolesStr.split(","))
                        .map(String::trim).filter(s -> !s.isEmpty())
                        .map(SimpleGrantedAuthority::new).toList();

        log.debug("[JwtFilter] principal={}, rolesStr={}, authorities={}", email, rolesStr, authorities);

        var principal = new UserPrincipal(userIdInt != null ? userIdInt.longValue() : null, email, rolesStr);
        var authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(authentication);

        chain.doFilter(req, res);
    }

    /**
     * Bỏ qua filter cho:
     * - Preflight CORS (OPTIONS /**)
     * - Endpoint public đồng nhất với SecurityConfig:
     * POST /api/auth/login
     * POST /api/user
     * GET /api/events/**
     * GET /api/feedback/**
     * GET /api/review/**
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) {
        final String path = req.getServletPath();
        final String method = req.getMethod();

        if (HttpMethod.OPTIONS.matches(method))
            return true;

        if (HttpMethod.POST.matches(method) && "/api/auth/login".equals(path))
            return true;
        if (HttpMethod.POST.matches(method) && "/api/user".equals(path))
            return true;
        if (HttpMethod.POST.matches(method) && "/api/user/register".equals(path))
            return true;

        if (HttpMethod.GET.matches(method)) {
            if (path.startsWith("/api/events"))
                return true;
            if (path.startsWith("/api/feedback"))
                return true;
            if (path.startsWith("/api/review"))
                return true;
        }
        // KHÔNG bỏ qua /api/admin/**
        return false;
    }

    // Principal tối giản để Controller/Service dùng khi cần
    public record UserPrincipal(Long userId, String email, String roles) {
    }
}
