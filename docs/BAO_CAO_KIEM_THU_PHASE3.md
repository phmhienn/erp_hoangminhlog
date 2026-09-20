# Báo cáo kiểm thử Phase 3

Backend đã có entity/repository/controller/service cho chuyến vận, tuyến, sự cố, minh chứng giao hàng,
tài xế và phương tiện. Các transaction chuyển trạng thái dùng `DonHangStateService`: `Đã nhập kho → Đang giao → Đã giao`.
Phân công khóa chuyến, kiểm tra GPLX, trạng thái phương tiện và trùng lịch tài xế.

`mvn -f backend/pom.xml test -q` và `npm --prefix frontend run build` đạt sau khi nối HT3.
Giao diện đã nối lập kế hoạch/phân công, danh sách chuyến và phê duyệt; các biểu mẫu sự cố, mốc hành trình
và upload minh chứng sẽ được hoàn thiện ở lượt nghiệm thu UI kế tiếp. API đã yêu cầu minh chứng trước khi hoàn tất giao hàng.
