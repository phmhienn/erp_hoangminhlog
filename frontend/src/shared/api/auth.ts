import { http } from './http'
import type { DangNhapResponse, QuyenCuaToi } from './types'

/** Đăng nhập bằng bảng dùng chung TaiKhoan (nhiệm vụ 0.4). */
export async function dangNhap(tenDangNhap: string, matKhau: string): Promise<DangNhapResponse> {
  const { data } = await http.post<DangNhapResponse>('/auth/dang-nhap', { tenDangNhap, matKhau })
  return data
}

/** Đọc ma trận PHỤ LỤC B của chính người dùng đang đăng nhập. */
export async function layQuyenCuaToi(): Promise<QuyenCuaToi> {
  const { data } = await http.get<QuyenCuaToi>('/quyen/cua-toi')
  return data
}
