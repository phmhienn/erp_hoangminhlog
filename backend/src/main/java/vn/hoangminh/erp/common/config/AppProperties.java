package vn.hoangminh.erp.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Cấu hình ngoài của ứng dụng (prefix {@code erp}) khai báo trong application.yml. */
@ConfigurationProperties(prefix = "erp")
public record AppProperties(Jwt jwt, Upload upload, Cors cors) {

  /** Tham số ký và hết hạn của JWT dùng cho phiên làm việc (nhiệm vụ 0.4). */
  public record Jwt(String secret, long expirationMinutes, String issuer) {}

  /** Thư mục lưu file ảnh minh chứng / sự cố; CSDL chỉ lưu đường dẫn. */
  public record Upload(String dir) {}

  /** Origin của frontend khi chạy dev server tách cổng. */
  public record Cors(String allowedOrigins) {}
}
