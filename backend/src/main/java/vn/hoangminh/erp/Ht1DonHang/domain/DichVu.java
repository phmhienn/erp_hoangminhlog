package vn.hoangminh.erp.Ht1DonHang.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "DichVu")
@Getter @Setter
public class DichVu {
  @Id
  @Column(name = "maDichVu", length = 20)
  private String maDichVu;
  @Column(name = "tenDichVu", length = 100)
  private String tenDichVu;
  @Column(name = "donGia", precision = 15, scale = 2)
  private BigDecimal donGia;
  @Column(name = "moTa", length = 255)
  private String moTa;
}

