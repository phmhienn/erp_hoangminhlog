// Danh sách tài khoản demo — bản sao trình bày của dữ liệu seed trong
// backend/src/main/resources/db/migration/V2__seed.sql (nhiệm vụ 0.5).
// Chỉ dùng để người thử nghiệm bấm chọn nhanh trên màn hình đăng nhập;
// đây KHÔNG phải một chức năng của hệ thống (không gọi API, không lưu ở CSDL).

export interface TaiKhoanDemo {
  tenDangNhap: string
  matKhau: string
  vaiTro: string
  tenVaiTro: string
  hoTen: string
}

export const MAT_KHAU_DEMO = '123456'

export const TAI_KHOAN_DEMO: TaiKhoanDemo[] = [
  { tenDangNhap: 'nvkd', matKhau: MAT_KHAU_DEMO, vaiTro: 'NV_KINH_DOANH', tenVaiTro: 'Nhân viên kinh doanh', hoTen: 'Nguyễn Văn An' },
  { tenDangNhap: 'qlkd', matKhau: MAT_KHAU_DEMO, vaiTro: 'QL_KINH_DOANH', tenVaiTro: 'Quản lý kinh doanh', hoTen: 'Trần Thị Bích' },
  { tenDangNhap: 'nvkho', matKhau: MAT_KHAU_DEMO, vaiTro: 'NV_KHO', tenVaiTro: 'Nhân viên kho', hoTen: 'Lê Văn Cường' },
  { tenDangNhap: 'qlkho', matKhau: MAT_KHAU_DEMO, vaiTro: 'QL_KHO', tenVaiTro: 'Quản lý kho', hoTen: 'Phạm Thị Dung' },
  { tenDangNhap: 'nvdp', matKhau: MAT_KHAU_DEMO, vaiTro: 'NV_DIEU_PHOI', tenVaiTro: 'Nhân viên điều phối', hoTen: 'Hoàng Văn Em' },
  { tenDangNhap: 'qldp', matKhau: MAT_KHAU_DEMO, vaiTro: 'QL_DIEU_PHOI', tenVaiTro: 'Quản lý điều phối', hoTen: 'Vũ Thị Giang' },
  { tenDangNhap: 'nvgh', matKhau: MAT_KHAU_DEMO, vaiTro: 'NV_GIAO_HANG', tenVaiTro: 'Nhân viên giao hàng', hoTen: 'Đặng Văn Hải' },
  { tenDangNhap: 'nvns', matKhau: MAT_KHAU_DEMO, vaiTro: 'NV_NHAN_SU', tenVaiTro: 'Nhân viên nhân sự', hoTen: 'Đỗ Thị Kim' },
  { tenDangNhap: 'qlns', matKhau: MAT_KHAU_DEMO, vaiTro: 'QL_NHAN_SU', tenVaiTro: 'Quản lý nhân sự', hoTen: 'Ngô Văn Long' },
  { tenDangNhap: 'ktv', matKhau: MAT_KHAU_DEMO, vaiTro: 'KE_TOAN_VIEN', tenVaiTro: 'Kế toán viên', hoTen: 'Lý Thị Mai' },
  { tenDangNhap: 'thuquy', matKhau: MAT_KHAU_DEMO, vaiTro: 'THU_QUY', tenVaiTro: 'Thủ quỹ', hoTen: 'Trịnh Văn Nam' },
  { tenDangNhap: 'ktt', matKhau: MAT_KHAU_DEMO, vaiTro: 'KE_TOAN_TRUONG', tenVaiTro: 'Kế toán trưởng', hoTen: 'Phan Thị Oanh' },
  { tenDangNhap: 'nvthuong', matKhau: MAT_KHAU_DEMO, vaiTro: 'NHAN_VIEN', tenVaiTro: 'Nhân viên thường', hoTen: 'Võ Văn Phúc' },
]
