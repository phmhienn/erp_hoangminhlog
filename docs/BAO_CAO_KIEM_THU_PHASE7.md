# Báo cáo kiểm thử Phase 7 — Dữ liệu demo và nghiệm thu

Phase 7 bổ sung migration `V5__phase7_demo.sql` cho năm nhánh demo: điều phối cần điều chỉnh,
sự cố vận tải, minh chứng giao hàng, COD sai lệch và đối soát có chênh lệch. Dữ liệu dùng khóa ngoại
đến các đơn, nhân viên, tài xế và phương tiện đã có; không thêm bảng hoặc sửa lược đồ 33 bảng.

Đã xác nhận:

- Flyway validate/migrate thành công trên MySQL.
- Unit test: `mvn -f backend/pom.xml test -q`.
- Integration test: `mvn -f backend/pom.xml test -Pit -q` với `DB_PASSWORD=123456`.
- Frontend build: `npm.cmd --prefix frontend run build`.
- Kiểm tra định dạng thay đổi: `git diff --check`.
