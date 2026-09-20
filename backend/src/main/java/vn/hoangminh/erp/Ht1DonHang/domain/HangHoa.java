package vn.hoangminh.erp.Ht1DonHang.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "HangHoa")
@Getter @Setter
public class HangHoa {
  @Id
  @Column(name = "maHangHoa", length = 10)
  private String maHangHoa;
  @Column(name = "maDonHang", length = 10)
  private String maDonHang;
  @Column(name = "maSP", length = 20)
  private String maSP;
  @Column(name = "loaiHangHoa", length = 50)
  private String loaiHangHoa;
  @Column(name = "soLuong")
  private Integer soLuong;
  @Column(name = "trongLuong")
  private Double trongLuong;
}

