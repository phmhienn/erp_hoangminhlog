import type { ReactNode } from 'react'
import { Navigate, useLocation } from 'react-router-dom'
import { usePermission } from '@/shared/permission/PermissionContext'

/** Chặn vào vùng nghiệp vụ khi chưa đăng nhập (phiên làm việc theo JWT — nhiệm vụ 0.4). */
export function ProtectedRoute({ children }: { children: ReactNode }) {
  const { nguoiDung } = usePermission()
  const location = useLocation()

  if (!nguoiDung) {
    return <Navigate to="/dang-nhap" replace state={{ tu: location.pathname }} />
  }
  return <>{children}</>
}
