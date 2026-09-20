# Báo cáo kiểm thử Phase 2

`mvn -f backend/pom.xml test -q`: biên dịch và toàn bộ unit test nền tảng đạt.
`mvn -f backend/pom.xml test -Pit -q` với MySQL local: Spring context, Flyway V4 và JPA mapping HT2 khởi động thành công.
`npm --prefix frontend run build`: TypeScript và Vite build đạt.

Các API đã triển khai theo thiết kế: tra cứu đơn đã duyệt, lập/gửi/duyệt/từ chối phiếu nhập, cộng tồn có khóa dòng,
put-away chống trùng vị trí, xuất kiểm tra đủ tồn, kiểm kê, dashboard và báo cáo nhập theo khoảng ngày.
Màn hình demo đã nối tra cứu đơn, lập phiếu, duyệt phiếu và dashboard; form xuất/kiểm kê chi tiết sẽ được nghiệm thu
tiếp khi hoàn thiện UI của Phase 2.
