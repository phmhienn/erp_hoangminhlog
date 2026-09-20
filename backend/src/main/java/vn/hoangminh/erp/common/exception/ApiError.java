package vn.hoangminh.erp.common.exception;

/**
 * Cấu trúc lỗi JSON thống nhất của toàn hệ thống: {@code { maLoi, thongBao, chiTiet }}.
 *
 * @param maLoi    mã lỗi = khoá trong {@code messages_vi.properties}
 * @param thongBao câu thông báo nguyên văn trả về cho người dùng
 * @param chiTiet  chi tiết kỹ thuật (lỗi từng trường, ràng buộc…); {@code null} nếu không có
 */
public record ApiError(String maLoi, String thongBao, Object chiTiet) {}
