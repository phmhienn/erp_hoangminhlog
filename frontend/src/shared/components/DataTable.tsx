import type { ReactNode } from 'react'

/** Một cột của bảng. `render` trả về nội dung ô; mặc định hiển thị giá trị của `key`. */
export interface Cot<T> {
  key: string
  tieuDe: string
  render?: (dong: T) => ReactNode
  /** Căn phải + font số cho cột tiền tệ/số lượng. */
  so?: boolean
  width?: string
}

interface DataTableProps<T> {
  cot: Cot<T>[]
  duLieu: T[]
  /** Hàm lấy khoá của một dòng (React key). */
  khoa: (dong: T) => string
  /** Tiêu đề khối trạng thái rỗng — đúng luồng phụ "không có dữ liệu thì thông báo". */
  trongTieuDe?: string
  trongNoiDung?: ReactNode
}

/**
 * Bảng dữ liệu dùng chung của component library nội bộ (mục 4 Frontend).
 * Mọi màn hình danh sách của 5 hệ thống dùng chung component này để nhất quán.
 */
export function DataTable<T>({
  cot,
  duLieu,
  khoa,
  trongTieuDe = 'Không có dữ liệu',
  trongNoiDung,
}: DataTableProps<T>) {
  if (duLieu.length === 0) {
    return (
      <div className="empty-state">
        <div className="tieu-de">{trongTieuDe}</div>
        {trongNoiDung ? <div>{trongNoiDung}</div> : null}
      </div>
    )
  }

  return (
    <div className="table-wrap">
      <table className="data-table">
        <thead>
          <tr>
            {cot.map((c) => (
              <th
                key={c.key}
                className={c.so ? 'so' : undefined}
                style={c.width ? { width: c.width } : undefined}
              >
                {c.tieuDe}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {duLieu.map((dong) => (
            <tr key={khoa(dong)}>
              {cot.map((c) => (
                <td key={c.key} className={c.so ? 'so' : undefined}>
                  {c.render
                    ? c.render(dong)
                    : ((dong as unknown as Record<string, ReactNode>)[c.key] ?? '')}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
