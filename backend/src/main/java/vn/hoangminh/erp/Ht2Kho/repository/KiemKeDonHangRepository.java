package vn.hoangminh.erp.Ht2Kho.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.hoangminh.erp.Ht2Kho.domain.KiemKeDonHang;

public interface KiemKeDonHangRepository extends JpaRepository<KiemKeDonHang,KiemKeDonHang.Id> {
  List<KiemKeDonHang> findByIdMaPhieuKiemKe(String maPhieuKiemKe);
}
