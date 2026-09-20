package vn.hoangminh.erp.common.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Yêu cầu đăng nhập bằng bảng dùng chung {@code TaiKhoan} (nhiệm vụ 0.4).
 * Độ dài theo PHỤ LỤC A: {@code tenDangNhap VARCHAR(50)}, {@code matKhau VARCHAR(255)}.
 */
public record DangNhapRequest(
    @NotBlank(message = "{auth.dangnhap.thieuthongtin}")
        @Size(max = 50, message = "{loi.dulieu.khonghople}")
        String tenDangNhap,
    @NotBlank(message = "{auth.dangnhap.thieuthongtin}")
        @Size(max = 255, message = "{loi.dulieu.khonghople}")
        String matKhau) {}
