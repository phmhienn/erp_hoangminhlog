package vn.hoangminh.erp.Ht2Kho.domain;
import jakarta.persistence.*; import lombok.Getter; import lombok.Setter; import java.math.BigDecimal;
@Entity @Table(name="SanPham") @Getter @Setter public class SanPham {
  @Id @Column(name="maSP",length=20) private String maSP;
  @Column(name="tenSP",length=150) private String tenSP;
  @Column(name="donViTinh",length=50) private String donViTinh;
  @Column(name="gia",precision=15,scale=2) private BigDecimal gia;
  @Column(name="trangThai",length=30) private String trangThai;
  @Column(name="dieuKienBaoQuan",length=255) private String dieuKienBaoQuan;
}
