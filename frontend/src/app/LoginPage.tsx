import { useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { TAI_KHOAN_DEMO, type TaiKhoanDemo } from '@/app/demoAccounts'
import { CHUOI_CHINH, DOANH_NGHIEP, HE_THONG_NEN } from '@/app/chuoiNghiepVu'
import { thongBaoLoi } from '@/shared/api/http'
import { usePermission } from '@/shared/permission/PermissionContext'
import { HeIcon } from '@/shared/components/HeIcon'

/** Nhóm tài khoản demo theo phân hệ để người thử nghiệm chọn đúng vai trò cần xem. */
const NHOM_TAI_KHOAN: { ten: string; ma: string; taiKhoan: string[] }[] = [
  { ten: 'Kinh doanh', ma: 'HT1', taiKhoan: ['nvkd', 'qlkd'] },
  { ten: 'Kho vận', ma: 'HT2', taiKhoan: ['nvkho', 'qlkho'] },
  { ten: 'Vận tải – Giao nhận', ma: 'HT3', taiKhoan: ['nvdp', 'qldp', 'nvgh'] },
  { ten: 'Nhân sự', ma: 'HT4', taiKhoan: ['nvns', 'qlns', 'nvthuong'] },
  { ten: 'Tài chính – Kế toán', ma: 'HT5', taiKhoan: ['ktv', 'thuquy', 'ktt'] },
]

/**
 * Màn hình đăng nhập — nhiệm vụ 0.4: đăng nhập bằng bảng dùng chung {@code TaiKhoan};
 * tài khoản có {@code trangThai} khác hoạt động bị từ chối (backend trả 403 kèm câu thông báo).
 */
export function LoginPage() {
  const { dangNhap } = usePermission()
  const navigate = useNavigate()

  const [tenDangNhap, setTenDangNhap] = useState('')
  const [matKhau, setMatKhau] = useState('')
  const [loi, setLoi] = useState<string | null>(null)
  const [dangXuLy, setDangXuLy] = useState(false)

  const theoTen = new Map<string, TaiKhoanDemo>(TAI_KHOAN_DEMO.map((t) => [t.tenDangNhap, t]))

  async function xuLyDangNhap(ten: string, mk: string) {
    setDangXuLy(true)
    setLoi(null)
    try {
      await dangNhap(ten, mk)
      navigate('/', { replace: true })
    } catch (e) {
      setLoi(thongBaoLoi(e, 'Đăng nhập không thành công'))
    } finally {
      setDangXuLy(false)
    }
  }

  function onSubmit(e: FormEvent) {
    e.preventDefault()
    void xuLyDangNhap(tenDangNhap, matKhau)
  }

  return (
    <div className="login-page">
      <aside className="login-aside">
        <div className="login-brand">
          <div className="brand-mark">HM</div>
          <div>
            <div className="ten-thuong-hieu">{DOANH_NGHIEP.thuongHieu}</div>
            <div className="nhan">Hệ thống hoạch định nguồn lực doanh nghiệp</div>
          </div>
        </div>

        <div>
          <h1>Một đơn hàng, năm hệ thống, một cơ sở dữ liệu dùng chung</h1>
          <p>
            Chương trình thử nghiệm ERP cho {DOANH_NGHIEP.tenDayDu}, minh hoạ dòng dữ liệu xuyên suốt từ lúc
            tiếp nhận yêu cầu của khách hàng đến khi đối soát xong tiền thu hộ.
          </p>
        </div>

        <ol className="chuoi-dang-nhap">
          {CHUOI_CHINH.map((he) => (
            <li key={he.ma}>
              <span className="icon-he"><HeIcon he={he.ma} /></span>
              <span className="ma">{he.ma}</span>
              <span className="ten">{he.ten}</span>
              <span className="phan-he">{he.phanHe}</span>
            </li>
          ))}
          <li className="nen">
            <span className="icon-he"><HeIcon he={HE_THONG_NEN.ma} /></span>
            <span className="ma">{HE_THONG_NEN.ma}</span>
            <span className="ten">{HE_THONG_NEN.ten}</span>
            <span className="phan-he">dữ liệu nền cho HT2 &amp; HT3</span>
          </li>
        </ol>

        <p className="chan-trang">
          MST {DOANH_NGHIEP.maSoThue} · {DOANH_NGHIEP.diaChi}
        </p>
      </aside>

      <main className="login-main">
        <div className="login-card">
          <h2>Đăng nhập hệ thống</h2>
          <p className="mo-ta">
            Dùng tài khoản trong bảng <span className="mono">TaiKhoan</span>. Mật khẩu demo của mọi
            tài khoản: <span className="mono">123456</span>
          </p>

          {loi ? (
            <div className="alert alert-loi" role="alert">
              {loi}
            </div>
          ) : null}

          <form className="login-form" onSubmit={onSubmit}>
            <div className="field">
              <label className="label" htmlFor="tenDangNhap">
                Tên đăng nhập <span className="bat-buoc">*</span>
              </label>
              <input
                id="tenDangNhap"
                className="input"
                value={tenDangNhap}
                onChange={(e) => setTenDangNhap(e.target.value)}
                placeholder="nvkd"
                autoComplete="username"
                autoFocus
                maxLength={50}
                required
              />
            </div>

            <div className="field">
              <label className="label" htmlFor="matKhau">
                Mật khẩu <span className="bat-buoc">*</span>
              </label>
              <input
                id="matKhau"
                className="input"
                type="password"
                value={matKhau}
                onChange={(e) => setMatKhau(e.target.value)}
                placeholder="123456"
                autoComplete="current-password"
                maxLength={255}
                required
              />
            </div>

            <button className="btn btn-chinh btn-khoi" type="submit" disabled={dangXuLy}>
              {dangXuLy ? 'Đang đăng nhập…' : 'Đăng nhập'}
            </button>
          </form>

          <div className="demo-accounts">
            <div className="tieu-de">Tài khoản theo vai trò — bấm để vào thẳng</div>
            <div className="danh-sach-demo" tabIndex={0} role="group" aria-label="Danh sách tài khoản demo theo phân hệ">
              {NHOM_TAI_KHOAN.map((nhom) => (
                <section className="nhom-demo" key={nhom.ma}>
                  <h3>
                    <span className="ma-he">{nhom.ma}</span> {nhom.ten}
                  </h3>
                  <div className="demo-grid">
                    {nhom.taiKhoan.map((ten) => {
                      const tk = theoTen.get(ten)
                      if (!tk) return null
                      return (
                        <button
                          key={tk.tenDangNhap}
                          type="button"
                          className="demo-chip"
                          title={`${tk.hoTen} — ${tk.tenVaiTro}`}
                          onClick={() => {
                            setTenDangNhap(tk.tenDangNhap)
                            setMatKhau(tk.matKhau)
                            setLoi(null)
                            void xuLyDangNhap(tk.tenDangNhap, tk.matKhau)
                          }}
                          disabled={dangXuLy}
                        >
                          <span className="tk">{tk.tenDangNhap}</span>
                          <span className="vt">{tk.tenVaiTro}</span>
                        </button>
                      )
                    })}
                  </div>
                </section>
              ))}
            </div>
          </div>
        </div>
      </main>
    </div>
  )
}
