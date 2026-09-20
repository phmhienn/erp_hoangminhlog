package vn.hoangminh.erp.Ht1DonHang.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "KhachHang")
@Getter @Setter
public class KhachHang {
  @Id
  @Column(name = "maKhachHang", length = 20)
  private String maKhachHang;
  @Column(name = "tenKhachHang", length = 100)
  private String tenKhachHang;
  @Column(name = "soDienThoai", length = 10)
  private String soDienThoai;
  @Column(name = "diachi", length = 255)
  private String diachi;
  @Column(name = "email", length = 100)
  private String email;
  @Column(name = "loaiKH", length = 30)
  private String loaiKH;
}

