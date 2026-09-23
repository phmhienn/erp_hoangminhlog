import { useEffect, useState, type ReactNode } from 'react'
import { http, thongBaoLoi } from '@/shared/api/http'
import { StatusBadge } from '@/shared/components/StatusBadge'
import { usePermission } from '@/shared/permission/PermissionContext'

interface Order { maDonHang: string; tenKhachHang: string | null; nguoiNhan: string | null; diachiGiaoHang: string | null; khoiLuong: number | null; matHang: string; trangThai: string }
interface Receipt { maPhieuNhap: string; maDonHang: string; ngayNhap: string; trangThai: string; ghiChu: string | null; matHang: string }
interface Issue { maPhieuXuat: string; maDonHang: string; ngayXuat: string; trangThai: string; ghiChu: string | null; lyDoXuat: string | null; matHang: string }
interface Dashboard { donChoNhap: number; donTrongKho: number; donChoNhanVanChuyen: number; phieuNhapChoDuyet: number; phieuXuatChoDuyet: number }
interface Count { maPhieuKiemKe: string; ngayKiemKe: string; khuVucKiemKe: string | null; chiTiet: { maDonHang: string; soLuongHeThong: number; soLuongThucTe: number; chenhLech: number; ghiChu: string | null }[] }
interface Incident { maBienBan: string; maDonHang: string; maPhieuNhap: string | null; loaiSuCo: string; moTa: string | null; ngayLap: string }
interface History { loai: string; ma: string; maDonHang: string; tenKhachHang: string | null; ngay: string; trangThai: string; maNV: string | null; matHang: string | null }
interface Report { soPhieuNhap: number; donDaNhap: number; soPhieuXuat: number; donDaXuat: number; theoTrangThaiNhap: Record<string, number>; theoNgayNhap: Record<string, number>; theoTrangThaiXuat: Record<string, number>; theoNgayXuat: Record<string, number> }
const today = () => { const d = new Date(); return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}` }
const monthNameToNumber: Record<string, string> = {
  jan: '01', january: '01',
  feb: '02', february: '02',
  mar: '03', march: '03',
  apr: '04', april: '04',
  may: '05',
  jun: '06', june: '06',
  jul: '07', july: '07',
  aug: '08', august: '08',
  sep: '09', sept: '09', september: '09',
  oct: '10', october: '10',
  nov: '11', november: '11',
  dec: '12', december: '12',
}
const chuanHoaNgay = (value: string) => {
  const raw = value.trim()
  if (/^\d{4}-\d{2}-\d{2}$/.test(raw)) return raw

  const named = raw.match(/^(\d{1,2})[-/\s]([A-Za-z]{3,9})[-/\s](\d{4})$/)
  if (named) {
    const month = monthNameToNumber[named[2].toLowerCase()]
    if (month) return `${named[3]}-${month}-${named[1].padStart(2, '0')}`
  }

  const numbered = raw.match(/^(\d{1,2})[-/\s](\d{1,2})[-/\s](\d{4})$/)
  if (numbered) return `${numbered[3]}-${numbered[2].padStart(2, '0')}-${numbered[1].padStart(2, '0')}`

  return raw
}
const titles: Record<string, string> = { 'phieu-nhap': 'Tiếp nhận đơn hàng vào kho', 'phieu-xuat': 'Bàn giao đơn hàng vận chuyển', 'phe-duyet': 'Duyệt tiếp nhận – bàn giao', 'kiem-ke': 'Kiểm kê đơn hàng trong kho', dashboard: 'Theo dõi kho', 'bao-cao': 'Báo cáo nhập – xuất đơn hàng', 'bao-cao-phieu': 'Lịch sử đơn hàng qua kho' }

function Card({ title, children }: { title: string; children: ReactNode }) {
  return <div className="card"><div className="card-header"><h3>{title}</h3></div><div className="card-body">{children}</div></div>
}
function Orders({ rows, choose, selected }: { rows: Order[]; choose?: (o: Order) => void; selected?: string }) {
  return <div className="table-wrap"><table className="data-table"><thead><tr><th>Đơn hàng</th><th>Khách hàng</th><th>Hàng trong đơn</th><th>Người nhận</th><th>Địa chỉ giao</th><th>Khối lượng</th>{choose && <th>Thao tác</th>}</tr></thead><tbody>
    {rows.map(o => <tr key={o.maDonHang}><td className="mono">{o.maDonHang}</td><td>{o.tenKhachHang ?? '—'}</td><td>{o.matHang || '—'}</td><td>{o.nguoiNhan ?? '—'}</td><td>{o.diachiGiaoHang ?? '—'}</td><td>{o.khoiLuong ?? '—'}</td>{choose && <td><button type="button" className="btn btn-nho" disabled={selected === o.maDonHang} onClick={() => choose(o)}>{selected === o.maDonHang ? 'Đang chọn' : 'Chọn đơn'}</button></td>}</tr>)}
    {!rows.length && <tr><td colSpan={choose ? 7 : 6}>Không có đơn hàng phù hợp.</td></tr>}
  </tbody></table></div>
}

export function Ht2Page({ mode }: { mode: string }) {
  const { coChucNang } = usePermission()
  const manager = coChucNang('HT2_PHE_DUYET_PHIEU_NHAP')
  const [orders, setOrders] = useState<Order[]>([])
  const [receipts, setReceipts] = useState<Receipt[]>([])
  const [issues, setIssues] = useState<Issue[]>([])
  const [counts, setCounts] = useState<Count[]>([])
  const [incidents, setIncidents] = useState<Incident[]>([])
  const [history, setHistory] = useState<History[]>([])
  const [dashboard, setDashboard] = useState<Dashboard | null>(null)
  const [report, setReport] = useState<Report | null>(null)
  const [period, setPeriod] = useState({ tuNgay: today().slice(0, 8) + '01', denNgay: today() })
  const [filter, setFilter] = useState({ maDonHang: '', tuKhoa: '', maNV: '', tuNgay: '', denNgay: '', loai: '' })
  const [search, setSearch] = useState('')
  const [status, setStatus] = useState('')
  const [edit, setEdit] = useState('')
  const [form, setForm] = useState({ maDonHang: '', ngay: today(), ghiChu: '', lyDoXuat: 'Bàn giao vận chuyển' })
  const [count, setCount] = useState({ maDonHang: '', ngayKiemKe: today(), khuVucKiemKe: '', soLuongThucTe: 1, ghiChu: '' })
  const [incident, setIncident] = useState({ maDonHang: '', maPhieuNhap: '', loaiSuCo: 'Thiếu', moTa: '', duongDanAnh: '' })
  const [busy, setBusy] = useState(false)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  async function load() {
    setLoading(true)
    try {
      if (mode === 'phieu-nhap') {
        const [o, p] = await Promise.all([http.get<Order[]>('/ht2/don-cho-nhap'), http.get<Receipt[]>('/ht2/phieu-nhap')]); setOrders(o.data); setReceipts(p.data)
      } else if (mode === 'phieu-xuat') {
        const [o, p] = await Promise.all([http.get<Order[]>('/ht2/don-cho-xuat'), http.get<Issue[]>('/ht2/phieu-xuat')]); setOrders(o.data); setIssues(p.data)
      } else if (mode === 'phe-duyet') {
        const [n, x] = await Promise.all([http.get<Receipt[]>('/ht2/phieu-nhap'), http.get<Issue[]>('/ht2/phieu-xuat')]); setReceipts(n.data); setIssues(x.data)
      } else if (mode === 'dashboard') {
        const [o, d] = await Promise.all([http.get<Order[]>('/ht2/ton-kho'), http.get<Dashboard>('/ht2/dashboard')]); setOrders(o.data); setDashboard(d.data)
      } else if (mode === 'kiem-ke') {
        const [o, c, b] = await Promise.all([http.get<Order[]>('/ht2/ton-kho'), http.get<Count[]>('/ht2/phieu-kiem-ke'), http.get<Incident[]>('/ht2/bien-ban')]); setOrders(o.data); setCounts(c.data); setIncidents(b.data)
      } else if (mode === 'bao-cao') setReport((await http.get<Report>('/ht2/bao-cao-nhap', { params: period })).data)
      else if (mode === 'bao-cao-phieu') setHistory((await http.get<History[]>('/ht2/tra-cuu', { params: Object.fromEntries(Object.entries(filter).filter(([, v]) => v)) })).data)
    } catch (e) { setError(thongBaoLoi(e)) } finally { setLoading(false) }
  }
  useEffect(() => { void load() }, [mode])
  async function act(work: () => Promise<unknown>, message: string) {
    setBusy(true); setError(''); setSuccess('')
    try { await work(); setSuccess(message); await load() } catch (e) { setError(thongBaoLoi(e)) } finally { setBusy(false) }
  }
  const matches = (...values: (string | null | undefined)[]) => values.some(v => (v ?? '').toLowerCase().includes(search.trim().toLowerCase()))
  const displayedOrders = orders.filter(o => matches(o.maDonHang, o.tenKhachHang, o.nguoiNhan, o.matHang))
  const isReceipt = mode === 'phieu-nhap'
  function reset() { setEdit(''); setForm({ maDonHang: '', ngay: today(), ghiChu: '', lyDoXuat: 'Bàn giao vận chuyển' }) }
  function actions(kind: 'phieu-nhap' | 'phieu-xuat', id: string, state: string, startEdit: () => void) {
    return <div className="hanh-dong">
      {mode !== 'phe-duyet' && ['Lưu tạm', 'Từ chối'].includes(state) && coChucNang('HT2_CAP_NHAT_PHIEU') && <button className="btn btn-nho" disabled={busy} onClick={startEdit}>Sửa</button>}
      {mode !== 'phe-duyet' && state === 'Lưu tạm' && <button className="btn btn-nho" disabled={busy} onClick={() => void act(() => http.post(`/ht2/${kind}/${id}/gui-duyet`), 'Đã gửi duyệt')}>Gửi duyệt</button>}
      {manager && state === 'Chờ duyệt' && <>
        <button className="btn btn-nho btn-chinh" disabled={busy} onClick={() => void act(() => http.post(`/ht2/${kind}/${id}/duyet`), kind === 'phieu-nhap' ? 'Đã tiếp nhận đơn vào kho' : 'Đã xuất nguyên đơn, chờ tài xế nhận')}>Duyệt</button>
        <button className="btn btn-nho btn-nguy-hiem" disabled={busy} onClick={() => { const lyDo = window.prompt('Lý do từ chối'); if (lyDo?.trim()) void act(() => http.post(`/ht2/${kind}/${id}/tu-choi`, { lyDo: lyDo.trim() }), 'Đã từ chối phiếu') }}>Từ chối</button>
      </>}
    </div>
  }
  function receiptTable() {
    const rows = receipts.filter(p => matches(p.maPhieuNhap, p.maDonHang, p.matHang) && (!status || p.trangThai === status))
    return <Card title="Phiếu tiếp nhận đơn hàng"><div className="table-wrap"><table className="data-table"><thead><tr><th>Phiếu nhập</th><th>Đơn hàng</th><th>Hàng trong đơn</th><th>Ngày nhập</th><th>Trạng thái</th><th>Ghi chú</th><th>Thao tác</th></tr></thead><tbody>
      {rows.map(p => <tr key={p.maPhieuNhap}><td>{p.maPhieuNhap}</td><td>{p.maDonHang}</td><td>{p.matHang || '—'}</td><td>{p.ngayNhap}</td><td><StatusBadge trangThai={p.trangThai} /></td><td>{p.ghiChu ?? '—'}</td><td>{actions('phieu-nhap', p.maPhieuNhap, p.trangThai, () => { setEdit(p.maPhieuNhap); setForm({ maDonHang: p.maDonHang, ngay: p.ngayNhap, ghiChu: p.ghiChu ?? '', lyDoXuat: '' }); window.scrollTo({ top: 0, behavior: 'smooth' }) })}</td></tr>)}
      {!rows.length && <tr><td colSpan={7}>Không có phiếu phù hợp.</td></tr>}
    </tbody></table></div></Card>
  }
  function issueTable() {
    const rows = issues.filter(p => matches(p.maPhieuXuat, p.maDonHang, p.matHang) && (!status || p.trangThai === status))
    return <Card title="Phiếu bàn giao vận chuyển"><div className="table-wrap"><table className="data-table"><thead><tr><th>Phiếu xuất</th><th>Đơn hàng</th><th>Hàng trong đơn</th><th>Ngày xuất</th><th>Trạng thái</th><th>Lý do / ghi chú</th><th>Thao tác</th></tr></thead><tbody>
      {rows.map(p => <tr key={p.maPhieuXuat}><td>{p.maPhieuXuat}</td><td>{p.maDonHang}</td><td>{p.matHang || '—'}</td><td>{p.ngayXuat}</td><td><StatusBadge trangThai={p.trangThai} /></td><td>{p.lyDoXuat} {p.ghiChu}</td><td>{actions('phieu-xuat', p.maPhieuXuat, p.trangThai, () => { setEdit(p.maPhieuXuat); setForm({ maDonHang: p.maDonHang, ngay: p.ngayXuat, ghiChu: p.ghiChu ?? '', lyDoXuat: p.lyDoXuat ?? '' }); window.scrollTo({ top: 0, behavior: 'smooth' }) })}</td></tr>)}
      {!rows.length && <tr><td colSpan={7}>Không có phiếu phù hợp.</td></tr>}
    </tbody></table></div></Card>
  }
  return <>
    <div className="page-header"><h1 className="page-title">{titles[mode]}</h1></div>
    {error && <div role="alert" className="alert alert-loi">{error}</div>}
    {success && <div role="status" className="alert alert-thanh-cong">{success}</div>}
    {loading && <p role="status">Đang tải…</p>}
    {!['bao-cao', 'bao-cao-phieu'].includes(mode) && <div className="card"><div className="card-body form-actions">
      <label>Tìm đơn / phiếu / khách hàng<input className="input" value={search} onChange={e => setSearch(e.target.value)} /></label>
      {['phieu-nhap', 'phieu-xuat', 'phe-duyet'].includes(mode) && <label>Trạng thái<select className="select" value={status} onChange={e => setStatus(e.target.value)}><option value="">Tất cả</option>{['Lưu tạm', 'Chờ duyệt', 'Đã duyệt', 'Đã xuất', 'Từ chối'].map(s => <option key={s}>{s}</option>)}</select></label>}
      <button className="btn" disabled={loading || busy} onClick={() => { setError(''); void load() }}>Tải lại</button>
    </div></div>}
    {['phieu-nhap', 'phieu-xuat'].includes(mode) && <>
      {!edit && <Card title={isReceipt ? 'Đơn đã duyệt chờ tiếp nhận' : 'Đơn đang lưu kho'}><Orders rows={displayedOrders} selected={form.maDonHang} choose={o => setForm({ ...form, maDonHang: o.maDonHang })} /></Card>}
      <Card title={edit ? `Cập nhật ${edit}` : isReceipt ? 'Lập phiếu tiếp nhận' : 'Lập phiếu bàn giao'}>
        <form onSubmit={e => { e.preventDefault(); const endpoint = `/ht2/${mode}`; const ngay = chuanHoaNgay(form.ngay); const body = { maDonHang: form.maDonHang, ghiChu: form.ghiChu || null, ...(isReceipt ? { ngayNhap: ngay } : { ngayXuat: ngay, lyDoXuat: form.lyDoXuat }) }; void act(async () => { if (edit) await http.put(`${endpoint}/${edit}`, body); else await http.post(endpoint, body); reset() }, 'Đã lưu phiếu. Gửi duyệt để hoàn tất.') }}>
          <div className="form-grid">
            <label>Đơn hàng *<input className="input" required readOnly value={form.maDonHang} placeholder="Chọn đơn ở danh sách phía trên" /></label>
            <label>{isReceipt ? 'Ngày tiếp nhận' : 'Ngày bàn giao'} *<input className="input" type="date" required value={form.ngay} onChange={e => setForm({ ...form, ngay: e.target.value })} /></label>
            {!isReceipt && <label>Lý do xuất<input className="input" maxLength={255} value={form.lyDoXuat} onChange={e => setForm({ ...form, lyDoXuat: e.target.value })} /></label>}
            <label>Ghi chú / tình trạng đơn<textarea className="textarea" maxLength={255} value={form.ghiChu} onChange={e => setForm({ ...form, ghiChu: e.target.value })} /></label>
          </div><div className="form-actions"><button className="btn btn-chinh" disabled={busy || !form.maDonHang}>Lưu tạm</button><button type="button" className="btn" onClick={reset}>Làm mới</button></div>
        </form>
      </Card>
    </>}
    {['phieu-nhap', 'phe-duyet'].includes(mode) && receiptTable()}
    {['phieu-xuat', 'phe-duyet'].includes(mode) && issueTable()}
    {mode === 'dashboard' && <>
      {dashboard && <div className="stat-grid">{[['Chờ tiếp nhận', dashboard.donChoNhap], ['Đang lưu kho', dashboard.donTrongKho], ['Đã xuất, chờ tài xế nhận', dashboard.donChoNhanVanChuyen], ['Phiếu nhập chờ duyệt', dashboard.phieuNhapChoDuyet], ['Phiếu xuất chờ duyệt', dashboard.phieuXuatChoDuyet]].map(([label, value]) => <div className="stat-card" key={label}><div className="nhan">{label}</div><div className="gia-tri">{value}</div></div>)}</div>}
      <Card title="Đơn hàng đang lưu kho"><Orders rows={displayedOrders} /></Card>
    </>}
    {mode === 'kiem-ke' && <>
      <Card title="Đối chiếu đơn trong kho"><Orders rows={displayedOrders} selected={count.maDonHang} choose={o => setCount({ ...count, maDonHang: o.maDonHang })} /></Card>
      <Card title="Ghi nhận kiểm kê đơn hàng"><form onSubmit={e => { e.preventDefault(); void act(() => http.post('/ht2/phieu-kiem-ke', { ngayKiemKe: chuanHoaNgay(count.ngayKiemKe), khuVucKiemKe: count.khuVucKiemKe || null, ghiChu: count.ghiChu || null, chiTiet: [{ maDonHang: count.maDonHang, soLuongThucTe: count.soLuongThucTe, ghiChu: count.ghiChu || null }] }), 'Đã lưu kết quả kiểm kê') }}>
        <div className="form-grid"><label>Đơn hàng *<input className="input" required maxLength={10} value={count.maDonHang} onChange={e => setCount({ ...count, maDonHang: e.target.value })} /></label>
          <label>Thực tế<select className="select" value={count.soLuongThucTe} onChange={e => setCount({ ...count, soLuongThucTe: Number(e.target.value) })}><option value={1}>Có đơn trong kho</option><option value={0}>Không tìm thấy đơn</option></select></label>
          <label>Ngày kiểm kê *<input className="input" type="date" required value={count.ngayKiemKe} onChange={e => setCount({ ...count, ngayKiemKe: e.target.value })} /></label>
          <label>Khu vực<input className="input" maxLength={100} value={count.khuVucKiemKe} onChange={e => setCount({ ...count, khuVucKiemKe: e.target.value })} /></label>
          <label>Ghi chú<input className="input" maxLength={255} value={count.ghiChu} onChange={e => setCount({ ...count, ghiChu: e.target.value })} /></label></div>
        <div className="form-actions"><button className="btn btn-chinh" disabled={busy}>Chốt kiểm kê</button></div>
      </form></Card>
      <Card title="Kết quả kiểm kê"><div className="table-wrap"><table className="data-table"><thead><tr><th>Phiếu</th><th>Ngày / khu vực</th><th>Đơn hàng</th><th>Theo hệ thống</th><th>Thực tế</th><th>Kết quả</th><th>Ghi chú</th></tr></thead><tbody>
        {counts.flatMap(p => p.chiTiet.map(c => <tr key={`${p.maPhieuKiemKe}-${c.maDonHang}`}><td>{p.maPhieuKiemKe}</td><td>{p.ngayKiemKe} {p.khuVucKiemKe}</td><td>{c.maDonHang}</td><td>{c.soLuongHeThong ? 'Trong kho' : 'Ngoài kho'}</td><td>{c.soLuongThucTe ? 'Có' : 'Không'}</td><td>{c.chenhLech === 0 ? 'Khớp' : c.chenhLech < 0 ? 'Thiếu đơn' : 'Thừa đơn'}</td><td>{c.ghiChu}</td></tr>))}
        {!counts.length && <tr><td colSpan={7}>Chưa có kiểm kê đơn hàng.</td></tr>}
      </tbody></table></div></Card>
      <Card title="Lập biên bản sự cố đơn hàng"><form onSubmit={e => { e.preventDefault(); void act(() => http.post('/ht2/bien-ban', { ...incident, maPhieuNhap: incident.maPhieuNhap || null, duongDanAnh: incident.duongDanAnh || null }), 'Đã lập biên bản') }}>
        <div className="form-grid"><label>Đơn hàng *<input className="input" required maxLength={10} value={incident.maDonHang} onChange={e => setIncident({ ...incident, maDonHang: e.target.value })} /></label>
          <label>Phiếu nhập liên quan<input className="input" maxLength={20} value={incident.maPhieuNhap} onChange={e => setIncident({ ...incident, maPhieuNhap: e.target.value })} /></label>
          <label>Loại sự cố<select className="select" value={incident.loaiSuCo} onChange={e => setIncident({ ...incident, loaiSuCo: e.target.value })}>{['Thừa', 'Thiếu', 'Hư hỏng'].map(t => <option key={t}>{t}</option>)}</select></label>
          <label>Ảnh minh chứng<input className="input" maxLength={255} value={incident.duongDanAnh} onChange={e => setIncident({ ...incident, duongDanAnh: e.target.value })} /></label>
          <label>Mô tả<textarea className="textarea" maxLength={500} value={incident.moTa} onChange={e => setIncident({ ...incident, moTa: e.target.value })} /></label></div>
        <div className="form-actions"><button className="btn btn-chinh" disabled={busy}>Lưu biên bản</button></div>
      </form></Card>
      <Card title="Biên bản sự cố"><div className="table-wrap"><table className="data-table"><thead><tr><th>Mã</th><th>Đơn hàng</th><th>Phiếu nhập</th><th>Loại</th><th>Mô tả</th><th>Ngày lập</th></tr></thead><tbody>{incidents.map(b => <tr key={b.maBienBan}><td>{b.maBienBan}</td><td>{b.maDonHang}</td><td>{b.maPhieuNhap}</td><td>{b.loaiSuCo}</td><td>{b.moTa}</td><td>{new Date(b.ngayLap).toLocaleString('vi-VN')}</td></tr>)}{!incidents.length && <tr><td colSpan={6}>Chưa có biên bản.</td></tr>}</tbody></table></div></Card>
    </>}
    {mode === 'bao-cao-phieu' && <>
      <Card title="Tra cứu lịch sử"><form className="form-grid" onSubmit={e => { e.preventDefault(); setError(''); void load() }}>
        <label>Mã đơn / phiếu<input className="input" value={filter.maDonHang} onChange={e => setFilter({ ...filter, maDonHang: e.target.value })} /></label>
        <label>Khách hàng<input className="input" value={filter.tuKhoa} onChange={e => setFilter({ ...filter, tuKhoa: e.target.value })} /></label>
        <label>Nhân viên<input className="input" value={filter.maNV} onChange={e => setFilter({ ...filter, maNV: e.target.value })} /></label>
        <label>Từ ngày<input className="input" type="date" value={filter.tuNgay} onChange={e => setFilter({ ...filter, tuNgay: e.target.value })} /></label>
        <label>Đến ngày<input className="input" type="date" value={filter.denNgay} onChange={e => setFilter({ ...filter, denNgay: e.target.value })} /></label>
        <label>Loại<select className="select" value={filter.loai} onChange={e => setFilter({ ...filter, loai: e.target.value })}><option value="">Tất cả</option>{['Đơn chờ nhập', 'Phiếu nhập', 'Phiếu xuất'].map(t => <option key={t}>{t}</option>)}</select></label><button className="btn btn-chinh" disabled={loading}>Tra cứu</button>
      </form></Card>
      <Card title="Lịch sử tiếp nhận – bàn giao"><div className="table-wrap"><table className="data-table"><thead><tr><th>Loại</th><th>Mã</th><th>Đơn hàng</th><th>Khách hàng</th><th>Ngày</th><th>Trạng thái</th><th>Nhân viên</th><th>Hàng trong đơn</th></tr></thead><tbody>{history.map(h => <tr key={`${h.loai}-${h.ma}`}><td>{h.loai}</td><td>{h.ma}</td><td>{h.maDonHang}</td><td>{h.tenKhachHang}</td><td>{h.ngay}</td><td><StatusBadge trangThai={h.trangThai} /></td><td>{h.maNV}</td><td>{h.matHang}</td></tr>)}{!history.length && <tr><td colSpan={8}>Không có dữ liệu phù hợp.</td></tr>}</tbody></table></div></Card>
    </>}
    {mode === 'bao-cao' && <>
      <Card title="Kỳ báo cáo"><form className="form-actions" onSubmit={e => { e.preventDefault(); setError(''); void load() }}><label>Từ ngày<input className="input" required type="date" value={period.tuNgay} onChange={e => setPeriod({ ...period, tuNgay: e.target.value })} /></label><label>Đến ngày<input className="input" required type="date" value={period.denNgay} onChange={e => setPeriod({ ...period, denNgay: e.target.value })} /></label><button className="btn btn-chinh" disabled={loading}>Xem báo cáo</button></form></Card>
      {report && <><div className="stat-grid">{[['Phiếu nhập', report.soPhieuNhap], ['Đơn đã tiếp nhận', report.donDaNhap], ['Phiếu xuất', report.soPhieuXuat], ['Đơn đã bàn giao', report.donDaXuat]].map(([label, value]) => <div className="stat-card" key={label}><div className="nhan">{label}</div><div className="gia-tri">{value}</div></div>)}</div>
        <Groups title="Phiếu nhập theo trạng thái" values={report.theoTrangThaiNhap} /><Groups title="Phiếu xuất theo trạng thái" values={report.theoTrangThaiXuat} /><Groups title="Đơn tiếp nhận theo ngày" values={report.theoNgayNhap} /><Groups title="Đơn bàn giao theo ngày" values={report.theoNgayXuat} />
      </>}
    </>}
  </>
}
function Groups({ title, values }: { title: string; values: Record<string, number> }) {
  return <Card title={title}><table className="data-table"><thead><tr><th>Nhóm</th><th>Số lượng</th></tr></thead><tbody>{Object.entries(values).map(([k, v]) => <tr key={k}><td>{k}</td><td>{v}</td></tr>)}{!Object.keys(values).length && <tr><td colSpan={2}>Không có dữ liệu.</td></tr>}</tbody></table></Card>
}
