package vn.hoangminh.erp.Ht4NhanSu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.hoangminh.erp.Ht4NhanSu.domain.NhanVien;

/**
 * Repository nội bộ của module HT4. Module khác <b>không</b> dùng trực tiếp —
 * phải qua {@code NhanVienQueryService} (quy tắc module, mục 3 huong_di_lap_trinh).
 */
public interface NhanVienRepository extends JpaRepository<NhanVien, String> {}
