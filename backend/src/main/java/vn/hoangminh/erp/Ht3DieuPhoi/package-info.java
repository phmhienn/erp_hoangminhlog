/**
 * <b>HT3 — Quản lý điều phối &amp; giao hàng</b> (phân hệ Vận tải – Giao nhận, mục 2.3 và 3.3).
 *
 * <p>Bảng sở hữu (3.3.4 và 4.1): {@code PhuongTien}, {@code ChuyenVan}, {@code LoTrinh},
 * {@code SuCoVanTai}, {@code ChiTietChuyenHang} (ma trận 4.3); bảng kỹ thuật
 * {@code MinhChungGiaoHang} [G16].
 *
 * <p>Trạng thái đơn do HT3 sở hữu: <i>Đang giao</i>, <i>Đã giao</i> (PHỤ LỤC C).
 * Nhãn hiển thị của 3.3.3 ("Chờ tiếp nhận", "Đang giao hàng", "Đã hoàn thành") chỉ là ánh xạ
 * trình bày của ENUM 4.2, không tạo giá trị trạng thái mới.
 * Quyền: PHỤ LỤC B cột HT3 (Đ/T/S/X trên bảng điều phối; Sửa(trangThai) trên TaiXe).
 *
 * <p>Triển khai ở Phase 3.
 */
package vn.hoangminh.erp.Ht3DieuPhoi;
