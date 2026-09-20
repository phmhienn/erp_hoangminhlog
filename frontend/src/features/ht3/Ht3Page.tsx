import { useEffect, useState, type FormEvent, type ReactNode } from 'react'
import { http, thongBaoLoi } from '@/shared/api/http'
import { StatusBadge } from '@/shared/components/StatusBadge'
import { ChonAnh } from '@/shared/components/ChonAnh'
import { AnhXemDuoc } from '@/shared/components/XemAnh'
import { usePermission } from '@/shared/permission/PermissionContext'

interface Trip {
  maChuyen: number; maNguoiLap: string; maTaiXe: string | null; hoTenTaiXe: string | null
  maPhuongTien: number | null; bienSo: string | null; trangThai: string
  ngayKhoiHanh: string | null; thoiGianDuKien: string | null; thoiGianThucTe: string | null
  diemXuatPhat: string | null; diemKetThuc: string | null; khoangCach: number | null; moTa: string | null
  maDonHang: string[]
}
interface Driver { maTaiXe: string; maNV: string; hoTen: string | null; soGPLX: string | null; loaiGPLX: string | null; trangThai: string }
interface Vehicle { maPhuongTien: number; bienSo: string; loaiXe: string; taiTrong: number; trangThai: string }
interface Order { maDonHang: string; maKhachHang: string; nguoiNhan: string; diachiGiaoHang: string; khoiLuong: number; tienCOD: number; trangThai: string }
interface DonNhiemVu { maDonHang: string; nguoiNhan: string | null; sdtNguoiNhan: string | null; diachiLayHang: string | null; diachiGiaoHang: string | null; khoiLuong: number | null; tienCOD: number | null; trangThai: string; hangHoa: string }
interface Moc { maMoc: number; maChuyen: number; maDonHang: string | null; diem: string; moTa: string | null; thoiGian: string; maNVCapNhat: string }
interface NhiemVu {
  maChuyen: number; trangThai: string; ngayKhoiHanh: string | null; thoiGianDuKien: string | null; thoiGianThucTe: string | null
  diemXuatPhat: string | null; diemKetThuc: string | null; khoangCach: number | null; moTa: string | null
  bienSo: string | null; donHang: DonNhiemVu[]; moc: Moc[]
}
interface SuCo {
  maSuCo: number; maChuyen: number; maDonHang: string | null; maTaiXe: string | null; loaiSuCo: string
  moTa: string | null; thoiGian: string; trangThai: string; hinhAnh: string | null
  huongXuLy: string | null; nguoiXuLy: string | null; thoiGianXuLy: string | null
}
interface TraCuu { maChuyen: number; maDonHang: string; tenKhachHang: string | null; bienSo: string | null; hoTenTaiXe: string | null; trangThaiChuyen: string; trangThaiDon: string | null; ngayKhoiHanh: string | null; thoiGianThucTe: string | null; diemKetThuc: string | null; soSuCo: number; mocGanNhat: string | null; thoiGianMoc: string | null; soMoc: number }
interface LichSu { maChuyen: number; maDonHang: string | null; trangThaiChuyen: string; trangThaiDon: string | null; ngayKhoiHanh: string | null; thoiGianThucTe: string | null; diemXuatPhat: string | null; diemKetThuc: string | null; khoangCach: number | null }

const maCV = (id: number) => `CV${String(id).padStart(7, '0')}`
const gio = (s?: string | null) => (s ? new Date(s).toLocaleString('vi-VN', { day: '2-digit', month: '2-digit', hour: '2-digit', minute: '2-digit' }) : '—')
const tien = (n?: number | null) => (n == null ? '—' : n.toLocaleString('vi-VN'))
const nowLocal = (offsetMs = 0) => new Date(Date.now() + offsetMs - new Date().getTimezoneOffset() * 60000).toISOString().slice(0, 16)

/** Các mốc hành trình theo thứ tự tài xế thường báo (đặc tả 3.3.2). */
const MOC_HANH_TRINH = ['Đã lấy hàng', 'Đang trung chuyển', 'Đã đến điểm giao', 'Đang chờ khách nhận']
/** Gợi ý mốc tiếp theo dựa trên mốc gần nhất đã báo, để bấm cập nhật là thấy đổi ngay. */
const mocKeTiep = (daBao: { diem: string }[]) => {
  if (!daBao.length) return MOC_HANH_TRINH[0]
  const i = MOC_HANH_TRINH.indexOf(daBao[daBao.length - 1].diem)
  return MOC_HANH_TRINH[Math.min(i + 1, MOC_HANH_TRINH.length - 1)] ?? MOC_HANH_TRINH[0]
}

const TIEU_DE: Record<string, [string, string]> = {
  'ke-hoach': ['Kế hoạch & phân luồng tuyến', 'Chọn đơn đã sẵn sàng ở kho, dựng lộ trình và thời gian dự kiến cho chuyến.'],
  'phan-cong': ['Phân công tài xế & phương tiện', 'Chỉ định tài xế và xe cho chuyến chờ phân công; hệ thống chặn nếu thiếu GPLX, xe bận hoặc tài xế trùng lịch trong 4 giờ.'],
  'phe-duyet': ['Phê duyệt điều phối', 'Xem toàn bộ phương án rồi phê duyệt, hoặc trả lại kèm lý do khi cần điều chỉnh.'],
  'nhiem-vu': ['Nhiệm vụ của tôi', 'Danh sách điểm giao/nhận, hàng hoá và lộ trình của chuyến được giao cho bạn.'],
  'giao-hang': ['Xác nhận giao hàng', 'Đơn bạn đang giao; xác nhận thành công kèm ảnh minh chứng hoặc chữ ký người nhận.'],
  'su-co': ['Báo cáo sự cố', 'Ghi nhận sự cố dọc đường kèm ảnh hiện trường và theo dõi tình trạng xử lý.'],
  'lich-su': ['Tra cứu lịch sử giao hàng', 'Tìm theo mã vận đơn, biển số xe, khoảng ngày hoặc tên khách hàng.'],
}

export function Ht3Page({ mode }: { mode: string }) {
  const { coChucNang } = usePermission()
  const laDieuPhoi = coChucNang('HT3_LAP_KE_HOACH_TUYEN') || coChucNang('HT3_DUYET_DIEU_PHOI')
  const [xuLy, setXuLy] = useState<{ maSuCo: number; trangThai: string; huongXuLy: string; hinhAnh: string } | null>(null)
  const [trips, setTrips] = useState<Trip[]>([])
  const [drivers, setDrivers] = useState<Driver[]>([])
  const [vehicles, setVehicles] = useState<Vehicle[]>([])
  const [orders, setOrders] = useState<Order[]>([])
  const [nhiemVu, setNhiemVu] = useState<NhiemVu[]>([])
  const [suCo, setSuCo] = useState<SuCo[]>([])
  const [ketQua, setKetQua] = useState<TraCuu[]>([])
  const [lichSu, setLichSu] = useState<LichSu[]>([])
  const [timKiem, setTimKiem] = useState({ maDonHang: '', bienSo: '', tuNgay: '', denNgay: '', tuKhoa: '' })
  const [chonDon, setChonDon] = useState<string[]>([])
  const [suaChuyen, setSuaChuyen] = useState<number | null>(null)
  const [daGui, setDaGui] = useState(false)
  const [form, setForm] = useState({ ngayKhoiHanh: nowLocal(86400000), thoiGianDuKien: nowLocal(90000000), diemXuatPhat: 'Kho trung chuyển Hoàng Minh', diemKetThuc: '', khoangCach: 10, thoiGianDuKienPhut: 120, moTa: '' })
  const [phanCong, setPhanCong] = useState<{ maChuyen: number; maTaiXe: string; maPhuongTien: number } | null>(null)
  const [formMoc, setFormMoc] = useState<{ maChuyen: number; maDonHang: string; diem: string; moTa: string } | null>(null)
  const [formGiao, setFormGiao] = useState<{ maDonHang: string; loaiMinhChung: string; duongDan: string } | null>(null)
  const [formSuCo, setFormSuCo] = useState({ maChuyen: '', maDonHang: '', loaiSuCo: 'Hỏng hàng', moTa: '', hinhAnh: '' })
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)
  const [loi, setLoi] = useState('')
  const [ok, setOk] = useState('')

  async function tai() {
    setLoading(true); setLoi('')
    try {
      if (mode === 'ke-hoach') {
        const [o, t] = await Promise.all([
          http.get<Order[]>('/ht3/don-chon-duoc', { params: suaChuyen ? { maChuyen: suaChuyen } : {} }),
          http.get<Trip[]>('/ht3/chuyen'),
        ])
        setOrders(o.data); setTrips(t.data)
      } else if (mode === 'phan-cong') {
        const [t, d, v] = await Promise.all([http.get<Trip[]>('/ht3/chuyen'), http.get<Driver[]>('/ht3/tai-xe'), http.get<Vehicle[]>('/ht3/phuong-tien')])
        setTrips(t.data); setDrivers(d.data); setVehicles(v.data)
      } else if (mode === 'phe-duyet') setTrips((await http.get<Trip[]>('/ht3/chuyen')).data)
      else if (mode === 'nhiem-vu' || mode === 'giao-hang') setNhiemVu((await http.get<NhiemVu[]>('/ht3/nhiem-vu-cua-toi')).data)
      else if (mode === 'su-co') {
        const [t, s] = await Promise.all([http.get<Trip[]>('/ht3/chuyen').catch(() => ({ data: [] as Trip[] })), http.get<SuCo[]>('/ht3/su-co')])
        setTrips(t.data); setSuCo(s.data)
        if (!t.data.length) {
          const nv = (await http.get<NhiemVu[]>('/ht3/nhiem-vu-cua-toi').catch(() => ({ data: [] as NhiemVu[] }))).data
          setNhiemVu(nv)
        }
      } else if (mode === 'lich-su') {
        setKetQua((await http.get<TraCuu[]>('/ht3/tra-cuu', { params: locHopLe() })).data)
        try { setLichSu((await http.get<LichSu[]>('/ht3/lich-su-ca-nhan')).data) } catch { setLichSu([]) }
      }
    } catch (e) { setLoi(thongBaoLoi(e)) } finally { setLoading(false) }
  }
  const locHopLe = () => Object.fromEntries(Object.entries(timKiem).filter(([, v]) => v !== ''))
  useEffect(() => { setOk(''); setChonDon([]); setSuaChuyen(null); setDaGui(false) }, [mode])
  // Đổi chuyến đang sửa thì phải tải lại danh sách đơn chọn được (đơn của chính chuyến đó phải hiện ra).
  useEffect(() => { void tai() }, [mode, suaChuyen])

  async function chay(viec: () => Promise<void>, thongBao: string) {
    setBusy(true); setLoi(''); setOk('')
    try { await viec(); setOk(thongBao); await tai() }
    catch (e) { setLoi(thongBaoLoi(e)) } finally { setBusy(false) }
  }

  const [tieuDe, moTa] = TIEU_DE[mode] ?? TIEU_DE['lich-su']
  const loiKeHoach = !chonDon.length ? 'Chọn ít nhất một đơn hàng cho chuyến.'
    : !form.diemXuatPhat.trim() || !form.diemKetThuc.trim() ? 'Nhập đủ điểm xuất phát và điểm kết thúc.'
    : !form.ngayKhoiHanh || !form.thoiGianDuKien ? 'Nhập thời gian khởi hành và thời gian dự kiến hoàn tất.'
    : new Date(form.thoiGianDuKien) <= new Date(form.ngayKhoiHanh) ? 'Thời gian dự kiến hoàn tất phải sau thời gian khởi hành.'
    : !(form.khoangCach > 0) ? 'Khoảng cách phải lớn hơn 0 km.'
    : !(form.thoiGianDuKienPhut >= 1) ? 'Thời gian chạy phải từ 1 phút trở lên.'
    : ''
  const loiSuCo = !formSuCo.maChuyen ? 'Chọn chuyến đang gặp sự cố.'
    : !formSuCo.moTa.trim() ? 'Nhập mô tả sự cố.'
    : !formSuCo.hinhAnh ? 'Đính kèm ảnh hiện trường.'
    : ''
  const chuyenCuaToiChonSuCo = trips.length ? trips : nhiemVu.map((n) => ({ maChuyen: n.maChuyen, trangThai: n.trangThai, maDonHang: n.donHang.map((d) => d.maDonHang) } as Trip))

  return <>
    <div className="page-header"><h1 className="page-title">{tieuDe}</h1><p className="page-sub">{moTa}</p></div>
    {loi && <div role="alert" className="alert alert-loi">{loi}</div>}
    {ok && <div role="status" className="alert alert-thanh-cong">{ok}</div>}

    {/* ---------- 1. Lập kế hoạch: chọn đơn từ kho rồi dựng lộ trình ---------- */}
    {mode === 'ke-hoach' && <>
      <div className="card"><div className="card-header"><h3>{suaChuyen ? `Đơn cho chuyến ${maCV(suaChuyen)}` : 'Đơn đã sẵn sàng tại kho'}</h3>
        <span className="phu">{suaChuyen ? 'đang sửa chuyến; bỏ chọn đơn cũ hoặc chọn thêm đơn còn trống' : 'đơn đã có chuyến sẽ không còn ở đây — mỗi đơn chỉ lập kế hoạch một lần'}</span>
        <span className="can-phai">{chonDon.length} đơn đã chọn</span></div>
        <div className="card-body khong-dem">
          <div className="table-wrap"><table className="data-table"><thead><tr>
            <th style={{ width: 44 }}>Chọn</th><th>Đơn hàng</th><th>Người nhận</th><th>Địa chỉ giao</th><th className="so">Khối lượng</th><th className="so">COD</th>
          </tr></thead><tbody>
            {orders.map((o) => <tr key={o.maDonHang}>
              <td><input type="checkbox" aria-label={`Chọn ${o.maDonHang}`} checked={chonDon.includes(o.maDonHang)}
                onChange={(e) => setChonDon(e.target.checked ? [...chonDon, o.maDonHang] : chonDon.filter((x) => x !== o.maDonHang))} /></td>
              <td className="mono">{o.maDonHang}</td><td>{o.nguoiNhan}</td><td>{o.diachiGiaoHang}</td>
              <td className="so">{o.khoiLuong}</td><td className="so">{tien(o.tienCOD)}</td>
            </tr>)}
            {!loading && !orders.length && <tr><td colSpan={6}>Không còn đơn nào chờ lập kế hoạch: đơn phải ở trạng thái "Đã nhập kho" và chưa thuộc chuyến nào.</td></tr>}
          </tbody></table></div>
        </div>
      </div>
      <div className="card"><div className="card-header"><h3>{suaChuyen ? `Sửa lộ trình chuyến ${maCV(suaChuyen)}` : 'Lộ trình và thời gian'}</h3></div><div className="card-body">
        <form onSubmit={(e: FormEvent) => {
          e.preventDefault()
          setDaGui(true)
          if (loiKeHoach) return
          const than = {
            maDonHang: chonDon, ngayKhoiHanh: new Date(form.ngayKhoiHanh).toISOString(), thoiGianDuKien: new Date(form.thoiGianDuKien).toISOString(),
            diemXuatPhat: form.diemXuatPhat.trim(), diemKetThuc: form.diemKetThuc.trim(), khoangCach: form.khoangCach, thoiGianDuKienPhut: form.thoiGianDuKienPhut, moTa: form.moTa,
          }
          const dangSua = suaChuyen
          void chay(async () => {
            if (dangSua) await http.put(`/ht3/chuyen/${dangSua}`, than)
            else await http.post('/ht3/chuyen', than)
            setChonDon([]); setSuaChuyen(null); setDaGui(false)
          }, dangSua ? `Đã cập nhật chuyến ${maCV(dangSua)}` : 'Đã lập chuyến, chuyển sang bước phân công tài xế')
        }}>
          <div className="form-grid">
            <label>Điểm xuất phát *<input className="input" required maxLength={255} value={form.diemXuatPhat} onChange={(e) => setForm({ ...form, diemXuatPhat: e.target.value })} /></label>
            <label>Điểm kết thúc *<input className="input" required maxLength={255} placeholder="Địa chỉ giao cuối tuyến" value={form.diemKetThuc} onChange={(e) => setForm({ ...form, diemKetThuc: e.target.value })} /></label>
            <label>Khởi hành *<input className="input" type="datetime-local" required value={form.ngayKhoiHanh} onChange={(e) => setForm({ ...form, ngayKhoiHanh: e.target.value })} /></label>
            <label>Dự kiến hoàn tất *<input className="input" type="datetime-local" required min={form.ngayKhoiHanh} value={form.thoiGianDuKien} onChange={(e) => setForm({ ...form, thoiGianDuKien: e.target.value })} /></label>
            <label>Khoảng cách (km) *<input className="input" type="number" required min={0.1} step={0.1} value={form.khoangCach} onChange={(e) => setForm({ ...form, khoangCach: Number(e.target.value) })} /></label>
            <label>Thời gian chạy (phút) *<input className="input" type="number" required min={1} value={form.thoiGianDuKienPhut} onChange={(e) => setForm({ ...form, thoiGianDuKienPhut: Number(e.target.value) })} /></label>
            <label className="form-span">Ghi chú tuyến<input className="input" maxLength={500} value={form.moTa} onChange={(e) => setForm({ ...form, moTa: e.target.value })} /></label>
          </div>
          {daGui && loiKeHoach && <p className="canh-bao-form" role="alert">{loiKeHoach}</p>}
          <div className="form-actions">
            {suaChuyen && <button type="button" className="btn" onClick={() => { setSuaChuyen(null); setChonDon([]); setDaGui(false) }}>Huỷ sửa</button>}
            <button className="btn btn-chinh" disabled={busy}>{suaChuyen ? 'Lưu thay đổi' : `Lập chuyến cho ${chonDon.length || 0} đơn`}</button>
          </div>
        </form>
      </div></div>
      <div className="card"><div className="card-header"><h3>Chuyến vừa lập, chờ phân công</h3>
        <span className="phu">sửa lại hoặc xoá hẳn khi lập nhầm; đã phân công thì khoá</span></div><div className="card-body khong-dem">
        <BangChuyen rows={trips.filter((t) => t.trangThai === 'Nháp' || t.trangThai === 'Cần điều chỉnh')} loading={loading} trong="Chưa có chuyến nào chờ phân công."
          thaoTac={(t) => <>
            <button className="btn btn-nho" disabled={busy} onClick={() => {
              setSuaChuyen(t.maChuyen); setChonDon(t.maDonHang)
              setForm({
                ngayKhoiHanh: t.ngayKhoiHanh ? t.ngayKhoiHanh.slice(0, 16) : nowLocal(86400000),
                thoiGianDuKien: t.thoiGianDuKien ? t.thoiGianDuKien.slice(0, 16) : nowLocal(90000000),
                diemXuatPhat: t.diemXuatPhat ?? '', diemKetThuc: t.diemKetThuc ?? '',
                khoangCach: t.khoangCach ?? 1, thoiGianDuKienPhut: 120, moTa: t.moTa ?? '',
              })
              window.scrollTo({ top: 0, behavior: 'smooth' })
            }}>Sửa</button>
            <button className="btn btn-nho btn-nguy-hiem" disabled={busy} onClick={() => {
              if (window.confirm(`Xoá chuyến ${maCV(t.maChuyen)}? Đơn hàng sẽ quay lại danh sách chờ lập kế hoạch.`))
                void chay(async () => { await http.delete(`/ht3/chuyen/${t.maChuyen}`); if (suaChuyen === t.maChuyen) { setSuaChuyen(null); setChonDon([]); setDaGui(false) } }, `Đã xoá chuyến ${maCV(t.maChuyen)}`)
            }}>Xoá</button>
          </>} />
      </div></div>
    </>}

    {/* ---------- 2. Phân công: nguồn lực tài xế/xe + gán cho chuyến ---------- */}
    {mode === 'phan-cong' && <>
      <div className="card"><div className="card-header"><h3>Chuyến chờ phân công</h3></div><div className="card-body khong-dem">
        <BangChuyen rows={trips.filter((t) => t.trangThai === 'Nháp' || t.trangThai === 'Cần điều chỉnh')} loading={loading}
          trong="Không còn chuyến nào chờ phân công."
          thaoTac={(t) => <button className="btn btn-nho btn-chinh" onClick={() => setPhanCong({ maChuyen: t.maChuyen, maTaiXe: t.maTaiXe ?? '', maPhuongTien: t.maPhuongTien ?? 0 })}>Phân công</button>} />
      </div></div>

      {phanCong && <div className="card"><div className="card-body"><form className="ht1-editor" onSubmit={(e: FormEvent) => {
        e.preventDefault()
        void chay(async () => { await http.post(`/ht3/phan-cong/${phanCong.maChuyen}`, { maTaiXe: phanCong.maTaiXe, maPhuongTien: phanCong.maPhuongTien }); setPhanCong(null) }, 'Đã phân công, chuyến chờ quản lý phê duyệt')
      }}>
        <h2>Phân công chuyến {maCV(phanCong.maChuyen)}</h2>
        <div className="form-grid">
          <label>Tài xế *<select className="select" required value={phanCong.maTaiXe} onChange={(e) => setPhanCong({ ...phanCong, maTaiXe: e.target.value })}>
            <option value="">— Chọn tài xế —</option>
            {drivers.map((d) => <option key={d.maTaiXe} value={d.maTaiXe} disabled={!d.soGPLX || d.trangThai !== 'Hoạt động'}>
              {d.hoTen ?? d.maTaiXe} · {d.maTaiXe} · {d.soGPLX ? `GPLX ${d.loaiGPLX}` : 'thiếu GPLX'} · {d.trangThai}</option>)}
          </select></label>
          <label>Phương tiện *<select className="select" required value={phanCong.maPhuongTien} onChange={(e) => setPhanCong({ ...phanCong, maPhuongTien: Number(e.target.value) })}>
            <option value={0}>— Chọn xe —</option>
            {vehicles.map((v) => <option key={v.maPhuongTien} value={v.maPhuongTien} disabled={v.trangThai !== 'Sẵn sàng'}>{v.bienSo} · {v.loaiXe} · {v.trangThai}</option>)}
          </select></label>
        </div>
        <div className="form-actions"><button className="btn btn-chinh" disabled={busy}>Lưu phân công</button><button type="button" className="btn" onClick={() => setPhanCong(null)}>Đóng</button></div>
      </form></div></div>}

      <div className="danh-sach-bao-cao">
        <div className="card"><div className="card-header"><h3>Tài xế</h3><span className="phu">chứng chỉ và khối lượng công việc</span></div><div className="card-body khong-dem">
          <div className="table-wrap"><table className="data-table"><thead><tr><th>Tài xế</th><th>Mã</th><th>GPLX</th><th className="so">Chuyến đang gán</th><th>Trạng thái</th></tr></thead><tbody>
            {drivers.map((d) => {
              const dangGan = trips.filter((t) => t.maTaiXe === d.maTaiXe && t.trangThai !== 'Hoàn thành').length
              const thieu = !d.soGPLX || d.trangThai !== 'Hoạt động'
              return <tr key={d.maTaiXe}>
                <td>{d.hoTen ?? <span className="muted">Chưa có hồ sơ nhân viên</span>}</td>
                <td className="mono">{d.maTaiXe}</td>
                <td>{d.soGPLX ? `${d.soGPLX} · hạng ${d.loaiGPLX}` : <span className="badge loi">Thiếu GPLX</span>}</td>
                <td className="so">{dangGan}</td>
                <td>{thieu ? <StatusBadge trangThai="Từ chối" nhan="Không đủ điều kiện" /> : <StatusBadge trangThai={d.trangThai} />}</td>
              </tr>
            })}
            {!loading && !drivers.length && <tr><td colSpan={5}>Chưa có tài xế.</td></tr>}
          </tbody></table></div>
        </div></div>
        <div className="card"><div className="card-header"><h3>Phương tiện</h3></div><div className="card-body khong-dem">
          <div className="table-wrap"><table className="data-table"><thead><tr><th>Biển số</th><th>Loại xe</th><th className="so">Tải trọng</th><th>Trạng thái</th></tr></thead><tbody>
            {vehicles.map((v) => <tr key={v.maPhuongTien}>
              <td className="mono">{v.bienSo}</td><td>{v.loaiXe}</td><td className="so">{v.taiTrong}</td><td><StatusBadge trangThai={v.trangThai} /></td>
            </tr>)}
            {!loading && !vehicles.length && <tr><td colSpan={4}>Chưa có phương tiện.</td></tr>}
          </tbody></table></div>
        </div></div>
      </div>
    </>}

    {/* ---------- 3. Phê duyệt: xem trọn phương án rồi duyệt / trả lại ---------- */}
    {mode === 'phe-duyet' && <>
      {loading ? <p role="status">Đang tải phương án điều phối…</p> : <>
        {trips.filter((t) => t.trangThai === 'Đã phân công').map((t) => (
          <div className="card phuong-an" key={t.maChuyen}>
            <div className="card-header"><h3>{maCV(t.maChuyen)}</h3><span className="phu">chờ phê duyệt</span>
              <span className="can-phai ht1-actions">
                <button className="btn btn-nho btn-chinh" disabled={busy} onClick={() => void chay(async () => { await http.post(`/ht3/chuyen/${t.maChuyen}/phe-duyet`) }, `Đã phê duyệt ${maCV(t.maChuyen)}`)}>Phê duyệt</button>
                <button className="btn btn-nho btn-nguy-hiem" disabled={busy} onClick={() => {
                  const lyDo = window.prompt('Lý do yêu cầu điều chỉnh:')
                  if (lyDo) void chay(async () => { await http.post(`/ht3/chuyen/${t.maChuyen}/yeu-cau-dieu-chinh`, { lyDo }) }, 'Đã trả lại cho điều phối viên')
                }}>Yêu cầu điều chỉnh</button>
              </span>
            </div>
            <div className="card-body"><div className="form-grid">
              <div className="field"><span className="label">Lộ trình</span><span>{t.diemXuatPhat ?? '—'} → {t.diemKetThuc ?? '—'}{t.khoangCach ? ` (${t.khoangCach} km)` : ''}</span></div>
              <div className="field"><span className="label">Thời gian</span><span>{gio(t.ngayKhoiHanh)} → {gio(t.thoiGianDuKien)}</span></div>
              <div className="field"><span className="label">Tài xế</span><span>{t.hoTenTaiXe ?? t.maTaiXe ?? '—'}</span></div>
              <div className="field"><span className="label">Phương tiện</span><span className="mono">{t.bienSo ?? '—'}</span></div>
              <div className="field"><span className="label">Đơn hàng</span><span className="mono">{t.maDonHang.join(', ') || '—'}</span></div>
              <div className="field"><span className="label">Ghi chú</span><span>{t.moTa ?? '—'}</span></div>
            </div></div>
          </div>
        ))}
        {!trips.some((t) => t.trangThai === 'Đã phân công') && <div className="card"><div className="empty-state">
          <div className="tieu-de">Không có phương án nào chờ phê duyệt</div>Các chuyến mới phân công sẽ xuất hiện ở đây.
        </div></div>}
        <div className="card"><div className="card-header"><h3>Chuyến đã trả lại để điều chỉnh</h3></div><div className="card-body khong-dem">
          <BangChuyen rows={trips.filter((t) => t.trangThai === 'Cần điều chỉnh')} loading={false} trong="Không có chuyến nào cần điều chỉnh." />
        </div></div>
      </>}
    </>}

    {/* ---------- 4. Nhiệm vụ của tôi: thẻ nhiệm vụ cho tài xế ---------- */}
    {mode === 'nhiem-vu' && <>
      {formMoc && <div className="card"><div className="card-body"><form className="ht1-editor" onSubmit={(e: FormEvent) => {
        e.preventDefault()
        void chay(async () => {
          await http.post(`/ht3/chuyen/${formMoc.maChuyen}/moc-hanh-trinh`, { diem: formMoc.diem, moTa: formMoc.moTa, maDonHang: formMoc.maDonHang || null })
          setFormMoc(null)
        }, `Đã ghi mốc "${formMoc.diem}"${formMoc.maDonHang ? ` cho đơn ${formMoc.maDonHang}` : ' cho toàn chuyến'}`)
      }}>
        <h2>Cập nhật mốc · {maCV(formMoc.maChuyen)}</h2>
        <div className="form-grid">
          <label>Đơn hàng<select className="select" value={formMoc.maDonHang} onChange={(e) => setFormMoc({ ...formMoc, maDonHang: e.target.value })}>
            <option value="">— Toàn chuyến —</option>
            {(nhiemVu.find((n) => n.maChuyen === formMoc.maChuyen)?.donHang ?? []).map((d) => <option key={d.maDonHang} value={d.maDonHang}>{d.maDonHang} · {d.nguoiNhan ?? 'chưa rõ người nhận'}</option>)}
          </select></label>
          <label>Mốc *<select className="select" value={formMoc.diem} onChange={(e) => setFormMoc({ ...formMoc, diem: e.target.value })}>
            {MOC_HANH_TRINH.map((t) => <option key={t}>{t}</option>)}</select></label>
          <label className="form-span">Ghi chú<input className="input" maxLength={500} value={formMoc.moTa} onChange={(e) => setFormMoc({ ...formMoc, moTa: e.target.value })} /></label>
        </div>
        <div className="form-actions"><button className="btn btn-chinh" disabled={busy}>Lưu mốc</button><button type="button" className="btn" onClick={() => setFormMoc(null)}>Đóng</button></div>
      </form></div></div>}

      {loading ? <p role="status">Đang tải nhiệm vụ…</p> : nhiemVu.length ? nhiemVu.map((n) => (
        <div className="card the-nhiem-vu" key={n.maChuyen}>
          <div className="card-header"><h3>{maCV(n.maChuyen)}</h3>
            <span className="phu">{n.bienSo ? `xe ${n.bienSo}` : 'chưa có xe'} · {gio(n.ngayKhoiHanh)}</span>
            <span className="can-phai ht1-actions">
              <StatusBadge trangThai={n.trangThai} />
              {n.trangThai === 'Đã phê duyệt' && <button className="btn btn-nho btn-chinh" disabled={busy}
                onClick={() => void chay(async () => { await http.post(`/ht3/chuyen/${n.maChuyen}/nhan`) }, 'Đã nhận chuyến, đơn chuyển sang "Đang giao"')}>Nhận chuyến</button>}
              {n.trangThai === 'Đang giao' && <button className="btn btn-nho" onClick={() => setFormMoc({ maChuyen: n.maChuyen, maDonHang: '', diem: mocKeTiep(n.moc.filter((m) => !m.maDonHang)), moTa: '' })}>Mốc toàn chuyến</button>}
            </span>
          </div>
          <div className="card-body">
            <p className="lo-trinh-tom-tat">
              <b>{n.diemXuatPhat ?? '—'}</b> → <b>{n.diemKetThuc ?? '—'}</b>
              {n.khoangCach ? ` · ${n.khoangCach} km` : ''} · dự kiến xong {gio(n.thoiGianDuKien)}
              {n.moTa ? ` · ${n.moTa}` : ''}
            </p>
            <ol className="diem-giao">
              {n.donHang.map((d) => {
                const mocDon = n.moc.filter((m) => m.maDonHang === d.maDonHang)
                const cuoi = mocDon.length ? mocDon[mocDon.length - 1] : null
                return <li key={d.maDonHang}>
                  <div className="dong-dau"><span className="mono">{d.maDonHang}</span><StatusBadge trangThai={d.trangThai} />
                    {n.trangThai === 'Đang giao' && d.trangThai === 'Đang giao' && <button className="btn btn-nho can-phai" disabled={busy}
                      onClick={() => setFormMoc({ maChuyen: n.maChuyen, maDonHang: d.maDonHang, diem: mocKeTiep(mocDon), moTa: '' })}>Cập nhật mốc</button>}
                  </div>
                  <div className="nguoi-nhan">{d.nguoiNhan ?? '—'}{d.sdtNguoiNhan ? ` · ${d.sdtNguoiNhan}` : ''}</div>
                  <div className="dia-chi">{d.diachiGiaoHang ?? '—'}</div>
                  <div className="ghi-chu-hang">{d.hangHoa || 'Không có ghi chú hàng hoá'}{d.khoiLuong ? ` · ${d.khoiLuong} kg` : ''}
                    {d.tienCOD ? <> · thu hộ <b>{tien(d.tienCOD)} đ</b></> : ' · không thu hộ'}</div>
                  <div className="moc-cua-don">
                    {cuoi
                      ? <><span className="nhan-moc">{cuoi.diem}</span> <span className="muted">{gio(cuoi.thoiGian)}</span>{cuoi.moTa ? ` — ${cuoi.moTa}` : ''}
                        {mocDon.length > 1 && <ul className="danh-sach-moc moc-truoc">{mocDon.slice(0, -1).reverse().map((m) => <li key={m.maMoc}>{m.diem} · <span className="muted">{gio(m.thoiGian)}</span>{m.moTa ? ` — ${m.moTa}` : ''}</li>)}</ul>}</>
                      : <span className="muted">Chưa có mốc hành trình nào cho đơn này.</span>}
                  </div>
                </li>
              })}
              {!n.donHang.length && <li className="muted">Chuyến chưa gắn đơn hàng nào.</li>}
            </ol>
            {n.moc.some((m) => !m.maDonHang) && <>
              <h4 style={{ margin: '16px 0 8px', fontSize: 13 }}>Mốc chung của cả chuyến</h4>
              <ul className="danh-sach-moc">{n.moc.filter((m) => !m.maDonHang).map((m) => <li key={m.maMoc}><b>{m.diem}</b> · <span className="muted">{gio(m.thoiGian)}</span>{m.moTa ? ` — ${m.moTa}` : ''}</li>)}</ul>
            </>}
          </div>
        </div>
      )) : <div className="card"><div className="empty-state">
        <div className="tieu-de">Bạn chưa được phân công chuyến nào</div>Điều phối viên sẽ gán chuyến và quản lý phê duyệt trước khi bạn nhận.
      </div></div>}
    </>}

    {/* ---------- 5. Xác nhận giao hàng: theo từng đơn đang giao ---------- */}
    {mode === 'giao-hang' && <>
      {formGiao && <div className="card"><div className="card-body"><form className="ht1-editor" onSubmit={(e: FormEvent) => {
        e.preventDefault()
        void chay(async () => { await http.post(`/ht3/don-hang/${formGiao.maDonHang}/hoan-tat`, { loaiMinhChung: formGiao.loaiMinhChung, duongDan: formGiao.duongDan }); setFormGiao(null) }, 'Đã xác nhận giao hàng thành công')
      }}>
        <h2>Xác nhận giao · đơn {formGiao.maDonHang}</h2>
        <div className="form-grid">
          <label>Loại minh chứng *<select className="select" value={formGiao.loaiMinhChung} onChange={(e) => setFormGiao({ ...formGiao, loaiMinhChung: e.target.value })}>
            <option value="Anh">Ảnh giao hàng</option><option value="ChuKy">Chữ ký người nhận</option></select></label>
          <ChonAnh nhan={formGiao.loaiMinhChung === 'ChuKy' ? 'Ảnh chữ ký người nhận' : 'Ảnh giao hàng'} batBuoc
            giaTri={formGiao.duongDan} onThayDoi={(d) => setFormGiao({ ...formGiao, duongDan: d })} />
        </div>
        <p className="muted" style={{ fontSize: 12.5 }}>Thiếu minh chứng thì không hoàn tất được đơn (đặc tả 3.3.3). Sau khi xác nhận, hệ thống tự sinh giao dịch COD cho kế toán đối soát.</p>
        <div className="form-actions"><button className="btn btn-chinh" disabled={busy || !formGiao.duongDan}>Xác nhận giao thành công</button><button type="button" className="btn" onClick={() => setFormGiao(null)}>Đóng</button></div>
      </form></div></div>}

      <div className="card"><div className="card-header"><h3>Đơn đang giao của tôi</h3></div><div className="card-body khong-dem">
        {loading ? <p role="status" style={{ padding: 18 }}>Đang tải…</p> : <div className="table-wrap"><table className="data-table"><thead><tr>
          <th>Đơn hàng</th><th>Chuyến</th><th>Người nhận</th><th>Địa chỉ giao</th><th className="so">Thu hộ</th><th>Trạng thái</th><th>Thao tác</th>
        </tr></thead><tbody>
          {nhiemVu.flatMap((n) => n.donHang.map((d) => ({ n, d }))).map(({ n, d }) => <tr key={d.maDonHang}>
            <td className="mono">{d.maDonHang}</td><td className="mono">{maCV(n.maChuyen)}</td>
            <td>{d.nguoiNhan ?? '—'}<br /><span className="muted">{d.sdtNguoiNhan ?? ''}</span></td>
            <td>{d.diachiGiaoHang ?? '—'}</td><td className="so">{tien(d.tienCOD)}</td>
            <td><StatusBadge trangThai={d.trangThai} /></td>
            <td><div className="hanh-dong">{d.trangThai === 'Đang giao'
              ? <button className="btn btn-nho btn-chinh" onClick={() => setFormGiao({ maDonHang: d.maDonHang, loaiMinhChung: 'Anh', duongDan: '' })}>Giao thành công</button>
              : <span className="muted">{d.trangThai === 'Đã giao' ? 'Đã hoàn tất' : 'Chưa nhận chuyến'}</span>}</div></td>
          </tr>)}
          {!nhiemVu.some((n) => n.donHang.length) && <tr><td colSpan={7}>Bạn chưa có đơn nào đang giao.</td></tr>}
        </tbody></table></div>}
      </div></div>
    </>}

    {/* ---------- 6. Báo cáo sự cố: form + danh sách sự cố ---------- */}
    {mode === 'su-co' && <>
      <div className="card"><div className="card-header"><h3>Ghi nhận sự cố</h3><span className="phu">gửi ngay cho điều phối để điều xe thay thế hoặc đổi lộ trình</span></div><div className="card-body">
        <form onSubmit={(e: FormEvent) => {
          e.preventDefault()
          setDaGui(true)
          if (loiSuCo) return
          void chay(async () => {
            await http.post('/ht3/su-co', { maChuyen: Number(formSuCo.maChuyen), maDonHang: formSuCo.maDonHang || null, loaiSuCo: formSuCo.loaiSuCo, moTa: formSuCo.moTa.trim(), hinhAnh: formSuCo.hinhAnh })
            setFormSuCo({ ...formSuCo, moTa: '', hinhAnh: '' }); setDaGui(false)
          }, 'Đã gửi báo cáo sự cố')
        }}>
          <div className="form-grid">
            <label>Chuyến *<select className="select" required value={formSuCo.maChuyen} onChange={(e) => {
              const t = chuyenCuaToiChonSuCo.find((x) => String(x.maChuyen) === e.target.value)
              setFormSuCo({ ...formSuCo, maChuyen: e.target.value, maDonHang: t?.maDonHang?.[0] ?? '' })
            }}>
              <option value="">— Chọn chuyến —</option>{chuyenCuaToiChonSuCo.map((t) => <option key={t.maChuyen} value={t.maChuyen}>{maCV(t.maChuyen)} · {t.trangThai}</option>)}</select></label>
            <label>Loại sự cố *<select className="select" value={formSuCo.loaiSuCo} onChange={(e) => setFormSuCo({ ...formSuCo, loaiSuCo: e.target.value })}>
              {['Hỏng hàng', 'Thiếu hàng', 'Thừa hàng', 'Tai nạn', 'Ách tắc giao thông', 'Khách vắng mặt', 'Khách từ chối nhận', 'Mất hàng'].map((t) => <option key={t}>{t}</option>)}</select></label>
            <label>Đơn hàng liên quan<input className="input" maxLength={10} value={formSuCo.maDonHang} onChange={(e) => setFormSuCo({ ...formSuCo, maDonHang: e.target.value })} /></label>
            <ChonAnh nhan="Ảnh hiện trường" batBuoc giaTri={formSuCo.hinhAnh} onThayDoi={(d) => setFormSuCo({ ...formSuCo, hinhAnh: d })} />
            <label className="form-span">Mô tả *<textarea className="textarea" required maxLength={2000} placeholder="Mô tả tình huống để điều phối biết cách xử lý" value={formSuCo.moTa} onChange={(e) => setFormSuCo({ ...formSuCo, moTa: e.target.value })} /></label>
          </div>
          {daGui && loiSuCo && <p className="canh-bao-form" role="alert">{loiSuCo}</p>}
          <div className="form-actions"><button className="btn btn-chinh" disabled={busy}>Gửi báo cáo</button></div>
        </form>
      </div></div>
      {xuLy && <div className="card"><div className="card-body"><form className="ht1-editor" onSubmit={(e: FormEvent) => {
        e.preventDefault()
        void chay(async () => { await http.put(`/ht3/su-co/${xuLy.maSuCo}`, { trangThai: xuLy.trangThai, huongXuLy: xuLy.huongXuLy, hinhAnh: xuLy.hinhAnh || null }); setXuLy(null) }, 'Đã cập nhật xử lý sự cố, tài xế sẽ thấy phương án này')
      }}>
        <h2>Cập nhật xử lý sự cố #{xuLy.maSuCo}</h2>
        <div className="form-grid">
          <label>Trạng thái *<select className="select" value={xuLy.trangThai} onChange={(e) => setXuLy({ ...xuLy, trangThai: e.target.value })}>
            {['Chờ xử lý', 'Đang xử lý', 'Đã xử lý'].map((t) => <option key={t}>{t}</option>)}</select></label>
          <ChonAnh nhan="Ảnh kèm theo (hiện trường hoặc ảnh xử lý)" giaTri={xuLy.hinhAnh} onThayDoi={(d) => setXuLy({ ...xuLy, hinhAnh: d })} />
          <label className="form-span">Phương án xử lý gửi tài xế<textarea className="textarea" maxLength={500} placeholder="Ví dụ: đã điều xe 51C-678.90 thay thế, tài xế chờ tại điểm hiện tại" value={xuLy.huongXuLy} onChange={(e) => setXuLy({ ...xuLy, huongXuLy: e.target.value })} /></label>
        </div>
        <div className="form-actions"><button className="btn btn-chinh" disabled={busy}>Lưu xử lý</button><button type="button" className="btn" onClick={() => setXuLy(null)}>Đóng</button></div>
      </form></div></div>}

      <div className="card"><div className="card-header"><h3>Sự cố đã ghi nhận</h3><span className="phu">{suCo.length} sự cố</span></div><div className="card-body khong-dem">
        {loading ? <p role="status" style={{ padding: 18 }}>Đang tải…</p> : <div className="table-wrap"><table className="data-table"><thead><tr>
          <th>Chuyến</th><th>Loại</th><th>Đơn</th><th>Thời gian</th><th>Mô tả</th><th>Ảnh</th><th>Trạng thái</th><th>Phương án xử lý</th><th>Thao tác</th>
        </tr></thead><tbody>
          {suCo.map((s) => <tr key={s.maSuCo}>
            <td className="mono">{maCV(s.maChuyen)}</td><td>{s.loaiSuCo}</td><td className="mono">{s.maDonHang ?? '—'}</td>
            <td>{gio(s.thoiGian)}</td><td>{s.moTa ?? '—'}</td>
            <td>{s.hinhAnh ? <AnhXemDuoc src={s.hinhAnh} alt={`Ảnh sự cố ${s.loaiSuCo}`} /> : '—'}</td>
            <td><StatusBadge trangThai={s.trangThai === 'Mới' ? 'Chờ xử lý' : s.trangThai} nhan={s.trangThai} /></td>
            <td>{s.huongXuLy ? <>{s.huongXuLy}<br /><span className="muted">{s.nguoiXuLy} · {gio(s.thoiGianXuLy)}</span></> : <span className="muted">Chưa có</span>}</td>
            <td><div className="hanh-dong ht1-actions">
              {laDieuPhoi && <button className="btn btn-nho btn-chinh" disabled={busy}
                onClick={() => setXuLy({ maSuCo: s.maSuCo, trangThai: s.trangThai === 'Mới' ? 'Đang xử lý' : s.trangThai, huongXuLy: s.huongXuLy ?? '', hinhAnh: s.hinhAnh ?? '' })}>Cập nhật xử lý</button>}
              {!s.nguoiXuLy && s.trangThai !== 'Đã xử lý' && <button className="btn btn-nho btn-nguy-hiem" disabled={busy}
                onClick={() => { if (window.confirm(`Thu hồi báo cáo sự cố #${s.maSuCo}? Thao tác này xoá hẳn báo cáo.`)) void chay(async () => { await http.delete(`/ht3/su-co/${s.maSuCo}`) }, 'Đã thu hồi báo cáo sự cố') }}>Thu hồi</button>}
              {s.nguoiXuLy && !laDieuPhoi && <span className="muted">Điều phối đã xử lý</span>}
            </div></td>
          </tr>)}
          {!suCo.length && <tr><td colSpan={9}>Chưa có sự cố nào được ghi nhận.</td></tr>}
        </tbody></table></div>}
      </div></div>
    </>}

    {/* ---------- 7. Tra cứu lịch sử theo tiêu chí của đặc tả ---------- */}
    {mode === 'lich-su' && <>
      <div className="card"><div className="card-body">
        <form className="search-filter" onSubmit={(e) => { e.preventDefault(); void tai() }}>
          <label className="field">Mã vận đơn<input className="input" placeholder="DH0000001" value={timKiem.maDonHang} onChange={(e) => setTimKiem({ ...timKiem, maDonHang: e.target.value })} /></label>
          <label className="field">Biển số xe<input className="input" placeholder="51C-123.45" value={timKiem.bienSo} onChange={(e) => setTimKiem({ ...timKiem, bienSo: e.target.value })} /></label>
          <label className="field">Từ ngày<input className="input" type="date" value={timKiem.tuNgay} onChange={(e) => setTimKiem({ ...timKiem, tuNgay: e.target.value })} /></label>
          <label className="field">Đến ngày<input className="input" type="date" value={timKiem.denNgay} onChange={(e) => setTimKiem({ ...timKiem, denNgay: e.target.value })} /></label>
          <label className="field">Khách hàng / tài xế<input className="input" placeholder="Tên khách hàng" value={timKiem.tuKhoa} onChange={(e) => setTimKiem({ ...timKiem, tuKhoa: e.target.value })} /></label>
          <div className="hanh-dong">
            <button className="btn btn-chinh" disabled={loading}>Tra cứu</button>
            <button type="button" className="btn" onClick={() => { setTimKiem({ maDonHang: '', bienSo: '', tuNgay: '', denNgay: '', tuKhoa: '' }); setTimeout(() => void tai(), 0) }}>Xoá lọc</button>
          </div>
        </form>
      </div></div>
      <div className="card"><div className="card-header"><h3>Kết quả</h3><span className="phu">{ketQua.length} dòng</span></div><div className="card-body khong-dem">
        {loading ? <p role="status" style={{ padding: 18 }}>Đang tra cứu…</p> : <div className="table-wrap"><table className="data-table"><thead><tr>
          <th>Vận đơn</th><th>Khách hàng</th><th>Chuyến</th><th>Xe</th><th>Tài xế</th><th>Điểm đến</th><th>Khởi hành</th><th>Mốc gần nhất</th><th>Đơn</th><th className="so">Sự cố</th>
        </tr></thead><tbody>
          {ketQua.map((r) => <tr key={`${r.maChuyen}-${r.maDonHang}`}>
            <td className="mono">{r.maDonHang}</td><td>{r.tenKhachHang ?? '—'}</td><td className="mono">{maCV(r.maChuyen)}</td>
            <td className="mono">{r.bienSo ?? '—'}</td><td>{r.hoTenTaiXe ?? '—'}</td><td>{r.diemKetThuc ?? '—'}</td>
            <td>{gio(r.ngayKhoiHanh)}</td>
            <td>{r.mocGanNhat
              ? <><b>{r.mocGanNhat}</b><br /><span className="muted">{gio(r.thoiGianMoc)}{r.soMoc > 1 ? ` · ${r.soMoc} mốc` : ''}</span></>
              : <span className="muted">Chưa báo mốc</span>}</td>
            <td><StatusBadge trangThai={r.trangThaiDon ?? ''} /></td>
            <td className="so">{r.soSuCo || '—'}</td>
          </tr>)}
          {!ketQua.length && <tr><td colSpan={10}>Không tìm thấy chuyến giao hàng phù hợp với tiêu chí đã chọn.</td></tr>}
        </tbody></table></div>}
      </div></div>
      {lichSu.length > 0 && <div className="card"><div className="card-header"><h3>Lịch sử cá nhân của tôi</h3><span className="phu">quãng đường và thời gian làm việc theo từng chuyến</span></div>
        <div className="card-body khong-dem"><div className="table-wrap"><table className="data-table"><thead><tr>
          <th>Chuyến</th><th>Đơn</th><th>Lộ trình</th><th className="so">Km</th><th>Khởi hành</th><th>Thực tế</th><th>Trạng thái đơn</th><th>Trạng thái chuyến</th>
        </tr></thead><tbody>
          {lichSu.map((l) => <tr key={`${l.maChuyen}-${l.maDonHang}`}>
            <td className="mono">{maCV(l.maChuyen)}</td><td className="mono">{l.maDonHang ?? '—'}</td>
            <td>{l.diemXuatPhat ?? '—'} → {l.diemKetThuc ?? '—'}</td><td className="so">{l.khoangCach ?? '—'}</td>
            <td>{gio(l.ngayKhoiHanh)}</td><td>{gio(l.thoiGianThucTe)}</td>
            <td>{l.trangThaiDon ? <StatusBadge trangThai={l.trangThaiDon} /> : '—'}</td>
            <td><StatusBadge trangThai={l.trangThaiChuyen} /></td>
          </tr>)}
        </tbody></table></div></div>
      </div>}
    </>}
  </>
}

function BangChuyen({ rows, loading, thaoTac, trong }: { rows: Trip[]; loading: boolean; thaoTac?: (t: Trip) => ReactNode; trong?: string }) {
  if (loading) return <p role="status" style={{ padding: 18 }}>Đang tải chuyến…</p>
  return <div className="table-wrap"><table className="data-table"><thead><tr>
    <th>Chuyến</th><th>Đơn hàng</th><th>Lộ trình</th><th>Khởi hành</th><th>Tài xế</th><th>Xe</th><th>Trạng thái</th>{thaoTac && <th>Thao tác</th>}
  </tr></thead><tbody>
    {rows.map((t) => <tr key={t.maChuyen}>
      <td className="mono">{maCV(t.maChuyen)}</td>
      <td className="mono">{t.maDonHang.join(', ') || '—'}</td>
      <td>{t.diemXuatPhat ?? '—'} → {t.diemKetThuc ?? '—'}</td>
      <td>{gio(t.ngayKhoiHanh)}</td>
      <td>{t.hoTenTaiXe ?? <span className="muted">Chưa phân công</span>}</td>
      <td className="mono">{t.bienSo ?? '—'}</td>
      <td><StatusBadge trangThai={t.trangThai} /></td>
      {thaoTac && <td><div className="hanh-dong ht1-actions">{thaoTac(t)}</div></td>}
    </tr>)}
    {!rows.length && <tr><td colSpan={thaoTac ? 8 : 7}>{trong ?? 'Chưa có chuyến nào.'}</td></tr>}
  </tbody></table></div>
}
