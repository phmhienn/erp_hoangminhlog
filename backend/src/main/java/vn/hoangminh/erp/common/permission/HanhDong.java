package vn.hoangminh.erp.common.permission;

/**
 * Hành động trong ma trận phân quyền PHỤ LỤC B (mục 4.3).
 * Ký hiệu tài liệu: Đ = Đọc, T = Thêm, S = Sửa, X = Xóa ("Đ/T/S/X"), kèm các quyền ghi chú riêng.
 */
public enum HanhDong {
  DOC("Đọc"),
  THEM("Thêm"),
  SUA("Sửa"),
  XOA("Xóa"),
  HUY_MEM("hủy mềm"),
  SUA_TRANG_THAI("Sửa (trangThai)"),
  SUA_TIEN_COD("Sửa (tienCOD)"),
  GHI_CHI_PHI_LUU_KHO("Ghi ChiPhiLuuKho");

  private final String tenHienThi;

  HanhDong(String tenHienThi) {
    this.tenHienThi = tenHienThi;
  }

  public String tenHienThi() {
    return tenHienThi;
  }
}
