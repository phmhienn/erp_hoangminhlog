# THIẾT KẾ BACKEND — ERP HOÀNG MINH (chương trình thử nghiệm)

Tài liệu này là **bản thiết kế** phần backend (Java 17 + Spring Boot 3.5 + MySQL 8), viết trước khi lập
trình chi tiết từng phase. Mọi quyết định ở đây đều truy vết được về `ERP_nhóm 2 (1).docx` qua
`ERP_build_prompt.md` (PHỤ LỤC A–D) và `docs/NOTES.md`. Phần đã lập trình xong ở Phase 0 được đánh dấu ✅.

---

## 1. Ràng buộc thiết kế (từ tài liệu)

| Nguồn | Ràng buộc | Hệ quả thiết kế |
|---|---|---|
| 1.3 | Web, demo dữ liệu giả lập; không app mobile, không API thanh toán/định vị, không tối ưu tuyến/AI | Không tích hợp bên thứ ba; upload ảnh chỉ lưu file cục bộ |
| 2.6 | Java + Spring Boot, React, MySQL, client–server, **một CSDL dùng chung**, Git, lặp theo hệ thống | Modular monolith; một schema `erp_hoangminh`; nhánh Git theo phase |
| 4.2 | 5 bảng dùng chung, ENUM trạng thái | Entity dùng chung đặt ở module sở hữu chính, module khác gọi qua service công khai |
| 4.3 | Ma trận phân quyền theo (bảng × hệ thống) | `QuyenService` là nguồn chân lý duy nhất; API chặn cả khi UI ẩn |
| 3.x.3 | Luồng + câu chữ thông báo nguyên văn | Mã thông báo tập trung `messages_vi.properties`; service ném lỗi theo mã |

Kiến trúc chọn: **modular monolith** — một ứng dụng Spring Boot, chia package theo 5 hệ thống; không
microservice (5 hệ thống chung một CSDL theo 2.6, phạm vi là chương trình thử nghiệm theo 1.3).

```
React SPA (trình duyệt, cổng 5173)
   │  REST/JSON + Authorization: Bearer <JWT>
   ▼
Spring Boot (cổng 8080)
   ├─ controller : @RestController, DTO record, Bean Validation, kiểm quyền
   ├─ service    : luật nghiệp vụ, @Transactional, state machine đơn hàng, liên thông hệ thống
   ├─ repository : Spring Data JPA → MySQL erp_hoangminh
   └─ common     : security, permission, exception, sequence, audit, upload, messages
File ảnh (minh chứng, sự cố) → thư mục ./uploads, CSDL chỉ lưu đường dẫn (G8, G16)
```

Quy tắc tầng bắt buộc (mục 2 huong_di_lap_trinh):
- Controller **không** chứa luật nghiệp vụ; chỉ nhận DTO, gọi service, trả DTO.
- Service **không** trả entity ra ngoài; mọi payload là `record` trong gói `dto`.
- Repository chỉ truy vấn; không gọi service khác.
- Module này **không** dùng repository của module khác — chỉ gọi service công khai của module đó.
- Mọi thao tác đổi tồn kho / số dư / trạng thái nằm trong **một** `@Transactional`.

## 2. Cấu trúc package ✅ (khung đã tạo ở Phase 0)

```
backend/src/main/java/vn/hoangminh/erp/
├─ ErpHoangMinhApplication.java
├─ common/
│  ├─ config/       AppProperties, SecurityConfig ✅
│  ├─ security/     JwtService, JwtAuthFilter, AuthService, AuthController, TaiKhoanRepository ✅
│  ├─ permission/   VaiTro, HanhDong, NhomBang, ChucNang, QuyenService, QuyenController ✅
│  ├─ exception/    LoiNghiepVuException, ApiError, GlobalExceptionHandler ✅
│  ├─ sequence/     LoaiChungTu, MaChungTuService ✅
│  ├─ audit/        LichSuDonHangService            (Phase 1)
│  ├─ donhang/      DonHangStateService             (Phase 1)
│  └─ upload/       UploadService, UploadConfig     (Phase 3)
├─ Ht1DonHang/      {controller, service, repository, domain, dto}   (Phase 1)
├─ Ht2Kho/          {…}                                              (Phase 2)
├─ Ht3DieuPhoi/     {…}                                              (Phase 3)
├─ Ht4NhanSu/       {…}   domain: NhanVien, PhongBan, ChucVu, TrangThaiNhanVien, TaiXe ✅
│                         service: NhanVienQueryService (API công khai cho module khác) ✅
└─ Ht5ThuChiCod/    {…}                                              (Phase 5)
```

## 3. Ánh xạ CSDL → Entity (33 bảng)

Tên bảng/tên cột giữ **nguyên văn** lược đồ (`@Table(name="PHIEU_NHAP")`, `@Column(name="maPhieuNhap")`),
kể cả cách viết hoa/thường. `ddl-auto: validate` để Hibernate tự đối chiếu với schema mỗi lần khởi động.

| Nhóm quyền (NhomBang) | Bảng | Module sở hữu entity |
|---|---|---|
| KHACH_HANG_DICH_VU | KhachHang, DichVu | Ht1DonHang |
| DON_HANG_HANG_HOA | DonHang, HangHoa, LichSuDonHang | Ht1DonHang |
| KHO | SanPham, PHIEU_NHAP, CHI_TIET_PHIEU_NHAP, PHIEU_XUAT, CHI_TIET_PHIEU_XUAT, PHIEU_KIEM_KE, CHI_TIET_KIEM_KE, TON_KHO, BienBanSuCo, ChiPhiLuuKho | Ht2Kho |
| DIEU_PHOI | PhuongTien, ChuyenVan, ChiTietChuyenHang, LoTrinh, SuCoVanTai, MinhChungGiaoHang | Ht3DieuPhoi |
| NHAN_SU | NhanVien, PhongBan, ChucVu, TrangThaiNhanVien | Ht4NhanSu |
| TAI_XE | TaiXe | Ht4NhanSu |
| HT5_THU_CHI | GiaoDichCOD, PhieuThuChi, SoQuy, CongNo, DoiSoatCOD, ChiTietDoiSoat | Ht5ThuChiCod |
| TAI_KHOAN | TaiKhoan | common/security |

Quy ước entity: Lombok `@Getter/@Setter`, khoá `@Id @Column`, quan hệ `@ManyToOne(fetch = LAZY)` chỉ
trong cùng module; tiền dùng `BigDecimal`; ngày giờ dùng `LocalDateTime`/`LocalDate`.

## 4. Bảo mật & phân quyền ✅

- Đăng nhập: `POST /api/auth/dang-nhap` đọc bảng `TaiKhoan`; sai tên/mật khẩu → 401 `auth.dangnhap.sai`;
  `trangThai != "Hoạt động"` → 403 `auth.dangnhap.taikhoan.khonghoatdong` (nhiệm vụ 0.4). ✅
- Phiên: JWT HS256 (jjwt 0.12), claims `maTaiKhoan, maNV, tenDangNhap, hoTen, vaiTro`; hết hạn 480 phút.
  Mỗi request, `JwtAuthFilter` đọc lại `TaiKhoan` để tài khoản vừa bị khóa mất hiệu lực ngay. ✅
- Lỗi xác thực/phân quyền trả JSON thống nhất `{ maLoi, thongBao, chiTiet }` qua entry point và
  access denied handler của `SecurityConfig`. ✅
- Ma trận 4.3 mã hoá trong `QuyenService` (8 nhóm bảng × 5 hệ thống × 8 hành động), đúng từng ô của
  PHỤ LỤC B; hai nhóm bổ sung theo NOTES N9. Kiểm tra trong controller/service bằng
  `quyen.yeuCauQuyen(vaiTro, NhomBang.X, HanhDong.THEM)` → ném 403 `quyen.tuchoi.bang` nếu ô là "–". ✅
- Quyền chức năng (3.x.2) mã hoá trong enum `ChucNang` (40 chức năng, mỗi chức năng liệt kê vai trò được
  phép); kiểm bằng `quyen.yeuCauChucNang(vaiTro, ChucNang.X)` → 403 `quyen.tuchoi.chucnang`. ✅
  Phân tách NV/QL trong từng hệ thống sẽ đối chiếu lại với 3.x.2 ở phase tương ứng (ghi NOTES).
- Endpoint công khai duy nhất: `/api/auth/dang-nhap`. Mọi endpoint khác yêu cầu token. ✅

## 5. State machine đơn hàng (PHỤ LỤC C) — `common/donhang/DonHangStateService` (Phase 1)

Chỉ hệ thống sở hữu giai đoạn được ghi giá trị tương ứng; mọi lần đổi đều ghi `LichSuDonHang`.

| Trạng thái ENUM | Hệ thống được ghi | Điểm kích hoạt |
|---|---|---|
| Đã tạo | HT1 | lưu đơn mới |
| Đã hủy | HT1 | hủy mềm (ghi `lyDoHuy`, người, thời gian) |
| Đã nhập kho | HT2 | duyệt phiếu nhập |
| Đang giao | HT3 | NV giao hàng xác nhận nhận đơn |
| Đã giao | HT3 | xác nhận giao thành công (có minh chứng) |
| Đã đối soát | HT5 | hoàn tất đợt đối soát chứa giao dịch của đơn |

API nội bộ: `doiTrangThai(heSoHuu, maDonHang, trangThaiMoi, nguoiThucHien, ghiChu)` — kiểm tra
(a) hệ gọi đúng chủ sở hữu của `trangThaiMoi`, (b) chuyển tiếp hợp lệ theo bảng trên (không nhảy cóc,
không đi ngược), (c) ghi `LichSuDonHang` cùng transaction. Module khác **không** được `UPDATE trangThai`.

Nhãn hiển thị 3.3.3 ("Chờ tiếp nhận", "Đang giao hàng", "Đã hoàn thành") chỉ là ánh xạ hiển thị của
`Đã nhập kho / Đang giao / Đã giao` — xử lý ở frontend (`StatusBadge`), không sinh giá trị mới. ✅

## 6. Liên thông dữ liệu một chiều (mục 5 huong_di) — quy tắc transaction

| Sự kiện | Ghi thêm trong cùng transaction | Quyền sở hữu |
|---|---|---|
| Duyệt phiếu nhập (HT2) | `TON_KHO.soLuongTon +=` (khóa dòng `FOR UPDATE`), `LichSuDonHang`, trạng thái `Đã nhập kho` | HT2 |
| Lập phiếu xuất (HT2) | `TON_KHO.soLuongTon -=`, trạng thái đơn sang giai đoạn giao vận, `ChiPhiLuuKho` | HT2 gọi `Ht5ThuChiCod.ghiChiPhiLuuKho(...)` (quyền Ghi của HT5, ma trận 4.3) |
| Đơn → `Đã giao` (HT3) | sinh `GiaoDichCOD(soTienPhaiThu = DonHang.tienCOD)` | HT3 gọi `Ht5ThuChiCod.taoGiaoDichCodTuDon(...)` |
| Xác nhận phiếu thu/chi (HT5) | ghi `SoQuy` (1 dòng/phiếu, G14), cập nhật `CongNo.soTienDaTra` (`soDuNo` là cột GENERATED, không ghi tay) | HT5 |
| Hàng không đạt khi nhập (HT2) | `BienBanSuCo`, **kết thúc** luồng lô lỗi, không tăng tồn | HT2 |

Mỗi dòng trên là **một** `@Transactional` ở service khởi phát; service đích được gọi trong cùng
transaction (REQUIRED). Không hệ nào nhập lại dữ liệu hệ trước đã có (6.1).

## 7. Sinh mã chứng từ ✅ (`common/sequence`)

`MaChungTuService.capMa(LoaiChungTu)` = tiền tố + 7 số, tính từ `MAX(cột mã) LIKE 'tiền tố%'`.
Bảng tiền tố: DH, PN, PX, KK, BB, KH, NV, TA, TX, GD, PT, PC, SQ, CN, DS, CP, MC, TK (xem `LoaiChungTu`).
Mã cấp **tự động khi lưu** (đặc tả 3.1.3 bước 2), người dùng không nhập. `ChuyenVan` dùng
`INT AUTO_INCREMENT`, mã `CV…` chỉ để trình bày (NOTES N11).

## 8. Thiết kế API theo phase

Quy ước: tiền tố `/api`; lỗi trả `{ maLoi, thongBao, chiTiet }` với 400/403/409/401 (NOTES N7);
mọi endpoint gắn kiểm quyền (nhóm bảng hoặc chức năng); DTO là record; phân trang `?trang=0&kichThuoc=20`
chỉ dùng cho danh sách lớn, mặc định trả đủ vì dữ liệu demo nhỏ.

### Phase 0 — nền tảng ✅
| Method + path | Quyền | Nội dung |
|---|---|---|
| `POST /api/auth/dang-nhap` | công khai | đăng nhập, trả JWT + quyền |
| `GET /api/quyen/cua-toi` | đã đăng nhập | vai trò, hệ thống, quyền bảng, chức năng |
| `GET /api/quyen/ma-tran` | đã đăng nhập | ma trận 4.3 đầy đủ (tham chiếu nghiệm thu) |
| `GET /api/quyen/vai-tro` | đã đăng nhập | 13 vai trò + hệ thống |

### Phase 1 — HT1 (mục 3.1) ✅ triển khai và kiểm thử 14/09/2026
| Method + path | Quyền | Nghiệp vụ / thông báo |
|---|---|---|
| `GET /api/ht1/khach-hang?tuKhoa=` | HT1_QUAN_LY_KHACH_HANG | tra cứu theo mã KH, tên, liên hệ (3.1.2) |
| `POST /api/ht1/khach-hang` | HT1_QUAN_LY_KHACH_HANG | tạo hồ sơ KH mới, cấp mã KH |
| `PUT /api/ht1/khach-hang/{ma}` | HT1_QUAN_LY_KHACH_HANG | cập nhật khi có thay đổi |
| `GET /api/ht1/don-hang?ma=&khachHang=&tuNgay=&denNgay=` | HT1_TAO_DON_HANG | tra cứu đơn + lịch sử xử lý (3.1.3 tra cứu) |
| `POST /api/ht1/don-hang` | HT1_TAO_DON_HANG | luồng 3.1.3 bước 1–2: kiểm tra, lưu, cấp mã DH, trạng thái `Đã tạo`, ghi lịch sử |
| `PUT /api/ht1/don-hang/{ma}` | HT1_CAP_NHAT_DON_HANG | chỉ khi đơn chưa xử lý bước kế; 409 nếu đã gửi duyệt |
| `POST /api/ht1/don-hang/{ma}/gui-duyet` | HT1_TAO_DON_HANG | bước 3; 400 `…chinhsuatrước…` nếu dữ liệu thiếu |
| `POST /api/ht1/don-hang/{ma}/duyet` | HT1_KIEM_DUYET_DON_HANG | bước 4–6: duyệt → thông báo kết quả, chuyển bước tiếp |
| `POST /api/ht1/don-hang/{ma}/tu-choi` | HT1_KIEM_DUYET_DON_HANG | luồng phụ: trả về NVKD bổ sung/chỉnh sửa, kèm lý do |
| `POST /api/ht1/don-hang/{ma}/huy` | HT1_HUY_DON_HANG | hủy mềm: kiểm điều kiện hủy, ghi `lyDoHuy` + người + thời gian (3.1.1) |
| `GET /api/ht1/don-hang/{ma}/lich-su` | HT1_THEO_DOI_DON_HANG | `LichSuDonHang` theo thời gian |
| `GET /api/ht1/bao-cao?tuNgay=&denNgay=&maKhachHang=&maDichVu=` | HT1_XEM_BAO_CAO_DON_HANG | chỉ đọc/tổng hợp; không có dữ liệu → thông báo; tiêu chí sai → 400 yêu cầu nhập lại |

Điều kiện hủy (3.1.1 "kiểm tra điều kiện hủy theo quy định doanh nghiệp"): tài liệu không liệt kê cụ thể →
chốt: chỉ hủy được khi trạng thái còn `Đã tạo` (chưa có phiếu nhập); ghi NOTES ở Phase 1.

### Phase 2 — HT2 (mục 3.2) ✅ backend triển khai, UI đang hoàn thiện
`GET /api/ht2/don-cho-nhap` (2.1 tra cứu đơn/PO đã duyệt); `POST /api/ht2/phieu-nhap` (2.2 lưu tạm +
gửi duyệt); `POST /api/ht2/phieu-nhap/{ma}/duyet|tu-choi` (2.3, duyệt → tăng tồn real-time);
`POST /api/ht2/bien-ban-su-co` (luồng phụ bước 4, kết thúc lô lỗi); `PUT /api/ht2/ton-kho/{ma}/vi-tri`
(2.4 put-away); `POST /api/ht2/phieu-xuat` (2.5 giảm tồn + sinh ChiPhiLuuKho qua HT5);
`POST /api/ht2/phieu-kiem-ke` + `PUT …/chi-tiet` (2.6); `GET /api/ht2/dashboard` (2.7);
`GET /api/ht2/bao-cao-nhap?tuNgay=&denNgay=&maKho=&maNV=&xuat=excel|pdf` (2.8).

### Phase 3 — HT3 (mục 3.3)
`POST /api/ht3/chuyen` (3.1 lập kế hoạch: tuyến, phương tiện, tài xế, danh sách đơn, thời gian; kiểm trùng
lịch tài xế/xe); `POST /api/ht3/phan-cong` (3.2 cảnh báo trùng lịch, thiếu GPLX theo `TaiXe.soGPLX/loaiGPLX`,
quá giờ); `POST /api/ht3/chuyen/{ma}/phe-duyet|yeu-cau-dieu-chinh` (3.6);
`GET /api/ht3/nhiem-vu-cua-toi` (3.3); `POST /api/ht3/don-hang/{ma}/nhan` (3.3: `Đã nhập kho`→`Đang giao`;
409 + thông báo "Đơn hàng đã được nhận bởi nhân viên khác" nếu đã có người nhận);
`POST /api/ht3/don-hang/{ma}/moc-hanh-trinh` (3.4); `POST /api/ht3/su-co` (3.4 kèm ảnh → `SuCoVanTai.HinhAnh`);
`POST /api/ht3/don-hang/{ma}/hoan-tat` (3.5: bắt buộc `MinhChungGiaoHang`, thiếu → 400 yêu cầu tải bổ sung;
đơn đã hoàn thành → thông báo đã xác nhận trước đó); `GET /api/ht3/lich-su…` (3.7).

### Phase 4 — HT4 (mục 3.4)
`GET/POST/PUT /api/ht4/ho-so…` với thông báo nguyên văn 3.4.3 ("Thêm hồ sơ nhân viên thành công",
"Hồ sơ nhân viên đã tồn tại"…); `PUT /api/ht4/ho-so/{ma}/trang-thai`; `GET /api/ht4/ho-so-cua-toi`;
`POST /api/ht4/yeu-cau-cap-nhat`; chặn đọc hồ sơ người khác với `NHAN_VIEN` theo `TaiKhoan.maNV`.

### Phase 5 — HT5 (mục 3.5)
`GET /api/ht5/cod?…` (5.1); `POST /api/ht5/doi-soat` (5.2, đánh dấu giao dịch sai lệch);
`POST /api/ht5/phieu-thu-chi` + `POST …/gui` (5.3); `POST /api/ht5/giao-dich/{ma}/xac-nhan` (5.4 thủ quỹ,
đúng 2 cột Actor–Hệ thống và 3 ngoại lệ 3.5.3); `POST /api/ht5/giao-dich/{ma}/phe-duyet` (5.3 kế toán trưởng);
`GET /api/ht5/so-quy`, `GET /api/ht5/cong-no` (5.5); `GET /api/ht5/bao-cao` (5.6).

### Phase 6 — tích hợp
Không thêm endpoint mới; thêm test luồng trọn vòng đời và kiểm tra `DonHangStateService` chặn ghi sai
chủ sở hữu; rà soát toàn bộ menu/API theo ma trận (6.3).

## 9. Kiểm tra hợp lệ hai lớp

1. **Bean Validation** trên DTO: `@NotBlank`, `@Size` theo PHỤ LỤC A (sđt ≤ 15, địa chỉ ≤ 255, lý do hủy ≤ 300…),
   `@DecimalMin("0")` cho tiền/khối lượng; thông báo lấy mã từ `messages_vi.properties`.
2. **Luật nghiệp vụ trong service**: điều kiện duyệt/từ chối, điều kiện hủy mềm, tồn kho đủ xuất,
   trùng lịch tài xế/xe, thiếu GPLX, bắt buộc ảnh minh chứng, 3 ngoại lệ 3.5.3; ném
   `LoiNghiepVuException` với mã thông báo nguyên văn của bảng 3.x.3.

## 10. Đồng thời & toàn vẹn

- Đọc–ghi tồn kho và số dư quỹ: `SELECT … FOR UPDATE` trong transaction (không thêm cột version vào
  lược đồ tài liệu); khoá dòng `TON_KHO` theo `(maSP, maViTri)` nhờ `UNIQUE` (G5).
- `CongNo.soDuNo` là cột GENERATED — ứng dụng không ghi (G11).
- Sinh mã chứng từ: trùng đồng thời bị PK chặn → service bắt `DataIntegrityViolationException`,
  sinh lại một lần (409 chỉ dành cho xung đột trạng thái nghiệp vụ).
- Hủy mềm: không `DELETE` vật lý với DonHang (3.1.1 + nhận xét mục 25 loierp).

## 11. Upload file (Phase 3)

`UploadService` lưu vào `./uploads/{yyyy}/{MM}/{uuid}.{ext}` (chấp nhận png/jpg/jpeg/pdf, ≤ 10MB),
trả đường dẫn tương đối; CSDL chỉ lưu đường dẫn (`SuCoVanTai.HinhAnh`, `MinhChungGiaoHang.duongDan`,
`BienBanSuCo.duongDanAnh`). Phục vụ tĩnh qua `/uploads/**` có kiểm quyền đọc theo nhóm bảng tương ứng.

## 12. Thư viện bổ sung (chỉ thêm khi phase cần)

| Phase | Thư viện | Lý do |
|---|---|---|
| 2 | Apache POI, OpenPDF | 2.8 yêu cầu xuất Excel và in PDF; OpenPDF thay iText (giấy phép LGPL) |
| 2 | (frontend) Recharts | biểu đồ dashboard/báo cáo |
| — | không thêm gì khác | JWT, validation, Flyway, JPA đã đủ |

## 13. Kiểm thử

- Unit (không CSDL): luật nghiệp vụ thuần — ma trận quyền ✅, JWT ✅, sinh mã, điều kiện duyệt/hủy,
  state machine, 3 ngoại lệ 3.5.3.
- Integration (`@Tag("integration")`, chạy bằng `mvn test -Pit` với biến môi trường `DB_PASSWORD`):
  schema 33 bảng, seed, đăng nhập từng vai trò, luồng liên hệ thống, **test phân quyền cho từng ô "–"**
  (gọi API bằng token vai trò đó, khẳng định 403).
- Báo cáo kiểm thử ghi vào `docs/BAO_CAO_KIEM_THU.md` theo mẫu tài liệu (chức năng | dữ liệu thử | ĐẠT/KHÔNG).

## 14. Vận hành

```bash
# biến môi trường
DB_PASSWORD=***        # bắt buộc
ERP_JWT_SECRET=…       # tuỳ chọn, có giá trị dev
ERP_UPLOAD_DIR=./uploads

mvn -f backend/pom.xml spring-boot:run     # cổng 8080, Flyway tự chạy V1+V2
npm --prefix frontend run dev              # cổng 5173, proxy /api → 8080
mvn -f backend/pom.xml test -Pit           # test tích hợp cần MySQL
```

## 15. Thứ tự dựng backend còn lại (đối chiếu lộ trình 2.7)

1. **Phase 1 (HT1) ✅** — schema+seed MySQL, DonHangStateService, LichSuDonHangService,
   toàn bộ endpoint mục 8 Phase 1, test unit+integration, frontend HT1; xem NOTES N19–N25.
2. **Phase 2 (HT2)** — entity kho, tồn kho FOR UPDATE, put-away, kiểm kê, dashboard, báo cáo + POI/OpenPDF.
3. **Phase 3 (HT3)** — chuyến/lộ trình/sự cố, upload minh chứng, cảnh báo trùng lịch/GPLX.
4. **Phase 4 (HT4)** — hồ sơ nhân viên, chặn theo `maNV`, thông báo nguyên văn 3.4.3.
5. **Phase 5 (HT5)** — COD, đối soát, phiếu thu/chi, sổ quỹ/công nợ, 3 ngoại lệ 3.5.3.
6. **Phase 6** — test vòng đời trọn vẹn, rà soát ma trận toàn hệ thống.
7. **Phase 7** — seed kịch bản luồng phụ, báo cáo kiểm thử, hoàn thiện demo.

Mỗi phase: nhánh `phase/N-…`, checklist nghiệm thu tự đánh giá, cập nhật NOTES.md, rồi **dừng chờ lệnh**.
