package vn.hoangminh.erp.Ht2Kho.domain;
import jakarta.persistence.*; import lombok.Getter; import lombok.Setter; import java.math.BigDecimal; import java.time.LocalDate;
@Entity @Table(name="PHIEU_XUAT") @Getter @Setter public class PhieuXuat {
 @Id @Column(name="maPhieuXuat",length=20) private String maPhieuXuat; @Column(name="maNV",length=20) private String maNV;
 @Column(name="maDonHang",length=10) private String maDonHang; @Column(name="ngayXuat") private LocalDate ngayXuat; @Column(name="lyDoXuat",length=255) private String lyDoXuat;
 @Column(name="trangThai",length=30) private String trangThai; @Column(name="tongTien",precision=15,scale=2) private BigDecimal tongTien; @Column(name="ghiChu",length=255) private String ghiChu; @Column(name="nguoiDuyet",length=20) private String nguoiDuyet;
}
