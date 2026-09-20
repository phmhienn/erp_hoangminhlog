package vn.hoangminh.erp.Ht1DonHang.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.hoangminh.erp.Ht1DonHang.domain.HangHoa;
public interface HangHoaRepository extends JpaRepository<HangHoa, String> {
  java.util.List<HangHoa> findByMaDonHangOrderByMaHangHoa(String ma);
  void deleteByMaDonHang(String ma);
}

