package vn.hoangminh.erp.Ht2Kho.repository; import org.springframework.data.jpa.repository.JpaRepository; import vn.hoangminh.erp.Ht2Kho.domain.PhieuNhap; import java.time.LocalDate; import java.util.*; public interface PhieuNhapRepository extends JpaRepository<PhieuNhap,String> { List<PhieuNhap> findByNgayNhapBetween(LocalDate a,LocalDate b); long countByTrangThai(String trangThai);  @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
 @org.springframework.data.jpa.repository.Query("select p from PhieuNhap p where p.maPhieuNhap = :ma")
 Optional<PhieuNhap> khoa(@org.springframework.data.repository.query.Param("ma") String ma);
}
