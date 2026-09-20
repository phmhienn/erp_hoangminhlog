import { useEffect, useState, type FormEvent } from 'react'
import { http, thongBaoLoi } from '@/shared/api/http'
import { StatusBadge } from '@/shared/components/StatusBadge'

interface GiaoDich {
  maGiaoDich: string; maDonHang: string; tenKhachHang?: string | null; trangThaiDonHang?: string | null
  phaiThu: number; thucThu?: number | null; chenhLech?: number | null; trangThai: string; ngayTao: string; nguoiCapNhat?: string | null
  lyDoSaiLech?: string | null; nguoiXuLySaiLech?: string | null; thoiGianXuLySaiLech?: string | null
}
interface Phieu {
  maPhieu: string; loaiPhieu: string; maGiaoDich?: string | null; maDonHang?: string | null; soTien: number
  ngayLap: string; noiDung?: string | null; trangThai: string; nguoiLap?: string | null; nguoiDuyet?: string | null
  nguoiXacNhan?: string | null; thoiGianXacNhan?: string | null
}
interface DoiSoat { maDoiSoat: string; ngayDoiSoat: string; tongTien: number; chenhLech: number; trangThai: string; nguoiDoiSoat?: string | null; thoiGian?: string | null; maGiaoDich: string[] }
interface SoQuy { maSoQuy: string; maPhieu: string; thu: number; chi: number; ngayGhiSo: string }
interface CongNo { maCongNo: string; maKhachHang: string; tenKhachHang?: string | null; phaiTra: number; daTra: number; conLai: number; trangThai: string; ngayCapNhat?: string | null }
interface LichSu { thoiGian?: string | null; loai: string; ma: string; maDonHang?: string | null; soTien?: number | null; trangThai?: string | null; nguoiThucHien?: string | null; moTa?: string | null }
interface BaoCao { tuNgay: string; denNgay: string; soGiaoDich: number; tongPhaiThu: number; tongThucThu: number; tongDaChiTra: number; conPhaiTra: number; soGiaoDichSaiLech: number; saiLech: GiaoDich[]; soDuQuy: number; theoTrangThai: Record<string, number>; thongBao?: string | null }

const tien = (n?: number | null) => (n == null ? '—' : n.toLocaleString('vi-VN'))
const homNay = () => new Date().toISOString().slice(0, 10)
const dauThang = () => homNay().slice(0, 8) + '01'
const gio = (s?: string | null) => (s ? new Date(s).toLocaleString('vi-VN', { day: '2-digit', month: '2-digit', hour: '2-digit', minute: '2-digit' }) : '—')

/** Bộ lọc tại chỗ cho nhật ký giao dịch — dữ liệu đã tải sẵn nên gõ tới đâu lọc tới đó. */
interface LocLichSu { tuKhoa: string; loai: string; trangThai: string; tuNgay: string; denNgay: string }
const LOC_LS_RONG: LocLichSu = { tuKhoa: '', loai: '', trangThai: '', tuNgay: '', denNgay: '' }
function khopLichSu(l: LichSu, f: LocLichSu) {
  const k = f.tuKhoa.trim().toLowerCase()
  if (k && ![l.ma, l.maDonHang ?? '', l.nguoiThucHien ?? '', l.moTa ?? ''].some((v) => v.toLowerCase().includes(k))) return false
  if (f.loai && l.loai !== f.loai) return false
  if (f.trangThai && l.trangThai !== f.trangThai) return false
  const d = (l.thoiGian ?? '').slice(0, 10)
  if (f.tuNgay && (!d || d < f.tuNgay)) return false
  if (f.denNgay && (!d || d > f.denNgay)) return false
  return true
}

/** Mỗi màn hình là một usecase của biểu đồ 3.5.3; thứ tự theo actor Kế toán viên → Thủ quỹ → Kế toán trưởng. */
const TIEU_DE: Record<string, [string, string]> = {
  'cod': ['Tra cứu dữ liệu COD', 'Tra cứu tiền thu hộ theo đơn hàng, khách hàng và trạng thái thu/chi; giao dịch được hệ thống tiếp nhận khi đơn chuyển sang "Đã giao" (3.5.1 bước 1).'],
  'doi-soat': ['Đối soát COD', 'Đối soát theo đợt: chọn các giao dịch cần đối chiếu, hệ thống tính chênh lệch giữa tiền phải thu và thực thu (3.5.1 bước 2).'],
  'phieu-thu': ['Lập phiếu thu', 'Ghi nhận tiền COD đã thu về theo từng giao dịch; phiếu chờ kế toán trưởng phê duyệt (3.5.1 bước 3).'],
  'phieu-chi': ['Lập phiếu chi', 'Lập phiếu hoàn trả tiền thu hộ cho khách hàng/đối tác sau khi đủ điều kiện đối soát (3.5.1 bước 4).'],
  'sai-lech': ['Quản lý sai lệch COD', 'Giao dịch có tiền thực thu khác tiền phải thu; ghi nhận nguyên nhân và hướng xử lý để phục vụ đối soát.'],
  'xac-nhan-thu': ['Xác nhận thu tiền', 'Thủ quỹ kiểm tiền mặt thực nhận rồi xác nhận phiếu thu đã duyệt; hệ thống ghi sổ quỹ ngay sau đó.'],
  'xac-nhan-chi': ['Xác nhận chi trả COD', 'Thủ quỹ xác nhận đã chi trả tiền thu hộ theo phiếu chi đã duyệt; hệ thống ghi sổ quỹ và trừ công nợ.'],
  'xac-nhan': ['Cập nhật trạng thái giao dịch', 'Cập nhật số tiền thực thu và trạng thái của từng giao dịch COD theo kết quả thực hiện.'],
  'phe-duyet': ['Phê duyệt giao dịch', 'Kế toán trưởng kiểm tra và phê duyệt phiếu thu/chi trước khi chuyển cho thủ quỹ thực hiện.'],
  'lich-su': ['Tra cứu lịch sử giao dịch', 'Nhật ký thao tác trên giao dịch COD, phiếu thu/chi và các đợt đối soát, kèm người thực hiện và thời gian.'],
  'bao-cao': ['Xem báo cáo thu – chi COD', 'Tổng hợp tiền COD đã thu, đã chi trả, còn phải trả, giao dịch sai lệch, sổ quỹ và công nợ theo khoảng thời gian (3.5.1 bước 6).'],
}

export function Ht5Page({ mode }: { mode: string }) {
  const [gd, setGd] = useState<GiaoDich[]>([])
  const [phieu, setPhieu] = useState<Phieu[]>([])
  const [dot, setDot] = useState<DoiSoat[]>([])
  const [soQuy, setSoQuy] = useState<SoQuy[]>([])
  const [congNo, setCongNo] = useState<CongNo[]>([])
  const [lichSu, setLichSu] = useState<LichSu[]>([])
  const [baoCao, setBaoCao] = useState<BaoCao | null>(null)
  const [loc, setLoc] = useState('')
  const [tuKhoa, setTuKhoa] = useState('')
  const [ky, setKy] = useState({ tuNgay: dauThang(), denNgay: homNay() })
  const [chon, setChon] = useState<string[]>([])
  const [ngayDoiSoat, setNgayDoiSoat] = useState(homNay())
  const [capNhat, setCapNhat] = useState<{ ma: string; trangThai: string; soTienThucThu: string } | null>(null)
  const [xuLy, setXuLy] = useState<{ ma: string; lyDoSaiLech: string; trangThai: string } | null>(null)
  const [formPhieu, setFormPhieu] = useState({ maGiaoDich: '', soTien: '', ngayLap: homNay(), noiDung: '' })
  const [locLs, setLocLs] = useState({ tuKhoa: '', loai: '', trangThai: '', tuNgay: '', denNgay: '' })
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)
  const [loi, setLoi] = useState('')
  const [ok, setOk] = useState('')

  const laPhieuThu = mode === 'phieu-thu' || mode === 'xac-nhan-thu'
  const loaiPhieu = laPhieuThu ? 'Thu' : 'Chi'

  async function tai() {
    setLoading(true); setLoi('')
    try {
      if (mode === 'bao-cao') {
        setBaoCao((await http.get<BaoCao>('/ht5/bao-cao', { params: ky })).data)
        setSoQuy((await http.get<SoQuy[]>('/ht5/so-quy')).data)
        setCongNo((await http.get<CongNo[]>('/ht5/cong-no')).data)
      } else if (mode === 'lich-su') setLichSu((await http.get<LichSu[]>('/ht5/lich-su')).data)
      else if (mode === 'sai-lech') setGd((await http.get<GiaoDich[]>('/ht5/sai-lech')).data)
      else if (mode === 'phe-duyet' || mode === 'xac-nhan-thu' || mode === 'xac-nhan-chi') setPhieu((await http.get<Phieu[]>('/ht5/phieu-thu-chi')).data)
      else if (mode === 'phieu-thu' || mode === 'phieu-chi') {
        setPhieu((await http.get<Phieu[]>('/ht5/phieu-thu-chi')).data)
        setGd((await http.get<GiaoDich[]>('/ht5/giao-dich')).data)
      } else if (mode === 'doi-soat') {
        setGd((await http.get<GiaoDich[]>('/ht5/giao-dich')).data)
        setDot((await http.get<DoiSoat[]>('/ht5/doi-soat')).data)
      } else setGd((await http.get<GiaoDich[]>('/ht5/giao-dich', { params: { trangThai: loc || undefined, tuKhoa: tuKhoa || undefined } })).data)
    } catch (e) { setLoi(thongBaoLoi(e)) } finally { setLoading(false) }
  }
  useEffect(() => { setChon([]); setOk(''); setCapNhat(null); setXuLy(null); setLocLs(LOC_LS_RONG); void tai() }, [mode])

  async function chay(viec: () => Promise<void>, thongBao: string) {
    setBusy(true); setLoi(''); setOk('')
    try { await viec(); setOk(thongBao); await tai() }
    catch (e) { setLoi(thongBaoLoi(e)) } finally { setBusy(false) }
  }

  const [tieuDe, moTa] = TIEU_DE[mode] ?? TIEU_DE['cod']
  const chuaDoiSoat = gd.filter((g) => g.trangThai !== 'Đã đối soát')
  const tongChon = gd.filter((g) => chon.includes(g.maGiaoDich))
  const lechChon = tongChon.reduce((s, g) => s + (g.phaiThu - (g.thucThu ?? 0)), 0)
  const phieuTheoLoai = phieu.filter((p) => p.loaiPhieu.toLowerCase() === loaiPhieu.toLowerCase())
  const lichSuLoc = lichSu.filter((l) => khopLichSu(l, locLs))
  const loaiLichSu = [...new Set(lichSu.map((l) => l.loai))].sort()
  const trangThaiLichSu = [...new Set(lichSu.map((l) => l.trangThai).filter((x): x is string => !!x))].sort()

  return <>
    <div className="page-header"><h1 className="page-title">{tieuDe}</h1><p className="page-sub">{moTa}</p></div>
    {loi && <div role="alert" className="alert alert-loi">{loi}</div>}
    {ok && <div role="status" className="alert alert-thanh-cong">{ok}</div>}

    {/* ---------- Kế toán viên · Tra cứu dữ liệu COD ---------- */}
    {mode === 'cod' && <>
      <div className="card"><div className="card-body">
        <form className="search-filter" onSubmit={(e) => { e.preventDefault(); void tai() }}>
          <label className="field">Mã đơn / mã giao dịch / khách hàng<input className="input" placeholder="DH0000001 · Minh Phát" value={tuKhoa} onChange={(e) => setTuKhoa(e.target.value)} /></label>
          <label className="field">Trạng thái thu/chi<select className="select" value={loc} onChange={(e) => setLoc(e.target.value)}>
            <option value="">Tất cả trạng thái</option>{['Chưa thu', 'Đã thu', 'Sai lệch', 'Đã đối soát'].map((t) => <option key={t}>{t}</option>)}
          </select></label>
          <div className="hanh-dong">
            <button className="btn btn-chinh" disabled={loading}>Tra cứu</button>
            <button type="button" className="btn" onClick={() => { setTuKhoa(''); setLoc(''); setTimeout(() => void tai(), 0) }}>Xoá lọc</button>
          </div>
        </form>
      </div></div>
      <div className="stat-grid">
        <div className="stat-card"><div className="nhan">Giao dịch</div><div className="gia-tri">{gd.length}</div><div className="ghi-chu">theo tiêu chí đang lọc</div></div>
        <div className="stat-card"><div className="nhan">Phải thu</div><div className="gia-tri">{tien(gd.reduce((s, g) => s + g.phaiThu, 0))}</div><div className="ghi-chu">đồng</div></div>
        <div className="stat-card"><div className="nhan">Thực thu</div><div className="gia-tri">{tien(gd.reduce((s, g) => s + (g.thucThu ?? 0), 0))}</div><div className="ghi-chu">đồng</div></div>
        <div className="stat-card"><div className="nhan">Sai lệch</div><div className="gia-tri">{gd.filter((g) => g.chenhLech != null && g.chenhLech !== 0).length}</div><div className="ghi-chu">giao dịch lệch tiền</div></div>
      </div>
      <div className="card"><div className="card-header"><h3>Giao dịch COD</h3>
        <span className="phu">hệ thống tự tiếp nhận từ đơn đã giao, không nhập tay</span></div>
        <div className="card-body khong-dem"><BangGiaoDich rows={gd} loading={loading} chiTiet /></div>
      </div>
    </>}

    {/* ---------- Kế toán viên · Quản lý sai lệch COD ---------- */}
    {mode === 'sai-lech' && <>
      {xuLy && <div className="card"><div className="card-body"><form className="ht1-editor" onSubmit={(e: FormEvent) => {
        e.preventDefault()
        void chay(async () => { await http.put(`/ht5/sai-lech/${xuLy.ma}`, { lyDoSaiLech: xuLy.lyDoSaiLech, trangThai: xuLy.trangThai || null }); setXuLy(null) }, `Đã ghi nhận xử lý sai lệch cho ${xuLy.ma}`)
      }}>
        <h2>Ghi nhận sai lệch · {xuLy.ma}</h2>
        <div className="form-grid">
          <label>Trạng thái sau xử lý<select className="select" value={xuLy.trangThai} onChange={(e) => setXuLy({ ...xuLy, trangThai: e.target.value })}>
            <option value="">Giữ nguyên</option><option>Sai lệch</option><option>Đã thu</option></select></label>
          <label className="form-span">Nguyên nhân và hướng xử lý *<input className="input" required maxLength={255}
            placeholder="Ví dụ: khách trả thiếu 50.000đ, shipper đã bù và thu lại ngày 18/09" value={xuLy.lyDoSaiLech} onChange={(e) => setXuLy({ ...xuLy, lyDoSaiLech: e.target.value })} /></label>
        </div>
        <div className="form-actions"><button className="btn btn-chinh" disabled={busy || !xuLy.lyDoSaiLech.trim()}>Lưu ghi nhận</button><button type="button" className="btn" onClick={() => setXuLy(null)}>Đóng</button></div>
      </form></div></div>}

      <div className="card"><div className="card-header"><h3>Giao dịch lệch tiền</h3><span className="phu">{gd.length} giao dịch có thực thu khác phải thu</span></div>
        <div className="card-body khong-dem">
          {loading ? <p role="status" style={{ padding: 18 }}>Đang tải…</p> : <div className="table-wrap"><table className="data-table"><thead><tr>
            <th>Giao dịch</th><th>Đơn hàng</th><th>Khách hàng</th><th className="so">Phải thu</th><th className="so">Thực thu</th><th className="so">Chênh lệch</th><th>Trạng thái</th><th>Ghi nhận xử lý</th><th>Thao tác</th>
          </tr></thead><tbody>
            {gd.map((g) => <tr key={g.maGiaoDich}>
              <td className="mono">{g.maGiaoDich}</td><td className="mono">{g.maDonHang}</td><td>{g.tenKhachHang ?? '—'}</td>
              <td className="so">{tien(g.phaiThu)}</td><td className="so">{tien(g.thucThu)}</td><td className="so">{tien(g.chenhLech)}</td>
              <td><StatusBadge trangThai={g.trangThai} /></td>
              <td>{g.lyDoSaiLech ? <>{g.lyDoSaiLech}<br /><span className="muted">{g.nguoiXuLySaiLech} · {gio(g.thoiGianXuLySaiLech)}</span></> : <span className="muted">Chưa ghi nhận</span>}</td>
              <td><div className="hanh-dong">{g.trangThai === 'Đã đối soát'
                ? <span className="muted">Đã khoá</span>
                : <button className="btn btn-nho" disabled={busy} onClick={() => setXuLy({ ma: g.maGiaoDich, lyDoSaiLech: g.lyDoSaiLech ?? '', trangThai: '' })}>Ghi nhận</button>}</div></td>
            </tr>)}
            {!gd.length && <tr><td colSpan={9}>Không có giao dịch nào lệch tiền. Sai lệch phát sinh khi thủ quỹ cập nhật số thực thu khác số phải thu.</td></tr>}
          </tbody></table></div>}
        </div>
      </div>
    </>}

    {/* ---------- Thủ quỹ · Cập nhật trạng thái giao dịch ---------- */}
    {mode === 'xac-nhan' && <div className="card"><div className="card-body">
      {capNhat && <form className="ht1-editor" onSubmit={(e: FormEvent) => {
        e.preventDefault()
        void chay(async () => { await http.put(`/ht5/giao-dich/${capNhat.ma}`, { trangThai: capNhat.trangThai, soTienThucThu: capNhat.soTienThucThu === '' ? null : Number(capNhat.soTienThucThu) }); setCapNhat(null) }, 'Cập nhật trạng thái giao dịch thành công')
      }}>
        <h2>Cập nhật giao dịch {capNhat.ma}</h2>
        <div className="form-grid">
          <label>Trạng thái mới
            <select className="select" value={capNhat.trangThai} onChange={(e) => setCapNhat({ ...capNhat, trangThai: e.target.value })}>
              {['Chưa thu', 'Đã thu', 'Sai lệch'].map((t) => <option key={t}>{t}</option>)}
            </select>
          </label>
          <label>Số tiền thực thu<input className="input" type="number" min={0} value={capNhat.soTienThucThu} onChange={(e) => setCapNhat({ ...capNhat, soTienThucThu: e.target.value })} /></label>
        </div>
        <div className="form-actions"><button className="btn btn-chinh" disabled={busy}>Lưu cập nhật</button><button type="button" className="btn" onClick={() => setCapNhat(null)}>Đóng</button></div>
      </form>}
      <BangGiaoDich rows={gd} loading={loading} thaoTac={(g) => g.trangThai === 'Đã đối soát' ? <span className="muted">Đã khoá</span>
        : <button className="btn btn-nho" onClick={() => setCapNhat({ ma: g.maGiaoDich, trangThai: g.trangThai, soTienThucThu: String(g.thucThu ?? g.phaiThu) })}>Cập nhật</button>} />
    </div></div>}

    {/* ---------- Kế toán viên · Đối soát COD ---------- */}
    {mode === 'doi-soat' && <>
      <div className="card"><div className="card-header"><h3>Chọn giao dịch cho đợt đối soát</h3><span className="phu">{chon.length} giao dịch · chênh lệch dự kiến {tien(lechChon)} đ</span></div>
        <div className="card-body">
          <form className="form-actions" onSubmit={(e) => {
            e.preventDefault()
            void chay(async () => { await http.post('/ht5/doi-soat', { ngayDoiSoat, maGiaoDich: chon }); setChon([]) }, 'Đã lập đợt đối soát COD')
          }}>
            <label className="field" style={{ flex: 'none' }}>Ngày đối soát<input className="input" type="date" value={ngayDoiSoat} onChange={(e) => setNgayDoiSoat(e.target.value)} /></label>
            <button className="btn btn-chinh" disabled={busy || !chon.length}>Lập đợt đối soát</button>
          </form>
          <div className="table-wrap"><table className="data-table"><thead><tr>
            <th style={{ width: 44 }}><span className="muted">Chọn</span></th><th>Giao dịch</th><th>Đơn hàng</th><th className="so">Phải thu</th><th className="so">Thực thu</th><th className="so">Chênh lệch</th><th>Trạng thái</th>
          </tr></thead><tbody>
            {chuaDoiSoat.map((g) => <tr key={g.maGiaoDich}>
              <td><input type="checkbox" aria-label={`Chọn ${g.maGiaoDich}`} checked={chon.includes(g.maGiaoDich)}
                onChange={(e) => setChon(e.target.checked ? [...chon, g.maGiaoDich] : chon.filter((x) => x !== g.maGiaoDich))} /></td>
              <td className="mono">{g.maGiaoDich}</td><td className="mono">{g.maDonHang}</td>
              <td className="so">{tien(g.phaiThu)}</td><td className="so">{tien(g.thucThu)}</td>
              <td className="so">{tien(g.phaiThu - (g.thucThu ?? 0))}</td><td><StatusBadge trangThai={g.trangThai} /></td>
            </tr>)}
            {!loading && !chuaDoiSoat.length && <tr><td colSpan={7}>Không còn giao dịch nào chờ đối soát.</td></tr>}
          </tbody></table></div>
        </div>
      </div>
      <div className="card"><div className="card-header"><h3>Các đợt đã đối soát</h3></div><div className="card-body khong-dem">
        <div className="table-wrap"><table className="data-table"><thead><tr><th>Mã đợt</th><th>Ngày</th><th className="so">Tổng tiền</th><th className="so">Chênh lệch</th><th>Kết quả</th><th>Giao dịch</th></tr></thead><tbody>
          {dot.map((d) => <tr key={d.maDoiSoat}>
            <td className="mono">{d.maDoiSoat}</td><td>{d.ngayDoiSoat}</td><td className="so">{tien(d.tongTien)}</td>
            <td className="so">{tien(d.chenhLech)}</td><td><StatusBadge trangThai={d.chenhLech ? 'Sai lệch' : 'Đã duyệt'} nhan={d.trangThai} /></td>
            <td className="mono">{d.maGiaoDich.join(', ') || '—'}</td>
          </tr>)}
          {!dot.length && <tr><td colSpan={6}>Chưa có đợt đối soát nào.</td></tr>}
        </tbody></table></div>
      </div></div>
    </>}

    {/* ---------- Kế toán viên · Lập phiếu thu / Lập phiếu chi ---------- */}
    {(mode === 'phieu-thu' || mode === 'phieu-chi') && <>
      <div className="card"><div className="card-header"><h3>{laPhieuThu ? 'Lập phiếu thu tiền COD' : 'Lập phiếu chi hoàn trả COD'}</h3>
        <span className="phu">phiếu lập xong ở trạng thái "Chờ duyệt", kế toán trưởng duyệt rồi thủ quỹ mới thực hiện</span></div><div className="card-body">
        <form onSubmit={(e: FormEvent) => {
          e.preventDefault()
          void chay(async () => {
            await http.post('/ht5/phieu-thu-chi', { ...formPhieu, loaiPhieu, maGiaoDich: formPhieu.maGiaoDich || null, soTien: Number(formPhieu.soTien) })
            setFormPhieu({ maGiaoDich: '', soTien: '', ngayLap: homNay(), noiDung: '' })
          }, `Đã lập phiếu ${loaiPhieu.toLowerCase()}, chờ kế toán trưởng phê duyệt`)
        }}>
          <div className="form-grid">
            <label>Giao dịch COD<select className="select" value={formPhieu.maGiaoDich} onChange={(e) => {
              const g = gd.find((x) => x.maGiaoDich === e.target.value)
              setFormPhieu({ ...formPhieu, maGiaoDich: e.target.value, soTien: g ? String(g.thucThu ?? g.phaiThu) : formPhieu.soTien })
            }}>
              <option value="">Không gắn giao dịch</option>{gd.map((g) => <option key={g.maGiaoDich} value={g.maGiaoDich}>{g.maGiaoDich} · đơn {g.maDonHang} · {tien(g.phaiThu)} đ</option>)}
            </select></label>
            <label>Số tiền *<input className="input" type="number" min={0} required value={formPhieu.soTien} onChange={(e) => setFormPhieu({ ...formPhieu, soTien: e.target.value })} /></label>
            <label>Ngày lập *<input className="input" type="date" required value={formPhieu.ngayLap} onChange={(e) => setFormPhieu({ ...formPhieu, ngayLap: e.target.value })} /></label>
            <label className="form-span">Nội dung<input className="input" maxLength={100} placeholder={laPhieuThu ? 'Thu hộ đơn DH0000001' : 'Hoàn trả tiền thu hộ cho khách hàng'} value={formPhieu.noiDung} onChange={(e) => setFormPhieu({ ...formPhieu, noiDung: e.target.value })} /></label>
          </div>
          <div className="form-actions"><button className="btn btn-chinh" disabled={busy}>Lập phiếu {loaiPhieu.toLowerCase()}</button></div>
        </form>
      </div></div>
      <div className="card"><div className="card-header"><h3>Phiếu {loaiPhieu.toLowerCase()} đã lập</h3><span className="phu">{phieuTheoLoai.length} phiếu</span></div>
        <div className="card-body khong-dem"><BangPhieu rows={phieuTheoLoai} loading={loading} /></div></div>
    </>}

    {/* ---------- Kế toán trưởng · Phê duyệt giao dịch ---------- */}
    {mode === 'phe-duyet' && <>
      <div className="stat-grid">
        {['Chờ duyệt', 'Đã duyệt', 'Đã thực hiện'].map((t) => (
          <div className="stat-card" key={t}><div className="nhan">{t}</div>
            <div className="gia-tri">{phieu.filter((p) => p.trangThai === t).length}</div>
            <div className="ghi-chu">phiếu thu/chi</div></div>
        ))}
      </div>
      <div className="card"><div className="card-header"><h3>Phiếu cần kiểm soát</h3>
        <span className="phu">duyệt xong phiếu chuyển sang thủ quỹ xác nhận thực tế</span></div>
        <div className="card-body khong-dem">
          <BangPhieu rows={phieu} loading={loading} thaoTac={(p) => p.trangThai === 'Chờ duyệt'
            ? <button className="btn btn-nho btn-chinh" disabled={busy} onClick={() => void chay(async () => { await http.post(`/ht5/phieu-thu-chi/${p.maPhieu}/phe-duyet`) }, `Đã duyệt phiếu ${p.maPhieu}, chuyển thủ quỹ thực hiện`)}>Phê duyệt</button>
            : <span className="muted">{p.nguoiDuyet ? `Duyệt bởi ${p.nguoiDuyet}` : '—'}</span>} />
        </div>
      </div>
    </>}

    {/* ---------- Thủ quỹ · Xác nhận thu tiền / Xác nhận chi trả COD ---------- */}
    {(mode === 'xac-nhan-thu' || mode === 'xac-nhan-chi') && <>
      <div className="card"><div className="card-header"><h3>Phiếu {loaiPhieu.toLowerCase()} chờ thủ quỹ thực hiện</h3>
        <span className="phu">{phieuTheoLoai.filter((p) => p.trangThai === 'Đã duyệt').length} phiếu đã duyệt · xác nhận xong hệ thống ghi sổ quỹ và cập nhật công nợ</span></div>
        <div className="card-body khong-dem">
          <BangPhieu rows={phieuTheoLoai} loading={loading} thaoTac={(p) => p.trangThai === 'Đã duyệt'
            ? <button className="btn btn-nho btn-chinh" disabled={busy} onClick={() => void chay(async () => { await http.post(`/ht5/phieu-thu-chi/${p.maPhieu}/xac-nhan`) }, laPhieuThu ? `Đã xác nhận thu tiền phiếu ${p.maPhieu}` : `Đã xác nhận chi trả phiếu ${p.maPhieu}`)}>{laPhieuThu ? 'Xác nhận đã thu' : 'Xác nhận đã chi'}</button>
            : p.trangThai === 'Đã thực hiện' ? <span className="muted">{p.nguoiXacNhan} · {gio(p.thoiGianXacNhan)}</span>
              : <span className="muted">Chờ kế toán trưởng duyệt</span>} />
        </div>
      </div>
    </>}

    {/* ---------- Thủ quỹ và kế toán trưởng · Tra cứu lịch sử giao dịch ---------- */}
    {mode === 'lich-su' && <div className="card"><div className="card-header"><h3>Nhật ký giao dịch COD</h3>
      <span className="phu">mới nhất trước</span>
      <span className="can-phai">{lichSuLoc.length}/{lichSu.length} dòng</span></div>
      <div className="card-body thanh-loc">
        <form className="search-filter" onSubmit={(e) => e.preventDefault()}>
          <label className="field">Mã chứng từ / đơn / người thực hiện<input className="input" placeholder="GD0000001 · DH0000002 · NV0000012"
            value={locLs.tuKhoa} onChange={(e) => setLocLs({ ...locLs, tuKhoa: e.target.value })} /></label>
          <label className="field">Loại chứng từ<select className="select" value={locLs.loai} onChange={(e) => setLocLs({ ...locLs, loai: e.target.value })}>
            <option value="">Tất cả</option>{loaiLichSu.map((t) => <option key={t}>{t}</option>)}</select></label>
          <label className="field">Trạng thái<select className="select" value={locLs.trangThai} onChange={(e) => setLocLs({ ...locLs, trangThai: e.target.value })}>
            <option value="">Tất cả</option>{trangThaiLichSu.map((t) => <option key={t}>{t}</option>)}</select></label>
          <label className="field">Từ ngày<input className="input" type="date" value={locLs.tuNgay} onChange={(e) => setLocLs({ ...locLs, tuNgay: e.target.value })} /></label>
          <label className="field">Đến ngày<input className="input" type="date" value={locLs.denNgay} onChange={(e) => setLocLs({ ...locLs, denNgay: e.target.value })} /></label>
          <div className="hanh-dong"><button type="button" className="btn" onClick={() => setLocLs(LOC_LS_RONG)}>Xoá lọc</button></div>
        </form>
      </div>
      <div className="card-body khong-dem">
        {loading ? <p role="status" style={{ padding: 18 }}>Đang tải lịch sử…</p> : <div className="table-wrap"><table className="data-table"><thead><tr>
          <th>Thời gian</th><th>Loại</th><th>Mã chứng từ</th><th>Đơn hàng</th><th className="so">Số tiền</th><th>Trạng thái</th><th>Người thực hiện</th><th>Ghi chú</th>
        </tr></thead><tbody>
          {lichSuLoc.map((l, i) => <tr key={`${l.loai}-${l.ma}-${i}`}>
            <td>{gio(l.thoiGian)}</td><td><span className="badge trung-tinh">{l.loai}</span></td>
            <td className="mono">{l.ma}</td><td className="mono">{l.maDonHang ?? '—'}</td>
            <td className="so">{tien(l.soTien)}</td><td><StatusBadge trangThai={l.trangThai ?? ''} /></td>
            <td className="mono">{l.nguoiThucHien ?? '—'}</td><td>{l.moTa ?? '—'}</td>
          </tr>)}
          {!lichSuLoc.length && <tr><td colSpan={8}>{lichSu.length ? 'Không có dòng nào khớp bộ lọc.' : 'Chưa có thao tác nào được ghi nhận.'}</td></tr>}
        </tbody></table></div>}
      </div>
    </div>}

    {/* ---------- Kế toán viên và kế toán trưởng · Xem báo cáo thu – chi COD ---------- */}
    {mode === 'bao-cao' && <>
      <div className="card"><div className="card-body">
        <form className="form-actions" onSubmit={(e) => { e.preventDefault(); void tai() }}>
          <label className="field" style={{ flex: 'none' }}>Từ ngày<input className="input" type="date" value={ky.tuNgay} onChange={(e) => setKy({ ...ky, tuNgay: e.target.value })} /></label>
          <label className="field" style={{ flex: 'none' }}>Đến ngày<input className="input" type="date" value={ky.denNgay} onChange={(e) => setKy({ ...ky, denNgay: e.target.value })} /></label>
          <button className="btn btn-chinh" disabled={loading}>Xem báo cáo</button>
        </form>
      </div></div>
      {baoCao && <>
        <div className="stat-grid">
          <div className="stat-card"><div className="nhan">Giao dịch COD</div><div className="gia-tri">{baoCao.soGiaoDich}</div><div className="ghi-chu">{baoCao.tuNgay} → {baoCao.denNgay}</div></div>
          <div className="stat-card"><div className="nhan">Đã thu</div><div className="gia-tri">{tien(baoCao.tongThucThu)}</div><div className="ghi-chu">trên tổng phải thu {tien(baoCao.tongPhaiThu)}</div></div>
          <div className="stat-card"><div className="nhan">Đã chi trả</div><div className="gia-tri">{tien(baoCao.tongDaChiTra)}</div><div className="ghi-chu">phiếu chi thủ quỹ đã thực hiện</div></div>
          <div className="stat-card"><div className="nhan">Còn phải trả</div><div className="gia-tri">{tien(baoCao.conPhaiTra)}</div><div className="ghi-chu">đã thu − đã chi trả</div></div>
        </div>
        {baoCao.thongBao && <div className="alert alert-thong-tin">{baoCao.thongBao}</div>}
        <div className="card"><div className="card-header"><h3>Giao dịch sai lệch</h3><span className="phu">{baoCao.soGiaoDichSaiLech} giao dịch có thực thu khác phải thu</span></div>
          <div className="card-body khong-dem"><BangGiaoDich rows={baoCao.saiLech} loading={false} trong="Không có giao dịch sai lệch trong kỳ." /></div></div>
        <div className="card"><div className="card-header"><h3>Giao dịch theo trạng thái</h3><span className="phu">số dư quỹ hiện tại {tien(baoCao.soDuQuy)} đ</span></div><div className="card-body">
          <div className="chip-list">{Object.entries(baoCao.theoTrangThai).map(([k, v]) => <span key={k} className="badge trung-tinh">{k}: {v}</span>)}
            {!Object.keys(baoCao.theoTrangThai).length && <span className="muted">Không có dữ liệu.</span>}</div>
        </div></div>
        <div className="card"><div className="card-header"><h3>Sổ quỹ</h3><span className="phu">mỗi dòng gắn một phiếu thủ quỹ đã thực hiện (3.5.1 bước 5)</span></div><div className="card-body khong-dem">
          <div className="table-wrap"><table className="data-table"><thead><tr><th>Mã sổ quỹ</th><th>Phiếu</th><th className="so">Thu</th><th className="so">Chi</th><th>Ngày ghi sổ</th></tr></thead><tbody>
            {soQuy.map((s) => <tr key={s.maSoQuy}><td className="mono">{s.maSoQuy}</td><td className="mono">{s.maPhieu}</td><td className="so">{tien(s.thu)}</td><td className="so">{tien(s.chi)}</td><td>{s.ngayGhiSo}</td></tr>)}
            {!soQuy.length && <tr><td colSpan={5}>Chưa có bút toán nào. Sổ quỹ ghi khi thủ quỹ xác nhận một phiếu đã duyệt.</td></tr>}
          </tbody></table></div>
        </div></div>
        <div className="card"><div className="card-header"><h3>Công nợ khách hàng</h3><span className="phu">tổng còn phải trả {tien(congNo.reduce((s, x) => s + x.conLai, 0))} đ</span></div><div className="card-body khong-dem">
          <div className="table-wrap"><table className="data-table"><thead><tr><th>Mã công nợ</th><th>Khách hàng</th><th className="so">Phải trả</th><th className="so">Đã trả</th><th className="so">Còn lại</th><th>Trạng thái</th></tr></thead><tbody>
            {congNo.map((c) => <tr key={c.maCongNo}><td className="mono">{c.maCongNo}</td><td>{c.tenKhachHang ?? c.maKhachHang}</td><td className="so">{tien(c.phaiTra)}</td><td className="so">{tien(c.daTra)}</td><td className="so">{tien(c.conLai)}</td><td><StatusBadge trangThai={c.trangThai} /></td></tr>)}
            {!congNo.length && <tr><td colSpan={6}>Chưa phát sinh công nợ.</td></tr>}
          </tbody></table></div>
        </div></div>
      </>}
    </>}
  </>
}

function BangGiaoDich({ rows, loading, thaoTac, trong, chiTiet }: { rows: GiaoDich[]; loading: boolean; thaoTac?: (g: GiaoDich) => React.ReactNode; trong?: string; chiTiet?: boolean }) {
  if (loading) return <p role="status" style={{ padding: 18 }}>Đang tải giao dịch COD…</p>
  const cot = 6 + (chiTiet ? 3 : 0) + (thaoTac ? 1 : 0)
  return <div className="table-wrap"><table className="data-table"><thead><tr>
    <th>Mã giao dịch</th><th>Đơn hàng</th>
    {chiTiet && <th>Khách hàng</th>}
    {chiTiet && <th>Trạng thái giao hàng</th>}
    <th className="so">Phải thu</th><th className="so">Thực thu</th>
    {chiTiet && <th className="so">Chênh lệch</th>}
    <th>Trạng thái thu/chi</th><th>Ngày tạo</th>{thaoTac && <th>Thao tác</th>}
  </tr></thead><tbody>
    {rows.map((g) => <tr key={g.maGiaoDich}>
      <td className="mono">{g.maGiaoDich}</td><td className="mono">{g.maDonHang}</td>
      {chiTiet && <td>{g.tenKhachHang ?? '—'}</td>}
      {chiTiet && <td><StatusBadge trangThai={g.trangThaiDonHang ?? ''} /></td>}
      <td className="so">{tien(g.phaiThu)}</td><td className="so">{tien(g.thucThu)}</td>
      {chiTiet && <td className="so">{g.chenhLech ? tien(g.chenhLech) : '—'}</td>}
      <td><StatusBadge trangThai={g.trangThai} /></td><td>{g.ngayTao}</td>
      {thaoTac && <td><div className="hanh-dong">{thaoTac(g)}</div></td>}
    </tr>)}
    {!rows.length && <tr><td colSpan={cot}>{trong ?? 'Chưa có giao dịch COD. Giao dịch được tạo khi đơn hàng chuyển sang "Đã giao".'}</td></tr>}
  </tbody></table></div>
}

function BangPhieu({ rows, loading, thaoTac }: { rows: Phieu[]; loading: boolean; thaoTac?: (p: Phieu) => React.ReactNode }) {
  if (loading) return <p role="status" style={{ padding: 18 }}>Đang tải phiếu thu/chi…</p>
  return <div className="table-wrap"><table className="data-table"><thead><tr>
    <th>Mã phiếu</th><th>Loại</th><th>Giao dịch</th><th>Đơn hàng</th><th className="so">Số tiền</th><th>Ngày lập</th><th>Nội dung</th><th>Trạng thái</th>{thaoTac && <th>Thao tác</th>}
  </tr></thead><tbody>
    {rows.map((p) => <tr key={p.maPhieu}>
      <td className="mono">{p.maPhieu}</td><td>{p.loaiPhieu}</td><td className="mono">{p.maGiaoDich ?? '—'}</td><td className="mono">{p.maDonHang ?? '—'}</td>
      <td className="so">{tien(p.soTien)}</td><td>{p.ngayLap}</td><td>{p.noiDung ?? '—'}</td>
      <td><StatusBadge trangThai={p.trangThai} /></td>
      {thaoTac && <td><div className="hanh-dong">{thaoTac(p)}</div></td>}
    </tr>)}
    {!rows.length && <tr><td colSpan={thaoTac ? 9 : 8}>Chưa có phiếu thu/chi nào.</td></tr>}
  </tbody></table></div>
}
