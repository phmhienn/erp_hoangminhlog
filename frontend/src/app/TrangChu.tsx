import { usePermission } from '@/shared/permission/PermissionContext'
import { HeIcon } from '@/shared/components/HeIcon'
import {
  BAN_GIAO, CHUOI_CHINH, DOANH_NGHIEP, HE_THONG_NEN, VONG_DOI,
  type HeThongCotLoi,
} from '@/app/chuoiNghiepVu'

/** Trang chủ: chuỗi liên kết giữa 5 hệ thống cốt lõi (mục 2.3) và vị trí của người đang đăng nhập. */
export function TrangChu() {
  const { nguoiDung } = usePermission()
  if (!nguoiDung) return null

  const cuaToi = (ma: string) => nguoiDung.heThong.includes(ma)
  const heCuaToi = [...CHUOI_CHINH, HE_THONG_NEN].filter((h) => cuaToi(h.ma))

  return (
    <>
      <div className="page-header">
        <h1 className="page-title">Xin chào, {nguoiDung.hoTen}</h1>
        <p className="page-sub">
          {nguoiDung.tenVaiTro}
          {nguoiDung.tenPhongBan ? ` · ${nguoiDung.tenPhongBan}` : ''}
          {heCuaToi.length
            ? ` — bạn phụ trách ${heCuaToi.map((h) => `${h.ma} ${h.ten.toLowerCase()}`).join(' và ')}.`
            : ' — bạn xem được hồ sơ của chính mình.'}
        </p>
      </div>

      <div className="card">
        <div className="card-header">
          <h3>Chuỗi liên kết 5 hệ thống cốt lõi</h3>
          <span className="phu">một đơn hàng đi qua bốn hệ thống, nhân sự là dữ liệu nền dùng chung</span>
        </div>
        <div className="card-body">
          <div className="so-do-chuoi">
            {CHUOI_CHINH.map((he, i) => (
              <div className="mat-xich" key={he.ma}>
                <NutHeThong he={he} laCuaToi={cuaToi(he.ma)} />
                {i < CHUOI_CHINH.length - 1 && (
                  <div className="noi-chuoi" aria-hidden="true">
                    <span className="nhan-ban-giao">{BAN_GIAO[i]?.duLieu}</span>
                    <span className="mui-ten" />
                  </div>
                )}
              </div>
            ))}
          </div>

          <div className="nhanh-nen">
            <NutHeThong he={HE_THONG_NEN} laCuaToi={cuaToi(HE_THONG_NEN.ma)} nen />
            <p className="giai-thich-nen">
              <b>HT4</b> không nằm trên dòng chảy đơn hàng: hồ sơ nhân viên và tài xế là dữ liệu nền mà
              <b> HT2</b> dùng khi lập phiếu kho và <b>HT3</b> dùng khi phân công chuyến — đúng nguyên tắc
              một cơ sở dữ liệu dùng chung, không nhập lại dữ liệu.
            </p>
          </div>
        </div>
      </div>

      <div className="card">
        <div className="card-header">
          <h3>Vòng đời trạng thái đơn hàng</h3>
          <span className="phu">mỗi bước chỉ hệ thống sở hữu mới được ghi (PHỤ LỤC C)</span>
        </div>
        <div className="card-body">
          <ol className="vong-doi">
            {VONG_DOI.map((b) => (
              <li key={b.trangThai} className={cuaToi(b.chu) ? 'cua-toi' : undefined}>
                <span className="buoc">{b.trangThai}</span>
                <span className="chu-so-huu">{b.chu} ghi</span>
              </li>
            ))}
          </ol>
          <p className="muted" style={{ fontSize: 12.5, marginTop: 14 }}>
            Nhánh phụ: <b>HT1</b> ghi <i>Đã hủy</i> khi đơn chưa vào kho. Mọi lần đổi trạng thái đều lưu người
            thực hiện và thời gian trong <span className="mono">LichSuDonHang</span>; khi đơn sang <i>Đã giao</i>,
            hệ thống tự sinh <span className="mono">GiaoDichCOD</span> cho HT5 đối soát.
          </p>
        </div>
      </div>

      <div className="card">
        <div className="card-header"><h3>{DOANH_NGHIEP.thuongHieu}</h3><span className="phu">mục 2.1</span></div>
        <div className="card-body">
          <p className="ten-day-du">{DOANH_NGHIEP.tenDayDu}</p>
          <div className="quy-mo">
            {DOANH_NGHIEP.quyMo.map((q) => (
              <div key={q.nhan}><span className="gia">{q.gia}</span><span className="don-vi">{q.donVi}</span><span className="nhan">{q.nhan}</span></div>
            ))}
          </div>
          <dl className="thong-tin-dn">
            <dt>Mã số thuế</dt><dd className="mono">{DOANH_NGHIEP.maSoThue}</dd>
            <dt>Đại diện</dt><dd>{DOANH_NGHIEP.daiDien}</dd>
            <dt>Địa chỉ</dt><dd>{DOANH_NGHIEP.diaChi}</dd>
          </dl>
        </div>
      </div>
    </>
  )
}

function NutHeThong({ he, laCuaToi, nen }: { he: HeThongCotLoi; laCuaToi: boolean; nen?: boolean }) {
  return (
    <article className={`nut-he${laCuaToi ? ' cua-toi' : ''}${nen ? ' nen' : ''}`} data-he={he.ma}>
      <header>
        <span className="ma-he"><HeIcon he={he.ma} />{he.ma}</span>
        {laCuaToi && <span className="badge dang-xu-ly">Bạn ở đây</span>}
      </header>
      <h4>{he.ten}</h4>
      <p className="phan-he">Phân hệ {he.phanHe}</p>
      <p className="vai-tro-he">{he.vaiTro}</p>
      <ul>{he.viec.map((v) => <li key={v}>{v}</li>)}</ul>
    </article>
  )
}
