import { useEffect, useState } from 'react'
import { createPortal } from 'react-dom'

interface AnhXemDuocProps {
  /** Đường dẫn ảnh do backend trả về, ví dụ /uploads/abc.jpg */
  src: string
  alt?: string
  /** Lớp CSS cho ảnh thu nhỏ: anh-nho trong bảng, anh-xem-truoc trong form. */
  className?: string
}

/**
 * Ảnh thu nhỏ bấm được: phóng to ngay trong trang thay vì mở tab mới
 * (mở tab mới làm router SPA nuốt đường dẫn /uploads và báo lỗi quyền).
 */
export function AnhXemDuoc({ src, alt = 'Ảnh đính kèm', className = 'anh-nho' }: AnhXemDuocProps) {
  const [mo, setMo] = useState(false)
  const [hong, setHong] = useState(false)

  useEffect(() => {
    if (!mo) return
    const phim = (e: KeyboardEvent) => { if (e.key === 'Escape') setMo(false) }
    window.addEventListener('keydown', phim)
    document.body.classList.add('khoa-cuon')
    return () => { window.removeEventListener('keydown', phim); document.body.classList.remove('khoa-cuon') }
  }, [mo])

  if (hong) return <span className="anh-hong" title={src}>Ảnh không tải được</span>

  return <>
    <button type="button" className="nut-anh" title="Bấm để phóng to ảnh" onClick={() => setMo(true)}>
      <img className={className} src={src} alt={alt} loading="lazy" onError={() => setHong(true)} />
    </button>
    {mo && createPortal(
      <div className="lop-phu-anh" role="dialog" aria-modal="true" aria-label={alt} onClick={() => setMo(false)}>
        <button type="button" className="btn dong-anh" onClick={() => setMo(false)}>Đóng (Esc)</button>
        <img className="anh-phong-to" src={src} alt={alt} onClick={(e) => e.stopPropagation()} />
      </div>, document.body)}
  </>
}
