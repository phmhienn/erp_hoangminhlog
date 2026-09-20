package vn.hoangminh.erp.Ht1DonHang.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.hoangminh.erp.Ht1DonHang.domain.DonHang;
public interface DonHangRepository extends JpaRepository<DonHang, String> {
  @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @org.springframework.data.jpa.repository.Query("select d from DonHang d where d.maDonHang = :ma")
  java.util.Optional<DonHang> khoa(@org.springframework.data.repository.query.Param("ma") String ma);
}

