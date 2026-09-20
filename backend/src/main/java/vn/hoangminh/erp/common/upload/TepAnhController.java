package vn.hoangminh.erp.common.upload;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.hoangminh.erp.common.config.AppProperties;
import vn.hoangminh.erp.common.exception.LoiNghiepVuException;

/**
 * Tải ảnh minh chứng giao hàng và ảnh hiện trường sự cố (mục 3.3.3, 3.3.2) — trước đây API chỉ nhận
 * đường dẫn có sẵn nên trên giao diện không đính kèm được ảnh thật (NOTES N31).
 *
 * <p>File lưu ngoài CSDL tại {@code erp.upload.dir}; CSDL chỉ giữ đường dẫn tương đối
 * {@code /uploads/…} đúng nguyên tắc mục 2 huong_di_lap_trinh.
 */
@RestController
@RequestMapping("/api/tep")
@RequiredArgsConstructor
public class TepAnhController {

  /** Chỉ nhận ảnh; giới hạn dung lượng còn do spring.servlet.multipart chặn ở mức 10MB. */
  private static final Set<String> DUOI_CHO_PHEP = Set.of("jpg", "jpeg", "png", "webp", "gif");
  private static final DateTimeFormatter TEN_THEO_NGAY = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

  private final AppProperties props;

  public record TepDaTai(String duongDan, String tenGoc, long kichThuoc) {}

  @PostMapping("/anh")
  public TepDaTai taiAnh(@RequestParam("file") MultipartFile file) {
    if (file == null || file.isEmpty()) throw LoiNghiepVuException.duLieu("tep.rong");
    String loai = file.getContentType();
    if (loai == null || !loai.toLowerCase(Locale.ROOT).startsWith("image/"))
      throw LoiNghiepVuException.duLieu("tep.khongphaianh");

    String duoi = duoiFile(file.getOriginalFilename());
    if (!DUOI_CHO_PHEP.contains(duoi)) throw LoiNghiepVuException.duLieu("tep.khongphaianh");

    String ten = LocalDateTime.now().format(TEN_THEO_NGAY) + "-" + UUID.randomUUID().toString().substring(0, 8) + "." + duoi;
    try {
      Path thuMuc = Path.of(props.upload() == null ? "./uploads" : props.upload().dir()).toAbsolutePath().normalize();
      Files.createDirectories(thuMuc);
      try (var in = file.getInputStream()) {
        Files.copy(in, thuMuc.resolve(ten), StandardCopyOption.REPLACE_EXISTING);
      }
    } catch (IOException e) {
      throw LoiNghiepVuException.duLieu("tep.luukhongduoc");
    }
    return new TepDaTai("/uploads/" + ten, file.getOriginalFilename(), file.getSize());
  }

  private static String duoiFile(String ten) {
    if (ten == null) return "";
    int cham = ten.lastIndexOf('.');
    return cham < 0 ? "" : ten.substring(cham + 1).toLowerCase(Locale.ROOT);
  }
}
