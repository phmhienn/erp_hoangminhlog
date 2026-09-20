# Báo cáo kiểm thử Phase 0–1

Ngày chạy: **14/09/2026**. Môi trường: Windows, Java 21 (biên dịch target 17),
MySQL 8 cục bộ, Node 22.12, Maven 3.9.16.

## Kết quả tự động

`mvn -f backend/pom.xml test -Pit`: **40 test, 0 failure, 0 error, 0 skipped**.
Gồm 30 unit test và 10 integration test chạy MySQL thật.

`npm --prefix frontend run build`: **ĐẠT**, bao gồm TypeScript và Vite production build.

| Chức năng | Dữ liệu thử / điều kiện | Kết quả |
|---|---|---|
| Schema và seed | 33 bảng nghiệp vụ, 3 view; danh mục, tài khoản BCrypt | ĐẠT |
| Đăng nhập | Tất cả tài khoản hoạt động; sai mật khẩu; tài khoản khóa; API thiếu token | ĐẠT |
| Ma trận quyền | Unit test quyền bảng/chức năng; API HT1 từ các hệ thống khác | ĐẠT |
| Khách hàng | Tạo → sửa → tìm email; điện thoại vượt 10 ký tự | ĐẠT |
| Tạo đơn | Đủ thông tin, 1 dòng hàng, mã tự cấp; dữ liệu rỗng bị chặn | ĐẠT |
| Kiểm duyệt | Tạo → gửi → từ chối → sửa → gửi → duyệt | ĐẠT |
| Xung đột bước | NVKD duyệt; sửa sau gửi; duyệt lặp; gửi lặp | ĐẠT, trả 403/409 |
| Phạm vi NVKD | Đơn không thuộc người tạo hiện tại bị chặn ở service | ĐẠT |
| Hủy và lịch sử | Hủy có lý do, đơn/hàng còn nguyên, đủ 7 sự kiện với người/thời gian | ĐẠT |
| State machine | Đúng chủ sở hữu; chặn nhảy cóc, đi ngược, hủy sau nhập kho | ĐẠT |
| Báo cáo | QLKD có quyền; NVKD bị chặn; ngày ngược/sai định dạng; không có dữ liệu | ĐẠT |
| Cấp mã thử lại | Rollback trước khi mở transaction tiếp; tối đa 3 lần | ĐẠT |

Test HT1 tích hợp dùng transaction rollback để không để lại khách/đơn kiểm thử.
V3 giữ lại 3 đơn demo, phục vụ thao tác bằng giao diện.

## Kiểm tra giao diện trình duyệt

- Đăng nhập `qlkd` qua màn hình demo: thành công, menu HT1 hoạt động.
- Danh sách hiển thị đúng 3 đơn và các bước đã duyệt/chờ duyệt/cần bổ sung.
- Chi tiết `DH0000001` hiển thị hàng hóa, COD, địa chỉ và đủ lịch sử tạo/gửi/duyệt.
- Báo cáo hiển thị 3 đơn, mỗi nhóm kiểm duyệt 1 đơn và các tổng hợp ngày/tuần/tháng/KH/dịch vụ.
- Kiểm tra ảnh chụp danh sách và báo cáo; dùng lại kiểu bảng của giao diện hiện có.

## Lỗi nền tảng đã sửa

V2 chứa 5 số điện thoại giả lập vượt chiều dài schema; thay số giả lập đúng 10 ký tự.
Giữ nguyên V1. Loại trừ bảng kỹ thuật Flyway trong test đếm bảng.
Cấu hình Hibernate giữ nguyên tên bảng/cột. Chi tiết quyết định: NOTES N23.

## Phạm vi chưa nghiệm thu

HT2–HT5 và vòng đời liên phân hệ chưa triển khai. Chưa chạy kiểm thử tải/đồng thời nhiều kết nối
hoặc kiểm thử UI tự động toàn bộ biểu mẫu; cơ chế retry đã kiểm bằng unit test.
Truy vấn hiện hướng tới dữ liệu demo nhỏ. Phase tiếp theo là **Phase 2 — nhập/xuất kho**.
