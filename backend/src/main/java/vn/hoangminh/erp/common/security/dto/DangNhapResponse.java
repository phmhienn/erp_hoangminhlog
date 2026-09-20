package vn.hoangminh.erp.common.security.dto;

import vn.hoangminh.erp.common.permission.dto.QuyenCuaToiResponse;

/**
 * Kết quả đăng nhập: token của phiên làm việc kèm toàn bộ quyền của người dùng
 * (để frontend dựng menu/ngay mà không cần gọi thêm {@code /api/quyen/cua-toi}).
 */
public record DangNhapResponse(
    String accessToken,
    String loaiToken,
    long hetHanSauPhut,
    QuyenCuaToiResponse nguoiDung) {}
