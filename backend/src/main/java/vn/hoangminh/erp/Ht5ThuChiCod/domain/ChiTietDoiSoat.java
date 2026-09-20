package vn.hoangminh.erp.Ht5ThuChiCod.domain;
import jakarta.persistence.*; import lombok.Getter; import lombok.Setter; import lombok.EqualsAndHashCode;
/** [G15] Một đợt đối soát gồm nhiều giao dịch COD (2.4.5 "đối soát theo đợt"). */
@Entity @Table(name="ChiTietDoiSoat") @Getter @Setter public class ChiTietDoiSoat { @EmbeddedId private Id id; @Embeddable @Getter @Setter @EqualsAndHashCode public static class Id implements java.io.Serializable { @Column(name="maDoiSoat",length=10) private String maDoiSoat; @Column(name="maGiaoDich",length=10) private String maGiaoDich; public Id(){} public Id(String ds,String gd){maDoiSoat=ds;maGiaoDich=gd;} } }
