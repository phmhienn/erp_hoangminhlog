package vn.hoangminh.erp.common.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.hoangminh.erp.common.config.AppProperties;
import vn.hoangminh.erp.common.permission.VaiTro;

/** Test thuần cho JWT của phiên làm việc (nhiệm vụ 0.4) — không cần CSDL. */
class JwtServiceTest {

  private static final String SECRET = "erp-hoangminh-unit-test-secret-0123456789abcdef";

  private JwtService jwt(String secret) {
    return new JwtService(
        new AppProperties(
            new AppProperties.Jwt(secret, 480, "erp-hoangminh"),
            new AppProperties.Upload("./uploads"),
            new AppProperties.Cors("http://localhost:5173")));
  }

  private NguoiDungHienTai nguoiDungMau() {
    return new NguoiDungHienTai("TA0000001", "NV0000001", "nvkd", "Nguyễn Văn An",
        VaiTro.NV_KINH_DOANH);
  }

  @Test
  @DisplayName("Token đọc lại đúng người dùng và đúng vai trò")
  void tokenDocLaiDung() {
    JwtService service = jwt(SECRET);
    String token = service.taoToken(nguoiDungMau());

    NguoiDungHienTai doc = service.docToken(token);
    assertThat(doc.maTaiKhoan()).isEqualTo("TA0000001");
    assertThat(doc.maNV()).isEqualTo("NV0000001");
    assertThat(doc.tenDangNhap()).isEqualTo("nvkd");
    assertThat(doc.hoTen()).isEqualTo("Nguyễn Văn An");
    assertThat(doc.vaiTro()).isEqualTo(VaiTro.NV_KINH_DOANH);
  }

  @Test
  @DisplayName("Token ký bằng khoá khác bị từ chối")
  void tokenSaiKhoaBiTuChoi() {
    String token = jwt(SECRET).taoToken(nguoiDungMau());
    JwtService khacKhoa = jwt("mot-khoa-bi-mat-hoan-toan-khac-0123456789abcdef");

    assertThatThrownBy(() -> khacKhoa.docToken(token)).isInstanceOf(JwtException.class);
  }

  @Test
  @DisplayName("Token bị sửa nội dung không qua được kiểm tra chữ ký")
  void tokenSuaNoiDungBiTuChoi() {
    String token = jwt(SECRET).taoToken(nguoiDungMau());
    String giaMao = token.substring(0, token.length() - 4) + "AAAA";

    assertThatThrownBy(() -> jwt(SECRET).docToken(giaMao)).isInstanceOf(JwtException.class);
  }

  @Test
  @DisplayName("Khoá bí mật ngắn hơn 32 byte bị chặn ngay khi khởi động")
  void khoaNganBiChan() {
    assertThatThrownBy(() -> jwt("khoa-ngan"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("32 byte");
  }
}
