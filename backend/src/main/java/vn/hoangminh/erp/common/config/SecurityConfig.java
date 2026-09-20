package vn.hoangminh.erp.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import vn.hoangminh.erp.common.exception.ApiError;
import vn.hoangminh.erp.common.security.JwtAuthFilter;

/**
 * Bảo mật của ứng dụng (mục 4 Backend, mục 6 huong_di_lap_trinh):
 * phiên làm việc không trạng thái bằng JWT, phân quyền theo vai trò của {@code TaiKhoan.vaiTro},
 * lỗi trả về đúng cấu trúc {@link ApiError}.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  private static final Locale LOCALE_VI = Locale.forLanguageTag("vi-VN");

  /**
   * Các endpoint không yêu cầu đăng nhập. Ảnh minh chứng nằm ở {@code /uploads/**} vì thẻ
   * {@code <img>} của trình duyệt không gửi được header Authorization.
   */
  private static final String[] PUBLIC_ENDPOINTS = {"/api/auth/dang-nhap", "/uploads/**"};

  @Bean
  public PasswordEncoder passwordEncoder() {
    // Mật khẩu băm BCrypt lưu ở TaiKhoan.matKhau VARCHAR(255) (nhiệm vụ 0.5)
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain filterChain(
      HttpSecurity http,
      JwtAuthFilter jwtAuthFilter,
      ObjectMapper objectMapper,
      MessageSource messages,
      AppProperties props)
      throws Exception {

    http.csrf(csrf -> csrf.disable())
        .cors(cors -> cors.configurationSource(corsConfigurationSource(props)))
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(PUBLIC_ENDPOINTS)
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .exceptionHandling(
            ex ->
                ex.authenticationEntryPoint(
                        (request, response, e) -> {
                          Object maLoi = request.getAttribute(JwtAuthFilter.ATTR_LOI_TOKEN);
                          vietLoi(
                              objectMapper,
                              messages,
                              response,
                              HttpStatus.UNAUTHORIZED,
                              maLoi == null ? "auth.phien.hethang" : maLoi.toString());
                        })
                    .accessDeniedHandler(
                        (request, response, e) ->
                            vietLoi(
                                objectMapper,
                                messages,
                                response,
                                HttpStatus.FORBIDDEN,
                                "quyen.tuchoi")))
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource(AppProperties props) {
    String allowed =
        props.cors() == null ? null : props.cors().allowedOrigins();
    List<String> origins =
        allowed == null || allowed.isBlank()
            ? List.of("http://localhost:5173")
            : Arrays.stream(allowed.split(",")).map(String::trim).toList();

    CorsConfiguration cfg = new CorsConfiguration();
    cfg.setAllowedOrigins(origins);
    cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    cfg.setAllowedHeaders(List.of("*"));
    cfg.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", cfg);
    return source;
  }

  private void vietLoi(
      ObjectMapper objectMapper,
      MessageSource messages,
      HttpServletResponse response,
      HttpStatus status,
      String maLoi)
      throws IOException {
    String thongBao;
    try {
      thongBao = messages.getMessage(maLoi, null, LOCALE_VI);
    } catch (Exception e) {
      thongBao = maLoi;
    }
    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    objectMapper
        .writer()
        .writeValue(response.getWriter(), new ApiError(maLoi, thongBao, null));
  }
}
