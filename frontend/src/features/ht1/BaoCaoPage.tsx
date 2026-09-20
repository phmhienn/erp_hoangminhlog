import { useEffect,useState } from 'react'
import { http,thongBaoLoi } from '@/shared/api/http'
import type { BaoCao,KhachHang,DichVu } from './types'

export function BaoCaoPage() {
  const [data,setData]=useState<BaoCao|null>(null)
  const [filter,setFilter]=useState({tuNgay:'',denNgay:'',maKhachHang:'',maDichVu:''})
  const [kh,setKh]=useState<KhachHang[]>([])
  const [dv,setDv]=useState<DichVu[]>([])
  const [loi,setLoi]=useState('')
  const [busy,setBusy]=useState(true)
  async function tai() {
    setBusy(true);setLoi('');setData(null)
    try{setData((await http.get<BaoCao>('/ht1/bao-cao',{params:Object.fromEntries(Object.entries(filter).filter(([,v])=>v))})).data)}catch(e){setLoi(thongBaoLoi(e))}finally{setBusy(false)}
  }
  useEffect(()=>{void tai();Promise.all([http.get<KhachHang[]>('/ht1/khach-hang'),http.get<DichVu[]>('/ht1/dich-vu')]).then(([k,d])=>{setKh(k.data);setDv(d.data)}).catch(e=>setLoi(thongBaoLoi(e)))},[])
  return <><div className="page-header"><h1 className="page-title">Báo cáo đơn hàng</h1><p className="page-sub">Tổng hợp theo ngày tạo, khách hàng, dịch vụ và tình hình xử lý.</p></div>
    {loi&&<div className="alert alert-loi" role="alert">{loi}</div>}
    <div className="card"><div className="card-body"><form className="search-filter" onSubmit={e=>{e.preventDefault();void tai()}}>
      <label>Từ ngày<input className="input" type="date" value={filter.tuNgay} onChange={e=>setFilter({...filter,tuNgay:e.target.value})}/></label><label>Đến ngày<input className="input" type="date" value={filter.denNgay} onChange={e=>setFilter({...filter,denNgay:e.target.value})}/></label>
      <label>Khách hàng<select className="select" value={filter.maKhachHang} onChange={e=>setFilter({...filter,maKhachHang:e.target.value})}><option value="">Tất cả khách hàng</option>{kh.map(k=><option key={k.maKhachHang} value={k.maKhachHang}>{k.tenKhachHang}</option>)}</select></label>
      <label>Dịch vụ<select className="select" value={filter.maDichVu} onChange={e=>setFilter({...filter,maDichVu:e.target.value})}><option value="">Tất cả dịch vụ</option>{dv.map(d=><option key={d.maDichVu} value={d.maDichVu}>{d.tenDichVu}</option>)}</select></label><button className="btn btn-chinh" disabled={busy}>Xem báo cáo</button>
    </form>{busy&&<p role="status">Đang tổng hợp báo cáo…</p>}{data&&<><h2 className="ht1-total">{data.tongDon.toLocaleString('vi-VN')} đơn hàng</h2>{data.thongBao&&<p role="status">{data.thongBao}</p>}<div className="form-grid">{([['theoTrangThai','Trạng thái vận chuyển'],['theoKiemDuyet','Tình hình kiểm duyệt'],['theoNgay','Theo ngày'],['theoTuan','Theo tuần (ngày thứ Hai)'],['theoThang','Theo tháng'],['theoKhachHang','Theo khách hàng'],['theoDichVu','Theo dịch vụ']] as const).map(([k,title])=><section key={k}><h3>{title}</h3><table className="data-table"><thead><tr><th>Nhóm</th><th>Số đơn</th></tr></thead><tbody>{Object.entries(data[k]).map(([label,count])=><tr key={label}><td>{k==='theoKhachHang'?(kh.find(v=>v.maKhachHang===label)?.tenKhachHang??label):k==='theoDichVu'?(dv.find(v=>v.maDichVu===label)?.tenDichVu??label):label}</td><td>{count}</td></tr>)}</tbody></table></section>)}</div></>}</div></div>
  </>
}

