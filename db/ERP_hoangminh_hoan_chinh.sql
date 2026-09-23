-- ERP Hoàng Minh: cấu trúc và dữ liệu demo đầy đủ, MySQL 8.
-- Nguồn: ERP_database.sql đối chiếu V1; toàn bộ V1–V12 được giữ nguyên bên dưới.
-- Chạy MỘT LẦN trên database MỚI. Không có DROP DATABASE/TABLE, không tắt khóa ngoại.
-- Database riêng để không ghi đè dữ liệu đang dùng. Có thể đổi tên tại hai dòng sau.
-- Tài khoản demo và mật khẩu giữ nguyên từ V2 (123456).
-- Các đường dẫn ảnh demo là dữ liệu tham chiếu; SQL không chứa file ảnh.
-- Không nhập file này lên database đã chạy migration.
-- Nếu chạy backend trên database này: đặt SPRING_FLYWAY_BASELINE_VERSION=12
-- và SPRING_FLYWAY_BASELINE_ON_MIGRATE=true khi chủ động bật Flyway.
-- Cấu hình hiện tại tắt Flyway; không tự động thay đổi database đã nhập.
-- Flyway sẽ tạo lịch sử baseline 12; không chạy lại V1–V12 trên cấu trúc đã nhập.
SET NAMES utf8mb4;
-- Workbench Safe Updates: chỉ thay đổi trong phiên import, không đổi cấu hình toàn server.
SET @erp_previous_safe_updates = @@SESSION.SQL_SAFE_UPDATES;
SET SESSION SQL_SAFE_UPDATES = 0;
CREATE DATABASE erp_hoangminh_full_demo CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE erp_hoangminh_full_demo;
-- ===== V1__schema.sql =====
-- Flyway baseline V1: nguyen van ERP_database.sql, chi bo 2 lenh CREATE DATABASE / USE
-- (database duoc tao boi JDBC createDatabaseIfNotExist hoac lenh o db/README.md).

-- =====================================================================
-- CSDL HỆ THỐNG ERP CÔNG TY HOÀNG MINH  (MySQL 8, InnoDB, utf8mb4)
-- Nguồn thẩm quyền: "ERP_nhóm 2 (1).docx"
--   - Bảng dùng chung: mục 4.2            - Bảng riêng: mục 3.1.4 / 3.2.4 / 3.3.4 / 3.4.4 / 3.5.4
--   - Danh sách lớp & bảng giữ nguyên: 4.1 - Phân quyền: 4.3
-- Quy tắc dựng: DDL Chương 3 làm khung cho bảng riêng; mục 4.2 cho bảng dùng chung;
-- thuộc tính Chương 4 (4.1) liệt kê thêm được bổ sung dạng NULL và đánh dấu [Gx].
-- =====================================================================
-- GHI CHÚ QUYẾT ĐỊNH (mọi chỗ tài liệu im lặng/mâu thuẫn, KHÔNG đổi hành vi mô tả):
-- G1  Bảng phụ 1 cột sau rút gọn (DON_HANG 2.10, DonHang 3.4, DonHang 5.4, NHAN_VIEN 2.9,
--     NguoiDung 3.1) không tạo bảng vật lý; thể hiện bằng FK trỏ bảng chính (đúng ý 4.1).
-- G2  NguoiDung (3.3.4/4.1) trùng đối tượng với TaiKhoan (4.2) -> dùng TaiKhoan; không tạo NguoiDung.
--     Các FK "người thực hiện" trỏ NhanVien (hồ sơ) hoặc TaiKhoan (tài khoản đăng nhập) tuỳ ngữ cảnh.
-- G3  maNCC của PHIEU_NHAP giữ nguyên cột nhưng KHÔNG tạo FK (tài liệu không định nghĩa bảng NCC).
-- G4  PHIEU_NHAP bổ sung maDonHang, nguoiDuyet theo danh sách lớp 4.1 (2.1) - bắt buộc cho luồng 3.2.3.
-- G5  TON_KHO bổ sung maViTri theo 4.1 (2.7), kiểu chuỗi tự do, KHÔNG tạo bảng vị trí;
--     UNIQUE(maSP, maViTri) để một ô kệ chỉ có một dòng tồn.
-- G6  SAN_PHAM bổ sung dieuKienBaoQuan theo 4.1 (2.8).
-- G7  CHI_TIET_PHIEU_NHAP bổ sung tinhTrangHang theo 4.1 (2.2) - phục vụ 3.2.2 ghi nhận thừa/thiếu/hư hỏng.
-- G8  SuCoVanTai bổ sung hinhAnh theo 4.1 (3.7) - phục vụ 3.3.2/3.3.4 báo cáo sự cố kèm ảnh.
-- G9  ChuyenVan bổ sung MaDonHang theo 4.1 (3.5) cho chuyến 1 đơn; chuyến nhiều đơn dùng
--     ChiTietChuyenHang (bảng nêu trong ma trận phân quyền 4.3).
-- G10 "Mã vận đơn" (2.4.1, 3.3.2) chính là mã định danh đơn hàng (3.1.1) -> tra cứu theo maDonHang,
--     không thêm cột riêng.
-- G11 CongNo: maDoiTac (3.5.4) không có bảng đích -> dùng maKhachHang FK (theo 4.1 mục 5.1);
--     soDuNo (4.1) là cột GENERATED = soTienPhaiTra - soTienDaTra, không nhập tay.
-- G12 GiaoDichCOD: giữ soTienPhaiThu/soTienThucThu (4.1 mục 5.2), bỏ soTien trùng lặp của 3.5.4.
-- G13 PhieuThuChi bổ sung maGiaoDich, nguoiLap, nguoiDuyet theo 4.1 (5.3).
-- G14 SoQuy chuyển thành sổ nhật ký quỹ theo 4.1 (5.6): mỗi dòng gắn 1 phiếu thu/chi (maPhieu);
--     số dư hiện tại lấy từ view vw_SoDuQuy, không lưu trùng.
-- G15 Đối soát theo đợt (2.4.5) cần nhiều giao dịch/đợt -> thêm ChiTietDoiSoat (khoá kép);
--     DoiSoatCOD giữ nguyên cột của 3.5.4 + nguoiDoiSoat, thoiGian theo 4.1 (5.7).
-- G16 Bốn bảng kỹ thuật hiện thực hành vi tài liệu bắt buộc nhưng không định nghĩa bảng:
--     ChiPhiLuuKho (quyền "Ghi ChiPhiLuuKho" của HT5, ma trận 4.3),
--     BienBanSuCo (luồng phụ bước 4 đặc tả 3.2.3 "ghi nhận biên bản sự cố vào CSDL"),
--     MinhChungGiaoHang (3.3.3 bắt buộc ảnh minh chứng/chữ ký),
--     LichSuDonHang (3.1.1 lưu người+thời gian hủy; 3.1.2 và 3.5.2 yêu cầu truy vết/lịch sử).
-- G17 Tiền tệ dùng DECIMAL(15,2) thay cho DOUBLE ở các bảng Chương 3 khai báo DOUBLE
--     (DOUBLE gây sai số nhị phân cho tiền); trọng lượng/khoảng cách giữ kiểu tài liệu khai báo.
-- G18 FK trỏ về bảng dùng chung lấy kích thước của 4.2 (maDonHang 10, maNV 20, maSP 20,
--     maKhachHang 20, maDichVu 20, maTaiXe 20, maTaiKhoan 10) kể cả khi bảng riêng Chương 3 khai khác.
-- G19 `INT(10)` của tài liệu -> `INT` (MySQL 8.0.19+ bỏ display width, hành vi giống nhau).
--     `TON_KHO.maViTri` NOT NULL DEFAULT '' để ràng buộc UNIQUE(maSP, maViTri) có hiệu lực
--     khi hàng chưa put-away (MySQL cho phép nhiều NULL trong unique index).
-- =====================================================================


-- ---------------------------------------------------------------------
-- HT4: DANH MỤC NHÂN SỰ (3.4.4)
-- ---------------------------------------------------------------------
CREATE TABLE PhongBan (
  maPhongBan  VARCHAR(20)  NOT NULL,
  tenPhongBan VARCHAR(100) NOT NULL,
  PRIMARY KEY (maPhongBan)
) ENGINE=InnoDB;

CREATE TABLE ChucVu (
  maChucVu  VARCHAR(20)  NOT NULL,
  tenChucVu VARCHAR(100) NOT NULL,
  PRIMARY KEY (maChucVu)
) ENGINE=InnoDB;

CREATE TABLE TrangThaiNhanVien (
  maTrangThai  VARCHAR(20) NOT NULL,
  tenTrangThai VARCHAR(50) NOT NULL,
  PRIMARY KEY (maTrangThai)
) ENGINE=InnoDB;

-- Bảng dùng chung (4.2) + ngayVaoLam theo 4.1 (4.1)
CREATE TABLE NhanVien (
  maNV         VARCHAR(20)  NOT NULL,
  hoTen        VARCHAR(100) NOT NULL,
  ngaySinh     DATE         NULL,
  gioiTinh     VARCHAR(10)  NULL,
  soDienThoai  VARCHAR(15)  NULL,
  diaChi       VARCHAR(255) NULL,
  email        VARCHAR(100) NULL,
  maPhongBan   VARCHAR(20)  NULL,
  maChucVu     VARCHAR(20)  NULL,
  maTrangThai  VARCHAR(20)  NULL,
  ngayVaoLam   DATE         NULL,               -- [G] 4.1 (4.1)
  PRIMARY KEY (maNV),
  CONSTRAINT fk_nv_phongban  FOREIGN KEY (maPhongBan)  REFERENCES PhongBan (maPhongBan),
  CONSTRAINT fk_nv_chucvu    FOREIGN KEY (maChucVu)    REFERENCES ChucVu (maChucVu),
  CONSTRAINT fk_nv_trangthai FOREIGN KEY (maTrangThai) REFERENCES TrangThaiNhanVien (maTrangThai)
) ENGINE=InnoDB;
CREATE INDEX idx_nv_hoten ON NhanVien (hoTen);

-- Bảng dùng chung (4.2): tài khoản đăng nhập toàn hệ thống
CREATE TABLE TaiKhoan (
  maTaiKhoan  VARCHAR(10)  NOT NULL,
  maNV        VARCHAR(20)  NOT NULL,
  tenDangNhap VARCHAR(50)  NOT NULL,
  matKhau     VARCHAR(255) NOT NULL,
  vaiTro      VARCHAR(30)  NOT NULL,
  trangThai   VARCHAR(20)  NOT NULL,
  PRIMARY KEY (maTaiKhoan),
  UNIQUE KEY uq_taikhoan_tendangnhap (tenDangNhap),
  CONSTRAINT fk_taikhoan_nv FOREIGN KEY (maNV) REFERENCES NhanVien (maNV)
) ENGINE=InnoDB;

-- Bảng dùng chung (4.2): mở rộng tài xế của NhanVien, quan hệ 1-1
CREATE TABLE TaiXe (
  maTaiXe   VARCHAR(20) NOT NULL,
  maNV      VARCHAR(20) NOT NULL,
  soGPLX    VARCHAR(30) NULL,
  loaiGPLX  VARCHAR(20) NULL,
  trangThai VARCHAR(30) NULL,
  PRIMARY KEY (maTaiXe),
  UNIQUE KEY uq_taixe_manv (maNV),
  CONSTRAINT fk_taixe_nv FOREIGN KEY (maNV) REFERENCES NhanVien (maNV)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- HT1: KHACH HANG, DICH VU (3.1.4) + thuộc tính 4.1 (1.1, 1.2)
-- ---------------------------------------------------------------------
CREATE TABLE KhachHang (
  maKhachHang  VARCHAR(20)  NOT NULL,
  tenKhachHang VARCHAR(100) NOT NULL,
  soDienThoai  VARCHAR(10)  NULL,
  diachi       VARCHAR(255) NULL,
  email        VARCHAR(100) NULL,   -- [G] 4.1 (1.1)
  loaiKH       VARCHAR(30)  NULL,   -- [G] 4.1 (1.1)
  PRIMARY KEY (maKhachHang)
) ENGINE=InnoDB;
CREATE INDEX idx_kh_ten  ON KhachHang (tenKhachHang);
CREATE INDEX idx_kh_sdt  ON KhachHang (soDienThoai);

CREATE TABLE DichVu (
  maDichVu  VARCHAR(20)   NOT NULL,
  tenDichVu VARCHAR(100)  NOT NULL,
  donGia    DECIMAL(15,2) NULL,     -- [G] 4.1 (1.2)
  moTa      VARCHAR(255)  NULL,     -- [G] 4.1 (1.2)
  PRIMARY KEY (maDichVu)
) ENGINE=InnoDB;

-- Bảng dùng chung (4.2): DON HANG
CREATE TABLE DonHang (
  maDonHang     VARCHAR(10)   NOT NULL,
  maKhachHang   VARCHAR(20)   NOT NULL,
  maDichVu      VARCHAR(20)   NOT NULL,
  nguoiGui      VARCHAR(100)  NULL,
  nguoiNhan     VARCHAR(100)  NULL,
  sdtNguoiGui   VARCHAR(15)   NULL,
  sdtNguoiNhan  VARCHAR(15)   NULL,
  diachiLayHang  VARCHAR(255) NULL,
  diachiGiaoHang VARCHAR(255) NULL,
  khoiLuong     DECIMAL(10,2) NULL,
  tienCOD       DECIMAL(15,2) NULL,
  phiVanChuyen  DECIMAL(15,2) NULL,
  trangThai     VARCHAR(50)   NOT NULL
    COMMENT 'ENUM 4.2: Đã tạo / Đã nhập kho / Đang giao / Đã giao / Đã đối soát / Đã hủy',
  lyDoHuy       VARCHAR(300)  NULL,
  ngayTao       DATETIME      NOT NULL,
  PRIMARY KEY (maDonHang),
  CONSTRAINT chk_donhang_trangthai CHECK (trangThai IN
    ('Đã tạo','Đã nhập kho','Đang giao','Đã giao','Đã đối soát','Đã hủy')),
  CONSTRAINT fk_dh_kh  FOREIGN KEY (maKhachHang) REFERENCES KhachHang (maKhachHang),
  CONSTRAINT fk_dh_dv  FOREIGN KEY (maDichVu)    REFERENCES DichVu (maDichVu)
) ENGINE=InnoDB;
CREATE INDEX idx_dh_trangthai ON DonHang (trangThai);
CREATE INDEX idx_dh_ngaytao   ON DonHang (ngayTao);

-- Danh mục hàng hoá (3.2.4) + dieuKienBaoQuan theo 4.1 (2.8)
CREATE TABLE SanPham (
  maSP              VARCHAR(20)   NOT NULL,
  tenSP             VARCHAR(150)  NOT NULL,
  donViTinh         VARCHAR(50)   NULL,
  gia               DECIMAL(15,2) NULL,
  trangThai         VARCHAR(30)   NULL,
  dieuKienBaoQuan   VARCHAR(255)  NULL,   -- [G6] 4.1 (2.8)
  PRIMARY KEY (maSP)
) ENGINE=InnoDB;

-- Bảng dùng chung (4.2): HANG HOA gắn theo đơn, ánh xạ danh mục qua maSP
CREATE TABLE HangHoa (
  maHangHoa  VARCHAR(10)   NOT NULL,
  maDonHang  VARCHAR(10)   NOT NULL,
  maSP       VARCHAR(20)   NULL COMMENT 'NULL nếu chưa ánh xạ danh mục (4.2)',
  loaiHangHoa VARCHAR(50)  NULL,
  soLuong    INT           NULL,
  trongLuong DOUBLE        NULL,
  PRIMARY KEY (maHangHoa),
  CONSTRAINT fk_hh_dh FOREIGN KEY (maDonHang) REFERENCES DonHang (maDonHang),
  CONSTRAINT fk_hh_sp FOREIGN KEY (maSP)      REFERENCES SanPham (maSP)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- HT2: KHO (3.2.4) + bổ sung 4.1   [bảng SanPham khai báo ở khối HT1 do HangHoa trỏ tới]
-- ---------------------------------------------------------------------
CREATE TABLE PHIEU_NHAP (
  maPhieuNhap VARCHAR(20)   NOT NULL,
  maDonHang   VARCHAR(10)   NULL,     -- [G4] 4.1 (2.1)
  maNCC       VARCHAR(20)   NULL,     -- [G3] giữ cột, không FK
  maNV        VARCHAR(20)   NOT NULL,
  ngayNhap    DATE          NOT NULL,
  trangThai   VARCHAR(30)   NOT NULL,
  tongTien    DECIMAL(15,2) NULL,
  ghiChu      VARCHAR(255)  NULL,
  nguoiDuyet  VARCHAR(20)   NULL,     -- [G4] 4.1 (2.1)
  PRIMARY KEY (maPhieuNhap),
  CONSTRAINT fk_pn_dh  FOREIGN KEY (maDonHang)  REFERENCES DonHang (maDonHang),
  CONSTRAINT fk_pn_nv  FOREIGN KEY (maNV)       REFERENCES NhanVien (maNV),
  CONSTRAINT fk_pn_duyet FOREIGN KEY (nguoiDuyet) REFERENCES NhanVien (maNV)
) ENGINE=InnoDB;
CREATE INDEX idx_pn_ngaynhap ON PHIEU_NHAP (ngayNhap);

CREATE TABLE CHI_TIET_PHIEU_NHAP (
  maPhieuNhap   VARCHAR(20)   NOT NULL,
  maSP          VARCHAR(20)   NOT NULL,
  soLuong       INT           NOT NULL,
  donGia        DECIMAL(15,2) NULL,
  thanhTien     DECIMAL(15,2) NULL,
  tinhTrangHang VARCHAR(50)   NULL,   -- [G7] 4.1 (2.2)
  PRIMARY KEY (maPhieuNhap, maSP),
  CONSTRAINT fk_ctpn_pn FOREIGN KEY (maPhieuNhap) REFERENCES PHIEU_NHAP (maPhieuNhap),
  CONSTRAINT fk_ctpn_sp FOREIGN KEY (maSP)        REFERENCES SanPham (maSP),
  CONSTRAINT chk_ctpn_sl CHECK (soLuong >= 0)
) ENGINE=InnoDB;

CREATE TABLE PHIEU_XUAT (
  maPhieuXuat VARCHAR(20)   NOT NULL,
  maNV        VARCHAR(20)   NOT NULL,
  maDonHang   VARCHAR(10)   NULL,     -- [G18] kích thước theo 4.2
  ngayXuat    DATE          NOT NULL,
  lyDoXuat    VARCHAR(255)  NULL,
  trangThai   VARCHAR(30)   NOT NULL,
  tongTien    DECIMAL(15,2) NULL,
  ghiChu      VARCHAR(255)  NULL,
  PRIMARY KEY (maPhieuXuat),
  CONSTRAINT fk_px_nv FOREIGN KEY (maNV)      REFERENCES NhanVien (maNV),
  CONSTRAINT fk_px_dh FOREIGN KEY (maDonHang) REFERENCES DonHang (maDonHang)
) ENGINE=InnoDB;
CREATE INDEX idx_px_ngayxuat ON PHIEU_XUAT (ngayXuat);

CREATE TABLE CHI_TIET_PHIEU_XUAT (
  maPhieuXuat VARCHAR(20)   NOT NULL,
  maSP        VARCHAR(20)   NOT NULL,
  soLuong     INT           NOT NULL,
  donGia      DECIMAL(15,2) NULL,
  thanhTien   DECIMAL(15,2) NULL,
  PRIMARY KEY (maPhieuXuat, maSP),
  CONSTRAINT fk_ctpx_px FOREIGN KEY (maPhieuXuat) REFERENCES PHIEU_XUAT (maPhieuXuat),
  CONSTRAINT fk_ctpx_sp FOREIGN KEY (maSP)        REFERENCES SanPham (maSP),
  CONSTRAINT chk_ctpx_sl CHECK (soLuong >= 0)
) ENGINE=InnoDB;

CREATE TABLE PHIEU_KIEM_KE (
  maPhieuKiemKe  VARCHAR(20)  NOT NULL,
  ngayKiemKe     DATE         NOT NULL,
  khuVucKiemKe   VARCHAR(100) NULL,
  maNV           VARCHAR(20)  NOT NULL,
  trangThai      VARCHAR(30)  NOT NULL,
  ghiChu         VARCHAR(255) NULL,
  PRIMARY KEY (maPhieuKiemKe),
  CONSTRAINT fk_pkk_nv FOREIGN KEY (maNV) REFERENCES NhanVien (maNV)
) ENGINE=InnoDB;

CREATE TABLE CHI_TIET_KIEM_KE (
  maPhieuKiemKe   VARCHAR(20) NOT NULL,
  maSP            VARCHAR(20) NOT NULL,
  soLuongHeThong  INT         NULL,
  soLuongThucTe   INT         NULL,
  chenhLech       INT         NULL,
  ghiChu          VARCHAR(255) NULL,
  PRIMARY KEY (maPhieuKiemKe, maSP),
  CONSTRAINT fk_ctkk_pkk FOREIGN KEY (maPhieuKiemKe) REFERENCES PHIEU_KIEM_KE (maPhieuKiemKe),
  CONSTRAINT fk_ctkk_sp  FOREIGN KEY (maSP)          REFERENCES SanPham (maSP)
) ENGINE=InnoDB;

CREATE TABLE TON_KHO (
  maTonKho    VARCHAR(20) NOT NULL,
  maSP        VARCHAR(20) NOT NULL,
  maViTri     VARCHAR(50) NOT NULL DEFAULT '',  -- [G5] 4.1 (2.7), chuỗi tự do; '' = chưa put-away
  soLuongTon  INT         NOT NULL DEFAULT 0,
  ngayCapNhat DATETIME    NOT NULL,
  PRIMARY KEY (maTonKho),
  UNIQUE KEY uq_tonkho_sp_vitri (maSP, maViTri),
  CONSTRAINT fk_tk_sp FOREIGN KEY (maSP) REFERENCES SanPham (maSP),
  CONSTRAINT chk_tk_sl CHECK (soLuongTon >= 0)
) ENGINE=InnoDB;

-- [G16] Hạch toán chi phí lưu kho liên thông kế toán (ma trận 4.3, quyền HT5)
CREATE TABLE ChiPhiLuuKho (
  maChiPhi    VARCHAR(20)   NOT NULL,
  maPhieuNhap VARCHAR(20)   NOT NULL,
  soTien      DECIMAL(15,2) NOT NULL,
  ngayGhi     DATETIME      NOT NULL,
  maNVGhi     VARCHAR(20)   NOT NULL,
  PRIMARY KEY (maChiPhi),
  CONSTRAINT fk_cplk_pn FOREIGN KEY (maPhieuNhap) REFERENCES PHIEU_NHAP (maPhieuNhap),
  CONSTRAINT fk_cplk_nv FOREIGN KEY (maNVGhi)     REFERENCES NhanVien (maNV)
) ENGINE=InnoDB;

-- [G16] Biên bản sự cố lô hàng lỗi (luồng phụ bước 4, đặc tả 3.2.3)
CREATE TABLE BienBanSuCo (
  maBienBan   VARCHAR(20)  NOT NULL,
  maPhieuNhap VARCHAR(20)  NULL,
  maDonHang   VARCHAR(10)  NULL,
  loaiSuCo    VARCHAR(50)  NOT NULL COMMENT 'Thừa / Thiếu / Hư hỏng',
  moTa        VARCHAR(500) NULL,
  duongDanAnh VARCHAR(255) NULL,
  maNVLap     VARCHAR(20)  NOT NULL,
  ngayLap     DATETIME     NOT NULL,
  PRIMARY KEY (maBienBan),
  CONSTRAINT fk_bb_pn FOREIGN KEY (maPhieuNhap) REFERENCES PHIEU_NHAP (maPhieuNhap),
  CONSTRAINT fk_bb_dh FOREIGN KEY (maDonHang)   REFERENCES DonHang (maDonHang),
  CONSTRAINT fk_bb_nv FOREIGN KEY (maNVLap)     REFERENCES NhanVien (maNV)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- HT3: DIEU PHOI & GIAO HANG (3.3.4) + bổ sung 4.1
-- ---------------------------------------------------------------------
CREATE TABLE PhuongTien (
  MaPhuongTien INT AUTO_INCREMENT NOT NULL,
  BienSo       VARCHAR(20)   NOT NULL,
  LoaiXe       VARCHAR(50)   NULL,
  TaiTrong     DECIMAL(10,2) NULL,
  TrangThai    VARCHAR(30)   NULL,
  PRIMARY KEY (MaPhuongTien),
  UNIQUE KEY uq_pt_bienso (BienSo)
) ENGINE=InnoDB;

CREATE TABLE ChuyenVan (
  MaChuyen        INT AUTO_INCREMENT NOT NULL,
  MaNguoiLap      VARCHAR(20)  NOT NULL,   -- [G18] FK NhanVien
  MaTaiXe         VARCHAR(20)  NULL,       -- [G18] FK TaiXe
  MaPhuongTien    INT          NULL,
  MaDonHang       VARCHAR(10)  NULL,       -- [G9] 4.1 (3.5), chuyến 1 đơn
  NgayKhoiHanh    DATETIME     NULL,
  ThoiGianDuKien  DATETIME     NULL,
  ThoiGianThucTe  DATETIME     NULL,
  TrangThai       VARCHAR(30)  NOT NULL,
  PRIMARY KEY (MaChuyen),
  CONSTRAINT fk_cv_nl FOREIGN KEY (MaNguoiLap)   REFERENCES NhanVien (maNV),
  CONSTRAINT fk_cv_tx FOREIGN KEY (MaTaiXe)      REFERENCES TaiXe (maTaiXe),
  CONSTRAINT fk_cv_pt FOREIGN KEY (MaPhuongTien) REFERENCES PhuongTien (MaPhuongTien),
  CONSTRAINT fk_cv_dh FOREIGN KEY (MaDonHang)    REFERENCES DonHang (maDonHang)
) ENGINE=InnoDB;
CREATE INDEX idx_cv_trangthai ON ChuyenVan (TrangThai);

-- [G9] Liên kết chuyến - nhiều đơn (bảng nêu trong ma trận phân quyền 4.3)
CREATE TABLE ChiTietChuyenHang (
  MaChuyen  INT         NOT NULL,
  MaDonHang VARCHAR(10) NOT NULL,
  PRIMARY KEY (MaChuyen, MaDonHang),
  CONSTRAINT fk_ctch_cv FOREIGN KEY (MaChuyen)  REFERENCES ChuyenVan (MaChuyen),
  CONSTRAINT fk_ctch_dh FOREIGN KEY (MaDonHang) REFERENCES DonHang (maDonHang)
) ENGINE=InnoDB;

CREATE TABLE LoTrinh (
  MaLoTrinh      INT AUTO_INCREMENT NOT NULL,
  MaChuyen       INT           NOT NULL,
  DiemXuatPhat   VARCHAR(255)  NULL,
  DiemKetThuc    VARCHAR(255)  NULL,
  KhoangCach     DECIMAL(10,2) NULL,
  ThoiGianDuKien INT           NULL,
  MoTa           TEXT          NULL,
  PRIMARY KEY (MaLoTrinh),
  CONSTRAINT fk_lt_cv FOREIGN KEY (MaChuyen) REFERENCES ChuyenVan (MaChuyen)
) ENGINE=InnoDB;

CREATE TABLE SuCoVanTai (
  MaSuCo    INT AUTO_INCREMENT NOT NULL,
  MaChuyen  INT          NOT NULL,
  MaDonHang VARCHAR(10)  NULL,       -- [G18]
  MaTaiXe   VARCHAR(20)  NULL,       -- [G18]
  LoaiSuCo  VARCHAR(50)  NOT NULL,
  MoTa      TEXT         NULL,
  ThoiGian  DATETIME     NOT NULL,
  TrangThai VARCHAR(30)  NOT NULL,
  HinhAnh   VARCHAR(255) NULL,       -- [G8] 4.1 (3.7)
  PRIMARY KEY (MaSuCo),
  CONSTRAINT fk_scv_cv FOREIGN KEY (MaChuyen)  REFERENCES ChuyenVan (MaChuyen),
  CONSTRAINT fk_scv_dh FOREIGN KEY (MaDonHang) REFERENCES DonHang (maDonHang),
  CONSTRAINT fk_scv_tx FOREIGN KEY (MaTaiXe)   REFERENCES TaiXe (maTaiXe)
) ENGINE=InnoDB;

-- [G16] Ảnh minh chứng / chữ ký khi xác nhận giao hàng thành công (3.3.3)
CREATE TABLE MinhChungGiaoHang (
  maMinhChung  VARCHAR(20) NOT NULL,
  maDonHang    VARCHAR(10) NOT NULL,
  loaiMinhChung ENUM('Anh','ChuKy') NOT NULL,
  duongDan     VARCHAR(255) NOT NULL,
  maNVTai      VARCHAR(20)  NOT NULL,
  thoiGianTai  DATETIME     NOT NULL,
  PRIMARY KEY (maMinhChung),
  CONSTRAINT fk_mc_dh FOREIGN KEY (maDonHang) REFERENCES DonHang (maDonHang),
  CONSTRAINT fk_mc_nv FOREIGN KEY (maNVTai)   REFERENCES NhanVien (maNV)
) ENGINE=InnoDB;

-- [G16] Lịch sử đơn: tạo, cập nhật, duyệt, từ chối, hủy, đổi trạng thái (3.1.1, 3.1.2, 4.3)
CREATE TABLE LichSuDonHang (
  maLichSu    INT AUTO_INCREMENT NOT NULL,
  maDonHang   VARCHAR(10)  NOT NULL,
  hanhDong    VARCHAR(50)  NOT NULL
    COMMENT 'Tao / CapNhat / GuiDuyet / Duyet / TuChoi / Huy / DoiTrangThai',
  trangThai   VARCHAR(50)  NOT NULL,
  maTaiKhoan  VARCHAR(10)  NOT NULL,
  thoiGian    DATETIME     NOT NULL,
  ghiChu      VARCHAR(300) NULL,
  PRIMARY KEY (maLichSu),
  CONSTRAINT fk_ls_dh FOREIGN KEY (maDonHang)  REFERENCES DonHang (maDonHang),
  CONSTRAINT fk_ls_tk FOREIGN KEY (maTaiKhoan) REFERENCES TaiKhoan (maTaiKhoan)
) ENGINE=InnoDB;
CREATE INDEX ls_dh_don ON LichSuDonHang (maDonHang, thoiGian);

-- ---------------------------------------------------------------------
-- HT5: THU/CHI - COD (3.5.4) + bổ sung 4.1
-- ---------------------------------------------------------------------
CREATE TABLE GiaoDichCOD (
  maGiaoDich     VARCHAR(10)   NOT NULL,
  maDonHang      VARCHAR(10)   NOT NULL,
  maCOD          VARCHAR(10)   NULL,
  soTienPhaiThu  DECIMAL(15,2) NOT NULL,   -- [G12] 4.1 (5.2)
  soTienThucThu  DECIMAL(15,2) NULL,       -- [G12] 4.1 (5.2)
  trangThai      VARCHAR(20)   NOT NULL,
  ngayTao        DATE          NOT NULL,
  thoiGianCapNhat DATETIME     NULL,
  nguoiCapNhat   VARCHAR(20)   NULL,
  PRIMARY KEY (maGiaoDich),
  CONSTRAINT fk_gd_dh FOREIGN KEY (maDonHang)    REFERENCES DonHang (maDonHang),
  CONSTRAINT fk_gd_nv FOREIGN KEY (nguoiCapNhat) REFERENCES NhanVien (maNV)
) ENGINE=InnoDB;
CREATE INDEX idx_gd_trangthai ON GiaoDichCOD (trangThai);

CREATE TABLE PhieuThuChi (
  maPhieu    VARCHAR(10)   NOT NULL,
  loaiPhieu  VARCHAR(30)   NOT NULL COMMENT 'Thu / Chi',
  maGiaoDich VARCHAR(10)   NULL,       -- [G13] 4.1 (5.3)
  soTien     DECIMAL(15,2) NOT NULL,
  ngayLap    DATE          NOT NULL,
  noiDung    VARCHAR(100)  NULL,
  trangThai  VARCHAR(20)   NOT NULL,
  nguoiLap   VARCHAR(20)   NOT NULL,   -- [G13] 4.1 (5.3)
  nguoiDuyet VARCHAR(20)   NULL,       -- [G13] 4.1 (5.3)
  PRIMARY KEY (maPhieu),
  CONSTRAINT fk_ptc_gd  FOREIGN KEY (maGiaoDich) REFERENCES GiaoDichCOD (maGiaoDich),
  CONSTRAINT fk_ptc_lap FOREIGN KEY (nguoiLap)   REFERENCES NhanVien (maNV),
  CONSTRAINT fk_ptc_duyet FOREIGN KEY (nguoiDuyet) REFERENCES NhanVien (maNV),
  CONSTRAINT chk_ptc_tien CHECK (soTien >= 0)
) ENGINE=InnoDB;

-- [G14] Sổ quỹ dạng nhật ký: mỗi dòng gắn một phiếu thu/chi
CREATE TABLE SoQuy (
  maSoQuy   VARCHAR(10)   NOT NULL,
  maPhieu   VARCHAR(10)   NOT NULL,
  soTienThu DECIMAL(15,2) NOT NULL DEFAULT 0,
  soTienChi DECIMAL(15,2) NOT NULL DEFAULT 0,
  ngayGhiSo DATE          NOT NULL,
  maNVGhiSo VARCHAR(20)   NOT NULL,
  PRIMARY KEY (maSoQuy),
  UNIQUE KEY uq_soquay_phieu (maPhieu),
  CONSTRAINT fk_sq_ptc FOREIGN KEY (maPhieu)   REFERENCES PhieuThuChi (maPhieu),
  CONSTRAINT fk_sq_nv  FOREIGN KEY (maNVGhiSo) REFERENCES NhanVien (maNV),
  CONSTRAINT chk_sq_thu CHECK (soTienThu >= 0),
  CONSTRAINT chk_sq_chi CHECK (soTienChi >= 0)
) ENGINE=InnoDB;

CREATE TABLE CongNo (
  maCongNo      VARCHAR(10)   NOT NULL,
  maKhachHang   VARCHAR(20)   NOT NULL,   -- [G11]
  soTienPhaiTra DECIMAL(15,2) NOT NULL DEFAULT 0,
  soTienDaTra   DECIMAL(15,2) NOT NULL DEFAULT 0,
  soDuNo        DECIMAL(15,2) GENERATED ALWAYS AS (soTienPhaiTra - soTienDaTra) STORED,
  trangThai     VARCHAR(20)   NOT NULL,
  ngayCapNhat   DATETIME      NOT NULL,
  PRIMARY KEY (maCongNo),
  CONSTRAINT fk_cn_kh FOREIGN KEY (maKhachHang) REFERENCES KhachHang (maKhachHang)
) ENGINE=InnoDB;

CREATE TABLE DoiSoatCOD (
  maDoiSoat   VARCHAR(10)   NOT NULL,
  ngayDoiSoat DATE          NOT NULL,
  tongTien    DECIMAL(15,2) NULL,
  chenhLech   DECIMAL(15,2) NULL,
  trangThai   VARCHAR(20)   NOT NULL,
  nguoiDoiSoat VARCHAR(20)  NOT NULL,   -- [G15] 4.1 (5.7)
  thoiGian    DATETIME      NOT NULL,   -- [G15] 4.1 (5.7)
  PRIMARY KEY (maDoiSoat),
  CONSTRAINT fk_ds_nv FOREIGN KEY (nguoiDoiSoat) REFERENCES NhanVien (maNV)
) ENGINE=InnoDB;

-- [G15] Chi tiết đợt đối soát: một đợt gồm nhiều giao dịch COD
CREATE TABLE ChiTietDoiSoat (
  maDoiSoat  VARCHAR(10) NOT NULL,
  maGiaoDich VARCHAR(10) NOT NULL,
  PRIMARY KEY (maDoiSoat, maGiaoDich),
  CONSTRAINT fk_ctds_ds FOREIGN KEY (maDoiSoat)  REFERENCES DoiSoatCOD (maDoiSoat),
  CONSTRAINT fk_ctds_gd FOREIGN KEY (maGiaoDich) REFERENCES GiaoDichCOD (maGiaoDich)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- VIEW HỖ TRỢ TRA CỨU (không lưu trùng dữ liệu)
-- ---------------------------------------------------------------------
CREATE OR REPLACE VIEW vw_SoDuQuy AS
SELECT COALESCE(SUM(soTienThu),0) - COALESCE(SUM(soTienChi),0) AS soDuHienTai
FROM SoQuy;

CREATE OR REPLACE VIEW vw_TonKhoChiTiet AS
SELECT t.maTonKho, t.maSP, s.tenSP, s.donViTinh, t.maViTri, t.soLuongTon, t.ngayCapNhat
FROM TON_KHO t JOIN SanPham s ON s.maSP = t.maSP;

CREATE OR REPLACE VIEW vw_CongNoConLai AS
SELECT c.maCongNo, c.maKhachHang, k.tenKhachHang,
       c.soTienPhaiTra, c.soTienDaTra, c.soDuNo, c.trangThai, c.ngayCapNhat
FROM CongNo c JOIN KhachHang k ON k.maKhachHang = c.maKhachHang;

-- =====================================================================
-- BẢNG ĐỐI CHIẾU NGUỒN: bảng -> mục tài liệu
--   PhongBan, ChucVu, TrangThaiNhanVien            : 3.4.4
--   NhanVien, TaiXe, TaiKhoan, DonHang, HangHoa    : 4.2 (dùng chung)
--   KhachHang, DichVu                              : 3.1.4 (+4.1)
--   SanPham, PHIEU_NHAP, CHI_TIET_PHIEU_NHAP,
--   PHIEU_XUAT, CHI_TIET_PHIEU_XUAT,
--   PHIEU_KIEM_KE, CHI_TIET_KIEM_KE, TON_KHO       : 3.2.4 (+4.1)
--   PhuongTien, ChuyenVan, LoTrinh, SuCoVanTai     : 3.3.4 (+4.1)
--   ChiTietChuyenHang                              : ma trận 4.3
--   CongNo, GiaoDichCOD, PhieuThuChi, SoQuy,
--   DoiSoatCOD                                     : 3.5.4 (+4.1)
--   ChiPhiLuuKho, BienBanSuCo, MinhChungGiaoHang,
--   LichSuDonHang, ChiTietDoiSoat                  : bảng kỹ thuật [G15,G16]
-- =====================================================================

-- ===== V2__seed.sql =====
-- =====================================================================
-- V2__seed.sql — DỮ LIỆU GIẢ LẬP LỚP 1: DANH MỤC NỀN (nhiệm vụ 0.5)
-- Nguồn: mục 7 huong_di_lap_trinh (seed theo lớp) + 1.3 "dữ liệu giả lập vừa đủ minh họa luồng".
--
-- Lớp 1 (file này): phòng ban, chức vụ, trạng thái NV, nhân viên đủ vai trò + tài khoản
--                   đăng nhập, tài xế, phương tiện, khách hàng, dịch vụ, sản phẩm.
-- Lớp 2 (nghiệp vụ: đơn đã duyệt → phiếu nhập → tồn kho → …) seed ở phase tương ứng.
-- Lớp 3 (kịch bản luồng phụ) seed ở Phase 7 (nhiệm vụ 7.1).
--
-- MẬT KHẨU DEMO: mọi tài khoản dùng chung mật khẩu  123456
-- (băm BCrypt thật, đã kiểm chứng matches() = true; matKhau VARCHAR(255) theo 4.2).
-- =====================================================================
SET NAMES utf8mb4;

-- ---------------------------------------------------------------------
-- HT4: DANH MỤC NHÂN SỰ (3.4.4)
-- ---------------------------------------------------------------------
INSERT INTO PhongBan (maPhongBan, tenPhongBan) VALUES
  ('PB01', 'Phòng Kinh doanh'),
  ('PB02', 'Phòng Kho vận'),
  ('PB03', 'Phòng Vận tải – Giao nhận'),
  ('PB04', 'Phòng Nhân sự'),
  ('PB05', 'Phòng Tài chính – Kế toán');

INSERT INTO ChucVu (maChucVu, tenChucVu) VALUES
  ('CV01', 'Nhân viên kinh doanh'),
  ('CV02', 'Quản lý kinh doanh'),
  ('CV03', 'Nhân viên kho'),
  ('CV04', 'Quản lý kho'),
  ('CV05', 'Nhân viên điều phối'),
  ('CV06', 'Quản lý điều phối'),
  ('CV07', 'Nhân viên giao hàng'),
  ('CV08', 'Tài xế'),
  ('CV09', 'Nhân viên nhân sự'),
  ('CV10', 'Quản lý nhân sự'),
  ('CV11', 'Kế toán viên'),
  ('CV12', 'Thủ quỹ'),
  ('CV13', 'Kế toán trưởng'),
  ('CV14', 'Nhân viên');

INSERT INTO TrangThaiNhanVien (maTrangThai, tenTrangThai) VALUES
  ('TT01', 'Đang làm việc'),
  ('TT02', 'Nghỉ việc'),
  ('TT03', 'Thử việc');

-- ---------------------------------------------------------------------
-- NHÂN VIÊN ĐỦ CÁC VAI TRÒ NGHIỆP VỤ (danh sách vai trò: mục 6 huong_di_lap_trinh)
-- ---------------------------------------------------------------------
INSERT INTO NhanVien (maNV, hoTen, ngaySinh, gioiTinh, soDienThoai, diaChi, email, maPhongBan, maChucVu, maTrangThai, ngayVaoLam) VALUES
  ('NV0000001', 'Nguyễn Văn An',    '1994-03-12', 'Nam', '0903111222', '12 Lê Lợi, Q.1, TP.HCM',            'an.nguyen@hoangminh.vn',    'PB01', 'CV01', 'TT01', '2022-04-01'),
  ('NV0000002', 'Trần Thị Bích',    '1988-07-25', 'Nữ',  '0903222333', '45 Nguyễn Huệ, Q.1, TP.HCM',        'bich.tran@hoangminh.vn',    'PB01', 'CV02', 'TT01', '2019-08-15'),
  ('NV0000003', 'Lê Văn Cường',     '1996-11-02', 'Nam', '0903333444', '88 Quang Trung, Gò Vấp, TP.HCM',    'cuong.le@hoangminh.vn',     'PB02', 'CV03', 'TT01', '2023-02-01'),
  ('NV0000004', 'Phạm Thị Dung',    '1990-01-19', 'Nữ',  '0903444555', '210 Phan Văn Trị, Bình Thạnh',      'dung.pham@hoangminh.vn',    'PB02', 'CV04', 'TT01', '2020-06-10'),
  ('NV0000005', 'Hoàng Văn Em',     '1993-09-08', 'Nam', '0903555666', '77 Cách Mạng Tháng 8, Q.3',         'em.hoang@hoangminh.vn',     'PB03', 'CV05', 'TT01', '2021-09-01'),
  ('NV0000006', 'Vũ Thị Giang',     '1987-05-30', 'Nữ',  '0903666777', '9 Võ Thị Sáu, Q.3, TP.HCM',         'giang.vu@hoangminh.vn',     'PB03', 'CV06', 'TT01', '2018-11-20'),
  ('NV0000007', 'Đặng Văn Hải',     '1995-12-14', 'Nam', '0903777888', '34 Xô Viết Nghệ Tĩnh, Bình Thạnh',  'hai.dang@hoangminh.vn',     'PB03', 'CV07', 'TT01', '2022-07-05'),
  ('NV0000008', 'Bùi Văn Ích',      '1991-04-22', 'Nam', '0903888999', '120 Bạch Đằng, Q.Bình Thạnh',       'ich.bui@hoangminh.vn',      'PB03', 'CV08', 'TT01', '2020-03-16'),
  ('NV0000009', 'Đỗ Thị Kim',       '1992-08-09', 'Nữ',  '0903999000', '5 Nguyễn Đình Chiểu, Q.3',           'kim.do@hoangminh.vn',       'PB04', 'CV09', 'TT01', '2021-01-11'),
  ('NV0000010', 'Ngô Văn Long',     '1985-02-27', 'Nam', '0904111223', '66 Trần Quốc Thảo, Q.3',             'long.ngo@hoangminh.vn',     'PB04', 'CV10', 'TT01', '2017-05-02'),
  ('NV0000011', 'Lý Thị Mai',       '1993-06-17', 'Nữ',  '0904222334', '231 Hai Bà Trưng, Q.1',              'mai.ly@hoangminh.vn',       'PB05', 'CV11', 'TT01', '2020-10-01'),
  ('NV0000012', 'Trịnh Văn Nam',    '1989-10-03', 'Nam', '0904333445', '18 Cộng Hòa, Q.Tân Bình',            'nam.trinh@hoangminh.vn',    'PB05', 'CV12', 'TT01', '2019-03-25'),
  ('NV0000013', 'Phan Thị Oanh',    '1984-12-21', 'Nữ',  '0904444556', '90 Pasteur, Q.1, TP.HCM',            'oanh.phan@hoangminh.vn',    'PB05', 'CV13', 'TT01', '2016-09-12'),
  ('NV0000014', 'Võ Văn Phúc',      '1998-03-05', 'Nam', '0904555667', '44 Lê Văn Sỹ, Q.Phú Nhuận',         'phuc.vo@hoangminh.vn',      'PB01', 'CV14', 'TT03', '2025-01-06'),
  ('NV0000015', 'Dương Thị Quỳnh',  '1994-07-11', 'Nữ',  '0904666778', '3 Trần Hưng Đạo, Q.5',               'quynh.duong@hoangminh.vn',  'PB02', 'CV03', 'TT02', '2019-04-18'),
  ('NV0000016', 'Hồ Văn Rin',       '1997-01-29', 'Nam', '0904777889', '71 Tôn Thất Thuyết, Q.4',            'rin.ho@hoangminh.vn',       'PB03', 'CV07', 'TT01', '2023-08-14'),
  ('NV0000017', 'Mai Văn Sơn',      '1999-09-23', 'Nam', '0904888990', '56 Bùi Hữu Nghĩa, Q.5',              'son.mai@hoangminh.vn',      'PB03', 'CV08', 'TT03', '2025-03-03');

-- ---------------------------------------------------------------------
-- TÀI XẾ (bảng dùng chung 4.2) — TX0000004 cố tình thiếu GPLX để kiểm tra
-- cảnh báo chứng chỉ của 3.3.2 (PHỤ LỤC D mục 3: không có trường ngày hết hạn).
-- ---------------------------------------------------------------------
INSERT INTO TaiXe (maTaiXe, maNV, soGPLX, loaiGPLX, trangThai) VALUES
  ('TX0000001', 'NV0000007', '0123456789', 'B2', 'Hoạt động'),
  ('TX0000002', 'NV0000008', '0987654321', 'C',  'Hoạt động'),
  ('TX0000003', 'NV0000016', '7900123456', 'A1', 'Hoạt động'),
  ('TX0000004', 'NV0000017', NULL,         NULL, 'Hoạt động');

-- ---------------------------------------------------------------------
-- TÀI KHOẢN ĐĂNG NHẬP (bảng dùng chung 4.2) — mật khẩu demo: 123456
-- TA0000015 ở trạng thái "Đã khóa" để nghiệm thu 0.4 (tài khoản khóa bị từ chối).
-- ---------------------------------------------------------------------
INSERT INTO TaiKhoan (maTaiKhoan, maNV, tenDangNhap, matKhau, vaiTro, trangThai) VALUES
  ('TA0000001', 'NV0000001', 'nvkd',      '$2a$10$WjdWKMD/9n0bvBtJb6SDt.a3ru4wTPGveyGaQsuOnOf0CUrLZJnI2', 'NV_KINH_DOANH',  'Hoạt động'),
  ('TA0000002', 'NV0000002', 'qlkd',      '$2a$10$WjdWKMD/9n0bvBtJb6SDt.a3ru4wTPGveyGaQsuOnOf0CUrLZJnI2', 'QL_KINH_DOANH',  'Hoạt động'),
  ('TA0000003', 'NV0000003', 'nvkho',     '$2a$10$WjdWKMD/9n0bvBtJb6SDt.a3ru4wTPGveyGaQsuOnOf0CUrLZJnI2', 'NV_KHO',         'Hoạt động'),
  ('TA0000004', 'NV0000004', 'qlkho',     '$2a$10$WjdWKMD/9n0bvBtJb6SDt.a3ru4wTPGveyGaQsuOnOf0CUrLZJnI2', 'QL_KHO',         'Hoạt động'),
  ('TA0000005', 'NV0000005', 'nvdp',      '$2a$10$WjdWKMD/9n0bvBtJb6SDt.a3ru4wTPGveyGaQsuOnOf0CUrLZJnI2', 'NV_DIEU_PHOI',   'Hoạt động'),
  ('TA0000006', 'NV0000006', 'qldp',      '$2a$10$WjdWKMD/9n0bvBtJb6SDt.a3ru4wTPGveyGaQsuOnOf0CUrLZJnI2', 'QL_DIEU_PHOI',   'Hoạt động'),
  ('TA0000007', 'NV0000007', 'nvgh',      '$2a$10$WjdWKMD/9n0bvBtJb6SDt.a3ru4wTPGveyGaQsuOnOf0CUrLZJnI2', 'NV_GIAO_HANG',   'Hoạt động'),
  ('TA0000008', 'NV0000008', 'tx1',       '$2a$10$WjdWKMD/9n0bvBtJb6SDt.a3ru4wTPGveyGaQsuOnOf0CUrLZJnI2', 'NV_GIAO_HANG',   'Hoạt động'),
  ('TA0000009', 'NV0000009', 'nvns',      '$2a$10$WjdWKMD/9n0bvBtJb6SDt.a3ru4wTPGveyGaQsuOnOf0CUrLZJnI2', 'NV_NHAN_SU',     'Hoạt động'),
  ('TA0000010', 'NV0000010', 'qlns',      '$2a$10$WjdWKMD/9n0bvBtJb6SDt.a3ru4wTPGveyGaQsuOnOf0CUrLZJnI2', 'QL_NHAN_SU',     'Hoạt động'),
  ('TA0000011', 'NV0000011', 'ktv',       '$2a$10$WjdWKMD/9n0bvBtJb6SDt.a3ru4wTPGveyGaQsuOnOf0CUrLZJnI2', 'KE_TOAN_VIEN',   'Hoạt động'),
  ('TA0000012', 'NV0000012', 'thuquy',    '$2a$10$WjdWKMD/9n0bvBtJb6SDt.a3ru4wTPGveyGaQsuOnOf0CUrLZJnI2', 'THU_QUY',        'Hoạt động'),
  ('TA0000013', 'NV0000013', 'ktt',       '$2a$10$WjdWKMD/9n0bvBtJb6SDt.a3ru4wTPGveyGaQsuOnOf0CUrLZJnI2', 'KE_TOAN_TRUONG', 'Hoạt động'),
  ('TA0000014', 'NV0000014', 'nvthuong',  '$2a$10$WjdWKMD/9n0bvBtJb6SDt.a3ru4wTPGveyGaQsuOnOf0CUrLZJnI2', 'NHAN_VIEN',      'Hoạt động'),
  ('TA0000015', 'NV0000015', 'nvkho2',    '$2a$10$WjdWKMD/9n0bvBtJb6SDt.a3ru4wTPGveyGaQsuOnOf0CUrLZJnI2', 'NV_KHO',         'Đã khóa'),
  ('TA0000016', 'NV0000016', 'nvgh2',     '$2a$10$WjdWKMD/9n0bvBtJb6SDt.a3ru4wTPGveyGaQsuOnOf0CUrLZJnI2', 'NV_GIAO_HANG',   'Hoạt động');

-- ---------------------------------------------------------------------
-- HT1: KHÁCH HÀNG, DỊCH VỤ (3.1.4)
-- ---------------------------------------------------------------------
INSERT INTO KhachHang (maKhachHang, tenKhachHang, soDienThoai, diachi, email, loaiKH) VALUES
  ('KH0000001', 'Công ty TNHH Minh Phát',      '0908221144', '120 Nguyễn Văn Trỗi, Q.Phú Nhuận, TP.HCM', 'ke.toan@minhphat.vn',   'Doanh nghiệp'),
  ('KH0000002', 'Công ty CP Thực phẩm Xanh',   '0909112233', '45 KCN Tân Bình, Q.Tân Phú, TP.HCM',        'sales@thucphamxanh.vn', 'Doanh nghiệp'),
  ('KH0000003', 'Shop thời trang MiSa',        '0908123456',  '78 Lê Văn Sỹ, Q.3, TP.HCM',                 'misa.shop@gmail.com',   'Cá nhân'),
  ('KH0000004', 'Siêu thị điện máy Đại Lộc',   '0907556677', '201 Hùng Vương, Q.5, TP.HCM',               'nhaphang@dailoc.vn',    'Doanh nghiệp'),
  ('KH0000005', 'Bà Trần Thu Hà',              '0913567890',  '9 Đinh Tiên Hoàng, Q.1, TP.HCM',            'thuha.tran@yahoo.com',  'Cá nhân'),
  ('KH0000006', 'Công ty TNHH Dược Tâm An',    '0908445566', '332 Cách Mạng Tháng 8, Q.3, TP.HCM',        'mua.hang@duoctaman.vn', 'Doanh nghiệp'),
  ('KH0000007', 'Nhà sách Khai Trí',           '0909223344', '62 Lê Lợi, Q.1, TP.HCM',                    'nhaxuatkha@khai.com',   'Doanh nghiệp'),
  ('KH0000008', 'Ông Phạm Văn Đức',            '0937445566',  '15 Quang Trung, Gò Vấp, TP.HCM',            'ducpham.vn@gmail.com',  'Cá nhân');

INSERT INTO DichVu (maDichVu, tenDichVu, donGia, moTa) VALUES
  ('DV01', 'Giao hàng nội thành',         25000.00,  'Giao trong phạm vi TP.HCM, trong 24 giờ'),
  ('DV02', 'Giao hàng liên tỉnh',         60000.00,  'Giao đi các tỉnh, 2–4 ngày'),
  ('DV03', 'Chuyển phát nhanh trong ngày', 45000.00, 'Nhận trước 10 giờ, giao trước 18 giờ cùng ngày'),
  ('DV04', 'Vận chuyển hàng nguyên chuyến', 1500000.00, 'Thuê trọn xe theo chuyến, có tài xế'),
  ('DV05', 'Dịch vụ lưu kho',             500000.00,  'Lưu kho theo tháng, tính theo mét khối');

-- ---------------------------------------------------------------------
-- HT2: DANH MỤC SẢN PHẨM (3.2.4 + [G6] dieuKienBaoQuan theo 4.1)
-- ---------------------------------------------------------------------
INSERT INTO SanPham (maSP, tenSP, donViTinh, gia, trangThai, dieuKienBaoQuan) VALUES
  ('SP001', 'Thùng nước khoáng 500ml x 24 chai', 'Thùng',  95000.00, 'Đang kinh doanh', 'Nơi khô ráo, tránh ánh nắng trực tiếp'),
  ('SP002', 'Bao gạo ST25 5kg',                  'Bao',   185000.00, 'Đang kinh doanh', 'Kệ khô, cách mặt sàn 20cm'),
  ('SP003', 'Dầu ăn thực vật 5L',                'Can',   265000.00, 'Đang kinh doanh', 'Nhiệt độ thường, tránh nguồn nhiệt'),
  ('SP004', 'Nồi cơm điện 1.8L',                 'Cái',   890000.00, 'Đang kinh doanh', 'Kệ hàng điện tử, chống va đập'),
  ('SP005', 'Ấm siêu tốc inox 1.7L',             'Cái',   320000.00, 'Đang kinh doanh', 'Kệ hàng điện tử, chống ẩm'),
  ('SP006', 'Áo thun cotton size M',             'Cái',   120000.00, 'Đang kinh doanh', 'Kệ khô, đóng túi nilon'),
  ('SP007', 'Giày thể thao size 41',             'Đôi',   450000.00, 'Đang kinh doanh', 'Kệ khô, giữ nguyên hộp'),
  ('SP008', 'Thuốc bổ sung vitamin C',           'Hộp',   145000.00, 'Đang kinh doanh', 'Kho mát dưới 25 độ C'),
  ('SP009', 'Sách giáo khoa lớp 10 (bộ)',        'Bộ',    210000.00, 'Đang kinh doanh', 'Kệ khô, tránh ẩm mốc'),
  ('SP010', 'Máy sấy tóc 1200W',                 'Cái',   380000.00, 'Ngừng kinh doanh', 'Kệ hàng điện tử, chống va đập');

-- ---------------------------------------------------------------------
-- HT3: PHƯƠNG TIỆN (3.3.4)
-- ---------------------------------------------------------------------
INSERT INTO PhuongTien (MaPhuongTien, BienSo, LoaiXe, TaiTrong, TrangThai) VALUES
  (1, '51C-123.45', 'Xe tải 1 tấn',   1000.00, 'Sẵn sàng'),
  (2, '51D-234.56', 'Xe tải 2.5 tấn', 2500.00, 'Sẵn sàng'),
  (3, '62E-345.67', 'Xe tải 5 tấn',   5000.00, 'Đang bảo trì'),
  (4, '59F-456.78', 'Xe ba gác',       350.00, 'Sẵn sàng'),
  (5, '50A-567.89', 'Xe máy giao hàng', 80.00, 'Sẵn sàng');


-- ===== V3__ht1_demo.sql =====
-- Dữ liệu giả lập lớp 2 cho Phase 1. Chưa chuyển sang trạng thái do HT2/HT3 sở hữu.
INSERT INTO DonHang (maDonHang,maKhachHang,maDichVu,nguoiGui,nguoiNhan,sdtNguoiGui,sdtNguoiNhan,diachiLayHang,diachiGiaoHang,khoiLuong,tienCOD,phiVanChuyen,trangThai,ngayTao) VALUES
('DH0000001','KH0000001','DV02','Minh Phát','Nguyễn An','0908221144','0901234567','120 Nguyễn Văn Trỗi, TP.HCM','50 Trần Phú, Đà Nẵng',12,1500000,60000,'Đã tạo','2026-09-14 08:00:00'),
('DH0000002','KH0000003','DV01','Shop MiSa','Trần Bình','0908123456','0902345678','78 Lê Văn Sỹ, TP.HCM','25 Nguyễn Huệ, TP.HCM',3,450000,25000,'Đã tạo','2026-09-14 09:00:00'),
('DH0000003','KH0000005','DV03','Trần Thu Hà','Lê Chi','0913567890','0903456789','9 Đinh Tiên Hoàng, TP.HCM','12 Lê Lợi, TP.HCM',2,0,45000,'Đã tạo','2026-09-13 08:00:00');
INSERT INTO HangHoa (maHangHoa,maDonHang,loaiHangHoa,soLuong,trongLuong) VALUES
('HH0000001','DH0000001','Thùng hàng gia dụng',4,12),
('HH0000002','DH0000002','Kiện quần áo',2,3),
('HH0000003','DH0000003','Hồ sơ đóng kiện',1,2);
INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
('DH0000001','Tao','Đã tạo','TA0000001','2026-09-14 08:00:00','Dữ liệu demo Phase 1'),
('DH0000001','GuiDuyet','Đã tạo','TA0000001','2026-09-14 08:10:00',NULL),
('DH0000001','Duyet','Đã tạo','TA0000002','2026-09-14 08:20:00','Sẵn sàng cho HT2 tiếp nhận'),
('DH0000002','Tao','Đã tạo','TA0000001','2026-09-14 09:00:00','Dữ liệu demo Phase 1'),
('DH0000002','GuiDuyet','Đã tạo','TA0000001','2026-09-14 09:10:00',NULL),
('DH0000003','Tao','Đã tạo','TA0000001','2026-09-13 08:00:00','Dữ liệu demo Phase 1'),
('DH0000003','GuiDuyet','Đã tạo','TA0000001','2026-09-13 08:10:00',NULL),
('DH0000003','TuChoi','Đã tạo','TA0000002','2026-09-13 08:20:00','Xác nhận lại địa chỉ người nhận');

-- ===== V4__ht2_demo.sql =====
INSERT INTO PHIEU_NHAP (maPhieuNhap,maDonHang,maNCC,maNV,ngayNhap,trangThai,tongTien,ghiChu)
VALUES ('PN0000001','DH0000001','NCC-DEMO','NV0000003','2026-09-14','Chờ duyệt',1200000,'Phiếu demo Phase 2 chờ quản lý kho duyệt');
INSERT INTO CHI_TIET_PHIEU_NHAP (maPhieuNhap,maSP,soLuong,donGia,thanhTien,tinhTrangHang)
VALUES ('PN0000001','SP001',12,100000,1200000,'Đạt');

-- ===== V5__phase7_demo.sql =====
-- Phase 7: dữ liệu giả lập cho các luồng phụ và màn hình nghiệm thu.
-- Không đổi lược đồ; mọi mã được cố định để kịch bản có thể đối chiếu trong tài liệu demo.
INSERT INTO ChuyenVan (MaChuyen, MaNguoiLap, MaTaiXe, MaPhuongTien, MaDonHang, NgayKhoiHanh, ThoiGianDuKien, TrangThai)
VALUES (1, 'NV0000005', 'TX0000001', 1, 'DH0000001', '2026-09-15 08:00:00', '2026-09-15 12:00:00', 'Chờ điều chỉnh');
INSERT INTO ChiTietChuyenHang (MaChuyen, MaDonHang) VALUES (1, 'DH0000001');
INSERT INTO LoTrinh (MaLoTrinh, MaChuyen, DiemXuatPhat, DiemKetThuc, KhoangCach, ThoiGianDuKien, MoTa)
VALUES (1, 1, 'Kho Hoàng Minh', '50 Trần Phú, Đà Nẵng', 950, 240, 'Luồng phụ: điều phối yêu cầu điều chỉnh tuyến');
INSERT INTO SuCoVanTai (MaSuCo, MaChuyen, MaDonHang, MaTaiXe, LoaiSuCo, MoTa, ThoiGian, TrangThai, HinhAnh)
VALUES (1, 1, 'DH0000001', 'TX0000001', 'Tắc đường', 'Tuyến quốc lộ ùn tắc, đề nghị đổi lộ trình', '2026-09-15 09:30:00', 'Đang xử lý', 'demo/tac-duong.jpg');
INSERT INTO MinhChungGiaoHang (maMinhChung, maDonHang, loaiMinhChung, duongDan, maNVTai, thoiGianTai)
VALUES ('MC0000001', 'DH0000001', 'Anh', 'demo/giao-hang-dh0000001.jpg', 'NV0000007', '2026-09-15 14:00:00');
INSERT INTO GiaoDichCOD (maGiaoDich, maDonHang, maCOD, soTienPhaiThu, soTienThucThu, trangThai, ngayTao, thoiGianCapNhat, nguoiCapNhat)
VALUES ('GD0000001', 'DH0000002', 'COD01', 450000, 400000, 'Sai lệch', '2026-09-15', '2026-09-15 18:00:00', 'NV0000011');
INSERT INTO PhieuThuChi (maPhieu, loaiPhieu, maGiaoDich, soTien, ngayLap, noiDung, trangThai, nguoiLap)
VALUES ('PT0000001', 'Thu', 'GD0000001', 400000, '2026-09-15', 'Thu COD thiếu 50.000 đồng, chờ kiểm soát', 'Chờ duyệt', 'NV0000012');
INSERT INTO DoiSoatCOD (maDoiSoat, ngayDoiSoat, tongTien, chenhLech, trangThai, nguoiDoiSoat, thoiGian)
VALUES ('DS0000001', '2026-09-15', 450000, 50000, 'Có sai lệch', 'NV0000011', '2026-09-15 19:00:00');
INSERT INTO ChiTietDoiSoat (maDoiSoat, maGiaoDich) VALUES ('DS0000001', 'GD0000001');

-- ===== V6__bo_sung_nghiep_vu.sql =====
-- =====================================================================
-- V6 — Bảng kỹ thuật bổ sung cho hành vi tài liệu bắt buộc nhưng không định nghĩa bảng
-- (cùng nguyên tắc với 4 bảng kỹ thuật [G16] của V1: ChiPhiLuuKho, BienBanSuCo,
--  MinhChungGiaoHang, LichSuDonHang).
-- G20  MocHanhTrinh: đặc tả 3.3.2 "Cập nhật trạng thái thực tế: báo cáo mốc quan trọng
--      theo thời gian thực" và 3.3.1 "Theo dõi thực tế và cập nhật trạng thái đa chiều"
--      yêu cầu lưu vết từng mốc hành trình; Chương 3/4 không định nghĩa bảng nào chứa mốc.
-- G21  YeuCauCapNhatHoSo: đặc tả 3.4.2 vai trò 3 "gửi yêu cầu cập nhật thông tin khi có
--      thay đổi" — yêu cầu phải tồn tại để nhân sự xử lý; Chương 3/4 không có bảng tương ứng.
-- =====================================================================

CREATE TABLE MocHanhTrinh (
  maMoc        INT AUTO_INCREMENT NOT NULL,
  maChuyen     INT          NOT NULL,
  maDonHang    VARCHAR(10)  NULL,
  diem         VARCHAR(255) NOT NULL COMMENT 'Đã lấy hàng / Đang trung chuyển / Đến điểm giao…',
  moTa         VARCHAR(500) NULL,
  thoiGian     DATETIME     NOT NULL,
  maNVCapNhat  VARCHAR(20)  NOT NULL,
  PRIMARY KEY (maMoc),
  CONSTRAINT fk_moc_cv FOREIGN KEY (maChuyen)    REFERENCES ChuyenVan (MaChuyen),
  CONSTRAINT fk_moc_dh FOREIGN KEY (maDonHang)   REFERENCES DonHang (maDonHang),
  CONSTRAINT fk_moc_nv FOREIGN KEY (maNVCapNhat) REFERENCES NhanVien (maNV)
) ENGINE=InnoDB;
CREATE INDEX idx_moc_chuyen ON MocHanhTrinh (maChuyen, thoiGian);

CREATE TABLE YeuCauCapNhatHoSo (
  maYeuCau     INT AUTO_INCREMENT NOT NULL,
  maNV         VARCHAR(20)  NOT NULL COMMENT 'Người gửi yêu cầu',
  noiDung      VARCHAR(500) NOT NULL,
  trangThai    VARCHAR(20)  NOT NULL COMMENT 'Chờ xử lý / Đã xử lý / Từ chối',
  thoiGianGui  DATETIME     NOT NULL,
  nguoiXuLy    VARCHAR(20)  NULL,
  thoiGianXuLy DATETIME     NULL,
  phanHoi      VARCHAR(300) NULL,
  PRIMARY KEY (maYeuCau),
  CONSTRAINT fk_ycch_nv FOREIGN KEY (maNV)      REFERENCES NhanVien (maNV),
  CONSTRAINT fk_ycch_xl FOREIGN KEY (nguoiXuLy) REFERENCES NhanVien (maNV)
) ENGINE=InnoDB;
CREATE INDEX idx_ycch_trangthai ON YeuCauCapNhatHoSo (trangThai, thoiGianGui);

-- ===== V7__su_co_xu_ly.sql =====
-- =====================================================================
-- V7 — Bổ sung phương án xử lý cho sự cố vận tải (HT3)
-- G22  Đặc tả 3.3.2 vai trò nhân viên điều phối: "Cập nhật và xử lý sự cố vận tải: ghi nhận, xử lý
--      tình huống phát sinh; điều động xe thay thế hoặc đổi lộ trình kịp thời" — điều phối phải cập
--      nhật được trạng thái và phương án xử lý vào chính sự cố tài xế đã báo, để tài xế đọc lại
--      (3.3.2 vai trò tài xế: "yêu cầu hỗ trợ/phê duyệt phương án thay thế").
--      Lược đồ 3.3.4 chỉ có TrangThai nên bổ sung ba cột lưu vết việc xử lý.
-- =====================================================================

ALTER TABLE SuCoVanTai
  ADD COLUMN HuongXuLy    VARCHAR(500) NULL COMMENT 'Phương án xử lý điều phối gửi cho tài xế',
  ADD COLUMN NguoiXuLy    VARCHAR(20)  NULL,
  ADD COLUMN ThoiGianXuLy DATETIME     NULL,
  ADD CONSTRAINT fk_scv_xuly FOREIGN KEY (NguoiXuLy) REFERENCES NhanVien (maNV);

-- ===== V8__dong_bo_trang_thai_chuyen.sql =====
-- [G23] Dọn hệ quả của lỗi "một đơn lập được nhiều chuyến" (Phòng vận tải – giao nhận, mục 1.1).
-- Trước bản vá, lập kế hoạch không đánh dấu đơn đã có chuyến nên cùng một đơn sinh ra nhiều chuyến
-- với nhiều tài xế; chuyến thừa treo ở "Nháp"/"Cần điều chỉnh"/"Đang giao" dù đơn đã giao xong,
-- khiến "Nhiệm vụ của tôi" và "Lịch sử cá nhân" hiển thị sai trạng thái.

-- 1. Chuyến đang giao mà mọi đơn đã giao/đối soát thì chốt "Hoàn thành" và ghi thời gian thực tế.
UPDATE ChuyenVan c
SET c.TrangThai = 'Hoàn thành',
    c.ThoiGianThucTe = COALESCE(c.ThoiGianThucTe, NOW())
WHERE c.TrangThai = 'Đang giao'
  AND NOT EXISTS (
    SELECT 1 FROM ChiTietChuyenHang ct JOIN DonHang d ON d.MaDonHang = ct.MaDonHang
    WHERE ct.MaChuyen = c.MaChuyen AND d.TrangThai NOT IN ('Đã giao', 'Đã đối soát', 'Đã hủy'))
  AND EXISTS (SELECT 1 FROM ChiTietChuyenHang ct WHERE ct.MaChuyen = c.MaChuyen);

-- 2. Chuyến chưa khởi hành mà đơn đã được chuyến khác giao xong là chuyến lập thừa -> huỷ.
UPDATE ChuyenVan c
SET c.TrangThai = 'Đã hủy'
WHERE c.TrangThai IN ('Nháp', 'Cần điều chỉnh', 'Đã phân công', 'Đã phê duyệt')
  AND NOT EXISTS (
    SELECT 1 FROM ChiTietChuyenHang ct JOIN DonHang d ON d.MaDonHang = ct.MaDonHang
    WHERE ct.MaChuyen = c.MaChuyen AND d.TrangThai NOT IN ('Đã giao', 'Đã đối soát', 'Đã hủy'))
  AND EXISTS (SELECT 1 FROM ChiTietChuyenHang ct WHERE ct.MaChuyen = c.MaChuyen);

-- 3. Cùng một đơn còn nằm ở nhiều chuyến còn hiệu lực: giữ chuyến tiến xa nhất (hoà thì giữ mã nhỏ
--    nhất), huỷ các chuyến còn lại để từ nay mỗi đơn chỉ còn đúng một kế hoạch.
UPDATE ChuyenVan c
JOIN ChiTietChuyenHang ct ON ct.MaChuyen = c.MaChuyen
JOIN (
  SELECT ct2.MaDonHang,
         CAST(SUBSTRING_INDEX(GROUP_CONCAT(c2.MaChuyen ORDER BY
           FIELD(c2.TrangThai, 'Đang giao', 'Đã phê duyệt', 'Đã phân công', 'Cần điều chỉnh', 'Nháp'),
           c2.MaChuyen), ',', 1) AS UNSIGNED) AS GiuLai
  FROM ChiTietChuyenHang ct2 JOIN ChuyenVan c2 ON c2.MaChuyen = ct2.MaChuyen
  WHERE c2.TrangThai IN ('Nháp', 'Cần điều chỉnh', 'Đã phân công', 'Đã phê duyệt', 'Đang giao')
  GROUP BY ct2.MaDonHang
  HAVING COUNT(DISTINCT c2.MaChuyen) > 1
) k ON k.MaDonHang = ct.MaDonHang
SET c.TrangThai = 'Đã hủy'
WHERE c.TrangThai IN ('Nháp', 'Cần điều chỉnh', 'Đã phân công', 'Đã phê duyệt', 'Đang giao')
  AND c.MaChuyen <> k.GiuLai;

-- ===== V9__chuan_hoa_trang_thai_chuyen.sql =====
-- [G24] Chuẩn hoá trạng thái chuyến bị lệch chữ giữa dữ liệu mẫu và mã nguồn.
-- V5 gieo 'Chờ điều chỉnh' trong khi Ht3Service.yeuCauDieuChinh ghi 'Cần điều chỉnh', nên chuyến
-- CV0000001 không lọt vào bất kỳ bộ lọc nào: không sửa, không xoá, không phân công, không duyệt được
-- mà vẫn hiện ở "Nhiệm vụ của tôi" (Phòng vận tải – giao nhận, mục 2.3).
UPDATE ChuyenVan SET TrangThai = 'Cần điều chỉnh' WHERE TrangThai = 'Chờ điều chỉnh';

-- Chạy lại bước dọn của V8 cho những dòng vừa được chuẩn hoá: chuyến chưa khởi hành mà đơn đã được
-- chuyến khác giao xong là chuyến lập thừa.
UPDATE ChuyenVan c
SET c.TrangThai = 'Đã hủy'
WHERE c.TrangThai IN ('Nháp', 'Cần điều chỉnh', 'Đã phân công', 'Đã phê duyệt')
  AND NOT EXISTS (
    SELECT 1 FROM ChiTietChuyenHang ct JOIN DonHang d ON d.MaDonHang = ct.MaDonHang
    WHERE ct.MaChuyen = c.MaChuyen AND d.TrangThai NOT IN ('Đã giao', 'Đã đối soát', 'Đã hủy'))
  AND EXISTS (SELECT 1 FROM ChiTietChuyenHang ct WHERE ct.MaChuyen = c.MaChuyen);

-- ===== V10__ht5_theo_usecase.sql =====
-- [G25] Bổ sung cột cho hai usecase HT5 bị thiếu so với biểu đồ usecase 3.5.3 của đặc tả.

-- Usecase "Xác nhận thu tiền" / "Xác nhận chi trả COD" (Thủ quỹ): quy trình 3.5.1 bước 4 nói phiếu
-- sau khi duyệt "chuyển cho thủ quỹ xác nhận thực tế", bước 5 mới ghi sổ quỹ và công nợ. Trước đây
-- bước phê duyệt của kế toán trưởng ghi sổ luôn nên vai trò Thủ quỹ không có gì để làm.
ALTER TABLE PhieuThuChi
  ADD COLUMN nguoiXacNhan    VARCHAR(20) NULL AFTER nguoiDuyet,
  ADD COLUMN thoiGianXacNhan DATETIME    NULL AFTER nguoiXacNhan,
  ADD CONSTRAINT fk_ptc_xacnhan FOREIGN KEY (nguoiXacNhan) REFERENCES NhanVien (maNV);

-- Usecase "Quản lý sai lệch COD" (Kế toán viên): quy trình 3.5.1 bước 2 — "các giao dịch sai lệch
-- được đánh dấu để kiểm tra"; 3.5.2 vai trò kế toán viên "ghi nhận sai lệch".
ALTER TABLE GiaoDichCOD
  ADD COLUMN lyDoSaiLech         VARCHAR(255) NULL AFTER nguoiCapNhat,
  ADD COLUMN nguoiXuLySaiLech    VARCHAR(20)  NULL AFTER lyDoSaiLech,
  ADD COLUMN thoiGianXuLySaiLech DATETIME     NULL AFTER nguoiXuLySaiLech,
  ADD CONSTRAINT fk_gd_sailech FOREIGN KEY (nguoiXuLySaiLech) REFERENCES NhanVien (maNV);

-- Phiếu đã duyệt trước bản vá coi như thủ quỹ đã thực hiện (sổ quỹ đã ghi ở bước duyệt cũ).
UPDATE PhieuThuChi SET TrangThai = 'Đã thực hiện', nguoiXacNhan = nguoiDuyet, thoiGianXacNhan = NOW()
WHERE TrangThai = 'Đã duyệt';

-- ===== V11__phe_duyet_phieu_xuat.sql =====
-- [G26] Bổ sung người duyệt cho phiếu xuất kho.
-- Đặc tả 3.2.2 giao cho Người quản lý kho usecase "Quản lý phiếu nhập/ xuất (kiểm tra & phê duyệt)"
-- và biểu đồ usecase 3.2.3 có "Quản lí phiếu nhập_xuất", nhưng bản cài đặt cũ chỉ phê duyệt phiếu
-- nhập; phiếu xuất do nhân viên kho tự xác nhận và trừ tồn, không ai kiểm soát.
ALTER TABLE PHIEU_XUAT
  ADD COLUMN nguoiDuyet VARCHAR(20) NULL AFTER trangThai,
  ADD CONSTRAINT fk_px_duyet FOREIGN KEY (nguoiDuyet) REFERENCES NhanVien (maNV);

-- Phiếu xuất đã trừ tồn ở luồng cũ coi như quản lý kho đã duyệt (ghi theo người lập phiếu).
UPDATE PHIEU_XUAT SET nguoiDuyet = maNV WHERE trangThai = 'Đã xuất';

-- ===== V12__kiem_ke_don_hang.sql =====
-- Kho lưu nguyên đơn hàng. Giữ nguyên bảng kiểm kê sản phẩm cũ để bảo toàn lịch sử.
CREATE TABLE KIEM_KE_DON_HANG (
  maPhieuKiemKe VARCHAR(20) NOT NULL,
  maDonHang VARCHAR(10) NOT NULL,
  soLuongHeThong INT NOT NULL,
  soLuongThucTe INT NOT NULL,
  chenhLech INT NOT NULL,
  ghiChu VARCHAR(255) NULL,
  PRIMARY KEY (maPhieuKiemKe, maDonHang),
  CONSTRAINT fk_kkdh_phieu FOREIGN KEY (maPhieuKiemKe) REFERENCES PHIEU_KIEM_KE(maPhieuKiemKe),
  CONSTRAINT fk_kkdh_don FOREIGN KEY (maDonHang) REFERENCES DonHang(maDonHang),
  CONSTRAINT chk_kkdh_ht CHECK (soLuongHeThong IN (0, 1)),
  CONSTRAINT chk_kkdh_tt CHECK (soLuongThucTe IN (0, 1)),
  CONSTRAINT chk_kkdh_lech CHECK (chenhLech = soLuongThucTe - soLuongHeThong)
) ENGINE=InnoDB;

-- DỮ LIỆU BỔ SUNG: chỉ INSERT/UPDATE, không thay đổi cấu trúc của migration.
-- Giữ toàn bộ bản ghi nguồn, bổ sung một chu trình kho/thu chi để phủ các bảng còn trống.
START TRANSACTION;
INSERT INTO PHIEU_NHAP (maPhieuNhap,maDonHang,maNCC,maNV,ngayNhap,trangThai,tongTien,ghiChu,nguoiDuyet)
VALUES ('PN0000002',NULL,'NCC-DEMO','NV0000003','2026-09-16','Đã duyệt',950000,'Nhập dự trữ demo: 10 SP001','NV0000004');
INSERT INTO CHI_TIET_PHIEU_NHAP (maPhieuNhap,maSP,soLuong,donGia,thanhTien,tinhTrangHang)
VALUES ('PN0000002','SP001',10,95000,950000,'Đạt');
INSERT INTO PHIEU_XUAT (maPhieuXuat,maNV,maDonHang,ngayXuat,lyDoXuat,trangThai,nguoiDuyet,tongTien,ghiChu)
VALUES ('PX0000001','NV0000003',NULL,'2026-09-17','Xuất mẫu demo','Đã xuất','NV0000004',190000,'Xuất 2 SP001 từ lô dự trữ');
INSERT INTO CHI_TIET_PHIEU_XUAT (maPhieuXuat,maSP,soLuong,donGia,thanhTien)
VALUES ('PX0000001','SP001',2,95000,190000);
INSERT INTO TON_KHO (maTonKho,maSP,maViTri,soLuongTon,ngayCapNhat)
VALUES ('TK0000001','SP001','A-01',8,'2026-09-17 10:00:00');
INSERT INTO PHIEU_KIEM_KE (maPhieuKiemKe,ngayKiemKe,khuVucKiemKe,maNV,trangThai,ghiChu)
VALUES ('KK0000001','2026-09-17','A-01','NV0000003','Đã kiểm kê','Đối chiếu lô dự trữ sau xuất');
INSERT INTO CHI_TIET_KIEM_KE (maPhieuKiemKe,maSP,soLuongHeThong,soLuongThucTe,chenhLech,ghiChu)
VALUES ('KK0000001','SP001',8,8,0,'Khớp tồn');
INSERT INTO ChiPhiLuuKho (maChiPhi,maPhieuNhap,soTien,ngayGhi,maNVGhi)
VALUES ('CP0000001','PN0000002',20000,'2026-09-17 10:00:00','NV0000011');
INSERT INTO BienBanSuCo (maBienBan,maPhieuNhap,maDonHang,loaiSuCo,moTa,duongDanAnh,maNVLap,ngayLap)
VALUES ('BB0000001','PN0000001','DH0000001','Hư hỏng','Bao bì rách: chờ kiểm tra trước phê duyệt',NULL,'NV0000003','2026-09-14 11:00:00');
INSERT INTO YeuCauCapNhatHoSo (maNV,noiDung,trangThai,thoiGianGui,nguoiXuLy,thoiGianXuLy,phanHoi)
VALUES ('NV0000014','Đề nghị cập nhật địa chỉ liên hệ','Chờ xử lý','2026-09-18 08:00:00',NULL,NULL,NULL),
('NV0000016','Kiểm tra lại số điện thoại hồ sơ','Đã xử lý','2026-09-17 08:00:00','NV0000009','2026-09-17 09:00:00','Đã xác minh thông tin hồ sơ hiện tại chính xác');
-- Luồng tài chính độc lập: thu 100.000, chi trả 60.000, quỹ và công nợ còn 40.000.
INSERT INTO DonHang (maDonHang,maKhachHang,maDichVu,nguoiGui,nguoiNhan,sdtNguoiGui,sdtNguoiNhan,diachiLayHang,diachiGiaoHang,khoiLuong,tienCOD,phiVanChuyen,trangThai,ngayTao)
VALUES ('DH0000004','KH0000003','DV01','Shop MiSa','Khách demo','0908123456','0901112223','78 Lê Văn Sỹ, TP.HCM','25 Nguyễn Huệ, TP.HCM',1,100000,25000,'Đã đối soát','2026-09-16 08:00:00');
INSERT INTO HangHoa (maHangHoa,maDonHang,maSP,loaiHangHoa,soLuong,trongLuong)
VALUES ('HH0000004','DH0000004','SP006','Áo thun',1,1);
INSERT INTO PHIEU_NHAP (maPhieuNhap,maDonHang,maNV,ngayNhap,trangThai,tongTien,nguoiDuyet)
VALUES ('PN0000003','DH0000004','NV0000003','2026-09-16','Đã duyệt',120000,'NV0000004');
INSERT INTO CHI_TIET_PHIEU_NHAP VALUES ('PN0000003','SP006',1,120000,120000,'Đạt');
INSERT INTO PHIEU_XUAT (maPhieuXuat,maNV,maDonHang,ngayXuat,lyDoXuat,trangThai,nguoiDuyet,tongTien)
VALUES ('PX0000002','NV0000003','DH0000004','2026-09-16','Giao hàng','Đã xuất','NV0000004',120000);
INSERT INTO CHI_TIET_PHIEU_XUAT VALUES ('PX0000002','SP006',1,120000,120000);
INSERT INTO TON_KHO VALUES ('TK0000002','SP006','B-01',0,'2026-09-16 10:00:00');
INSERT INTO ChuyenVan (MaChuyen,MaNguoiLap,MaTaiXe,MaPhuongTien,MaDonHang,NgayKhoiHanh,ThoiGianDuKien,ThoiGianThucTe,TrangThai)
VALUES (2,'NV0000005','TX0000003',5,'DH0000004','2026-09-16 10:00:00','2026-09-16 12:00:00','2026-09-16 11:00:00','Hoàn thành');
INSERT INTO ChiTietChuyenHang VALUES (2,'DH0000004');
INSERT INTO LoTrinh (MaChuyen,DiemXuatPhat,DiemKetThuc,KhoangCach,ThoiGianDuKien,MoTa)
VALUES (2,'Kho Hoàng Minh','25 Nguyễn Huệ, TP.HCM',8,120,'Tuyến giao nội thành demo');
INSERT INTO MocHanhTrinh (maChuyen,maDonHang,diem,moTa,thoiGian,maNVCapNhat)
VALUES (2,'DH0000004','Đã lấy hàng','Đã nhận đủ kiện','2026-09-16 10:00:00','NV0000016'),
(2,'DH0000004','Đến điểm giao','Người nhận đã nhận hàng','2026-09-16 11:00:00','NV0000016');
INSERT INTO MinhChungGiaoHang VALUES ('MC0000002','DH0000004','ChuKy','demo/chu-ky-dh0000004.png','NV0000016','2026-09-16 11:00:00');
INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
('DH0000004','Tao','Đã tạo','TA0000001','2026-09-16 08:00:00','Demo hoàn chỉnh'),
('DH0000004','GuiDuyet','Đã tạo','TA0000001','2026-09-16 08:10:00',NULL),
('DH0000004','Duyet','Đã tạo','TA0000002','2026-09-16 08:20:00',NULL),
('DH0000004','DoiTrangThai','Đã nhập kho','TA0000004','2026-09-16 09:00:00',NULL),
('DH0000004','DoiTrangThai','Đang giao','TA0000016','2026-09-16 10:00:00',NULL),
('DH0000004','DoiTrangThai','Đã giao','TA0000016','2026-09-16 11:00:00',NULL),
('DH0000004','DoiTrangThai','Đã đối soát','TA0000011','2026-09-16 15:00:00','DS0000002');
INSERT INTO GiaoDichCOD (maGiaoDich,maDonHang,maCOD,soTienPhaiThu,soTienThucThu,trangThai,ngayTao,thoiGianCapNhat,nguoiCapNhat)
VALUES ('GD0000002','DH0000004','COD02',100000,100000,'Đã đối soát','2026-09-16','2026-09-16 15:00:00','NV0000011');
INSERT INTO DoiSoatCOD VALUES ('DS0000002','2026-09-16',100000,0,'Khớp','NV0000011','2026-09-16 15:00:00');
INSERT INTO ChiTietDoiSoat VALUES ('DS0000002','GD0000002');
INSERT INTO PhieuThuChi (maPhieu,loaiPhieu,maGiaoDich,soTien,ngayLap,noiDung,trangThai,nguoiLap,nguoiDuyet,nguoiXacNhan,thoiGianXacNhan) VALUES
('PT0000002','Thu','GD0000002',100000,'2026-09-16','Thu COD demo','Đã thực hiện','NV0000011','NV0000013','NV0000012','2026-09-16 16:00:00'),
('PC0000001','Chi','GD0000002',60000,'2026-09-16','Chi trả một phần COD demo','Đã thực hiện','NV0000011','NV0000013','NV0000012','2026-09-16 17:00:00');
INSERT INTO SoQuy VALUES ('SQ0000001','PT0000002',100000,0,'2026-09-16','NV0000012'),('SQ0000002','PC0000001',0,60000,'2026-09-16','NV0000012');
INSERT INTO CongNo (maCongNo,maKhachHang,soTienPhaiTra,soTienDaTra,trangThai,ngayCapNhat)
VALUES ('CN0000001','KH0000003',100000,60000,'Còn nợ','2026-09-16 17:00:00');
COMMIT;

-- Kiểm kê nguyên đơn: đơn DH0000004 đã hoàn tất nên không còn nằm trong kho.
INSERT INTO PHIEU_KIEM_KE (maPhieuKiemKe,ngayKiemKe,khuVucKiemKe,maNV,trangThai,ghiChu)
VALUES ('KK0000002','2026-09-18','Kho Hoàng Minh','NV0000003','Đã chốt','Kiểm tra đơn đã bàn giao');
INSERT INTO KIEM_KE_DON_HANG VALUES ('KK0000002','DH0000004',0,0,0,'Đơn đã giao, không còn lưu kho');

-- Kiểm đếm: mỗi bảng nghiệp vụ phải có ít nhất một bản ghi.
SELECT 'PhongBan' AS tenBang, COUNT(*) AS soBanGhi FROM `PhongBan`
UNION ALL
SELECT 'ChucVu' AS tenBang, COUNT(*) AS soBanGhi FROM `ChucVu`
UNION ALL
SELECT 'TrangThaiNhanVien' AS tenBang, COUNT(*) AS soBanGhi FROM `TrangThaiNhanVien`
UNION ALL
SELECT 'NhanVien' AS tenBang, COUNT(*) AS soBanGhi FROM `NhanVien`
UNION ALL
SELECT 'TaiKhoan' AS tenBang, COUNT(*) AS soBanGhi FROM `TaiKhoan`
UNION ALL
SELECT 'TaiXe' AS tenBang, COUNT(*) AS soBanGhi FROM `TaiXe`
UNION ALL
SELECT 'KhachHang' AS tenBang, COUNT(*) AS soBanGhi FROM `KhachHang`
UNION ALL
SELECT 'DichVu' AS tenBang, COUNT(*) AS soBanGhi FROM `DichVu`
UNION ALL
SELECT 'DonHang' AS tenBang, COUNT(*) AS soBanGhi FROM `DonHang`
UNION ALL
SELECT 'SanPham' AS tenBang, COUNT(*) AS soBanGhi FROM `SanPham`
UNION ALL
SELECT 'HangHoa' AS tenBang, COUNT(*) AS soBanGhi FROM `HangHoa`
UNION ALL
SELECT 'PHIEU_NHAP' AS tenBang, COUNT(*) AS soBanGhi FROM `PHIEU_NHAP`
UNION ALL
SELECT 'CHI_TIET_PHIEU_NHAP' AS tenBang, COUNT(*) AS soBanGhi FROM `CHI_TIET_PHIEU_NHAP`
UNION ALL
SELECT 'PHIEU_XUAT' AS tenBang, COUNT(*) AS soBanGhi FROM `PHIEU_XUAT`
UNION ALL
SELECT 'CHI_TIET_PHIEU_XUAT' AS tenBang, COUNT(*) AS soBanGhi FROM `CHI_TIET_PHIEU_XUAT`
UNION ALL
SELECT 'PHIEU_KIEM_KE' AS tenBang, COUNT(*) AS soBanGhi FROM `PHIEU_KIEM_KE`
UNION ALL
SELECT 'CHI_TIET_KIEM_KE' AS tenBang, COUNT(*) AS soBanGhi FROM `CHI_TIET_KIEM_KE`
UNION ALL
SELECT 'TON_KHO' AS tenBang, COUNT(*) AS soBanGhi FROM `TON_KHO`
UNION ALL
SELECT 'ChiPhiLuuKho' AS tenBang, COUNT(*) AS soBanGhi FROM `ChiPhiLuuKho`
UNION ALL
SELECT 'BienBanSuCo' AS tenBang, COUNT(*) AS soBanGhi FROM `BienBanSuCo`
UNION ALL
SELECT 'PhuongTien' AS tenBang, COUNT(*) AS soBanGhi FROM `PhuongTien`
UNION ALL
SELECT 'ChuyenVan' AS tenBang, COUNT(*) AS soBanGhi FROM `ChuyenVan`
UNION ALL
SELECT 'ChiTietChuyenHang' AS tenBang, COUNT(*) AS soBanGhi FROM `ChiTietChuyenHang`
UNION ALL
SELECT 'LoTrinh' AS tenBang, COUNT(*) AS soBanGhi FROM `LoTrinh`
UNION ALL
SELECT 'SuCoVanTai' AS tenBang, COUNT(*) AS soBanGhi FROM `SuCoVanTai`
UNION ALL
SELECT 'MinhChungGiaoHang' AS tenBang, COUNT(*) AS soBanGhi FROM `MinhChungGiaoHang`
UNION ALL
SELECT 'LichSuDonHang' AS tenBang, COUNT(*) AS soBanGhi FROM `LichSuDonHang`
UNION ALL
SELECT 'GiaoDichCOD' AS tenBang, COUNT(*) AS soBanGhi FROM `GiaoDichCOD`
UNION ALL
SELECT 'PhieuThuChi' AS tenBang, COUNT(*) AS soBanGhi FROM `PhieuThuChi`
UNION ALL
SELECT 'SoQuy' AS tenBang, COUNT(*) AS soBanGhi FROM `SoQuy`
UNION ALL
SELECT 'CongNo' AS tenBang, COUNT(*) AS soBanGhi FROM `CongNo`
UNION ALL
SELECT 'DoiSoatCOD' AS tenBang, COUNT(*) AS soBanGhi FROM `DoiSoatCOD`
UNION ALL
SELECT 'ChiTietDoiSoat' AS tenBang, COUNT(*) AS soBanGhi FROM `ChiTietDoiSoat`
UNION ALL
SELECT 'MocHanhTrinh' AS tenBang, COUNT(*) AS soBanGhi FROM `MocHanhTrinh`
UNION ALL
SELECT 'YeuCauCapNhatHoSo' AS tenBang, COUNT(*) AS soBanGhi FROM `YeuCauCapNhatHoSo`
UNION ALL
SELECT 'KIEM_KE_DON_HANG' AS tenBang, COUNT(*) AS soBanGhi FROM `KIEM_KE_DON_HANG`;
SELECT * FROM vw_SoDuQuy;
SELECT * FROM vw_TonKhoChiTiet;
SELECT * FROM vw_CongNoConLai;
SET SESSION SQL_SAFE_UPDATES = @erp_previous_safe_updates;
