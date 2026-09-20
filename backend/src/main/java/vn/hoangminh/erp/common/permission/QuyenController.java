package vn.hoangminh.erp.common.permission;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.hoangminh.erp.common.permission.dto.QuyenCuaToiResponse;
import vn.hoangminh.erp.common.security.AuthService;

/**
 * API phân quyền: frontend đọc ma trận PHỤ LỤC B của chính người dùng đang đăng nhập
 * để dựng menu và nút thao tác (mục 4 Frontend — {@code usePermission()}).
 */
@RestController
@RequestMapping("/api/quyen")
public class QuyenController {

  private final AuthService authService;
  private final QuyenService quyenService;

  public QuyenController(AuthService authService, QuyenService quyenService) {
    this.authService = authService;
    this.quyenService = quyenService;
  }

  @GetMapping("/cua-toi")
  public QuyenCuaToiResponse cuaToi() {
    return authService.quyenHienTai();
  }

  /** Toàn bộ ma trận PHỤ LỤC B (dữ liệu tham chiếu cho kiểm thử phân quyền mục 6). */
  @GetMapping("/ma-tran")
  public Map<NhomBang, Map<String, Set<HanhDong>>> maTran() {
    return quyenService.maTranDayDu();
  }

  /** Danh sách vai trò và chức năng — dùng cho trang quản trị và kiểm thử. */
  @GetMapping("/vai-tro")
  public List<Map<String, Object>> vaiTro() {
    return java.util.Arrays.stream(VaiTro.values())
        .map(
            v ->
                Map.<String, Object>of(
                    "ma", v.name(),
                    "ten", quyenService.tenVaiTro(v),
                    "heThong", v.heThong().stream().sorted().toList()))
        .toList();
  }
}
