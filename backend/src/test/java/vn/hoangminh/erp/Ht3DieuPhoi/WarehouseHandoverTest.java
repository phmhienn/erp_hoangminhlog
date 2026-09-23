package vn.hoangminh.erp.Ht3DieuPhoi;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import vn.hoangminh.erp.Ht1DonHang.domain.DonHang;
import vn.hoangminh.erp.Ht1DonHang.repository.DonHangRepository;
import vn.hoangminh.erp.Ht2Kho.repository.PhieuXuatRepository;
import vn.hoangminh.erp.Ht3DieuPhoi.domain.ChuyenVan;
import vn.hoangminh.erp.Ht3DieuPhoi.repository.*;
import vn.hoangminh.erp.Ht3DieuPhoi.service.Ht3Service;
import vn.hoangminh.erp.Ht4NhanSu.domain.TaiXe;
import vn.hoangminh.erp.Ht4NhanSu.repository.*;
import vn.hoangminh.erp.common.donhang.DonHangStateService;
import vn.hoangminh.erp.common.permission.*;
import vn.hoangminh.erp.common.security.NguoiDungHienTai;

@ExtendWith(MockitoExtension.class)
class WarehouseHandoverTest {
  @Mock ChuyenVanRepository chuyen;
  @Mock ChiTietChuyenHangRepository chiTiet;
  @Mock PhieuXuatRepository xuat;
  @Mock DonHangRepository don;
  @Mock TaiXeRepository taiXe;
  @Mock NhanVienRepository nhanVien;
  @Mock LoTrinhRepository loTrinh;
  @Mock QuyenService quyen;
  @Mock DonHangStateService state;
  @InjectMocks Ht3Service service;
  private ChuyenVan trip;
  @BeforeEach void setup() {
    var n = new NguoiDungHienTai("TA1", "NV1", "tx", "Tài xế", VaiTro.NV_GIAO_HANG);
    SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(n, null, n.authorities()));
    trip = new ChuyenVan(); trip.setMaChuyen(1); trip.setMaDonHang("DH1"); trip.setMaTaiXe("TX1"); trip.setTrangThai("Đã phê duyệt");
    when(chuyen.khoa(1)).thenReturn(Optional.of(trip));
    var tx = new TaiXe(); tx.setMaTaiXe("TX1"); tx.setMaNV("NV1"); when(taiXe.findAll()).thenReturn(List.of(tx));
    var d = new DonHang(); d.setMaDonHang("DH1"); d.setTrangThai("Đã nhập kho"); when(don.khoa("DH1")).thenReturn(Optional.of(d));
  }
  @AfterEach void cleanup() { SecurityContextHolder.clearContext(); }
  @Test void driverCannotCollectBeforeWarehouseApproval() {
    assertThatThrownBy(() -> service.nhanDon("1")).hasMessage("ht3.chuabangiaokho");
    assertThat(trip.getTrangThai()).isEqualTo("Đã phê duyệt"); verifyNoInteractions(state);
  }
  @Test void driverCanCollectAfterWarehouseApproval() {
    when(xuat.existsByMaDonHangAndTrangThai("DH1", "Đã xuất")).thenReturn(true);
    service.nhanDon("1");
    assertThat(trip.getTrangThai()).isEqualTo("Đang giao");
    verify(state).doiTrangThai(eq("HT3"), eq("DH1"), eq("Đang giao"), any(), anyString());
  }
}
