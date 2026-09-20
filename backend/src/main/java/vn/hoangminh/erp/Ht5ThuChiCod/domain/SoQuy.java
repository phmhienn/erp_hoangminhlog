package vn.hoangminh.erp.Ht5ThuChiCod.domain; import jakarta.persistence.*; import java.math.BigDecimal; import java.time.*; import lombok.*;
@Entity @Table(name="SoQuy") @Getter @Setter @NoArgsConstructor public class SoQuy { @Id @Column(length=10) String maSoQuy; String maPhieu; BigDecimal soTienThu; BigDecimal soTienChi; LocalDate ngayGhiSo; String maNVGhiSo; }
