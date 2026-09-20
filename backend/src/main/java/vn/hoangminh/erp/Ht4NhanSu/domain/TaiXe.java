package vn.hoangminh.erp.Ht4NhanSu.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tài xế — <b>bảng dùng chung</b> (mục 4.2), mở rộng 1-1 của {@link NhanVien}.
 *
 * <p>PHỤ LỤC B: HT4 có Đ/T/S/X trên {@code soGPLX}, {@code loaiGPLX}; HT3 chỉ Sửa ({@code trangThai}).
 * Tài liệu không có trường ngày hết hạn bằng (PHỤ LỤC D mục 3) nên cảnh báo chứng chỉ của 3.3.2
 * đối chiếu trên {@code soGPLX}, {@code loaiGPLX}, {@code trangThai}.
 */
@Entity
@Table(name = "TaiXe")
@Getter
@Setter
@NoArgsConstructor
public class TaiXe {

  @Id
  @Column(name = "maTaiXe", length = 20, nullable = false)
  private String maTaiXe;

  @Column(name = "maNV", length = 20, nullable = false, unique = true)
  private String maNV;

  @Column(name = "soGPLX", length = 30)
  private String soGPLX;

  @Column(name = "loaiGPLX", length = 20)
  private String loaiGPLX;

  @Column(name = "trangThai", length = 30)
  private String trangThai;
}
