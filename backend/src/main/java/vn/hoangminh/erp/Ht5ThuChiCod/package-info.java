/**
 * <b>HT5 — Quản lý thu chi &amp; COD</b> (phân hệ Tài chính – Kế toán, mục 2.3 và 3.5).
 *
 * <p>Bảng sở hữu (3.5.4 và 4.1): {@code CongNo}, {@code GiaoDichCOD}, {@code PhieuThuChi},
 * {@code SoQuy}, {@code DoiSoatCOD}; bảng kỹ thuật {@code ChiTietDoiSoat} [G15] và
 * {@code ChiPhiLuuKho} [G16].
 *
 * <p>Trạng thái đơn do HT5 sở hữu: <i>Đã đối soát</i> (PHỤ LỤC C). Đơn sang <i>Đã giao</i> →
 * tự sinh {@code GiaoDichCOD} với {@code soTienPhaiThu = DonHang.tienCOD} (mục 5 huong_di).
 * {@code CongNo.soDuNo} là cột GENERATED — không ghi tay [G11].
 * Quyền: PHỤ LỤC B cột HT5 (Đọc + Ghi ChiPhiLuuKho trên bảng kho; Đọc/Sửa(trangThai, tienCOD)
 * trên DonHang).
 *
 * <p>Triển khai ở Phase 5.
 */
package vn.hoangminh.erp.Ht5ThuChiCod;
