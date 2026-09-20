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
