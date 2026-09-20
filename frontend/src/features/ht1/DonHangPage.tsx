import { useEffect, useState, type FormEvent, type ReactNode } from 'react'
import { http, thongBaoLoi } from '@/shared/api/http'
import { usePermission } from '@/shared/permission/PermissionContext'
import { StatusBadge } from '@/shared/components/StatusBadge'
import type { DichVu, DonHang, DonInput, KhachHang, LichSu } from './types'

const rong = (): DonInput => ({ maKhachHang: '', maDichVu: '', nguoiGui: '', nguoiNhan: '', sdtNguoiGui: '', sdtNguoiNhan: '', diachiLayHang: '', diachiGiaoHang: '', khoiLuong: 1, tienCOD: 0, phiVanChuyen: 0, hangHoa: [{ loaiHangHoa: '', soLuong: 1, trongLuong: 1 }] })
const texts = [['nguoiGui', 'Người gửi', 100], ['sdtNguoiGui', 'SĐT người gửi', 15], ['diachiLayHang', 'Địa chỉ lấy hàng', 255], ['nguoiNhan', 'Người nhận', 100], ['sdtNguoiNhan', 'SĐT người nhận', 15], ['diachiGiaoHang', 'Địa chỉ giao hàng', 255]] as const
const numbers = [['khoiLuong', 'Khối lượng (kg)'], ['tienCOD', 'Tiền COD (đ)'], ['phiVanChuyen', 'Phí vận chuyển (đ)']] as const
const events: Record<string, string> = { Tao: 'Tạo đơn', CapNhat: 'Cập nhật', GuiDuyet: 'Gửi duyệt', Duyet: 'Duyệt đơn', TuChoi: 'Từ chối', Huy: 'Hủy đơn', DoiTrangThai: 'Đổi trạng thái' }

const tien = (n: number) => n.toLocaleString('vi-VN')
const ngay = (s: string) => new Date(s).toLocaleDateString('vi-VN')
/** Số ngày kể từ lúc tạo đơn — dùng để phát hiện đơn tồn đọng ở màn theo dõi. */
const soNgayCho = (s: string) => Math.floor((Date.now() - new Date(s).getTime()) / 86400000)

const TIEU_DE: Record<string, [string, string]> = {
  'don-hang': ['Tạo đơn hàng', 'Tiếp nhận yêu cầu dịch vụ của khách hàng, nhập thông tin gửi – nhận – hàng hoá rồi gửi kiểm duyệt.'],
  'kiem-duyet': ['Kiểm duyệt đơn hàng', 'Kiểm tra thông tin đơn đang chờ và duyệt, hoặc trả về để nhân viên bổ sung.'],
  'tra-cuu': ['Tra cứu đơn hàng', 'Tra theo mã đơn, khách hàng hoặc thời gian tạo để trả lời khách và cung cấp thông tin cho bộ phận liên quan.'],
  'theo-doi': ['Theo dõi đơn hàng', 'Giám sát tiến độ xử lý toàn bộ đơn và phát hiện đơn tồn đọng cần can thiệp.'],
}

export function DonHangPage({ mode }: { mode: 'don-hang' | 'kiem-duyet' | 'tra-cuu' | 'theo-doi' }) {
  const { coChucNang } = usePermission()
  const [ds, setDs] = useState<DonHang[]>([])
  const [kh, setKh] = useState<KhachHang[]>([])
  const [dv, setDv] = useState<DichVu[]>([])
  const [filter, setFilter] = useState({ ma: '', khachHang: '', tuNgay: '', denNgay: '' })
  const [form, setForm] = useState<DonInput>(rong)
  const [edit, setEdit] = useState<string | null>(mode === 'don-hang' ? '' : null)
  const [selected, setSelected] = useState<DonHang | null>(null)
  const [history, setHistory] = useState<LichSu[]>([])
  const [reason, setReason] = useState('')
  const [action, setAction] = useState<{ id: string; kind: string } | null>(null)
  const [loi, setLoi] = useState('')
  const [ok, setOk] = useState('')
  const [busy, setBusy] = useState(false)
  const [loading, setLoading] = useState(true)

  async function tai() {
    setLoading(true)
    try { setDs((await http.get<DonHang[]>('/ht1/don-hang', { params: Object.fromEntries(Object.entries(filter).filter(([, v]) => v)) })).data) }
    catch (e) { setLoi(thongBaoLoi(e)) } finally { setLoading(false) }
  }
  useEffect(() => {
    let active = true
    void tai()
    Promise.all([http.get<KhachHang[]>('/ht1/khach-hang'), http.get<DichVu[]>('/ht1/dich-vu')])
      .then(([k, d]) => { if (active) { setKh(k.data); setDv(d.data) } })
      .catch((e) => { if (active) setLoi(thongBaoLoi(e)) })
    return () => { active = false }
  }, [])

  async function chiTiet(d: DonHang) {
    setBusy(true); setLoi('')
    try {
      const [detail, ls] = await Promise.all([
        http.get<DonHang>(`/ht1/don-hang/${d.maDonHang}`),
        http.get<LichSu[]>(`/ht1/don-hang/${d.maDonHang}/lich-su`),
      ])
      setSelected(detail.data); setHistory(ls.data)
    } catch (e) { setLoi(thongBaoLoi(e)) } finally { setBusy(false) }
  }
  async function luu(e: FormEvent) {
    e.preventDefault(); setBusy(true); setLoi(''); setOk('')
    try {
      const r = edit ? await http.put<DonHang>(`/ht1/don-hang/${edit}`, form) : await http.post<DonHang>('/ht1/don-hang', form)
      setOk(`Đã lưu đơn hàng ${r.data.maDonHang}. Bấm "Gửi duyệt" ở danh sách bên dưới để chuyển quản lý kiểm duyệt.`)
      setEdit(mode === 'don-hang' ? '' : null); setForm(rong()); setSelected(null); await tai()
    } catch (e) { setLoi(thongBaoLoi(e)) } finally { setBusy(false) }
  }
  async function xuLy(id: string, kind: string) {
    setBusy(true); setLoi(''); setOk('')
    try {
      await http.post(`/ht1/don-hang/${id}/${kind}`, { lyDo: reason })
      setOk(kind === 'tu-choi' ? 'Đơn hàng được trả về để bổ sung/chỉnh sửa' : kind === 'huy' ? 'Đã hủy đơn hàng' : kind === 'duyet' ? 'Đã duyệt đơn hàng' : 'Đã gửi đơn hàng chờ kiểm duyệt')
      setAction(null); setReason(''); setSelected(null); await tai()
    } catch (e) { setLoi(thongBaoLoi(e)); await tai() } finally { setBusy(false) }
  }

  const tenKhach = (ma: string) => kh.find((k) => k.maKhachHang === ma)?.tenKhachHang ?? ma
  const tenDichVu = (ma: string) => dv.find((d) => d.maDichVu === ma)?.tenDichVu ?? ma
  const duocSua = (d: DonHang) => d.trangThai === 'Đã tạo' && ['Chưa gửi', 'Cần bổ sung'].includes(d.kiemDuyet)
  const nhap = ds.filter(duocSua)
  const daGui = ds.filter((d) => !duocSua(d))
  const choDuyet = ds.filter((d) => d.kiemDuyet === 'Chờ duyệt' && d.trangThai === 'Đã tạo')
  const [tieuDe, moTa] = TIEU_DE[mode]

  const bieuMau = (
    <form className="ht1-editor" onSubmit={luu}>
      <h2>{edit ? `Chỉnh sửa đơn ${edit}` : 'Thông tin yêu cầu dịch vụ'}</h2>
      <p className="muted">Các trường đều bắt buộc. Mã đơn được hệ thống cấp tự động khi lưu.</p>
      <div className="form-grid">
        <label>Khách hàng<select className="select" required value={form.maKhachHang} onChange={(e) => setForm({ ...form, maKhachHang: e.target.value })}>
          <option value="">Chọn khách hàng</option>{kh.map((k) => <option key={k.maKhachHang} value={k.maKhachHang}>{k.maKhachHang} — {k.tenKhachHang}</option>)}</select></label>
        <label>Dịch vụ<select className="select" required value={form.maDichVu} onChange={(e) => setForm({ ...form, maDichVu: e.target.value })}>
          <option value="">Chọn dịch vụ</option>{dv.map((d) => <option key={d.maDichVu} value={d.maDichVu}>{d.tenDichVu}</option>)}</select></label>
        {texts.map(([k, l, max]) => <label key={k}>{l}<input className="input" required maxLength={max} value={form[k]} onChange={(e) => setForm({ ...form, [k]: e.target.value })} /></label>)}
        {numbers.map(([k, l]) => <label key={k}>{l}<input className="input" type="number" step="0.01" min={k === 'khoiLuong' ? 0.01 : 0} max={k === 'khoiLuong' ? 99999999.99 : 9999999999999.99} required value={form[k]} onChange={(e) => setForm({ ...form, [k]: Number(e.target.value) })} /></label>)}
      </div>
      <h3>Hàng hoá / kiện hàng</h3>
      {form.hangHoa.map((h, i) => <div className="ht1-goods" key={i}>
        <label>Loại hàng<input className="input" required maxLength={50} value={h.loaiHangHoa} onChange={(e) => setForm({ ...form, hangHoa: form.hangHoa.map((v, j) => j === i ? { ...v, loaiHangHoa: e.target.value } : v) })} /></label>
        <label>Số lượng<input className="input" required type="number" min={1} max={2147483647} step={1} value={h.soLuong} onChange={(e) => setForm({ ...form, hangHoa: form.hangHoa.map((v, j) => j === i ? { ...v, soLuong: Number(e.target.value) } : v) })} /></label>
        <label>Trọng lượng (kg)<input className="input" required type="number" min={0.01} step="0.01" value={h.trongLuong} onChange={(e) => setForm({ ...form, hangHoa: form.hangHoa.map((v, j) => j === i ? { ...v, trongLuong: Number(e.target.value) } : v) })} /></label>
        <button className="btn" type="button" disabled={form.hangHoa.length === 1} onClick={() => setForm({ ...form, hangHoa: form.hangHoa.filter((_, j) => j !== i) })}>Xoá dòng {i + 1}</button>
      </div>)}
      <div className="form-actions">
        <button type="button" className="btn" disabled={form.hangHoa.length >= 100} onClick={() => setForm({ ...form, hangHoa: [...form.hangHoa, { loaiHangHoa: '', soLuong: 1, trongLuong: 1 }] })}>Thêm kiện hàng</button>
        <button className="btn btn-chinh" disabled={busy}>{edit ? 'Lưu thay đổi' : 'Lưu đơn hàng'}</button>
        {(edit || mode !== 'don-hang') && <button className="btn" type="button" disabled={busy} onClick={() => { setEdit(mode === 'don-hang' ? '' : null); setForm(rong()) }}>Huỷ chỉnh sửa</button>}
      </div>
    </form>
  )

  const formLyDo = action && (
    <form className="ht1-editor" onSubmit={(e) => { e.preventDefault(); void xuLy(action.id, action.kind) }}>
      <h3>{action.kind === 'huy' ? 'Hủy' : 'Từ chối'} đơn {action.id}</h3>
      <label>Lý do bắt buộc<textarea className="textarea" required maxLength={300} value={reason} onChange={(e) => setReason(e.target.value)} /></label>
      <div className="form-actions"><button className="btn btn-nguy-hiem" disabled={busy}>Xác nhận</button><button type="button" className="btn" disabled={busy} onClick={() => setAction(null)}>Quay lại</button></div>
    </form>
  )

  return <>
    <div className="page-header"><h1 className="page-title">{tieuDe}</h1><p className="page-sub">{moTa}</p></div>
    {loi && <div role="alert" className="alert alert-loi">{loi}</div>}
    {ok && <div role="status" className="alert alert-thanh-cong">{ok}</div>}

    {/* ---------- Tạo đơn hàng: biểu mẫu là trung tâm, kèm việc còn dang dở ---------- */}
    {mode === 'don-hang' && <>
      <div className="card"><div className="card-header"><h3>Tiếp nhận yêu cầu dịch vụ</h3>
        <span className="phu">nhập trực tiếp, không cần mở thêm màn hình</span></div>
        <div className="card-body">{edit !== null ? bieuMau : <button className="btn btn-chinh" onClick={() => { setEdit(''); setForm(rong()) }}>Nhập đơn mới</button>}</div>
      </div>

      {formLyDo && <div className="card"><div className="card-body">{formLyDo}</div></div>}

      <div className="card"><div className="card-header"><h3>Đơn nháp cần xử lý</h3>
        <span className="phu">chưa gửi hoặc bị trả về bổ sung</span><span className="can-phai">{nhap.length} đơn</span></div>
        <div className="card-body khong-dem">
          <BangDon rows={nhap} loading={loading} tenKhach={tenKhach} trong="Không còn đơn nháp nào. Nhập đơn mới ở trên."
            thaoTac={(d) => <>
              <button className="btn btn-nho" disabled={busy} onClick={() => void chiTiet(d)}>Chi tiết</button>
              {coChucNang('HT1_CAP_NHAT_DON_HANG') && <button className="btn btn-nho" disabled={busy} onClick={() => { setEdit(d.maDonHang); setForm(d); setAction(null); window.scrollTo({ top: 0, behavior: 'smooth' }) }}>Sửa</button>}
              {coChucNang('HT1_TAO_DON_HANG') && <button className="btn btn-nho btn-chinh" disabled={busy} onClick={() => void xuLy(d.maDonHang, 'gui-duyet')}>Gửi duyệt</button>}
              {coChucNang('HT1_HUY_DON_HANG') && <button className="btn btn-nho btn-nguy-hiem" disabled={busy} onClick={() => { setReason(''); setAction({ id: d.maDonHang, kind: 'huy' }) }}>Hủy</button>}
            </>} />
        </div>
      </div>

      <div className="card"><div className="card-header"><h3>Đơn đã gửi kiểm duyệt</h3>
        <span className="phu">theo dõi kết quả duyệt của quản lý</span><span className="can-phai">{daGui.length} đơn</span></div>
        <div className="card-body khong-dem">
          <BangDon rows={daGui} loading={loading} tenKhach={tenKhach} trong="Bạn chưa gửi đơn nào đi kiểm duyệt."
            thaoTac={(d) => <button className="btn btn-nho" disabled={busy} onClick={() => void chiTiet(d)}>Chi tiết</button>} />
        </div>
      </div>
    </>}

    {/* ---------- Kiểm duyệt: hàng đợi đơn chờ duyệt ---------- */}
    {mode === 'kiem-duyet' && <>
      {formLyDo && <div className="card"><div className="card-body">{formLyDo}</div></div>}
      <div className="card"><div className="card-header"><h3>Đơn chờ kiểm duyệt</h3><span className="can-phai">{choDuyet.length} đơn</span></div>
        <div className="card-body khong-dem">
          <BangDon rows={choDuyet} loading={loading} tenKhach={tenKhach} trong="Không có đơn nào đang chờ kiểm duyệt."
            thaoTac={(d) => <>
              <button className="btn btn-nho" disabled={busy} onClick={() => void chiTiet(d)}>Chi tiết</button>
              <button className="btn btn-nho btn-chinh" disabled={busy} onClick={() => void xuLy(d.maDonHang, 'duyet')}>Duyệt</button>
              <button className="btn btn-nho" disabled={busy} onClick={() => { setReason(''); setAction({ id: d.maDonHang, kind: 'tu-choi' }) }}>Từ chối</button>
            </>} />
        </div>
      </div>
    </>}

    {/* ---------- Tra cứu: nhân viên tìm lại đơn của mình để trả lời khách ---------- */}
    {mode === 'tra-cuu' && <>
      <div className="card"><div className="card-body">
        <form className="search-filter" onSubmit={(e) => { e.preventDefault(); setLoi(''); void tai() }}>
          <label className="field">Mã đơn / mã vận đơn<input className="input" placeholder="DH0000001" value={filter.ma} onChange={(e) => setFilter({ ...filter, ma: e.target.value })} /></label>
          <label className="field">Mã hoặc tên khách hàng<input className="input" value={filter.khachHang} onChange={(e) => setFilter({ ...filter, khachHang: e.target.value })} /></label>
          <label className="field">Tạo từ ngày<input className="input" type="date" value={filter.tuNgay} onChange={(e) => setFilter({ ...filter, tuNgay: e.target.value })} /></label>
          <label className="field">Đến ngày<input className="input" type="date" value={filter.denNgay} onChange={(e) => setFilter({ ...filter, denNgay: e.target.value })} /></label>
          <div className="hanh-dong">
            <button className="btn btn-chinh" disabled={loading}>Tra cứu</button>
            <button type="button" className="btn" onClick={() => { setFilter({ ma: '', khachHang: '', tuNgay: '', denNgay: '' }); setTimeout(() => void tai(), 0) }}>Xoá lọc</button>
          </div>
        </form>
      </div></div>

      <div className="card"><div className="card-header"><h3>Kết quả tra cứu</h3>
        <span className="phu">đơn do bạn tiếp nhận</span><span className="can-phai">{ds.length} đơn</span></div>
        <div className="card-body khong-dem">
          {loading ? <p role="status" style={{ padding: 18 }}>Đang tra cứu…</p> : <div className="table-wrap"><table className="data-table"><thead><tr>
            <th>Mã đơn</th><th>Ngày tạo</th><th>Khách hàng</th><th>Người nhận</th><th>Địa chỉ giao</th><th>Trạng thái</th><th>Kiểm duyệt</th><th>Thao tác</th>
          </tr></thead><tbody>
            {ds.map((d) => <tr key={d.maDonHang}>
              <td className="mono">{d.maDonHang}</td><td>{ngay(d.ngayTao)}</td>
              <td>{tenKhach(d.maKhachHang)}</td><td>{d.nguoiNhan}<br /><span className="muted">{d.sdtNguoiNhan}</span></td>
              <td>{d.diachiGiaoHang}</td>
              <td><StatusBadge trangThai={d.trangThai} /></td><td><StatusBadge trangThai={d.kiemDuyet} /></td>
              <td><div className="hanh-dong"><button className="btn btn-nho" disabled={busy} onClick={() => void chiTiet(d)}>Chi tiết / lịch sử</button></div></td>
            </tr>)}
            {!ds.length && <tr><td colSpan={8}>Không tìm thấy đơn hàng nào khớp tiêu chí tra cứu.</td></tr>}
          </tbody></table></div>}
        </div>
      </div>
    </>}

    {/* ---------- Theo dõi: giám sát tiến độ toàn bộ đơn ---------- */}
    {mode === 'theo-doi' && <>
      <div className="stat-grid">
        <div className="stat-card"><div className="nhan">Tổng đơn</div><div className="gia-tri">{ds.length}</div><div className="ghi-chu">theo tiêu chí đang lọc</div></div>
        <div className="stat-card"><div className="nhan">Chờ duyệt</div><div className="gia-tri">{choDuyet.length}</div><div className="ghi-chu">cần quản lý kiểm duyệt</div></div>
        <div className="stat-card"><div className="nhan">Cần bổ sung</div><div className="gia-tri">{ds.filter((d) => d.kiemDuyet === 'Cần bổ sung').length}</div><div className="ghi-chu">đã trả về nhân viên</div></div>
        <div className="stat-card"><div className="nhan">Tồn đọng</div><div className="gia-tri">{ds.filter((d) => d.trangThai === 'Đã tạo' && soNgayCho(d.ngayTao) >= 2).length}</div><div className="ghi-chu">chưa vào kho sau 2 ngày</div></div>
      </div>

      <div className="card"><div className="card-body">
        <form className="search-filter" onSubmit={(e) => { e.preventDefault(); setLoi(''); void tai() }}>
          <label className="field">Mã đơn<input className="input" value={filter.ma} onChange={(e) => setFilter({ ...filter, ma: e.target.value })} /></label>
          <label className="field">Mã hoặc tên khách hàng<input className="input" value={filter.khachHang} onChange={(e) => setFilter({ ...filter, khachHang: e.target.value })} /></label>
          <label className="field">Từ ngày<input className="input" type="date" value={filter.tuNgay} onChange={(e) => setFilter({ ...filter, tuNgay: e.target.value })} /></label>
          <label className="field">Đến ngày<input className="input" type="date" value={filter.denNgay} onChange={(e) => setFilter({ ...filter, denNgay: e.target.value })} /></label>
          <div className="hanh-dong">
            <button className="btn btn-chinh" disabled={loading}>Tra cứu</button>
            <button type="button" className="btn" onClick={() => { setFilter({ ma: '', khachHang: '', tuNgay: '', denNgay: '' }); setTimeout(() => void tai(), 0) }}>Xoá lọc</button>
          </div>
        </form>
      </div></div>

      <div className="card"><div className="card-header"><h3>Tiến độ xử lý</h3>
        <span className="phu">đơn quá 2 ngày chưa vào kho được đánh dấu để kiểm tra</span></div>
        <div className="card-body khong-dem">
          {loading ? <p role="status" style={{ padding: 18 }}>Đang tải đơn hàng…</p> : <div className="table-wrap"><table className="data-table"><thead><tr>
            <th>Mã đơn</th><th>Khách hàng</th><th>Dịch vụ</th><th>Ngày tạo</th><th className="so">Số ngày</th><th>Trạng thái</th><th>Kiểm duyệt</th><th className="so">COD</th><th>Thao tác</th>
          </tr></thead><tbody>
            {ds.map((d) => {
              const treHan = d.trangThai === 'Đã tạo' && soNgayCho(d.ngayTao) >= 2
              return <tr key={d.maDonHang}>
                <td className="mono">{d.maDonHang}</td>
                <td>{tenKhach(d.maKhachHang)}</td>
                <td>{tenDichVu(d.maDichVu)}</td>
                <td>{ngay(d.ngayTao)}</td>
                <td className="so">{treHan ? <span className="badge canh-bao">{soNgayCho(d.ngayTao)} ngày</span> : soNgayCho(d.ngayTao)}</td>
                <td><StatusBadge trangThai={d.trangThai} /></td>
                <td><StatusBadge trangThai={d.kiemDuyet} /></td>
                <td className="so">{tien(d.tienCOD)}</td>
                <td><div className="hanh-dong"><button className="btn btn-nho" disabled={busy} onClick={() => void chiTiet(d)}>Chi tiết / lịch sử</button></div></td>
              </tr>
            })}
            {!ds.length && <tr><td colSpan={9}>Không có đơn hàng phù hợp với tiêu chí đã chọn.</td></tr>}
          </tbody></table></div>}
        </div>
      </div>
    </>}

    {selected && <div className="card"><div className="card-header"><h3>Đơn hàng {selected.maDonHang}</h3>
      <span className="phu">{tenKhach(selected.maKhachHang)} · {tenDichVu(selected.maDichVu)}</span>
      <span className="can-phai"><button className="btn btn-nho" onClick={() => setSelected(null)}>Đóng</button></span></div>
      <div className="card-body">
        <div className="form-grid">
          <div className="field"><span className="label">Người gửi</span><span>{selected.nguoiGui} · {selected.sdtNguoiGui}<br /><span className="muted">{selected.diachiLayHang}</span></span></div>
          <div className="field"><span className="label">Người nhận</span><span>{selected.nguoiNhan} · {selected.sdtNguoiNhan}<br /><span className="muted">{selected.diachiGiaoHang}</span></span></div>
          <div className="field"><span className="label">Khối lượng</span><span>{selected.khoiLuong} kg</span></div>
          <div className="field"><span className="label">Tiền COD</span><span>{tien(selected.tienCOD)} đ</span></div>
          <div className="field"><span className="label">Phí vận chuyển</span><span>{tien(selected.phiVanChuyen)} đ</span></div>
          <div className="field"><span className="label">Hàng hoá</span><span>{selected.hangHoa.map((h) => `${h.loaiHangHoa} — ${h.soLuong} kiện · ${h.trongLuong} kg`).join('; ')}</span></div>
        </div>
        {selected.lyDoHuy && <div className="alert alert-loi" style={{ marginTop: 14 }}>Lý do hủy: {selected.lyDoHuy}</div>}
        <h4 style={{ margin: '16px 0 8px', fontSize: 13 }}>Lịch sử xử lý</h4>
        <ul className="danh-sach-moc">
          {history.map((h) => <li key={h.maLichSu}>
            <b>{events[h.hanhDong] ?? h.hanhDong}</b> · <span className="muted">{new Date(h.thoiGian).toLocaleString('vi-VN')} · {h.maTaiKhoan} · {h.trangThai}</span>
            {h.ghiChu ? ` — ${h.ghiChu}` : ''}
          </li>)}
          {!history.length && <li className="muted">Chưa có lịch sử.</li>}
        </ul>
      </div>
    </div>}
  </>
}

function BangDon({ rows, loading, tenKhach, thaoTac, trong }: {
  rows: DonHang[]; loading: boolean; tenKhach: (ma: string) => string; thaoTac: (d: DonHang) => ReactNode; trong: string
}) {
  if (loading) return <p role="status" style={{ padding: 18 }}>Đang tải đơn hàng…</p>
  return <div className="table-wrap"><table className="data-table"><thead><tr>
    <th>Mã đơn</th><th>Ngày tạo</th><th>Khách hàng</th><th>Người nhận</th><th className="so">COD</th><th>Kiểm duyệt</th><th>Thao tác</th>
  </tr></thead><tbody>
    {rows.map((d) => <tr key={d.maDonHang}>
      <td className="mono">{d.maDonHang}</td><td>{ngay(d.ngayTao)}</td>
      <td>{tenKhach(d.maKhachHang)}</td><td>{d.nguoiNhan}</td>
      <td className="so">{tien(d.tienCOD)}</td><td><StatusBadge trangThai={d.kiemDuyet} /></td>
      <td><div className="hanh-dong ht1-actions">{thaoTac(d)}</div></td>
    </tr>)}
    {!rows.length && <tr><td colSpan={7}>{trong}</td></tr>}
  </tbody></table></div>
}
