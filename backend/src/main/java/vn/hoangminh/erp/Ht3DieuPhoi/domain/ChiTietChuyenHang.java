package vn.hoangminh.erp.Ht3DieuPhoi.domain;
import jakarta.persistence.*; import lombok.Getter; import lombok.Setter; import lombok.EqualsAndHashCode;
@Entity @Table(name="ChiTietChuyenHang") @Getter @Setter public class ChiTietChuyenHang { @EmbeddedId private Id id; @Embeddable @Getter @Setter @EqualsAndHashCode public static class Id implements java.io.Serializable { @Column(name="MaChuyen") private Integer maChuyen; @Column(name="MaDonHang",length=10) private String maDonHang; public Id(){} public Id(Integer c,String d){maChuyen=c;maDonHang=d;} } }
