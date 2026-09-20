// Kiểu dữ liệu trao đổi với backend — khớp DTO của Spring Boot (không tự đặt lại tên).

/** Một dòng của ma trận phân quyền PHỤ LỤC B (mục 4.3). */
export interface QuyenBangDto {
  nhomBang: string
  /** Chữ nguyên văn của dòng trong ma trận, ví dụ "KhachHang, DichVu". */
  ten: string
  /** Rỗng = ô "–" của ma trận: không hiển thị và API cũng chặn. */
  hanhDong: string[]
}

/** Một quyền chức năng theo 3.x.2. */
export interface ChucNangDto {
  ma: string
  ten: string
  heThong: string
}

/** Payload của GET /api/quyen/cua-toi. */
export interface QuyenCuaToi {
  maTaiKhoan: string
  maNV: string
  tenDangNhap: string
  hoTen: string
  vaiTro: string
  tenVaiTro: string
  tenPhongBan: string | null
  tenChucVu: string | null
  heThong: string[]
  quyenBang: QuyenBangDto[]
  chucNang: ChucNangDto[]
}

export interface DangNhapResponse {
  accessToken: string
  loaiToken: string
  hetHanSauPhut: number
  nguoiDung: QuyenCuaToi
}

/** Cấu trúc lỗi thống nhất { maLoi, thongBao, chiTiet }. */
export interface ApiErrorBody {
  maLoi: string
  thongBao: string
  chiTiet: unknown
}
