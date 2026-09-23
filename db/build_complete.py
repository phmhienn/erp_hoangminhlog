"""Build the standalone SQL without modifying source migrations."""
from pathlib import Path
import re

root = Path(__file__).resolve().parents[1]
folder = root / 'backend/src/main/resources/db/migration'
files = sorted(folder.glob('V*__*.sql'), key=lambda p: int(p.name.split('__')[0][1:]))
def statements(text):
    return re.sub(r'--[^\n]*', '', text).strip()
base = (root / 'db/ERP_database.sql').read_text(encoding='utf-8-sig')
base = re.sub(r'CREATE DATABASE.*?;', '', base, flags=re.S | re.I)
base = re.sub(r'USE\s+\w+;', '', base, flags=re.I)
assert statements(base) == statements(files[0].read_text(encoding='utf-8-sig')), 'Baseline differs from V1'
header = '''-- ERP Hoàng Minh: cấu trúc và dữ liệu demo đầy đủ, MySQL 8.
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
'''
extra = '''
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
'''
extra += """
-- Kiểm kê nguyên đơn: đơn DH0000004 đã hoàn tất nên không còn nằm trong kho.
INSERT INTO PHIEU_KIEM_KE (maPhieuKiemKe,ngayKiemKe,khuVucKiemKe,maNV,trangThai,ghiChu)
VALUES ('KK0000002','2026-09-18','Kho Hoàng Minh','NV0000003','Đã chốt','Kiểm tra đơn đã bàn giao');
INSERT INTO KIEM_KE_DON_HANG VALUES ('KK0000002','DH0000004',0,0,0,'Đơn đã giao, không còn lưu kho');
"""
body = '\n'.join('-- ===== '+p.name+' =====\n'+p.read_text(encoding='utf-8-sig') for p in files)
tables = re.findall(r'CREATE TABLE\s+(\w+)', body, re.I)
inserts = set(t.lower() for t in re.findall(r'INSERT INTO\s+(\w+)', body+extra, re.I))
assert len(tables)==36 and all(t.lower() in inserts for t in tables)
checks = '\n-- Kiểm đếm: mỗi bảng nghiệp vụ phải có ít nhất một bản ghi.\n'
checks += '\nUNION ALL\n'.join(f"SELECT '{t}' AS tenBang, COUNT(*) AS soBanGhi FROM `{t}`" for t in tables)+';\n'
checks += 'SELECT * FROM vw_SoDuQuy;\nSELECT * FROM vw_TonKhoChiTiet;\nSELECT * FROM vw_CongNoConLai;\n'
checks += 'SET SESSION SQL_SAFE_UPDATES = @erp_previous_safe_updates;\n'
(root/'db/ERP_hoangminh_hoan_chinh.sql').write_text(header+body+extra+checks,encoding='utf-8')
print(f'Generated: {len(files)} migrations, {len(tables)} tables; all tables have demo INSERTs.')
