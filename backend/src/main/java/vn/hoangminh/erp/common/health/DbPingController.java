package vn.hoangminh.erp.common.health;

import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DbPingController {
  private final DataSource dataSource;

  public DbPingController(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @GetMapping("/api/ping/db")
  public ResponseEntity<DbPingResponse> ping() {
    try (var connection = dataSource.getConnection();
        var statement = connection.createStatement()) {
      statement.setQueryTimeout(5);
      try (var result = statement.executeQuery("SELECT 1")) {
        if (result.next() && result.getInt(1) == 1) {
          return ResponseEntity.ok().cacheControl(CacheControl.noStore())
              .body(new DbPingResponse("UP"));
        }
      }
    } catch (SQLException ignored) {
      // Không trả thông tin kết nối hoặc lỗi SQL nội bộ qua endpoint công khai.
    }
    return ResponseEntity.status(503).cacheControl(CacheControl.noStore())
        .body(new DbPingResponse("DOWN"));
  }

  public record DbPingResponse(String status) {}
}
