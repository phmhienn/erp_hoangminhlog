# Báo cáo kiểm thử Phase 6 — Tích hợp

Phase 6 không thêm endpoint mới. Phạm vi đã hoàn thành:

- Kiểm tra vòng đời `Đã tạo → Đã nhập kho → Đang giao → Đã giao → Đã đối soát`.
- Kiểm tra hệ sở hữu trạng thái: HT2, HT3, HT5; ghi sai hệ thống trả 403.
- Kiểm tra API HT5 theo ma trận: kế toán viên được đọc, nhân viên kinh doanh bị từ chối, chưa xác thực trả 401.
- Rà soát menu HT1–HT5: các chức năng đã triển khai được bật theo quyền chức năng; Phase 6 không tạo menu nghiệp vụ mới.

Kiểm thử: `mvn -f backend/pom.xml test -q`, `mvn -f backend/pom.xml test -Pit -q`, `npm.cmd --prefix frontend run build`.
