package vn.hoangminh.erp.Ht2Kho.domain;
import jakarta.persistence.*; import java.time.LocalDateTime; import lombok.Getter; import lombok.Setter;
/** [G16] Biên bản sự cố lô hàng lỗi — luồng phụ bước 4 của đặc tả 3.2.3 (thừa/thiếu/hư hỏng). */
@Entity @Table(name="BienBanSuCo") @Getter @Setter public class BienBanSuCo { @Id @Column(length=20) private String maBienBan; @Column(length=20) private String maPhieuNhap; @Column(length=10) private String maDonHang; @Column(length=50) private String loaiSuCo; @Column(length=500) private String moTa; @Column(length=255) private String duongDanAnh; @Column(length=20) private String maNVLap; private LocalDateTime ngayLap; }
