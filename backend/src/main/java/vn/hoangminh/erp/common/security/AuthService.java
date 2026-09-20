package vn.hoangminh.erp.common.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hoangminh.erp.Ht4NhanSu.service.NhanVienQueryService;
import vn.hoangminh.erp.common.exception.LoiNghiepVuException;
import vn.hoangminh.erp.common.permission.QuyenService;
import vn.hoangminh.erp.common.permission.dto.QuyenCuaToiResponse;
import vn.hoangminh.erp.common.security.domain.TaiKhoan;
import vn.hoangminh.erp.common.security.dto.DangNhapRequest;
import vn.hoangminh.erp.common.security.dto.DangNhapResponse;
import org.springframework.security.authentication.BadCredentialsException;

/**
 * Đăng nhập / phiên làm việc trên bảng dùng chung {@code TaiKhoan} (nhiệm vụ 0.4).
 *
 * <p>Ba trường hợp bị từ chối: không tìm thấy tài khoản, sai mật khẩu (cùng một thông báo
 * để không lộ tên đăng nhập tồn tại), và {@code trangThai} khác "Hoạt động".
 */
@Service
public class AuthService {

  private final TaiKhoanRepository taiKhoanRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final QuyenService quyenService;
  private final NhanVienQueryService nhanVienQueryService;

  public AuthService(
      TaiKhoanRepository taiKhoanRepository,
      PasswordEncoder passwordEncoder,
      JwtService jwtService,
      QuyenService quyenService,
      NhanVienQueryService nhanVienQueryService) {
    this.taiKhoanRepository = taiKhoanRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
    this.quyenService = quyenService;
    this.nhanVienQueryService = nhanVienQueryService;
  }

  @Transactional(readOnly = true)
  public DangNhapResponse dangNhap(DangNhapRequest request) {
    String tenDangNhap = request.tenDangNhap() == null ? "" : request.tenDangNhap().trim();

    TaiKhoan tk =
        taiKhoanRepository
            .findByTenDangNhap(tenDangNhap)
            .orElseThrow(() -> new BadCredentialsException("auth.dangnhap.sai"));

    if (!passwordEncoder.matches(request.matKhau(), tk.getMatKhau())) {
      throw new BadCredentialsException("auth.dangnhap.sai");
    }

    if (!tk.conHoatDong()) {
      throw LoiNghiepVuException.tuChoiQuyen("auth.dangnhap.taikhoan.khonghoatdong");
    }

    String hoTen =
        nhanVienQueryService.tenNhanVien(tk.getMaNV()).orElse(tk.getTenDangNhap());
    NguoiDungHienTai nguoiDung = NguoiDungHienTai.tu(tk, hoTen);

    return new DangNhapResponse(
        jwtService.taoToken(nguoiDung),
        "Bearer",
        jwtService.expirationMinutes(),
        quyenService.quyenCuaToi(nguoiDung));
  }

  /** Quyền của người dùng đang đăng nhập — {@code GET /api/quyen/cua-toi}. */
  @Transactional(readOnly = true)
  public QuyenCuaToiResponse quyenHienTai() {
    return quyenService.quyenCuaToi(NguoiDungHienTai.hienTai());
  }
}
