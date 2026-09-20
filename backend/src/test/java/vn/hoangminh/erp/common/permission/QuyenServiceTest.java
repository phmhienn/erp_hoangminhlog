package vn.hoangminh.erp.common.permission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.http.HttpStatus;
import vn.hoangminh.erp.Ht4NhanSu.service.NhanVienQueryService;
import vn.hoangminh.erp.common.exception.LoiNghiepVuException;

/**
 * Kiểm tra {@link QuyenService} khớp từng ô của ma trận PHỤ LỤC B (mục 4.3).
 * Test thuần, không cần CSDL.
 */
class QuyenServiceTest {

  private final QuyenService quyen =
      new QuyenService(new StaticMessageSource(), mock(NhanVienQueryService.class));

  /** 33 bảng vật lý của ERP_database.sql — mọi bảng phải thuộc đúng một dòng của ma trận. */
  private static final List<String> TAT_CA_BANG =
      List.of(
          "PhongBan", "ChucVu", "TrangThaiNhanVien", "NhanVien", "TaiKhoan", "TaiXe",
          "KhachHang", "DichVu", "DonHang", "SanPham", "HangHoa",
          "PHIEU_NHAP", "CHI_TIET_PHIEU_NHAP", "PHIEU_XUAT", "CHI_TIET_PHIEU_XUAT",
          "PHIEU_KIEM_KE", "CHI_TIET_KIEM_KE", "TON_KHO", "ChiPhiLuuKho", "BienBanSuCo",
          "PhuongTien", "ChuyenVan", "ChiTietChuyenHang", "LoTrinh", "SuCoVanTai",
          "MinhChungGiaoHang", "LichSuDonHang",
          "GiaoDichCOD", "PhieuThuChi", "SoQuy", "CongNo", "DoiSoatCOD", "ChiTietDoiSoat");

  @Test
  @DisplayName("33 bảng của lược đồ đều ánh xạ được vào một dòng của ma trận phân quyền")
  void moiBangDeuThuocMotNhomBang() {
    assertThat(TAT_CA_BANG).hasSize(33);
    assertThatCode(
            () -> TAT_CA_BANG.forEach(b -> assertThat(NhomBang.cuaBang(b)).isNotNull()))
        .doesNotThrowAnyException();
  }

  @Test
  @DisplayName("Dòng 'DonHang, HangHoa': HT1 = Đ/T/S + hủy mềm; HT2/HT3 = Đọc + Sửa(trangThai); HT4 = –")
  void quyenTrenDonHang() {
    assertThat(quyen.quyenTrenBang(VaiTro.NV_KINH_DOANH, NhomBang.DON_HANG_HANG_HOA))
        .containsExactlyInAnyOrder(
            HanhDong.DOC, HanhDong.THEM, HanhDong.SUA, HanhDong.HUY_MEM);

    assertThat(quyen.quyenTrenBang(VaiTro.NV_KHO, NhomBang.DON_HANG_HANG_HOA))
        .containsExactlyInAnyOrder(HanhDong.DOC, HanhDong.SUA_TRANG_THAI);

    assertThat(quyen.quyenTrenBang(VaiTro.NV_GIAO_HANG, NhomBang.DON_HANG_HANG_HOA))
        .containsExactlyInAnyOrder(HanhDong.DOC, HanhDong.SUA_TRANG_THAI);

    // HT5: Đọc/Sửa (trangThai, tienCOD)
    assertThat(quyen.quyenTrenBang(VaiTro.KE_TOAN_VIEN, NhomBang.DON_HANG_HANG_HOA))
        .containsExactlyInAnyOrder(
            HanhDong.DOC, HanhDong.SUA_TRANG_THAI, HanhDong.SUA_TIEN_COD);

    // Ô "–" của HT4
    assertThat(quyen.quyenTrenBang(VaiTro.NV_NHAN_SU, NhomBang.DON_HANG_HANG_HOA)).isEmpty();
  }

  @Test
  @DisplayName("Dòng kho: HT2 = Đ/T/S/X; HT5 = Đọc + Ghi ChiPhiLuuKho; HT1/HT3 chỉ Đọc")
  void quyenTrenBangKho() {
    assertThat(quyen.quyenTrenBang(VaiTro.QL_KHO, NhomBang.KHO))
        .containsExactlyInAnyOrder(
            HanhDong.DOC, HanhDong.THEM, HanhDong.SUA, HanhDong.XOA);

    assertThat(quyen.quyenTrenBang(VaiTro.THU_QUY, NhomBang.KHO))
        .containsExactlyInAnyOrder(HanhDong.DOC, HanhDong.GHI_CHI_PHI_LUU_KHO);

    assertThat(quyen.quyenTrenBang(VaiTro.NV_KINH_DOANH, NhomBang.KHO))
        .containsExactly(HanhDong.DOC);
    assertThat(quyen.quyenTrenBang(VaiTro.NV_DIEU_PHOI, NhomBang.KHO))
        .containsExactly(HanhDong.DOC);
    assertThat(quyen.quyenTrenBang(VaiTro.NV_NHAN_SU, NhomBang.KHO)).isEmpty();
  }

  @Test
  @DisplayName("Dòng điều phối: HT3 = Đ/T/S/X; HT1 = – (ô gạch)")
  void quyenTrenBangDieuPhoi() {
    assertThat(quyen.quyenTrenBang(VaiTro.QL_DIEU_PHOI, NhomBang.DIEU_PHOI))
        .containsExactlyInAnyOrder(
            HanhDong.DOC, HanhDong.THEM, HanhDong.SUA, HanhDong.XOA);
    assertThat(quyen.quyenTrenBang(VaiTro.NV_KINH_DOANH, NhomBang.DIEU_PHOI)).isEmpty();
    assertThat(quyen.quyenTrenBang(VaiTro.NV_KHO, NhomBang.DIEU_PHOI))
        .containsExactly(HanhDong.DOC);
  }

  @Test
  @DisplayName("Dòng nhân sự: HT4 = Đ/T/S/X, các hệ còn lại chỉ Đọc")
  void quyenTrenBangNhanSu() {
    assertThat(quyen.quyenTrenBang(VaiTro.QL_NHAN_SU, NhomBang.NHAN_SU))
        .containsExactlyInAnyOrder(
            HanhDong.DOC, HanhDong.THEM, HanhDong.SUA, HanhDong.XOA);
    assertThat(quyen.quyenTrenBang(VaiTro.NV_KINH_DOANH, NhomBang.NHAN_SU))
        .containsExactly(HanhDong.DOC);
    assertThat(quyen.quyenTrenBang(VaiTro.KE_TOAN_TRUONG, NhomBang.NHAN_SU))
        .containsExactly(HanhDong.DOC);
  }

  @Test
  @DisplayName("Dòng TaiXe: HT3 chỉ Sửa(trangThai); HT4 = Đ/T/S/X; HT2 = –")
  void quyenTrenTaiXe() {
    assertThat(quyen.quyenTrenBang(VaiTro.NV_GIAO_HANG, NhomBang.TAI_XE))
        .contains(HanhDong.SUA_TRANG_THAI)
        .doesNotContain(HanhDong.THEM, HanhDong.XOA);
    assertThat(quyen.quyenTrenBang(VaiTro.NV_NHAN_SU, NhomBang.TAI_XE))
        .containsExactlyInAnyOrder(
            HanhDong.DOC, HanhDong.THEM, HanhDong.SUA, HanhDong.XOA);
    assertThat(quyen.quyenTrenBang(VaiTro.NV_KHO, NhomBang.TAI_XE)).isEmpty();
  }

  @Test
  @DisplayName("Nhân viên thường không có quyền trên bảng nghiệp vụ nào (3.4.2)")
  void nhanVienThuongKhongCoQuyenBang() {
    for (NhomBang nhom : NhomBang.values()) {
      assertThat(quyen.quyenTrenBang(VaiTro.NHAN_VIEN, nhom))
          .as("nhóm bảng %s", nhom)
          .isEmpty();
    }
    // nhưng vẫn xem được hồ sơ của chính mình
    assertThat(quyen.coChucNang(VaiTro.NHAN_VIEN, ChucNang.HT4_XEM_HO_SO_CA_NHAN)).isTrue();
    assertThat(quyen.coChucNang(VaiTro.NHAN_VIEN, ChucNang.HT4_GUI_YEU_CAU_CAP_NHAT)).isTrue();
    assertThat(quyen.coChucNang(VaiTro.NHAN_VIEN, ChucNang.HT4_XEM_HO_SO)).isFalse();
  }

  @Test
  @DisplayName("Chỉ Quản lý kinh doanh được kiểm duyệt đơn và xem báo cáo đơn hàng (3.1.2/3.1.3)")
  void chucNangKiemDuyetVaBaoCao() {
    assertThat(quyen.coChucNang(VaiTro.QL_KINH_DOANH, ChucNang.HT1_KIEM_DUYET_DON_HANG)).isTrue();
    assertThat(quyen.coChucNang(VaiTro.NV_KINH_DOANH, ChucNang.HT1_KIEM_DUYET_DON_HANG))
        .isFalse();
    assertThat(quyen.coChucNang(VaiTro.QL_KINH_DOANH, ChucNang.HT1_XEM_BAO_CAO_DON_HANG))
        .isTrue();
    assertThat(quyen.coChucNang(VaiTro.NV_KINH_DOANH, ChucNang.HT1_XEM_BAO_CAO_DON_HANG))
        .isFalse();
    // Actor của usecase "Tạo đơn hàng" gồm cả Quản lý kinh doanh
    assertThat(quyen.coChucNang(VaiTro.QL_KINH_DOANH, ChucNang.HT1_TAO_DON_HANG)).isTrue();
  }

  @Test
  @DisplayName("Chỉ Quản lý kho duyệt phiếu nhập; chỉ Kế toán trưởng duyệt giao dịch; chỉ Thủ quỹ xác nhận")
  void chucNangPheDuyetCacHe() {
    assertThat(quyen.coChucNang(VaiTro.QL_KHO, ChucNang.HT2_PHE_DUYET_PHIEU_NHAP)).isTrue();
    assertThat(quyen.coChucNang(VaiTro.NV_KHO, ChucNang.HT2_PHE_DUYET_PHIEU_NHAP)).isFalse();

    assertThat(quyen.coChucNang(VaiTro.KE_TOAN_TRUONG, ChucNang.HT5_PHE_DUYET_GIAO_DICH))
        .isTrue();
    assertThat(quyen.coChucNang(VaiTro.KE_TOAN_VIEN, ChucNang.HT5_PHE_DUYET_GIAO_DICH)).isFalse();

    assertThat(quyen.coChucNang(VaiTro.THU_QUY, ChucNang.HT5_CAP_NHAT_TRANG_THAI_GD)).isTrue();
    assertThat(quyen.coChucNang(VaiTro.KE_TOAN_TRUONG, ChucNang.HT5_CAP_NHAT_TRANG_THAI_GD))
        .isFalse();
  }

  @Test
  @DisplayName("Vai trò chỉ thuộc đúng hệ thống của mình (mục 6 huong_di_lap_trinh)")
  void anhXaVaiTroHeThong() {
    assertThat(VaiTro.NV_KINH_DOANH.heThong()).containsExactly("HT1");
    assertThat(VaiTro.QL_KHO.heThong()).containsExactly("HT2");
    assertThat(VaiTro.NV_GIAO_HANG.heThong()).containsExactly("HT3");
    assertThat(VaiTro.QL_NHAN_SU.heThong()).containsExactly("HT4");
    assertThat(VaiTro.THU_QUY.heThong()).containsExactly("HT5");
    assertThat(VaiTro.NHAN_VIEN.heThong()).isEmpty();
    assertThat(VaiTro.values()).hasSize(13);
  }

  @Test
  @DisplayName("Ô '–' của ma trận: gọi API phải bị chặn bằng 403")
  void chanQuyenTraVe403() {
    assertThatThrownBy(
            () -> quyen.yeuCauQuyen(VaiTro.NV_NHAN_SU, NhomBang.DON_HANG_HANG_HOA, HanhDong.DOC))
        .isInstanceOf(LoiNghiepVuException.class)
        .satisfies(
            e -> {
              LoiNghiepVuException loi = (LoiNghiepVuException) e;
              assertThat(loi.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
              assertThat(loi.getMaLoi()).isEqualTo("quyen.tuchoi.bang");
            });

    assertThatThrownBy(
            () -> quyen.yeuCauChucNang(VaiTro.NV_KINH_DOANH, ChucNang.HT1_KIEM_DUYET_DON_HANG))
        .isInstanceOf(LoiNghiepVuException.class)
        .satisfies(
            e ->
                assertThat(((LoiNghiepVuException) e).getStatus())
                    .isEqualTo(HttpStatus.FORBIDDEN));
  }
}
