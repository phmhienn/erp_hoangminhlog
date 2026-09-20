# NOTES.md — QUYẾT ĐỊNH KỸ THUẬT (kèm trích dẫn mục tài liệu)

Nguyên tắc: tài liệu là đặc tả duy nhất; mọi chỗ tài liệu **im lặng hoặc mâu thuẫn** đều ghi tại đây,
không tự đổi hành vi đã mô tả (PHỤ LỤC D của `ERP_build_prompt.md`).
Ký hiệu `[Gx]` trỏ ghi chú trong `ERP_database.sql`; `[Nx]` là mục của file này.

---

## Nhóm A — Kế thừa từ lược đồ (ERP_database.sql)

- **N1** `[G1,G2]` Các bảng phụ một cột sau rút gọn không tạo bảng vật lý; `NguoiDung` không tồn tại,
  dùng bảng dùng chung `TaiKhoan` (4.2). *Trích dẫn: 4.1, 3.3.4, 4.2.*
- **N2** `[G16]` Bốn bảng kỹ thuật bắt buộc bởi hành vi tài liệu nhưng tài liệu không định nghĩa:
  `ChiPhiLuuKho` (quyền "Ghi ChiPhiLuuKho" của HT5, ma trận 4.3), `BienBanSuCo` (luồng phụ bước 4,
  đặc tả 3.2.3), `MinhChungGiaoHang` (3.3.3 bắt buộc ảnh minh chứng/chữ ký),
  `LichSuDonHang` (3.1.1, 3.1.2, 4.3 yêu cầu truy vết).
- **N3** `[G17]` Tiền tệ dùng `DECIMAL(15,2)` thay `DOUBLE` của Chương 3 để tránh sai số nhị phân;
  trọng lượng/khoảng cách giữ kiểu tài liệu khai báo.

## Nhóm B — Quyết định mới của Phase 0

- **N4 — Mã vai trò.** Tài liệu liệt kê 13 vai trò bằng chữ (mục 6 huong_di_lap_trinh; 3.x.2) nhưng
  không nêu giá trị chuỗi lưu ở `TaiKhoan.vaiTro VARCHAR(30)`. Chọn mã không dấu, đủ ngắn:
  `NV_KINH_DOANH, QL_KINH_DOANH, NV_KHO, QL_KHO, NV_DIEU_PHOI, QL_DIEU_PHOI, NV_GIAO_HANG,
  NV_NHAN_SU, QL_NHAN_SU, KE_TOAN_VIEN, THU_QUY, KE_TOAN_TRUONG, NHAN_VIEN`.
  Ánh xạ vai trò → hệ thống: HT1 {NV/QL kinh doanh}, HT2 {NV/QL kho}, HT3 {NV/QL điều phối, NV giao hàng},
  HT4 {NV/QL nhân sự}, HT5 {kế toán viên, thủ quỹ, kế toán trưởng}; `NHAN_VIEN` không thuộc hệ thống nào.
- **N5 — Giá trị `TaiKhoan.trangThai`.** Nhiệm vụ 0.4 chỉ nói "trangThai khác hoạt động bị từ chối",
  không nêu tập giá trị. Chọn `Hoạt động` / `Đã khóa`; hằng số định nghĩa một chỗ
  (`TaiKhoan.TRANG_THAI_HOAT_DONG`).
- **N6 — Mật khẩu demo.** Nhiệm vụ 0.5 yêu cầu mật khẩu băm BCrypt nhưng không nêu giá trị.
  Mọi tài khoản seed dùng `123456` (băm thật, đã kiểm chứng `matches() = true`); công bố trên màn hình
  đăng nhập vì đây là chương trình demo dữ liệu giả lập (1.3).
- **N7 — Mã HTTP khi đăng nhập thất bại.** Mục 4 Backend chỉ quy định 400/403/409 cho
  dữ liệu/quyền/trạng thái. Đăng nhập là điều kiện tiên quyết ở 3.x.3, tài liệu không nêu mã HTTP.
  Chọn: sai tên đăng nhập hoặc mật khẩu → **401**; tài khoản không hoạt động → **403**
  (đúng nghĩa "bị từ chối" của 0.4); thiếu trường → **400**.
- **N8 — Quyền Đọc của HT3 trên `TaiXe`.** Ma trận 4.3 dòng TaiXe chỉ ghi "Sửa (trangThai)" cho HT3,
  nhưng 3.3.1 bắt buộc nhập "phương tiện, tài xế" khi lập kế hoạch → không đọc được danh sách tài xế
  thì nghiệp vụ không chạy. Bổ sung `Đọc` cho HT3 trên dòng TaiXe; **không** thêm quyền ghi nào khác.
- **N9 — Hai nhóm bảng không có dòng trong ma trận 4.3.** Ma trận chỉ có 6 dòng; các bảng của HT5
  (`CongNo, GiaoDichCOD, PhieuThuChi, SoQuy, DoiSoatCOD, ChiTietDoiSoat`) và bảng `TaiKhoan`
  không xuất hiện (đúng nhận xét mục 23 của `loierp.txt`). Quyết định: chỉ hệ thống sở hữu được thao tác
  (HT5: Đ/T/S/X trên nhóm bảng của mình; HT4: Đ/T/S trên `TaiKhoan` vì tài khoản gắn 1-1 với hồ sơ
  nhân viên), mọi hệ thống khác là ô "–". Không cấp quyền chéo để tránh tự thêm hành vi.
- **N10 — Câu chữ đăng nhập.** Các bảng đặc tả 3.x.3 không mô tả luồng đăng nhập (đăng nhập chỉ là
  điều kiện tiên quyết), nên câu thông báo đăng nhập không có nguyên văn trong tài liệu. Đặt tập trung
  trong `messages_vi.properties` (`auth.*`), frontend đồng bộ qua `locales/vi.json`.
- **N11 — Mã chứng từ.** Tiền tố + 7 chữ số (`DH0000001`…), vừa khít độ dài khoá PHỤ LỤC A.
  Cấp bằng `MAX(mã) LIKE '<tiền tố>%'` trong cùng transaction với thao tác lưu; trùng do ghi đồng thời
  sẽ bị ràng buộc PK chặn và nghiệp vụ thử lại. `ChuyenVan.MaChuyen` là `INT AUTO_INCREMENT` theo lược đồ
  (3.3.4/4.1) nên mã `CV…` chỉ là dạng trình bày của số đó, không thêm cột.
- **N12 — Seed theo lớp.** Lớp 1 (danh mục nền) ở `V2__seed.sql` của Phase 0; lớp 2 (nghiệp vụ chính)
  và lớp 3 (kịch bản luồng phụ) seed ở phase tương ứng và Phase 7 (nhiệm vụ 7.1), tránh seed trước
  dữ liệu của chức năng chưa lập trình.
- **N13 — Nhân viên thường.** Theo 3.4.2, `NHAN_VIEN` chỉ xem hồ sơ chính mình và gửi yêu cầu cập nhật:
  cấp đúng hai chức năng `HT4_XEM_HO_SO_CA_NHAN`, `HT4_GUI_YEU_CAU_CAP_NHAT` (mọi vai trò đều có hai chức
  năng này vì ai cũng xem được hồ sơ của mình); chặn theo `TaiKhoan.maNV` ở tầng service, không chỉ ẩn UI.
- **N14 — Ngôn ngữ.** Locale cố định `vi_VN` (`spring.web.locale-resolver: fixed`); thông báo đọc từ
  `messages_vi.properties` theo UTF-8. Chuỗi hiển thị frontend lấy từ `locales/vi.json` đồng bộ mã.
- **N15 — Công cụ kiểm thử của chương trình thử nghiệm.** Màn hình đăng nhập có lưới tài khoản demo và
  trang "Phân quyền của tôi" hiển thị ma trận 4.3 đọc từ backend. Đây là công cụ nghiệm thu của chương
  trình demo (1.3), không phải chức năng nghiệp vụ mới; không thêm bảng hay API nghiệp vụ nào cho chúng
  ngoài `GET /api/quyen/cua-toi` và `GET /api/quyen/ma-tran` (dữ liệu tham chiếu phân quyền).
- **N16 — Baseline Flyway.** `ERP_database.sql` giữ nguyên làm nguồn đối chiếu tại `db/`; bản chạy thật
  là `backend/src/main/resources/db/migration/V1__schema.sql` (nguyên văn, chỉ bỏ `CREATE DATABASE`/`USE`
  vì JDBC tạo database bằng `createDatabaseIfNotExist=true`). Mọi thay đổi lược đồ sau này là migration
  V3+, không sửa V1.

## Nhóm C — Chờ quyết định của người dùng

- **N17** Đã nhận cấu hình MySQL từ người dùng và kiểm thử thành công ngày 14/09/2026.
  Mật khẩu chỉ truyền bằng biến môi trường khi chạy; không ghi vào mã nguồn.
- **N18** Thư viện xuất Excel / in PDF cho báo cáo 2.8: đề xuất Apache POI + OpenPDF (xem thiết kế backend
  mục 12); chưa cài vì Phase 2 mới dùng.

## Nhóm D — Phase 1 (14/09/2026)

- **N19 — Kiểm duyệt tách khỏi vận chuyển.** Sáu trạng thái ở 4.2 không có “Chờ duyệt”.
  Dùng hành động `GuiDuyet/Duyet/TuChoi` trong `LichSuDonHang` để suy ra
  `Chưa gửi/Chờ duyệt/Đã duyệt/Cần bổ sung`. Duyệt giữ `Đã tạo`; HT2 mới ghi `Đã nhập kho`.
  Lịch sử sắp theo khóa tự tăng để ổn định khi nhiều thao tác cùng giây.
- **N20 — Phạm vi NVKD.** Mục 3.1.2 giới hạn “phạm vi công việc” nhưng không có bảng phân công.
  NVKD chỉ đọc/sửa/gửi/hủy đơn mình tạo, xác định qua lịch sử `Tao`. QLKD quản lý toàn bộ.
  Danh mục khách hàng dùng chung. Service kiểm tra cả API chi tiết/lịch sử.
- **N21 — Hủy và sửa.** Chỉ hủy khi còn `Đã tạo`, bắt buộc lý do ≤300 ký tự;
  lưu người/thời gian qua lịch sử. Chỉ sửa/gửi ở `Chưa gửi` hoặc `Cần bổ sung`;
  chỉ duyệt/từ chối ở `Chờ duyệt`. Khóa dòng đơn khi xử lý để chặn ghi đồng thời sai bước.
- **N22 — Dữ liệu và tra cứu.** Thêm GET `/api/ht1/dich-vu` và GET `/api/ht1/don-hang/{ma}`
  để chọn dịch vụ, xem chi tiết. Giữ điện thoại KH ≤10 ký tự theo schema;
  điện thoại người gửi/nhận ≤15 ký tự. Đơn cần đủ thông tin, ≥1 dòng hàng, số lượng
  và trọng lượng >0; COD/phí ≥0. Mã sản phẩm để NULL chờ HT2 ánh xạ.
  Báo cáo theo ngày tạo, tuần bắt đầu thứ Hai, tháng, KH, dịch vụ và hai nhóm trạng thái.
- **N23 — Sửa lỗi nền tảng.** V2 chưa chạy thành công do 5 điện thoại giả lập dài 11 số
  trong cột VARCHAR(10). Sửa các số giả lập thành 10 số; không đổi V1.
  Kiểm tra CSDL không có nhân viên/khách/đơn sau rollback; xóa riêng bản ghi Flyway V2
  `success=0` rồi chạy lại thành công. Không xóa bảng hoặc dữ liệu nghiệp vụ.
  Hibernate dùng `PhysicalNamingStrategyStandardImpl` để giữ nguyên tên cột theo schema.
  Test số bảng loại trừ bảng kỹ thuật `flyway_schema_history`.
- **N24 — Cấp mã.** Tạo/sửa có cấp mã dùng `TransactionTemplate`; khi trùng ràng buộc
  rollback rồi thử lại tối đa 2 lần trong transaction mới. Không tiếp tục transaction
  đã rollback-only. Truy vấn hiện trả đủ dữ liệu demo, chưa tối ưu cho khối lượng lớn.
- **N25 — Demo.** V3 thêm 3 đơn kèm hàng/lịch sử: đã duyệt, chờ duyệt và cần bổ sung.
  Không tạo phiếu kho hoặc giao dịch COD trước phase sở hữu.

## Nhóm E — Phase 2 (14/09/2026)

- **N26 — Trạng thái phiếu kho.** Phiếu nhập dùng `Lưu tạm → Chờ duyệt → Đã duyệt/Từ chối`;
  chỉ `QL_KHO` được duyệt. Khi duyệt, tồn kho được cộng trong cùng transaction và đơn đã duyệt
  chuyển sang `Đã nhập kho` qua `DonHangStateService`.
- **N27 — Tồn kho.** Tồn chưa put-away dùng `maViTri=''`. Duyệt nhập khóa dòng bằng
  `PESSIMISTIC_WRITE`; nếu chưa có dòng thì tạo mã `TK…`. Xuất chỉ cho phép số lượng đủ,
  khóa dòng rồi trừ trong cùng transaction. Put-away đổi vị trí một lần, chống trùng `(maSP, maViTri)`.
- **N28 — Phạm vi Phase 2.** Đã có API entity/repository/service/controller cho nhập, duyệt,
  tồn, put-away, xuất, kiểm kê, dashboard và báo cáo nhập. Giao diện đã nối tra cứu đơn chờ nhập,
  lập phiếu nhập, phê duyệt và dashboard tồn. Form xuất/kiểm kê/báo cáo chi tiết tiếp tục hoàn thiện
  trong lần nghiệm thu UI kế tiếp; API đã sẵn sàng.

## Nhóm F — Phase 3 (14/09/2026)

- **N29 — Sở hữu trạng thái giao hàng.** HT3 chỉ chuyển `Đã nhập kho → Đang giao → Đã giao` qua
  `DonHangStateService`; không sửa trực tiếp trạng thái ở repository đơn hàng.
- **N30 — Điều kiện phân công.** Tài xế phải hoạt động và có cả số/loại GPLX; phương tiện phải `Sẵn sàng`.
  Chuyến của cùng tài xế trong khoảng bốn giờ bị cảnh báo xung đột. Thiếu hạn GPLX trong schema nên
  chưa thể kiểm tra ngày hết hạn.
- **N31 — Minh chứng.** `MinhChungGiaoHang` bắt buộc khi hoàn tất; API nhận đường dẫn tương đối đã upload.
  File upload vật lý và kiểm tra MIME/10MB theo `UploadService` sẽ được nối ở lượt UI kế tiếp.

## Nhóm G — Hoàn thiện chức năng còn thiếu so với đặc tả (15/09/2026)

- **N32 — Tự sinh COD khi giao thành công.** Mục 2.4.5 và 3.5.1 yêu cầu COD "tự động tổng hợp khi
  đơn hàng chuyển trạng thái đã giao thành công". `DonHangStateService` phát
  `DonHangDoiTrangThaiEvent`; `Ht5Service` lắng nghe và sinh `GiaoDichCOD` cho đơn có `tienCOD > 0`.
  Dùng sự kiện thay vì gọi trực tiếp để HT3 không phụ thuộc ngược vào HT5; listener chạy trong cùng
  transaction nên hoặc cùng thành công, hoặc cùng rollback.
- **N33 — Chênh lệch đối soát.** Trước đây `chenhLech` luôn ghi 0. Nay tính thật
  `Σ soTienPhaiThu − Σ soTienThucThu` của các giao dịch trong đợt, trạng thái đợt là
  `Khớp` hoặc `Có chênh lệch`. Chi tiết đợt lưu vào `ChiTietDoiSoat` [G15]; giao dịch đã đối soát bị
  khoá không cho sửa; đơn ở `Đã giao` chuyển sang `Đã đối soát` (PHỤ LỤC C — HT5 sở hữu trạng thái này).
- **N34 — Sổ quỹ và công nợ.** Duyệt phiếu thu/chi ghi một dòng `SoQuy` (thu hoặc chi) và cập nhật
  `CongNo` của khách hàng theo đơn của giao dịch: phiếu thu tăng `soTienPhaiTra`, phiếu chi tăng
  `soTienDaTra`; `soDuNo` là cột GENERATED nên không ghi tay. Trạng thái công nợ tự chuyển
  `Còn nợ`/`Đã tất toán`.
- **N35 — [G20] MocHanhTrinh.** Đặc tả 3.3.2 yêu cầu tài xế "báo cáo mốc quan trọng theo thời gian
  thực" nhưng Chương 3/4 không định nghĩa bảng. Thêm bảng kỹ thuật `MocHanhTrinh` (V6) — trước đó
  API nhận mốc rồi bỏ đi không lưu. Chỉ tài xế của chuyến mới được ghi mốc.
- **N36 — [G21] YeuCauCapNhatHoSo.** Đặc tả 3.4.2 vai trò 3 cho phép nhân viên "gửi yêu cầu cập nhật
  thông tin". Trước đây yêu cầu chỉ nằm trong hàng đợi bộ nhớ, mất khi khởi động lại và không ai duyệt
  được. Thêm bảng `YeuCauCapNhatHoSo` (V6) với luồng `Chờ xử lý → Đã xử lý/Từ chối` kèm phản hồi.
- **N37 — Nhiệm vụ và lịch sử cá nhân của NV giao hàng.** `/ht3/nhiem-vu-cua-toi` trước đây trả về
  toàn bộ chuyến của mọi tài xế và kiểm tra nhầm quyền `HT3_TRA_CUU_LICH_SU_GIAO`. Nay lọc đúng theo
  `TaiXe.maNV` của người đăng nhập và kiểm tra `HT3_NHIEM_VU_CUA_TOI`; bổ sung `/ht3/lich-su-ca-nhan`
  cho `HT3_LICH_SU_CA_NHAN` (quãng đường, thời gian thực tế theo từng chuyến).
- **N38 — Biên bản sự cố kho.** Luồng phụ bước 4 của đặc tả 3.2.3 ("ghi nhận biên bản sự cố vào CSDL")
  nay có API `/ht2/bien-ban` ghi vào bảng `BienBanSuCo` [G16] đã có sẵn trong lược đồ.
- **N39 — Báo cáo COD.** Bổ sung `/ht5/bao-cao` cho `HT5_BAO_CAO_DOI_SOAT` (bước 6 mục 3.5.1): tổng
  phải thu/thực thu, đã chi trả, còn phải trả, danh sách giao dịch sai lệch và số dư quỹ.
- **N40 — Giao diện.** Các màn hình trước đây là placeholder hoặc bảng read-only dùng chung nay có
  thao tác thật: HT2 (xuất kho, kiểm kê, báo cáo, put-away, gửi duyệt/từ chối, biên bản sự cố),
  HT3 (nhận chuyến, mốc hành trình, báo cáo sự cố, xác nhận giao hàng kèm minh chứng),
  HT4 (sửa hồ sơ, đổi trạng thái, duyệt yêu cầu cập nhật), HT5 (đối soát, phiếu thu/chi, phê duyệt,
  sổ quỹ & công nợ, báo cáo). Xuất Excel/PDF vẫn nằm ngoài phạm vi theo N18.
- **N41 — Mỗi chức năng một màn hình riêng.** Trước đây nhiều mục menu của HT3/HT4 dùng chung một
  bảng nên nhìn giống hệt nhau. Nay mỗi mục bám đúng nội dung đặc tả: HT3 tách kế hoạch (chọn đơn
  sẵn sàng từ kho + dựng lộ trình), phân công (bảng nguồn lực tài xế/xe kèm cảnh báo GPLX, xe bận),
  phê duyệt (xem trọn phương án rồi duyệt/trả lại), nhiệm vụ của tôi (thẻ nhiệm vụ có điểm giao,
  người nhận, hàng hoá, tiền thu hộ, mốc đã báo — 3.3.2), xác nhận giao hàng (theo từng đơn đang
  giao), sự cố (biểu mẫu + danh sách), tra cứu (theo mã vận đơn/biển số/ngày/khách hàng đúng 3.3.2).
  HT4 tách danh sách (lọc phòng ban/chức vụ/trạng thái + xem chi tiết) khỏi quản lý trạng thái
  (thống kê theo trạng thái + hàng đợi yêu cầu). HT5 tra cứu COD hiển thị thêm khách hàng, trạng
  thái giao hàng và chênh lệch theo 3.5.2.
- **N42 — Quyền xử lý sự cố của điều phối viên.** Đặc tả 3.3.2 liệt kê "Cập nhật và xử lý sự cố vận
  tải" thuộc vai trò nhân viên điều phối, nhưng `HT3_BAO_CAO_SU_CO` trước đây chỉ cấp cho
  `NV_GIAO_HANG` và `QL_DIEU_PHOI`. Đã bổ sung `NV_DIEU_PHOI` cho khớp tài liệu. Tài xế chỉ thấy sự
  cố trên chuyến của mình; điều phối thấy toàn bộ.
- **N43 — Theo dõi đơn hàng chỉ dành cho quản lý kinh doanh.** Theo yêu cầu nghiệp vụ của nhóm,
  mục menu "Theo dõi đơn hàng" (màn giám sát toàn bộ đơn) chỉ cấp cho `QL_KINH_DOANH`; đặc tả 3.1.2
  có liệt kê mục "Theo dõi đơn hàng" ở cả vai trò nhân viên nên đây là điểm thu hẹp có chủ ý.
  Nhân viên kinh doanh **vẫn theo dõi được đơn do mình tạo** ngay trên màn "Tạo đơn hàng"
  (khối "Đơn đã gửi kiểm duyệt"): API đọc đơn `danhSach/chiTiet/lichSu` nay chấp nhận
  `HT1_THEO_DOI_DON_HANG` **hoặc** `HT1_TAO_DON_HANG`, phạm vi dữ liệu vẫn bị `trongPhamVi` giới hạn
  đúng các đơn người đó tạo.
- **N44 — Ba màn HT1 không còn trùng lặp.** "Tạo đơn hàng" lấy biểu mẫu tiếp nhận yêu cầu làm trung
  tâm (mở sẵn) + hai khối việc của nhân viên: đơn nháp cần xử lý và đơn đã gửi kiểm duyệt.
  "Kiểm duyệt đơn hàng" chỉ còn hàng đợi đơn chờ duyệt. "Theo dõi đơn hàng" thành màn giám sát:
  bốn chỉ số (tổng đơn, chờ duyệt, cần bổ sung, tồn đọng), bộ lọc tra cứu và bảng tiến độ có cột số
  ngày chờ, đánh dấu đơn quá 2 ngày chưa vào kho — bám mục 3.1.2 vai trò 2 "kiểm tra đơn hàng bất
  thường/cần xử lý"; màn này chỉ xem, không có thao tác sửa/gửi duyệt/hủy.
- **N45 — Giữ nguyên luồng 3.1.1 sau khi tách quyền theo dõi.** Biểu đồ usecase tổng quát 3.1.3 vốn
  tách hai usecase riêng: *Theo dõi đơn hàng* gắn với quản lý kinh doanh và *Tra cứu đơn hàng* gắn
  với nhân viên kinh doanh. Vì vậy bên cạnh `HT1_THEO_DOI_DON_HANG` (chỉ `QL_KINH_DOANH`) đã bổ sung
  `HT1_TRA_CUU_DON_HANG` (chỉ `NV_KINH_DOANH`) kèm màn "Tra cứu đơn hàng": tra theo mã đơn, khách
  hàng, khoảng thời gian tạo và xem chi tiết + lịch sử — đúng câu chữ 3.1.2 "tra cứu theo mã đơn
  hàng, khách hàng hoặc thời gian tạo đơn; cung cấp thông tin cho bộ phận liên quan" và bước 6 của
  3.1.1. API `danhSach/chiTiet/lichSu` nhận một trong hai quyền, dữ liệu vẫn bị `trongPhamVi` giới
  hạn: nhân viên chỉ thấy đơn mình tạo, quản lý thấy toàn bộ. Kiểm thử lại đủ 7 bước 3.1.1 trên
  cổng 8081: tạo → gửi duyệt → kiểm duyệt → tra cứu/theo dõi → hủy, lịch sử ghi đủ 4 mốc.
- **N46 — HT2 bám biểu đồ usecase 3.2.3, hai actor không kế thừa nhau.** Trước đây `QL_KHO` được cấp
  gần như mọi chức năng của `NV_KHO`. Biểu đồ 3.2.3 không có quan hệ generalization giữa "Nhân viên
  kho" và "Người quản lí kho", nên đã tách dứt: nhân viên kho giữ tra cứu, lập – cập nhật phiếu,
  kiểm tra hàng, sắp xếp vị trí; quản lý kho giữ quản lí phiếu (duyệt/từ chối), theo dõi và báo cáo.
  Dữ liệu tồn kho dùng chung cho cả hai nên `tonKho()` nhận một trong các quyền liên quan.
- **N47 — Bổ sung "cập nhật phiếu nhập/xuất".** Usecase "Lập - cập nhật phiếu nhập_xuất kho" trước
  đây mới làm phần lập. Thêm `HT2_CAP_NHAT_PHIEU` với `PUT /ht2/phieu-nhap/{ma}` (chỉ khi phiếu ở
  `Lưu tạm` hoặc `Từ chối`) và `PUT /ht2/phieu-xuat/{ma}` (chỉ khi `Lưu tạm`).
- **N48 — Phiếu xuất tách hai bước theo quy trình 3.2.1.** Trước đây lập phiếu xuất là trừ tồn ngay,
  nên không thể "cập nhật" được. Nay lập phiếu ở trạng thái `Lưu tạm` (soạn hàng, đóng gói — bước
  2–3 luồng xuất), chỉ khi `POST /phieu-xuat/{ma}/xac-nhan` mới kiểm tra và trừ tồn trong cùng
  transaction (bước 4). Xác nhận lần hai bị chặn 409.
- **N49 — Tra cứu đúng tên usecase.** "Tra cứu đơn hàng - phiếu nhập_xuất" nay là một màn gộp ba
  loại chứng từ (đơn chờ nhập, phiếu nhập, phiếu xuất) với bộ lọc mã đơn/mã phiếu, tên khách hàng,
  khoảng ngày, mã nhân viên thực hiện và loại chứng từ — đúng 3.2.2 "tra cứu lịch sử theo mã đơn
  hàng, tên khách hàng, ngày nhập hoặc mã nhân viên thực hiện, phục vụ đối soát".
- **N50 — Báo cáo hai chiều.** `HT2_BAO_CAO_NHAP_KHO` đổi tên hiển thị thành "Báo cáo nhập – xuất
  kho" và bổ sung thống kê phía xuất: theo trạng thái, theo ngày và số lượng theo mặt hàng cho cả
  hai chiều (usecase "Báo cáo nhập_xuất kho").
- **N51 — Sửa lỗi 403 ở màn lập phiếu nhập.** Sau khi tách vai trò theo N46, `danhSachNhap()` vẫn chỉ
  cấp cho `QL_KHO`, trong khi màn "Lập – cập nhật phiếu nhập" của nhân viên kho gọi API này để hiện
  "Phiếu nhập của tôi"; một lần 403 làm hỏng cả trang (Promise.all thất bại nên danh mục sản phẩm và
  đơn chờ nhập cũng trống). Nay `danhSachNhap`/`danhSachXuat` nhận cả quyền lập lẫn quyền quản lí:
  nhân viên kho chỉ thấy phiếu do chính mình lập (lọc theo `maNV`), quản lý kho thấy toàn bộ.
  Màn "Quản lí phiếu nhập – xuất" của quản lý kho vì thế hiển thị cả hai loại phiếu, đúng tên usecase.
- **N52 — Bỏ chức năng "Sắp xếp vị trí kho".** Theo yêu cầu của nhóm: put-away chỉ xuất hiện ở phần
  mô tả quy trình 3.2.1 (bước 5 luồng nhập) và sơ đồ phân hệ Kho vận, không có trong yêu cầu chức
  năng 3.2.2 lẫn biểu đồ usecase 3.2.3. Đã gỡ `HT2_SAP_XEP_VI_TRI_KHO`, endpoint
  `PUT /ht2/ton-kho/{ma}/vi-tri`, `Ht2Service.putAway`, `PutAwayInput` và mục menu tương ứng.
  Cột `TON_KHO.maViTri` giữ nguyên trong lược đồ (mặc định rỗng) vì khoá `UNIQUE(maSP, maViTri)` và
  logic cộng/trừ tồn vẫn dựa trên nó.

## Nhóm H — Sửa lỗi HT3 theo phản hồi nghiệm thu (17/09/2026)

- **N53 — Tải ảnh thật cho minh chứng và sự cố.** N31 trước đây hoãn phần upload vật lý nên giao
  diện chỉ nhập được đường dẫn bằng tay, thực tế không đính kèm được ảnh. Nay có
  `POST /api/tep/anh` (multipart) lưu file vào `erp.upload.dir`, kiểm tra MIME `image/*` và đuôi
  jpg/jpeg/png/webp/gif, giới hạn 10MB theo `spring.servlet.multipart`. CSDL vẫn chỉ lưu đường dẫn
  tương đối `/uploads/…` đúng mục 2 huong_di_lap_trinh. Ảnh phục vụ tại `/uploads/**` và được
  permitAll vì thẻ `<img>` không gửi được header Authorization. Frontend có component dùng chung
  `ChonAnh` (chọn tệp, xem trước, xoá) dùng ở báo cáo sự cố và xác nhận giao hàng.
- **N54 — Thu hồi báo cáo sự cố.** Đặc tả không cấm rút lại sự cố đã báo nhầm; nay có
  `DELETE /ht3/su-co/{ma}`: tài xế của chuyến hoặc điều phối được thu hồi, nhưng chỉ khi điều phối
  **chưa** xử lý (`nguoiXuLy` rỗng và trạng thái khác "Đã xử lý") để không xoá mất vết đã xử lý.
- **N55 — [G22] Cập nhật xử lý vào chính sự cố cũ.** Trước đây điều phối muốn đổi trạng thái phải
  tạo sự cố mới, sự cố cũ treo mãi ở trạng thái ban đầu. Nay `PUT /ht3/su-co/{ma}` cập nhật trạng
  thái (`Chờ xử lý` / `Đang xử lý` / `Đã xử lý`) kèm `HuongXuLy` — phương án xử lý hiển thị lại cho
  tài xế đọc, đúng 3.3.2 "điều động xe thay thế hoặc đổi lộ trình kịp thời" và "yêu cầu hỗ trợ/phê
  duyệt phương án thay thế". Chỉ `NV_DIEU_PHOI`/`QL_DIEU_PHOI` được cập nhật; tài xế bị chặn.
  Sự cố mới tạo nay mang trạng thái "Chờ xử lý" cho khớp chú thích lược đồ 3.3.4 thay vì "Mới".
- **N56 — Chuyến chốt trạng thái khi giao xong.** `hoanTat()` chỉ đổi trạng thái đơn, chuyến vẫn
  treo "Đang giao" nên lịch sử cá nhân của tài xế hiển thị sai dù đơn đã giao. Nay sau khi đơn cuối
  cùng của chuyến rời trạng thái đang giao, chuyến chuyển "Hoàn thành" và ghi `ThoiGianThucTe` —
  đúng nghĩa "thời gian thực tế" của lược đồ 3.3.4 (trước đây bị ghi ở bước nhận chuyến).
- **N57 — Ảnh `/uploads` phải đi qua proxy dev.** `vite.config.ts` chỉ proxy `/api`, nên ở chế độ dev
  mọi đường dẫn `/uploads/...` bị dev server trả về `index.html`: ảnh sự cố không hiển thị, còn khi bấm
  vào ảnh thì router SPA không khớp route nào và rơi vào trang "Không có quyền truy cập". Nay proxy thêm
  `/uploads` sang backend (đã kiểm chứng: qua dev server trả `HTTP 200 image/png`, không còn HTML).
- **N58 — Xem ảnh bằng lớp phủ, không mở tab mới.** Thêm `shared/components/XemAnh.tsx`: ảnh thu nhỏ
  bấm được, phóng to ngay trong trang (đóng bằng Esc hoặc bấm nền), kèm trạng thái "Ảnh không tải được"
  khi `onError`. Dùng cho bảng sự cố (HT3) và ô xem trước của `ChonAnh`, thay cho `<a target="_blank">`.
- **N59 — Điều phối cập nhật được cả ảnh khi xử lý sự cố.** `XuLySuCoInput` nhận thêm `hinhAnh`; form
  "Cập nhật xử lý sự cố" có `ChonAnh`. Bỏ trống (null) thì giữ nguyên ảnh cũ, gửi chuỗi rỗng thì xoá ảnh —
  để điều phối bổ sung ảnh hiện trường/ảnh phương án mà không phải nhờ tài xế báo lại.
- **N60 — [G20] Mốc hành trình gắn với từng đơn.** `MocHanhTrinh` vốn chỉ gắn theo chuyến nên màn
  "Nhiệm vụ của tôi" hiện mốc ở cuối thẻ chuyến, tài xế bấm cập nhật không thấy gì đổi tại đơn. Nay
  `MocInput` có `maDonHang` (bỏ trống = mốc chung toàn chuyến), backend chặn đơn không thuộc chuyến
  (`ht3.donngoaichuyen`), mỗi đơn hiển thị mốc gần nhất và các mốc trước ngay tại dòng đơn đó, và nút
  "Cập nhật mốc" nằm ngay tại đơn với mốc kế tiếp được gợi ý sẵn theo thứ tự đặc tả 3.3.2.
- **N61 — Tra cứu lịch sử phản ánh mốc vừa báo.** `TraCuuView` bổ sung `mocGanNhat`, `thoiGianMoc`,
  `soMoc` (tính theo từng đơn, gộp cả mốc chung của chuyến) và bảng kết quả có cột "Mốc gần nhất".
  Trước đây tra cứu chỉ hiện trạng thái đơn nên tài xế cập nhật mốc xong vẫn thấy màn tra cứu y nguyên.
- **N62 — Đổi "Tra cứu đơn hàng – phiếu" thành "Báo cáo phiếu nhập/xuất".** Theo yêu cầu của nhóm,
  usecase của nhân viên kho đổi tên hiển thị trong `ChucNang`, mục menu và tiêu đề màn; đường dẫn đổi
  `/ht2/tra-cuu-don` → `/ht2/bao-cao-phieu` cho khớp tên. Mã hằng `HT2_TRA_CUU_DON_NHAP_KHO`, endpoint
  `GET /ht2/tra-cuu` và toàn bộ bộ lọc giữ nguyên nên không ảnh hưởng phân quyền hay dữ liệu.
  *Lưu ý:* HT2 nay có hai mục tên gần giống nhau nhưng khác vai trò và khác nội dung — "Báo cáo phiếu
  nhập/xuất" (NV_KHO) liệt kê từng chứng từ để đối soát, còn "Báo cáo nhập – xuất kho" (QL_KHO) là số
  liệu tổng hợp theo kỳ. Tên cũ theo biểu đồ usecase 3.2.3 được ghi lại ở N49.
- **N63 — Tìm kiếm ngay trong danh sách phiếu của mình.** Hai màn "Lập – cập nhật phiếu nhập/xuất"
  tải sẵn toàn bộ phiếu do mình lập (10 phiếu nhập trong dữ liệu demo) nên khó tìm phiếu cần sửa.
  Thêm `BoLocPhieu` ngay trong thẻ "Phiếu nhập/xuất của tôi": lọc theo mã phiếu – mã đơn – mã mặt hàng,
  trạng thái và khoảng ngày lập. Lọc tại chỗ trên máy khách (dữ liệu đã có sẵn, không gọi lại API),
  gõ tới đâu lọc tới đó, tiêu đề thẻ hiện `<đã lọc>/<tổng>`. Bảng phiếu xuất của màn "Quản lí phiếu
  nhập – xuất" (QL_KHO) cố ý không dùng bộ lọc này để quản lý vẫn thấy toàn bộ phiếu.

## Nhóm I — Sửa lỗi HT3 theo "Phòng vận tải – giao nhận.pdf" (17/09/2026)

- **N64 — [Mục 1.1] Một đơn chỉ được lập kế hoạch một lần.** Gốc lỗi: `lapKeHoach` chỉ kiểm tra đơn ở
  trạng thái "Đã nhập kho", mà lập chuyến lại không đổi trạng thái đơn, nên bấm lập bao nhiêu lần cũng
  được — cùng một đơn sinh ra nhiều chuyến với nhiều tài xế khác nhau. Nay có `donDaLapChuyen()`: đơn
  đang nằm trong bất kỳ chuyến nào chưa huỷ đều bị coi là đã có kế hoạch. `POST /ht3/chuyen` trả 409
  `ht3.dondalapchuyen`, và `donSanSang()` loại luôn đơn đó khỏi bảng "Đơn đã sẵn sàng tại kho".
- **N65 — [Mục 1.1] Sửa và xoá chuyến chưa phân công.** Thêm `PUT /ht3/chuyen/{ma}` và
  `DELETE /ht3/chuyen/{ma}`, chỉ mở cho trạng thái "Nháp"/"Cần điều chỉnh" (`ht3.chuyendakhoa` nếu đã
  phân công trở đi) và chặn xoá khi chuyến đã có biên bản sự cố (`ht3.chuyencosuco`). Xoá chuyến thì
  đơn quay lại danh sách chờ lập kế hoạch. Khi sửa, `GET /ht3/don-chon-duoc?maChuyen=` trả về đơn còn
  trống cộng đơn của chính chuyến đang sửa, nếu không đơn cũ sẽ biến mất khỏi ô chọn.
- **N66 — [Mục 1.1] Chặn dữ liệu trống và thời gian ngược.** `ChuyenInput` nay bắt buộc điểm xuất phát,
  điểm kết thúc, khoảng cách (> 0) và thời gian chạy (≥ 1 phút); service thêm `ht3.thoigianketthuc` khi
  thời gian dự kiến hoàn tất không sau giờ khởi hành. Form phía frontend cũng chặn trước khi gọi API và
  hiện rõ thiếu gì, thay vì để người dùng bấm rồi mới nhận lỗi.
- **N67 — [Mục 1.2 và 2.2] Sự cố phải có mô tả và ảnh hiện trường.** `SuCoInput.moTa` và
  `SuCoInput.hinhAnh` thêm `@NotBlank`; nút "Gửi báo cáo" khoá cho tới khi đủ chuyến, mô tả và ảnh.
- **N68 — [Mục 1.3] Hiện tên tài xế.** `TaiXeView` bổ sung `hoTen` (lấy từ `NhanVien` qua `TaiXe`); ô
  chọn tài xế và bảng nguồn lực của màn phân công hiển thị tên trước mã, trước đây chỉ có mã TX….
- **N69 — [Mục 2.1, 2.3 và 4] Trạng thái khớp nhau giữa ba màn.** Ba sửa đổi:
  (a) `nhanDon` trước đây chỉ đổi `ChuyenVan.MaDonHang` — đơn đầu tiên — nên chuyến gom nhiều đơn thì
  các đơn còn lại kẹt ở "Đã nhập kho"; nay đổi trạng thái cho mọi đơn của chuyến.
  (b) `hoanTat` tìm chuyến bằng `findFirst()` nên khi một đơn lỡ nằm ở nhiều chuyến (lỗi N64) có thể
  chốt nhầm chuyến nháp; nay ưu tiên chuyến đang "Đang giao".
  (c) `LichSuGiaoView` tách theo từng đơn và mang thêm `trangThaiDon`, vì trước đây chỉ hiện trạng thái
  chuyến nên ô "Lịch sử cá nhân của tôi" đọc là "Đang giao" trong khi ô "Kết quả" đã là "Đã giao".
  Ngoài ra `nhiemVuCuaToi` bỏ qua chuyến "Đã hủy" — chuyến đã huỷ không còn là nhiệm vụ, tài xế vẫn
  xem lại được ở tra cứu và lịch sử cá nhân.
- **N70 — [G23] Dọn dữ liệu sinh ra từ lỗi N64.** `V8__dong_bo_trang_thai_chuyen.sql`: chốt "Hoàn thành"
  cho chuyến đang giao mà mọi đơn đã giao xong, huỷ chuyến chưa khởi hành mà đơn đã được chuyến khác
  giao, và với đơn còn nằm ở nhiều chuyến thì giữ chuyến tiến xa nhất rồi huỷ phần còn lại.
- **N71 — [G24] Chuẩn hoá "Chờ điều chỉnh" → "Cần điều chỉnh".** Lỗi này không có trong báo cáo nhưng
  lộ ra khi kiểm thử: `V5__phase7_demo.sql` gieo trạng thái "Chờ điều chỉnh" còn `yeuCauDieuChinh()` ghi
  "Cần điều chỉnh", nên chuyến CV0000001 không lọt vào bộ lọc nào — không sửa, xoá, phân công hay duyệt
  được mà vẫn hiện ở "Nhiệm vụ của tôi". `V9__chuan_hoa_trang_thai_chuyen.sql` chuẩn hoá lại (không sửa
  V5 vì Flyway đã ghi checksum). `StatusBadge` cũng bổ sung màu cho vòng đời chuyến (Nháp, Cần điều
  chỉnh, Đã phân công, Đã phê duyệt, Hoàn thành) và cho Lưu tạm/Đã xuất/Đang xử lý.
- **N72 — Cảnh báo chỉ hiện sau khi bấm gửi.** N66/N67 gắn dòng cảnh báo trực tiếp vào biểu thức kiểm
  tra nên vừa mở "Kế hoạch & phân luồng tuyến" hoặc "Báo cáo sự cố" là đã thấy ngay chữ đỏ dù người
  dùng chưa nhập gì. Nay có cờ `daGui`: cảnh báo chỉ hiện khi đã bấm nút gửi mà dữ liệu còn thiếu, và
  tự tắt ngay khi điền đủ. Nút gửi cũng thôi bị khoá theo lỗi (chỉ khoá khi `busy`) để bấm vào là biết
  còn thiếu gì, thay vì một nút xám không giải thích.

## Nhóm J — Đưa HT5 về đúng biểu đồ usecase 3.5.3 (17/09/2026)

- **N73 — Danh sách chức năng HT5 lệch hẳn biểu đồ usecase.** Biểu đồ 3.5.3 có 11 usecase chia cho ba
  actor, bản cài đặt cũ chỉ có 7 và gán vai trò không khớp. Nay `ChucNang` HT5 đúng 11 mục:
  *Kế toán viên* — Tra cứu dữ liệu COD, Đối soát COD, Lập phiếu thu, Lập phiếu chi, Quản lý sai lệch
  COD, Xem báo cáo thu – chi COD; *Thủ quỹ* — Xác nhận thu tiền, Xác nhận chi trả COD, Cập nhật trạng
  thái giao dịch, Tra cứu lịch sử giao dịch; *Kế toán trưởng* — Phê duyệt giao dịch, Xem báo cáo
  thu – chi COD, Tra cứu lịch sử giao dịch. Các sai lệch cụ thể đã sửa:
  (a) "Lập phiếu thu/chi" gộp làm một và cấp nhầm cho cả Thủ quỹ — nay tách hai usecase, chỉ Kế toán
  viên lập (3.5.2 chỉ giao Thủ quỹ việc *xác nhận*);
  (b) "Đối soát COD" cấp thừa cho Kế toán trưởng — biểu đồ chỉ nối Kế toán viên;
  (c) "Tra cứu dữ liệu COD" cấp cho cả ba vai trò — biểu đồ chỉ nối Kế toán viên, hai vai trò còn lại
  có usecase riêng là "Tra cứu lịch sử giao dịch";
  (d) "Cập nhật sổ quỹ và công nợ" không phải usecase của actor nào mà là bước 5 của quy trình 3.5.1
  do hệ thống làm — gỡ khỏi menu, hai bảng sổ quỹ và công nợ chuyển vào màn báo cáo;
  (e) "Báo cáo và đối soát" đổi đúng tên biểu đồ là "Xem báo cáo thu – chi COD".
- **N74 — Bổ sung ba usecase bị thiếu hẳn.** "Quản lý sai lệch COD" (`GET/PUT /ht5/sai-lech`): liệt kê
  giao dịch thực thu khác phải thu, kế toán viên ghi nguyên nhân và hướng xử lý — đúng 3.5.1 bước 2
  "các giao dịch sai lệch được đánh dấu để kiểm tra". "Tra cứu lịch sử giao dịch" (`GET /ht5/lich-su`):
  gộp nhật ký giao dịch COD, phiếu thu/chi và các đợt đối soát kèm người thực hiện, phục vụ 3.5.2
  ("lịch sử điều chỉnh"). Thêm cột `GiaoDichCOD.lyDoSaiLech/nguoiXuLySaiLech/thoiGianXuLySaiLech`
  trong `V10__ht5_theo_usecase.sql` [G25].
- **N75 — Tách bước "Thủ quỹ xác nhận thực tế" ra khỏi bước phê duyệt.** 3.5.1 bước 4 nói phiếu sau khi
  duyệt "chuyển cho thủ quỹ xác nhận thực tế", bước 5 mới ghi sổ quỹ và công nợ. Bản cũ cho `duyet()`
  của kế toán trưởng ghi sổ luôn nên vai trò Thủ quỹ không có việc gì trong luồng phiếu — đó là lý do
  hai usecase "Xác nhận thu tiền" và "Xác nhận chi trả COD" không tồn tại. Nay vòng đời phiếu là
  `Chờ duyệt → Đã duyệt → Đã thực hiện`: `duyet()` chỉ chốt phương án, `xacNhan()` của thủ quỹ mới ghi
  `SoQuy` và cập nhật `CongNo`. `V10` thêm `PhieuThuChi.nguoiXacNhan/thoiGianXacNhan` và chuyển phiếu
  "Đã duyệt" cũ sang "Đã thực hiện" (sổ quỹ của chúng đã ghi ở luồng cũ). Báo cáo tính "đã chi trả"
  theo phiếu "Đã thực hiện" thay vì "Đã duyệt".
- **N76 — Bỏ nút "Tạo thủ công" giao dịch COD.** Nút này không có trong biểu đồ usecase, lại gắn nhầm
  quyền `HT5_CAP_NHAT_TRANG_THAI_GD` (Thủ quỹ) trong khi nằm trên màn của Kế toán viên — bấm vào là
  403. Thay bằng `dongBoTuDonDaGiao()` chạy ngầm khi mở màn tra cứu: đúng 3.5.1 bước 1 "hệ thống tiếp
  nhận dữ liệu đơn hàng đã giao và số tiền COD cần thu", đồng thời vá được các đơn đã giao trước khi
  có event listener COD.

## Nhóm K — Bổ sung tra cứu PO và phê duyệt phiếu xuất cho HT2 (20/09/2026)

- **N77 — Tra cứu đơn hàng/PO trên cả hai màn lập phiếu.** Đặc tả 3.2.3 (luồng chính bước 7–8) yêu cầu
  nhân viên kho "nhập hoặc quét mã đơn hàng (mã PO) để tra cứu" và hệ thống "hiển thị thông tin chi
  tiết đơn hàng". Trước đây màn lập phiếu nhập chỉ có một `select` liệt kê mã đơn trơ trọi, màn lập
  phiếu xuất thì để người dùng gõ tay mã đơn — không tra cứu được gì. Nay cả hai màn có khối
  **Tra cứu đơn hàng / PO**: tìm theo mã PO, tên khách hàng hoặc mặt hàng; bảng kết quả hiện khách
  hàng, người nhận kèm SĐT, địa chỉ giao, mặt hàng, khối lượng, trạng thái và nút "Chọn" để điền
  thẳng vào biểu mẫu. Backend thêm `DonKhoView`, `GET /ht2/don-cho-nhap?tuKhoa=` (đơn đã duyệt chờ
  nhập) và `GET /ht2/don-cho-xuat?tuKhoa=` (đơn đã nhập kho). Ô "Đơn hàng" của phiếu xuất đổi từ
  ô nhập tay sang `select` nên không còn gõ nhầm mã đơn.
- **N78 — [G26] Quản lý kho phê duyệt phiếu xuất.** 3.2.2 giao cho Người quản lý kho usecase
  "Quản lý phiếu nhập/ xuất (kiểm tra & phê duyệt)" và biểu đồ 3.2.3 có "Quản lí phiếu nhập_xuất",
  nhưng bản cũ chỉ duyệt phiếu nhập; phiếu xuất do nhân viên kho tự bấm "Xác nhận xuất" và trừ tồn
  luôn, không ai kiểm soát. Nay phiếu xuất đi đúng vòng đời đối xứng với phiếu nhập:
  `Lưu tạm → Chờ duyệt → Đã xuất | Từ chối`. Nhân viên kho `POST /phieu-xuat/{ma}/gui-duyet`; quản lý
  kho `POST /phieu-xuat/{ma}/duyet` (kiểm tồn rồi trừ tồn trong cùng transaction — giữ nguyên bảo đảm
  của N48) hoặc `POST /phieu-xuat/{ma}/tu-choi` kèm lý do. Phiếu bị từ chối sửa lại được rồi gửi duyệt
  lần nữa, giống phiếu nhập. Bỏ `xacNhanXuat`. `V11__phe_duyet_phieu_xuat.sql` thêm cột
  `PHIEU_XUAT.nguoiDuyet` và gán người lập làm người duyệt cho các phiếu "Đã xuất" của luồng cũ.
- **N79 — Trả lại quan hệ kế thừa Quản lý kho → Nhân viên kho.** N46 từng gỡ kế thừa theo yêu cầu
  miệng của nhóm, khiến `QL_KHO` chỉ còn 3 chức năng và mất hết màn tác nghiệp. Biểu đồ usecase 3.2.3
  vẽ rõ generalization (tam giác rỗng ở phía *Nhân viên kho*, mũi tên từ *Người quản lí kho*), nghĩa là
  quản lý kho có đủ chức năng của nhân viên kho cộng thêm phần của mình. Nay `QL_KHO` được cấp lại sáu
  chức năng tác nghiệp; riêng "Quản lí phiếu nhập – xuất" và "Báo cáo nhập – xuất kho" vẫn chỉ quản lý
  kho có. Đồng thời sửa một lệch nữa: "Theo dõi kho" trong biểu đồ nối với *Nhân viên kho* nhưng code
  để `HT2_DASHBOARD_NHAP_XUAT` là `QL_KHO` — nay cấp cho cả hai. Kết quả: nvkho 7 chức năng, qlkho 9.
  Danh sách phiếu vẫn phân biệt phạm vi (`danhSachNhap`/`danhSachXuat` lọc theo `maNV` khi người dùng
  không có quyền phê duyệt) nên tiêu đề thẻ đổi thành "Phiếu nhập/xuất của kho" khi là quản lý kho.

