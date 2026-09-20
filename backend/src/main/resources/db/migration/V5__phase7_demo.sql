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
