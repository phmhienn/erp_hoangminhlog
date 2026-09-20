import { useRef, useState } from 'react'
import { taiAnh, thongBaoLoi } from '@/shared/api/http'
import { AnhXemDuoc } from './XemAnh'

interface ChonAnhProps {
  /** Đường dẫn ảnh đã lưu (giá trị gửi lên API), rỗng nghĩa là chưa có ảnh. */
  giaTri: string
  onThayDoi: (duongDan: string) => void
  nhan?: string
  batBuoc?: boolean
}

/**
 * Chọn ảnh từ máy và tải lên máy chủ, trả về đường dẫn tương đối để lưu vào CSDL
 * (ảnh hiện trường sự cố 3.3.2, minh chứng giao hàng 3.3.3).
 */
export function ChonAnh({ giaTri, onThayDoi, nhan = 'Ảnh', batBuoc = false }: ChonAnhProps) {
  const oFile = useRef<HTMLInputElement>(null)
  const [dangTai, setDangTai] = useState(false)
  const [loi, setLoi] = useState('')

  async function chon(file: File | undefined) {
    if (!file) return
    setDangTai(true); setLoi('')
    try { onThayDoi(await taiAnh(file)) }
    catch (e) { setLoi(thongBaoLoi(e, 'Không tải được ảnh')) }
    finally { setDangTai(false); if (oFile.current) oFile.current.value = '' }
  }

  return (
    <div className="chon-anh">
      <span className="label">{nhan}{batBuoc && <span className="bat-buoc"> *</span>}</span>
      {giaTri ? (
        <div className="anh-da-chon">
          <AnhXemDuoc src={giaTri} alt={nhan} className="anh-xem-truoc" />
          <div>
            <span className="mono">{giaTri.split('/').pop()}</span>
            <button type="button" className="btn btn-nho btn-nguy-hiem" onClick={() => onThayDoi('')}>Xoá ảnh</button>
          </div>
        </div>
      ) : (
        <>
          <input ref={oFile} className="input" type="file" accept="image/*" disabled={dangTai}
            onChange={(e) => void chon(e.target.files?.[0])} />
          <span className="goi-y">{dangTai ? 'Đang tải ảnh lên…' : 'Chọn ảnh JPG, PNG, WEBP hoặc GIF (tối đa 10MB)'}</span>
        </>
      )}
      {loi && <span className="goi-y loi-anh">{loi}</span>}
    </div>
  )
}
