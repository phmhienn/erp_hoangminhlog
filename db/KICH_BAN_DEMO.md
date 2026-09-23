# Dữ liệu demo ERP Hoàng Minh

`ERP_hoangminh_hoan_chinh.sql` tạo một database mới, giữ toàn bộ cấu trúc CREATE/ALTER từ V1–V12 và thay dữ liệu thử nghiệm cũ bằng các kịch bản dưới đây. Không import file này vào DB cloud đang có dữ liệu.

## Kịch bản đơn hàng

| Đơn | Tình huống | Có thể thử tiếp |
|---|---|---|
| DH0000001 | Đơn nháp | Kinh doanh sửa và gửi duyệt |
| DH0000002 | Chờ kinh doanh duyệt | Quản lý duyệt/từ chối |
| DH0000003 | Cần bổ sung địa chỉ | Kinh doanh sửa, gửi lại |
| DH0000004 | Đã hủy theo yêu cầu khách | Xem lý do và lịch sử |
| DH0000005 | Đã duyệt, chưa lập phiếu nhập | Kho lập phiếu tiếp nhận |
| DH0000006 | Phiếu nhập chờ duyệt, bao bì móp | Xem biên bản và quyết định tiếp nhận |
| DH0000007 | Đang lưu kho, chưa lập chuyến | Kiểm kê, lập phiếu xuất, lập chuyến |
| DH0000008 | Phiếu xuất chờ duyệt, chuyến 1 nháp | Kho duyệt xuất; điều phối phân công |
| DH0000009 | Đã xuất, chuyến 2 đã phê duyệt | Tài xế `nvgh` nhận chuyến |
| DH0000010 | Chuyến 3 đang giao, có sự cố tắc đường | Điều phối xử lý, tài xế cập nhật/giao hàng |
| DH0000011 | Đã giao, COD 120.000 chưa thu | Kế toán cập nhật giao dịch |
| DH0000012 | COD 600.000 đã đối soát và trả hết | Xem thu/chi, sổ quỹ và công nợ tất toán |
| DH0000013 | COD phải thu 300.000, thực thu 250.000 | Xử lý sai lệch 50.000; duyệt phiếu thu đang chờ |
| DH0000014 | COD 450.000, đã trả khách 200.000 | Xem công nợ 250.000; lập chi trả phần còn lại |

Chuyến 4 gom bốn đơn 11–14, có mốc giao và minh chứng theo từng đơn. Ngày demo cố định 20 và 23/09/2026; chọn khoảng ngày này khi xem báo cáo. Thời gian nhận/giao, xuất kho, đối soát và thu/chi theo đúng thứ tự.

## Kho

Kho tính tồn theo **đơn hàng**: hiện có DH0000007 và DH0000008. DH0000009 đã xuất và chờ tài xế nhận nên không còn tính là đơn lưu kho. Không có nhà cung cấp hay giá nhập/xuất.

Các bảng sản phẩm/chi tiết tồn cũ vẫn có bản ghi đối chiếu lịch sử của đơn 12 để giữ đủ dữ liệu cho 36 bảng. Tồn sản phẩm lịch sử bằng 0, đơn giá để NULL; không được dùng các bảng này thay cho danh sách đơn đang lưu kho. `SanPham` chỉ mô tả loại kiện khách gửi.

## Tài chính

- Thu đã thực hiện: 1.050.000 đồng.
- Chi đã thực hiện: 800.000 đồng.
- Số dư quỹ: 250.000 đồng, khớp tổng công nợ chưa trả.
- Phiếu thu đơn 13 đang chờ duyệt nên chưa ghi sổ hoặc tăng công nợ.
- Giao dịch chưa thu/sai lệch chưa thuộc đợt đối soát; giao dịch đã đối soát có chi tiết và trạng thái đơn tương ứng.

## Tài khoản và nhân sự

Giữ 16 tài khoản theo vai trò, mật khẩu demo `123456`: `nvkd`, `qlkd`, `nvkho`, `qlkho`, `nvdp`, `qldp`, `nvgh`, `tx1`, `nvns`, `qlns`, `ktv`, `thuquy`, `ktt`, `nvthuong`, `nvkho2`, `nvgh2`. `nvkho2` bị khóa do nhân viên đã nghỉ việc. Có tài xế thiếu GPLX để thử cảnh báo, không được gán chuyến; có xe bảo trì và yêu cầu cập nhật hồ sơ chờ xử lý/đã xử lý.

Đường dẫn minh chứng là dữ liệu tham chiếu; file SQL không chứa ảnh/chữ ký thực tế.

## Tạo lại file

Chạy `python db/build_complete.py` từ thư mục dự án. Script chỉ ghi file SQL, không kết nối hoặc thay đổi database. Dữ liệu nghiệp vụ sinh trực tiếp theo kịch bản, không chạy lại các seed thử nghiệm cũ rồi UPDATE vá dữ liệu.
