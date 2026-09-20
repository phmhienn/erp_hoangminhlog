package vn.hoangminh.erp.common.donhang;

/**
 * Sự kiện đơn hàng đổi trạng thái, phát khi {@link DonHangStateService#doiTrangThai} thành công.
 *
 * <p>Dùng để các phân hệ hạ nguồn tự chạy nghiệp vụ liên thông mà không phụ thuộc ngược lên nhau
 * (mục 2.4.5: COD "tự động tổng hợp khi đơn hàng chuyển trạng thái đã giao thành công").
 */
public record DonHangDoiTrangThaiEvent(String maDonHang, String trangThaiCu, String trangThaiMoi) {}
