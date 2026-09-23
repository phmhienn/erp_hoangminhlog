package vn.hoangminh.erp.Ht2Kho.repository; import org.springframework.data.jpa.repository.JpaRepository; import vn.hoangminh.erp.Ht2Kho.domain.PhieuXuat; import java.time.LocalDate; import java.util.*; public interface PhieuXuatRepository extends JpaRepository<PhieuXuat,String> { List<PhieuXuat> findByNgayXuatBetween(LocalDate a,LocalDate b);  @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
 @org.springframework.data.jpa.repository.Query("select p from PhieuXuat p where p.maPhieuXuat = :ma")
 Optional<PhieuXuat> khoa(@org.springframework.data.repository.query.Param("ma") String ma);
 boolean existsByMaDonHangAndTrangThai(String maDonHang,String trangThai);
}
