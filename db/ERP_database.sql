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

CREATE DATABASE IF NOT EXISTS erp_hoangminh
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE erp_hoangminh;

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