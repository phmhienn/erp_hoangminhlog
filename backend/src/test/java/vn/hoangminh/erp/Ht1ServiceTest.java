package vn.hoangminh.erp;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.springframework.context.MessageSource;
import vn.hoangminh.erp.Ht1DonHang.domain.DonHang;
import vn.hoangminh.erp.Ht1DonHang.dto.Ht1Dto.*;
import vn.hoangminh.erp.Ht1DonHang.repository.*;
import vn.hoangminh.erp.Ht1DonHang.service.Ht1Service;
import vn.hoangminh.erp.common.audit.LichSuDonHangService;
import vn.hoangminh.erp.common.donhang.DonHangStateService;
import vn.hoangminh.erp.common.exception.LoiNghiepVuException;
import vn.hoangminh.erp.common.permission.*;
import vn.hoangminh.erp.common.security.NguoiDungHienTai;
import vn.hoangminh.erp.common.sequence.*;
import java.util.*;
import java.time.LocalDate;

@ExtendWith(MockitoExtension.class)
class Ht1ServiceTest {
  @Mock KhachHangRepository kh;
  @Mock DichVuRepository dv;
  @Mock DonHangRepository dh;
  @Mock HangHoaRepository hh;
  @Mock LichSuDonHangService ls;
  @Mock DonHangStateService state;
  @Mock MaChungTuService ma;
  @Mock TaoChungTuTransaction tx;
  @Mock MessageSource messages;
  @Mock jakarta.persistence.EntityManager entityManager;
  Ht1Service service;
  private final Validator validator=Validation.buildDefaultValidatorFactory().getValidator();
  @BeforeEach void setup() { service=new Ht1Service(kh,dv,dh,hh,ls,state,ma,tx,new QuyenService(messages,null),validator,messages,entityManager); login(VaiTro.QL_KINH_DOANH); }
  @AfterEach void clear() { SecurityContextHolder.clearContext(); }
  void login(VaiTro role) { var n=new NguoiDungHienTai("TA_TEST","NV_TEST","test","Test",role); SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(n,null,n.authorities())); }
  DonHang don(String status) { var d=new DonHang();d.setMaDonHang("DH0000001");d.setTrangThai(status);when(dh.khoa(d.getMaDonHang())).thenReturn(Optional.of(d));return d; }
  void status(Runnable work,int expected) { assertThatThrownBy(work::run).isInstanceOfSatisfying(LoiNghiepVuException.class,e->assertThat(e.getStatus().value()).isEqualTo(expected)); }
  @Test void nhanVienKhongDuocDuyet() { login(VaiTro.NV_KINH_DOANH);status(()->service.xuLy("DH0000001","duyet",null),403);verifyNoInteractions(dh); }
  @Test void cacHeKhacKhongDocDon() { for(var role:VaiTro.values()) if(!role.heThong().contains("HT1")) {login(role);status(()->service.danhSach(null,null,null,null),403);}verifyNoInteractions(dh); }
  @Test void nhanVienChiXuLyDonMinhTao() { login(VaiTro.NV_KINH_DOANH);don("Đã tạo");when(ls.doc(anyString())).thenReturn(List.of());status(()->service.xuLy("DH0000001","huy","Khách yêu cầu"),403);verifyNoInteractions(state); }
  @Test void khongDuyetTruocKhiGui() { don("Đã tạo");when(ls.kiemDuyet(anyString())).thenReturn("Chưa gửi");status(()->service.xuLy("DH0000001","duyet",null),409);verify(ls,never()).ghi(any(),any(),any(),any(),any()); }
  @Test void khongGuiLapLai() { don("Đã tạo");when(ls.kiemDuyet(anyString())).thenReturn("Chờ duyệt");status(()->service.xuLy("DH0000001","gui-duyet",null),409); }
  @Test void tuChoiCanLyDo() { don("Đã tạo");status(()->service.xuLy("DH0000001","tu-choi"," "),400); }
  @Test void tuChoiGhiNguoiVaLyDo() { var d=don("Đã tạo");when(ls.kiemDuyet(anyString())).thenReturn("Chờ duyệt");service.xuLy(d.getMaDonHang(),"tu-choi","Bổ sung địa chỉ");verify(ls).ghi(d.getMaDonHang(),"TuChoi","Đã tạo","TA_TEST","Bổ sung địa chỉ"); }
  @Test void duyetKhongDoiTrangThaiVanChuyen() { var d=don("Đã tạo");when(ls.kiemDuyet(anyString())).thenReturn("Chờ duyệt");service.xuLy(d.getMaDonHang(),"duyet",null);assertThat(d.getTrangThai()).isEqualTo("Đã tạo");verify(ls).ghi(d.getMaDonHang(),"Duyet","Đã tạo","TA_TEST",null); }
  @Test void baoCaoNgayNguoc() { status(()->service.baoCao(LocalDate.of(2026,9,15),LocalDate.of(2026,9,1),null,null),400); }
  @Test void kiemTraDoDaiVaEmail() { assertThat(validator.validate(new KhachHangInput("KH","01234567890","a","sai-email",""))).hasSize(2); }
}
