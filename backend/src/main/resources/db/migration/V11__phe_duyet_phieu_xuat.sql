-- [G26] Bổ sung người duyệt cho phiếu xuất kho.
-- Đặc tả 3.2.2 giao cho Người quản lý kho usecase "Quản lý phiếu nhập/ xuất (kiểm tra & phê duyệt)"
-- và biểu đồ usecase 3.2.3 có "Quản lí phiếu nhập_xuất", nhưng bản cài đặt cũ chỉ phê duyệt phiếu
-- nhập; phiếu xuất do nhân viên kho tự xác nhận và trừ tồn, không ai kiểm soát.
ALTER TABLE PHIEU_XUAT
  ADD COLUMN nguoiDuyet VARCHAR(20) NULL AFTER trangThai,
  ADD CONSTRAINT fk_px_duyet FOREIGN KEY (nguoiDuyet) REFERENCES NhanVien (maNV);

-- Phiếu xuất đã trừ tồn ở luồng cũ coi như quản lý kho đã duyệt (ghi theo người lập phiếu).
UPDATE PHIEU_XUAT SET nguoiDuyet = maNV WHERE trangThai = 'Đã xuất';
