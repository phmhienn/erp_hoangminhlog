/**
 * <b>HT1 — Quản lý đơn hàng/dịch vụ</b> (phân hệ Kinh doanh, mục 2.3 và 3.1 tài liệu).
 *
 * <p>Bảng sở hữu: {@code KhachHang}, {@code DichVu} (3.1.4); đọc/ghi bảng dùng chung
 * {@code DonHang}, {@code HangHoa} (4.2) và bảng kỹ thuật {@code LichSuDonHang} [G16].
 *
 * <p>Trạng thái đơn do HT1 sở hữu: <i>Đã tạo</i>, <i>Đã hủy</i> (PHỤ LỤC C).
 * Quyền: PHỤ LỤC B dòng "KhachHang, DichVu" và "DonHang, HangHoa" — cột HT1.
 *
 * <p>Module khác chỉ được gọi service công khai của package {@code service}, không dùng
 * {@code repository} của HT1 (quy tắc module, mục 3 huong_di_lap_trinh).
 *
 * <p>Triển khai ở Phase 1.
 */
package vn.hoangminh.erp.Ht1DonHang;
