# HƯỚNG DẪN DEMO

Mỗi phase nghiệm thu xong sẽ bổ sung một mục hướng dẫn thao tác tại đây.

## Phase 7 — Kịch bản luồng phụ và nghiệm thu tổng thể

Migration V5 tạo dữ liệu demo cho các nhánh cần kiểm tra: chuyến điều phối yêu cầu điều chỉnh
(`CV0000001`), sự cố tắc đường có đường dẫn ảnh, minh chứng giao hàng, giao dịch COD sai lệch
(`GD0000001`), phiếu thu chờ duyệt (`PT0000001`) và đợt đối soát có chênh lệch (`DS0000001`).

1. Đăng nhập `nvdp`, mở kế hoạch điều phối và kiểm tra chuyến `CV0000001` đang ở trạng thái
   `Chờ điều chỉnh`, cùng lộ trình và sự cố hiện trường.
2. Đăng nhập `nvgh`, mở nhiệm vụ giao hàng để kiểm tra yêu cầu minh chứng ảnh/chữ ký và đường dẫn
   minh chứng demo.
3. Đăng nhập `ktv`, mở dữ liệu COD để kiểm tra `GD0000001` có số tiền phải thu 450.000 và thực thu
   400.000; đợt đối soát hiển thị chênh lệch 50.000.
4. Đăng nhập `thuquy`, kiểm tra quy trình cập nhật trạng thái giao dịch; đăng nhập `ktt` để duyệt
   phiếu thu sau khi thủ quỹ hoàn tất.
5. Chạy toàn bộ kiểm thử unit/integration và build frontend trước nghiệm thu.

## Phase 1 — Khách hàng và đơn hàng

Chạy backend với biến môi trường `DB_PASSWORD`, frontend bằng `npm --prefix frontend run dev`.
Flyway V3 tạo 3 đơn: `DH0000001` đã duyệt, `DH0000002` chờ duyệt,
`DH0000003` cần bổ sung. Dữ liệu giả lập thuộc tài khoản `nvkd`.

1. Đăng nhập `nvkd`, vào **Thông tin khách hàng**: tìm theo mã/tên/liên hệ, thêm hoặc sửa hồ sơ.
2. Vào **Tạo đơn hàng**, chọn khách hàng/dịch vụ, điền người gửi/nhận, địa chỉ, khối lượng,
   COD/phí và ít nhất một dòng hàng. Lưu nhận mã tự động, trạng thái `Đã tạo`/`Chưa gửi`.
3. Bấm **Gửi duyệt**: đơn chuyển `Chờ duyệt`, không còn nút sửa.
4. Đăng xuất, đăng nhập `qlkd`, vào **Kiểm duyệt đơn hàng**, mở **Chi tiết / lịch sử**.
   Chọn **Từ chối**, nhập lý do: đơn trả về `Cần bổ sung`.
5. Với `nvkd`, sửa đơn, gửi lại. Với `qlkd`, bấm **Duyệt**: kiểm duyệt là `Đã duyệt`,
   trạng thái vận chuyển vẫn `Đã tạo`, sẵn sàng cho Phase 2.
6. **Hủy** yêu cầu lý do, giữ lại đơn/hàng và lịch sử. Không hủy được sau nhập kho.
7. **Theo dõi đơn hàng** hỗ trợ mã, tên/mã khách và khoảng ngày. Mở lịch sử để xem người,
   thời gian và lý do. NVKD chỉ xem đơn mình tạo; QLKD xem toàn bộ.
8. Với `qlkd`, mở **Báo cáo đơn hàng**, lọc ngày/KH/dịch vụ, xem số lượng theo ngày/tuần/tháng
   và trạng thái. Ngày đảo ngược bị từ chối; khoảng không có đơn hiển thị thông báo rỗng.

Kiểm thử: `mvn -f backend/pom.xml test -Pit` và `npm --prefix frontend run build`.
Chi tiết kết quả tại `docs/BAO_CAO_KIEM_THU.md`. Phase tiếp theo: HT2 nhập–xuất kho.

## Phase 0 — Nền tảng, CSDL, đăng nhập, dữ liệu nền

Điều kiện: backend chạy (`mvn -f backend/pom.xml spring-boot:run` với `DB_PASSWORD`), frontend chạy
(`npm --prefix frontend run dev`), mở http://localhost:5173.

1. **Đăng nhập từng vai trò.** Trên màn hình đăng nhập bấm một ô trong khối
   "Tài khoản theo vai trò" (mật khẩu tự điền `123456`) → vào hệ thống.
   Kiểm chứng nghiệm thu 0.4:
   - bấm ô **nvkho2** (tài khoản `trangThai = Đã khóa`) → bị từ chối, hiện
     "Tài khoản không ở trạng thái hoạt động, đăng nhập bị từ chối";
   - gõ sai mật khẩu → hiện "Tên đăng nhập hoặc mật khẩu không đúng".
2. **Menu theo quyền (PHỤ LỤC B + 3.x.2).** Đăng nhập lần lượt `nvkd`, `nvkho`, `nvdp`, `nvns`, `ktv`,
   `nvthuong`: thanh bên chỉ hiện đúng các phân hệ của vai trò đó; `nvthuong` chỉ còn
   "Hồ sơ của tôi" và "Phân quyền của tôi". Các màn hình chưa tới phase hiện bảng
   "Chưa triển khai — Phase N" kèm danh sách nhiệm vụ của phase đó.
3. **Ma trận phân quyền.** Vào "Phân quyền của tôi": bảng 8 nhóm bảng × HT1…HT5 hiển thị nguyên văn
   PHỤ LỤC B (ô "–" là không có quyền), cột của vai trò đang đăng nhập được tô sáng; phía dưới là
   quyền thực tế của phiên và danh sách chức năng được phép.
4. **Trang chủ.** Xem thẻ vai trò/hệ thống, bảng tiến độ phase và bảng dữ liệu nền đã seed
   (đối chiếu với `V2__seed.sql`).
5. **Kiểm tra CSDL.** Trong MySQL Workbench/CLI mở schema `erp_hoangminh`: đủ 33 bảng, FK theo
   PHỤ LỤC A; bảng `flyway_schema_history` có 2 dòng V1, V2.

## Phase 2 — Nhập, xuất và tồn kho

Đăng nhập `nvkho` để tra cứu đơn đã duyệt và lập phiếu nhập; nhập `qlkho` để mở **Phê duyệt phiếu nhập**.
Phiếu mẫu `PN0000001` của đơn `DH0000001` đang chờ duyệt. Bấm duyệt sẽ cộng tồn `SP001` và chuyển đơn
sang `Đã nhập kho` trong cùng giao dịch. **Dashboard nhập – xuất** hiển thị tổng sản phẩm, tổng tồn,
phiếu chờ duyệt và sản phẩm sắp hết. API put-away dùng `PUT /api/ht2/ton-kho/{ma}/vi-tri` với vị trí kho;
API xuất dùng `POST /api/ht2/phieu-xuat` và từ chối nếu tồn không đủ. Kiểm kê dùng `POST /api/ht2/phieu-kiem-ke`.
