package vn.hoangminh.erp.Ht4NhanSu.service;

import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hoangminh.erp.Ht4NhanSu.domain.NhanVien;
import vn.hoangminh.erp.Ht4NhanSu.dto.NhanVienTomTat;
import vn.hoangminh.erp.Ht4NhanSu.repository.NhanVienRepository;
import vn.hoangminh.erp.common.exception.LoiNghiepVuException;

/**
 * Service công khai của module HT4 cho các module khác đọc hồ sơ nhân viên
 * (bảng dùng chung {@code NhanVien} — mục 4.2, 4.3: không sao chép bản thứ hai).
 */
@Service
public class NhanVienQueryService {

  private final NhanVienRepository nhanVienRepository;

  public NhanVienQueryService(NhanVienRepository nhanVienRepository) {
    this.nhanVienRepository = nhanVienRepository;
  }

  @Transactional(readOnly = true)
  public NhanVienTomTat tomTat(String maNV) {
    NhanVien nv =
        nhanVienRepository
            .findById(maNV)
            .orElseThrow(() -> LoiNghiepVuException.khongTimThay("loi.khongtimthay"));
    return new NhanVienTomTat(
        nv.getMaNV(),
        nv.getHoTen(),
        nv.getPhongBan() == null ? null : nv.getPhongBan().getMaPhongBan(),
        nv.getPhongBan() == null ? null : nv.getPhongBan().getTenPhongBan(),
        nv.getChucVu() == null ? null : nv.getChucVu().getMaChucVu(),
        nv.getChucVu() == null ? null : nv.getChucVu().getTenChucVu(),
        nv.getTrangThai() == null ? null : nv.getTrangThai().getTenTrangThai(),
        nv.getEmail(),
        nv.getSoDienThoai());
  }

  /** Tên nhân viên dùng cho các chỗ chỉ cần hiển thị (người lập, người duyệt…). */
  @Transactional(readOnly = true)
  public Optional<String> tenNhanVien(String maNV) {
    return nhanVienRepository.findById(maNV).map(NhanVien::getHoTen);
  }

  @Transactional(readOnly = true)
  public boolean tonTai(String maNV) {
    return nhanVienRepository.existsById(maNV);
  }
}
