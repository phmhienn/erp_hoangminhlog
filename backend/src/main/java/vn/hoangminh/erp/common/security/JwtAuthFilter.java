package vn.hoangminh.erp.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.hoangminh.erp.common.security.domain.TaiKhoan;

/**
 * Bộ lọc JWT: dựng {@code Authentication} cho mỗi request.
 *
 * <p>Token hợp lệ vẫn bị từ chối nếu {@code TaiKhoan.trangThai} trong CSDL không còn
 * "Hoạt động" — đúng yêu cầu 0.4 (tài khoản khoá bị chặn), không đợi hết hạn token.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

  public static final String ATTR_LOI_TOKEN = "ERP_LOI_TOKEN";
  private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

  private final JwtService jwtService;
  private final TaiKhoanRepository taiKhoanRepository;

  public JwtAuthFilter(JwtService jwtService, TaiKhoanRepository taiKhoanRepository) {
    this.jwtService = jwtService;
    this.taiKhoanRepository = taiKhoanRepository;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {

    String header = request.getHeader("Authorization");
    if (header != null && header.startsWith("Bearer ")) {
      String token = header.substring(7).trim();
      try {
        NguoiDungHienTai tuToken = jwtService.docToken(token);
        TaiKhoan tk = taiKhoanRepository.findById(tuToken.maTaiKhoan()).orElse(null);
        if (tk != null && tk.conHoatDong()) {
          UsernamePasswordAuthenticationToken auth =
              new UsernamePasswordAuthenticationToken(tuToken, null, tuToken.authorities());
          SecurityContextHolder.getContext().setAuthentication(auth);
        } else {
          request.setAttribute(ATTR_LOI_TOKEN, "auth.dangnhap.taikhoan.khonghoatdong");
          SecurityContextHolder.clearContext();
        }
      } catch (Exception e) {
        log.debug("Token khong dung: {}", e.getMessage());
        request.setAttribute(ATTR_LOI_TOKEN, "auth.token.khonghople");
        SecurityContextHolder.clearContext();
      }
    }
    chain.doFilter(request, response);
  }
}
