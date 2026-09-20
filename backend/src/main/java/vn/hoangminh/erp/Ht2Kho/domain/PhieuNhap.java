package vn.hoangminh.erp.Ht2Kho.domain;
import jakarta.persistence.*; import lombok.Getter; import lombok.Setter; import java.math.BigDecimal; import java.time.LocalDate;
@Entity @Table(name="PHIEU_NHAP") @Getter @Setter public class PhieuNhap {
 @Id @Column(name="maPhieuNhap",length=20) private String maPhieuNhap;
 @Column(name="maDonHang",length=10) private String maDonHang; @Column(name="maNCC",length=20) private String maNCC;
 @Column(name="maNV",length=20) private String maNV; @Column(name="ngayNhap") private LocalDate ngayNhap;
 @Column(name="trangThai",length=30) private String trangThai; @Column(name="tongTien",precision=15,scale=2) private BigDecimal tongTien;
 @Column(name="ghiChu",length=255) private String ghiChu; @Column(name="nguoiDuyet",length=20) private String nguoiDuyet;
}
