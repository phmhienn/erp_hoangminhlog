package vn.hoangminh.erp.common.sequence;

/**
 * Các loại chứng từ được cấp mã tự động, tập trung (mục 4 Backend):
 * {@code DH0000001, PN…, PX…, KK…, CV…, GD…, PT/PC…, BB…}.
 *
 * <p>{@code bang}/{@code cotMa} lấy đúng tên trong {@code ERP_database.sql} (không chuẩn hoá lại tên).
 * Mã = tiền tố + số thứ tự 7 chữ số, vừa khít độ dài khoá của PHỤ LỤC A
 * (ví dụ {@code DonHang.maDonHang VARCHAR(10)}).
 */
public enum LoaiChungTu {
  DON_HANG("DH", "DonHang", "maDonHang"),
  HANG_HOA("HH", "HangHoa", "maHangHoa"),
  KHACH_HANG("KH", "KhachHang", "maKhachHang"),

  PHIEU_NHAP("PN", "PHIEU_NHAP", "maPhieuNhap"),
  PHIEU_XUAT("PX", "PHIEU_XUAT", "maPhieuXuat"),
  PHIEU_KIEM_KE("KK", "PHIEU_KIEM_KE", "maPhieuKiemKe"),
  TON_KHO("TK", "TON_KHO", "maTonKho"),
  BIEN_BAN_SU_CO("BB", "BienBanSuCo", "maBienBan"),
  CHI_PHI_LUU_KHO("CP", "ChiPhiLuuKho", "maChiPhi"),

  MINH_CHUNG_GIAO("MC", "MinhChungGiaoHang", "maMinhChung"),

  GIAO_DICH_COD("GD", "GiaoDichCOD", "maGiaoDich"),
  PHIEU_THU("PT", "PhieuThuChi", "maPhieu"),
  PHIEU_CHI("PC", "PhieuThuChi", "maPhieu"),
  SO_QUY("SQ", "SoQuy", "maSoQuy"),
  CONG_NO("CN", "CongNo", "maCongNo"),
  DOI_SOAT("DS", "DoiSoatCOD", "maDoiSoat"),

  TAI_KHOAN("TA", "TaiKhoan", "maTaiKhoan"),
  NHAN_VIEN("NV", "NhanVien", "maNV"),
  TAI_XE("TX", "TaiXe", "maTaiXe");

  /** Số chữ số của phần thứ tự (mã dài = tiền tố + số này). */
  public static final int SO_CHU_SO = 7;

  private final String tienTo;
  private final String bang;
  private final String cotMa;

  LoaiChungTu(String tienTo, String bang, String cotMa) {
    this.tienTo = tienTo;
    this.bang = bang;
    this.cotMa = cotMa;
  }

  public String tienTo() {
    return tienTo;
  }

  public String bang() {
    return bang;
  }

  public String cotMa() {
    return cotMa;
  }

  /** Ghép mã hoàn chỉnh từ số thứ tự. */
  public String ma(int soThuTu) {
    return tienTo + String.format("%0" + SO_CHU_SO + "d", soThuTu);
  }
}
