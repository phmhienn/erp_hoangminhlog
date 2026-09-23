package vn.hoangminh.erp.Ht2Kho;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import jakarta.persistence.EntityManager;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Supplier;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import vn.hoangminh.erp.Ht1DonHang.domain.DonHang;
import vn.hoangminh.erp.Ht1DonHang.repository.*;
import vn.hoangminh.erp.Ht2Kho.domain.*;
import vn.hoangminh.erp.Ht2Kho.dto.Ht2Dto.*;
import vn.hoangminh.erp.Ht2Kho.repository.*;
import vn.hoangminh.erp.Ht2Kho.service.Ht2Service;
import vn.hoangminh.erp.common.audit.LichSuDonHangService;
import vn.hoangminh.erp.common.donhang.DonHangStateService;
import vn.hoangminh.erp.common.exception.LoiNghiepVuException;
import vn.hoangminh.erp.common.permission.*;
import vn.hoangminh.erp.common.security.NguoiDungHienTai;
import vn.hoangminh.erp.common.sequence.*;

@ExtendWith(MockitoExtension.class)
class Ht2WorkflowTest {
  @Mock PhieuNhapRepository nhap;
  @Mock PhieuXuatRepository xuat;
  @Mock PhieuKiemKeRepository kk;
  @Mock KiemKeDonHangRepository kkDon;
  @Mock BienBanSuCoRepository bienBan;
  @Mock DonHangRepository don;
  @Mock HangHoaRepository hang;
  @Mock KhachHangRepository khach;
  @Mock LichSuDonHangService lichSu;
  @Mock DonHangStateService state;
  @Mock MaChungTuService ma;
  @Mock TaoChungTuTransaction transaction;
  @Mock QuyenService quyen;
  @Mock EntityManager em;
  private final jakarta.validation.ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
  @Spy Validator validator = factory.getValidator();
  @InjectMocks Ht2Service service;

  @BeforeEach void login() {
    var n = new NguoiDungHienTai("TA1", "NV1", "kho", "Kho", VaiTro.QL_KHO);
    SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(n, null, n.authorities()));
  }
  @AfterEach void cleanup() { SecurityContextHolder.clearContext(); factory.close(); }
  private DonHang order(String id, String status) {
    var d = new DonHang(); d.setMaDonHang(id); d.setTrangThai(status); return d;
  }
  private void executeTransaction() {
    when(transaction.thucHien(any())).thenAnswer(i -> ((Supplier<?>) i.getArgument(0)).get());
  }
  private PhieuXuat issue(String status) {
    var p = new PhieuXuat(); p.setMaPhieuXuat("PX1"); p.setMaDonHang("DH1"); p.setMaNV("NV1"); p.setTrangThai(status); return p;
  }

  @Test void receiptNeedsOnlyApprovedOrderAndNeverCreatesProductStock() {
    var d = order("DH1", "Đã tạo"); when(don.khoa("DH1")).thenReturn(Optional.of(d));
    when(lichSu.kiemDuyet("DH1")).thenReturn("Đã duyệt");
    when(ma.capMa(LoaiChungTu.PHIEU_NHAP)).thenReturn("PN1"); executeTransaction();
    assertThat(service.taoNhap(new NhapInput("DH1", LocalDate.now(), "Nguyên kiện")).maDonHang()).isEqualTo("DH1");
    var persisted = ArgumentCaptor.forClass(Object.class); verify(em).persist(persisted.capture());
    assertThat(persisted.getValue()).isInstanceOf(PhieuNhap.class);
    var p = (PhieuNhap) persisted.getValue(); assertThat(p.getMaNCC()).isNull(); assertThat(p.getTongTien()).isNull();
  }
  @Test void duplicateReceiptIsRejectedBeforeAnyWrite() {
    when(don.khoa("DH1")).thenReturn(Optional.of(order("DH1", "Đã tạo")));
    when(lichSu.kiemDuyet("DH1")).thenReturn("Đã duyệt");
    var p = new PhieuNhap(); p.setMaPhieuNhap("PN1"); p.setMaDonHang("DH1"); p.setTrangThai("Chờ duyệt"); when(nhap.findAll()).thenReturn(List.of(p));
    assertThatThrownBy(() -> service.taoNhap(new NhapInput("DH1", LocalDate.now(), null))).hasMessage("ht2.dontrungphieu"); verifyNoInteractions(em);
  }
  @Test void approvalMovesTheOrderIntoWarehouseOnlyOnce() {
    var d = order("DH1", "Đã tạo"); when(don.khoa("DH1")).thenReturn(Optional.of(d)); when(lichSu.kiemDuyet("DH1")).thenReturn("Đã duyệt");
    var p = new PhieuNhap(); p.setMaPhieuNhap("PN1"); p.setMaDonHang("DH1"); p.setTrangThai("Chờ duyệt"); when(nhap.khoa("PN1")).thenReturn(Optional.of(p));
    service.duyet("PN1"); assertThat(p.getTrangThai()).isEqualTo("Đã duyệt");
    verify(state).doiTrangThai(eq("HT2"), eq("DH1"), eq("Đã nhập kho"), any(), anyString());
    assertThatThrownBy(() -> service.duyet("PN1")).hasMessage("ht2.chopheduyet"); verifyNoInteractions(em);
  }
  @Test void stockContainsOrdersNotYetHandedOver() {
    when(quyen.coChucNang(any(), any())).thenReturn(true);
    when(don.findAll()).thenReturn(List.of(order("DH1", "Đã nhập kho"), order("DH2", "Đã nhập kho"), order("DH3", "Đang giao")));
    when(xuat.existsByMaDonHangAndTrangThai(anyString(), eq("Đã xuất")))
        .thenAnswer(i -> "DH2".equals(i.getArgument(0)));
    assertThat(service.tonKho()).extracting(DonKhoView::maDonHang).containsExactly("DH1");
  }
  @Test void exportRequiresAnOrderAndRejectsAlreadyExportedOrder() {
    assertThatThrownBy(() -> service.taoXuat(new XuatInput(null, LocalDate.now(), null, null))).isInstanceOf(LoiNghiepVuException.class);
    when(don.khoa("DH1")).thenReturn(Optional.of(order("DH1", "Đã nhập kho")));
    when(xuat.existsByMaDonHangAndTrangThai("DH1", "Đã xuất")).thenReturn(true);
    assertThatThrownBy(() -> service.taoXuat(new XuatInput("DH1", LocalDate.now(), null, null))).hasMessage("ht2.donchuadanhap"); verifyNoInteractions(em);
  }
  @Test void duplicateExportIsRejected() {
    when(don.khoa("DH1")).thenReturn(Optional.of(order("DH1", "Đã nhập kho"))); when(xuat.findAll()).thenReturn(List.of(issue("Lưu tạm")));
    assertThatThrownBy(() -> service.taoXuat(new XuatInput("DH1", LocalDate.now(), null, null))).hasMessage("ht2.dontrungphieu");
  }
  @Test void exportApprovalHandsOverWholeOrderWithoutProductQuantities() {
    var p = issue("Chờ duyệt"); when(xuat.khoa("PX1")).thenReturn(Optional.of(p)); when(don.khoa("DH1")).thenReturn(Optional.of(order("DH1", "Đã nhập kho")));
    service.duyetXuat("PX1"); assertThat(p.getTrangThai()).isEqualTo("Đã xuất"); assertThat(p.getNguoiDuyet()).isEqualTo("NV1");
    verify(lichSu).ghi(eq("DH1"), eq("XuatKho"), eq("Đã xuất"), eq("TA1"), anyString()); verifyNoInteractions(em, state);
    assertThatThrownBy(() -> service.duyetXuat("PX1")).hasMessage("ht2.chopheduyet");
  }
  @Test void missingOrderCountIsRecordedWithoutChangingStock() {
    when(don.khoa("DH1")).thenReturn(Optional.of(order("DH1", "Đã nhập kho"))); when(ma.capMa(LoaiChungTu.PHIEU_KIEM_KE)).thenReturn("KK1"); executeTransaction();
    service.taoKiemKe(new KiemKeInput(LocalDate.now(), "A", null, List.of(new KiemKeChiTiet("DH1", 0, "Không tìm thấy"))));
    var persisted = ArgumentCaptor.forClass(Object.class); verify(em, times(2)).persist(persisted.capture());
    var detail = (KiemKeDonHang) persisted.getAllValues().get(1);
    assertThat(detail.getId().getMaDonHang()).isEqualTo("DH1"); assertThat(detail.getSoLuongHeThong()).isEqualTo(1); assertThat(detail.getChenhLech()).isEqualTo(-1); verifyNoInteractions(state);
  }
  @Test void cannotChangeOrderOnExistingReceipt() {
    var p = new PhieuNhap(); p.setMaDonHang("DH1"); p.setMaNV("NV1"); p.setTrangThai("Lưu tạm"); when(nhap.khoa("PN1")).thenReturn(Optional.of(p));
    assertThatThrownBy(() -> service.capNhatNhap("PN1", new NhapInput("DH2", LocalDate.now(), null))).hasMessage("ht2.khongdoidon"); verifyNoInteractions(don);
  }
  @Test void staffCannotEditAnotherStaffsReceipt() {
    var p = new PhieuNhap(); p.setMaDonHang("DH1"); p.setMaNV("NV2"); p.setTrangThai("Lưu tạm"); when(nhap.khoa("PN1")).thenReturn(Optional.of(p));
    assertThatThrownBy(() -> service.capNhatNhap("PN1", new NhapInput("DH1", LocalDate.now(), null))).hasMessage("quyen.tuchoi");
  }
}
