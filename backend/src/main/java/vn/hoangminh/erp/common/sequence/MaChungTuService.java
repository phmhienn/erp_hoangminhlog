package vn.hoangminh.erp.common.sequence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Cấp mã chứng từ <b>tự động</b> sau khi lưu (đặc tả 3.1.3 bước 2), tập trung một nơi
 * để mọi hệ thống dùng chung quy tắc (mục 4 Backend). Người dùng không được nhập mã.
 *
 * <p>Cách cấp: lấy {@code MAX(mã)} hiện có theo đúng tiền tố rồi +1. Vì phần số luôn
 * cố định 7 chữ số nên so sánh chuỗi trùng với so sánh số. Mã được cấp trong cùng
 * transaction với thao tác lưu; nếu trùng do ghi đồng thời, ràng buộc PK sẽ báo và
 * nghiệp vụ được thử lại — demo một phiên nên không cần bảng cấp số riêng
 * (không thêm bảng ngoài lược đồ tài liệu).
 */
@Service
public class MaChungTuService {

  private static final Logger log = LoggerFactory.getLogger(MaChungTuService.class);

  private final JdbcTemplate jdbc;

  public MaChungTuService(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  /** Mã kế tiếp của một loại chứng từ, ví dụ {@code DH0000001}. */
  public String capMa(LoaiChungTu loai) {
    // Tên bảng/cột là hằng số của enum (đúng ERP_database.sql), không nhận từ người dùng
    String sql =
        "SELECT MAX(" + loai.cotMa() + ") FROM " + loai.bang() + " WHERE " + loai.cotMa() + " LIKE ?";
    String max = jdbc.queryForObject(sql, String.class, loai.tienTo() + "%");
    int soThuTu = 1;
    if (max != null && max.length() > loai.tienTo().length()) {
      try {
        soThuTu = Integer.parseInt(max.substring(loai.tienTo().length()).trim()) + 1;
      } catch (NumberFormatException e) {
        log.warn(
            "Ma hien co '{}' cua {} khong theo dinh dang {}+{} chu so; cap moi tu 1",
            max,
            loai,
            loai.tienTo(),
            LoaiChungTu.SO_CHU_SO);
      }
    }
    return loai.ma(soThuTu);
  }

  /**
   * Mã hiển thị của chuyến vận: {@code ChuyenVan.MaChuyen} là {@code INT AUTO_INCREMENT}
   * theo lược đồ (3.3.4/4.1) nên mã {@code CV…} là dạng trình bày của số đó, không lưu thêm cột.
   */
  public String maHienThiChuyen(long maChuyen) {
    return "CV" + String.format("%0" + LoaiChungTu.SO_CHU_SO + "d", maChuyen);
  }
}
