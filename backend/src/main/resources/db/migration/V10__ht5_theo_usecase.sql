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
