import axios, { type AxiosError } from 'axios'
import type { ApiErrorBody } from './types'

console.log("BACKEND URL =", import.meta.env.VITE_BACKEND_URL);

const backendUrl = (import.meta.env.VITE_BACKEND_URL ?? '').trim().replace(/\/+$/, '')
const apiBaseUrl = backendUrl
  ? backendUrl.endsWith('/api') ? backendUrl : `${backendUrl}/api`
  : '/api'

/** Khoá lưu token của phiên làm việc trong localStorage. */
export const TOKEN_KEY = 'erp.accessToken'

export const http = axios.create({
  baseURL: apiBaseUrl,
  timeout: 20000,
  headers: { 'Content-Type': 'application/json' },
})

/**
 * Client riêng cho tải tệp: không đặt sẵn Content-Type để trình duyệt tự sinh
 * boundary của multipart/form-data.
 */
export const httpTep = axios.create({ baseURL: apiBaseUrl, timeout: 60000 })

function ganToken<T extends { headers: Record<string, unknown> }>(config: T): T {
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
}

http.interceptors.request.use(ganToken)
httpTep.interceptors.request.use(ganToken)

/** Tải một ảnh lên máy chủ, trả về đường dẫn tương đối để lưu vào CSDL. */
export async function taiAnh(file: File): Promise<string> {
  const form = new FormData()
  form.append('file', file)
  const { data } = await httpTep.post<{ duongDan: string }>('/tep/anh', form)
  return data.duongDan
}

let xuLyHetPhien: (() => void) | null = null

/** Đăng ký hành vi khi phiên hết hạn (401): xoá token và đưa về màn hình đăng nhập. */
export function dangKyHetPhien(fn: () => void) {
  xuLyHetPhien = fn
}

function xuLyLoi(error: AxiosError<ApiErrorBody>) {
  const url = error.config?.url ?? ''
  const laDangNhap = url.includes('/auth/dang-nhap')
  if (error.response?.status === 401 && !laDangNhap && xuLyHetPhien) {
    xuLyHetPhien()
  }
  return Promise.reject(error)
}

http.interceptors.response.use((response) => response, xuLyLoi)
httpTep.interceptors.response.use((response) => response, xuLyLoi)

/** Lấy câu thông báo nguyên văn backend trả về (từ messages_vi.properties). */
export function thongBaoLoi(error: unknown, macDinh = 'Hệ thống đang xử lý lỗi, vui lòng thử lại'): string {
  if (axios.isAxiosError(error)) {
    const duLieu = error.response?.data as ApiErrorBody | undefined
    if (duLieu?.thongBao) return duLieu.thongBao
    if (error.code === 'ERR_NETWORK') {
      return 'Không kết nối được máy chủ. Vui lòng kiểm tra kết nối hoặc thử lại sau.'
    }
    if (error.response?.status === 403) return 'Bạn không có quyền thực hiện thao tác này'
  }
  return macDinh
}

/** Mã lỗi (khoá trong messages_vi.properties) — dùng cho kiểm thử và xử lý riêng từng lỗi. */
export function maLoiCua(error: unknown): string | undefined {
  if (axios.isAxiosError(error)) {
    return (error.response?.data as ApiErrorBody | undefined)?.maLoi
  }
  return undefined
}
