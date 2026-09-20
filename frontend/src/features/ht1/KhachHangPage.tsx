import { useEffect, useState, type FormEvent } from 'react'
import { http, thongBaoLoi } from '@/shared/api/http'
import type { KhachHang } from './types'

const rong = { tenKhachHang: '', soDienThoai: '', diachi: '', email: '', loaiKH: '' }
const fields = [ ['tenKhachHang','Tên khách hàng',100], ['soDienThoai','Số điện thoại',10], ['diachi','Địa chỉ',255], ['email','Email',100], ['loaiKH','Loại khách hàng',30] ] as const

export function KhachHangPage() {
  const [ds,setDs] = useState<KhachHang[]>([])
  const [tuKhoa,setTuKhoa] = useState('')
  const [form,setForm] = useState(rong)
  const [id,setId] = useState('')
  const [mo,setMo] = useState(false)
  const [busy,setBusy] = useState(false)
  const [loading,setLoading] = useState(true)
  const [loi,setLoi] = useState('')
  const [ok,setOk] = useState('')
  async function tai() {
    setLoading(true); setLoi('')
    try { setDs((await http.get<KhachHang[]>('/ht1/khach-hang',{params:{tuKhoa}})).data) }
    catch(e) { setLoi(thongBaoLoi(e)) } finally { setLoading(false) }
  }
  useEffect(() => { void tai() }, [])
  async function luu(e: FormEvent) {
    e.preventDefault(); setBusy(true); setLoi(''); setOk('')
    try {
      if(id) await http.put(`/ht1/khach-hang/${id}`,form)
      else await http.post('/ht1/khach-hang',form)
      setMo(false); setOk('Đã lưu thông tin khách hàng'); await tai()
    } catch(e) { setLoi(thongBaoLoi(e)) } finally { setBusy(false) }
  }
  return <>
    <div className="page-header"><h1 className="page-title">Thông tin khách hàng</h1><p className="page-sub">Tra cứu và cập nhật hồ sơ khách hàng dùng chung cho đơn hàng.</p></div>
    {loi && <div role="alert" className="alert alert-loi">{loi}</div>}{ok && <div role="status" className="alert">{ok}</div>}
    <div className="card"><div className="card-body">
      <form className="form-actions" onSubmit={e => { e.preventDefault(); void tai() }}>
        <input className="input" aria-label="Mã, tên hoặc liên hệ khách hàng" placeholder="Mã, tên, số điện thoại hoặc email" value={tuKhoa} onChange={e=>setTuKhoa(e.target.value)}/>
        <button className="btn" disabled={loading}>Tìm kiếm</button>
        <button type="button" className="btn btn-chinh" onClick={()=>{setId('');setForm(rong);setMo(true);setOk('')}}>Thêm khách hàng</button>
      </form>
      {mo && <form onSubmit={luu} className="ht1-editor"><h2>{id ? `Cập nhật ${id}` : 'Khách hàng mới'}</h2>
        <div className="form-grid">{fields.map(([key,label,max])=><label key={key}>{label}{key==='tenKhachHang'?' *':''}<input className="input" required={key==='tenKhachHang'} maxLength={max} type={key==='email'?'email':'text'} value={form[key] ?? ''} onChange={e=>setForm({...form,[key]:e.target.value})}/></label>)}</div>
        <div className="form-actions"><button className="btn btn-chinh" disabled={busy}>Lưu khách hàng</button><button type="button" className="btn" disabled={busy} onClick={()=>setMo(false)}>Đóng</button></div>
      </form>}
      {loading?<p role="status">Đang tải khách hàng…</p>:<div className="table-wrap"><table className="data-table"><thead><tr><th>Mã</th><th>Khách hàng</th><th>Điện thoại</th><th>Email</th><th>Địa chỉ</th><th>Thao tác</th></tr></thead><tbody>
        {ds.map(k=><tr key={k.maKhachHang}><td>{k.maKhachHang}</td><td>{k.tenKhachHang}</td><td>{k.soDienThoai}</td><td>{k.email}</td><td>{k.diachi}</td><td><button className="btn btn-nho" onClick={()=>{setId(k.maKhachHang);setForm(k);setMo(true);setOk('')}}>Chỉnh sửa</button></td></tr>)}
        {!ds.length && <tr><td colSpan={6}>Không có khách hàng phù hợp.</td></tr>}
      </tbody></table></div>}
    </div></div>
  </>
}

