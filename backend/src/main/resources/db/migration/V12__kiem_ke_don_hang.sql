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
