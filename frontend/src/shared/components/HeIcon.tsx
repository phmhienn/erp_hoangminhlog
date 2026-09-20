interface HeIconProps {
  /** Mã hệ thống: HT0 (tổng quan) … HT5, khớp với MenuHeThong.he. */
  he: string
  className?: string
}

const DUONG_VE: Record<string, string> = {
  // Tổng quan — la bàn/trang chủ
  HT0: 'M11 3.5 3.5 11l1.4 1.4L11 6.3l6.1 6.1L18.5 11 11 3.5Zm-5.5 9V18a1 1 0 0 0 1 1h3v-4h3v4h3a1 1 0 0 0 1-1v-5.5',
  // HT1 — đơn hàng/dịch vụ: kiện hàng
  HT1: 'M11 2.6 3 6.6v8.8l8 4 8-4V6.6l-8-4Zm0 0v9.8m0 0L3 6.8m8 5.6 8-5.6m-8 9.8v-9.8',
  // HT2 — kho: kệ hàng
  HT2: 'M3 4.5h16M3 4.5v13h16v-13M3 10.5h16M3 16h16M7 4.5V10M13 4.5V10M7 12v4M13 12v4',
  // HT3 — điều phối & giao hàng: xe tải
  HT3: 'M2.5 6.5h9v8h-9v-8Zm9 3h3.6l2.4 2.6v2.4h-6v-5Zm-6.6 8a1.6 1.6 0 1 0 0-3.2 1.6 1.6 0 0 0 0 3.2Zm9.6 0a1.6 1.6 0 1 0 0-3.2 1.6 1.6 0 0 0 0 3.2Z',
  // HT4 — nhân sự: người
  HT4: 'M11 11a3.6 3.6 0 1 0 0-7.2 3.6 3.6 0 0 0 0 7.2Zm-6.5 8v-1.4A5 5 0 0 1 9 12.8h4a5 5 0 0 1 4.5 4.8V19',
  // HT5 — thu chi & COD: ví tiền
  HT5: 'M3 6.8h13.5a1.7 1.7 0 0 1 1.7 1.7v7a1.7 1.7 0 0 1-1.7 1.7H5a2 2 0 0 1-2-2V6.8Zm0 0a2 2 0 0 1 2-2h10M13.6 11.3h.01',
}

/** Icon đơn nét, đồng bộ theo mã hệ thống — dùng ở sidebar và trang đăng nhập. */
export function HeIcon({ he, className }: HeIconProps) {
  const d = DUONG_VE[he] ?? DUONG_VE.HT0
  return (
    <svg
      className={className ?? 'icon'}
      width="16"
      height="16"
      viewBox="0 0 22 22"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.6"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <path d={d} />
    </svg>
  )
}
