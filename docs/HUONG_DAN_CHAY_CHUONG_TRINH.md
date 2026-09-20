# Hướng dẫn chạy chương trình ERP Hoàng Minh

Tài liệu này hướng dẫn chạy backend Spring Boot, frontend React và cơ sở dữ liệu MySQL trên Windows.

## 1. Yêu cầu môi trường

- JDK 17 trở lên.
- Maven 3.9 trở lên.
- Node.js 18 trở lên.
- MySQL 8 đang chạy ở cổng `3306`.
- Cơ sở dữ liệu có thể để trống; Flyway sẽ tự tạo schema `erp_hoangminh` và nạp dữ liệu.

Kiểm tra nhanh:

```powershell
java -version
mvn -version
node --version
npm --version
```

## 2. Cấu hình biến môi trường

Sao chép `.env.example` thành `.env` rồi thay các giá trị phù hợp. File `.env` chỉ dùng cho cấu hình
local và đã được Spring Boot tự đọc khi chạy từ thư mục gốc dự án hoặc thư mục `backend`.

Ví dụ nội dung `.env`:

```dotenv
DB_USER=root
DB_PASSWORD=mat-khau-root-mysql
DB_HOST=localhost
DB_PORT=3306
ERP_JWT_SECRET=chuoi-bi-mat-dai-it-nhat-32-ky-tu
ERP_UPLOAD_DIR=./uploads
```

Biến môi trường của hệ điều hành vẫn được ưu tiên hơn file `.env`. Nếu không muốn dùng file `.env`,
có thể nạp biến trực tiếp trong PowerShell:

```powershell
$env:DB_USER='root'
$env:DB_PASSWORD='mat-khau-root-mysql'
$env:DB_HOST='localhost'
$env:DB_PORT='3306'
$env:ERP_JWT_SECRET='chuoi-bi-mat-dai-it-nhat-32-ky-tu'
$env:ERP_UPLOAD_DIR='./uploads'
```

Nếu backend không chạy ở `localhost:8080`, sao chép `frontend/.env.example` thành `frontend/.env.local`,
sửa `VITE_BACKEND_URL`, rồi khởi động lại Vite.

## 3. Chạy backend

Mở PowerShell tại thư mục gốc dự án:

```powershell
$env:DB_PASSWORD='mat-khau-root-mysql'
mvn -f backend/pom.xml spring-boot:run
```

Backend chạy tại `http://localhost:8080`.

Khi chạy lần đầu, Flyway thực hiện các migration `V1` đến `V5`, tạo 33 bảng và dữ liệu demo cho các phase.
Không dừng cửa sổ PowerShell đang chạy backend.

Nếu MySQL dùng tài khoản khác `root`, có thể truyền thêm:

```powershell
$env:DB_USERNAME='ten-dang-nhap-mysql'
$env:DB_PASSWORD='mat-khau-mysql'
mvn -f backend/pom.xml spring-boot:run
```

## 4. Chạy frontend

Mở PowerShell thứ hai tại thư mục gốc dự án:

```powershell
npm.cmd --prefix frontend install
npm.cmd --prefix frontend run dev
```

Mở trình duyệt tại [http://localhost:5173](http://localhost:5173).

Frontend được cấu hình gọi backend tại `http://localhost:8080/api`. Nếu backend chạy cổng khác,
đặt biến môi trường `VITE_API_URL` trước khi chạy frontend:

```powershell
$env:VITE_API_URL='http://localhost:8081/api'
npm.cmd --prefix frontend run dev
```

## 5. Tài khoản demo

Mật khẩu demo mặc định là `123456`.

| Tài khoản | Vai trò |
|---|---|
| `nvkd` | Nhân viên kinh doanh |
| `qlkd` | Quản lý kinh doanh |
| `nvkho` | Nhân viên kho |
| `qlkho` | Quản lý kho |
| `nvdp` | Nhân viên điều phối |
| `qldp` | Quản lý điều phối |
| `nvgh`, `nvgh2` | Nhân viên giao hàng |
| `nvns` | Nhân viên nhân sự |
| `qlns` | Quản lý nhân sự |
| `ktv` | Kế toán viên |
| `thuquy` | Thủ quỹ |
| `ktt` | Kế toán trưởng |
| `nvthuong` | Nhân viên thường |

Tài khoản `nvkho2` được seed ở trạng thái đã khóa để kiểm tra trường hợp đăng nhập bị từ chối.

## 6. Kiểm thử

Unit test không cần kết nối MySQL:

```powershell
mvn -f backend/pom.xml test -q
```

Integration test cần MySQL và biến `DB_PASSWORD`:

```powershell
$env:DB_PASSWORD='mat-khau-root-mysql'
mvn -f backend/pom.xml test -Pit -q
```

Build frontend:

```powershell
npm.cmd --prefix frontend run build
```

## 7. Dữ liệu demo theo phase

- Phase 1: đơn hàng `DH0000001` đến `DH0000003`.
- Phase 2: phiếu nhập `PN0000001` đang chờ duyệt.
- Phase 7: chuyến điều phối `CV0000001`, giao dịch COD `GD0000001`, phiếu thu `PT0000001` và đợt đối soát `DS0000001`.

Chi tiết thao tác nghiệp vụ xem tại [HUONG_DAN_DEMO.md](HUONG_DAN_DEMO.md).

## 8. Xử lý lỗi thường gặp

### Không kết nối được MySQL

Kiểm tra MySQL đang chạy, cổng `3306` mở và `DB_PASSWORD` đúng. Nếu schema bị tạo dở trong môi trường
test, kiểm tra bảng `flyway_schema_history` rồi chạy lại backend sau khi xử lý migration lỗi.

### Frontend báo lỗi kết nối API

Đảm bảo backend đang chạy ở cổng `8080`. Xóa tiến trình frontend cũ, đặt lại `VITE_API_URL` nếu cần,
rồi chạy lại `npm.cmd --prefix frontend run dev`.

### Muốn dựng lại dữ liệu demo

Chỉ thực hiện trên môi trường phát triển. Xóa schema `erp_hoangminh` trong MySQL rồi khởi động lại
backend; Flyway sẽ tạo lại toàn bộ 33 bảng và dữ liệu từ đầu.
