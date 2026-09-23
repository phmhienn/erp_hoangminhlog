package vn.hoangminh.erp.common.permission;

import static vn.hoangminh.erp.common.permission.VaiTro.KE_TOAN_TRUONG;
import static vn.hoangminh.erp.common.permission.VaiTro.KE_TOAN_VIEN;
import static vn.hoangminh.erp.common.permission.VaiTro.NV_DIEU_PHOI;
import static vn.hoangminh.erp.common.permission.VaiTro.NV_GIAO_HANG;
import static vn.hoangminh.erp.common.permission.VaiTro.NV_KHO;
import static vn.hoangminh.erp.common.permission.VaiTro.NV_KINH_DOANH;
import static vn.hoangminh.erp.common.permission.VaiTro.NV_NHAN_SU;
import static vn.hoangminh.erp.common.permission.VaiTro.QL_DIEU_PHOI;
import static vn.hoangminh.erp.common.permission.VaiTro.QL_KHO;
import static vn.hoangminh.erp.common.permission.VaiTro.QL_KINH_DOANH;
import static vn.hoangminh.erp.common.permission.VaiTro.QL_NHAN_SU;
import static vn.hoangminh.erp.common.permission.VaiTro.THU_QUY;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Quyền <b>chức năng</b> theo vai trò, bổ sung cho ma trận quyền trên bảng của PHỤ LỤC B
 * ("Bổ sung phân quyền chức năng theo đúng mục 3.1.2, 3.2.2, 3.3.2, 3.4.2, 3.5.2").
 *
 * <p>Menu và nút thao tác của frontend render theo danh sách này; API chặn bằng
 * {@code QuyenService.coChucNang(...)}. Tên chức năng lấy theo đúng chữ của đặc tả 3.x
 * (đã đối chiếu 3.1.2/3.1.3); các hệ thống còn lại đối chiếu khi lập phase tương ứng — xem NOTES.md.
 */
public enum ChucNang {
  // ---------- HT1: Quản lý đơn hàng/dịch vụ (3.1) ----------
  HT1_QUAN_LY_KHACH_HANG("HT1", "Quản lý thông tin khách hàng", NV_KINH_DOANH, QL_KINH_DOANH),
  HT1_TAO_DON_HANG("HT1", "Tạo đơn hàng", NV_KINH_DOANH, QL_KINH_DOANH),
  HT1_CAP_NHAT_DON_HANG("HT1", "Cập nhật/điều chỉnh thông tin đơn hàng", NV_KINH_DOANH, QL_KINH_DOANH),
  HT1_KIEM_DUYET_DON_HANG("HT1", "Kiểm duyệt đơn hàng", QL_KINH_DOANH),
  HT1_HUY_DON_HANG("HT1", "Hủy đơn hàng", NV_KINH_DOANH, QL_KINH_DOANH),
  // Biểu đồ usecase tổng quát (3.1.3) tách hai usecase: "Theo dõi đơn hàng" của quản lý kinh doanh
  // (giám sát toàn bộ đơn, phát hiện đơn bất thường — 3.1.2 vai trò 2) và "Tra cứu đơn hàng" của
  // nhân viên kinh doanh (tra theo mã đơn, khách hàng, thời gian tạo trên các đơn do mình tạo).
  HT1_THEO_DOI_DON_HANG("HT1", "Theo dõi đơn hàng", QL_KINH_DOANH),
  HT1_TRA_CUU_DON_HANG("HT1", "Tra cứu đơn hàng", NV_KINH_DOANH),
  HT1_XEM_BAO_CAO_DON_HANG("HT1", "Xem báo cáo đơn hàng", QL_KINH_DOANH),

  // ---------- HT2: Quản lý nhập – xuất kho (3.2) ----------
  // Biểu đồ usecase 3.2.3 KHÔNG có quan hệ kế thừa giữa hai actor: nhân viên kho làm tác nghiệp
  // (tra cứu, lập – cập nhật phiếu, kiểm tra hàng), quản lý kho làm giám sát (quản lí phiếu,
  // theo dõi, báo cáo). Vì vậy mỗi chức năng chỉ thuộc đúng một vai trò.
  // Biểu đồ usecase 3.2.3 vẽ quan hệ generalization: Người quản lí kho KẾ THỪA Nhân viên kho,
  // nên quản lý kho có đủ chức năng tác nghiệp của nhân viên, cộng thêm phê duyệt và báo cáo.
  HT2_TRA_CUU_DON_NHAP_KHO("HT2", "Báo cáo phiếu nhập/xuất", NV_KHO, QL_KHO),
  HT2_LAP_PHIEU_NHAP("HT2", "Lập phiếu nhập kho", NV_KHO, QL_KHO),
  HT2_CAP_NHAT_PHIEU("HT2", "Cập nhật phiếu nhập/xuất kho", NV_KHO, QL_KHO),
  HT2_LAP_BIEN_BAN_SU_CO("HT2", "Lập biên bản sự cố hàng không đạt", NV_KHO, QL_KHO),
  HT2_LAP_PHIEU_XUAT("HT2", "Lập phiếu xuất kho", NV_KHO, QL_KHO),
  HT2_KIEM_KE("HT2", "Kiểm tra hàng nhập – xuất", NV_KHO, QL_KHO),
  // "Theo dõi kho" nối với Nhân viên kho trong biểu đồ; quản lý kho có nhờ kế thừa.
  HT2_DASHBOARD_NHAP_XUAT("HT2", "Theo dõi kho", NV_KHO, QL_KHO),
  // Chỉ riêng Người quản lí kho
  HT2_PHE_DUYET_PHIEU_NHAP("HT2", "Quản lí phiếu nhập – xuất", QL_KHO),
  HT2_BAO_CAO_NHAP_KHO("HT2", "Báo cáo nhập – xuất kho", QL_KHO),

  // ---------- HT3: Quản lý điều phối & giao hàng (3.3) ----------
  HT3_LAP_KE_HOACH_TUYEN("HT3", "Lập kế hoạch và phân luồng tuyến đường", NV_DIEU_PHOI, QL_DIEU_PHOI),
  HT3_PHAN_CONG_TAI_XE("HT3", "Phân công tài xế và phương tiện", NV_DIEU_PHOI, QL_DIEU_PHOI),
  HT3_DUYET_DIEU_PHOI("HT3", "Phê duyệt phương án điều phối", QL_DIEU_PHOI),
  HT3_NHIEM_VU_CUA_TOI("HT3", "Nhận nhiệm vụ và lộ trình", NV_GIAO_HANG),
  HT3_XAC_NHAN_NHAN_DON("HT3", "Xác nhận nhận đơn hàng", NV_GIAO_HANG),
  HT3_CAP_NHAT_HANH_TRINH("HT3", "Cập nhật mốc trạng thái hành trình", NV_GIAO_HANG),
  // NV_DIEU_PHOI có mặt theo đặc tả 3.3.2 vai trò nhân viên điều phối:
  // "Cập nhật và xử lý sự cố vận tải: ghi nhận, xử lý tình huống phát sinh".
  HT3_BAO_CAO_SU_CO("HT3", "Báo cáo và xử lý sự cố vận tải", NV_GIAO_HANG, NV_DIEU_PHOI, QL_DIEU_PHOI),
  HT3_XAC_NHAN_GIAO_HANG("HT3", "Xác nhận giao hàng thành công", NV_GIAO_HANG),
  HT3_TRA_CUU_LICH_SU_GIAO("HT3", "Tra cứu lịch sử giao hàng", NV_DIEU_PHOI, QL_DIEU_PHOI, NV_GIAO_HANG),
  HT3_LICH_SU_CA_NHAN("HT3", "Tra cứu lịch sử cá nhân NV giao hàng", NV_GIAO_HANG),

  // ---------- HT4: Quản lý hồ sơ nhân viên (3.4) ----------
  HT4_THEM_HO_SO("HT4", "Thêm hồ sơ nhân viên", NV_NHAN_SU, QL_NHAN_SU),
  HT4_SUA_HO_SO("HT4", "Sửa hồ sơ nhân viên", NV_NHAN_SU, QL_NHAN_SU),
  HT4_XEM_HO_SO("HT4", "Xem hồ sơ nhân viên", NV_NHAN_SU, QL_NHAN_SU),
  HT4_TIM_KIEM_HO_SO("HT4", "Tìm kiếm hồ sơ nhân viên", NV_NHAN_SU, QL_NHAN_SU),
  HT4_QUAN_LY_TRANG_THAI("HT4", "Quản lý trạng thái nhân viên", NV_NHAN_SU, QL_NHAN_SU),
  HT4_XEM_HO_SO_CA_NHAN("HT4", "Xem hồ sơ của chính mình", VaiTro.values()),
  HT4_GUI_YEU_CAU_CAP_NHAT("HT4", "Gửi yêu cầu cập nhật thông tin", VaiTro.values()),

  // ---------- HT5: Quản lý thu/chi – COD (3.5) ----------
  // Kế toán viên (biểu đồ usecase 3.5.3)
  HT5_TRA_CUU_COD("HT5", "Tra cứu dữ liệu COD", KE_TOAN_VIEN),
  HT5_DOI_SOAT_COD("HT5", "Đối soát COD", KE_TOAN_VIEN),
  HT5_LAP_PHIEU_THU("HT5", "Lập phiếu thu", KE_TOAN_VIEN),
  HT5_LAP_PHIEU_CHI("HT5", "Lập phiếu chi", KE_TOAN_VIEN),
  HT5_QUAN_LY_SAI_LECH("HT5", "Quản lý sai lệch COD", KE_TOAN_VIEN),
  // Thủ quỹ
  HT5_XAC_NHAN_THU_TIEN("HT5", "Xác nhận thu tiền", THU_QUY),
  HT5_XAC_NHAN_CHI_TRA("HT5", "Xác nhận chi trả COD", THU_QUY),
  HT5_CAP_NHAT_TRANG_THAI_GD("HT5", "Cập nhật trạng thái giao dịch", THU_QUY),
  // Kế toán trưởng
  HT5_PHE_DUYET_GIAO_DICH("HT5", "Phê duyệt giao dịch", KE_TOAN_TRUONG),
  // Dùng chung giữa các vai trò theo đúng mũi tên của biểu đồ
  HT5_LICH_SU_GIAO_DICH("HT5", "Tra cứu lịch sử giao dịch", THU_QUY, KE_TOAN_TRUONG),
  HT5_BAO_CAO_THU_CHI("HT5", "Xem báo cáo thu – chi COD", KE_TOAN_VIEN, KE_TOAN_TRUONG);

  private final String heThong;
  private final String ten;
  private final Set<VaiTro> vaiTro;

  ChucNang(String heThong, String ten, VaiTro... vaiTro) {
    this.heThong = heThong;
    this.ten = ten;
    this.vaiTro = Set.of(vaiTro);
  }

  public String heThong() {
    return heThong;
  }

  /** Tên chức năng đúng chữ của đặc tả 3.x. */
  public String ten() {
    return ten;
  }

  public Set<VaiTro> vaiTro() {
    return vaiTro;
  }

  /** Mọi chức năng của một vai trò. */
  public static Set<ChucNang> cuaVaiTro(VaiTro v) {
    return Arrays.stream(values()).filter(c -> c.vaiTro.contains(v)).collect(Collectors.toSet());
  }
}
