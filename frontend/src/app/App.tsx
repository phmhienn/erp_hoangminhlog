import { Navigate, Route, Routes } from 'react-router-dom'
import { Layout } from '@/app/Layout'
import { LoginPage } from '@/app/LoginPage'
import { ProtectedRoute } from '@/app/ProtectedRoute'
import { TrangChu } from '@/app/TrangChu'
import { MENU } from '@/app/menu'
import { KhachHangPage } from '@/features/ht1/KhachHangPage'
import { DonHangPage } from '@/features/ht1/DonHangPage'
import { BaoCaoPage } from '@/features/ht1/BaoCaoPage'
import { Ht2Page } from '@/features/ht2/Ht2Page'
import { Ht3Page } from '@/features/ht3/Ht3Page'
import { Ht4Page } from '@/features/ht4/Ht4Page'
import { Ht5Page } from '@/features/ht5/Ht5Page'
import { usePermission } from '@/shared/permission/PermissionContext'

/**
 * Định tuyến theo 5 phân hệ (mục 4 Frontend). Mỗi đường dẫn của một màn hình nghiệp vụ
 * chỉ tồn tại nếu người dùng có quyền chức năng tương ứng — người không có quyền
 * không thấy menu và khi gõ thẳng URL sẽ gặp màn hình "không có quyền".
 */
export function App() {
  const { nguoiDung, coChucNang } = usePermission()

  const duocPhepTruyCap = MENU.flatMap((he) => he.muc).filter((m) => coChucNang(m.ma))

  return (
    <Routes>
      <Route
        path="/dang-nhap"
        element={nguoiDung ? <Navigate to="/" replace /> : <LoginPage />}
      />

      <Route
        element={
          <ProtectedRoute>
            <Layout />
          </ProtectedRoute>
        }
      >
        <Route path="/" element={<TrangChu />} />

        {duocPhepTruyCap.map((muc) => (
          <Route key={muc.ma} path={muc.duongDan} element={
            muc.duongDan === '/ht1/khach-hang' ? <KhachHangPage /> :
            muc.duongDan === '/ht1/bao-cao' ? <BaoCaoPage /> :
            muc.duongDan.startsWith('/ht1/') ? <DonHangPage key={muc.ma} mode={muc.duongDan.split('/')[2] as 'don-hang'|'kiem-duyet'|'tra-cuu'|'theo-doi'} /> :
            muc.duongDan.startsWith('/ht2/') ? <Ht2Page key={muc.ma} mode={muc.duongDan.split('/')[2]} /> :
            muc.duongDan.startsWith('/ht3/') ? <Ht3Page key={muc.ma} mode={muc.duongDan.split('/')[2]} /> : muc.duongDan.startsWith('/ht4/') ? <Ht4Page key={muc.ma} mode={muc.duongDan.split('/')[2]} /> : muc.duongDan.startsWith('/ht5/') ? <Ht5Page key={muc.ma} mode={muc.duongDan.split('/')[2]} /> : <KhongCoQuyen />
          } />
        ))}

        <Route path="*" element={<KhongCoQuyen />} />
      </Route>
    </Routes>
  )
}

/** Đường dẫn không thuộc quyền của vai trò đang đăng nhập (ô "–" của ma trận PHỤ LỤC B). */
function KhongCoQuyen() {
  return (
    <>
      <div className="page-header">
        <h1 className="page-title">Không có quyền truy cập</h1>
        <p className="page-sub">
          Chức năng này không thuộc quyền của vai trò đang đăng nhập theo ma trận phân quyền
          PHỤ LỤC B (mục 4.3). API phía backend cũng trả về 403 cho yêu cầu tương ứng.
        </p>
      </div>
      <div className="card">
        <div className="card-body">
          <div className="alert alert-loi">Bạn không có quyền thực hiện thao tác này</div>
        </div>
      </div>
    </>
  )
}
