package vn.hoangminh.erp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import vn.hoangminh.erp.common.donhang.DonHangStateService;
import vn.hoangminh.erp.common.exception.LoiNghiepVuException;
import vn.hoangminh.erp.common.permission.VaiTro;
import vn.hoangminh.erp.common.security.NguoiDungHienTai;

/** Phase 6: kiểm tra vòng đời liên phân hệ và quyền sở hữu trạng thái. */
@SpringBootTest @AutoConfigureMockMvc @org.junit.jupiter.api.Tag("integration")
class Phase6IntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired JdbcTemplate jdbc;

  private RequestPostProcessor as(String username) {
    Map<String,Object> r = jdbc.queryForMap("SELECT maTaiKhoan,maNV,vaiTro FROM TaiKhoan WHERE tenDangNhap=?", username);
    var n = new NguoiDungHienTai((String)r.get("maTaiKhoan"),(String)r.get("maNV"),username,username,VaiTro.valueOf((String)r.get("vaiTro")));
    return authentication(new UsernamePasswordAuthenticationToken(n,null,n.authorities()));
  }

  @Test void stateMachineLienPhanHeChanSaiChuSoHuu() {
    DonHangStateService.kiemTra("HT2", "Đã tạo", "Đã nhập kho");
    DonHangStateService.kiemTra("HT3", "Đã nhập kho", "Đang giao");
    DonHangStateService.kiemTra("HT3", "Đang giao", "Đã giao");
    DonHangStateService.kiemTra("HT5", "Đã giao", "Đã đối soát");
    org.assertj.core.api.Assertions.assertThatThrownBy(() -> DonHangStateService.kiemTra("HT1", "Đang giao", "Đã giao"))
        .isInstanceOf(LoiNghiepVuException.class)
        .satisfies(x -> assertThat(((LoiNghiepVuException)x).getStatus().value()).isEqualTo(403));
  }

  @Test void apiHT5TheoMaTranQuyen() throws Exception {
    mvc.perform(get("/api/ht5/giao-dich").with(as("ktv"))).andExpect(status().isOk());
    mvc.perform(get("/api/ht5/giao-dich").with(as("nvkd"))).andExpect(status().isForbidden());
    mvc.perform(get("/api/ht5/giao-dich")).andExpect(status().isUnauthorized());
  }
}
