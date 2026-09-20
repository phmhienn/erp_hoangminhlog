// Mô tả chuỗi liên kết giữa 5 hệ thống cốt lõi, lấy đúng nội dung tài liệu đặc tả:
// mục 2.3 (vì sao là cốt lõi), 2.4 (đặc điểm từng hệ thống), 3.x.1 (bàn giao dữ liệu giữa các hệ thống),
// mục 4.2–4.3 (bảng dùng chung) và PHỤ LỤC C (chủ sở hữu từng trạng thái đơn hàng).

export interface HeThongCotLoi {
  ma: string
  ten: string
  phanHe: string
  /** Vai trò của hệ thống trong chuỗi — mục 2.3. */
  vaiTro: string
  /** Việc chính hệ thống làm — mục 2.4. */
  viec: string[]
}

/** Bốn hệ thống nằm trên dòng chảy chính của một đơn hàng, theo đúng thứ tự vòng đời. */
export const CHUOI_CHINH: HeThongCotLoi[] = [
  {
    ma: 'HT1', ten: 'Quản lý đơn hàng/dịch vụ', phanHe: 'Kinh doanh',
    vaiTro: 'Điểm khởi đầu của toàn bộ chuỗi, quyết định dữ liệu đầu vào cho các hệ thống còn lại.',
    viec: ['Tiếp nhận yêu cầu, quản lý khách hàng', 'Tạo đơn và cấp mã vận đơn', 'Kiểm duyệt và theo dõi đơn'],
  },
  {
    ma: 'HT2', ten: 'Quản lý nhập – xuất kho', phanHe: 'Kho vận',
    vaiTro: 'Khâu trung chuyển bắt buộc giữa tiếp nhận đơn và quá trình giao hàng.',
    viec: ['Kiểm đếm và lập phiếu nhập', 'Sắp xếp vị trí lưu trữ', 'Xuất kho bàn giao vận tải'],
  },
  {
    ma: 'HT3', ten: 'Quản lý điều phối & giao hàng', phanHe: 'Vận tải – Giao nhận',
    vaiTro: 'Quyết định trực tiếp chất lượng dịch vụ: thời gian và độ chính xác giao hàng.',
    viec: ['Lập tuyến, phân công tài xế và xe', 'Cập nhật mốc hành trình, sự cố', 'Xác nhận giao hàng kèm minh chứng'],
  },
  {
    ma: 'HT5', ten: 'Quản lý thu chi & COD', phanHe: 'Tài chính – Kế toán',
    vaiTro: 'Điểm kết thúc của chuỗi giá trị, tổng hợp toàn bộ dữ liệu tài chính của dịch vụ.',
    viec: ['Đối soát COD theo đợt', 'Lập và duyệt phiếu thu/chi', 'Ghi sổ quỹ, theo dõi công nợ'],
  },
]

/** Hệ thống nền, không nằm trên dòng chảy đơn hàng mà cấp dữ liệu dùng chung cho HT2 và HT3. */
export const HE_THONG_NEN: HeThongCotLoi = {
  ma: 'HT4', ten: 'Quản lý hồ sơ nhân viên', phanHe: 'Nhân sự',
  vaiTro: 'Cấp dữ liệu nhân sự dùng chung cho Kho và Vận tải khi phân công công việc.',
  viec: ['Hồ sơ nhân viên và tài xế', 'Trạng thái làm việc', 'Yêu cầu cập nhật thông tin'],
}

/** Dữ liệu được bàn giao giữa hai hệ thống liền kề trong chuỗi. */
export const BAN_GIAO: { tu: string; den: string; duLieu: string }[] = [
  { tu: 'HT1', den: 'HT2', duLieu: 'Đơn đã duyệt + hàng hoá' },
  { tu: 'HT2', den: 'HT3', duLieu: 'Hàng đã nhập kho, sẵn sàng đi' },
  { tu: 'HT3', den: 'HT5', duLieu: 'Đơn "Đã giao" + tiền COD phải thu' },
]

/** Vòng đời trạng thái đơn hàng và hệ thống sở hữu mỗi bước (ENUM 4.2 — PHỤ LỤC C). */
export const VONG_DOI: { trangThai: string; chu: string }[] = [
  { trangThai: 'Đã tạo', chu: 'HT1' },
  { trangThai: 'Đã nhập kho', chu: 'HT2' },
  { trangThai: 'Đang giao', chu: 'HT3' },
  { trangThai: 'Đã giao', chu: 'HT3' },
  { trangThai: 'Đã đối soát', chu: 'HT5' },
]

/** Hồ sơ doanh nghiệp — mục 2.1.1 và 2.1.6 của tài liệu. */
export const DOANH_NGHIEP = {
  tenDayDu: 'Công ty Cổ phần Đầu tư Thương mại và Dịch vụ Hoàng Minh',
  thuongHieu: 'Hoàng Minh Logistics',
  maSoThue: '0109259120',
  daiDien: 'Nguyễn Minh Tân — Tổng Giám đốc',
  diaChi: 'Số 12, Đường Tiên Phong, Thôn Lương Châu, Xã Sóc Sơn, Hà Nội',
  quyMo: [
    { nhan: 'Nhân sự', gia: '60 – 80', donVi: 'người' },
    { nhan: 'Kho trung chuyển', gia: '500 – 700', donVi: 'm²' },
    { nhan: 'Điểm giao dịch', gia: '3 – 5', donVi: 'điểm' },
    { nhan: 'Sản lượng', gia: '500 – 800', donVi: 'đơn/ngày' },
  ],
}
