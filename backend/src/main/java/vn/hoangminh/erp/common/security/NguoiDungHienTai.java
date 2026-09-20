package vn.hoangminh.erp.common.security;

import java.util.Collection;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import vn.hoangminh.erp.common.exception.LoiNghiepVuException;
import vn.hoangminh.erp.common.permission.VaiTro;
import vn.hoangminh.erp.common.security.domain.TaiKhoan;

/**
 * Người dùng của phiên làm việc hiện tại — principal gắn vào {@link Authentication}.
 * {@code maTaiKhoan} là giá trị ghi trong {@code LichSuDonHang.maTaiKhoan} (yêu cầu truy vết 4.3).
 */
public record NguoiDungHienTai(
    String maTaiKhoan, String maNV, String tenDangNhap, String hoTen, VaiTro vaiTro) {

  public static NguoiDungHienTai tu(TaiKhoan tk, String hoTen) {
    return new NguoiDungHienTai(
        tk.getMaTaiKhoan(), tk.getMaNV(), tk.getTenDangNhap(), hoTen, VaiTro.tu(tk.getVaiTro()));
  }

  /** Authority dạng {@code ROLE_<vaiTro>} để dùng với {@code @PreAuthorize("hasRole('…')")}. */
  public Collection<? extends GrantedAuthority> authorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + vaiTro.name()));
  }

  /** Người dùng đang đăng nhập; chưa đăng nhập → 401. */
  public static NguoiDungHienTai hienTai() {
    Authentication a = SecurityContextHolder.getContext().getAuthentication();
    if (a != null && a.getPrincipal() instanceof NguoiDungHienTai nd) {
      return nd;
    }
    throw LoiNghiepVuException.chuaXacThuc("auth.phien.hethang");
  }
}
