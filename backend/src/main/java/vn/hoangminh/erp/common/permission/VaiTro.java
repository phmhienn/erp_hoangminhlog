package vn.hoangminh.erp.common.permission;

import java.util.Set;

/**
 * Danh sách vai trò tối thiểu của hệ thống (nhiệm vụ 0.5 và mục 6 huong_di_lap_trinh).
 * Mỗi vai trò thuộc một hoặc nhiều hệ thống cốt lõi (mục 2.3); quyền trên từng bảng
 * suy ra từ ma trận PHỤ LỤC B (mục 4.3) theo hệ thống mà vai trò đó thuộc về.
 *
 * <p>Giá trị {@code vaiTro} lưu trong bảng dùng chung {@code TaiKhoan} (4.2).
 */
public enum VaiTro {
  NV_KINH_DOANH(Set.of("HT1")),
  QL_KINH_DOANH(Set.of("HT1")),

  NV_KHO(Set.of("HT2")),
  QL_KHO(Set.of("HT2")),

  NV_DIEU_PHOI(Set.of("HT3")),
  QL_DIEU_PHOI(Set.of("HT3")),
  NV_GIAO_HANG(Set.of("HT3")),

  NV_NHAN_SU(Set.of("HT4")),
  QL_NHAN_SU(Set.of("HT4")),

  KE_TOAN_VIEN(Set.of("HT5")),
  THU_QUY(Set.of("HT5")),
  KE_TOAN_TRUONG(Set.of("HT5")),

  /** Nhân viên thường: không có quyền trên bảng nghiệp vụ, chỉ xem hồ sơ của chính mình (3.4.2). */
  NHAN_VIEN(Set.of());

  private final Set<String> heThong;

  VaiTro(Set<String> heThong) {
    this.heThong = heThong;
  }

  /** Các hệ thống cốt lõi mà vai trò này tham gia (HT1…HT5). */
  public Set<String> heThong() {
    return heThong;
  }

  /** Ánh xạ {@code TaiKhoan.vaiTro} (chuỗi trong CSDL) sang enum; chuỗi lạ → {@code NHAN_VIEN}. */
  public static VaiTro tu(String giaTri) {
    if (giaTri == null) {
      return NHAN_VIEN;
    }
    for (VaiTro v : values()) {
      if (v.name().equalsIgnoreCase(giaTri.trim())) {
        return v;
      }
    }
    return NHAN_VIEN;
  }
}
