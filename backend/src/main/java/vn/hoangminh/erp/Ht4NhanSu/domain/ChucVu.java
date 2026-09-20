package vn.hoangminh.erp.Ht4NhanSu.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Danh mục chức vụ — bảng riêng HT4 (mục 3.4.4). */
@Entity
@Table(name = "ChucVu")
@Getter
@Setter
@NoArgsConstructor
public class ChucVu {

  @Id
  @Column(name = "maChucVu", length = 20, nullable = false)
  private String maChucVu;

  @Column(name = "tenChucVu", length = 100, nullable = false)
  private String tenChucVu;
}
