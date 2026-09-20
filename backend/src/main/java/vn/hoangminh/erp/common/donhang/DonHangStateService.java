package vn.hoangminh.erp.common.donhang;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hoangminh.erp.Ht1DonHang.repository.DonHangRepository;
import vn.hoangminh.erp.common.audit.LichSuDonHangService;
import vn.hoangminh.erp.common.exception.LoiNghiepVuException;
import vn.hoangminh.erp.common.security.NguoiDungHienTai;

@Service @RequiredArgsConstructor
public class DonHangStateService {
  private final DonHangRepository donHang;
  private final LichSuDonHangService lichSu;
  private final ApplicationEventPublisher suKien;
  private static final Map<String,String> OWNER = Map.of("Đã hủy","HT1", "Đã nhập kho","HT2",
      "Đang giao","HT3", "Đã giao","HT3", "Đã đối soát","HT5");
  public static void kiemTra(String he, String cu, String moi) {
    if (!he.equals(OWNER.get(moi))) throw LoiNghiepVuException.tuChoiQuyen("quyen.tuchoi.bang");
    boolean hopLe = switch(cu) {
      case "Đã tạo" -> moi.equals("Đã nhập kho") || moi.equals("Đã hủy");
      case "Đã nhập kho" -> moi.equals("Đang giao");
      case "Đang giao" -> moi.equals("Đã giao");
      case "Đã giao" -> moi.equals("Đã đối soát");
      default -> false;
    };
    if (!hopLe) throw LoiNghiepVuException.xungDot("loi.trangthai.xungdot");
  }
  @Transactional
  public void doiTrangThai(String he, String ma, String moi, NguoiDungHienTai nguoi, String ghiChu) {
    if (!nguoi.vaiTro().heThong().contains(he)) throw LoiNghiepVuException.tuChoiQuyen("quyen.tuchoi.bang");
    var d = donHang.khoa(ma).orElseThrow(() -> LoiNghiepVuException.khongTimThay("loi.khongtimthay"));
    kiemTra(he, d.getTrangThai(), moi);
    if (moi.equals("Đã nhập kho") && !lichSu.kiemDuyet(ma).equals("Đã duyệt"))
      throw LoiNghiepVuException.xungDot("ht1.chuaduyet");
    if (moi.equals("Đã hủy")) {
      if (ghiChu == null || ghiChu.isBlank() || ghiChu.length()>300) throw LoiNghiepVuException.duLieu("ht1.lydo");
      d.setLyDoHuy(ghiChu);
    }
    String cu = d.getTrangThai();
    d.setTrangThai(moi);
    lichSu.ghi(ma, moi.equals("Đã hủy") ? "Huy" : "DoiTrangThai", moi, nguoi.maTaiKhoan(), ghiChu);
    suKien.publishEvent(new DonHangDoiTrangThaiEvent(ma, cu, moi));
  }
}
