package vn.hoangminh.erp.common.security;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.hoangminh.erp.common.security.domain.TaiKhoan;

/** Repository của bảng dùng chung {@code TaiKhoan} — phục vụ đăng nhập/phiên làm việc (0.4). */
public interface TaiKhoanRepository extends JpaRepository<TaiKhoan, String> {

  Optional<TaiKhoan> findByTenDangNhap(String tenDangNhap);

  boolean existsByTenDangNhap(String tenDangNhap);
}
