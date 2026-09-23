import { t } from '@/locales/i18n'

// Menu của 5 hệ thống cốt lõi (mục 2.3 tài liệu).
// Mỗi mục gắn với MỘT quyền chức năng (ChucNang ở backend) — mục nào người dùng không có
// quyền thì không render (ô "–" của ma trận PHỤ LỤC B cũng bị API chặn).

export interface MenuItem {
  /** Mã quyền chức năng, khớp enum ChucNang của backend. */
  ma: string
  ten: string
  duongDan: string
}

export interface MenuHeThong {
  he: string
  ten: string
  phanHe: string
  muc: MenuItem[]
}

export const MENU: MenuHeThong[] = [
  {
    he: 'HT1',
    ten: t('hethong.HT1'),
    phanHe: t('phanhe.HT1'),
    muc: [
      { ma: 'HT1_QUAN_LY_KHACH_HANG', ten: 'Thông tin khách hàng', duongDan: '/ht1/khach-hang' },
      { ma: 'HT1_TAO_DON_HANG', ten: 'Tạo đơn hàng', duongDan: '/ht1/don-hang' },
      { ma: 'HT1_KIEM_DUYET_DON_HANG', ten: 'Kiểm duyệt đơn hàng', duongDan: '/ht1/kiem-duyet' },
      { ma: 'HT1_TRA_CUU_DON_HANG', ten: 'Tra cứu đơn hàng', duongDan: '/ht1/tra-cuu' },
      { ma: 'HT1_THEO_DOI_DON_HANG', ten: 'Theo dõi đơn hàng', duongDan: '/ht1/theo-doi' },
      { ma: 'HT1_XEM_BAO_CAO_DON_HANG', ten: 'Báo cáo đơn hàng', duongDan: '/ht1/bao-cao' },
    ],
  },
  {
    he: 'HT2',
    ten: t('hethong.HT2'),
    phanHe: t('phanhe.HT2'),
    muc: [
      // Nhân viên kho — tác nghiệp (biểu đồ usecase 3.2.3)
      { ma: 'HT2_TRA_CUU_DON_NHAP_KHO', ten: 'Báo cáo phiếu nhập/xuất', duongDan: '/ht2/bao-cao-phieu' },
      { ma: 'HT2_LAP_PHIEU_NHAP', ten: 'Lập – cập nhật phiếu nhập', duongDan: '/ht2/phieu-nhap' },
      { ma: 'HT2_LAP_PHIEU_XUAT', ten: 'Lập – cập nhật phiếu xuất', duongDan: '/ht2/phieu-xuat' },
      { ma: 'HT2_KIEM_KE', ten: 'Kiểm kê đơn hàng', duongDan: '/ht2/kiem-ke' },
      // Người quản lý kho — giám sát
      { ma: 'HT2_PHE_DUYET_PHIEU_NHAP', ten: 'Quản lí phiếu nhập – xuất', duongDan: '/ht2/phe-duyet' },
      { ma: 'HT2_DASHBOARD_NHAP_XUAT', ten: 'Theo dõi kho', duongDan: '/ht2/dashboard' },
      { ma: 'HT2_BAO_CAO_NHAP_KHO', ten: 'Báo cáo nhập – xuất kho', duongDan: '/ht2/bao-cao' },
    ],
  },
  {
    he: 'HT3',
    ten: t('hethong.HT3'),
    phanHe: t('phanhe.HT3'),
    muc: [
      { ma: 'HT3_LAP_KE_HOACH_TUYEN', ten: 'Kế hoạch & phân luồng tuyến', duongDan: '/ht3/ke-hoach' },
      { ma: 'HT3_PHAN_CONG_TAI_XE', ten: 'Phân công tài xế & phương tiện', duongDan: '/ht3/phan-cong' },
      { ma: 'HT3_DUYET_DIEU_PHOI', ten: 'Phê duyệt điều phối', duongDan: '/ht3/phe-duyet' },
      { ma: 'HT3_NHIEM_VU_CUA_TOI', ten: 'Nhiệm vụ của tôi', duongDan: '/ht3/nhiem-vu' },
      { ma: 'HT3_XAC_NHAN_GIAO_HANG', ten: 'Xác nhận giao hàng', duongDan: '/ht3/giao-hang' },
      { ma: 'HT3_BAO_CAO_SU_CO', ten: 'Báo cáo sự cố', duongDan: '/ht3/su-co' },
      { ma: 'HT3_TRA_CUU_LICH_SU_GIAO', ten: 'Tra cứu lịch sử giao hàng', duongDan: '/ht3/lich-su' },
    ],
  },
  {
    he: 'HT4',
    ten: t('hethong.HT4'),
    phanHe: t('phanhe.HT4'),
    muc: [
      { ma: 'HT4_XEM_HO_SO', ten: 'Danh sách hồ sơ nhân viên', duongDan: '/ht4/ho-so' },
      { ma: 'HT4_THEM_HO_SO', ten: 'Thêm hồ sơ nhân viên', duongDan: '/ht4/them' },
      { ma: 'HT4_QUAN_LY_TRANG_THAI', ten: 'Quản lý trạng thái nhân viên', duongDan: '/ht4/trang-thai' },
      { ma: 'HT4_XEM_HO_SO_CA_NHAN', ten: 'Hồ sơ của tôi', duongDan: '/ht4/ca-nhan' },
    ],
  },
  {
    he: 'HT5',
    ten: t('hethong.HT5'),
    phanHe: t('phanhe.HT5'),
    muc: [
      // Kế toán viên (biểu đồ usecase 3.5.3)
      { ma: 'HT5_TRA_CUU_COD', ten: 'Tra cứu dữ liệu COD', duongDan: '/ht5/cod' },
      { ma: 'HT5_DOI_SOAT_COD', ten: 'Đối soát COD', duongDan: '/ht5/doi-soat' },
      { ma: 'HT5_LAP_PHIEU_THU', ten: 'Lập phiếu thu', duongDan: '/ht5/phieu-thu' },
      { ma: 'HT5_LAP_PHIEU_CHI', ten: 'Lập phiếu chi', duongDan: '/ht5/phieu-chi' },
      { ma: 'HT5_QUAN_LY_SAI_LECH', ten: 'Quản lý sai lệch COD', duongDan: '/ht5/sai-lech' },
      // Thủ quỹ
      { ma: 'HT5_XAC_NHAN_THU_TIEN', ten: 'Xác nhận thu tiền', duongDan: '/ht5/xac-nhan-thu' },
      { ma: 'HT5_XAC_NHAN_CHI_TRA', ten: 'Xác nhận chi trả COD', duongDan: '/ht5/xac-nhan-chi' },
      { ma: 'HT5_CAP_NHAT_TRANG_THAI_GD', ten: 'Cập nhật trạng thái giao dịch', duongDan: '/ht5/xac-nhan' },
      // Kế toán trưởng
      { ma: 'HT5_PHE_DUYET_GIAO_DICH', ten: 'Phê duyệt giao dịch', duongDan: '/ht5/phe-duyet' },
      // Dùng chung theo mũi tên của biểu đồ
      { ma: 'HT5_LICH_SU_GIAO_DICH', ten: 'Tra cứu lịch sử giao dịch', duongDan: '/ht5/lich-su' },
      { ma: 'HT5_BAO_CAO_THU_CHI', ten: 'Xem báo cáo thu – chi COD', duongDan: '/ht5/bao-cao' },
    ],
  },
]

/** Mục dùng chung, không gắn quyền chức năng (mọi người dùng đã đăng nhập đều xem được). */
export const MENU_CHUNG: MenuItem[] = [
  { ma: 'TRANG_CHU', ten: 'Trang chủ', duongDan: '/' },
]




