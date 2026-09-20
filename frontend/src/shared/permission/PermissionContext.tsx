import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'
import { dangNhap as apiDangNhap, layQuyenCuaToi } from '@/shared/api/auth'
import { TOKEN_KEY, dangKyHetPhien } from '@/shared/api/http'
import type { QuyenCuaToi } from '@/shared/api/types'

const NGUOI_DUNG_KEY = 'erp.nguoiDung'

interface PermissionContextValue {
  nguoiDung: QuyenCuaToi | null
  dangNhap: (tenDangNhap: string, matKhau: string) => Promise<void>
  dangXuat: () => void
  /** Quyền chức năng theo 3.x.2 — quyết định hiển thị menu và nút thao tác. */
  coChucNang: (ma: string) => boolean
  /** Quyền trên bảng theo ma trận PHỤ LỤC B; ô "–" trả về false. */
  coQuyen: (nhomBang: string, hanhDong: string) => boolean
  thuocHeThong: (heThong: string) => boolean
}

const PermissionContext = createContext<PermissionContextValue | null>(null)

function docTuLocalStorage(): QuyenCuaToi | null {
  try {
    const raw = localStorage.getItem(NGUOI_DUNG_KEY)
    return raw ? (JSON.parse(raw) as QuyenCuaToi) : null
  } catch {
    return null
  }
}

export function PermissionProvider({ children }: { children: ReactNode }) {
  const [nguoiDung, setNguoiDung] = useState<QuyenCuaToi | null>(() => {
    // Chỉ khôi phục phiên khi còn token; quyền sẽ được đọc lại từ backend bên dưới
    return localStorage.getItem(TOKEN_KEY) ? docTuLocalStorage() : null
  })

  const luuPhien = useCallback((token: string, nd: QuyenCuaToi) => {
    localStorage.setItem(TOKEN_KEY, token)
    localStorage.setItem(NGUOI_DUNG_KEY, JSON.stringify(nd))
    setNguoiDung(nd)
  }, [])

  const dangXuat = useCallback(() => {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(NGUOI_DUNG_KEY)
    setNguoiDung(null)
  }, [])

  // Phiên hết hạn (401) -> xoá phiên, màn hình sẽ tự đưa về trang đăng nhập
  useEffect(() => {
    dangKyHetPhien(dangXuat)
  }, [dangXuat])

  // Tải lại quyền từ backend khi mở ứng dụng với token còn trong máy
  useEffect(() => {
    if (!localStorage.getItem(TOKEN_KEY)) return
    let huy = false
    layQuyenCuaToi()
      .then((nd) => {
        if (huy) return
        localStorage.setItem(NGUOI_DUNG_KEY, JSON.stringify(nd))
        setNguoiDung(nd)
      })
      .catch(() => {
        if (!huy) dangXuat()
      })
    return () => {
      huy = true
    }
  }, [dangXuat])

  const dangNhap = useCallback(
    async (tenDangNhap: string, matKhau: string) => {
      const ketQua = await apiDangNhap(tenDangNhap, matKhau)
      luuPhien(ketQua.accessToken, ketQua.nguoiDung)
    },
    [luuPhien],
  )

  const value = useMemo<PermissionContextValue>(() => {
    const chucNang = new Set((nguoiDung?.chucNang ?? []).map((c) => c.ma))
    const bang = new Map((nguoiDung?.quyenBang ?? []).map((q) => [q.nhomBang, new Set(q.hanhDong)]))
    const heThong = new Set(nguoiDung?.heThong ?? [])
    return {
      nguoiDung,
      dangNhap,
      dangXuat,
      coChucNang: (ma) => chucNang.has(ma),
      coQuyen: (nhomBang, hanhDong) => bang.get(nhomBang)?.has(hanhDong) ?? false,
      thuocHeThong: (ht) => heThong.has(ht),
    }
  }, [nguoiDung, dangNhap, dangXuat])

  return <PermissionContext.Provider value={value}>{children}</PermissionContext.Provider>
}

/**
 * Hook phân quyền của frontend (mục 4 Frontend): đọc ma trận PHỤ LỤC B từ backend
 * qua {@code GET /api/quyen/cua-toi}; menu và nút thao tác render theo quyền.
 */
export function usePermission(): PermissionContextValue {
  const ctx = useContext(PermissionContext)
  if (!ctx) throw new Error('usePermission phai nam trong <PermissionProvider>')
  return ctx
}
