package vn.hoangminh.erp;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Nghiệm thu Phase 0 trên MySQL thật: schema + seed + đăng nhập từng vai trò + phân quyền.
 * Chạy bằng: {@code mvn test -Pit -DDB_PASSWORD=<mật khẩu root>}
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("integration")
class Phase0IntegrationTest {

  private static final String MAT_KHAU_DEMO = "123456";

  /** 13 vai trò + 1 tài khoản NV giao hàng thứ hai (nhiệm vụ 0.5). */
  private static final Map<String, String> TAI_KHOAN_THEO_VAI_TRO =
      Map.ofEntries(
          Map.entry("nvkd", "NV_KINH_DOANH"),
          Map.entry("qlkd", "QL_KINH_DOANH"),
          Map.entry("nvkho", "NV_KHO"),
          Map.entry("qlkho", "QL_KHO"),
          Map.entry("nvdp", "NV_DIEU_PHOI"),
          Map.entry("qldp", "QL_DIEU_PHOI"),
          Map.entry("nvgh", "NV_GIAO_HANG"),
          Map.entry("nvgh2", "NV_GIAO_HANG"),
          Map.entry("tx1", "NV_GIAO_HANG"),
          Map.entry("nvns", "NV_NHAN_SU"),
          Map.entry("qlns", "QL_NHAN_SU"),
          Map.entry("ktv", "KE_TOAN_VIEN"),
          Map.entry("thuquy", "THU_QUY"),
          Map.entry("ktt", "KE_TOAN_TRUONG"),
          Map.entry("nvthuong", "NHAN_VIEN"));

  @Autowired private TestRestTemplate rest;
  @Autowired private JdbcTemplate jdbc;

  @Test
  @DisplayName("0.2/0.3 — Schema tạo sạch, đủ 33 bảng và 3 view theo ERP_database.sql")
  void schemaDuBang() {
    Integer soBang =
        jdbc.queryForObject(
            "SELECT COUNT(*) FROM information_schema.tables "
                + "WHERE table_schema = DATABASE() AND table_type = 'BASE TABLE' AND table_name <> 'flyway_schema_history'",
            Integer.class);
    assertThat(soBang).as("số bảng vật lý").isEqualTo(33);

    Integer soView =
        jdbc.queryForObject(
            "SELECT COUNT(*) FROM information_schema.views WHERE table_schema = DATABASE()",
            Integer.class);
    assertThat(soView).as("số view hỗ trợ tra cứu").isEqualTo(3);

    // 0.3: ChiTietChuyenHang có đúng 2 cột, khoá chính kép
    List<String> cot =
        jdbc.queryForList(
            "SELECT column_name FROM information_schema.columns "
                + "WHERE table_schema = DATABASE() AND table_name = 'ChiTietChuyenHang' "
                + "ORDER BY ordinal_position",
            String.class);
    assertThat(cot).containsExactly("MaChuyen", "MaDonHang");

    Integer soKhoaNgoai =
        jdbc.queryForObject(
            "SELECT COUNT(DISTINCT constraint_name) FROM information_schema.table_constraints "
                + "WHERE table_schema = DATABASE() AND constraint_type = 'FOREIGN KEY'",
            Integer.class);
    assertThat(soKhoaNgoai).as("số ràng buộc FK").isGreaterThan(30);
  }

  @Test
  @DisplayName("0.5 — Dữ liệu nền đủ để chạy các phase sau không phải nhập tay danh mục")
  void duLieuNenDaSeed() {
    assertThat(dem("PhongBan")).isEqualTo(5);
    assertThat(dem("ChucVu")).isEqualTo(14);
    assertThat(dem("TrangThaiNhanVien")).isEqualTo(3);
    assertThat(dem("NhanVien")).isEqualTo(17);
    assertThat(dem("TaiKhoan")).isEqualTo(16);
    assertThat(dem("TaiXe")).isEqualTo(4);
    assertThat(dem("KhachHang")).isEqualTo(8);
    assertThat(dem("DichVu")).isEqualTo(5);
    assertThat(dem("SanPham")).isEqualTo(10);
    assertThat(dem("PhuongTien")).isEqualTo(5);

    // Mật khẩu phải được băm, không lưu trắng
    Integer soMatKhauBam =
        jdbc.queryForObject(
            "SELECT COUNT(*) FROM TaiKhoan WHERE matKhau LIKE '$2a$%' AND LENGTH(matKhau) = 60",
            Integer.class);
    assertThat(soMatKhauBam).as("mọi mật khẩu đều băm BCrypt").isEqualTo(16);

    // Có đúng một tài khoản không ở trạng thái hoạt động để nghiệm thu 0.4
    Integer soTaiKhoanKhoa =
        jdbc.queryForObject(
            "SELECT COUNT(*) FROM TaiKhoan WHERE trangThai <> 'Hoạt động'", Integer.class);
    assertThat(soTaiKhoanKhoa).isEqualTo(1);
  }

  @Test
  @DisplayName("0.4 — Đăng nhập thành công với từng vai trò, trả đúng quyền theo ma trận")
  @SuppressWarnings("unchecked")
  void dangNhapThanhCongVoiTungVaiTro() {
    TAI_KHOAN_THEO_VAI_TRO.forEach(
        (tenDangNhap, vaiTroMongDoi) -> {
          ResponseEntity<Map> res = dangNhap(tenDangNhap, MAT_KHAU_DEMO);
          assertThat(res.getStatusCode()).as("đăng nhập %s", tenDangNhap).isEqualTo(HttpStatus.OK);

          Map<?, ?> body = res.getBody();
          assertThat(body).isNotNull();
          assertThat((String) body.get("accessToken")).isNotBlank();
          assertThat(body.get("loaiToken")).isEqualTo("Bearer");

          Map<?, ?> nguoiDung = (Map<?, ?>) body.get("nguoiDung");
          assertThat(nguoiDung.get("vaiTro")).isEqualTo(vaiTroMongDoi);
          assertThat((String) nguoiDung.get("hoTen")).isNotBlank();
          assertThat((String) nguoiDung.get("tenVaiTro")).isNotBlank();
          assertThat((List<?>) nguoiDung.get("chucNang")).isNotEmpty();

          // Quyền trên DonHang phải đúng cột của hệ thống mình trong PHỤ LỤC B
          List<?> quyenBang = (List<?>) nguoiDung.get("quyenBang");
          Map<?, ?> donHang =
              quyenBang.stream()
                  .map(q -> (Map<?, ?>) q)
                  .filter(q -> "DON_HANG_HANG_HOA".equals(q.get("nhomBang")))
                  .findFirst()
                  .orElseThrow();
          List<String> hanhDong = (List<String>) donHang.get("hanhDong");

          switch (vaiTroMongDoi) {
            case "NV_KINH_DOANH", "QL_KINH_DOANH" ->
                assertThat(hanhDong).containsExactlyInAnyOrder("DOC", "THEM", "SUA", "HUY_MEM");
            case "NV_KHO", "QL_KHO", "NV_DIEU_PHOI", "QL_DIEU_PHOI", "NV_GIAO_HANG" ->
                assertThat(hanhDong).containsExactlyInAnyOrder("DOC", "SUA_TRANG_THAI");
            case "KE_TOAN_VIEN", "THU_QUY", "KE_TOAN_TRUONG" ->
                assertThat(hanhDong)
                    .containsExactlyInAnyOrder("DOC", "SUA_TRANG_THAI", "SUA_TIEN_COD");
            case "NV_NHAN_SU", "QL_NHAN_SU", "NHAN_VIEN" -> assertThat(hanhDong).isEmpty();
            default -> throw new IllegalStateException("vai trò chưa kiểm tra: " + vaiTroMongDoi);
          }
        });
  }

  @Test
  @DisplayName("0.4 — Sai mật khẩu bị từ chối (401) và không lộ tài khoản có tồn tại hay không")
  void saiMatKhauBiTuChoi() {
    ResponseEntity<Map> res = dangNhap("nvkd", "sai-mat-khau");
    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(res.getBody()).isNotNull();
    assertThat(res.getBody().get("maLoi")).isEqualTo("auth.dangnhap.sai");

    ResponseEntity<Map> khongTonTai = dangNhap("khongtonthai", MAT_KHAU_DEMO);
    assertThat(khongTonTai.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(khongTonTai.getBody().get("maLoi")).isEqualTo("auth.dangnhap.sai");
  }

  @Test
  @DisplayName("0.4 — Tài khoản có trangThai khác hoạt động bị từ chối đăng nhập (403)")
  void taiKhoanKhoaBiTuChoi() {
    ResponseEntity<Map> res = dangNhap("nvkho2", MAT_KHAU_DEMO);
    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(res.getBody()).isNotNull();
    assertThat(res.getBody().get("maLoi"))
        .isEqualTo("auth.dangnhap.taikhoan.khonghoatdong");
    assertThat((String) res.getBody().get("thongBao")).isNotBlank();
  }

  @Test
  @DisplayName("Thiếu dữ liệu đăng nhập -> 400, câu chữ lấy từ messages_vi.properties")
  void thieuThongTinDangNhap() {
    ResponseEntity<Map> res = rest.postForEntity("/api/auth/dang-nhap", Map.of(), Map.class);
    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(res.getBody().get("maLoi")).isEqualTo("loi.dulieu.khonghople");
    assertThat(res.getBody()).containsKey("thongBao");
  }

  @Test
  @DisplayName("API nghiệp vụ phải có token; /api/quyen/cua-toi trả đúng ma trận của người dùng")
  @SuppressWarnings("unchecked")
  void apiYeuCauToken() {
    ResponseEntity<Map> khongToken = rest.getForEntity("/api/quyen/cua-toi", Map.class);
    assertThat(khongToken.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

    String token = (String) dangNhap("nvkd", MAT_KHAU_DEMO).getBody().get("accessToken");
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);

    ResponseEntity<Map> coToken =
        rest.exchange(
            "/api/quyen/cua-toi", HttpMethod.GET, new HttpEntity<>(headers), Map.class);
    assertThat(coToken.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(coToken.getBody().get("tenDangNhap")).isEqualTo("nvkd");
    assertThat((List<String>) coToken.getBody().get("heThong")).containsExactly("HT1");

    // Token giả phải bị chặn
    HttpHeaders headersGia = new HttpHeaders();
    headersGia.setBearerAuth(token.substring(0, token.length() - 3) + "abc");
    ResponseEntity<Map> tokenGia =
        rest.exchange(
            "/api/quyen/cua-toi", HttpMethod.GET, new HttpEntity<>(headersGia), Map.class);
    assertThat(tokenGia.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  private int dem(String bang) {
    Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM " + bang, Integer.class);
    return n == null ? -1 : n;
  }

  @SuppressWarnings("rawtypes")
  private ResponseEntity<Map> dangNhap(String tenDangNhap, String matKhau) {
    return rest.postForEntity(
        "/api/auth/dang-nhap", Map.of("tenDangNhap", tenDangNhap, "matKhau", matKhau), Map.class);
  }
}
