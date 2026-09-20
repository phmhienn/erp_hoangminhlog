package vn.hoangminh.erp.Ht4NhanSu.dto;

/**
 * Thông tin rút gọn của hồ sơ nhân viên — DTO công khai của module HT4
 * (service không trả entity ra ngoài module, mục 2 huong_di_lap_trinh).
 */
public record NhanVienTomTat(
    String maNV,
    String hoTen,
    String maPhongBan,
    String tenPhongBan,
    String maChucVu,
    String tenChucVu,
    String tenTrangThai,
    String email,
    String soDienThoai) {}
