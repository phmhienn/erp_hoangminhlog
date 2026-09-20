package vn.hoangminh.erp.Ht1DonHang.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "DonHang")
@Getter @Setter
public class DonHang {
  @Id
  @Column(name = "maDonHang", length = 10)
  private String maDonHang;
  @Column(name = "maKhachHang", length = 20)
  private String maKhachHang;
  @Column(name = "maDichVu", length = 20)
  private String maDichVu;
  @Column(name = "nguoiGui", length = 100)
  private String nguoiGui;
  @Column(name = "nguoiNhan", length = 100)
  private String nguoiNhan;
  @Column(name = "sdtNguoiGui", length = 15)
  private String sdtNguoiGui;
  @Column(name = "sdtNguoiNhan", length = 15)
  private String sdtNguoiNhan;
  @Column(name = "diachiLayHang", length = 255)
  private String diachiLayHang;
  @Column(name = "diachiGiaoHang", length = 255)
  private String diachiGiaoHang;
  @Column(name = "khoiLuong", precision = 10, scale = 2)
  private BigDecimal khoiLuong;
  @Column(name = "tienCOD", precision = 15, scale = 2)
  private BigDecimal tienCOD;
  @Column(name = "phiVanChuyen", precision = 15, scale = 2)
  private BigDecimal phiVanChuyen;
  @Column(name = "trangThai", length = 50)
  private String trangThai;
  @Column(name = "lyDoHuy", length = 300)
  private String lyDoHuy;
  @Column(name = "ngayTao")
  private LocalDateTime ngayTao;
}

