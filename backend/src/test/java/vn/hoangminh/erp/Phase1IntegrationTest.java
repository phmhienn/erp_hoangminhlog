package vn.hoangminh.erp;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import vn.hoangminh.erp.common.security.NguoiDungHienTai;
import vn.hoangminh.erp.common.permission.VaiTro;
import java.util.*;

@SpringBootTest @AutoConfigureMockMvc @Tag("integration") @Transactional
class Phase1IntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired JdbcTemplate jdbc;
  @Autowired ObjectMapper json;
  RequestPostProcessor as(String username) {
    var r=jdbc.queryForMap("SELECT maTaiKhoan,maNV,vaiTro FROM TaiKhoan WHERE tenDangNhap=?",username);
    var n=new NguoiDungHienTai((String)r.get("maTaiKhoan"),(String)r.get("maNV"),username,username,VaiTro.valueOf((String)r.get("vaiTro")));
    return authentication(new UsernamePasswordAuthenticationToken(n,null,n.authorities()));
  }
  String input() throws Exception {
    String kh=jdbc.queryForObject("SELECT MIN(maKhachHang) FROM KhachHang",String.class);
    String dv=jdbc.queryForObject("SELECT MIN(maDichVu) FROM DichVu",String.class);
    return json.writeValueAsString(Map.ofEntries(Map.entry("maKhachHang",kh),Map.entry("maDichVu",dv),Map.entry("nguoiGui","Người gửi test"),Map.entry("nguoiNhan","Người nhận test"),Map.entry("sdtNguoiGui","0900000001"),Map.entry("sdtNguoiNhan","0900000002"),Map.entry("diachiLayHang","Hà Nội"),Map.entry("diachiGiaoHang","Đà Nẵng"),Map.entry("khoiLuong",2),Map.entry("tienCOD",100000),Map.entry("phiVanChuyen",30000),Map.entry("hangHoa",List.of(Map.of("loaiHangHoa","Kiện test","soLuong",2,"trongLuong",2)))));
  }
  @Test void taoTuChoiSuaDuyetHuyVaLichSu() throws Exception {
    var body=input();
    var created=mvc.perform(post("/api/ht1/don-hang").with(as("nvkd")).contentType("application/json").content(body)).andExpect(status().isOk()).andExpect(jsonPath("$.trangThai").value("Đã tạo")).andReturn().getResponse().getContentAsString();
    String id=json.readTree(created).get("maDonHang").asText();String url="/api/ht1/don-hang/"+id;
    mvc.perform(post(url+"/duyet").with(as("nvkd"))).andExpect(status().isForbidden());
    mvc.perform(post(url+"/gui-duyet").with(as("nvkd"))).andExpect(status().isOk()).andExpect(jsonPath("$.kiemDuyet").value("Chờ duyệt"));
    mvc.perform(put(url).with(as("nvkd")).contentType("application/json").content(body)).andExpect(status().isConflict());
    mvc.perform(post(url+"/tu-choi").with(as("qlkd")).contentType("application/json").content("{\"lyDo\":\"Bổ sung thông tin\"}")).andExpect(status().isOk()).andExpect(jsonPath("$.kiemDuyet").value("Cần bổ sung"));
    mvc.perform(put(url).with(as("nvkd")).contentType("application/json").content(body)).andExpect(status().isOk());
    mvc.perform(post(url+"/gui-duyet").with(as("nvkd"))).andExpect(status().isOk());
    mvc.perform(post(url+"/duyet").with(as("qlkd"))).andExpect(status().isOk()).andExpect(jsonPath("$.kiemDuyet").value("Đã duyệt")).andExpect(jsonPath("$.trangThai").value("Đã tạo"));
    mvc.perform(post(url+"/duyet").with(as("qlkd"))).andExpect(status().isConflict());
    mvc.perform(post(url+"/huy").with(as("nvkd")).contentType("application/json").content("{\"lyDo\":\"Khách yêu cầu hủy\"}")).andExpect(status().isOk()).andExpect(jsonPath("$.trangThai").value("Đã hủy"));
    mvc.perform(get(url+"/lich-su").with(as("nvkd"))).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(7));
    assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM HangHoa WHERE maDonHang=?",Integer.class,id)).isEqualTo(1);
  }
  @Test void apiPhanQuyenVaTieuChi() throws Exception {
    mvc.perform(get("/api/ht1/don-hang")).andExpect(status().isUnauthorized());
    for(String u:List.of("nvkho","nvdp","nvgh","nvns","ktv","thuquy","ktt","nvthuong")) mvc.perform(get("/api/ht1/don-hang").with(as(u))).andExpect(status().isForbidden());
    mvc.perform(get("/api/ht1/bao-cao").with(as("nvkd"))).andExpect(status().isForbidden());
    mvc.perform(get("/api/ht1/bao-cao?tuNgay=2026-09-15&denNgay=2026-09-01").with(as("qlkd"))).andExpect(status().isBadRequest());
    mvc.perform(get("/api/ht1/bao-cao?tuNgay=sai").with(as("qlkd"))).andExpect(status().isBadRequest());
    mvc.perform(get("/api/ht1/bao-cao?tuNgay=2099-01-01").with(as("qlkd"))).andExpect(status().isOk()).andExpect(jsonPath("$.tongDon").value(0)).andExpect(jsonPath("$.thongBao").isNotEmpty());
    mvc.perform(post("/api/ht1/don-hang").with(as("nvkd")).contentType("application/json").content("{}")).andExpect(status().isBadRequest());
  }
  @Test void taoSuaTraCuuKhachHang() throws Exception {
    String body="{\"tenKhachHang\":\"Khách kiểm thử HT1\",\"soDienThoai\":\"0901234567\",\"diachi\":\"Đà Nẵng\",\"email\":\"ht1@example.test\",\"loaiKH\":\"Cá nhân\"}";
    var response=mvc.perform(post("/api/ht1/khach-hang").with(as("nvkd")).contentType("application/json").content(body)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
    var id=json.readTree(response).get("maKhachHang").asText();
    mvc.perform(put("/api/ht1/khach-hang/"+id).with(as("nvkd")).contentType("application/json").content(body.replace("Đà Nẵng","Hà Nội"))).andExpect(status().isOk()).andExpect(jsonPath("$.diachi").value("Hà Nội"));
    mvc.perform(get("/api/ht1/khach-hang").param("tuKhoa","ht1@example.test").with(as("nvkd"))).andExpect(status().isOk()).andExpect(jsonPath("$[0].maKhachHang").value(id));
    mvc.perform(post("/api/ht1/khach-hang").with(as("nvkd")).contentType("application/json").content(body.replace("0901234567","090123456789"))).andExpect(status().isBadRequest());
  }
}
