package vn.hoangminh.erp.common.security;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.hoangminh.erp.common.security.dto.DangNhapRequest;
import vn.hoangminh.erp.common.security.dto.DangNhapResponse;

/** API đăng nhập (nhiệm vụ 0.4). Controller không chứa luật nghiệp vụ. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/dang-nhap")
  public ResponseEntity<DangNhapResponse> dangNhap(@Valid @RequestBody DangNhapRequest request) {
    return ResponseEntity.ok(authService.dangNhap(request));
  }
}
