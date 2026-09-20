package vn.hoangminh.erp.common.permission;

import static vn.hoangminh.erp.common.permission.HanhDong.DOC;
import static vn.hoangminh.erp.common.permission.HanhDong.GHI_CHI_PHI_LUU_KHO;
import static vn.hoangminh.erp.common.permission.HanhDong.HUY_MEM;
import static vn.hoangminh.erp.common.permission.HanhDong.SUA;
import static vn.hoangminh.erp.common.permission.HanhDong.SUA_TIEN_COD;
import static vn.hoangminh.erp.common.permission.HanhDong.SUA_TRANG_THAI;
import static vn.hoangminh.erp.common.permission.HanhDong.THEM;
import static vn.hoangminh.erp.common.permission.HanhDong.XOA;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import vn.hoangminh.erp.Ht4NhanSu.dto.NhanVienTomTat;
import vn.hoangminh.erp.Ht4NhanSu.service.NhanVienQueryService;
import vn.hoangminh.erp.common.exception.LoiNghiepVuException;
import vn.hoangminh.erp.common.permission.dto.QuyenCuaToiResponse;
import vn.hoangminh.erp.common.permission.dto.QuyenCuaToiResponse.ChucNangDto;
import vn.hoangminh.erp.common.permission.dto.QuyenCuaToiResponse.QuyenBangDto;
import vn.hoangminh.erp.common.security.NguoiDungHienTai;

/**
 * Hiện thực ma trận phân quyền PHỤ LỤC B (mục 4.3) — nguồn duy nhất cho cả UI lẫn API.
 *
 * <p>Ô "–" của ma trận = tập quyền rỗng: không hiển thị trên UI và API trả 403.
 * Mọi module gọi {@link #yeuCauQuyen} / {@link #yeuCauChucNang} trước khi ghi dữ liệu.
 */
@Service
public class QuyenService {

  private static final Locale LOCALE_VI = Locale.forLanguageTag("vi-VN");

  private final MessageSource messages;
  private final NhanVienQueryService nhanVienQueryService;

  public QuyenService(MessageSource messages, NhanVienQueryService nhanVienQueryService) {
    this.messages = messages;
    this.nhanVienQueryService = nhanVienQueryService;
  }

  private static final Map<NhomBang, Map<String, Set<HanhDong>>> MA_TRAN =
      new EnumMap<>(NhomBang.class);

  static {
    // | KhachHang, DichVu | Đ/T/S, hủy mềm | Đọc | – | – | Đọc |
    MA_TRAN.put(
        NhomBang.KHACH_HANG_DICH_VU,
        row("HT1", Set.of(DOC, THEM, SUA, HUY_MEM), "HT2", Set.of(DOC), "HT3", Set.of(), "HT4",
            Set.of(), "HT5", Set.of(DOC)));

    // | DonHang, HangHoa | Đ/T/S, hủy mềm | Đọc/Sửa (trangThai) | Đọc/Sửa (trangThai) | – | Đọc/Sửa (trangThai, tienCOD) |
    MA_TRAN.put(
        NhomBang.DON_HANG_HANG_HOA,
        row("HT1", Set.of(DOC, THEM, SUA, HUY_MEM), "HT2", Set.of(DOC, SUA_TRANG_THAI), "HT3",
            Set.of(DOC, SUA_TRANG_THAI), "HT4", Set.of(), "HT5",
            Set.of(DOC, SUA_TRANG_THAI, SUA_TIEN_COD)));

    // | PHIEU_NHAP...TON_KHO, SanPham | Đọc | Đ/T/S/X | Đọc | – | Đọc + Ghi ChiPhiLuuKho |
    MA_TRAN.put(
        NhomBang.KHO,
        row("HT1", Set.of(DOC), "HT2", Set.of(DOC, THEM, SUA, XOA), "HT3", Set.of(DOC), "HT4",
            Set.of(), "HT5", Set.of(DOC, GHI_CHI_PHI_LUU_KHO)));

    // | PhuongTien, ChuyenVan, LoTrinh, SuCoVanTai, ChiTietChuyenHang | – | Đọc | Đ/T/S/X | – | Đọc |
    MA_TRAN.put(
        NhomBang.DIEU_PHOI,
        row("HT1", Set.of(), "HT2", Set.of(DOC), "HT3", Set.of(DOC, THEM, SUA, XOA), "HT4",
            Set.of(), "HT5", Set.of(DOC)));

    // | NhanVien, PhongBan, ChucVu, TrangThaiNhanVien | Đọc | Đọc | Đọc | Đ/T/S/X | Đọc |
    MA_TRAN.put(
        NhomBang.NHAN_SU,
        row("HT1", Set.of(DOC), "HT2", Set.of(DOC), "HT3", Set.of(DOC), "HT4",
            Set.of(DOC, THEM, SUA, XOA), "HT5", Set.of(DOC)));

    // | TaiXe | Đọc | – | Sửa (trangThai) | Đ/T/S/X (soGPLX, loaiGPLX) | Đọc |
    // [NOTES] HT3 bổ sung quyền Đọc: 3.3.1 bắt buộc chọn tài xế khi lập kế hoạch nên phải đọc được
    // danh sách tài xế; ma trận chỉ ghi "Sửa (trangThai)" cho HT3.
    MA_TRAN.put(
        NhomBang.TAI_XE,
        row("HT1", Set.of(DOC), "HT2", Set.of(), "HT3", Set.of(DOC, SUA_TRANG_THAI), "HT4",
            Set.of(DOC, THEM, SUA, XOA), "HT5", Set.of(DOC)));

    // [NOTES] Hai dòng dưới KHÔNG có trong ma trận 4.3 (tài liệu bỏ sót — loierp mục 20/23).
    // Chỉ cấp cho hệ thống sở hữu bảng; các hệ thống khác = ô "–".
    MA_TRAN.put(
        NhomBang.HT5_THU_CHI,
        row("HT1", Set.of(), "HT2", Set.of(), "HT3", Set.of(), "HT4", Set.of(), "HT5",
            Set.of(DOC, THEM, SUA, XOA)));
    MA_TRAN.put(
        NhomBang.TAI_KHOAN,
        row("HT1", Set.of(), "HT2", Set.of(), "HT3", Set.of(), "HT4", Set.of(DOC, THEM, SUA),
            "HT5", Set.of()));
  }

  private static Map<String, Set<HanhDong>> row(
      String ht1, Set<HanhDong> q1,
      String ht2, Set<HanhDong> q2,
      String ht3, Set<HanhDong> q3,
      String ht4, Set<HanhDong> q4,
      String ht5, Set<HanhDong> q5) {
    Map<String, Set<HanhDong>> m = new java.util.HashMap<>();
    m.put(ht1, Collections.unmodifiableSet(q1));
    m.put(ht2, Collections.unmodifiableSet(q2));
    m.put(ht3, Collections.unmodifiableSet(q3));
    m.put(ht4, Collections.unmodifiableSet(q4));
    m.put(ht5, Collections.unmodifiableSet(q5));
    return Collections.unmodifiableMap(m);
  }

  /** Quyền của một vai trò trên một nhóm bảng = hợp quyền của các hệ thống vai trò đó thuộc về. */
  public Set<HanhDong> quyenTrenBang(VaiTro vaiTro, NhomBang nhomBang) {
    Map<String, Set<HanhDong>> theoHe = MA_TRAN.get(nhomBang);
    if (theoHe == null) {
      return Set.of();
    }
    return vaiTro.heThong().stream()
        .map(ht -> theoHe.getOrDefault(ht, Set.of()))
        .flatMap(Set::stream)
        .collect(java.util.stream.Collectors.toUnmodifiableSet());
  }

  public boolean coQuyen(VaiTro vaiTro, NhomBang nhomBang, HanhDong hanhDong) {
    return quyenTrenBang(vaiTro, nhomBang).contains(hanhDong);
  }

  /** Chặn nếu vai trò rơi vào ô "–" (hoặc thiếu hành động) — HTTP 403. */
  public void yeuCauQuyen(VaiTro vaiTro, NhomBang nhomBang, HanhDong hanhDong) {
    if (!coQuyen(vaiTro, nhomBang, hanhDong)) {
      throw LoiNghiepVuException.tuChoiQuyen("quyen.tuchoi.bang");
    }
  }

  public boolean coChucNang(VaiTro vaiTro, ChucNang chucNang) {
    return chucNang.vaiTro().contains(vaiTro);
  }

  /** Chặn nếu vai trò không có chức năng (ví dụ NV kinh doanh gọi "Kiểm duyệt đơn hàng"). */
  public void yeuCauChucNang(VaiTro vaiTro, ChucNang chucNang) {
    if (!coChucNang(vaiTro, chucNang)) {
      throw LoiNghiepVuException.tuChoiQuyen("quyen.tuchoi");
    }
  }

  public Set<ChucNang> chucNangCua(VaiTro vaiTro) {
    return ChucNang.cuaVaiTro(vaiTro);
  }

  /** Toàn bộ ma trận — dùng cho trang quản trị/kiểm thử và tài liệu hoá. */
  public Map<NhomBang, Map<String, Set<HanhDong>>> maTranDayDu() {
    return Collections.unmodifiableMap(MA_TRAN);
  }

  /**
   * Dựng payload quyền của một người dùng: vai trò + hệ thống + quyền trên bảng (PHỤ LỤC B)
   * + quyền chức năng (3.x.2). Frontend dùng payload này cho {@code usePermission()}.
   */
  public QuyenCuaToiResponse quyenCuaToi(NguoiDungHienTai nd) {
    VaiTro vaiTro = nd.vaiTro();
    NhanVienTomTat hoSo = nhanVienQueryService.tomTat(nd.maNV());

    List<QuyenBangDto> quyenBang = new ArrayList<>();
    for (NhomBang nhom : NhomBang.values()) {
      Set<HanhDong> hd = quyenTrenBang(vaiTro, nhom);
      quyenBang.add(
          new QuyenBangDto(
              nhom.name(),
              nhom.ten(),
              hd.stream().map(Enum::name).sorted().toList()));
    }

    List<ChucNangDto> chucNang =
        chucNangCua(vaiTro).stream()
            .map(c -> new ChucNangDto(c.name(), c.ten(), c.heThong()))
            .sorted(java.util.Comparator.comparing(ChucNangDto::ma))
            .toList();

    return new QuyenCuaToiResponse(
        nd.maTaiKhoan(),
        nd.maNV(),
        nd.tenDangNhap(),
        hoSo.hoTen(),
        vaiTro.name(),
        tenVaiTro(vaiTro),
        hoSo.tenPhongBan(),
        hoSo.tenChucVu(),
        vaiTro.heThong().stream().sorted().toList(),
        quyenBang,
        chucNang);
  }

  /** Tên vai trò tiếng Việt từ {@code messages_vi.properties} (khoá {@code vaitro.<VAI_TRO>}). */
  public String tenVaiTro(VaiTro vaiTro) {
    try {
      return messages.getMessage("vaitro." + vaiTro.name(), null, LOCALE_VI);
    } catch (Exception e) {
      return vaiTro.name();
    }
  }
}
