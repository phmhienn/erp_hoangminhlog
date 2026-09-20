package vn.hoangminh.erp.common.audit;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hoangminh.erp.Ht1DonHang.domain.LichSuDonHang;
import vn.hoangminh.erp.Ht1DonHang.dto.Ht1Dto.LichSuView;
import vn.hoangminh.erp.Ht1DonHang.repository.LichSuDonHangRepository;

@Service @RequiredArgsConstructor
public class LichSuDonHangService {
  private final LichSuDonHangRepository repository;
  @Transactional(propagation=org.springframework.transaction.annotation.Propagation.MANDATORY)
  public void ghi(String ma, String hanhDong, String trangThai, String taiKhoan, String ghiChu) {
    var l = new LichSuDonHang();
    l.setMaDonHang(ma); l.setHanhDong(hanhDong); l.setTrangThai(trangThai);
    l.setMaTaiKhoan(taiKhoan); l.setThoiGian(LocalDateTime.now()); l.setGhiChu(ghiChu);
    repository.saveAndFlush(l);
  }
  @Transactional(readOnly=true)
  public List<LichSuView> doc(String ma) {
    return repository.findByMaDonHangOrderByMaLichSuAsc(ma).stream().map(l -> new LichSuView(
        l.getMaLichSu(), l.getHanhDong(), l.getTrangThai(), l.getMaTaiKhoan(), l.getThoiGian(), l.getGhiChu())).toList();
  }
  public String kiemDuyet(String ma) {
    String k = "Chưa gửi";
    for (var l : doc(ma)) {
      k = switch(l.hanhDong()) {
        case "GuiDuyet" -> "Chờ duyệt";
        case "Duyet" -> "Đã duyệt";
        case "TuChoi" -> "Cần bổ sung";
        default -> k;
      };
    }
    return k;
  }
}
