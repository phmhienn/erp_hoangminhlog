package vn.hoangminh.erp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Chương trình thử nghiệm ERP — Công ty CP Đầu tư Thương mại và Dịch vụ Hoàng Minh.
 *
 * <p>Kiến trúc: modular monolith (mục 2 huong_di_lap_trinh). Một ứng dụng Spring Boot,
 * 5 module theo 5 hệ thống cốt lõi (mục 2.3 tài liệu), một CSDL dùng chung duy nhất (mục 2.6).
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class ErpHoangMinhApplication {

  public static void main(String[] args) {
    SpringApplication.run(ErpHoangMinhApplication.class, args);
  }
}
