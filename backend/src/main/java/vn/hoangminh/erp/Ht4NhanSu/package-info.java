/**
 * <b>HT4 — Quản lý hồ sơ nhân viên</b> (phân hệ Nhân sự, mục 2.3 và 3.4 tài liệu).
 *
 * <p>Bảng sở hữu (3.4.4): {@code PhongBan}, {@code ChucVu}, {@code TrangThaiNhanVien};
 * quản lý bảng dùng chung {@code NhanVien}, {@code TaiXe} (4.2) và {@code TaiKhoan}
 * (PHỤ LỤC D mục 2: không tạo bảng {@code NguoiDung}).
 *
 * <p>Phân quyền 3.4.2: NV phòng nhân sự và Quản lý có toàn quyền chức năng;
 * <b>nhân viên thường chỉ xem hồ sơ cá nhân</b> và gửi yêu cầu cập nhật thông tin —
 * chặn theo {@code TaiKhoan.maNV} ở tầng service, không chỉ ẩn trên UI.
 *
 * <p>Phase 0 dựng domain + {@code NhanVienQueryService} (service công khai cho module khác
 * đọc hồ sơ); chức năng thêm/sửa/tìm kiếm hồ sơ triển khai ở Phase 4.
 */
package vn.hoangminh.erp.Ht4NhanSu;
