import { useEffect, useState, type FormEvent, type ReactNode } from 'react'
import { http, thongBaoLoi } from '@/shared/api/http'
import { StatusBadge } from '@/shared/components/StatusBadge'

interface HoSo {
  maNV: string; hoTen: string; ngaySinh?: string | null; gioiTinh?: string | null; soDienThoai?: string | null
  diaChi?: string | null; email?: string | null; maPhongBan?: string | null; tenPhongBan?: string | null
  maChucVu?: string | null; tenChucVu?: string | null; maTrangThai?: string | null; tenTrangThai?: string | null; ngayVaoLam?: string | null
}
interface YeuCau { maYeuCau: number; maNV: string; hoTen?: string | null; noiDung: string; trangThai: string; thoiGianGui: string; nguoiXuLy?: string | null; thoiGianXuLy?: string | null; phanHoi?: string | null }

const rong = { hoTen: '', ngaySinh: '', gioiTinh: '', soDienThoai: '', diaChi: '', email: '', maPhongBan: '', maChucVu: '', maTrangThai: '', ngayVaoLam: '' }
type Form = typeof rong
const gio = (s?: string | null) => (s ? new Date(s).toLocaleString('vi-VN') : '—')

const TIEU_DE: Record<string, [string, string]> = {
  'ho-so': ['Danh sách hồ sơ nhân viên', 'Tìm theo mã, họ tên, phòng ban, chức vụ hoặc trạng thái; mở hồ sơ để xem đầy đủ thông tin.'],
  'them': ['Thêm hồ sơ nhân viên', 'Tạo hồ sơ mới; hệ thống tự cấp mã nhân viên dùng chung cho kho, điều phối và kế toán.'],
  'trang-thai': ['Quản lý trạng thái nhân viên', 'Theo dõi nhân sự theo trạng thái làm việc và xử lý yêu cầu cập nhật thông tin.'],
  'ca-nhan': ['Hồ sơ của tôi', 'Xem thông tin cá nhân và gửi yêu cầu cập nhật khi có thay đổi.'],
}

export function Ht4Page({ mode }: { mode: string }) {
  const [ds, setDs] = useState<HoSo[]>([])
  const [tatCa, setTatCa] = useState<HoSo[]>([])
  const [me, setMe] = useState<HoSo | null>(null)
  const [chiTiet, setChiTiet] = useState<HoSo | null>(null)
  const [yeuCau, setYeuCau] = useState<YeuCau[]>([])
  const [loc, setLoc] = useState({ tuKhoa: '', maPhongBan: '', maChucVu: '', maTrangThai: '' })
  const [form, setForm] = useState<Form>(rong)
  const [suaId, setSuaId] = useState('')
  const [moForm, setMoForm] = useState(false)
  const [doiTrangThai, setDoiTrangThai] = useState<{ maNV: string; hoTen: string; maTrangThai: string } | null>(null)
  const [noiDung, setNoiDung] = useState('')
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)
  const [loi, setLoi] = useState('')
  const [ok, setOk] = useState('')

  async function tai() {
    setLoading(true); setLoi('')
    try {
      if (mode === 'ca-nhan') {
        setMe((await http.get<HoSo>('/ht4/ho-so-cua-toi')).data)
        try { setYeuCau((await http.get<YeuCau[]>('/ht4/yeu-cau-cap-nhat')).data) } catch { setYeuCau([]) }
      } else if (mode === 'them') {
        setTatCa((await http.get<HoSo[]>('/ht4/ho-so')).data)
      } else {
        const params = Object.fromEntries(Object.entries(loc).filter(([, v]) => v !== ''))
        const [rows, all] = await Promise.all([
          http.get<HoSo[]>('/ht4/ho-so', { params }),
          tatCa.length ? Promise.resolve({ data: tatCa }) : http.get<HoSo[]>('/ht4/ho-so'),
        ])
        setDs(rows.data); setTatCa(all.data)
        if (mode === 'trang-thai') { try { setYeuCau((await http.get<YeuCau[]>('/ht4/yeu-cau-cap-nhat')).data) } catch { setYeuCau([]) } }
      }
    } catch (e) { setLoi(thongBaoLoi(e)) } finally { setLoading(false) }
  }
  useEffect(() => { setOk(''); setChiTiet(null); setMoForm(mode === 'them'); setSuaId(''); setForm(rong); void tai() }, [mode])

  async function chay(viec: () => Promise<void>, thongBao: string) {
    setBusy(true); setLoi(''); setOk('')
    try { await viec(); setOk(thongBao); await tai() }
    catch (e) { setLoi(thongBaoLoi(e)) } finally { setBusy(false) }
  }

  const danhMuc = (lay: (h: HoSo) => [string | null | undefined, string | null | undefined]) => {
    const map = new Map<string, string>()
    for (const h of tatCa) { const [ma, ten] = lay(h); if (ma) map.set(ma, ten ?? ma) }
    return [...map.entries()]
  }
  const phongBan = danhMuc((h) => [h.maPhongBan, h.tenPhongBan])
  const chucVu = danhMuc((h) => [h.maChucVu, h.tenChucVu])
  const trangThai = danhMuc((h) => [h.maTrangThai, h.tenTrangThai])

  function moSua(h: HoSo) {
    setSuaId(h.maNV)
    setForm({
      hoTen: h.hoTen ?? '', ngaySinh: h.ngaySinh ?? '', gioiTinh: h.gioiTinh ?? '', soDienThoai: h.soDienThoai ?? '',
      diaChi: h.diaChi ?? '', email: h.email ?? '', maPhongBan: h.maPhongBan ?? '', maChucVu: h.maChucVu ?? '',
      maTrangThai: h.maTrangThai ?? '', ngayVaoLam: h.ngayVaoLam ?? '',
    })
    setMoForm(true); setChiTiet(null); setOk('')
  }

  const body = () => {
    const payload: Record<string, string | null> = {}
    for (const [k, v] of Object.entries(form)) payload[k] = v === '' ? null : v
    payload.hoTen = form.hoTen
    return payload
  }

  const formHoSo = (
    <form className="ht1-editor" onSubmit={(e: FormEvent) => {
      e.preventDefault()
      void chay(async () => {
        if (suaId) await http.put(`/ht4/ho-so/${suaId}`, body())
        else await http.post('/ht4/ho-so', body())
        setMoForm(mode === 'them'); setSuaId(''); setForm(rong)
      }, suaId ? 'Sửa hồ sơ nhân viên thành công' : 'Thêm hồ sơ nhân viên thành công')
    }}>
      <h2>{suaId ? `Cập nhật hồ sơ ${suaId}` : 'Hồ sơ nhân viên mới'}</h2>
      <div className="form-grid">
        <label>Họ tên *<input className="input" required maxLength={100} value={form.hoTen} onChange={(e) => setForm({ ...form, hoTen: e.target.value })} /></label>
        <label>Ngày sinh<input className="input" type="date" value={form.ngaySinh} onChange={(e) => setForm({ ...form, ngaySinh: e.target.value })} /></label>
        <label>Giới tính<select className="select" value={form.gioiTinh} onChange={(e) => setForm({ ...form, gioiTinh: e.target.value })}>
          <option value="">—</option><option>Nam</option><option>Nữ</option></select></label>
        <label>Số điện thoại<input className="input" maxLength={15} value={form.soDienThoai} onChange={(e) => setForm({ ...form, soDienThoai: e.target.value })} /></label>
        <label>Email<input className="input" type="email" maxLength={100} value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} /></label>
        <label>Ngày vào làm<input className="input" type="date" value={form.ngayVaoLam} onChange={(e) => setForm({ ...form, ngayVaoLam: e.target.value })} /></label>
        <label>Phòng ban<select className="select" value={form.maPhongBan} onChange={(e) => setForm({ ...form, maPhongBan: e.target.value })}>
          <option value="">— Chưa gán —</option>{phongBan.map(([ma, ten]) => <option key={ma} value={ma}>{ten}</option>)}</select></label>
        <label>Chức vụ<select className="select" value={form.maChucVu} onChange={(e) => setForm({ ...form, maChucVu: e.target.value })}>
          <option value="">— Chưa gán —</option>{chucVu.map(([ma, ten]) => <option key={ma} value={ma}>{ten}</option>)}</select></label>
        <label>Trạng thái<select className="select" value={form.maTrangThai} onChange={(e) => setForm({ ...form, maTrangThai: e.target.value })}>
          <option value="">— Chưa gán —</option>{trangThai.map(([ma, ten]) => <option key={ma} value={ma}>{ten}</option>)}</select></label>
        <label className="form-span">Địa chỉ<input className="input" maxLength={255} value={form.diaChi} onChange={(e) => setForm({ ...form, diaChi: e.target.value })} /></label>
      </div>
      <div className="form-actions">
        <button className="btn btn-chinh" disabled={busy}>{suaId ? 'Lưu thay đổi' : 'Thêm hồ sơ'}</button>
        {mode !== 'them' && <button type="button" className="btn" onClick={() => { setMoForm(false); setSuaId('') }}>Đóng</button>}
      </div>
    </form>
  )

  const [tieuDe, moTa] = TIEU_DE[mode] ?? TIEU_DE['ho-so']

  return <>
    <div className="page-header"><h1 className="page-title">{tieuDe}</h1><p className="page-sub">{moTa}</p></div>
    {loi && <div role="alert" className="alert alert-loi">{loi}</div>}
    {ok && <div role="status" className="alert alert-thanh-cong">{ok}</div>}

    {/* ---------- Thêm hồ sơ: chỉ biểu mẫu ---------- */}
    {mode === 'them' && <div className="card"><div className="card-body">{formHoSo}</div></div>}

    {/* ---------- Danh sách hồ sơ: tìm kiếm nhiều tiêu chí + xem chi tiết ---------- */}
    {mode === 'ho-so' && <>
      <div className="card"><div className="card-body">
        <form className="search-filter" onSubmit={(e) => { e.preventDefault(); void tai() }}>
          <label className="field">Mã hoặc họ tên<input className="input" placeholder="NV0000001 / Nguyễn Văn An" value={loc.tuKhoa} onChange={(e) => setLoc({ ...loc, tuKhoa: e.target.value })} /></label>
          <label className="field">Phòng ban<select className="select" value={loc.maPhongBan} onChange={(e) => setLoc({ ...loc, maPhongBan: e.target.value })}>
            <option value="">Tất cả</option>{phongBan.map(([ma, ten]) => <option key={ma} value={ma}>{ten}</option>)}</select></label>
          <label className="field">Chức vụ<select className="select" value={loc.maChucVu} onChange={(e) => setLoc({ ...loc, maChucVu: e.target.value })}>
            <option value="">Tất cả</option>{chucVu.map(([ma, ten]) => <option key={ma} value={ma}>{ten}</option>)}</select></label>
          <label className="field">Trạng thái<select className="select" value={loc.maTrangThai} onChange={(e) => setLoc({ ...loc, maTrangThai: e.target.value })}>
            <option value="">Tất cả</option>{trangThai.map(([ma, ten]) => <option key={ma} value={ma}>{ten}</option>)}</select></label>
          <div className="hanh-dong">
            <button className="btn btn-chinh" disabled={loading}>Tìm kiếm</button>
            <button type="button" className="btn" onClick={() => { setLoc({ tuKhoa: '', maPhongBan: '', maChucVu: '', maTrangThai: '' }); setTimeout(() => void tai(), 0) }}>Xoá lọc</button>
          </div>
        </form>
        {moForm && formHoSo}
      </div></div>

      {chiTiet && <div className="card"><div className="card-header"><h3>{chiTiet.hoTen}</h3><span className="phu mono">{chiTiet.maNV}</span>
        <span className="can-phai ht1-actions">
          <button className="btn btn-nho" onClick={() => moSua(chiTiet)}>Sửa hồ sơ</button>
          <button className="btn btn-nho" onClick={() => setChiTiet(null)}>Đóng</button>
        </span></div>
        <div className="card-body"><div className="form-grid">
          <div className="field"><span className="label">Phòng ban</span><span>{chiTiet.tenPhongBan ?? '—'}</span></div>
          <div className="field"><span className="label">Chức vụ</span><span>{chiTiet.tenChucVu ?? '—'}</span></div>
          <div className="field"><span className="label">Trạng thái</span><span><StatusBadge trangThai={chiTiet.tenTrangThai ?? ''} /></span></div>
          <div className="field"><span className="label">Ngày sinh</span><span>{chiTiet.ngaySinh ?? '—'}</span></div>
          <div className="field"><span className="label">Giới tính</span><span>{chiTiet.gioiTinh ?? '—'}</span></div>
          <div className="field"><span className="label">Ngày vào làm</span><span>{chiTiet.ngayVaoLam ?? '—'}</span></div>
          <div className="field"><span className="label">Điện thoại</span><span>{chiTiet.soDienThoai ?? '—'}</span></div>
          <div className="field"><span className="label">Email</span><span>{chiTiet.email ?? '—'}</span></div>
          <div className="field form-span"><span className="label">Địa chỉ</span><span>{chiTiet.diaChi ?? '—'}</span></div>
        </div></div>
      </div>}

      <div className="card"><div className="card-header"><h3>Kết quả</h3><span className="phu">{ds.length} hồ sơ</span>
        <span className="can-phai"><button className="btn btn-nho btn-chinh" onClick={() => { setSuaId(''); setForm(rong); setMoForm(true); setOk('') }}>Thêm hồ sơ</button></span></div>
        <div className="card-body khong-dem">
          {loading ? <p role="status" style={{ padding: 18 }}>Đang tải hồ sơ…</p> : <div className="table-wrap"><table className="data-table"><thead><tr>
            <th>Mã NV</th><th>Họ tên</th><th>Phòng ban</th><th>Chức vụ</th><th>Liên hệ</th><th>Trạng thái</th><th>Thao tác</th>
          </tr></thead><tbody>
            {ds.map((h) => <tr key={h.maNV}>
              <td className="mono">{h.maNV}</td><td>{h.hoTen}</td><td>{h.tenPhongBan ?? '—'}</td><td>{h.tenChucVu ?? '—'}</td>
              <td>{h.soDienThoai ?? h.email ?? '—'}</td><td><StatusBadge trangThai={h.tenTrangThai ?? ''} /></td>
              <td><div className="hanh-dong ht1-actions">
                <button className="btn btn-nho" onClick={async () => {
                  setLoi('')
                  try { setChiTiet((await http.get<HoSo>(`/ht4/ho-so/${h.maNV}`)).data) } catch { setChiTiet(h) }
                }}>Xem</button>
                <button className="btn btn-nho" onClick={() => moSua(h)}>Sửa</button>
              </div></td>
            </tr>)}
            {!ds.length && <tr><td colSpan={7}>Không có hồ sơ nào khớp tiêu chí tìm kiếm.</td></tr>}
          </tbody></table></div>}
        </div>
      </div>
    </>}

    {/* ---------- Quản lý trạng thái: bảng theo trạng thái + hàng đợi yêu cầu ---------- */}
    {mode === 'trang-thai' && <>
      <div className="stat-grid">
        {trangThai.map(([ma, ten]) => (
          <div className="stat-card" key={ma}>
            <div className="nhan">{ten}</div>
            <div className="gia-tri">{tatCa.filter((h) => h.maTrangThai === ma).length}</div>
            <div className="ghi-chu mono">{ma}</div>
          </div>
        ))}
      </div>

      {doiTrangThai && <div className="card"><div className="card-body"><form className="ht1-editor" onSubmit={(e: FormEvent) => {
        e.preventDefault()
        void chay(async () => { await http.put(`/ht4/ho-so/${doiTrangThai.maNV}/trang-thai`, { maTrangThai: doiTrangThai.maTrangThai }); setDoiTrangThai(null) }, 'Đã cập nhật trạng thái nhân viên')
      }}>
        <h2>Đổi trạng thái · {doiTrangThai.hoTen} ({doiTrangThai.maNV})</h2>
        <div className="form-grid"><label>Trạng thái mới *<select className="select" required value={doiTrangThai.maTrangThai} onChange={(e) => setDoiTrangThai({ ...doiTrangThai, maTrangThai: e.target.value })}>
          <option value="">— Chọn trạng thái —</option>{trangThai.map(([ma, ten]) => <option key={ma} value={ma}>{ten}</option>)}
        </select></label></div>
        <div className="form-actions"><button className="btn btn-chinh" disabled={busy}>Lưu trạng thái</button><button type="button" className="btn" onClick={() => setDoiTrangThai(null)}>Đóng</button></div>
      </form></div></div>}

      <div className="card"><div className="card-header"><h3>Nhân viên theo trạng thái</h3>
        <span className="can-phai"><select className="select" aria-label="Lọc theo trạng thái" value={loc.maTrangThai}
          onChange={(e) => { setLoc({ ...loc, maTrangThai: e.target.value }); setTimeout(() => void tai(), 0) }}>
          <option value="">Tất cả trạng thái</option>{trangThai.map(([ma, ten]) => <option key={ma} value={ma}>{ten}</option>)}
        </select></span></div>
        <div className="card-body khong-dem">
          {loading ? <p role="status" style={{ padding: 18 }}>Đang tải…</p> : <div className="table-wrap"><table className="data-table"><thead><tr>
            <th>Mã NV</th><th>Họ tên</th><th>Phòng ban</th><th>Ngày vào làm</th><th>Trạng thái</th><th>Thao tác</th>
          </tr></thead><tbody>
            {ds.map((h) => <tr key={h.maNV}>
              <td className="mono">{h.maNV}</td><td>{h.hoTen}</td><td>{h.tenPhongBan ?? '—'}</td><td>{h.ngayVaoLam ?? '—'}</td>
              <td><StatusBadge trangThai={h.tenTrangThai ?? ''} /></td>
              <td><div className="hanh-dong"><button className="btn btn-nho btn-chinh" onClick={() => setDoiTrangThai({ maNV: h.maNV, hoTen: h.hoTen, maTrangThai: h.maTrangThai ?? '' })}>Đổi trạng thái</button></div></td>
            </tr>)}
            {!ds.length && <tr><td colSpan={6}>Không có nhân viên nào ở trạng thái này.</td></tr>}
          </tbody></table></div>}
        </div>
      </div>

      <div className="card"><div className="card-header"><h3>Yêu cầu cập nhật thông tin</h3>
        <span className="phu">{yeuCau.filter((y) => y.trangThai === 'Chờ xử lý').length} yêu cầu chờ xử lý</span></div>
        <div className="card-body khong-dem"><BangYeuCau rows={yeuCau} loading={loading} thaoTac={(y) => y.trangThai === 'Chờ xử lý' ? <>
          <button className="btn btn-nho btn-chinh" disabled={busy} onClick={() => {
            const phanHoi = window.prompt('Phản hồi cho nhân viên (có thể bỏ trống):') ?? ''
            void chay(async () => { await http.post(`/ht4/yeu-cau-cap-nhat/${y.maYeuCau}/xu-ly`, { trangThai: 'Đã xử lý', phanHoi }) }, 'Đã xử lý yêu cầu cập nhật')
          }}>Đã xử lý</button>
          <button className="btn btn-nho btn-nguy-hiem" disabled={busy} onClick={() => {
            const phanHoi = window.prompt('Lý do từ chối:')
            if (phanHoi) void chay(async () => { await http.post(`/ht4/yeu-cau-cap-nhat/${y.maYeuCau}/xu-ly`, { trangThai: 'Từ chối', phanHoi }) }, 'Đã từ chối yêu cầu')
          }}>Từ chối</button>
        </> : <span className="muted">{y.nguoiXuLy ? `Xử lý bởi ${y.nguoiXuLy}` : '—'}</span>} /></div>
      </div>
    </>}

    {/* ---------- Hồ sơ của tôi ---------- */}
    {mode === 'ca-nhan' && <>
      {me && <div className="card"><div className="card-header"><h3>{me.hoTen}</h3><span className="phu mono">{me.maNV}</span>
        <span className="can-phai"><StatusBadge trangThai={me.tenTrangThai ?? 'Đang làm việc'} /></span></div>
        <div className="card-body"><div className="form-grid">
          <div className="field"><span className="label">Phòng ban</span><span>{me.tenPhongBan ?? '—'}</span></div>
          <div className="field"><span className="label">Chức vụ</span><span>{me.tenChucVu ?? '—'}</span></div>
          <div className="field"><span className="label">Ngày vào làm</span><span>{me.ngayVaoLam ?? '—'}</span></div>
          <div className="field"><span className="label">Điện thoại</span><span>{me.soDienThoai ?? '—'}</span></div>
          <div className="field"><span className="label">Email</span><span>{me.email ?? '—'}</span></div>
          <div className="field"><span className="label">Địa chỉ</span><span>{me.diaChi ?? '—'}</span></div>
        </div></div>
      </div>}
      <div className="card"><div className="card-header"><h3>Gửi yêu cầu cập nhật thông tin</h3><span className="phu">nhân sự sẽ xem xét và phản hồi</span></div><div className="card-body">
        <form onSubmit={(e: FormEvent) => {
          e.preventDefault()
          void chay(async () => { await http.post('/ht4/yeu-cau-cap-nhat', { noiDung }); setNoiDung('') }, 'Đã gửi yêu cầu cập nhật thông tin')
        }}>
          <div className="form-grid"><label className="form-span">Nội dung cần cập nhật *<textarea className="textarea" required maxLength={500} placeholder="Ví dụ: đổi số điện thoại thành 0909xxxxxx" value={noiDung} onChange={(e) => setNoiDung(e.target.value)} /></label></div>
          <div className="form-actions"><button className="btn btn-chinh" disabled={busy}>Gửi yêu cầu</button></div>
        </form>
      </div></div>
      <div className="card"><div className="card-header"><h3>Yêu cầu đã gửi</h3></div><div className="card-body khong-dem">
        <BangYeuCau rows={yeuCau} loading={loading} />
      </div></div>
    </>}
  </>
}

function BangYeuCau({ rows, loading, thaoTac }: { rows: YeuCau[]; loading: boolean; thaoTac?: (y: YeuCau) => ReactNode }) {
  if (loading) return <p role="status" style={{ padding: 18 }}>Đang tải yêu cầu…</p>
  return <div className="table-wrap"><table className="data-table"><thead><tr>
    <th>Mã</th><th>Nhân viên</th><th>Nội dung</th><th>Gửi lúc</th><th>Trạng thái</th><th>Phản hồi</th>{thaoTac && <th>Thao tác</th>}
  </tr></thead><tbody>
    {rows.map((y) => <tr key={y.maYeuCau}>
      <td className="mono">YC{String(y.maYeuCau).padStart(4, '0')}</td>
      <td>{y.hoTen ?? y.maNV}</td><td>{y.noiDung}</td><td>{gio(y.thoiGianGui)}</td>
      <td><StatusBadge trangThai={y.trangThai} /></td>
      <td>{y.phanHoi ?? '—'}</td>
      {thaoTac && <td><div className="hanh-dong ht1-actions">{thaoTac(y)}</div></td>}
    </tr>)}
    {!rows.length && <tr><td colSpan={thaoTac ? 7 : 6}>Chưa có yêu cầu cập nhật nào.</td></tr>}
  </tbody></table></div>
}
