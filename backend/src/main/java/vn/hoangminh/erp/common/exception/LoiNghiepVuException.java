package vn.hoangminh.erp.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Ngoại lệ luật nghiệp vụ. Mang theo <b>mã thông báo</b> trong {@code messages_vi.properties}
 * để câu chữ trả về cho người dùng là nguyên văn đặc tả, không hard-code rải rác (mục 4 Backend).
 *
 * <p>Mã HTTP theo quy ước thống nhất: 400 dữ liệu không hợp lệ, 403 ô "–" của ma trận phân quyền,
 * 409 xung đột trạng thái chứng từ.
 */
public class LoiNghiepVuException extends RuntimeException {

  private final String maLoi;
  private final transient Object[] args;
  private final HttpStatus status;

  public LoiNghiepVuException(String maLoi, HttpStatus status, Object... args) {
    super(maLoi);
    this.maLoi = maLoi;
    this.args = args;
    this.status = status;
  }

  /** Dữ liệu không hợp lệ — HTTP 400. */
  public static LoiNghiepVuException duLieu(String maLoi, Object... args) {
    return new LoiNghiepVuException(maLoi, HttpStatus.BAD_REQUEST, args);
  }

  /** Không có quyền (ô "–" của ma trận PHỤ LỤC B) — HTTP 403. */
  public static LoiNghiepVuException tuChoiQuyen(String maLoi, Object... args) {
    return new LoiNghiepVuException(maLoi, HttpStatus.FORBIDDEN, args);
  }

  /** Xung đột trạng thái: đơn đã duyệt, phiếu đã nhận… — HTTP 409. */
  public static LoiNghiepVuException xungDot(String maLoi, Object... args) {
    return new LoiNghiepVuException(maLoi, HttpStatus.CONFLICT, args);
  }

  /** Không tìm thấy dữ liệu — HTTP 404. */
  public static LoiNghiepVuException khongTimThay(String maLoi, Object... args) {
    return new LoiNghiepVuException(maLoi, HttpStatus.NOT_FOUND, args);
  }

  /** Chưa đăng nhập / phiên hết hạn — HTTP 401. */
  public static LoiNghiepVuException chuaXacThuc(String maLoi, Object... args) {
    return new LoiNghiepVuException(maLoi, HttpStatus.UNAUTHORIZED, args);
  }

  public String getMaLoi() {
    return maLoi;
  }

  public Object[] getArgs() {
    return args;
  }

  public HttpStatus getStatus() {
    return status;
  }
}
