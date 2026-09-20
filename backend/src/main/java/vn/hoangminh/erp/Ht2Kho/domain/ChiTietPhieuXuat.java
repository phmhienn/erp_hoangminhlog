package vn.hoangminh.erp.Ht2Kho.domain;
import jakarta.persistence.*; import lombok.Getter; import lombok.Setter; import java.math.BigDecimal;
@Entity @Table(name="CHI_TIET_PHIEU_XUAT") @Getter @Setter public class ChiTietPhieuXuat {
 @EmbeddedId private Id id; @Column(name="soLuong") private Integer soLuong; @Column(name="donGia",precision=15,scale=2) private BigDecimal donGia; @Column(name="thanhTien",precision=15,scale=2) private BigDecimal thanhTien;
 @Embeddable @Getter @Setter public static class Id implements java.io.Serializable { @Column(name="maPhieuXuat",length=20) private String maPhieuXuat; @Column(name="maSP",length=20) private String maSP; public Id(){} public Id(String p,String s){maPhieuXuat=p;maSP=s;} }
}
