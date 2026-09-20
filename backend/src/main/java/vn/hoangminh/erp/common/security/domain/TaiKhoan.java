package vn.hoangminh.erp.common.security.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tài khoản đăng nhập — <b>bảng dùng chung</b> của toàn hệ thống (mục 4.2).
 *
 * <p>PHỤ LỤC D mục 2: {@code NguoiDung} (3.3.4, 4.1) trùng đối tượng với {@code TaiKhoan}
 * nên chỉ dùng bảng này, không tạo bảng {@code NguoiDung}.
 * {@code maNV} trỏ hồ sơ nhân viên (bảng dùng chung {@code NhanVien}); đọc hồ sơ qua
 * service công khai của HT4, không join trực tiếp ngoài module.
 */
@Entity
@Table(name = "TaiKhoan")
@Getter
@Setter
@NoArgsConstructor
public class TaiKhoan {

  /** Giá trị {@code trangThai} duy nhất được phép đăng nhập (nhiệm vụ 0.4). */
  public static final String TRANG_THAI_HOAT_DONG = "Hoạt động";

  @Id
  @Column(name = "maTaiKhoan", length = 10, nullable = false)
  private String maTaiKhoan;

  @Column(name = "maNV", length = 20, nullable = false)
  private String maNV;

  @Column(name = "tenDangNhap", length = 50, nullable = false, unique = true)
  private String tenDangNhap;

  /** Mật khẩu băm BCrypt — không bao giờ trả ra ngoài API. */
  @Column(name = "matKhau", length = 255, nullable = false)
  private String matKhau;

  @Column(name = "vaiTro", length = 30, nullable = false)
  private String vaiTro;

  @Column(name = "trangThai", length = 20, nullable = false)
  private String trangThai;

  /** Tài khoản còn đăng nhập được hay không (0.4). */
  public boolean conHoatDong() {
    return TRANG_THAI_HOAT_DONG.equalsIgnoreCase(
        trangThai == null ? "" : trangThai.trim());
  }
}
