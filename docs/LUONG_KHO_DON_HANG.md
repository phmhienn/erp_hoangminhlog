# Kho lưu trữ đơn hàng

Kho tiếp nhận và lưu giữ nguyên đơn hàng từ khách hàng sau khi HT1 duyệt. Hàng hóa trong đơn chỉ dùng để đối chiếu kiện/hàng khi tiếp nhận; kho không mua sản phẩm từ nhà cung cấp, không tính giá nhập/xuất và không quản lý mức tồn tối thiểu theo sản phẩm.

## Quy trình

1. HT1 tạo và duyệt đơn. Đơn xuất hiện trong danh sách chờ tiếp nhận.
2. Nhân viên kho đối chiếu đơn thực tế, lập phiếu nhập theo mã đơn, ghi chú tình trạng và gửi duyệt. Nếu thiếu hoặc hư hỏng, lập biên bản gắn với đơn hàng.
3. Quản lý kho duyệt phiếu nhập. Đơn chuyển sang `Đã nhập kho`, xuất hiện trong danh sách lưu kho. Phiếu nháp/chờ duyệt không làm đơn được tính là đã nhập kho.
4. HT3 có thể lập kế hoạch chuyến cho đơn đã nhập kho. Nhân viên kho lập phiếu xuất nguyên đơn và gửi quản lý duyệt khi bàn giao cho vận chuyển.
5. Duyệt phiếu xuất chuyển phiếu thành `Đã xuất`. Đơn ra khỏi danh sách lưu kho và được đếm riêng là `Đã xuất, chờ tài xế nhận`. Trạng thái chung của đơn vẫn là `Đã nhập kho` trong thời gian chờ nhận, nhằm giữ tương thích với HT3 và lược đồ cũ.
6. Tài xế nhận chuyến đã phê duyệt. Tất cả đơn trong chuyến phải có phiếu xuất `Đã xuất`; sau đó HT3 chuyển đơn thành `Đang giao`. Không cho nhận chuyến nếu còn đơn chưa được kho bàn giao.

Một phiếu mới luôn gắn với một mã đơn. Không chia số lượng sản phẩm để xuất một phần đơn. Không đổi mã đơn khi sửa phiếu; phiếu chỉ sửa khi lưu tạm hoặc bị từ chối. Một đơn không được tạo nhiều phiếu cùng chiều còn hiệu lực. Quản lý kho có quyền duyệt; nhân viên chỉ sửa/gửi phiếu của mình. Khi xuất đã hoàn tất, không thể xuất đơn lần thứ hai.

## Kiểm kê và báo cáo

- Kiểm kê theo mã đơn: `1` là có đơn trong kho, `0` là không có. Hệ thống lưu kết quả đối chiếu, không tự sửa trạng thái đơn khi phát hiện lệch.
- Có thể nhập mã đơn đang nằm ngoài danh sách lưu kho để ghi nhận trường hợp tìm thấy thừa đơn. Mã đơn phải tồn tại trong hệ thống.
- Báo cáo thống kê phiếu nhập/xuất và số đơn hoàn tất nhập/xuất theo ngày, không thống kê giá trị mua bán sản phẩm.
- Nội dung hàng trong đơn được đọc từ `HangHoa` của HT1. Các bảng tồn/chi tiết sản phẩm cũ được bảo toàn để tra cứu lịch sử trong DB, không còn là nguồn tính tồn kho cho workflow mới.
- Phiếu cũ không gắn mã đơn và kiểm kê sản phẩm cũ không hiển thị trong màn nghiệp vụ đơn hàng. Không tự suy đoán hoặc chuyển chúng sang đơn bất kỳ.

## Cập nhật database đang dùng

Backend hiện đặt `spring.flyway.enabled: false`. Trước khi chạy phiên bản này, chạy **một lần** file `backend/src/main/resources/db/migration/V12__kiem_ke_don_hang.sql` trên đúng database đã có cấu trúc V1–V11. File chỉ thêm bảng kiểm kê đơn hàng; không xóa/sửa dữ liệu cũ. Không chạy lại file SQL toàn bộ trên DB cloud đang dùng.

Nếu môi trường đã bật Flyway và có lịch sử V1–V11 hợp lệ, Flyway sẽ tự áp dụng V12. Không áp dụng thủ công rồi bật Flyway để chạy lại cùng migration.

`db/ERP_hoangminh_hoan_chinh.sql` đã được tạo lại với V1–V12 và dữ liệu demo cho bảng mới, chỉ dùng cho database mới. Dữ liệu demo sản phẩm cũ vẫn được giữ nguyên làm lịch sử, không dùng làm tồn kho trong luồng mới.

Sau khi cập nhật DB, khởi động lại backend và frontend cùng phiên bản. Các API HT2 nhập/xuất hiện nhận mã đơn, ngày và ghi chú; không nhận mã nhà cung cấp, mã sản phẩm hoặc đơn giá. `GET /api/ht2/ton-kho` trả về danh sách đơn hàng.

## Kiểm thử thủ công

Tạo/duyệt một đơn ở HT1 → lập/gửi/duyệt phiếu nhập → kiểm tra đơn có trong kho → lập chuyến HT3 → thử nhận chuyến khi chưa duyệt xuất (phải bị chặn) → lập/gửi/duyệt phiếu xuất → kiểm tra đơn ra khỏi tồn kho → tài xế nhận chuyến → đơn chuyển `Đang giao`. Thử lập phiếu trùng, đổi mã đơn trên phiếu, duyệt hai lần và kiểm kê không tìm thấy đơn để kiểm tra các nhánh lỗi.
