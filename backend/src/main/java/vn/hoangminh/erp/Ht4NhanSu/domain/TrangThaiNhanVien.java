package vn.hoangminh.erp.Ht4NhanSu.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Danh mục trạng thái nhân viên (đang làm việc, nghỉ việc…) — bảng riêng HT4 (mục 3.4.4). */
@Entity
@Table(name = "TrangThaiNhanVien")
@Getter
@Setter
@NoArgsConstructor
public class TrangThaiNhanVien {

  @Id
  @Column(name = "maTrangThai", length = 20, nullable = false)
  private String maTrangThai;

  @Column(name = "tenTrangThai", length = 50, nullable = false)
  private String tenTrangThai;
}
