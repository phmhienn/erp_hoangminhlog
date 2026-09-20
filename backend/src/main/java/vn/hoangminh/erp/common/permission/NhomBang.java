package vn.hoangminh.erp.common.permission;

import java.util.List;

/**
 * Các dòng (nhóm bảng) của ma trận phân quyền PHỤ LỤC B — mục 4.3 tài liệu.
 * {@link #ten()} giữ nguyên văn chữ trong ma trận; {@link #bang()} liệt kê bảng vật lý
 * của {@code ERP_database.sql} thuộc dòng đó (kể cả bảng kỹ thuật [G15,G16]).
 *
 * <p>Hai dòng cuối KHÔNG có trong ma trận 4.3 (tài liệu bỏ sót — xem PHỤ LỤC D và NOTES.md):
 * {@link #HT5_THU_CHI} và {@link #TAI_KHOAN}. Quyền của chúng là quyết định ghi trong NOTES.md,
 * không phải chữ của tài liệu.
 */
public enum NhomBang {
  KHACH_HANG_DICH_VU("KhachHang, DichVu", List.of("KhachHang", "DichVu")),

  DON_HANG_HANG_HOA("DonHang, HangHoa", List.of("DonHang", "HangHoa", "LichSuDonHang")),

  KHO(
      "PHIEU_NHAP...TON_KHO, SanPham",
      List.of(
          "PHIEU_NHAP",
          "CHI_TIET_PHIEU_NHAP",
          "PHIEU_XUAT",
          "CHI_TIET_PHIEU_XUAT",
          "PHIEU_KIEM_KE",
          "CHI_TIET_KIEM_KE",
          "TON_KHO",
          "SanPham",
          "ChiPhiLuuKho",
          "BienBanSuCo")),

  DIEU_PHOI(
      "PhuongTien, ChuyenVan, LoTrinh, SuCoVanTai, ChiTietChuyenHang",
      List.of(
          "PhuongTien",
          "ChuyenVan",
          "LoTrinh",
          "SuCoVanTai",
          "ChiTietChuyenHang",
          "MinhChungGiaoHang")),

  NHAN_SU(
      "NhanVien, PhongBan, ChucVu, TrangThaiNhanVien",
      List.of("NhanVien", "PhongBan", "ChucVu", "TrangThaiNhanVien")),

  TAI_XE("TaiXe", List.of("TaiXe")),

  /** [NOTES] Không có dòng trong ma trận 4.3. */
  HT5_THU_CHI(
      "CongNo, GiaoDichCOD, PhieuThuChi, SoQuy, DoiSoatCOD (không có trong ma trận 4.3)",
      List.of("CongNo", "GiaoDichCOD", "PhieuThuChi", "SoQuy", "DoiSoatCOD", "ChiTietDoiSoat")),

  /** [NOTES] Không có dòng trong ma trận 4.3. */
  TAI_KHOAN("TaiKhoan (không có dòng trong ma trận 4.3)", List.of("TaiKhoan"));

  private final String ten;
  private final List<String> bang;

  NhomBang(String ten, List<String> bang) {
    this.ten = ten;
    this.bang = bang;
  }

  /** Chữ nguyên văn của dòng tương ứng trong ma trận PHỤ LỤC B. */
  public String ten() {
    return ten;
  }

  /** Tên bảng vật lý trong lược đồ thuộc dòng này. */
  public List<String> bang() {
    return bang;
  }

  /** Tra nhóm bảng theo tên bảng vật lý (dùng khi gắn quyền cho entity/repository). */
  public static NhomBang cuaBang(String tenBang) {
    for (NhomBang n : values()) {
      if (n.bang.stream().anyMatch(b -> b.equalsIgnoreCase(tenBang))) {
        return n;
      }
    }
    throw new IllegalArgumentException("Bang khong nam trong ma tran phan quyen: " + tenBang);
  }
}
