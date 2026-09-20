package vn.hoangminh.erp.Ht1DonHang.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.hoangminh.erp.Ht1DonHang.domain.LichSuDonHang;
public interface LichSuDonHangRepository extends JpaRepository<LichSuDonHang, Integer> {
  java.util.List<LichSuDonHang> findByMaDonHangOrderByMaLichSuAsc(String ma);
}

