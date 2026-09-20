import { useEffect, useState, type FormEvent, type ReactNode } from 'react'
import { http, thongBaoLoi } from '@/shared/api/http'
import { StatusBadge } from '@/shared/components/StatusBadge'
import { usePermission } from '@/shared/permission/PermissionContext'

interface Product { maSP: string; tenSP: string; donViTinh: string; gia: number; trangThai: string }
interface Stock { maTonKho: string; maSP: string; tenSP: string; maViTri: string; soLuongTon: number; ngayCapNhat: string }
interface Dashboard { tongSanPham: number; tongSoLuong: number; tongGiaTri: number; phieuNhapChoDuyet: number; phieuNhapDaDuyet: number; phieuXuat: number; sanPhamSapHet: number; sapHet: Stock[] }
interface NhapChiTiet { maSP: string; soLuong: number; donGia: number | null; tinhTrangHang: string | null }
interface Receipt { maPhieuNhap: string; maDonHang: string; maNV: string; ngayNhap: string; trangThai: string; tongTien: number; ghiChu: string | null; chiTiet: NhapChiTiet[] }
interface XuatChiTiet { maSP: string; soLuong: number; donGia: number | null }
interface Issue { maPhieuXuat: string; maDonHang: string | null; maNV: string; ngayXuat: string; trangThai: string; tongTien: number; lyDoXuat: string | null; ghiChu: string | null; nguoiDuyet: string | null; chiTiet: XuatChiTiet[] }
interface KiemKe { maPhieuKiemKe: string; ngayKiemKe: string; khuVucKiemKe: string | null; trangThai: string; ghiChu: string | null; chiTiet: { maSP: string; soLuongHeThong: number; soLuongThucTe: number; chenhLech: number; ghiChu: string | null }[] }
interface BienBan { maBienBan: string; maPhieuNhap: string | null; maDonHang: string | null; loaiSuCo: string; moTa: string | null; duongDanAnh: string | null; maNVLap: string; ngayLap: string }
interface Order {
  maDonHang: string; maKhachHang: string | null; tenKhachHang: string | null; nguoiNhan: string | null; sdtNguoiNhan: string | null
  diachiGiaoHang: string | null; khoiLuong: number | null; tienCOD: number | null; trangThai: string; ngayTao: string | null; matHang: string | null
}
interface TraCuu { loai: string; ma: string; maDonHang: string | null; tenKhachHang: string | null; ngay: string | null; trangThai: string; maNV: string | null; tongTien: number | null; matHang: string | null }
interface BaoCao {
  tuNgay: string; denNgay: string; soPhieuNhap: number; tongNhap: number; soPhieuXuat: number; tongXuat: number
  theoTrangThaiNhap: Record<string, number>; theoNgayNhap: Record<string, number>
  theoTrangThaiXuat: Record<string, number>; theoNgayXuat: Record<string, number>
  nhapTheoSanPham: Record<string, number>; xuatTheoSanPham: Record<string, number>; thongBao: string | null
}

const tien = (n?: number | null) => (n == null ? '—' : n.toLocaleString('vi-VN'))

/** Bộ lọc tại chỗ cho danh sách phiếu của mình (mã, trạng thái, khoảng ngày lập phiếu). */
interface LocPhieu { tuKhoa: string; trangThai: string; tuNgay: string; denNgay: string }
const LOC_RONG: LocPhieu = { tuKhoa: '', trangThai: '', tuNgay: '', denNgay: '' }
/** Danh sách phiếu đã tải sẵn nên lọc ngay trên máy khách, gõ tới đâu lọc tới đó. */
function khopLoc(l: LocPhieu, ma: string, maDon: string | null, ngay: string, trangThai: string, matHang: string) {
  const k = l.tuKhoa.trim().toLowerCase()
  if (k && ![ma, maDon ?? '', matHang].some((v) => v.toLowerCase().includes(k))) return false
  if (l.trangThai && trangThai !== l.trangThai) return false
  const d = (ngay ?? '').slice(0, 10)
  if (l.tuNgay && d < l.tuNgay) return false
  if (l.denNgay && d > l.denNgay) return false
  return true
}
/**
 * Gọi API phụ (các bảng danh sách) mà không để một lỗi làm hỏng cả màn hình:
 * biểu mẫu và danh mục vẫn dùng được, phần lỗi chỉ hiện bảng rỗng.
 */
async function phu<T>(p: Promise<{ data: T }>, macDinh: T): Promise<T> {
  try { return (await p).data } catch { return macDinh }
}
const homNay = () => new Date().toISOString().slice(0, 10)
const dauThang = () => homNay().slice(0, 8) + '01'

const TIEU_DE: Record<string, [string, string]> = {
  'bao-cao-phieu': ['Báo cáo phiếu nhập/xuất', 'Kết xuất danh sách phiếu nhập, phiếu xuất và đơn chờ nhập theo mã đơn, tên khách hàng, khoảng ngày hoặc nhân viên thực hiện để đối soát.'],
  'phieu-nhap': ['Lập – cập nhật phiếu nhập kho', 'Đối chiếu đơn đã duyệt, kiểm đếm thực tế rồi lập phiếu; phiếu còn ở Lưu tạm hoặc bị từ chối thì sửa lại được.'],
  'phieu-xuat': ['Lập – cập nhật phiếu xuất kho', 'Soạn hàng và lưu tạm phiếu; chỉ khi xác nhận xuất hệ thống mới trừ tồn kho.'],
  'kiem-ke': ['Kiểm tra hàng nhập – xuất', 'Kiểm đếm thực tế so với số liệu hệ thống và lập biên bản khi hàng thừa, thiếu hoặc hư hỏng.'],
  'phe-duyet': ['Quản lí phiếu nhập – xuất', 'Xem xét phiếu nhân viên gửi lên, phê duyệt để tăng tồn kho hoặc từ chối kèm lý do; theo dõi cả phiếu xuất đã thực hiện.'],
  'dashboard': ['Theo dõi nhập – xuất kho', 'Theo dõi tồn kho, phiếu chờ duyệt và hàng sắp hết theo thời gian thực.'],
  'bao-cao': ['Báo cáo nhập – xuất kho', 'Tổng hợp cả hai chiều nhập và xuất theo khoảng thời gian, trạng thái, ngày và mặt hàng.'],
}

export function Ht2Page({ mode }: { mode: string }) {
  const { coChucNang } = usePermission()
  // Quản lý kho kế thừa nhân viên kho nên vào được màn lập phiếu, nhưng danh sách trả về là
  // toàn bộ phiếu của kho chứ không riêng phiếu mình lập — đổi tiêu đề cho khỏi hiểu nhầm.
  const laQuanLyKho = coChucNang('HT2_PHE_DUYET_PHIEU_NHAP')
  const [products, setProducts] = useState<Product[]>([])
  const [stock, setStock] = useState<Stock[]>([])
  const [orders, setOrders] = useState<Order[]>([])
  const [receipts, setReceipts] = useState<Receipt[]>([])
  const [issues, setIssues] = useState<Issue[]>([])
  const [kiemKe, setKiemKe] = useState<KiemKe[]>([])
  const [bienBan, setBienBan] = useState<BienBan[]>([])
  const [dashboard, setDashboard] = useState<Dashboard | null>(null)
  const [baoCao, setBaoCao] = useState<BaoCao | null>(null)
  const [ketQua, setKetQua] = useState<TraCuu[]>([])
  const [timKiem, setTimKiem] = useState({ maDonHang: '', tuKhoa: '', tuNgay: '', denNgay: '', maNV: '', loai: '' })
  const [ky, setKy] = useState({ tuNgay: dauThang(), denNgay: homNay() })
  const [formNhap, setFormNhap] = useState({ maDonHang: '', ngayNhap: homNay(), maNCC: '', ghiChu: '', maSP: '', soLuong: 1, donGia: 0, tinhTrangHang: 'Đạt' })
  const [suaNhap, setSuaNhap] = useState('')
  const [timDon, setTimDon] = useState('')
  const [locNhap, setLocNhap] = useState<LocPhieu>(LOC_RONG)
  const [formXuat, setFormXuat] = useState({ maDonHang: '', ngayXuat: homNay(), lyDoXuat: 'Bàn giao vận tải', ghiChu: '', maSP: '', soLuong: 1, donGia: 0 })
  const [suaXuat, setSuaXuat] = useState('')
  const [locXuat, setLocXuat] = useState<LocPhieu>(LOC_RONG)
  const [formKk, setFormKk] = useState({ ngayKiemKe: homNay(), khuVucKiemKe: '', ghiChu: '', maSP: '', soLuongThucTe: 0 })
  const [formBb, setFormBb] = useState({ maPhieuNhap: '', maDonHang: '', loaiSuCo: 'Thiếu', moTa: '', duongDanAnh: '' })
  const [moBienBan, setMoBienBan] = useState(false)
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)
  const [loi, setLoi] = useState('')
  const [ok, setOk] = useState('')

  async function tai() {
    setLoading(true); setLoi('')
    try {
      if (mode === 'bao-cao-phieu') {
        const params = Object.fromEntries(Object.entries(timKiem).filter(([, v]) => v !== ''))
        setKetQua((await http.get<TraCuu[]>('/ht2/tra-cuu', { params })).data)
      } else if (mode === 'phieu-nhap') {
        const [p, o, r] = await Promise.all([
          phu(http.get<Product[]>('/ht2/san-pham'), [] as Product[]),
          phu(http.get<Order[]>('/ht2/don-cho-nhap', { params: timDon ? { tuKhoa: timDon } : {} }), [] as Order[]),
          phu(http.get<Receipt[]>('/ht2/phieu-nhap'), [] as Receipt[]),
        ])
        setProducts(p); setOrders(o); setReceipts(r)
        setFormNhap((f) => ({ ...f, maSP: f.maSP || p[0]?.maSP || '', maDonHang: f.maDonHang || o[0]?.maDonHang || '' }))
      } else if (mode === 'phe-duyet') {
        const [r, x] = await Promise.all([
          phu(http.get<Receipt[]>('/ht2/phieu-nhap'), [] as Receipt[]),
          phu(http.get<Issue[]>('/ht2/phieu-xuat'), [] as Issue[]),
        ])
        setReceipts(r); setIssues(x)
      } else if (mode === 'phieu-xuat') {
        const [p, t, x, o] = await Promise.all([
          phu(http.get<Product[]>('/ht2/san-pham'), [] as Product[]),
          phu(http.get<Stock[]>('/ht2/ton-kho'), [] as Stock[]),
          phu(http.get<Issue[]>('/ht2/phieu-xuat'), [] as Issue[]),
          phu(http.get<Order[]>('/ht2/don-cho-xuat', { params: timDon ? { tuKhoa: timDon } : {} }), [] as Order[]),
        ])
        setProducts(p); setStock(t); setIssues(x); setOrders(o)
        setFormXuat((f) => ({ ...f, maSP: f.maSP || t.find((s) => s.soLuongTon > 0)?.maSP || p[0]?.maSP || '' }))
      } else if (mode === 'kiem-ke') {
        const [p, t, k, bb] = await Promise.all([
          phu(http.get<Product[]>('/ht2/san-pham'), [] as Product[]),
          phu(http.get<Stock[]>('/ht2/ton-kho'), [] as Stock[]),
          phu(http.get<KiemKe[]>('/ht2/phieu-kiem-ke'), [] as KiemKe[]),
          phu(http.get<BienBan[]>('/ht2/bien-ban'), [] as BienBan[]),
        ])
        setProducts(p); setStock(t); setKiemKe(k); setBienBan(bb)
        setFormKk((f) => ({ ...f, maSP: f.maSP || p[0]?.maSP || '' }))
      } else if (mode === 'dashboard') {
        const [d, t] = await Promise.all([
          phu(http.get<Dashboard>('/ht2/dashboard'), null as Dashboard | null),
          phu(http.get<Stock[]>('/ht2/ton-kho'), [] as Stock[]),
        ])
        setDashboard(d); setStock(t)
      } else if (mode === 'bao-cao') setBaoCao((await http.get<BaoCao>('/ht2/bao-cao-nhap', { params: ky })).data)
    } catch (e) { setLoi(thongBaoLoi(e)) } finally { setLoading(false) }
  }
  useEffect(() => { setOk(''); setSuaNhap(''); setSuaXuat(''); setTimDon(''); void tai() }, [mode])

  async function chay(viec: () => Promise<void>, thongBao: string) {
    setBusy(true); setLoi(''); setOk('')
    try { await viec(); setOk(thongBao); await tai() }
    catch (e) { setLoi(thongBaoLoi(e)) } finally { setBusy(false) }
  }

  const [tieuDe, moTa] = TIEU_DE[mode] ?? TIEU_DE['dashboard']
  const tonCuaSp = (maSP: string) => stock.filter((s) => s.maSP === maSP).reduce((a, b) => a + b.soLuongTon, 0)
  const nhapLoc = receipts.filter((r) => khopLoc(locNhap, r.maPhieuNhap, r.maDonHang, r.ngayNhap, r.trangThai, r.chiTiet.map((c) => c.maSP).join(' ')))
  const xuatLoc = issues.filter((x) => khopLoc(locXuat, x.maPhieuXuat, x.maDonHang, x.ngayXuat, x.trangThai, x.chiTiet.map((c) => c.maSP).join(' ')))
  const thanNhap = () => ({ maDonHang: formNhap.maDonHang, ngayNhap: formNhap.ngayNhap, maNCC: formNhap.maNCC || null, ghiChu: formNhap.ghiChu || null, chiTiet: [{ maSP: formNhap.maSP, soLuong: formNhap.soLuong, donGia: formNhap.donGia, tinhTrangHang: formNhap.tinhTrangHang }] })
  const thanXuat = () => ({ maDonHang: formXuat.maDonHang || null, ngayXuat: formXuat.ngayXuat, lyDoXuat: formXuat.lyDoXuat, ghiChu: formXuat.ghiChu || null, chiTiet: [{ maSP: formXuat.maSP, soLuong: formXuat.soLuong, donGia: formXuat.donGia }] })

  return <>
    <div className="page-header"><h1 className="page-title">{tieuDe}</h1><p className="page-sub">{moTa}</p></div>
    {loi && <div role="alert" className="alert alert-loi">{loi}</div>}
    {ok && <div role="status" className="alert alert-thanh-cong">{ok}</div>}

    {/* ---------- Báo cáo phiếu nhập/xuất ---------- */}
    {mode === 'bao-cao-phieu' && <>
      <div className="card"><div className="card-body">
        <form className="search-filter" onSubmit={(e) => { e.preventDefault(); void tai() }}>
          <label className="field">Mã đơn / mã phiếu<input className="input" placeholder="DH0000001 · PN0000001" value={timKiem.maDonHang} onChange={(e) => setTimKiem({ ...timKiem, maDonHang: e.target.value })} /></label>
          <label className="field">Tên khách hàng<input className="input" value={timKiem.tuKhoa} onChange={(e) => setTimKiem({ ...timKiem, tuKhoa: e.target.value })} /></label>
          <label className="field">Từ ngày<input className="input" type="date" value={timKiem.tuNgay} onChange={(e) => setTimKiem({ ...timKiem, tuNgay: e.target.value })} /></label>
          <label className="field">Đến ngày<input className="input" type="date" value={timKiem.denNgay} onChange={(e) => setTimKiem({ ...timKiem, denNgay: e.target.value })} /></label>
          <label className="field">Nhân viên thực hiện<input className="input" placeholder="NV0000003" value={timKiem.maNV} onChange={(e) => setTimKiem({ ...timKiem, maNV: e.target.value })} /></label>
          <label className="field">Loại chứng từ<select className="select" value={timKiem.loai} onChange={(e) => setTimKiem({ ...timKiem, loai: e.target.value })}>
            <option value="">Tất cả</option><option>Đơn chờ nhập</option><option>Phiếu nhập</option><option>Phiếu xuất</option></select></label>
          <div className="hanh-dong">
            <button className="btn btn-chinh" disabled={loading}>Kết xuất báo cáo</button>
            <button type="button" className="btn" onClick={() => { setTimKiem({ maDonHang: '', tuKhoa: '', tuNgay: '', denNgay: '', maNV: '', loai: '' }); setTimeout(() => void tai(), 0) }}>Xoá lọc</button>
          </div>
        </form>
      </div></div>
      <div className="stat-grid">
        {['Đơn chờ nhập', 'Phiếu nhập', 'Phiếu xuất'].map((l) => (
          <div className="stat-card" key={l}><div className="nhan">{l}</div>
            <div className="gia-tri">{ketQua.filter((r) => r.loai === l).length}</div>
            <div className="ghi-chu">trong kỳ báo cáo</div></div>
        ))}
      </div>
      <div className="card"><div className="card-header"><h3>Kết quả</h3><span className="can-phai">{ketQua.length} chứng từ</span></div>
        <div className="card-body khong-dem">
          {loading ? <p role="status" style={{ padding: 18 }}>Đang kết xuất…</p> : <div className="table-wrap"><table className="data-table"><thead><tr>
            <th>Loại</th><th>Mã chứng từ</th><th>Đơn hàng</th><th>Khách hàng</th><th>Ngày</th><th>Mặt hàng</th><th>Người thực hiện</th><th>Trạng thái</th>
          </tr></thead><tbody>
            {ketQua.map((r) => <tr key={`${r.loai}-${r.ma}`}>
              <td><span className="badge trung-tinh">{r.loai}</span></td>
              <td className="mono">{r.ma}</td><td className="mono">{r.maDonHang ?? '—'}</td>
              <td>{r.tenKhachHang ?? '—'}</td><td>{r.ngay ?? '—'}</td>
              <td className="mono">{r.matHang || '—'}</td><td className="mono">{r.maNV ?? '—'}</td>
              <td><StatusBadge trangThai={r.trangThai} /></td>
            </tr>)}
            {!ketQua.length && <tr><td colSpan={8}>Không có chứng từ nào khớp tiêu chí báo cáo.</td></tr>}
          </tbody></table></div>}
        </div>
      </div>
    </>}

    {/* ---------- Lập – cập nhật phiếu nhập ---------- */}
    {mode === 'phieu-nhap' && <>
      <TraCuuDon rows={orders} loading={loading} tuKhoa={timDon} doiTuKhoa={setTimDon} timKiem={() => void tai()}
        daChon={formNhap.maDonHang} chon={(o) => setFormNhap({ ...formNhap, maDonHang: o.maDonHang })}
        trong="Không có đơn nào chờ nhập kho. Đơn phải ở trạng thái &quot;Đã tạo&quot; và đã được quản lý kinh doanh duyệt." />
      <div className="card"><div className="card-header"><h3>{suaNhap ? `Cập nhật phiếu ${suaNhap}` : 'Lập phiếu nhập'}</h3>
        <span className="phu">phiếu lưu tạm, gửi duyệt để quản lý kho phê duyệt</span></div><div className="card-body">
        <form onSubmit={(e: FormEvent) => {
          e.preventDefault()
          void chay(async () => {
            if (suaNhap) { await http.put(`/ht2/phieu-nhap/${suaNhap}`, thanNhap()); setSuaNhap('') }
            else await http.post('/ht2/phieu-nhap', thanNhap())
          }, suaNhap ? 'Đã cập nhật phiếu nhập' : 'Đã lập phiếu nhập ở trạng thái Lưu tạm')
        }}>
          <div className="form-grid">
            <label>Đơn hàng *<select className="select" required value={formNhap.maDonHang} onChange={(e) => setFormNhap({ ...formNhap, maDonHang: e.target.value })}>
              <option value="">— Chọn đơn đã duyệt —</option>{orders.map((o) => <option key={o.maDonHang} value={o.maDonHang}>{o.maDonHang}</option>)}
              {suaNhap && !orders.some((o) => o.maDonHang === formNhap.maDonHang) && <option value={formNhap.maDonHang}>{formNhap.maDonHang}</option>}</select></label>
            <label>Ngày nhập *<input className="input" type="date" required value={formNhap.ngayNhap} onChange={(e) => setFormNhap({ ...formNhap, ngayNhap: e.target.value })} /></label>
            <label>Sản phẩm *<select className="select" required value={formNhap.maSP} onChange={(e) => setFormNhap({ ...formNhap, maSP: e.target.value })}>
              {products.map((p) => <option key={p.maSP} value={p.maSP}>{p.maSP} · {p.tenSP}</option>)}</select></label>
            <label>Số lượng thực nhập *<input className="input" type="number" min={1} required value={formNhap.soLuong} onChange={(e) => setFormNhap({ ...formNhap, soLuong: Number(e.target.value) })} /></label>
            <label>Đơn giá<input className="input" type="number" min={0} value={formNhap.donGia} onChange={(e) => setFormNhap({ ...formNhap, donGia: Number(e.target.value) })} /></label>
            <label>Tình trạng hàng<select className="select" value={formNhap.tinhTrangHang} onChange={(e) => setFormNhap({ ...formNhap, tinhTrangHang: e.target.value })}>
              {['Đạt', 'Thừa', 'Thiếu', 'Hư hỏng'].map((t) => <option key={t}>{t}</option>)}</select></label>
            <label>Nhà cung cấp<input className="input" maxLength={20} value={formNhap.maNCC} onChange={(e) => setFormNhap({ ...formNhap, maNCC: e.target.value })} /></label>
            <label className="form-span">Ghi chú<input className="input" maxLength={255} value={formNhap.ghiChu} onChange={(e) => setFormNhap({ ...formNhap, ghiChu: e.target.value })} /></label>
          </div>
          <div className="form-actions">
            {suaNhap && <button type="button" className="btn" onClick={() => setSuaNhap('')}>Huỷ sửa</button>}
            <button className="btn btn-chinh" disabled={busy || !formNhap.maDonHang}>{suaNhap ? 'Lưu thay đổi' : 'Lưu phiếu nhập'}</button>
          </div>
        </form>
      </div></div>

      <div className="card"><div className="card-header"><h3>{laQuanLyKho ? 'Phiếu nhập của kho' : 'Phiếu nhập của tôi'}</h3>
        <span className="can-phai">{nhapLoc.length}/{receipts.length} phiếu</span></div>
        <div className="card-body thanh-loc">
          <BoLocPhieu gia={locNhap} doi={setLocNhap} nhanNgay="Ngày nhập" trangThai={['Lưu tạm', 'Chờ duyệt', 'Đã duyệt', 'Từ chối']}
            goiY="PN0000001 · DH0000001 · SP001" />
        </div>
        <div className="card-body khong-dem">
          <BangPhieuNhap rows={nhapLoc} loading={loading} daLoc={nhapLoc.length !== receipts.length} thaoTac={(r) => {
            const suaDuoc = r.trangThai === 'Lưu tạm' || r.trangThai === 'Từ chối'
            return <>
              {suaDuoc && <button className="btn btn-nho" disabled={busy} onClick={() => {
                const d = r.chiTiet[0]
                setSuaNhap(r.maPhieuNhap)
                setFormNhap({ maDonHang: r.maDonHang, ngayNhap: r.ngayNhap, maNCC: '', ghiChu: r.ghiChu ?? '', maSP: d?.maSP ?? '', soLuong: d?.soLuong ?? 1, donGia: d?.donGia ?? 0, tinhTrangHang: d?.tinhTrangHang ?? 'Đạt' })
                window.scrollTo({ top: 0, behavior: 'smooth' })
              }}>Sửa</button>}
              {r.trangThai === 'Lưu tạm' && <button className="btn btn-nho btn-chinh" disabled={busy} onClick={() => void chay(async () => { await http.post(`/ht2/phieu-nhap/${r.maPhieuNhap}/gui-duyet`) }, `Đã gửi duyệt phiếu ${r.maPhieuNhap}`)}>Gửi duyệt</button>}
              {!suaDuoc && r.trangThai !== 'Lưu tạm' && <span className="muted">{r.trangThai}</span>}
            </>
          }} />
      </div></div>
    </>}

    {/* ---------- Quản lí phiếu (quản lý kho) ---------- */}
    {mode === 'phe-duyet' && <>
      <div className="stat-grid">
        {['Chờ duyệt', 'Đã duyệt', 'Từ chối', 'Lưu tạm'].map((t) => (
          <div className="stat-card" key={t}><div className="nhan">{t}</div>
            <div className="gia-tri">{receipts.filter((r) => r.trangThai === t).length}</div>
            <div className="ghi-chu">phiếu nhập</div></div>
        ))}
      </div>
      <div className="card"><div className="card-header"><h3>Phiếu nhập</h3><span className="phu">duyệt để tăng tồn kho và chuyển đơn sang "Đã nhập kho"</span></div>
        <div className="card-body khong-dem">
          <BangPhieuNhap rows={receipts} loading={loading} thaoTac={(r) => r.trangThai === 'Chờ duyệt' ? <>
            <button className="btn btn-nho btn-chinh" disabled={busy} onClick={() => void chay(async () => { await http.post(`/ht2/phieu-nhap/${r.maPhieuNhap}/duyet`) }, `Đã duyệt ${r.maPhieuNhap}, tồn kho đã tăng và đơn chuyển "Đã nhập kho"`)}>Duyệt</button>
            <button className="btn btn-nho btn-nguy-hiem" disabled={busy} onClick={() => {
              const lyDo = window.prompt('Lý do từ chối phiếu nhập:')
              if (lyDo) void chay(async () => { await http.post(`/ht2/phieu-nhap/${r.maPhieuNhap}/tu-choi`, { lyDo }) }, `Đã từ chối phiếu ${r.maPhieuNhap}`)
            }}>Từ chối</button>
          </> : <span className="muted">{r.trangThai}</span>} />
        </div>
      </div>
      <div className="card"><div className="card-header"><h3>Phiếu xuất</h3>
        <span className="phu">duyệt để trừ tồn kho và cho hàng rời kho</span>
        <span className="can-phai">{issues.filter((x) => x.trangThai === 'Chờ duyệt').length} phiếu chờ duyệt</span></div>
        <div className="card-body khong-dem">
          <div className="table-wrap"><table className="data-table"><thead><tr>
            <th>Mã phiếu</th><th>Đơn hàng</th><th>Ngày xuất</th><th>Mặt hàng</th><th className="so">Tổng tiền</th><th>Người lập</th><th>Trạng thái</th><th>Thao tác</th>
          </tr></thead><tbody>
            {issues.map((x) => <tr key={x.maPhieuXuat}>
              <td className="mono">{x.maPhieuXuat}</td><td className="mono">{x.maDonHang ?? '—'}</td><td>{x.ngayXuat}</td>
              <td className="mono">{x.chiTiet.map((c) => `${c.maSP}×${c.soLuong}`).join(', ')}</td>
              <td className="so">{tien(x.tongTien)}</td><td className="mono">{x.maNV}</td>
              <td><StatusBadge trangThai={x.trangThai} />{x.trangThai === 'Từ chối' && x.ghiChu ? <><br /><span className="muted">{x.ghiChu}</span></> : null}</td>
              <td><div className="hanh-dong ht1-actions">{x.trangThai === 'Chờ duyệt' ? <>
                <button className="btn btn-nho btn-chinh" disabled={busy} onClick={() => void chay(async () => { await http.post(`/ht2/phieu-xuat/${x.maPhieuXuat}/duyet`) }, `Đã duyệt ${x.maPhieuXuat}, tồn kho đã giảm`)}>Duyệt</button>
                <button className="btn btn-nho btn-nguy-hiem" disabled={busy} onClick={() => {
                  const lyDo = window.prompt('Lý do từ chối phiếu xuất:')
                  if (lyDo) void chay(async () => { await http.post(`/ht2/phieu-xuat/${x.maPhieuXuat}/tu-choi`, { lyDo }) }, `Đã từ chối phiếu xuất ${x.maPhieuXuat}`)
                }}>Từ chối</button>
              </> : <span className="muted">{x.nguoiDuyet ? `Duyệt bởi ${x.nguoiDuyet}` : x.trangThai}</span>}</div></td>
            </tr>)}
            {!loading && !issues.length && <tr><td colSpan={8}>Chưa có phiếu xuất nào.</td></tr>}
          </tbody></table></div>
        </div>
      </div>
    </>}

    {/* ---------- Lập – cập nhật phiếu xuất ---------- */}
    {mode === 'phieu-xuat' && <>
      <TraCuuDon rows={orders} loading={loading} tuKhoa={timDon} doiTuKhoa={setTimDon} timKiem={() => void tai()}
        daChon={formXuat.maDonHang} chon={(o) => setFormXuat({ ...formXuat, maDonHang: o.maDonHang })}
        trong="Không có đơn nào đã nhập kho để lập phiếu xuất." />
      <div className="card"><div className="card-header"><h3>{suaXuat ? `Cập nhật phiếu ${suaXuat}` : 'Soạn phiếu xuất'}</h3>
        <span className="phu">lưu tạm trước, gửi quản lý kho duyệt; duyệt xong hệ thống mới trừ tồn</span></div><div className="card-body">
        <form onSubmit={(e: FormEvent) => {
          e.preventDefault()
          void chay(async () => {
            if (suaXuat) { await http.put(`/ht2/phieu-xuat/${suaXuat}`, thanXuat()); setSuaXuat('') }
            else await http.post('/ht2/phieu-xuat', thanXuat())
          }, suaXuat ? 'Đã cập nhật phiếu xuất' : 'Đã lưu tạm phiếu xuất, bấm "Xác nhận xuất" khi giao hàng rời kho')
        }}>
          <div className="form-grid">
            <label>Sản phẩm *<select className="select" required value={formXuat.maSP} onChange={(e) => setFormXuat({ ...formXuat, maSP: e.target.value })}>
              {products.map((p) => <option key={p.maSP} value={p.maSP}>{p.maSP} · {p.tenSP} (tồn {tonCuaSp(p.maSP)})</option>)}</select></label>
            <label>Số lượng xuất *<input className="input" type="number" min={1} required value={formXuat.soLuong} onChange={(e) => setFormXuat({ ...formXuat, soLuong: Number(e.target.value) })} /></label>
            <label>Ngày xuất *<input className="input" type="date" required value={formXuat.ngayXuat} onChange={(e) => setFormXuat({ ...formXuat, ngayXuat: e.target.value })} /></label>
            <label>Đơn giá<input className="input" type="number" min={0} value={formXuat.donGia} onChange={(e) => setFormXuat({ ...formXuat, donGia: Number(e.target.value) })} /></label>
            <label>Đơn hàng<select className="select" value={formXuat.maDonHang} onChange={(e) => setFormXuat({ ...formXuat, maDonHang: e.target.value })}>
              <option value="">Không gắn đơn hàng</option>{orders.map((o) => <option key={o.maDonHang} value={o.maDonHang}>{o.maDonHang} · {o.tenKhachHang ?? o.maKhachHang}</option>)}
              {formXuat.maDonHang && !orders.some((o) => o.maDonHang === formXuat.maDonHang) && <option value={formXuat.maDonHang}>{formXuat.maDonHang}</option>}</select></label>
            <label>Lý do xuất<input className="input" maxLength={255} value={formXuat.lyDoXuat} onChange={(e) => setFormXuat({ ...formXuat, lyDoXuat: e.target.value })} /></label>
          </div>
          <div className="form-actions">
            {suaXuat && <button type="button" className="btn" onClick={() => setSuaXuat('')}>Huỷ sửa</button>}
            <button className="btn btn-chinh" disabled={busy}>{suaXuat ? 'Lưu thay đổi' : 'Lưu tạm phiếu xuất'}</button>
          </div>
        </form>
      </div></div>
      <div className="card"><div className="card-header"><h3>{laQuanLyKho ? 'Phiếu xuất của kho' : 'Phiếu xuất của tôi'}</h3>
        <span className="can-phai">{xuatLoc.length}/{issues.length} phiếu</span></div>
        <div className="card-body thanh-loc">
          <BoLocPhieu gia={locXuat} doi={setLocXuat} nhanNgay="Ngày xuất" trangThai={['Lưu tạm', 'Chờ duyệt', 'Đã xuất', 'Từ chối']}
            goiY="PX0000001 · DH0000001 · SP001" />
        </div>
        <div className="card-body khong-dem">
          {loading ? <p role="status" style={{ padding: 18 }}>Đang tải…</p> : <div className="table-wrap"><table className="data-table"><thead><tr>
            <th>Mã phiếu</th><th>Đơn hàng</th><th>Ngày xuất</th><th>Mặt hàng</th><th className="so">Tổng tiền</th><th>Lý do</th><th>Trạng thái</th><th>Thao tác</th>
          </tr></thead><tbody>
            {xuatLoc.map((x) => <tr key={x.maPhieuXuat}>
              <td className="mono">{x.maPhieuXuat}</td><td className="mono">{x.maDonHang ?? '—'}</td><td>{x.ngayXuat}</td>
              <td className="mono">{x.chiTiet.map((c) => `${c.maSP}×${c.soLuong}`).join(', ')}</td><td className="so">{tien(x.tongTien)}</td>
              <td>{x.lyDoXuat ?? '—'}</td><td><StatusBadge trangThai={x.trangThai} /></td>
              <td><div className="hanh-dong ht1-actions">{x.trangThai === 'Lưu tạm' || x.trangThai === 'Từ chối' ? <>
                <button className="btn btn-nho" disabled={busy} onClick={() => {
                  const d = x.chiTiet[0]
                  setSuaXuat(x.maPhieuXuat)
                  setFormXuat({ maDonHang: x.maDonHang ?? '', ngayXuat: x.ngayXuat, lyDoXuat: x.lyDoXuat ?? '', ghiChu: '', maSP: d?.maSP ?? '', soLuong: d?.soLuong ?? 1, donGia: d?.donGia ?? 0 })
                  window.scrollTo({ top: 0, behavior: 'smooth' })
                }}>Sửa</button>
                {x.trangThai === 'Lưu tạm' && <button className="btn btn-nho btn-chinh" disabled={busy} onClick={() => void chay(async () => { await http.post(`/ht2/phieu-xuat/${x.maPhieuXuat}/gui-duyet`) }, `Đã gửi duyệt phiếu xuất ${x.maPhieuXuat}`)}>Gửi duyệt</button>}
              </> : <span className="muted">{x.trangThai === 'Chờ duyệt' ? 'Chờ quản lý kho duyệt' : 'Đã xuất kho'}</span>}</div></td>
            </tr>)}
            {!xuatLoc.length && <tr><td colSpan={8}>{issues.length ? 'Không có phiếu xuất nào khớp bộ lọc.' : 'Chưa có phiếu xuất nào.'}</td></tr>}
          </tbody></table></div>}
      </div></div>
    </>}

    {/* ---------- Kiểm tra hàng nhập – xuất (kiểm kê + biên bản) ---------- */}
    {mode === 'kiem-ke' && <>
      <div className="card"><div className="card-header"><h3>Kiểm đếm thực tế</h3><span className="phu">hệ thống tự tính chênh lệch so với sổ sách</span></div><div className="card-body">
        <form onSubmit={(e: FormEvent) => {
          e.preventDefault()
          void chay(async () => {
            await http.post('/ht2/phieu-kiem-ke', { ngayKiemKe: formKk.ngayKiemKe, khuVucKiemKe: formKk.khuVucKiemKe || null, ghiChu: formKk.ghiChu || null, chiTiet: [{ maSP: formKk.maSP, soLuongThucTe: formKk.soLuongThucTe, ghiChu: null }] })
          }, 'Đã chốt phiếu kiểm kê')
        }}>
          <div className="form-grid">
            <label>Sản phẩm *<select className="select" required value={formKk.maSP} onChange={(e) => setFormKk({ ...formKk, maSP: e.target.value })}>
              {products.map((p) => <option key={p.maSP} value={p.maSP}>{p.maSP} · {p.tenSP} (hệ thống {tonCuaSp(p.maSP)})</option>)}</select></label>
            <label>Số lượng thực tế *<input className="input" type="number" min={0} required value={formKk.soLuongThucTe} onChange={(e) => setFormKk({ ...formKk, soLuongThucTe: Number(e.target.value) })} /></label>
            <label>Ngày kiểm *<input className="input" type="date" required value={formKk.ngayKiemKe} onChange={(e) => setFormKk({ ...formKk, ngayKiemKe: e.target.value })} /></label>
            <label>Khu vực<input className="input" maxLength={100} value={formKk.khuVucKiemKe} onChange={(e) => setFormKk({ ...formKk, khuVucKiemKe: e.target.value })} /></label>
            <label className="form-span">Ghi chú<input className="input" maxLength={255} value={formKk.ghiChu} onChange={(e) => setFormKk({ ...formKk, ghiChu: e.target.value })} /></label>
          </div>
          <div className="form-actions">
            <button type="button" className="btn btn-nguy-hiem" onClick={() => setMoBienBan(true)}>Hàng không đạt — lập biên bản</button>
            <button className="btn btn-chinh" disabled={busy}>Chốt kiểm kê</button>
          </div>
        </form>

        {moBienBan && <form className="ht1-editor" onSubmit={(e: FormEvent) => {
          e.preventDefault()
          void chay(async () => {
            await http.post('/ht2/bien-ban', { maPhieuNhap: formBb.maPhieuNhap || null, maDonHang: formBb.maDonHang || null, loaiSuCo: formBb.loaiSuCo, moTa: formBb.moTa || null, duongDanAnh: formBb.duongDanAnh || null })
            setMoBienBan(false); setFormBb({ maPhieuNhap: '', maDonHang: '', loaiSuCo: 'Thiếu', moTa: '', duongDanAnh: '' })
          }, 'Đã lập biên bản sự cố lô hàng')
        }}>
          <h2>Biên bản sự cố hàng không đạt</h2>
          <div className="form-grid">
            <label>Loại sự cố *<select className="select" value={formBb.loaiSuCo} onChange={(e) => setFormBb({ ...formBb, loaiSuCo: e.target.value })}>
              {['Thừa', 'Thiếu', 'Hư hỏng'].map((t) => <option key={t}>{t}</option>)}</select></label>
            <label>Đơn hàng<input className="input" maxLength={10} value={formBb.maDonHang} onChange={(e) => setFormBb({ ...formBb, maDonHang: e.target.value })} /></label>
            <label>Phiếu nhập liên quan<input className="input" maxLength={20} value={formBb.maPhieuNhap} onChange={(e) => setFormBb({ ...formBb, maPhieuNhap: e.target.value })} /></label>
            <label>Ảnh minh chứng<input className="input" maxLength={255} placeholder="/uploads/bien-ban-01.jpg" value={formBb.duongDanAnh} onChange={(e) => setFormBb({ ...formBb, duongDanAnh: e.target.value })} /></label>
            <label className="form-span">Mô tả<textarea className="textarea" maxLength={500} value={formBb.moTa} onChange={(e) => setFormBb({ ...formBb, moTa: e.target.value })} /></label>
          </div>
          <div className="form-actions"><button className="btn btn-chinh" disabled={busy}>Lưu biên bản</button><button type="button" className="btn" onClick={() => setMoBienBan(false)}>Đóng</button></div>
        </form>}
      </div></div>

      <div className="card"><div className="card-header"><h3>Phiếu kiểm kê đã chốt</h3></div><div className="card-body khong-dem">
        <div className="table-wrap"><table className="data-table"><thead><tr><th>Mã phiếu</th><th>Ngày</th><th>Khu vực</th><th>Sản phẩm</th><th className="so">Hệ thống</th><th className="so">Thực tế</th><th className="so">Chênh lệch</th></tr></thead><tbody>
          {kiemKe.flatMap((k) => k.chiTiet.length ? k.chiTiet.map((c, idx) => <tr key={`${k.maPhieuKiemKe}-${c.maSP}`}>
            {idx === 0 && <><td className="mono" rowSpan={k.chiTiet.length}>{k.maPhieuKiemKe}</td><td rowSpan={k.chiTiet.length}>{k.ngayKiemKe}</td><td rowSpan={k.chiTiet.length}>{k.khuVucKiemKe ?? '—'}</td></>}
            <td className="mono">{c.maSP}</td><td className="so">{c.soLuongHeThong}</td><td className="so">{c.soLuongThucTe}</td>
            <td className="so">{c.chenhLech === 0 ? '0' : <span className="badge canh-bao">{c.chenhLech > 0 ? `+${c.chenhLech}` : c.chenhLech}</span>}</td>
          </tr>) : [<tr key={k.maPhieuKiemKe}><td className="mono">{k.maPhieuKiemKe}</td><td>{k.ngayKiemKe}</td><td>{k.khuVucKiemKe ?? '—'}</td><td colSpan={4}>Không có dòng chi tiết.</td></tr>])}
          {!loading && !kiemKe.length && <tr><td colSpan={7}>Chưa có phiếu kiểm kê nào.</td></tr>}
        </tbody></table></div>
      </div></div>

      <div className="card"><div className="card-header"><h3>Biên bản sự cố</h3><span className="phu">hàng thừa, thiếu hoặc hư hỏng khi tiếp nhận</span></div><div className="card-body khong-dem">
        <div className="table-wrap"><table className="data-table"><thead><tr><th>Mã biên bản</th><th>Loại</th><th>Đơn hàng</th><th>Phiếu nhập</th><th>Mô tả</th><th>Người lập</th><th>Ngày lập</th></tr></thead><tbody>
          {bienBan.map((b) => <tr key={b.maBienBan}>
            <td className="mono">{b.maBienBan}</td><td><StatusBadge trangThai="Chờ xử lý" nhan={b.loaiSuCo} /></td>
            <td className="mono">{b.maDonHang ?? '—'}</td><td className="mono">{b.maPhieuNhap ?? '—'}</td><td>{b.moTa ?? '—'}</td>
            <td className="mono">{b.maNVLap}</td><td>{new Date(b.ngayLap).toLocaleString('vi-VN')}</td></tr>)}
          {!loading && !bienBan.length && <tr><td colSpan={7}>Chưa có biên bản sự cố nào.</td></tr>}
        </tbody></table></div>
      </div></div>
    </>}

    {/* ---------- Theo dõi nhập – xuất (quản lý kho) ---------- */}
    {mode === 'dashboard' && <>
      {dashboard && <div className="stat-grid">
        <div className="stat-card"><div className="nhan">Sản phẩm</div><div className="gia-tri">{dashboard.tongSanPham}</div><div className="ghi-chu">mã hàng trong danh mục</div></div>
        <div className="stat-card"><div className="nhan">Tổng tồn</div><div className="gia-tri">{dashboard.tongSoLuong}</div><div className="ghi-chu">giá trị {tien(dashboard.tongGiaTri)} đ</div></div>
        <div className="stat-card"><div className="nhan">Phiếu chờ duyệt</div><div className="gia-tri">{dashboard.phieuNhapChoDuyet}</div><div className="ghi-chu">{dashboard.phieuNhapDaDuyet} phiếu đã duyệt</div></div>
        <div className="stat-card"><div className="nhan">Sắp hết hàng</div><div className="gia-tri">{dashboard.sanPhamSapHet}</div><div className="ghi-chu">tồn ≤ 5 đơn vị</div></div>
      </div>}
      <div className="card"><div className="card-header"><h3>Tồn kho hiện tại</h3></div><div className="card-body khong-dem">
        <div className="table-wrap"><table className="data-table"><thead><tr><th>Sản phẩm</th><th>Vị trí</th><th className="so">Tồn</th><th>Cập nhật</th></tr></thead><tbody>
          {stock.map((t) => <tr key={t.maTonKho}><td>{t.maSP} · {t.tenSP}</td><td>{t.maViTri || <span className="muted">Chưa put-away</span>}</td>
            <td className="so">{t.soLuongTon <= 5 ? <span className="badge canh-bao">{t.soLuongTon}</span> : t.soLuongTon}</td><td>{new Date(t.ngayCapNhat).toLocaleString('vi-VN')}</td></tr>)}
          {!loading && !stock.length && <tr><td colSpan={4}>Chưa có tồn kho.</td></tr>}
        </tbody></table></div>
      </div></div>
    </>}

    {/* ---------- Báo cáo nhập – xuất ---------- */}
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
          <div className="stat-card"><div className="nhan">Phiếu nhập</div><div className="gia-tri">{baoCao.soPhieuNhap}</div><div className="ghi-chu">{baoCao.tuNgay} → {baoCao.denNgay}</div></div>
          <div className="stat-card"><div className="nhan">Giá trị nhập</div><div className="gia-tri">{tien(baoCao.tongNhap)}</div><div className="ghi-chu">đồng</div></div>
          <div className="stat-card"><div className="nhan">Phiếu xuất</div><div className="gia-tri">{baoCao.soPhieuXuat}</div><div className="ghi-chu">trong kỳ</div></div>
          <div className="stat-card"><div className="nhan">Giá trị xuất</div><div className="gia-tri">{tien(baoCao.tongXuat)}</div><div className="ghi-chu">đồng</div></div>
        </div>
        {baoCao.thongBao && <div className="alert alert-thong-tin">{baoCao.thongBao}</div>}
        <div className="danh-sach-bao-cao">
          <BangNhom tieuDe="Phiếu nhập theo trạng thái" data={baoCao.theoTrangThaiNhap} cot="Trạng thái" />
          <BangNhom tieuDe="Phiếu xuất theo trạng thái" data={baoCao.theoTrangThaiXuat} cot="Trạng thái" />
          <BangNhom tieuDe="Nhập theo ngày" data={baoCao.theoNgayNhap} cot="Ngày" />
          <BangNhom tieuDe="Xuất theo ngày" data={baoCao.theoNgayXuat} cot="Ngày" />
          <BangNhom tieuDe="Số lượng nhập theo mặt hàng" data={baoCao.nhapTheoSanPham} cot="Sản phẩm" />
          <BangNhom tieuDe="Số lượng xuất theo mặt hàng" data={baoCao.xuatTheoSanPham} cot="Sản phẩm" />
        </div>
      </>}
    </>}
  </>
}

function BangNhom({ tieuDe, data, cot }: { tieuDe: string; data: Record<string, number>; cot: string }) {
  const rows = Object.entries(data ?? {})
  return <div className="card"><div className="card-header"><h3>{tieuDe}</h3></div><div className="card-body khong-dem">
    <div className="table-wrap"><table className="data-table"><thead><tr><th>{cot}</th><th className="so">Số lượng</th></tr></thead><tbody>
      {rows.map(([k, v]) => <tr key={k}><td className={cot === 'Sản phẩm' ? 'mono' : undefined}>{k}</td><td className="so">{v}</td></tr>)}
      {!rows.length && <tr><td colSpan={2}>Không có dữ liệu trong kỳ.</td></tr>}
    </tbody></table></div>
  </div></div>
}

/**
 * Tra cứu đơn hàng theo mã PO — đặc tả 3.2.3 bước 7-8: nhân viên kho nhập mã PO, hệ thống hiển thị
 * thông tin chi tiết đơn hàng. Dùng chung cho cả màn lập phiếu nhập và lập phiếu xuất.
 */
function TraCuuDon({ rows, loading, tuKhoa, doiTuKhoa, timKiem, chon, daChon, trong }: {
  rows: Order[]; loading: boolean; tuKhoa: string; doiTuKhoa: (v: string) => void; timKiem: () => void
  chon: (o: Order) => void; daChon: string; trong: string
}) {
  return <div className="card"><div className="card-header"><h3>Tra cứu đơn hàng / PO</h3>
    <span className="phu">nhập mã PO, tên khách hàng hoặc mặt hàng để xem chi tiết đơn</span>
    <span className="can-phai">{rows.length} đơn</span></div>
    <div className="card-body thanh-loc">
      <form className="search-filter" onSubmit={(e) => { e.preventDefault(); timKiem() }}>
        <label className="field">Mã PO / khách hàng / mặt hàng<input className="input" placeholder="DH0000001 · Minh Phát · Thùng carton"
          value={tuKhoa} onChange={(e) => doiTuKhoa(e.target.value)} /></label>
        <div className="hanh-dong">
          <button className="btn btn-chinh" disabled={loading}>Tra cứu</button>
          <button type="button" className="btn" onClick={() => { doiTuKhoa(''); setTimeout(timKiem, 0) }}>Xoá lọc</button>
        </div>
      </form>
    </div>
    <div className="card-body khong-dem">
      {loading ? <p role="status" style={{ padding: 18 }}>Đang tra cứu đơn hàng…</p> : <div className="table-wrap"><table className="data-table"><thead><tr>
        <th>Mã PO</th><th>Khách hàng</th><th>Người nhận</th><th>Địa chỉ giao</th><th>Mặt hàng</th><th className="so">Khối lượng</th><th>Trạng thái</th><th>Thao tác</th>
      </tr></thead><tbody>
        {rows.map((o) => <tr key={o.maDonHang} className={o.maDonHang === daChon ? 'dong-chon' : undefined}>
          <td className="mono">{o.maDonHang}</td><td>{o.tenKhachHang ?? o.maKhachHang ?? '—'}</td>
          <td>{o.nguoiNhan ?? '—'}{o.sdtNguoiNhan ? <><br /><span className="muted">{o.sdtNguoiNhan}</span></> : null}</td>
          <td>{o.diachiGiaoHang ?? '—'}</td><td>{o.matHang || '—'}</td>
          <td className="so">{o.khoiLuong ?? '—'}</td><td><StatusBadge trangThai={o.trangThai} /></td>
          <td><div className="hanh-dong">{o.maDonHang === daChon
            ? <span className="muted">Đang chọn</span>
            : <button type="button" className="btn btn-nho" onClick={() => chon(o)}>Chọn</button>}</div></td>
        </tr>)}
        {!rows.length && <tr><td colSpan={8}>{trong}</td></tr>}
      </tbody></table></div>}
    </div>
  </div>
}

function BoLocPhieu({ gia, doi, trangThai, nhanNgay, goiY }: { gia: LocPhieu; doi: (v: LocPhieu) => void; trangThai: string[]; nhanNgay: string; goiY: string }) {
  return <form className="search-filter" onSubmit={(e) => e.preventDefault()}>
    <label className="field">Mã phiếu / đơn / mặt hàng<input className="input" placeholder={goiY} value={gia.tuKhoa} onChange={(e) => doi({ ...gia, tuKhoa: e.target.value })} /></label>
    <label className="field">Trạng thái<select className="select" value={gia.trangThai} onChange={(e) => doi({ ...gia, trangThai: e.target.value })}>
      <option value="">Tất cả</option>{trangThai.map((t) => <option key={t}>{t}</option>)}</select></label>
    <label className="field">{nhanNgay} từ<input className="input" type="date" value={gia.tuNgay} onChange={(e) => doi({ ...gia, tuNgay: e.target.value })} /></label>
    <label className="field">Đến ngày<input className="input" type="date" value={gia.denNgay} onChange={(e) => doi({ ...gia, denNgay: e.target.value })} /></label>
    <div className="hanh-dong"><button type="button" className="btn" onClick={() => doi(LOC_RONG)}>Xoá lọc</button></div>
  </form>
}

function BangPhieuNhap({ rows, loading, thaoTac, daLoc }: { rows: Receipt[]; loading: boolean; thaoTac: (r: Receipt) => ReactNode; daLoc?: boolean }) {
  if (loading) return <p role="status" style={{ padding: 18 }}>Đang tải phiếu nhập…</p>
  return <div className="table-wrap"><table className="data-table"><thead><tr>
    <th>Mã phiếu</th><th>Đơn hàng</th><th>Ngày nhập</th><th>Mặt hàng</th><th className="so">Tổng tiền</th><th>Trạng thái</th><th>Thao tác</th>
  </tr></thead><tbody>
    {rows.map((r) => <tr key={r.maPhieuNhap}>
      <td className="mono">{r.maPhieuNhap}</td><td className="mono">{r.maDonHang}</td><td>{r.ngayNhap}</td>
      <td className="mono">{r.chiTiet.map((c) => `${c.maSP}×${c.soLuong}${c.tinhTrangHang && c.tinhTrangHang !== 'Đạt' ? ` (${c.tinhTrangHang})` : ''}`).join(', ') || '—'}</td>
      <td className="so">{tien(r.tongTien)}</td><td><StatusBadge trangThai={r.trangThai} /></td>
      <td><div className="hanh-dong ht1-actions">{thaoTac(r)}</div></td>
    </tr>)}
    {!rows.length && <tr><td colSpan={7}>{daLoc ? 'Không có phiếu nhập nào khớp bộ lọc.' : 'Chưa có phiếu nhập nào.'}</td></tr>}
  </tbody></table></div>
}
