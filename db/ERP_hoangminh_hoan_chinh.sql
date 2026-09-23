-- ERP Hoàng Minh: CSDL hoàn chỉnh, dữ liệu demo theo workflow đơn hàng.
-- Cấu trúc: toàn bộ CREATE/ALTER của V1–V12; không sửa migration gốc.
-- Dữ liệu: danh mục V2 và kịch bản mới, không trộn các seed thử nghiệm V3–V5.
-- Chỉ chạy MỘT LẦN trên DB MỚI. Không chạy lên DB cloud đang có dữ liệu.
-- Mật khẩu tài khoản demo: 123456. Ảnh/chữ ký demo chỉ là đường dẫn tham chiếu.
-- Backend đang tắt Flyway. Nếu bật cho DB nhập sẵn này, baseline-on-migrate=true, baseline-version=12.
SET NAMES utf8mb4;
CREATE DATABASE erp_hoangminh_full_demo CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE erp_hoangminh_full_demo;
-- V1__schema.sql
CREATE TABLE PhongBan (
  maPhongBan  VARCHAR(20)  NOT NULL,
  tenPhongBan VARCHAR(100) NOT NULL,
  PRIMARY KEY (maPhongBan)
) ENGINE=InnoDB;

-- V1__schema.sql
CREATE TABLE ChucVu (
  maChucVu  VARCHAR(20)  NOT NULL,
  tenChucVu VARCHAR(100) NOT NULL,
  PRIMARY KEY (maChucVu)
) ENGINE=InnoDB;

-- V1__schema.sql
CREATE TABLE TrangThaiNhanVien (
  maTrangThai  VARCHAR(20) NOT NULL,
  tenTrangThai VARCHAR(50) NOT NULL,
  PRIMARY KEY (maTrangThai)
) ENGINE=InnoDB;

-- V1__schema.sql
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
  ngayVaoLam   DATE         NULL,               
  PRIMARY KEY (maNV),
  CONSTRAINT fk_nv_phongban  FOREIGN KEY (maPhongBan)  REFERENCES PhongBan (maPhongBan),
  CONSTRAINT fk_nv_chucvu    FOREIGN KEY (maChucVu)    REFERENCES ChucVu (maChucVu),
  CONSTRAINT fk_nv_trangthai FOREIGN KEY (maTrangThai) REFERENCES TrangThaiNhanVien (maTrangThai)
) ENGINE=InnoDB;

-- V1__schema.sql
CREATE INDEX idx_nv_hoten ON NhanVien (hoTen);

-- V1__schema.sql
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

-- V1__schema.sql
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

-- V1__schema.sql
CREATE TABLE KhachHang (
  maKhachHang  VARCHAR(20)  NOT NULL,
  tenKhachHang VARCHAR(100) NOT NULL,
  soDienThoai  VARCHAR(10)  NULL,
  diachi       VARCHAR(255) NULL,
  email        VARCHAR(100) NULL,   
  loaiKH       VARCHAR(30)  NULL,   
  PRIMARY KEY (maKhachHang)
) ENGINE=InnoDB;

-- V1__schema.sql
CREATE INDEX idx_kh_ten  ON KhachHang (tenKhachHang);

-- V1__schema.sql
CREATE INDEX idx_kh_sdt  ON KhachHang (soDienThoai);

-- V1__schema.sql
CREATE TABLE DichVu (
  maDichVu  VARCHAR(20)   NOT NULL,
  tenDichVu VARCHAR(100)  NOT NULL,
  donGia    DECIMAL(15,2) NULL,     
  moTa      VARCHAR(255)  NULL,     
  PRIMARY KEY (maDichVu)
) ENGINE=InnoDB;

-- V1__schema.sql
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

-- V1__schema.sql
CREATE INDEX idx_dh_trangthai ON DonHang (trangThai);

-- V1__schema.sql
CREATE INDEX idx_dh_ngaytao   ON DonHang (ngayTao);

-- V1__schema.sql
CREATE TABLE SanPham (
  maSP              VARCHAR(20)   NOT NULL,
  tenSP             VARCHAR(150)  NOT NULL,
  donViTinh         VARCHAR(50)   NULL,
  gia               DECIMAL(15,2) NULL,
  trangThai         VARCHAR(30)   NULL,
  dieuKienBaoQuan   VARCHAR(255)  NULL,   
  PRIMARY KEY (maSP)
) ENGINE=InnoDB;

-- V1__schema.sql
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

-- V1__schema.sql
CREATE TABLE PHIEU_NHAP (
  maPhieuNhap VARCHAR(20)   NOT NULL,
  maDonHang   VARCHAR(10)   NULL,     
  maNCC       VARCHAR(20)   NULL,     
  maNV        VARCHAR(20)   NOT NULL,
  ngayNhap    DATE          NOT NULL,
  trangThai   VARCHAR(30)   NOT NULL,
  tongTien    DECIMAL(15,2) NULL,
  ghiChu      VARCHAR(255)  NULL,
  nguoiDuyet  VARCHAR(20)   NULL,     
  PRIMARY KEY (maPhieuNhap),
  CONSTRAINT fk_pn_dh  FOREIGN KEY (maDonHang)  REFERENCES DonHang (maDonHang),
  CONSTRAINT fk_pn_nv  FOREIGN KEY (maNV)       REFERENCES NhanVien (maNV),
  CONSTRAINT fk_pn_duyet FOREIGN KEY (nguoiDuyet) REFERENCES NhanVien (maNV)
) ENGINE=InnoDB;

-- V1__schema.sql
CREATE INDEX idx_pn_ngaynhap ON PHIEU_NHAP (ngayNhap);

-- V1__schema.sql
CREATE TABLE CHI_TIET_PHIEU_NHAP (
  maPhieuNhap   VARCHAR(20)   NOT NULL,
  maSP          VARCHAR(20)   NOT NULL,
  soLuong       INT           NOT NULL,
  donGia        DECIMAL(15,2) NULL,
  thanhTien     DECIMAL(15,2) NULL,
  tinhTrangHang VARCHAR(50)   NULL,   
  PRIMARY KEY (maPhieuNhap, maSP),
  CONSTRAINT fk_ctpn_pn FOREIGN KEY (maPhieuNhap) REFERENCES PHIEU_NHAP (maPhieuNhap),
  CONSTRAINT fk_ctpn_sp FOREIGN KEY (maSP)        REFERENCES SanPham (maSP),
  CONSTRAINT chk_ctpn_sl CHECK (soLuong >= 0)
) ENGINE=InnoDB;

-- V1__schema.sql
CREATE TABLE PHIEU_XUAT (
  maPhieuXuat VARCHAR(20)   NOT NULL,
  maNV        VARCHAR(20)   NOT NULL,
  maDonHang   VARCHAR(10)   NULL,     
  ngayXuat    DATE          NOT NULL,
  lyDoXuat    VARCHAR(255)  NULL,
  trangThai   VARCHAR(30)   NOT NULL,
  tongTien    DECIMAL(15,2) NULL,
  ghiChu      VARCHAR(255)  NULL,
  PRIMARY KEY (maPhieuXuat),
  CONSTRAINT fk_px_nv FOREIGN KEY (maNV)      REFERENCES NhanVien (maNV),
  CONSTRAINT fk_px_dh FOREIGN KEY (maDonHang) REFERENCES DonHang (maDonHang)
) ENGINE=InnoDB;

-- V1__schema.sql
CREATE INDEX idx_px_ngayxuat ON PHIEU_XUAT (ngayXuat);

-- V1__schema.sql
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

-- V1__schema.sql
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

-- V1__schema.sql
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

-- V1__schema.sql
CREATE TABLE TON_KHO (
  maTonKho    VARCHAR(20) NOT NULL,
  maSP        VARCHAR(20) NOT NULL,
  maViTri     VARCHAR(50) NOT NULL DEFAULT '',  
  soLuongTon  INT         NOT NULL DEFAULT 0,
  ngayCapNhat DATETIME    NOT NULL,
  PRIMARY KEY (maTonKho),
  UNIQUE KEY uq_tonkho_sp_vitri (maSP, maViTri),
  CONSTRAINT fk_tk_sp FOREIGN KEY (maSP) REFERENCES SanPham (maSP),
  CONSTRAINT chk_tk_sl CHECK (soLuongTon >= 0)
) ENGINE=InnoDB;

-- V1__schema.sql
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

-- V1__schema.sql
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

-- V1__schema.sql
CREATE TABLE PhuongTien (
  MaPhuongTien INT AUTO_INCREMENT NOT NULL,
  BienSo       VARCHAR(20)   NOT NULL,
  LoaiXe       VARCHAR(50)   NULL,
  TaiTrong     DECIMAL(10,2) NULL,
  TrangThai    VARCHAR(30)   NULL,
  PRIMARY KEY (MaPhuongTien),
  UNIQUE KEY uq_pt_bienso (BienSo)
) ENGINE=InnoDB;

-- V1__schema.sql
CREATE TABLE ChuyenVan (
  MaChuyen        INT AUTO_INCREMENT NOT NULL,
  MaNguoiLap      VARCHAR(20)  NOT NULL,   
  MaTaiXe         VARCHAR(20)  NULL,       
  MaPhuongTien    INT          NULL,
  MaDonHang       VARCHAR(10)  NULL,       
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

-- V1__schema.sql
CREATE INDEX idx_cv_trangthai ON ChuyenVan (TrangThai);

-- V1__schema.sql
CREATE TABLE ChiTietChuyenHang (
  MaChuyen  INT         NOT NULL,
  MaDonHang VARCHAR(10) NOT NULL,
  PRIMARY KEY (MaChuyen, MaDonHang),
  CONSTRAINT fk_ctch_cv FOREIGN KEY (MaChuyen)  REFERENCES ChuyenVan (MaChuyen),
  CONSTRAINT fk_ctch_dh FOREIGN KEY (MaDonHang) REFERENCES DonHang (maDonHang)
) ENGINE=InnoDB;

-- V1__schema.sql
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

-- V1__schema.sql
CREATE TABLE SuCoVanTai (
  MaSuCo    INT AUTO_INCREMENT NOT NULL,
  MaChuyen  INT          NOT NULL,
  MaDonHang VARCHAR(10)  NULL,       
  MaTaiXe   VARCHAR(20)  NULL,       
  LoaiSuCo  VARCHAR(50)  NOT NULL,
  MoTa      TEXT         NULL,
  ThoiGian  DATETIME     NOT NULL,
  TrangThai VARCHAR(30)  NOT NULL,
  HinhAnh   VARCHAR(255) NULL,       
  PRIMARY KEY (MaSuCo),
  CONSTRAINT fk_scv_cv FOREIGN KEY (MaChuyen)  REFERENCES ChuyenVan (MaChuyen),
  CONSTRAINT fk_scv_dh FOREIGN KEY (MaDonHang) REFERENCES DonHang (maDonHang),
  CONSTRAINT fk_scv_tx FOREIGN KEY (MaTaiXe)   REFERENCES TaiXe (maTaiXe)
) ENGINE=InnoDB;

-- V1__schema.sql
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

-- V1__schema.sql
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

-- V1__schema.sql
CREATE INDEX ls_dh_don ON LichSuDonHang (maDonHang, thoiGian);

-- V1__schema.sql
CREATE TABLE GiaoDichCOD (
  maGiaoDich     VARCHAR(10)   NOT NULL,
  maDonHang      VARCHAR(10)   NOT NULL,
  maCOD          VARCHAR(10)   NULL,
  soTienPhaiThu  DECIMAL(15,2) NOT NULL,   
  soTienThucThu  DECIMAL(15,2) NULL,       
  trangThai      VARCHAR(20)   NOT NULL,
  ngayTao        DATE          NOT NULL,
  thoiGianCapNhat DATETIME     NULL,
  nguoiCapNhat   VARCHAR(20)   NULL,
  PRIMARY KEY (maGiaoDich),
  CONSTRAINT fk_gd_dh FOREIGN KEY (maDonHang)    REFERENCES DonHang (maDonHang),
  CONSTRAINT fk_gd_nv FOREIGN KEY (nguoiCapNhat) REFERENCES NhanVien (maNV)
) ENGINE=InnoDB;

-- V1__schema.sql
CREATE INDEX idx_gd_trangthai ON GiaoDichCOD (trangThai);

-- V1__schema.sql
CREATE TABLE PhieuThuChi (
  maPhieu    VARCHAR(10)   NOT NULL,
  loaiPhieu  VARCHAR(30)   NOT NULL COMMENT 'Thu / Chi',
  maGiaoDich VARCHAR(10)   NULL,       
  soTien     DECIMAL(15,2) NOT NULL,
  ngayLap    DATE          NOT NULL,
  noiDung    VARCHAR(100)  NULL,
  trangThai  VARCHAR(20)   NOT NULL,
  nguoiLap   VARCHAR(20)   NOT NULL,   
  nguoiDuyet VARCHAR(20)   NULL,       
  PRIMARY KEY (maPhieu),
  CONSTRAINT fk_ptc_gd  FOREIGN KEY (maGiaoDich) REFERENCES GiaoDichCOD (maGiaoDich),
  CONSTRAINT fk_ptc_lap FOREIGN KEY (nguoiLap)   REFERENCES NhanVien (maNV),
  CONSTRAINT fk_ptc_duyet FOREIGN KEY (nguoiDuyet) REFERENCES NhanVien (maNV),
  CONSTRAINT chk_ptc_tien CHECK (soTien >= 0)
) ENGINE=InnoDB;

-- V1__schema.sql
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

-- V1__schema.sql
CREATE TABLE CongNo (
  maCongNo      VARCHAR(10)   NOT NULL,
  maKhachHang   VARCHAR(20)   NOT NULL,   
  soTienPhaiTra DECIMAL(15,2) NOT NULL DEFAULT 0,
  soTienDaTra   DECIMAL(15,2) NOT NULL DEFAULT 0,
  soDuNo        DECIMAL(15,2) GENERATED ALWAYS AS (soTienPhaiTra - soTienDaTra) STORED,
  trangThai     VARCHAR(20)   NOT NULL,
  ngayCapNhat   DATETIME      NOT NULL,
  PRIMARY KEY (maCongNo),
  CONSTRAINT fk_cn_kh FOREIGN KEY (maKhachHang) REFERENCES KhachHang (maKhachHang)
) ENGINE=InnoDB;

-- V1__schema.sql
CREATE TABLE DoiSoatCOD (
  maDoiSoat   VARCHAR(10)   NOT NULL,
  ngayDoiSoat DATE          NOT NULL,
  tongTien    DECIMAL(15,2) NULL,
  chenhLech   DECIMAL(15,2) NULL,
  trangThai   VARCHAR(20)   NOT NULL,
  nguoiDoiSoat VARCHAR(20)  NOT NULL,   
  thoiGian    DATETIME      NOT NULL,   
  PRIMARY KEY (maDoiSoat),
  CONSTRAINT fk_ds_nv FOREIGN KEY (nguoiDoiSoat) REFERENCES NhanVien (maNV)
) ENGINE=InnoDB;

-- V1__schema.sql
CREATE TABLE ChiTietDoiSoat (
  maDoiSoat  VARCHAR(10) NOT NULL,
  maGiaoDich VARCHAR(10) NOT NULL,
  PRIMARY KEY (maDoiSoat, maGiaoDich),
  CONSTRAINT fk_ctds_ds FOREIGN KEY (maDoiSoat)  REFERENCES DoiSoatCOD (maDoiSoat),
  CONSTRAINT fk_ctds_gd FOREIGN KEY (maGiaoDich) REFERENCES GiaoDichCOD (maGiaoDich)
) ENGINE=InnoDB;

-- V1__schema.sql
CREATE OR REPLACE VIEW vw_SoDuQuy AS
SELECT COALESCE(SUM(soTienThu),0) - COALESCE(SUM(soTienChi),0) AS soDuHienTai
FROM SoQuy;

-- V1__schema.sql
CREATE OR REPLACE VIEW vw_TonKhoChiTiet AS
SELECT t.maTonKho, t.maSP, s.tenSP, s.donViTinh, t.maViTri, t.soLuongTon, t.ngayCapNhat
FROM TON_KHO t JOIN SanPham s ON s.maSP = t.maSP;

-- V1__schema.sql
CREATE OR REPLACE VIEW vw_CongNoConLai AS
SELECT c.maCongNo, c.maKhachHang, k.tenKhachHang,
       c.soTienPhaiTra, c.soTienDaTra, c.soDuNo, c.trangThai, c.ngayCapNhat
FROM CongNo c JOIN KhachHang k ON k.maKhachHang = c.maKhachHang;

-- V6__bo_sung_nghiep_vu.sql
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

-- V6__bo_sung_nghiep_vu.sql
CREATE INDEX idx_moc_chuyen ON MocHanhTrinh (maChuyen, thoiGian);

-- V6__bo_sung_nghiep_vu.sql
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

-- V6__bo_sung_nghiep_vu.sql
CREATE INDEX idx_ycch_trangthai ON YeuCauCapNhatHoSo (trangThai, thoiGianGui);

-- V7__su_co_xu_ly.sql
ALTER TABLE SuCoVanTai
  ADD COLUMN HuongXuLy    VARCHAR(500) NULL COMMENT 'Phương án xử lý điều phối gửi cho tài xế',
  ADD COLUMN NguoiXuLy    VARCHAR(20)  NULL,
  ADD COLUMN ThoiGianXuLy DATETIME     NULL,
  ADD CONSTRAINT fk_scv_xuly FOREIGN KEY (NguoiXuLy) REFERENCES NhanVien (maNV);

-- V10__ht5_theo_usecase.sql
ALTER TABLE PhieuThuChi
  ADD COLUMN nguoiXacNhan    VARCHAR(20) NULL AFTER nguoiDuyet,
  ADD COLUMN thoiGianXacNhan DATETIME    NULL AFTER nguoiXacNhan,
  ADD CONSTRAINT fk_ptc_xacnhan FOREIGN KEY (nguoiXacNhan) REFERENCES NhanVien (maNV);

-- V10__ht5_theo_usecase.sql
ALTER TABLE GiaoDichCOD
  ADD COLUMN lyDoSaiLech         VARCHAR(255) NULL AFTER nguoiCapNhat,
  ADD COLUMN nguoiXuLySaiLech    VARCHAR(20)  NULL AFTER lyDoSaiLech,
  ADD COLUMN thoiGianXuLySaiLech DATETIME     NULL AFTER nguoiXuLySaiLech,
  ADD CONSTRAINT fk_gd_sailech FOREIGN KEY (nguoiXuLySaiLech) REFERENCES NhanVien (maNV);

-- V11__phe_duyet_phieu_xuat.sql
ALTER TABLE PHIEU_XUAT
  ADD COLUMN nguoiDuyet VARCHAR(20) NULL AFTER trangThai,
  ADD CONSTRAINT fk_px_duyet FOREIGN KEY (nguoiDuyet) REFERENCES NhanVien (maNV);

-- V12__kiem_ke_don_hang.sql
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

START TRANSACTION;
-- Danh mục nhân sự, tài khoản, khách hàng, dịch vụ, phương tiện giữ từ V2. Mật khẩu: 123456.

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

INSERT INTO TaiXe (maTaiXe, maNV, soGPLX, loaiGPLX, trangThai) VALUES
  ('TX0000001', 'NV0000007', '0123456789', 'B2', 'Hoạt động'),
  ('TX0000002', 'NV0000008', '0987654321', 'C',  'Hoạt động'),
  ('TX0000003', 'NV0000016', '7900123456', 'A1', 'Hoạt động'),
  ('TX0000004', 'NV0000017', NULL,         NULL, 'Hoạt động');

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

INSERT INTO PhuongTien (MaPhuongTien, BienSo, LoaiXe, TaiTrong, TrangThai) VALUES
  (1, '51C-123.45', 'Xe tải 1 tấn',   1000.00, 'Sẵn sàng'),
  (2, '51D-234.56', 'Xe tải 2.5 tấn', 2500.00, 'Sẵn sàng'),
  (3, '62E-345.67', 'Xe tải 5 tấn',   5000.00, 'Đang bảo trì'),
  (4, '59F-456.78', 'Xe ba gác',       350.00, 'Sẵn sàng'),
  (5, '50A-567.89', 'Xe máy giao hàng', 80.00, 'Sẵn sàng');

-- Danh mục mô tả kiện hàng của khách; không có giá mua/bán hoặc nhà cung cấp.

INSERT INTO SanPham (maSP,tenSP,donViTinh,gia,trangThai,dieuKienBaoQuan) VALUES
  ('SP001','Kiện đồ gia dụng','Kiện',NULL,'Đang sử dụng','Giữ nguyên bao bì, tránh va đập'),
  ('SP002','Kiện quần áo','Kiện',NULL,'Đang sử dụng','Bảo quản khô ráo'),
  ('SP003','Hồ sơ đóng kiện','Kiện',NULL,'Đang sử dụng','Tránh ẩm, giữ niêm phong');

-- DH0000001 — Đơn nháp

INSERT INTO DonHang (maDonHang,maKhachHang,maDichVu,nguoiGui,nguoiNhan,sdtNguoiGui,sdtNguoiNhan,diachiLayHang,diachiGiaoHang,khoiLuong,tienCOD,phiVanChuyen,trangThai,lyDoHuy,ngayTao) VALUES
  ('DH0000001','KH0000001','DV01','Minh Phát','Nguyễn Minh Anh','0908221144','0901110001','120 Nguyễn Văn Trỗi, TP.HCM','10 Lê Lợi, TP.HCM',2,0,25000,'Đã tạo',NULL,'2026-09-23 07:00:00');

INSERT INTO HangHoa (maHangHoa,maDonHang,maSP,loaiHangHoa,soLuong,trongLuong) VALUES
  ('HH0000001','DH0000001','SP001','Kiện đồ gia dụng',1,2);

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000001','Tao','Đã tạo','TA0000001','2026-09-23 07:00:00','Tiếp nhận yêu cầu gửi hàng');

-- DH0000002 — Chờ kinh doanh duyệt

INSERT INTO DonHang (maDonHang,maKhachHang,maDichVu,nguoiGui,nguoiNhan,sdtNguoiGui,sdtNguoiNhan,diachiLayHang,diachiGiaoHang,khoiLuong,tienCOD,phiVanChuyen,trangThai,lyDoHuy,ngayTao) VALUES
  ('DH0000002','KH0000001','DV01','Minh Phát','Trần Đức Bình','0908221144','0901110002','120 Nguyễn Văn Trỗi, TP.HCM','20 Lê Lợi, TP.HCM',3,0,25000,'Đã tạo',NULL,'2026-09-23 07:00:00');

INSERT INTO HangHoa (maHangHoa,maDonHang,maSP,loaiHangHoa,soLuong,trongLuong) VALUES
  ('HH0000002','DH0000002','SP002','Kiện quần áo',2,3);

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000002','Tao','Đã tạo','TA0000001','2026-09-23 07:00:00','Tiếp nhận yêu cầu gửi hàng');

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000002','GuiDuyet','Đã tạo','TA0000001','2026-09-23 07:10:00',NULL);

-- DH0000003 — Cần bổ sung địa chỉ

INSERT INTO DonHang (maDonHang,maKhachHang,maDichVu,nguoiGui,nguoiNhan,sdtNguoiGui,sdtNguoiNhan,diachiLayHang,diachiGiaoHang,khoiLuong,tienCOD,phiVanChuyen,trangThai,lyDoHuy,ngayTao) VALUES
  ('DH0000003','KH0000001','DV01','Minh Phát','Lê Ngọc Chi','0908221144','0901110003','120 Nguyễn Văn Trỗi, TP.HCM','30 Lê Lợi, TP.HCM',1,0,25000,'Đã tạo',NULL,'2026-09-23 07:00:00');

INSERT INTO HangHoa (maHangHoa,maDonHang,maSP,loaiHangHoa,soLuong,trongLuong) VALUES
  ('HH0000003','DH0000003','SP003','Hồ sơ đóng kiện',1,1);

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000003','Tao','Đã tạo','TA0000001','2026-09-23 07:00:00','Tiếp nhận yêu cầu gửi hàng');

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000003','GuiDuyet','Đã tạo','TA0000001','2026-09-23 07:10:00',NULL);

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000003','TuChoi','Đã tạo','TA0000002','2026-09-23 07:20:00','Cần xác nhận số nhà người nhận');

-- DH0000004 — Đã hủy theo yêu cầu khách

INSERT INTO DonHang (maDonHang,maKhachHang,maDichVu,nguoiGui,nguoiNhan,sdtNguoiGui,sdtNguoiNhan,diachiLayHang,diachiGiaoHang,khoiLuong,tienCOD,phiVanChuyen,trangThai,lyDoHuy,ngayTao) VALUES
  ('DH0000004','KH0000001','DV01','Minh Phát','Phạm Văn Dũng','0908221144','0901110004','120 Nguyễn Văn Trỗi, TP.HCM','40 Lê Lợi, TP.HCM',2,0,25000,'Đã hủy','Khách đổi kế hoạch gửi hàng','2026-09-23 07:00:00');

INSERT INTO HangHoa (maHangHoa,maDonHang,maSP,loaiHangHoa,soLuong,trongLuong) VALUES
  ('HH0000004','DH0000004','SP001','Kiện đồ gia dụng',1,2);

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000004','Tao','Đã tạo','TA0000001','2026-09-23 07:00:00','Tiếp nhận yêu cầu gửi hàng');

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000004','GuiDuyet','Đã tạo','TA0000001','2026-09-23 07:10:00',NULL);

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000004','Huy','Đã hủy','TA0000001','2026-09-23 07:20:00','Khách đổi kế hoạch gửi hàng');

-- DH0000005 — Đã duyệt, chưa tiếp nhận

INSERT INTO DonHang (maDonHang,maKhachHang,maDichVu,nguoiGui,nguoiNhan,sdtNguoiGui,sdtNguoiNhan,diachiLayHang,diachiGiaoHang,khoiLuong,tienCOD,phiVanChuyen,trangThai,lyDoHuy,ngayTao) VALUES
  ('DH0000005','KH0000001','DV01','Minh Phát','Võ Thị Hà','0908221144','0901110005','120 Nguyễn Văn Trỗi, TP.HCM','50 Lê Lợi, TP.HCM',3,0,25000,'Đã tạo',NULL,'2026-09-23 07:00:00');

INSERT INTO HangHoa (maHangHoa,maDonHang,maSP,loaiHangHoa,soLuong,trongLuong) VALUES
  ('HH0000005','DH0000005','SP002','Kiện quần áo',2,3);

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000005','Tao','Đã tạo','TA0000001','2026-09-23 07:00:00','Tiếp nhận yêu cầu gửi hàng');

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000005','GuiDuyet','Đã tạo','TA0000001','2026-09-23 07:10:00',NULL);

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000005','Duyet','Đã tạo','TA0000002','2026-09-23 07:20:00','Đủ thông tin tiếp nhận');

-- DH0000006 — Phiếu nhập chờ duyệt

INSERT INTO DonHang (maDonHang,maKhachHang,maDichVu,nguoiGui,nguoiNhan,sdtNguoiGui,sdtNguoiNhan,diachiLayHang,diachiGiaoHang,khoiLuong,tienCOD,phiVanChuyen,trangThai,lyDoHuy,ngayTao) VALUES
  ('DH0000006','KH0000001','DV01','Minh Phát','Đỗ Thanh Huy','0908221144','0901110006','120 Nguyễn Văn Trỗi, TP.HCM','60 Lê Lợi, TP.HCM',1,0,25000,'Đã tạo',NULL,'2026-09-23 07:00:00');

INSERT INTO HangHoa (maHangHoa,maDonHang,maSP,loaiHangHoa,soLuong,trongLuong) VALUES
  ('HH0000006','DH0000006','SP003','Hồ sơ đóng kiện',1,1);

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000006','Tao','Đã tạo','TA0000001','2026-09-23 07:00:00','Tiếp nhận yêu cầu gửi hàng');

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000006','GuiDuyet','Đã tạo','TA0000001','2026-09-23 07:10:00',NULL);

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000006','Duyet','Đã tạo','TA0000002','2026-09-23 07:20:00','Đủ thông tin tiếp nhận');

INSERT INTO PHIEU_NHAP (maPhieuNhap,maDonHang,maNV,ngayNhap,trangThai,ghiChu,nguoiDuyet) VALUES
  ('PN0000006','DH0000006','NV0000003','2026-09-23','Chờ duyệt','Bao bì móp, chờ kiểm tra',NULL);

-- DH0000007 — Đang lưu kho, chưa lập chuyến

INSERT INTO DonHang (maDonHang,maKhachHang,maDichVu,nguoiGui,nguoiNhan,sdtNguoiGui,sdtNguoiNhan,diachiLayHang,diachiGiaoHang,khoiLuong,tienCOD,phiVanChuyen,trangThai,lyDoHuy,ngayTao) VALUES
  ('DH0000007','KH0000001','DV01','Minh Phát','Bùi Thu Lan','0908221144','0901110007','120 Nguyễn Văn Trỗi, TP.HCM','70 Lê Lợi, TP.HCM',2,0,25000,'Đã nhập kho',NULL,'2026-09-23 07:00:00');

INSERT INTO HangHoa (maHangHoa,maDonHang,maSP,loaiHangHoa,soLuong,trongLuong) VALUES
  ('HH0000007','DH0000007','SP001','Kiện đồ gia dụng',1,2);

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000007','Tao','Đã tạo','TA0000001','2026-09-23 07:00:00','Tiếp nhận yêu cầu gửi hàng');

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000007','GuiDuyet','Đã tạo','TA0000001','2026-09-23 07:10:00',NULL);

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000007','Duyet','Đã tạo','TA0000002','2026-09-23 07:20:00','Đủ thông tin tiếp nhận');

INSERT INTO PHIEU_NHAP (maPhieuNhap,maDonHang,maNV,ngayNhap,trangThai,ghiChu,nguoiDuyet) VALUES
  ('PN0000007','DH0000007','NV0000003','2026-09-23','Đã duyệt','Tiếp nhận đủ nguyên đơn','NV0000004');

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000007','DoiTrangThai','Đã nhập kho','TA0000004','2026-09-23 08:00:00','PN0000007');

-- DH0000008 — Phiếu xuất chờ duyệt, chuyến nháp

INSERT INTO DonHang (maDonHang,maKhachHang,maDichVu,nguoiGui,nguoiNhan,sdtNguoiGui,sdtNguoiNhan,diachiLayHang,diachiGiaoHang,khoiLuong,tienCOD,phiVanChuyen,trangThai,lyDoHuy,ngayTao) VALUES
  ('DH0000008','KH0000001','DV01','Minh Phát','Ngô Văn Nam','0908221144','0901110008','120 Nguyễn Văn Trỗi, TP.HCM','80 Lê Lợi, TP.HCM',3,0,25000,'Đã nhập kho',NULL,'2026-09-23 07:00:00');

INSERT INTO HangHoa (maHangHoa,maDonHang,maSP,loaiHangHoa,soLuong,trongLuong) VALUES
  ('HH0000008','DH0000008','SP002','Kiện quần áo',2,3);

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000008','Tao','Đã tạo','TA0000001','2026-09-23 07:00:00','Tiếp nhận yêu cầu gửi hàng');

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000008','GuiDuyet','Đã tạo','TA0000001','2026-09-23 07:10:00',NULL);

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000008','Duyet','Đã tạo','TA0000002','2026-09-23 07:20:00','Đủ thông tin tiếp nhận');

INSERT INTO PHIEU_NHAP (maPhieuNhap,maDonHang,maNV,ngayNhap,trangThai,ghiChu,nguoiDuyet) VALUES
  ('PN0000008','DH0000008','NV0000003','2026-09-23','Đã duyệt','Tiếp nhận đủ nguyên đơn','NV0000004');

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000008','DoiTrangThai','Đã nhập kho','TA0000004','2026-09-23 08:00:00','PN0000008');

INSERT INTO PHIEU_XUAT (maPhieuXuat,maNV,maDonHang,ngayXuat,lyDoXuat,trangThai,ghiChu,nguoiDuyet) VALUES
  ('PX0000008','NV0000003','DH0000008','2026-09-23','Bàn giao nguyên đơn cho vận chuyển','Chờ duyệt',NULL,NULL);

-- DH0000009 — Đã xuất, chuyến được duyệt chờ tài xế nhận

INSERT INTO DonHang (maDonHang,maKhachHang,maDichVu,nguoiGui,nguoiNhan,sdtNguoiGui,sdtNguoiNhan,diachiLayHang,diachiGiaoHang,khoiLuong,tienCOD,phiVanChuyen,trangThai,lyDoHuy,ngayTao) VALUES
  ('DH0000009','KH0000001','DV01','Minh Phát','Hồ Thị Oanh','0908221144','0901110009','120 Nguyễn Văn Trỗi, TP.HCM','90 Lê Lợi, TP.HCM',1,0,25000,'Đã nhập kho',NULL,'2026-09-23 07:00:00');

INSERT INTO HangHoa (maHangHoa,maDonHang,maSP,loaiHangHoa,soLuong,trongLuong) VALUES
  ('HH0000009','DH0000009','SP003','Hồ sơ đóng kiện',1,1);

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000009','Tao','Đã tạo','TA0000001','2026-09-23 07:00:00','Tiếp nhận yêu cầu gửi hàng');

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000009','GuiDuyet','Đã tạo','TA0000001','2026-09-23 07:10:00',NULL);

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000009','Duyet','Đã tạo','TA0000002','2026-09-23 07:20:00','Đủ thông tin tiếp nhận');

INSERT INTO PHIEU_NHAP (maPhieuNhap,maDonHang,maNV,ngayNhap,trangThai,ghiChu,nguoiDuyet) VALUES
  ('PN0000009','DH0000009','NV0000003','2026-09-23','Đã duyệt','Tiếp nhận đủ nguyên đơn','NV0000004');

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000009','DoiTrangThai','Đã nhập kho','TA0000004','2026-09-23 08:00:00','PN0000009');

INSERT INTO PHIEU_XUAT (maPhieuXuat,maNV,maDonHang,ngayXuat,lyDoXuat,trangThai,ghiChu,nguoiDuyet) VALUES
  ('PX0000009','NV0000003','DH0000009','2026-09-23','Bàn giao nguyên đơn cho vận chuyển','Đã xuất',NULL,'NV0000004');

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000009','XuatKho','Đã xuất','TA0000004','2026-09-23 13:45:00','PX0000009');

-- DH0000010 — Đang vận chuyển

INSERT INTO DonHang (maDonHang,maKhachHang,maDichVu,nguoiGui,nguoiNhan,sdtNguoiGui,sdtNguoiNhan,diachiLayHang,diachiGiaoHang,khoiLuong,tienCOD,phiVanChuyen,trangThai,lyDoHuy,ngayTao) VALUES
  ('DH0000010','KH0000001','DV01','Minh Phát','Mai Tuấn Phong','0908221144','0901110010','120 Nguyễn Văn Trỗi, TP.HCM','100 Lê Lợi, TP.HCM',2,0,25000,'Đang giao',NULL,'2026-09-23 07:00:00');

INSERT INTO HangHoa (maHangHoa,maDonHang,maSP,loaiHangHoa,soLuong,trongLuong) VALUES
  ('HH0000010','DH0000010','SP001','Kiện đồ gia dụng',1,2);

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000010','Tao','Đã tạo','TA0000001','2026-09-23 07:00:00','Tiếp nhận yêu cầu gửi hàng');

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000010','GuiDuyet','Đã tạo','TA0000001','2026-09-23 07:10:00',NULL);

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000010','Duyet','Đã tạo','TA0000002','2026-09-23 07:20:00','Đủ thông tin tiếp nhận');

INSERT INTO PHIEU_NHAP (maPhieuNhap,maDonHang,maNV,ngayNhap,trangThai,ghiChu,nguoiDuyet) VALUES
  ('PN0000010','DH0000010','NV0000003','2026-09-23','Đã duyệt','Tiếp nhận đủ nguyên đơn','NV0000004');

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000010','DoiTrangThai','Đã nhập kho','TA0000004','2026-09-23 08:00:00','PN0000010');

INSERT INTO PHIEU_XUAT (maPhieuXuat,maNV,maDonHang,ngayXuat,lyDoXuat,trangThai,ghiChu,nguoiDuyet) VALUES
  ('PX0000010','NV0000003','DH0000010','2026-09-23','Bàn giao nguyên đơn cho vận chuyển','Đã xuất',NULL,'NV0000004');

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000010','XuatKho','Đã xuất','TA0000004','2026-09-23 08:15:00','PX0000010');

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000010','DoiTrangThai','Đang giao','TA0000007','2026-09-23 08:30:00','Nhận đủ nguyên đơn từ kho');

-- DH0000011 — Đã giao, COD chưa thu

INSERT INTO DonHang (maDonHang,maKhachHang,maDichVu,nguoiGui,nguoiNhan,sdtNguoiGui,sdtNguoiNhan,diachiLayHang,diachiGiaoHang,khoiLuong,tienCOD,phiVanChuyen,trangThai,lyDoHuy,ngayTao) VALUES
  ('DH0000011','KH0000003','DV01','Shop MiSa','Lý Minh Quân','0908123456','0901110011','78 Lê Văn Sỹ, TP.HCM','110 Lê Lợi, TP.HCM',3,120000,25000,'Đã giao',NULL,'2026-09-20 07:00:00');

INSERT INTO HangHoa (maHangHoa,maDonHang,maSP,loaiHangHoa,soLuong,trongLuong) VALUES
  ('HH0000011','DH0000011','SP002','Kiện quần áo',2,3);

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000011','Tao','Đã tạo','TA0000001','2026-09-20 07:00:00','Tiếp nhận yêu cầu gửi hàng');

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000011','GuiDuyet','Đã tạo','TA0000001','2026-09-20 07:10:00',NULL);

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000011','Duyet','Đã tạo','TA0000002','2026-09-20 07:20:00','Đủ thông tin tiếp nhận');

INSERT INTO PHIEU_NHAP (maPhieuNhap,maDonHang,maNV,ngayNhap,trangThai,ghiChu,nguoiDuyet) VALUES
  ('PN0000011','DH0000011','NV0000003','2026-09-20','Đã duyệt','Tiếp nhận đủ nguyên đơn','NV0000004');

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000011','DoiTrangThai','Đã nhập kho','TA0000004','2026-09-20 08:00:00','PN0000011');

INSERT INTO PHIEU_XUAT (maPhieuXuat,maNV,maDonHang,ngayXuat,lyDoXuat,trangThai,ghiChu,nguoiDuyet) VALUES
  ('PX0000011','NV0000003','DH0000011','2026-09-20','Bàn giao nguyên đơn cho vận chuyển','Đã xuất',NULL,'NV0000004');

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000011','XuatKho','Đã xuất','TA0000004','2026-09-20 08:15:00','PX0000011');

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000011','DoiTrangThai','Đang giao','TA0000016','2026-09-20 08:30:00','Nhận đủ nguyên đơn từ kho');

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000011','DoiTrangThai','Đã giao','TA0000016','2026-09-20 09:15:00','Người nhận đã ký nhận');

-- DH0000012 — Đã đối soát và trả hết COD

INSERT INTO DonHang (maDonHang,maKhachHang,maDichVu,nguoiGui,nguoiNhan,sdtNguoiGui,sdtNguoiNhan,diachiLayHang,diachiGiaoHang,khoiLuong,tienCOD,phiVanChuyen,trangThai,lyDoHuy,ngayTao) VALUES
  ('DH0000012','KH0000003','DV01','Shop MiSa','Đặng Hoài Sơn','0908123456','0901110012','78 Lê Văn Sỹ, TP.HCM','120 Lê Lợi, TP.HCM',1,600000,25000,'Đã đối soát',NULL,'2026-09-20 07:00:00');

INSERT INTO HangHoa (maHangHoa,maDonHang,maSP,loaiHangHoa,soLuong,trongLuong) VALUES
  ('HH0000012','DH0000012','SP003','Hồ sơ đóng kiện',1,1);

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000012','Tao','Đã tạo','TA0000001','2026-09-20 07:00:00','Tiếp nhận yêu cầu gửi hàng');

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000012','GuiDuyet','Đã tạo','TA0000001','2026-09-20 07:10:00',NULL);

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000012','Duyet','Đã tạo','TA0000002','2026-09-20 07:20:00','Đủ thông tin tiếp nhận');

INSERT INTO PHIEU_NHAP (maPhieuNhap,maDonHang,maNV,ngayNhap,trangThai,ghiChu,nguoiDuyet) VALUES
  ('PN0000012','DH0000012','NV0000003','2026-09-20','Đã duyệt','Tiếp nhận đủ nguyên đơn','NV0000004');

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000012','DoiTrangThai','Đã nhập kho','TA0000004','2026-09-20 08:00:00','PN0000012');

INSERT INTO PHIEU_XUAT (maPhieuXuat,maNV,maDonHang,ngayXuat,lyDoXuat,trangThai,ghiChu,nguoiDuyet) VALUES
  ('PX0000012','NV0000003','DH0000012','2026-09-20','Bàn giao nguyên đơn cho vận chuyển','Đã xuất',NULL,'NV0000004');

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000012','XuatKho','Đã xuất','TA0000004','2026-09-20 08:15:00','PX0000012');

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000012','DoiTrangThai','Đang giao','TA0000016','2026-09-20 08:30:00','Nhận đủ nguyên đơn từ kho');

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000012','DoiTrangThai','Đã giao','TA0000016','2026-09-20 09:30:00','Người nhận đã ký nhận');

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000012','DoiTrangThai','Đã đối soát','TA0000011','2026-09-20 11:00:00','Đối soát COD khớp');

-- DH0000013 — Đã giao, COD thiếu, phiếu thu chờ duyệt

INSERT INTO DonHang (maDonHang,maKhachHang,maDichVu,nguoiGui,nguoiNhan,sdtNguoiGui,sdtNguoiNhan,diachiLayHang,diachiGiaoHang,khoiLuong,tienCOD,phiVanChuyen,trangThai,lyDoHuy,ngayTao) VALUES
  ('DH0000013','KH0000003','DV01','Shop MiSa','Trịnh Bảo Trâm','0908123456','0901110013','78 Lê Văn Sỹ, TP.HCM','130 Lê Lợi, TP.HCM',2,300000,25000,'Đã giao',NULL,'2026-09-20 07:00:00');

INSERT INTO HangHoa (maHangHoa,maDonHang,maSP,loaiHangHoa,soLuong,trongLuong) VALUES
  ('HH0000013','DH0000013','SP001','Kiện đồ gia dụng',1,2);

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000013','Tao','Đã tạo','TA0000001','2026-09-20 07:00:00','Tiếp nhận yêu cầu gửi hàng');

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000013','GuiDuyet','Đã tạo','TA0000001','2026-09-20 07:10:00',NULL);

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000013','Duyet','Đã tạo','TA0000002','2026-09-20 07:20:00','Đủ thông tin tiếp nhận');

INSERT INTO PHIEU_NHAP (maPhieuNhap,maDonHang,maNV,ngayNhap,trangThai,ghiChu,nguoiDuyet) VALUES
  ('PN0000013','DH0000013','NV0000003','2026-09-20','Đã duyệt','Tiếp nhận đủ nguyên đơn','NV0000004');

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000013','DoiTrangThai','Đã nhập kho','TA0000004','2026-09-20 08:00:00','PN0000013');

INSERT INTO PHIEU_XUAT (maPhieuXuat,maNV,maDonHang,ngayXuat,lyDoXuat,trangThai,ghiChu,nguoiDuyet) VALUES
  ('PX0000013','NV0000003','DH0000013','2026-09-20','Bàn giao nguyên đơn cho vận chuyển','Đã xuất',NULL,'NV0000004');

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000013','XuatKho','Đã xuất','TA0000004','2026-09-20 08:15:00','PX0000013');

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000013','DoiTrangThai','Đang giao','TA0000016','2026-09-20 08:30:00','Nhận đủ nguyên đơn từ kho');

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000013','DoiTrangThai','Đã giao','TA0000016','2026-09-20 09:45:00','Người nhận đã ký nhận');

-- DH0000014 — Đã đối soát, mới trả một phần COD

INSERT INTO DonHang (maDonHang,maKhachHang,maDichVu,nguoiGui,nguoiNhan,sdtNguoiGui,sdtNguoiNhan,diachiLayHang,diachiGiaoHang,khoiLuong,tienCOD,phiVanChuyen,trangThai,lyDoHuy,ngayTao) VALUES
  ('DH0000014','KH0000001','DV01','Minh Phát','Dương Hải Yến','0908221144','0901110014','120 Nguyễn Văn Trỗi, TP.HCM','140 Lê Lợi, TP.HCM',3,450000,25000,'Đã đối soát',NULL,'2026-09-20 07:00:00');

INSERT INTO HangHoa (maHangHoa,maDonHang,maSP,loaiHangHoa,soLuong,trongLuong) VALUES
  ('HH0000014','DH0000014','SP002','Kiện quần áo',2,3);

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000014','Tao','Đã tạo','TA0000001','2026-09-20 07:00:00','Tiếp nhận yêu cầu gửi hàng');

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000014','GuiDuyet','Đã tạo','TA0000001','2026-09-20 07:10:00',NULL);

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000014','Duyet','Đã tạo','TA0000002','2026-09-20 07:20:00','Đủ thông tin tiếp nhận');

INSERT INTO PHIEU_NHAP (maPhieuNhap,maDonHang,maNV,ngayNhap,trangThai,ghiChu,nguoiDuyet) VALUES
  ('PN0000014','DH0000014','NV0000003','2026-09-20','Đã duyệt','Tiếp nhận đủ nguyên đơn','NV0000004');

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000014','DoiTrangThai','Đã nhập kho','TA0000004','2026-09-20 08:00:00','PN0000014');

INSERT INTO PHIEU_XUAT (maPhieuXuat,maNV,maDonHang,ngayXuat,lyDoXuat,trangThai,ghiChu,nguoiDuyet) VALUES
  ('PX0000014','NV0000003','DH0000014','2026-09-20','Bàn giao nguyên đơn cho vận chuyển','Đã xuất',NULL,'NV0000004');

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000014','XuatKho','Đã xuất','TA0000004','2026-09-20 08:15:00','PX0000014');

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000014','DoiTrangThai','Đang giao','TA0000016','2026-09-20 08:30:00','Nhận đủ nguyên đơn từ kho');

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000014','DoiTrangThai','Đã giao','TA0000016','2026-09-20 10:00:00','Người nhận đã ký nhận');

INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
  ('DH0000014','DoiTrangThai','Đã đối soát','TA0000011','2026-09-20 11:00:00','Đối soát COD khớp');

INSERT INTO ChuyenVan (MaChuyen,MaNguoiLap,MaTaiXe,MaPhuongTien,MaDonHang,NgayKhoiHanh,ThoiGianDuKien,ThoiGianThucTe,TrangThai) VALUES
  (1,'NV0000005',NULL,NULL,'DH0000008','2026-09-23 16:00:00','2026-09-23 18:00:00',NULL,'Nháp');

INSERT INTO ChiTietChuyenHang (MaChuyen,MaDonHang) VALUES
  (1,'DH0000008');

INSERT INTO LoTrinh (MaChuyen,DiemXuatPhat,DiemKetThuc,KhoangCach,ThoiGianDuKien,MoTa) VALUES
  (1,'Kho Hoàng Minh','Đường Lê Lợi, TP.HCM',12,120,'Giao nguyên đơn theo địa chỉ người nhận');

INSERT INTO ChuyenVan (MaChuyen,MaNguoiLap,MaTaiXe,MaPhuongTien,MaDonHang,NgayKhoiHanh,ThoiGianDuKien,ThoiGianThucTe,TrangThai) VALUES
  (2,'NV0000005','TX0000001',1,'DH0000009','2026-09-23 14:00:00','2026-09-23 16:00:00',NULL,'Đã phê duyệt');

INSERT INTO ChiTietChuyenHang (MaChuyen,MaDonHang) VALUES
  (2,'DH0000009');

INSERT INTO LoTrinh (MaChuyen,DiemXuatPhat,DiemKetThuc,KhoangCach,ThoiGianDuKien,MoTa) VALUES
  (2,'Kho Hoàng Minh','Đường Lê Lợi, TP.HCM',12,120,'Giao nguyên đơn theo địa chỉ người nhận');

INSERT INTO ChuyenVan (MaChuyen,MaNguoiLap,MaTaiXe,MaPhuongTien,MaDonHang,NgayKhoiHanh,ThoiGianDuKien,ThoiGianThucTe,TrangThai) VALUES
  (3,'NV0000005','TX0000001',1,'DH0000010','2026-09-23 08:30:00','2026-09-23 11:30:00',NULL,'Đang giao');

INSERT INTO ChiTietChuyenHang (MaChuyen,MaDonHang) VALUES
  (3,'DH0000010');

INSERT INTO LoTrinh (MaChuyen,DiemXuatPhat,DiemKetThuc,KhoangCach,ThoiGianDuKien,MoTa) VALUES
  (3,'Kho Hoàng Minh','Đường Lê Lợi, TP.HCM',12,180,'Giao nguyên đơn theo địa chỉ người nhận');

INSERT INTO MocHanhTrinh (maChuyen,maDonHang,diem,moTa,thoiGian,maNVCapNhat) VALUES
  (3,'DH0000010','Đã lấy hàng','Đã đối chiếu phiếu xuất','2026-09-23 08:30:00','NV0000007');

INSERT INTO ChuyenVan (MaChuyen,MaNguoiLap,MaTaiXe,MaPhuongTien,MaDonHang,NgayKhoiHanh,ThoiGianDuKien,ThoiGianThucTe,TrangThai) VALUES
  (4,'NV0000005','TX0000003',5,'DH0000011','2026-09-20 08:30:00','2026-09-20 10:30:00','2026-09-20 10:00:00','Hoàn thành');

INSERT INTO ChiTietChuyenHang (MaChuyen,MaDonHang) VALUES
  (4,'DH0000011'),
  (4,'DH0000012'),
  (4,'DH0000013'),
  (4,'DH0000014');

INSERT INTO LoTrinh (MaChuyen,DiemXuatPhat,DiemKetThuc,KhoangCach,ThoiGianDuKien,MoTa) VALUES
  (4,'Kho Hoàng Minh','Đường Lê Lợi, TP.HCM',12,120,'Giao nguyên đơn theo địa chỉ người nhận');

INSERT INTO MocHanhTrinh (maChuyen,maDonHang,diem,moTa,thoiGian,maNVCapNhat) VALUES
  (4,'DH0000011','Đã lấy hàng','Đã đối chiếu phiếu xuất','2026-09-20 08:30:00','NV0000016');

INSERT INTO MocHanhTrinh (maChuyen,maDonHang,diem,moTa,thoiGian,maNVCapNhat) VALUES
  (4,'DH0000011','Đến điểm giao','Người nhận ký nhận','2026-09-20 09:15:00','NV0000016');

INSERT INTO MinhChungGiaoHang (maMinhChung,maDonHang,loaiMinhChung,duongDan,maNVTai,thoiGianTai) VALUES
  ('MC0000011','DH0000011','ChuKy','demo/chu-ky-DH0000011.png','NV0000016','2026-09-20 09:15:00');

INSERT INTO MocHanhTrinh (maChuyen,maDonHang,diem,moTa,thoiGian,maNVCapNhat) VALUES
  (4,'DH0000012','Đã lấy hàng','Đã đối chiếu phiếu xuất','2026-09-20 08:30:00','NV0000016');

INSERT INTO MocHanhTrinh (maChuyen,maDonHang,diem,moTa,thoiGian,maNVCapNhat) VALUES
  (4,'DH0000012','Đến điểm giao','Người nhận ký nhận','2026-09-20 09:30:00','NV0000016');

INSERT INTO MinhChungGiaoHang (maMinhChung,maDonHang,loaiMinhChung,duongDan,maNVTai,thoiGianTai) VALUES
  ('MC0000012','DH0000012','ChuKy','demo/chu-ky-DH0000012.png','NV0000016','2026-09-20 09:30:00');

INSERT INTO MocHanhTrinh (maChuyen,maDonHang,diem,moTa,thoiGian,maNVCapNhat) VALUES
  (4,'DH0000013','Đã lấy hàng','Đã đối chiếu phiếu xuất','2026-09-20 08:30:00','NV0000016');

INSERT INTO MocHanhTrinh (maChuyen,maDonHang,diem,moTa,thoiGian,maNVCapNhat) VALUES
  (4,'DH0000013','Đến điểm giao','Người nhận ký nhận','2026-09-20 09:45:00','NV0000016');

INSERT INTO MinhChungGiaoHang (maMinhChung,maDonHang,loaiMinhChung,duongDan,maNVTai,thoiGianTai) VALUES
  ('MC0000013','DH0000013','ChuKy','demo/chu-ky-DH0000013.png','NV0000016','2026-09-20 09:45:00');

INSERT INTO MocHanhTrinh (maChuyen,maDonHang,diem,moTa,thoiGian,maNVCapNhat) VALUES
  (4,'DH0000014','Đã lấy hàng','Đã đối chiếu phiếu xuất','2026-09-20 08:30:00','NV0000016');

INSERT INTO MocHanhTrinh (maChuyen,maDonHang,diem,moTa,thoiGian,maNVCapNhat) VALUES
  (4,'DH0000014','Đến điểm giao','Người nhận ký nhận','2026-09-20 10:00:00','NV0000016');

INSERT INTO MinhChungGiaoHang (maMinhChung,maDonHang,loaiMinhChung,duongDan,maNVTai,thoiGianTai) VALUES
  ('MC0000014','DH0000014','ChuKy','demo/chu-ky-DH0000014.png','NV0000016','2026-09-20 10:00:00');

INSERT INTO SuCoVanTai (MaChuyen,MaDonHang,MaTaiXe,LoaiSuCo,MoTa,ThoiGian,TrangThai,HuongXuLy,NguoiXuLy,ThoiGianXuLy) VALUES
  (3,'DH0000010','TX0000001','Tắc đường','Ùn tắc tại điểm rẽ','2026-09-23 09:00:00','Đang xử lý','Chuyển sang tuyến đường song song','NV0000005','2026-09-23 09:10:00');

INSERT INTO BienBanSuCo (maBienBan,maPhieuNhap,maDonHang,loaiSuCo,moTa,maNVLap,ngayLap) VALUES
  ('BB0000001','PN0000006','DH0000006','Hư hỏng','Bao bì móp; chờ khách xác nhận trước khi duyệt tiếp nhận','NV0000003','2026-09-23 07:50:00');

INSERT INTO PHIEU_KIEM_KE (maPhieuKiemKe,ngayKiemKe,khuVucKiemKe,maNV,trangThai,ghiChu) VALUES
  ('KK0000001','2026-09-23','Khu A','NV0000003','Đã chốt','Đối chiếu hai đơn đang lưu kho'),
  ('KK0000002','2026-09-20','Khu bàn giao','NV0000003','Đã chốt','Đối chiếu sau bàn giao');

INSERT INTO KIEM_KE_DON_HANG (maPhieuKiemKe,maDonHang,soLuongHeThong,soLuongThucTe,chenhLech,ghiChu) VALUES
  ('KK0000001','DH0000007',1,1,0,'Đủ nguyên đơn'),
  ('KK0000001','DH0000008',1,1,0,'Chờ duyệt phiếu xuất'),
  ('KK0000002','DH0000012',0,0,0,'Đã bàn giao');

-- Bảng chi tiết sản phẩm cũ: chỉ dữ liệu đối chiếu lịch sử của đơn 12, không tạo tồn sản phẩm mới.

INSERT INTO CHI_TIET_PHIEU_NHAP (maPhieuNhap,maSP,soLuong,tinhTrangHang) VALUES
  ('PN0000012','SP003',1,'Đạt');

INSERT INTO CHI_TIET_PHIEU_XUAT (maPhieuXuat,maSP,soLuong) VALUES
  ('PX0000012','SP003',1);

INSERT INTO TON_KHO (maTonKho,maSP,maViTri,soLuongTon,ngayCapNhat) VALUES
  ('TK0000001','SP003','Khu bàn giao',0,'2026-09-20 08:15:00');

INSERT INTO CHI_TIET_KIEM_KE (maPhieuKiemKe,maSP,soLuongHeThong,soLuongThucTe,chenhLech,ghiChu) VALUES
  ('KK0000002','SP003',0,0,0,'Đối chiếu lịch sử sau xuất nguyên đơn 12');

INSERT INTO ChiPhiLuuKho (maChiPhi,maPhieuNhap,soTien,ngayGhi,maNVGhi) VALUES
  ('CP0000001','PN0000007',10000,'2026-09-23 09:00:00','NV0000011');

INSERT INTO YeuCauCapNhatHoSo (maNV,noiDung,trangThai,thoiGianGui,nguoiXuLy,thoiGianXuLy,phanHoi) VALUES
  ('NV0000014','Đề nghị cập nhật địa chỉ liên hệ','Chờ xử lý','2026-09-23 08:00:00',NULL,NULL,NULL),
  ('NV0000016','Xác minh lại số điện thoại','Đã xử lý','2026-09-20 15:00:00','NV0000009','2026-09-20 16:00:00','Đã đối chiếu, số điện thoại hiện tại chính xác');

INSERT INTO GiaoDichCOD (maGiaoDich,maDonHang,soTienPhaiThu,soTienThucThu,trangThai,ngayTao,thoiGianCapNhat,nguoiCapNhat,lyDoSaiLech,nguoiXuLySaiLech,thoiGianXuLySaiLech) VALUES
  ('GD0000011','DH0000011',120000,NULL,'Chưa thu','2026-09-20',NULL,NULL,NULL,NULL,NULL);

INSERT INTO GiaoDichCOD (maGiaoDich,maDonHang,soTienPhaiThu,soTienThucThu,trangThai,ngayTao,thoiGianCapNhat,nguoiCapNhat,lyDoSaiLech,nguoiXuLySaiLech,thoiGianXuLySaiLech) VALUES
  ('GD0000012','DH0000012',600000,600000,'Đã đối soát','2026-09-20','2026-09-20 11:00:00','NV0000011',NULL,NULL,NULL);

INSERT INTO GiaoDichCOD (maGiaoDich,maDonHang,soTienPhaiThu,soTienThucThu,trangThai,ngayTao,thoiGianCapNhat,nguoiCapNhat,lyDoSaiLech,nguoiXuLySaiLech,thoiGianXuLySaiLech) VALUES
  ('GD0000013','DH0000013',300000,250000,'Sai lệch','2026-09-20','2026-09-20 11:00:00','NV0000011','Thiếu 50.000 đồng, đang xác minh với tài xế','NV0000011','2026-09-20 11:00:00');

INSERT INTO GiaoDichCOD (maGiaoDich,maDonHang,soTienPhaiThu,soTienThucThu,trangThai,ngayTao,thoiGianCapNhat,nguoiCapNhat,lyDoSaiLech,nguoiXuLySaiLech,thoiGianXuLySaiLech) VALUES
  ('GD0000014','DH0000014',450000,450000,'Đã đối soát','2026-09-20','2026-09-20 11:00:00','NV0000011',NULL,NULL,NULL);

INSERT INTO DoiSoatCOD (maDoiSoat,ngayDoiSoat,tongTien,chenhLech,trangThai,nguoiDoiSoat,thoiGian) VALUES
  ('DS0000012','2026-09-20',600000,0,'Khớp','NV0000011','2026-09-20 11:00:00');

INSERT INTO ChiTietDoiSoat (maDoiSoat,maGiaoDich) VALUES
  ('DS0000012','GD0000012');

INSERT INTO PhieuThuChi (maPhieu,loaiPhieu,maGiaoDich,soTien,ngayLap,noiDung,trangThai,nguoiLap,nguoiDuyet,nguoiXacNhan,thoiGianXacNhan) VALUES
  ('PT0000012','Thu','GD0000012',600000,'2026-09-20','Thu COD DH0000012','Đã thực hiện','NV0000011','NV0000013','NV0000012','2026-09-20 12:00:00');

INSERT INTO SoQuy (maSoQuy,maPhieu,soTienThu,soTienChi,ngayGhiSo,maNVGhiSo) VALUES
  ('SQ0000024','PT0000012',600000,0,'2026-09-20','NV0000012');

INSERT INTO PhieuThuChi (maPhieu,loaiPhieu,maGiaoDich,soTien,ngayLap,noiDung,trangThai,nguoiLap,nguoiDuyet,nguoiXacNhan,thoiGianXacNhan) VALUES
  ('PC0000012','Chi','GD0000012',600000,'2026-09-20','Chi COD DH0000012','Đã thực hiện','NV0000011','NV0000013','NV0000012','2026-09-20 13:00:00');

INSERT INTO SoQuy (maSoQuy,maPhieu,soTienThu,soTienChi,ngayGhiSo,maNVGhiSo) VALUES
  ('SQ0000025','PC0000012',0,600000,'2026-09-20','NV0000012');

INSERT INTO CongNo (maCongNo,maKhachHang,soTienPhaiTra,soTienDaTra,trangThai,ngayCapNhat) VALUES
  ('CN0000012','KH0000003',600000,600000,'Đã tất toán','2026-09-20 13:00:00');

INSERT INTO DoiSoatCOD (maDoiSoat,ngayDoiSoat,tongTien,chenhLech,trangThai,nguoiDoiSoat,thoiGian) VALUES
  ('DS0000014','2026-09-20',450000,0,'Khớp','NV0000011','2026-09-20 11:00:00');

INSERT INTO ChiTietDoiSoat (maDoiSoat,maGiaoDich) VALUES
  ('DS0000014','GD0000014');

INSERT INTO PhieuThuChi (maPhieu,loaiPhieu,maGiaoDich,soTien,ngayLap,noiDung,trangThai,nguoiLap,nguoiDuyet,nguoiXacNhan,thoiGianXacNhan) VALUES
  ('PT0000014','Thu','GD0000014',450000,'2026-09-20','Thu COD DH0000014','Đã thực hiện','NV0000011','NV0000013','NV0000012','2026-09-20 12:00:00');

INSERT INTO SoQuy (maSoQuy,maPhieu,soTienThu,soTienChi,ngayGhiSo,maNVGhiSo) VALUES
  ('SQ0000028','PT0000014',450000,0,'2026-09-20','NV0000012');

INSERT INTO PhieuThuChi (maPhieu,loaiPhieu,maGiaoDich,soTien,ngayLap,noiDung,trangThai,nguoiLap,nguoiDuyet,nguoiXacNhan,thoiGianXacNhan) VALUES
  ('PC0000014','Chi','GD0000014',200000,'2026-09-20','Chi COD DH0000014','Đã thực hiện','NV0000011','NV0000013','NV0000012','2026-09-20 13:00:00');

INSERT INTO SoQuy (maSoQuy,maPhieu,soTienThu,soTienChi,ngayGhiSo,maNVGhiSo) VALUES
  ('SQ0000029','PC0000014',0,200000,'2026-09-20','NV0000012');

INSERT INTO CongNo (maCongNo,maKhachHang,soTienPhaiTra,soTienDaTra,trangThai,ngayCapNhat) VALUES
  ('CN0000014','KH0000001',450000,200000,'Còn nợ','2026-09-20 13:00:00');

INSERT INTO PhieuThuChi (maPhieu,loaiPhieu,maGiaoDich,soTien,ngayLap,noiDung,trangThai,nguoiLap) VALUES
  ('PT0000013','Thu','GD0000013',250000,'2026-09-20','Thu COD thực tế; thiếu 50.000 đang xác minh','Chờ duyệt','NV0000011');
COMMIT;

-- Kiểm đếm đủ 36 bảng; sổ quỹ và công nợ còn 250.000 đồng.
SELECT 'PhongBan' tenBang, COUNT(*) soBanGhi FROM `PhongBan`
UNION ALL
SELECT 'ChucVu' tenBang, COUNT(*) soBanGhi FROM `ChucVu`
UNION ALL
SELECT 'TrangThaiNhanVien' tenBang, COUNT(*) soBanGhi FROM `TrangThaiNhanVien`
UNION ALL
SELECT 'NhanVien' tenBang, COUNT(*) soBanGhi FROM `NhanVien`
UNION ALL
SELECT 'TaiKhoan' tenBang, COUNT(*) soBanGhi FROM `TaiKhoan`
UNION ALL
SELECT 'TaiXe' tenBang, COUNT(*) soBanGhi FROM `TaiXe`
UNION ALL
SELECT 'KhachHang' tenBang, COUNT(*) soBanGhi FROM `KhachHang`
UNION ALL
SELECT 'DichVu' tenBang, COUNT(*) soBanGhi FROM `DichVu`
UNION ALL
SELECT 'DonHang' tenBang, COUNT(*) soBanGhi FROM `DonHang`
UNION ALL
SELECT 'SanPham' tenBang, COUNT(*) soBanGhi FROM `SanPham`
UNION ALL
SELECT 'HangHoa' tenBang, COUNT(*) soBanGhi FROM `HangHoa`
UNION ALL
SELECT 'PHIEU_NHAP' tenBang, COUNT(*) soBanGhi FROM `PHIEU_NHAP`
UNION ALL
SELECT 'CHI_TIET_PHIEU_NHAP' tenBang, COUNT(*) soBanGhi FROM `CHI_TIET_PHIEU_NHAP`
UNION ALL
SELECT 'PHIEU_XUAT' tenBang, COUNT(*) soBanGhi FROM `PHIEU_XUAT`
UNION ALL
SELECT 'CHI_TIET_PHIEU_XUAT' tenBang, COUNT(*) soBanGhi FROM `CHI_TIET_PHIEU_XUAT`
UNION ALL
SELECT 'PHIEU_KIEM_KE' tenBang, COUNT(*) soBanGhi FROM `PHIEU_KIEM_KE`
UNION ALL
SELECT 'CHI_TIET_KIEM_KE' tenBang, COUNT(*) soBanGhi FROM `CHI_TIET_KIEM_KE`
UNION ALL
SELECT 'TON_KHO' tenBang, COUNT(*) soBanGhi FROM `TON_KHO`
UNION ALL
SELECT 'ChiPhiLuuKho' tenBang, COUNT(*) soBanGhi FROM `ChiPhiLuuKho`
UNION ALL
SELECT 'BienBanSuCo' tenBang, COUNT(*) soBanGhi FROM `BienBanSuCo`
UNION ALL
SELECT 'PhuongTien' tenBang, COUNT(*) soBanGhi FROM `PhuongTien`
UNION ALL
SELECT 'ChuyenVan' tenBang, COUNT(*) soBanGhi FROM `ChuyenVan`
UNION ALL
SELECT 'ChiTietChuyenHang' tenBang, COUNT(*) soBanGhi FROM `ChiTietChuyenHang`
UNION ALL
SELECT 'LoTrinh' tenBang, COUNT(*) soBanGhi FROM `LoTrinh`
UNION ALL
SELECT 'SuCoVanTai' tenBang, COUNT(*) soBanGhi FROM `SuCoVanTai`
UNION ALL
SELECT 'MinhChungGiaoHang' tenBang, COUNT(*) soBanGhi FROM `MinhChungGiaoHang`
UNION ALL
SELECT 'LichSuDonHang' tenBang, COUNT(*) soBanGhi FROM `LichSuDonHang`
UNION ALL
SELECT 'GiaoDichCOD' tenBang, COUNT(*) soBanGhi FROM `GiaoDichCOD`
UNION ALL
SELECT 'PhieuThuChi' tenBang, COUNT(*) soBanGhi FROM `PhieuThuChi`
UNION ALL
SELECT 'SoQuy' tenBang, COUNT(*) soBanGhi FROM `SoQuy`
UNION ALL
SELECT 'CongNo' tenBang, COUNT(*) soBanGhi FROM `CongNo`
UNION ALL
SELECT 'DoiSoatCOD' tenBang, COUNT(*) soBanGhi FROM `DoiSoatCOD`
UNION ALL
SELECT 'ChiTietDoiSoat' tenBang, COUNT(*) soBanGhi FROM `ChiTietDoiSoat`
UNION ALL
SELECT 'MocHanhTrinh' tenBang, COUNT(*) soBanGhi FROM `MocHanhTrinh`
UNION ALL
SELECT 'YeuCauCapNhatHoSo' tenBang, COUNT(*) soBanGhi FROM `YeuCauCapNhatHoSo`
UNION ALL
SELECT 'KIEM_KE_DON_HANG' tenBang, COUNT(*) soBanGhi FROM `KIEM_KE_DON_HANG`;
SELECT * FROM vw_SoDuQuy;
SELECT * FROM vw_CongNoConLai;
