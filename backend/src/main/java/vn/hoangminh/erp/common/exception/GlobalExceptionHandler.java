package vn.hoangminh.erp.common.exception;

import jakarta.validation.ConstraintViolationException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Dịch mọi ngoại lệ về một dạng JSON duy nhất {@link ApiError} (mục 4 Backend).
 * Câu chữ lấy từ {@code messages_vi.properties}, không hard-code trong code.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  private final MessageSource messages;

  public GlobalExceptionHandler(MessageSource messages) {
    this.messages = messages;
  }

  @ExceptionHandler(LoiNghiepVuException.class)
  public ResponseEntity<ApiError> xuLyLoiNghiepVu(LoiNghiepVuException ex) {
    return ResponseEntity.status(ex.getStatus()).body(toApiError(ex.getMaLoi(), ex.getArgs(), null));
  }

  /** Bean Validation lớp 1: định dạng, độ dài theo PHỤ LỤC A. */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> xuLyLoiValid(MethodArgumentNotValidException ex) {
    Map<String, String> chiTiet = new LinkedHashMap<>();
    for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
      chiTiet.putIfAbsent(fe.getField(), fe.getDefaultMessage());
    }
    return ResponseEntity.badRequest()
        .body(toApiError("loi.dulieu.khonghople", null, chiTiet));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ApiError> xuLyLoiConstraint(ConstraintViolationException ex) {
    Map<String, String> chiTiet = new LinkedHashMap<>();
    ex.getConstraintViolations()
        .forEach(v -> chiTiet.putIfAbsent(v.getPropertyPath().toString(), v.getMessage()));
    return ResponseEntity.badRequest()
        .body(toApiError("loi.dulieu.khonghople", null, chiTiet));
  }

  /** Ô "–" của ma trận phân quyền PHỤ LỤC B — HTTP 403. */
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiError> xuLyTuChoiQuyen(AccessDeniedException ex) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(toApiError("quyen.tuchoi", null, null));
  }

  /** Sai tên đăng nhập/mật khẩu, tài khoản bị khoá — HTTP 401. */
  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ApiError> xuLyLoiXacThuc(AuthenticationException ex) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(toApiError("auth.dangnhap.sai", null, null));
  }

  /** Vi phạm ràng buộc CSDL (PK/FK/UNIQUE/CHECK) — xung đột, HTTP 409. */
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ApiError> xuLyLoiToanVen(DataIntegrityViolationException ex) {
    log.warn("Vi pham rang buoc CSDL: {}", ex.getMostSpecificCause().getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(toApiError("loi.trangthai.xungdot", null, ex.getMostSpecificCause().getMessage()));
  }

  @ExceptionHandler({org.springframework.http.converter.HttpMessageNotReadableException.class,
      org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class})
  public ResponseEntity<ApiError> xuLySaiDinhDang(Exception ex) {
    return ResponseEntity.badRequest().body(toApiError("loi.dulieu.khonghople", null, null));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> xuLyLoiHeThong(Exception ex) {
    log.error("Lỗi hệ thống chưa xử lý", ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(toApiError("loi.hethong", null, ex.getMessage()));
  }

  private ApiError toApiError(String maLoi, Object[] args, Object chiTiet) {
    Locale locale = Locale.forLanguageTag("vi-VN");
    String thongBao;
    try {
      thongBao = messages.getMessage(maLoi, args, locale);
    } catch (Exception e) {
      thongBao = maLoi;
    }
    return new ApiError(maLoi, thongBao, chiTiet);
  }
}
