type Mau = 'trung-tinh' | 'thanh-cong' | 'canh-bao' | 'loi' | 'dang-xu-ly'

/**
 * Ánh xạ trạng thái -> màu nhãn.
 *
 * NHÓM 1: ENUM {@code DonHang.trangThai} của mục 4.2 (PHỤ LỤC C) — không thêm giá trị mới.
 * NHÓM 2: nhãn hiển thị của đặc tả 3.3.3 ("Chờ tiếp nhận", "Đang giao hàng", "Đã hoàn thành");
 *         đây chỉ là ánh xạ trình bày của các mốc tương ứng trong ENUM.
 * NHÓM 3: trạng thái của chứng từ/danh mục (phiếu nhập, tài khoản, phương tiện…).
 */
const MAU_TRANG_THAI: Record<string, Mau> = {
  // ENUM 4.2 — DonHang.trangThai
  'Đã tạo': 'dang-xu-ly',
  'Đã nhập kho': 'canh-bao',
  'Đang giao': 'dang-xu-ly',
  'Đã giao': 'thanh-cong',
  'Đã đối soát': 'thanh-cong',
  'Đã hủy': 'loi',

  // Nhãn hiển thị của 3.3.3 (ánh xạ của ENUM, không phải trạng thái mới)
  'Chờ tiếp nhận': 'canh-bao',
  'Đang giao hàng': 'dang-xu-ly',
  'Đã hoàn thành': 'thanh-cong',

  // Chứng từ / danh mục
  'Bản nháp': 'trung-tinh',
  'Chờ duyệt': 'canh-bao',
  'Đã duyệt': 'thanh-cong',
  'Từ chối': 'loi',
  'Đã khóa': 'loi',
  'Hoạt động': 'thanh-cong',
  'Sẵn sàng': 'thanh-cong',
  'Đang bảo trì': 'canh-bao',
  'Đang làm việc': 'thanh-cong',
  'Thử việc': 'canh-bao',
  'Nghỉ việc': 'loi',
  'Đang kinh doanh': 'thanh-cong',
  'Ngừng kinh doanh': 'loi',
  'Chờ xử lý': 'canh-bao',
  'Đang xử lý': 'dang-xu-ly',
  'Đã xử lý': 'thanh-cong',
  'Lưu tạm': 'trung-tinh',
  'Đã xuất': 'thanh-cong',

  // Vòng đời chuyến vận chuyển (HT3)
  'Nháp': 'trung-tinh',
  'Cần điều chỉnh': 'canh-bao',
  'Đã phân công': 'dang-xu-ly',
  'Đã phê duyệt': 'thanh-cong',
  'Hoàn thành': 'thanh-cong',
}

interface StatusBadgeProps {
  trangThai: string | null | undefined
  /** Ghi đè nhãn hiển thị (ví dụ nhãn 3.3.3 của một mốc trong ENUM 4.2). */
  nhan?: string
}

/** Nhãn trạng thái thống nhất của component library nội bộ. */
export function StatusBadge({ trangThai, nhan }: StatusBadgeProps) {
  if (!trangThai) return <span className="badge khong-co-quyen">—</span>
  const mau = MAU_TRANG_THAI[trangThai] ?? 'trung-tinh'
  return <span className={`badge ${mau}`}>{nhan ?? trangThai}</span>
}
