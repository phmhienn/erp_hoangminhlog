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
