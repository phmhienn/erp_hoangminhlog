# Báo cáo kiểm thử Phase 5 — HT5

Đã triển khai module thu chi và COD:

- Tra cứu và khởi tạo giao dịch COD theo đơn hàng.
- Cập nhật trạng thái/thực thu giao dịch.
- Lập và phê duyệt phiếu thu/chi.
- Tạo đợt đối soát COD, tổng hợp số tiền và chênh lệch.
- Tra cứu sổ quỹ.
- API chặn theo `QuyenService` và chức năng HT5.

Kiểm thử: `mvn -f backend/pom.xml test -q`, integration test với MySQL và `npm.cmd --prefix frontend run build`.
