

// package tmtd.event.config;

// import java.io.IOException;
// import java.util.Arrays;
// import java.util.List;
// import java.util.stream.Collectors;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.http.HttpHeaders;
// import org.springframework.http.HttpStatus;
// import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// import org.springframework.security.core.authority.SimpleGrantedAuthority;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.stereotype.Component;
// import org.springframework.web.filter.OncePerRequestFilter;

// import io.jsonwebtoken.JwtException;
// import io.jsonwebtoken.lang.Collections;
// import jakarta.servlet.FilterChain;
// import jakarta.servlet.ServletException;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;
// import tmtd.event.auth.JwtUtil;

// @Component // Thêm annotation này để Spring biết đây là một Bean
// public class JwtFilter extends OncePerRequestFilter {
//     private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);
//     private final JwtUtil jwtUtil;

//     public JwtFilter(JwtUtil jwtUtil) {
//         this.jwtUtil = jwtUtil;
//     }

//     @Override
//     protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
//             throws ServletException, IOException {
//         logger.debug("Processing request: {}", request.getRequestURI());

//         String path = request.getRequestURI();
//         String method = request.getMethod();

//         // Bỏ qua yêu cầu OPTIONS
//         if ("OPTIONS".equalsIgnoreCase(method)) {
//             logger.debug("Bypassing JwtFilter for OPTIONS request: {}", path);
//             filterChain.doFilter(request, response);
//             return;
//         }

//         // ✅ Nếu là /api/auth/login, /api/user (POST), hoặc các endpoint công khai → Bỏ qua kiểm tra JWT
//         if (request.getRequestURI().equals("/api/auth/login")
//                 || request.getRequestURI().startsWith("/api/discounts") // startsWith để bao gồm tất cả các đường dẫn con
//                 || request.getRequestURI().startsWith("/api/supplier")
//                 || request.getRequestURI().startsWith("/api/feedback")
//                 || request.getRequestURI().equals("/api/review")
//                 || request.getRequestURI().startsWith("/api/events")
//                 || request.getRequestURI().startsWith("/api/registrations")
//                 || ("POST".equalsIgnoreCase(method) && path.equals("/api/user"))) {
//             logger.debug("Bypassing JwtFilter for endpoint: {}", path);
//             filterChain.doFilter(request, response);
//             return;
//         }

//         String token = request.getHeader(HttpHeaders.AUTHORIZATION);
//         logger.debug("Authorization header: {}", token);

//         if (token == null || !token.startsWith("Bearer ")) {
//             logger.warn("Missing or invalid Authorization header");
//             response.sendError(HttpStatus.UNAUTHORIZED.value(), "Bạn cần đăng nhập!");
//             return;
//         }

//         token = token.substring(7);
//         logger.debug("Extracted token: {}", token);

//         try {
//             String email = jwtUtil.extractEmail(token);
//             String roles = jwtUtil.extractRoles(token); // Lấy roles dưới dạng String
//             Integer userId = jwtUtil.extractUserId(token); // Lấy userId
//             List<String> roleList = (roles == null || roles.isEmpty())
//                     ? Collections.emptyList()
//                     : Arrays.asList(roles.split(",")); // Chuyển String thành List, phân tách bằng dấu phẩy
//             logger.debug("Extracted email: {}, roles: {}, userId: {}", email, roleList, userId);

//             // Chuyển roles thành GrantedAuthority
//             List<SimpleGrantedAuthority> authorities = roleList.stream()
//                     .map(SimpleGrantedAuthority::new)
//                     .collect(Collectors.toList());
//             logger.debug("Extracted authorities: {}", authorities);

//             // Tạo Authentication và lưu vào SecurityContextHolder
//             UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(email, null,
//                     authorities);
//             SecurityContextHolder.getContext().setAuthentication(authToken);
//             logger.debug("Set authentication for email: {}", email);

//             // Thêm email và userId vào request attribute để controller sử dụng
//             request.setAttribute("userEmail", email);
//             request.setAttribute("userId", userId);

//             filterChain.doFilter(request, response);
//         } catch (JwtException e) {
//             logger.error("Invalid token: {}", e.getMessage());
//             response.sendError(HttpStatus.UNAUTHORIZED.value(), "Token không hợp lệ!");
//         }
//     }
// }

package tmtd.event.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tmtd.event.config.Roles;
import tmtd.event.auth.JwtUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String path = req.getRequestURI();
        String method = req.getMethod();

        // Chỉ bypass đúng các route public
        if (isPublic(path, method)) {
            chain.doFilter(req, res);
            return;
        }

        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
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
        String rolesStr = jwtUtil.extractRoles(token); // "ROLE_USER,ROLE_ORGANIZER"

        List<SimpleGrantedAuthority> auths = Arrays.stream(rolesStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        var principal = new UserPrincipal(userId, email, rolesStr);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, auths);
        ((UsernamePasswordAuthenticationToken) authentication)
                .setDetails(new WebAuthenticationDetailsSource().buildDetails(req));

        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(req, res);
    }

    private boolean isPublic(String path, String method) {
        if ("/api/auth/login".equals(path) && "POST".equalsIgnoreCase(method)) return true;
        if ("/api/user".equals(path) && "POST".equalsIgnoreCase(method)) return true;
        if ("GET".equalsIgnoreCase(method) &&
                (path.startsWith("/api/events") || path.startsWith("/api/feedback") || path.startsWith("/api/review"))) {
            return true;
        }
        return false;
    }

    // Principal tối giản: có userId + email + rawRoles (để AuthFacade dùng)
    public record UserPrincipal(Long userId, String email, String roles) {}
}
