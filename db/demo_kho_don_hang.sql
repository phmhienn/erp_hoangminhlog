-- Chỉ dùng khi tạo database demo mới, sau V1–V12 và dữ liệu bổ sung.
-- Chuẩn hóa dữ liệu mẫu cũ sang luồng kho lưu nguyên đơn; không thay đổi cấu trúc.
START TRANSACTION;
UPDATE PHIEU_NHAP SET maNCC=NULL, tongTien=NULL WHERE maPhieuNhap IS NOT NULL;
UPDATE PHIEU_XUAT SET tongTien=NULL WHERE maPhieuXuat IS NOT NULL;
UPDATE CHI_TIET_PHIEU_NHAP SET donGia=NULL, thanhTien=NULL WHERE maPhieuNhap IS NOT NULL;
UPDATE CHI_TIET_PHIEU_XUAT SET donGia=NULL, thanhTien=NULL WHERE maPhieuXuat IS NOT NULL;

-- Hai đơn nguồn đã có bằng chứng giao/COD: bổ sung đủ tiếp nhận và bàn giao.
UPDATE DonHang SET trangThai='Đã giao' WHERE maDonHang IN ('DH0000001','DH0000002');
UPDATE PHIEU_NHAP SET trangThai='Đã duyệt', nguoiDuyet='NV0000004', ghiChu='Tiếp nhận nguyên đơn DH0000001'
WHERE maPhieuNhap='PN0000001';
UPDATE PHIEU_NHAP SET maDonHang='DH0000002', ngayNhap='2026-09-14', ghiChu='Tiếp nhận nguyên đơn DH0000002'
WHERE maPhieuNhap='PN0000002';
UPDATE PHIEU_XUAT SET maDonHang='DH0000001', ngayXuat='2026-09-15', lyDoXuat='Bàn giao vận chuyển', ghiChu='Bàn giao nguyên đơn DH0000001'
WHERE maPhieuXuat='PX0000001';
INSERT INTO PHIEU_XUAT (maPhieuXuat,maNV,maDonHang,ngayXuat,lyDoXuat,trangThai,nguoiDuyet)
VALUES ('PX0000003','NV0000003','DH0000002','2026-09-15','Bàn giao vận chuyển','Đã xuất','NV0000004');
UPDATE ChuyenVan SET TrangThai='Hoàn thành', ThoiGianThucTe='2026-09-15 14:00:00' WHERE MaChuyen=1;
INSERT INTO ChuyenVan (MaChuyen,MaNguoiLap,MaTaiXe,MaPhuongTien,MaDonHang,NgayKhoiHanh,ThoiGianDuKien,ThoiGianThucTe,TrangThai)
VALUES (3,'NV0000005','TX0000003',5,'DH0000002','2026-09-15 10:00:00','2026-09-15 12:00:00','2026-09-15 11:00:00','Hoàn thành');
INSERT INTO ChiTietChuyenHang VALUES (3,'DH0000002');
INSERT INTO LoTrinh (MaChuyen,DiemXuatPhat,DiemKetThuc,KhoangCach,ThoiGianDuKien,MoTa)
VALUES (3,'Kho Hoàng Minh','25 Nguyễn Huệ, TP.HCM',8,120,'Bàn giao nguyên đơn DH0000002');
INSERT INTO MinhChungGiaoHang VALUES ('MC0000003','DH0000002','ChuKy','demo/chu-ky-dh0000002.png','NV0000016','2026-09-15 11:00:00');
INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
('DH0000001','DoiTrangThai','Đã nhập kho','TA0000004','2026-09-14 12:00:00','PN0000001'),
('DH0000001','XuatKho','Đã xuất','TA0000004','2026-09-15 07:45:00','PX0000001'),
('DH0000001','DoiTrangThai','Đang giao','TA0000007','2026-09-15 08:00:00','Chuyến 1'),
('DH0000001','DoiTrangThai','Đã giao','TA0000007','2026-09-15 14:00:00','Đã có minh chứng giao hàng'),
('DH0000002','Duyet','Đã tạo','TA0000002','2026-09-14 09:20:00',NULL),
('DH0000002','DoiTrangThai','Đã nhập kho','TA0000004','2026-09-14 10:00:00','PN0000002'),
('DH0000002','XuatKho','Đã xuất','TA0000004','2026-09-15 09:45:00','PX0000003'),
('DH0000002','DoiTrangThai','Đang giao','TA0000016','2026-09-15 10:00:00','Chuyến 3'),
('DH0000002','DoiTrangThai','Đã giao','TA0000016','2026-09-15 11:00:00','Đã giao; COD đang xử lý sai lệch');

-- Các bảng sản phẩm được giữ để bảo toàn cấu trúc/lịch sử, không dùng làm tồn đơn.
UPDATE TON_KHO SET soLuongTon=0 WHERE maTonKho IS NOT NULL;
UPDATE CHI_TIET_KIEM_KE SET soLuongHeThong=0,soLuongThucTe=0,chenhLech=0,ghiChu='Dữ liệu đối chiếu cũ, không dùng tính tồn đơn' WHERE maPhieuKiemKe IS NOT NULL;
UPDATE PHIEU_KIEM_KE SET ghiChu='Đối chiếu sau bàn giao nguyên đơn',trangThai='Đã chốt' WHERE maPhieuKiemKe='KK0000001';
INSERT INTO KIEM_KE_DON_HANG VALUES ('KK0000001','DH0000002',0,0,0,'Đơn đã bàn giao');

-- Ba trạng thái có thể thao tác ngay: chờ nhập, đang lưu kho, đã xuất chờ tài xế.
INSERT INTO DonHang (maDonHang,maKhachHang,maDichVu,nguoiGui,nguoiNhan,sdtNguoiGui,sdtNguoiNhan,diachiLayHang,diachiGiaoHang,khoiLuong,tienCOD,phiVanChuyen,trangThai,ngayTao) VALUES
('DH0000005','KH0000001','DV01','Minh Phát','Nguyễn Lan','0908221144','0901112225','120 Nguyễn Văn Trỗi, TP.HCM','10 Lê Lợi, TP.HCM',2,0,25000,'Đã tạo','2026-09-23 08:00:00'),
('DH0000006','KH0000003','DV01','Shop MiSa','Trần Huy','0908123456','0901112226','78 Lê Văn Sỹ, TP.HCM','20 Lê Lợi, TP.HCM',3,0,25000,'Đã nhập kho','2026-09-23 08:00:00'),
('DH0000007','KH0000001','DV01','Minh Phát','Lê Mai','0908221144','0901112227','120 Nguyễn Văn Trỗi, TP.HCM','30 Lê Lợi, TP.HCM',1,0,25000,'Đã nhập kho','2026-09-23 08:00:00');
INSERT INTO HangHoa (maHangHoa,maDonHang,loaiHangHoa,soLuong,trongLuong) VALUES
('HH0000005','DH0000005','Kiện đồ gia dụng',1,2),
('HH0000006','DH0000006','Kiện quần áo',2,3),
('HH0000007','DH0000007','Hồ sơ đóng kiện',1,1);
INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu)
SELECT maDonHang,'Tao','Đã tạo','TA0000001','2026-09-23 08:00:00','Demo kho lưu đơn' FROM DonHang WHERE maDonHang IN ('DH0000005','DH0000006','DH0000007');
INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu)
SELECT maDonHang,'GuiDuyet','Đã tạo','TA0000001','2026-09-23 08:10:00',NULL FROM DonHang WHERE maDonHang IN ('DH0000005','DH0000006','DH0000007');
INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu)
SELECT maDonHang,'Duyet','Đã tạo','TA0000002','2026-09-23 08:20:00',NULL FROM DonHang WHERE maDonHang IN ('DH0000005','DH0000006','DH0000007');
INSERT INTO PHIEU_NHAP (maPhieuNhap,maDonHang,maNV,ngayNhap,trangThai,ghiChu,nguoiDuyet) VALUES
('PN0000004','DH0000005','NV0000003','2026-09-23','Chờ duyệt','Chờ xác nhận tiếp nhận nguyên đơn',NULL),
('PN0000005','DH0000006','NV0000003','2026-09-23','Đã duyệt','Đơn đang lưu tại khu A','NV0000004'),
('PN0000006','DH0000007','NV0000003','2026-09-23','Đã duyệt','Tiếp nhận nguyên đơn','NV0000004');
INSERT INTO PHIEU_XUAT (maPhieuXuat,maNV,maDonHang,ngayXuat,lyDoXuat,trangThai,nguoiDuyet)
VALUES ('PX0000004','NV0000003','DH0000007','2026-09-23','Bàn giao vận chuyển','Đã xuất','NV0000004');
INSERT INTO LichSuDonHang (maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu) VALUES
('DH0000006','DoiTrangThai','Đã nhập kho','TA0000004','2026-09-23 09:00:00','PN0000005'),
('DH0000007','DoiTrangThai','Đã nhập kho','TA0000004','2026-09-23 09:00:00','PN0000006'),
('DH0000007','XuatKho','Đã xuất','TA0000004','2026-09-23 10:00:00','PX0000004');
INSERT INTO PHIEU_KIEM_KE (maPhieuKiemKe,ngayKiemKe,khuVucKiemKe,maNV,trangThai,ghiChu)
VALUES ('KK0000003','2026-09-23','Khu A','NV0000003','Đã chốt','Kiểm kê nguyên đơn đang lưu kho');
INSERT INTO KIEM_KE_DON_HANG VALUES ('KK0000003','DH0000006',1,1,0,'Đủ nguyên đơn');
COMMIT;
