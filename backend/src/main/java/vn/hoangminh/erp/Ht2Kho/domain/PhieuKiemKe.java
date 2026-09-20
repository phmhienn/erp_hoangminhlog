package vn.hoangminh.erp.Ht2Kho.domain;
import jakarta.persistence.*; import lombok.Getter; import lombok.Setter; import java.time.LocalDate;
@Entity @Table(name="PHIEU_KIEM_KE") @Getter @Setter public class PhieuKiemKe { @Id @Column(name="maPhieuKiemKe",length=20) private String maPhieuKiemKe; @Column(name="ngayKiemKe") private LocalDate ngayKiemKe; @Column(name="khuVucKiemKe",length=100) private String khuVucKiemKe; @Column(name="maNV",length=20) private String maNV; @Column(name="trangThai",length=30) private String trangThai; @Column(name="ghiChu",length=255) private String ghiChu; }
