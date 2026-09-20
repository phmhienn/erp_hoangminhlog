package vn.hoangminh.erp.Ht1DonHang.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.time.DayOfWeek;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hoangminh.erp.Ht1DonHang.domain.*;
import vn.hoangminh.erp.Ht1DonHang.dto.Ht1Dto.*;
import vn.hoangminh.erp.Ht1DonHang.repository.*;
import vn.hoangminh.erp.common.audit.LichSuDonHangService;
import vn.hoangminh.erp.common.donhang.DonHangStateService;
import vn.hoangminh.erp.common.exception.LoiNghiepVuException;
import vn.hoangminh.erp.common.permission.*;
import vn.hoangminh.erp.common.security.NguoiDungHienTai;
import vn.hoangminh.erp.common.sequence.*;
import static vn.hoangminh.erp.common.permission.ChucNang.*;

@Service @RequiredArgsConstructor
public class Ht1Service {
  private final KhachHangRepository khachHang;
  private final DichVuRepository dichVu;
  private final DonHangRepository donHang;
  private final HangHoaRepository hangHoa;
  private final LichSuDonHangService lichSu;
  private final DonHangStateService state;
  private final MaChungTuService ma;
  private final TaoChungTuTransaction tao;
  private final QuyenService quyen;
  private final Validator validator;
  private final MessageSource messages;
  private final jakarta.persistence.EntityManager entityManager;

  private NguoiDungHienTai yeuCau(ChucNang c) {
    var n = NguoiDungHienTai.hienTai(); quyen.yeuCauChucNang(n.vaiTro(), c); return n;
  }
  /** Cho qua nếu vai trò có ít nhất một trong các chức năng — dùng cho phần đọc đơn dùng chung. */
  private NguoiDungHienTai yeuCauMot(ChucNang... cs) {
    var n = NguoiDungHienTai.hienTai();
    for (ChucNang c : cs) if (quyen.coChucNang(n.vaiTro(), c)) return n;
    throw LoiNghiepVuException.tuChoiQuyen("quyen.tuchoi");
  }
  private void hopLe(Object input) {
    if (!validator.validate(input).isEmpty()) throw LoiNghiepVuException.duLieu("loi.dulieu.khonghople");
  }
  private boolean trongPhamVi(String id, NguoiDungHienTai n) {
    return n.vaiTro()==VaiTro.QL_KINH_DOANH || lichSu.doc(id).stream()
        .anyMatch(l -> l.hanhDong().equals("Tao") && l.maTaiKhoan().equals(n.maTaiKhoan()));
  }
  private DonHang lay(String id, NguoiDungHienTai n, boolean khoa) {
    var d = (khoa ? donHang.khoa(id) : donHang.findById(id))
        .orElseThrow(() -> LoiNghiepVuException.khongTimThay("loi.khongtimthay"));
    if (!trongPhamVi(id,n)) throw LoiNghiepVuException.tuChoiQuyen("quyen.tuchoi.bang");
    return d;
  }
  private static boolean chuaXuLy(DonHang d) { return d.getTrangThai().equals("Đã tạo"); }
  private void duocSua(DonHang d) {
    var k=lichSu.kiemDuyet(d.getMaDonHang());
    if (!chuaXuLy(d) || k.equals("Chờ duyệt") || k.equals("Đã duyệt"))
      throw LoiNghiepVuException.xungDot("ht1.khoasua");
  }
  private static boolean chua(String value,String term) {
    return term==null || term.isBlank() || (value!=null && value.toLowerCase(Locale.ROOT).contains(term.trim().toLowerCase(Locale.ROOT)));
  }
  private KhachHangView view(KhachHang k) { return new KhachHangView(k.getMaKhachHang(),k.getTenKhachHang(),k.getSoDienThoai(),k.getDiachi(),k.getEmail(),k.getLoaiKH()); }
  @Transactional(readOnly=true)
  public List<KhachHangView> khachHang(String tuKhoa) {
    yeuCau(HT1_QUAN_LY_KHACH_HANG);
    return khachHang.findAll().stream().filter(k -> chua(k.getMaKhachHang(),tuKhoa)||chua(k.getTenKhachHang(),tuKhoa)||chua(k.getSoDienThoai(),tuKhoa)||chua(k.getEmail(),tuKhoa))
        .sorted(Comparator.comparing(KhachHang::getMaKhachHang)).map(this::view).toList();
  }
  public KhachHangView taoKhach(KhachHangInput i) {
    yeuCau(HT1_QUAN_LY_KHACH_HANG); hopLe(i);
    return tao.thucHien(() -> { var k=new KhachHang(); k.setMaKhachHang(ma.capMa(LoaiChungTu.KHACH_HANG)); return luuKhach(k,i); });
  }
  @Transactional
  public KhachHangView suaKhach(String id, KhachHangInput i) {
    yeuCau(HT1_QUAN_LY_KHACH_HANG); hopLe(i);
    return luuKhach(khachHang.findById(id).orElseThrow(() -> LoiNghiepVuException.khongTimThay("loi.khongtimthay")),i);
  }
  private KhachHangView luuKhach(KhachHang k, KhachHangInput i) {
    k.setTenKhachHang(i.tenKhachHang().trim()); k.setSoDienThoai(i.soDienThoai()); k.setDiachi(i.diachi()); k.setEmail(i.email()); k.setLoaiKH(i.loaiKH());
    // persist đảm bảo INSERT: merge có thể ghi đè bản ghi vừa được transaction khác tạo cùng mã.
    if (!entityManager.contains(k)) entityManager.persist(k);
    khachHang.flush();
    return view(k);
  }
  @Transactional(readOnly=true)
  public List<DichVuView> dichVu() {
    yeuCau(HT1_TAO_DON_HANG);
    return dichVu.findAll().stream().map(d -> new DichVuView(d.getMaDichVu(),d.getTenDichVu(),d.getDonGia(),d.getMoTa())).toList();
  }
  private void dien(DonHang d, DonHangInput i) {
    hopLe(i);
    if (!khachHang.existsById(i.maKhachHang()) || !dichVu.existsById(i.maDichVu())) throw LoiNghiepVuException.duLieu("ht1.danhmuc");
    if (i.hangHoa().stream().anyMatch(h -> !Double.isFinite(h.trongLuong()))) throw LoiNghiepVuException.duLieu("loi.dulieu.khonghople");
    d.setMaKhachHang(i.maKhachHang()); d.setMaDichVu(i.maDichVu()); d.setNguoiGui(i.nguoiGui().trim()); d.setNguoiNhan(i.nguoiNhan().trim());
    d.setSdtNguoiGui(i.sdtNguoiGui()); d.setSdtNguoiNhan(i.sdtNguoiNhan()); d.setDiachiLayHang(i.diachiLayHang().trim()); d.setDiachiGiaoHang(i.diachiGiaoHang().trim());
    d.setKhoiLuong(i.khoiLuong()); d.setTienCOD(i.tienCOD()); d.setPhiVanChuyen(i.phiVanChuyen());
  }
  private void luuHang(String id,List<HangHoaInput> inputs) {
    // Lấy số kế tiếp trước khi xóa để không tái sử dụng mã hàng cũ khi sửa đơn.
    String next=ma.capMa(LoaiChungTu.HANG_HOA); int so=Integer.parseInt(next.substring(2));
    hangHoa.deleteByMaDonHang(id); hangHoa.flush();
    for(var i:inputs) {
      var h=new HangHoa(); h.setMaHangHoa(LoaiChungTu.HANG_HOA.ma(so++)); h.setMaDonHang(id);
      h.setLoaiHangHoa(i.loaiHangHoa()); h.setSoLuong(i.soLuong()); h.setTrongLuong(i.trongLuong()); entityManager.persist(h); entityManager.flush();
    }
  }
  public DonHangView taoDon(DonHangInput i) {
    var n=yeuCau(HT1_TAO_DON_HANG);
    return tao.thucHien(() -> {
      var d=new DonHang(); dien(d,i); d.setMaDonHang(ma.capMa(LoaiChungTu.DON_HANG)); d.setTrangThai("Đã tạo"); d.setNgayTao(LocalDateTime.now());
      entityManager.persist(d); entityManager.flush(); luuHang(d.getMaDonHang(),i.hangHoa()); lichSu.ghi(d.getMaDonHang(),"Tao",d.getTrangThai(),n.maTaiKhoan(),null); return view(d);
    });
  }
  public DonHangView suaDon(String id,DonHangInput i) {
    var n=yeuCau(HT1_CAP_NHAT_DON_HANG);
    return tao.thucHien(() -> {
      var d=lay(id,n,true); duocSua(d); dien(d,i); luuHang(id,i.hangHoa());
      lichSu.ghi(id,"CapNhat",d.getTrangThai(),n.maTaiKhoan(),null); return view(d);
    });
  }
  @Transactional
  public DonHangView xuLy(String id,String action,String reason) {
    var c=switch(action) { case "gui-duyet" -> HT1_TAO_DON_HANG; case "duyet","tu-choi" -> HT1_KIEM_DUYET_DON_HANG; case "huy" -> HT1_HUY_DON_HANG; default -> throw LoiNghiepVuException.khongTimThay("loi.khongtimthay"); };
    var n=yeuCau(c); var d=lay(id,n,true);
    if (action.equals("huy") || action.equals("tu-choi")) hopLe(new LyDoInput(reason));
    if(action.equals("huy")) { state.doiTrangThai("HT1",id,"Đã hủy",n,reason); return view(d); }
    if(!chuaXuLy(d)) throw LoiNghiepVuException.xungDot("loi.trangthai.xungdot");
    if(action.equals("gui-duyet")) {
      duocSua(d);
      var items=hangHoa.findByMaDonHangOrderByMaHangHoa(id).stream().map(h -> new HangHoaInput(h.getLoaiHangHoa(),h.getSoLuong(),h.getTrongLuong())).toList();
      hopLe(new DonHangInput(d.getMaKhachHang(),d.getMaDichVu(),d.getNguoiGui(),d.getNguoiNhan(),d.getSdtNguoiGui(),d.getSdtNguoiNhan(),d.getDiachiLayHang(),d.getDiachiGiaoHang(),d.getKhoiLuong(),d.getTienCOD(),d.getPhiVanChuyen(),items));
    } else if (!lichSu.kiemDuyet(id).equals("Chờ duyệt")) throw LoiNghiepVuException.xungDot("ht1.choguiduyet");
    String event=switch(action) { case "gui-duyet" -> "GuiDuyet"; case "duyet" -> "Duyet"; default -> "TuChoi"; };
    lichSu.ghi(id,event,d.getTrangThai(),n.maTaiKhoan(),reason); return view(d);
  }
  private DonHangView view(DonHang d) {
    var hs=hangHoa.findByMaDonHangOrderByMaHangHoa(d.getMaDonHang()).stream().map(h -> new HangHoaView(h.getMaHangHoa(),h.getMaSP(),h.getLoaiHangHoa(),h.getSoLuong(),h.getTrongLuong())).toList();
    return new DonHangView(d.getMaDonHang(),d.getMaKhachHang(),d.getMaDichVu(),d.getNguoiGui(),d.getNguoiNhan(),d.getSdtNguoiGui(),d.getSdtNguoiNhan(),d.getDiachiLayHang(),d.getDiachiGiaoHang(),d.getKhoiLuong(),d.getTienCOD(),d.getPhiVanChuyen(),d.getTrangThai(),lichSu.kiemDuyet(d.getMaDonHang()),d.getLyDoHuy(),d.getNgayTao(),hs);
  }
  private void ngay(LocalDate tu,LocalDate den) {
    if(tu!=null && den!=null && tu.isAfter(den)) throw LoiNghiepVuException.duLieu("ht1.tieuchi");
  }
  private List<DonHang> loc(String id,String kh,LocalDate tu,LocalDate den,String dv,NguoiDungHienTai n) {
    ngay(tu,den);
    var khach=khachHang.findAll().stream().collect(Collectors.toMap(KhachHang::getMaKhachHang,KhachHang::getTenKhachHang));
    return donHang.findAll().stream().filter(d -> trongPhamVi(d.getMaDonHang(),n))
        .filter(d -> chua(d.getMaDonHang(),id) && (chua(d.getMaKhachHang(),kh)||chua(khach.get(d.getMaKhachHang()),kh)))
        .filter(d -> (tu==null||!d.getNgayTao().toLocalDate().isBefore(tu)) && (den==null||!d.getNgayTao().toLocalDate().isAfter(den)))
        .filter(d -> dv==null||dv.isBlank()||dv.equals(d.getMaDichVu())).sorted(Comparator.comparing(DonHang::getNgayTao).reversed()).toList();
  }
  @Transactional(readOnly=true)
  public List<DonHangView> danhSach(String id,String kh,LocalDate tu,LocalDate den) {
    var n=yeuCauMot(HT1_THEO_DOI_DON_HANG,HT1_TRA_CUU_DON_HANG); return loc(id,kh,tu,den,null,n).stream().map(this::view).toList();
  }
  @Transactional(readOnly=true)
  public DonHangView chiTiet(String id) { return view(lay(id,yeuCauMot(HT1_THEO_DOI_DON_HANG,HT1_TRA_CUU_DON_HANG),false)); }
  @Transactional(readOnly=true)
  public List<LichSuView> lichSu(String id) { lay(id,yeuCauMot(HT1_THEO_DOI_DON_HANG,HT1_TRA_CUU_DON_HANG),false); return lichSu.doc(id); }
  private Map<String,Long> dem(List<DonHang> ds,Function<DonHang,String> key) { return ds.stream().collect(Collectors.groupingBy(key,TreeMap::new,Collectors.counting())); }
  @Transactional(readOnly=true)
  public BaoCao baoCao(LocalDate tu,LocalDate den,String kh,String dv) {
    var n=yeuCau(HT1_XEM_BAO_CAO_DON_HANG);
    if(kh!=null&&!kh.isBlank()&&!khachHang.existsById(kh)||dv!=null&&!dv.isBlank()&&!dichVu.existsById(dv)) throw LoiNghiepVuException.duLieu("ht1.tieuchi");
    var ds=loc(null,null,tu,den,dv,n).stream().filter(d -> kh==null||kh.isBlank()||kh.equals(d.getMaKhachHang())).toList();
    return new BaoCao(ds.size(),dem(ds,DonHang::getTrangThai),dem(ds,d -> lichSu.kiemDuyet(d.getMaDonHang())),
      dem(ds,d -> d.getNgayTao().toLocalDate().toString()),dem(ds,d -> d.getNgayTao().toLocalDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toString()),
      dem(ds,d -> d.getNgayTao().toLocalDate().toString().substring(0,7)),dem(ds,DonHang::getMaKhachHang),dem(ds,DonHang::getMaDichVu),
      ds.isEmpty()?messages.getMessage("ht1.baocao.trong",null,Locale.forLanguageTag("vi-VN")):null);
  }
}
