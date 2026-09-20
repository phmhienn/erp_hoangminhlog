package vn.hoangminh.erp.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import javax.crypto.SecretKey;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import vn.hoangminh.erp.common.config.AppProperties;
import vn.hoangminh.erp.common.permission.VaiTro;

/**
 * Ký và đọc JWT của phiên làm việc (nhiệm vụ 0.4). Không lưu phiên phía server;
 * mỗi request bộ lọc đọc token rồi đối chiếu lại {@code TaiKhoan.trangThai} trong CSDL
 * để tài khoản bị khoá lập tức mất hiệu lực.
 */
@Service
public class JwtService {

  private final SecretKey key;
  private final long expirationMinutes;
  private final String issuer;

  public JwtService(AppProperties props) {
    byte[] secret = props.jwt().secret().getBytes(StandardCharsets.UTF_8);
    if (secret.length < 32) {
      throw new IllegalStateException("erp.jwt.secret phai dai toi thieu 32 byte cho HS256");
    }
    this.key = Keys.hmacShaKeyFor(secret);
    this.expirationMinutes = props.jwt().expirationMinutes();
    this.issuer = props.jwt().issuer();
  }

  public String taoToken(NguoiDungHienTai nd) {
    Instant now = Instant.now();
    return Jwts.builder()
        .subject(nd.tenDangNhap())
        .issuer(issuer)
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plus(expirationMinutes, ChronoUnit.MINUTES)))
        .claim("maTaiKhoan", nd.maTaiKhoan())
        .claim("maNV", nd.maNV())
        .claim("hoTen", nd.hoTen())
        .claim("vaiTro", nd.vaiTro().name())
        .signWith(key)
        .compact();
  }

  /** Đọc token; token sai chữ ký/hết hạn → {@link io.jsonwebtoken.JwtException}. */
  public NguoiDungHienTai docToken(String token) {
    Claims c =
        Jwts.parser()
            .verifyWith(key)
            .requireIssuer(issuer)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    return new NguoiDungHienTai(
        c.get("maTaiKhoan", String.class),
        c.get("maNV", String.class),
        c.getSubject(),
        c.get("hoTen", String.class),
        VaiTro.tu(c.get("vaiTro", String.class)));
  }

  public long expirationMinutes() {
    return expirationMinutes;
  }
}
