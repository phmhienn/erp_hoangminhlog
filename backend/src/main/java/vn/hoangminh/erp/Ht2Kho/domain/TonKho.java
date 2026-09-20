package vn.hoangminh.erp.Ht2Kho.domain;
import jakarta.persistence.*; import lombok.Getter; import lombok.Setter; import java.time.LocalDateTime;
@Entity @Table(name="TON_KHO") @Getter @Setter public class TonKho {
 @Id @Column(name="maTonKho",length=20) private String maTonKho; @Column(name="maSP",length=20) private String maSP; @Column(name="maViTri",length=50) private String maViTri;
 @Column(name="soLuongTon") private Integer soLuongTon; @Column(name="ngayCapNhat") private LocalDateTime ngayCapNhat;
}
