package vn.hoangminh.erp.Ht1DonHang.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "LichSuDonHang")
@Getter @Setter
public class LichSuDonHang {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "maLichSu")
  private Integer maLichSu;
  @Column(name = "maDonHang", length = 10)
  private String maDonHang;
  @Column(name = "hanhDong", length = 50)
  private String hanhDong;
  @Column(name = "trangThai", length = 50)
  private String trangThai;
  @Column(name = "maTaiKhoan", length = 10)
  private String maTaiKhoan;
  @Column(name = "thoiGian")
  private LocalDateTime thoiGian;
  @Column(name = "ghiChu", length = 300)
  private String ghiChu;
}

