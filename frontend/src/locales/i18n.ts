import vi from './vi.json'

const NGUON: Record<string, string> = vi

/**
 * Lấy chuỗi hiển thị tiếng Việt theo mã.
 *
 * <p>Mã trong {@code vi.json} đồng bộ 1-1 với khoá của {@code messages_vi.properties} ở backend
 * (mục 4 Frontend): một thông báo chỉ có một nguồn, backend trả {@code maLoi} thì frontend
 * tra đúng khoá đó khi cần hiển thị lại mà không hard-code chuỗi.
 */
export function t(ma: string, macDinh?: string): string {
  return NGUON[ma] ?? macDinh ?? ma
}
