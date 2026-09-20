package vn.hoangminh.erp.Ht4NhanSu.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Hồ sơ nhân viên — <b>bảng dùng chung</b> (mục 4.2), HT4 sở hữu nghiệp vụ (Đ/T/S/X theo PHỤ LỤC B).
 * Các hệ thống khác chỉ đọc qua service công khai của HT4, không truy cập repository của HT4.
 */
@Entity
@Table(name = "NhanVien")
@Getter
@Setter
@NoArgsConstructor
public class NhanVien {

  @Id
  @Column(name = "maNV", length = 20, nullable = false)
  private String maNV;

  @Column(name = "hoTen", length = 100, nullable = false)
  private String hoTen;

  @Column(name = "ngaySinh")
  private LocalDate ngaySinh;

  @Column(name = "gioiTinh", length = 10)
  private String gioiTinh;

  @Column(name = "soDienThoai", length = 15)
  private String soDienThoai;

  @Column(name = "diaChi", length = 255)
  private String diaChi;

  @Column(name = "email", length = 100)
  private String email;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "maPhongBan")
  private PhongBan phongBan;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "maChucVu")
  private ChucVu chucVu;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "maTrangThai")
  private TrangThaiNhanVien trangThai;

  @Column(name = "ngayVaoLam")
  private LocalDate ngayVaoLam;
}
