package vn.hoangminh.erp.Ht2Kho.domain;
import jakarta.persistence.*; import lombok.Getter; import lombok.Setter; import java.math.BigDecimal;
@Entity @Table(name="CHI_TIET_PHIEU_NHAP") @Getter @Setter public class ChiTietPhieuNhap {
 @EmbeddedId private Id id; @Column(name="soLuong") private Integer soLuong; @Column(name="donGia",precision=15,scale=2) private BigDecimal donGia;
 @Column(name="thanhTien",precision=15,scale=2) private BigDecimal thanhTien; @Column(name="tinhTrangHang",length=50) private String tinhTrangHang;
 @Embeddable @Getter @Setter public static class Id implements java.io.Serializable { @Column(name="maPhieuNhap",length=20) private String maPhieuNhap; @Column(name="maSP",length=20) private String maSP; public Id(){} public Id(String p,String s){maPhieuNhap=p;maSP=s;} }
}
