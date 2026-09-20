# Báo cáo kiểm thử Phase 4 — HT4

## Phạm vi

- Tra cứu hồ sơ theo mã/tên và bộ lọc phòng ban, chức vụ, trạng thái.
- Thêm, sửa hồ sơ; cấp mã nhân viên tự động theo quy tắc `NV0000001`.
- Quản lý trạng thái nhân viên.
- Nhân viên xem hồ sơ của chính mình và gửi yêu cầu cập nhật thông tin.
- API áp dụng `QuyenService` theo chức năng HT4 và giới hạn hồ sơ cá nhân theo `maNV`.

## Kiểm thử

- `mvn -f backend/pom.xml test -q`: đạt.
- `npm.cmd --prefix frontend run build`: đạt.

Yêu cầu cập nhật được giữ ở hàng đợi nghiệp vụ trong phiên chạy hiện tại vì lược đồ dùng chung không có bảng yêu cầu riêng; không thêm bảng ngoài thiết kế cơ sở dữ liệu.
