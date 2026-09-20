package vn.hoangminh.erp.Ht3DieuPhoi.domain;
import jakarta.persistence.*; import lombok.Getter; import lombok.Setter; import java.math.BigDecimal;
@Entity @Table(name="PhuongTien") @Getter @Setter public class PhuongTien { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="MaPhuongTien") private Integer maPhuongTien; @Column(name="BienSo",length=20) private String bienSo; @Column(name="LoaiXe",length=50) private String loaiXe; @Column(name="TaiTrong",precision=10,scale=2) private BigDecimal taiTrong; @Column(name="TrangThai",length=30) private String trangThai; }
