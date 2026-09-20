package vn.hoangminh.erp.common.upload;

import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import vn.hoangminh.erp.common.config.AppProperties;

/** Phục vụ ảnh đã tải lên tại {@code /uploads/**} để giao diện hiển thị được minh chứng. */
@Configuration
@RequiredArgsConstructor
public class WebUploadConfig implements WebMvcConfigurer {

  private final AppProperties props;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    Path thuMuc = Path.of(props.upload() == null ? "./uploads" : props.upload().dir()).toAbsolutePath().normalize();
    registry.addResourceHandler("/uploads/**").addResourceLocations(thuMuc.toUri().toString());
  }
}
