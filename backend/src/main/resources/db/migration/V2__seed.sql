-- =====================================================================
-- V2__seed.sql — DỮ LIỆU GIẢ LẬP LỚP 1: DANH MỤC NỀN (nhiệm vụ 0.5)
-- Nguồn: mục 7 huong_di_lap_trinh (seed theo lớp) + 1.3 "dữ liệu giả lập vừa đủ minh họa luồng".
--
-- Lớp 1 (file này): phòng ban, chức vụ, trạng thái NV, nhân viên đủ vai trò + tài khoản
--                   đăng nhập, tài xế, phương tiện, khách hàng, dịch vụ, sản phẩm.
-- Lớp 2 (nghiệp vụ: đơn đã duyệt → phiếu nhập → tồn kho → …) seed ở phase tương ứng.
-- Lớp 3 (kịch bản luồng phụ) seed ở Phase 7 (nhiệm vụ 7.1).
--
-- MẬT KHẨU DEMO: mọi tài khoản dùng chung mật khẩu  123456
-- (băm BCrypt thật, đã kiểm chứng matches() = true; matKhau VARCHAR(255) theo 4.2).
-- =====================================================================
SET NAMES utf8mb4;

-- ---------------------------------------------------------------------
-- HT4: DANH MỤC NHÂN SỰ (3.4.4)
-- ---------------------------------------------------------------------
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

-- ---------------------------------------------------------------------
-- NHÂN VIÊN ĐỦ CÁC VAI TRÒ NGHIỆP VỤ (danh sách vai trò: mục 6 huong_di_lap_trinh)
-- ---------------------------------------------------------------------
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

-- ---------------------------------------------------------------------
-- TÀI XẾ (bảng dùng chung 4.2) — TX0000004 cố tình thiếu GPLX để kiểm tra
-- cảnh báo chứng chỉ của 3.3.2 (PHỤ LỤC D mục 3: không có trường ngày hết hạn).
-- ---------------------------------------------------------------------
INSERT INTO TaiXe (maTaiXe, maNV, soGPLX, loaiGPLX, trangThai) VALUES
  ('TX0000001', 'NV0000007', '0123456789', 'B2', 'Hoạt động'),
  ('TX0000002', 'NV0000008', '0987654321', 'C',  'Hoạt động'),
  ('TX0000003', 'NV0000016', '7900123456', 'A1', 'Hoạt động'),
  ('TX0000004', 'NV0000017', NULL,         NULL, 'Hoạt động');

-- ---------------------------------------------------------------------
-- TÀI KHOẢN ĐĂNG NHẬP (bảng dùng chung 4.2) — mật khẩu demo: 123456
-- TA0000015 ở trạng thái "Đã khóa" để nghiệm thu 0.4 (tài khoản khóa bị từ chối).
-- ---------------------------------------------------------------------
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

-- ---------------------------------------------------------------------
-- HT1: KHÁCH HÀNG, DỊCH VỤ (3.1.4)
-- ---------------------------------------------------------------------
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

-- ---------------------------------------------------------------------
-- HT2: DANH MỤC SẢN PHẨM (3.2.4 + [G6] dieuKienBaoQuan theo 4.1)
-- ---------------------------------------------------------------------
INSERT INTO SanPham (maSP, tenSP, donViTinh, gia, trangThai, dieuKienBaoQuan) VALUES
  ('SP001', 'Thùng nước khoáng 500ml x 24 chai', 'Thùng',  95000.00, 'Đang kinh doanh', 'Nơi khô ráo, tránh ánh nắng trực tiếp'),
  ('SP002', 'Bao gạo ST25 5kg',                  'Bao',   185000.00, 'Đang kinh doanh', 'Kệ khô, cách mặt sàn 20cm'),
  ('SP003', 'Dầu ăn thực vật 5L',                'Can',   265000.00, 'Đang kinh doanh', 'Nhiệt độ thường, tránh nguồn nhiệt'),
  ('SP004', 'Nồi cơm điện 1.8L',                 'Cái',   890000.00, 'Đang kinh doanh', 'Kệ hàng điện tử, chống va đập'),
  ('SP005', 'Ấm siêu tốc inox 1.7L',             'Cái',   320000.00, 'Đang kinh doanh', 'Kệ hàng điện tử, chống ẩm'),
  ('SP006', 'Áo thun cotton size M',             'Cái',   120000.00, 'Đang kinh doanh', 'Kệ khô, đóng túi nilon'),
  ('SP007', 'Giày thể thao size 41',             'Đôi',   450000.00, 'Đang kinh doanh', 'Kệ khô, giữ nguyên hộp'),
  ('SP008', 'Thuốc bổ sung vitamin C',           'Hộp',   145000.00, 'Đang kinh doanh', 'Kho mát dưới 25 độ C'),
  ('SP009', 'Sách giáo khoa lớp 10 (bộ)',        'Bộ',    210000.00, 'Đang kinh doanh', 'Kệ khô, tránh ẩm mốc'),
  ('SP010', 'Máy sấy tóc 1200W',                 'Cái',   380000.00, 'Ngừng kinh doanh', 'Kệ hàng điện tử, chống va đập');

-- ---------------------------------------------------------------------
-- HT3: PHƯƠNG TIỆN (3.3.4)
-- ---------------------------------------------------------------------
INSERT INTO PhuongTien (MaPhuongTien, BienSo, LoaiXe, TaiTrong, TrangThai) VALUES
  (1, '51C-123.45', 'Xe tải 1 tấn',   1000.00, 'Sẵn sàng'),
  (2, '51D-234.56', 'Xe tải 2.5 tấn', 2500.00, 'Sẵn sàng'),
  (3, '62E-345.67', 'Xe tải 5 tấn',   5000.00, 'Đang bảo trì'),
  (4, '59F-456.78', 'Xe ba gác',       350.00, 'Sẵn sàng'),
  (5, '50A-567.89', 'Xe máy giao hàng', 80.00, 'Sẵn sàng');

