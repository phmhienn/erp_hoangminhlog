# ERP Hoàng Minh — chương trình thử nghiệm

Hệ thống ERP cho Công ty CP Đầu tư Thương mại và Dịch vụ Hoàng Minh (logistics giao nhận), dựng theo
đúng đặc tả `ERP_nhóm 2 (1).docx`: ứng dụng web chạy demo trên dữ liệu giả lập (mục 1.3), backend
**Java + Spring Boot**, frontend **React**, một CSDL **MySQL** dùng chung duy nhất (mục 2.6), phát triển
lặp theo từng hệ thống cốt lõi HT1…HT5.

## Tài liệu trong repo

| File | Nội dung |
|---|---|
| [docs/THIET_KE_BACKEND.md](docs/THIET_KE_BACKEND.md) | Thiết kế backend: kiến trúc, phân quyền, state machine, API theo phase |
| [docs/NOTES.md](docs/NOTES.md) | Mọi quyết định khi tài liệu im lặng/mâu thuẫn (kèm trích dẫn mục) |
| [docs/HUONG_DAN_DEMO.md](docs/HUONG_DAN_DEMO.md) | Hướng dẫn thao tác demo theo từng phase đã nghiệm thu |
| [docs/HUONG_DAN_CHAY_CHUONG_TRINH.md](docs/HUONG_DAN_CHAY_CHUONG_TRINH.md) | Cài đặt môi trường, chạy backend/frontend và kiểm thử |
| [db/ERP_database.sql](db/ERP_database.sql) | Lược đồ 33 bảng (bản gốc để đối chiếu) |
| [backend/…/db/migration/V1__schema.sql](backend/src/main/resources/db/migration/V1__schema.sql) | Lược đồ chạy thật qua Flyway (V1), dẫn xuất nguyên văn từ bản gốc |
| [backend/…/db/migration/V2__seed.sql](backend/src/main/resources/db/migration/V2__seed.sql) | Dữ liệu nền lớp 1 (nhiệm vụ 0.5) |

## Chạy chương trình

Yêu cầu: JDK 17+, Maven 3.9+, Node 18+, MySQL 8 đang chạy.

```bash
set DB_PASSWORD=<mật khẩu MySQL root>
mvn -f backend/pom.xml spring-boot:run
```

```bash
npm --prefix frontend install
npm --prefix frontend run dev
```

Mở http://localhost:5173 — đăng nhập bằng một trong các tài khoản demo (mật khẩu chung `123456`):

| Tài khoản | Vai trò | Tài khoản | Vai trò |
|---|---|---|---|
| nvkd | Nhân viên kinh doanh | nvns | Nhân viên nhân sự |
| qlkd | Quản lý kinh doanh | qlns | Quản lý nhân sự |
| nvkho | Nhân viên kho | ktv | Kế toán viên |
| qlkho | Quản lý kho | thuquy | Thủ quỹ |
| nvdp | Nhân viên điều phối | ktt | Kế toán trưởng |
| qldp | Quản lý điều phối | nvthuong | Nhân viên thường |
| nvgh / nvgh2 | Nhân viên giao hàng | nvkho2 | **đã khóa** (kiểm tra 0.4) |

Lần chạy đầu tiên Flyway tự tạo schema `erp_hoangminh` (33 bảng) và nạp dữ liệu nền.

## Kiểm thử

```bash
mvn -f backend/pom.xml test          # unit (không cần CSDL)
mvn -f backend/pom.xml test -Pit     # tích hợp (cần MySQL + DB_PASSWORD)
npm --prefix frontend run typecheck  # TypeScript
```

## Tiến độ

| Phase | Phạm vi | Trạng thái |
|---|---|---|
| 0 | Nền tảng, CSDL, đăng nhập, dữ liệu nền, khung phân quyền | Đã kiểm thử MySQL |
| 1 | HT1 — Quản lý đơn hàng/dịch vụ | Đã triển khai và kiểm thử |
| 2 | HT2 — Quản lý nhập – xuất kho | Đã triển khai, chờ nghiệm thu |
| 3 | HT3 — Quản lý điều phối & giao hàng | Đã triển khai, chờ nghiệm thu |
| 4 | HT4 — Quản lý hồ sơ nhân viên | Đã triển khai, chờ nghiệm thu |
| 5 | HT5 — Quản lý thu chi & COD | Đã triển khai, chờ nghiệm thu |
| 6 | Tích hợp liên phân hệ, phân quyền toàn hệ | Đã kiểm thử |
| 7 | Dữ liệu giả lập luồng phụ, kiểm thử, demo | Đã hoàn thiện |
