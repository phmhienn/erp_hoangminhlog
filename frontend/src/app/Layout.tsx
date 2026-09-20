import { NavLink, Outlet, useLocation } from 'react-router-dom'
import { MENU, MENU_CHUNG, type MenuItem } from '@/app/menu'
import { usePermission } from '@/shared/permission/PermissionContext'
import { HeIcon } from '@/shared/components/HeIcon'

function vietTat(hoTen: string): string {
  const tu = hoTen.trim().split(/\s+/)
  if (tu.length === 0) return '?'
  const ho = tu[tu.length - 1]?.charAt(0) ?? '?'
  const dem = tu.slice(0, -1).map((t) => t.charAt(0)).join('.')
  return dem ? `${ho}.${dem}` : ho
}

function MucMenu({ muc }: { muc: MenuItem }) {
  return (
    <NavLink
      to={muc.duongDan}
      end={muc.duongDan === '/'}
      className={({ isActive }) => `nav-item${isActive ? ' active' : ''}`}
    >
      <span className="ten-muc">{muc.ten}</span>
    </NavLink>
  )
}

/** Khung ứng dụng: sidebar theo quyền + topbar + vùng nội dung (client–server qua trình duyệt). */
export function Layout() {
  const { nguoiDung, dangXuat, coChucNang } = usePermission()
  const location = useLocation()

  if (!nguoiDung) return null

  const mucHienTai =
    [...MENU_CHUNG, ...MENU.flatMap((he) => he.muc)].find((m) => m.duongDan === location.pathname) ??
    null

  const nhomHeThong = MENU.map((he) => ({
    ...he,
    muc: he.muc.filter((m) => coChucNang(m.ma)),
  })).filter((he) => he.muc.length > 0)

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="sidebar-brand">
          <div className="brand-mark">HM</div>
          <div>
            <div className="ten-cong-ty">ERP Hoàng Minh</div>
            <div className="mo-ta">Logistics giao nhận — chương trình thử nghiệm</div>
          </div>
        </div>

        <nav className="sidebar-nav">
          <div className="nav-group">
            <div className="nav-group-title">
              <HeIcon he="HT0" />
              <span className="ma-he">HT0</span> Tổng quan
            </div>
            {MENU_CHUNG.map((muc) => (
              <MucMenu key={muc.ma} muc={muc} />
            ))}
          </div>

          {nhomHeThong.map((he) => (
            <div className="nav-group" key={he.he}>
              <div className="nav-group-title" title={`Phân hệ ${he.phanHe}`}>
                <HeIcon he={he.he} />
                <span className="ma-he">{he.he}</span> {he.ten}
              </div>
              {he.muc.map((muc) => (
                <MucMenu key={muc.ma} muc={muc} />
              ))}
            </div>
          ))}
        </nav>

      </aside>

      <div className="main">
        <header className="topbar">
          <div className="duong-dan">
            {mucHienTai ? mucHienTai.ten : 'ERP Hoàng Minh'}
          </div>

          <div className="can-phai">
            <div className="user-chip">
              <div className="avatar">{vietTat(nguoiDung.hoTen)}</div>
              <div>
                <div className="ten">{nguoiDung.hoTen}</div>
                <div className="vai-tro">
                  {nguoiDung.tenVaiTro}
                  {nguoiDung.tenPhongBan ? ` · ${nguoiDung.tenPhongBan}` : ''}
                </div>
              </div>
            </div>
            <button className="btn btn-nho" type="button" onClick={dangXuat}>
              Đăng xuất
            </button>
          </div>
        </header>

        <div className="page">
          <Outlet />
        </div>
      </div>
    </div>
  )
}
