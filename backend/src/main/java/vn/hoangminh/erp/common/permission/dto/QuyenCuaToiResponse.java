package vn.hoangminh.erp.common.permission.dto;

import java.util.List;

/**
 * Quyền của người dùng đang đăng nhập — trả về cho frontend qua {@code GET /api/quyen/cua-toi}
 * và kèm trong kết quả đăng nhập. Menu, nút thao tác render theo payload này
 * (mục 4 Frontend: ẩn/chặn theo quyền; ô "–" không hiển thị và API cũng chặn).
 *
 * @param heThong   các hệ thống cốt lõi vai trò này tham gia (HT1…HT5)
 * @param quyenBang quyền trên từng dòng của ma trận PHỤ LỤC B; {@code hanhDong} rỗng = ô "–"
 * @param chucNang  quyền chức năng theo 3.x.2 (màn hình/nút được phép dùng)
 */
public record QuyenCuaToiResponse(
    String maTaiKhoan,
    String maNV,
    String tenDangNhap,
    String hoTen,
    String vaiTro,
    String tenVaiTro,
    String tenPhongBan,
    String tenChucVu,
    List<String> heThong,
    List<QuyenBangDto> quyenBang,
    List<ChucNangDto> chucNang) {

  /** Một dòng của ma trận PHỤ LỤC B. */
  public record QuyenBangDto(String nhomBang, String ten, List<String> hanhDong) {
    public boolean coQuyen(String hanhDong) {
      return hanhDong != null && this.hanhDong.contains(hanhDong);
    }
  }

  /** Một chức năng (màn hình/thao tác) được phép. */
  public record ChucNangDto(String ma, String ten, String heThong) {}
}
