package vn.hoangminh.erp.Ht3DieuPhoi.domain;
import jakarta.persistence.*; import java.time.LocalDateTime; import lombok.Getter; import lombok.Setter;
/** [G20] Mốc hành trình của chuyến — đặc tả 3.3.2 "báo cáo mốc quan trọng theo thời gian thực". */
@Entity @Table(name="MocHanhTrinh") @Getter @Setter public class MocHanhTrinh { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="maMoc") private Integer maMoc; @Column(name="maChuyen") private Integer maChuyen; @Column(name="maDonHang",length=10) private String maDonHang; @Column(name="diem",length=255) private String diem; @Column(name="moTa",length=500) private String moTa; @Column(name="thoiGian") private LocalDateTime thoiGian; @Column(name="maNVCapNhat",length=20) private String maNVCapNhat; }
