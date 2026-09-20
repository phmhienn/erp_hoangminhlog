package vn.hoangminh.erp.Ht4NhanSu.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Danh mục phòng ban — bảng riêng HT4 (mục 3.4.4). */
@Entity
@Table(name = "PhongBan")
@Getter
@Setter
@NoArgsConstructor
public class PhongBan {

  @Id
  @Column(name = "maPhongBan", length = 20, nullable = false)
  private String maPhongBan;

  @Column(name = "tenPhongBan", length = 100, nullable = false)
  private String tenPhongBan;
}
