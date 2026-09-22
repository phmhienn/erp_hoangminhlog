package vn.hoangminh.erp.common.health;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class DbPingControllerTest {
  @Test
  void returnsUpAfterSuccessfulQueryAndClosesResources() throws Exception {
    var source = mock(DataSource.class);
    var connection = mock(Connection.class);
    var statement = mock(Statement.class);
    var result = mock(ResultSet.class);
    when(source.getConnection()).thenReturn(connection);
    when(connection.createStatement()).thenReturn(statement);
    when(statement.executeQuery("SELECT 1")).thenReturn(result);
    when(result.next()).thenReturn(true);
    when(result.getInt(1)).thenReturn(1);
    MockMvcBuilders.standaloneSetup(new DbPingController(source)).build()
        .perform(get("/api/ping/db"))
        .andExpect(status().isOk())
        .andExpect(header().string("Cache-Control", "no-store"))
        .andExpect(content().json("{\"status\":\"UP\"}"));
    verify(statement).setQueryTimeout(5);
    verify(result).close();
    verify(statement).close();
    verify(connection).close();
  }

  @Test
  void returnsUnavailableWithoutLeakingConnectionError() throws Exception {
    var source = mock(DataSource.class);
    when(source.getConnection()).thenThrow(new SQLException("private connection details"));
    MockMvcBuilders.standaloneSetup(new DbPingController(source)).build()
        .perform(get("/api/ping/db"))
        .andExpect(status().isServiceUnavailable())
        .andExpect(header().string("Cache-Control", "no-store"))
        .andExpect(content().string("{\"status\":\"DOWN\"}"));
  }
}
