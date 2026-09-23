package vn.hoangminh.erp.Ht2Kho.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="KIEM_KE_DON_HANG") @Getter @Setter
public class KiemKeDonHang {
  @EmbeddedId private Id id;
  @Column(name="soLuongHeThong") private Integer soLuongHeThong;
  @Column(name="soLuongThucTe") private Integer soLuongThucTe;
  @Column(name="chenhLech") private Integer chenhLech;
  @Column(name="ghiChu",length=255) private String ghiChu;

  @Embeddable @Getter @Setter @EqualsAndHashCode @NoArgsConstructor @AllArgsConstructor
  public static class Id implements java.io.Serializable {
    @Column(name="maPhieuKiemKe",length=20) private String maPhieuKiemKe;
    @Column(name="maDonHang",length=10) private String maDonHang;
  }
}
