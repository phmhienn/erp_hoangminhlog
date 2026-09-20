/**
 * <b>HT2 — Quản lý nhập – xuất kho</b> (phân hệ Kho vận, mục 2.3 và 3.2 tài liệu).
 *
 * <p>Bảng sở hữu (3.2.4): {@code PHIEU_NHAP}, {@code CHI_TIET_PHIEU_NHAP}, {@code PHIEU_XUAT},
 * {@code CHI_TIET_PHIEU_XUAT}, {@code PHIEU_KIEM_KE}, {@code CHI_TIET_KIEM_KE}, {@code TON_KHO},
 * {@code SanPham}; bảng kỹ thuật {@code BienBanSuCo} [G16].
 *
 * <p>Trạng thái đơn do HT2 sở hữu: <i>Đã nhập kho</i> (PHỤ LỤC C). Duyệt phiếu nhập →
 * tăng {@code TON_KHO.soLuongTon} real-time trong cùng transaction.
 * Quyền: PHỤ LỤC B cột HT2 (Đ/T/S/X trên bảng kho; Đọc/Sửa(trangThai) trên DonHang).
 *
 * <p>Triển khai ở Phase 2.
 */
package vn.hoangminh.erp.Ht2Kho;
