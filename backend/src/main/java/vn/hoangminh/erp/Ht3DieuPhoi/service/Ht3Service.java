package vn.hoangminh.erp.Ht3DieuPhoi.service;
import jakarta.persistence.EntityManager; import jakarta.validation.Validator; import lombok.RequiredArgsConstructor; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import java.time.*; import java.util.*;
import vn.hoangminh.erp.Ht1DonHang.domain.DonHang; import vn.hoangminh.erp.Ht1DonHang.repository.DonHangRepository; import vn.hoangminh.erp.Ht3DieuPhoi.domain.*; import vn.hoangminh.erp.Ht3DieuPhoi.dto.Ht3Dto.*; import vn.hoangminh.erp.Ht3DieuPhoi.repository.*; import vn.hoangminh.erp.Ht4NhanSu.domain.TaiXe; import vn.hoangminh.erp.Ht4NhanSu.repository.TaiXeRepository; import vn.hoangminh.erp.common.donhang.DonHangStateService; import vn.hoangminh.erp.common.exception.LoiNghiepVuException; import vn.hoangminh.erp.common.permission.*; import vn.hoangminh.erp.common.security.NguoiDungHienTai; import vn.hoangminh.erp.common.sequence.*; import static vn.hoangminh.erp.common.permission.ChucNang.*;
@Service @RequiredArgsConstructor public class Ht3Service {
 private final vn.hoangminh.erp.Ht2Kho.repository.PhieuXuatRepository phieuXuat; private final ChuyenVanRepository chuyen; private final ChiTietChuyenHangRepository chiTiet; private final LoTrinhRepository loTrinh; private final SuCoVanTaiRepository suCo; private final MinhChungGiaoHangRepository minhChung; private final MocHanhTrinhRepository mocHanhTrinh; private final PhuongTienRepository phuongTien; private final TaiXeRepository taiXe; private final vn.hoangminh.erp.Ht4NhanSu.repository.NhanVienRepository nhanVien; private final DonHangRepository donHang; private final vn.hoangminh.erp.Ht1DonHang.repository.HangHoaRepository hangHoa; private final vn.hoangminh.erp.Ht1DonHang.repository.KhachHangRepository khachHang; private final DonHangStateService state; private final MaChungTuService ma; private final Validator validator; private final EntityManager em; private final QuyenService quyen;
 private NguoiDungHienTai nd(ChucNang c){var n=NguoiDungHienTai.hienTai();quyen.yeuCauChucNang(n.vaiTro(),c);return n;} private void valid(Object x){if(!validator.validate(x).isEmpty())throw LoiNghiepVuException.duLieu("loi.dulieu.khonghople");} private <T>T get(Optional<T> o){return o.orElseThrow(()->LoiNghiepVuException.khongTimThay("loi.khongtimthay"));}
 @Transactional(readOnly=true) public List<TaiXeView> taiXe(){nd(HT3_LAP_KE_HOACH_TUYEN);return taiXe.findAll().stream().map(x->new TaiXeView(x.getMaTaiXe(),x.getMaNV(),tenTaiXe(x.getMaTaiXe()),x.getSoGPLX(),x.getLoaiGPLX(),x.getTrangThai())).toList();}
 @Transactional(readOnly=true) public List<PhuongTienView> phuongTien(){nd(HT3_LAP_KE_HOACH_TUYEN);return phuongTien.findAll().stream().map(x->new PhuongTienView(x.getMaPhuongTien(),x.getBienSo(),x.getLoaiXe(),x.getTaiTrong(),x.getTrangThai())).toList();}
 /** Chuyến đã bị huỷ không còn giữ chỗ đơn hàng, mọi trạng thái khác đều tính là đang giữ đơn. */
 private static final Set<String> CHUYEN_HET_HIEU_LUC=Set.of("Đã hủy");
 /** Mã đơn đang được một chuyến khác giữ — dùng để một đơn chỉ lập kế hoạch đúng một lần (mục 1.1). */
 private Set<String> donDaLapChuyen(Integer boQuaChuyen){var ds=new java.util.HashSet<String>();
  for(var c:chuyen.findAll()){if(Objects.equals(c.getMaChuyen(),boQuaChuyen)||CHUYEN_HET_HIEU_LUC.contains(c.getTrangThai()))continue;ds.addAll(donCuaChuyen(c));}
  return ds;}
 @Transactional(readOnly=true) public List<DonHang> donSanSang(){nd(HT3_LAP_KE_HOACH_TUYEN);var daLap=donDaLapChuyen(null);
  return donHang.findAll().stream().filter(d->"Đã nhập kho".equals(d.getTrangThai())&&!daLap.contains(d.getMaDonHang())).toList();}
 /** Đơn được chọn cho một chuyến: đơn còn trống, cộng thêm đơn của chính chuyến đang sửa. */
 @Transactional(readOnly=true) public List<DonHang> donChonDuoc(Integer maChuyen){nd(HT3_LAP_KE_HOACH_TUYEN);var daLap=donDaLapChuyen(maChuyen);
  var cua=maChuyen==null?Set.<String>of():Set.copyOf(donCuaChuyen(get(chuyen.findById(maChuyen))));
  return donHang.findAll().stream().filter(d->cua.contains(d.getMaDonHang())||("Đã nhập kho".equals(d.getTrangThai())&&!daLap.contains(d.getMaDonHang()))).toList();}
 private List<String> donCuaChuyen(ChuyenVan c){var ds=new java.util.LinkedHashSet<String>();if(c.getMaDonHang()!=null)ds.add(c.getMaDonHang());chiTiet.findByIdMaChuyen(c.getMaChuyen()).forEach(x->ds.add(x.getId().getMaDonHang()));return List.copyOf(ds);}
 private String tenTaiXe(String maTaiXe){if(maTaiXe==null)return null;return taiXe.findById(maTaiXe).flatMap(t->nhanVien.findById(t.getMaNV())).map(n->n.getHoTen()).orElse(null);}
 private ChuyenView view(ChuyenVan c){var lt=loTrinh.findByMaChuyen(c.getMaChuyen()).stream().findFirst().orElse(null);var xe=c.getMaPhuongTien()==null?null:phuongTien.findById(c.getMaPhuongTien()).orElse(null);
  return new ChuyenView(c.getMaChuyen(),c.getMaNguoiLap(),c.getMaTaiXe(),tenTaiXe(c.getMaTaiXe()),c.getMaPhuongTien(),xe==null?null:xe.getBienSo(),c.getTrangThai(),c.getNgayKhoiHanh(),c.getThoiGianDuKien(),c.getThoiGianThucTe(),
   lt==null?null:lt.getDiemXuatPhat(),lt==null?null:lt.getDiemKetThuc(),lt==null?null:lt.getKhoangCach(),lt==null?null:lt.getMoTa(),donCuaChuyen(c));}
 @Transactional(readOnly=true) public List<ChuyenView> danhSach(){nd(HT3_TRA_CUU_LICH_SU_GIAO);return chuyen.findAll().stream().map(this::view).toList();}
 /**
  * Nhiệm vụ của tài xế đang đăng nhập — đặc tả 3.3.2: "nhận danh sách điểm giao/nhận, địa chỉ,
  * thời gian yêu cầu, ghi chú hàng hoá, lộ trình tối ưu" chứ không chỉ danh sách mã chuyến.
  */
 @Transactional(readOnly=true) public List<NhiemVuView> nhiemVuCuaToi(){var n=nd(HT3_NHIEM_VU_CUA_TOI);
  // Chuyến đã huỷ không còn là nhiệm vụ; tài xế vẫn xem lại được ở tra cứu và lịch sử cá nhân.
  return chuyenCuaToi(n.maNV()).stream().filter(c->!CHUYEN_HET_HIEU_LUC.contains(c.getTrangThai())).map(c->{var lt=loTrinh.findByMaChuyen(c.getMaChuyen()).stream().findFirst().orElse(null);var xe=c.getMaPhuongTien()==null?null:phuongTien.findById(c.getMaPhuongTien()).orElse(null);
   var dons=donCuaChuyen(c).stream().map(id->donHang.findById(id).orElse(null)).filter(java.util.Objects::nonNull)
     .map(d->new DonNhiemVu(d.getMaDonHang(),d.getNguoiNhan(),d.getSdtNguoiNhan(),d.getDiachiLayHang(),d.getDiachiGiaoHang(),d.getKhoiLuong(),d.getTienCOD(),d.getTrangThai(),
       hangHoa.findByMaDonHangOrderByMaHangHoa(d.getMaDonHang()).stream().map(h->h.getLoaiHangHoa()+" ×"+h.getSoLuong()).collect(java.util.stream.Collectors.joining(", ")))).toList();
   return new NhiemVuView(c.getMaChuyen(),c.getTrangThai(),c.getNgayKhoiHanh(),c.getThoiGianDuKien(),c.getThoiGianThucTe(),
     lt==null?null:lt.getDiemXuatPhat(),lt==null?null:lt.getDiemKetThuc(),lt==null?null:lt.getKhoangCach(),lt==null?null:lt.getMoTa(),
     xe==null?null:xe.getBienSo(),dons,mocHanhTrinh.findByMaChuyenOrderByThoiGianAsc(c.getMaChuyen()).stream().map(this::mocView).toList());}).toList();}
 /** Tra cứu lịch sử giao hàng theo mã vận đơn, biển số xe, khoảng ngày hoặc khách hàng (3.3.2). */
 @Transactional(readOnly=true) public List<TraCuuView> traCuu(String maDonHang,String bienSo,LocalDate tuNgay,LocalDate denNgay,String tuKhoa){nd(HT3_TRA_CUU_LICH_SU_GIAO);
  return chuyen.findAll().stream().flatMap(c->{var xe=c.getMaPhuongTien()==null?null:phuongTien.findById(c.getMaPhuongTien()).orElse(null);var lt=loTrinh.findByMaChuyen(c.getMaChuyen()).stream().findFirst().orElse(null);
    int soSuCo=suCo.findByMaChuyen(c.getMaChuyen()).size();
    var mocChuyen=mocHanhTrinh.findByMaChuyenOrderByThoiGianAsc(c.getMaChuyen());
    return donCuaChuyen(c).stream().map(id->{var d=donHang.findById(id).orElse(null);
      String tenKH=d==null||d.getMaKhachHang()==null?null:khachHang.findById(d.getMaKhachHang()).map(k->k.getTenKhachHang()).orElse(null);
      // Mốc của chính đơn này, kèm cả mốc ghi cho toàn chuyến (maDonHang rỗng).
      var mocDon=mocChuyen.stream().filter(m->m.getMaDonHang()==null||m.getMaDonHang().equals(id)).toList();
      var cuoi=mocDon.isEmpty()?null:mocDon.get(mocDon.size()-1);
      return new TraCuuView(c.getMaChuyen(),id,tenKH,xe==null?null:xe.getBienSo(),tenTaiXe(c.getMaTaiXe()),c.getTrangThai(),d==null?null:d.getTrangThai(),c.getNgayKhoiHanh(),c.getThoiGianThucTe(),lt==null?null:lt.getDiemKetThuc(),soSuCo,
        cuoi==null?null:cuoi.getDiem(),cuoi==null?null:cuoi.getThoiGian(),mocDon.size());});})
   .filter(v->maDonHang==null||maDonHang.isBlank()||v.maDonHang().toLowerCase().contains(maDonHang.toLowerCase().trim()))
   .filter(v->bienSo==null||bienSo.isBlank()||(v.bienSo()!=null&&v.bienSo().toLowerCase().contains(bienSo.toLowerCase().trim())))
   .filter(v->tuNgay==null||(v.ngayKhoiHanh()!=null&&!v.ngayKhoiHanh().toLocalDate().isBefore(tuNgay)))
   .filter(v->denNgay==null||(v.ngayKhoiHanh()!=null&&!v.ngayKhoiHanh().toLocalDate().isAfter(denNgay)))
   .filter(v->tuKhoa==null||tuKhoa.isBlank()||(v.tenKhachHang()!=null&&v.tenKhachHang().toLowerCase().contains(tuKhoa.toLowerCase().trim()))||(v.hoTenTaiXe()!=null&&v.hoTenTaiXe().toLowerCase().contains(tuKhoa.toLowerCase().trim())))
   .toList();}
 private List<ChuyenVan> chuyenCuaToi(String maNV){var cuaToi=taiXe.findAll().stream().filter(t->maNV.equals(t.getMaNV())).map(TaiXe::getMaTaiXe).collect(java.util.stream.Collectors.toSet());return chuyen.findAll().stream().filter(c->c.getMaTaiXe()!=null&&cuaToi.contains(c.getMaTaiXe())).toList();}
 /** Lịch sử giao hàng cá nhân: quãng đường, thời gian làm việc theo từng chuyến (đặc tả 3.3.2). */
 @Transactional(readOnly=true) public List<LichSuGiaoView> lichSuCaNhan(){var n=nd(HT3_LICH_SU_CA_NHAN);
  return chuyenCuaToi(n.maNV()).stream().flatMap(c->{var lt=loTrinh.findByMaChuyen(c.getMaChuyen()).stream().findFirst().orElse(null);
   return donCuaChuyen(c).stream().map(id->new LichSuGiaoView(c.getMaChuyen(),id,c.getTrangThai(),donHang.findById(id).map(DonHang::getTrangThai).orElse(null),
     c.getNgayKhoiHanh(),c.getThoiGianThucTe(),lt==null?null:lt.getDiemXuatPhat(),lt==null?null:lt.getDiemKetThuc(),lt==null?null:lt.getKhoangCach()));}).toList();}
/** Chuyến còn đang soạn thì điều phối sửa hoặc xoá được; đã phân công trở đi thì khoá (mục 1.1). */
 private static final Set<String> CHUYEN_CON_SUA_DUOC=Set.of("Nháp","Cần điều chỉnh");
 /**
  * Kiểm tra chung cho lập và cập nhật kế hoạch (mục 1.1): thời gian hoàn tất phải sau giờ khởi hành,
  * đơn phải đang sẵn ở kho và chưa nằm trong chuyến nào khác — mỗi đơn chỉ lập kế hoạch một lần.
  */
 private List<String> kiemTraKeHoach(ChuyenInput i,Integer boQuaChuyen){
  if(!i.thoiGianDuKien().isAfter(i.ngayKhoiHanh()))throw LoiNghiepVuException.duLieu("ht3.thoigianketthuc");
  var dons=i.maDonHang().stream().map(String::trim).filter(x->!x.isEmpty()).distinct().toList();
  if(dons.isEmpty())throw LoiNghiepVuException.duLieu("ht3.chuachondon");
  var daLap=donDaLapChuyen(boQuaChuyen);var cua=boQuaChuyen==null?Set.<String>of():Set.copyOf(donCuaChuyen(get(chuyen.findById(boQuaChuyen))));
  for(String id:dons){var d=get(donHang.findById(id));
   if(daLap.contains(id))throw LoiNghiepVuException.xungDot("ht3.dondalapchuyen");
   if(!cua.contains(id)&&!"Đã nhập kho".equals(d.getTrangThai()))throw LoiNghiepVuException.xungDot("ht3.donchuasan");}
  return dons;}
 private void ghiLoTrinh(LoTrinh route,Integer maChuyen,ChuyenInput i){route.setMaChuyen(maChuyen);route.setDiemXuatPhat(i.diemXuatPhat());route.setDiemKetThuc(i.diemKetThuc());route.setKhoangCach(i.khoangCach());route.setThoiGianDuKien(i.thoiGianDuKienPhut());route.setMoTa(i.moTa());}
 @Transactional public ChuyenView lapKeHoach(ChuyenInput i){var n=nd(HT3_LAP_KE_HOACH_TUYEN);valid(i);var dons=kiemTraKeHoach(i,null);
  var c=new ChuyenVan();c.setMaNguoiLap(n.maNV());c.setMaDonHang(dons.get(0));c.setNgayKhoiHanh(i.ngayKhoiHanh());c.setThoiGianDuKien(i.thoiGianDuKien());c.setTrangThai("Nháp");em.persist(c);em.flush();
  for(String id:dons){var x=new ChiTietChuyenHang();x.setId(new ChiTietChuyenHang.Id(c.getMaChuyen(),id));em.persist(x);}
  var route=new LoTrinh();ghiLoTrinh(route,c.getMaChuyen(),i);em.persist(route);return view(c);}
 /** Sửa lại chuyến còn ở "Nháp"/"Cần điều chỉnh": đổi đơn, đổi lộ trình và thời gian (mục 1.1). */
 @Transactional public ChuyenView capNhatKeHoach(Integer id,ChuyenInput i){nd(HT3_LAP_KE_HOACH_TUYEN);valid(i);var c=get(chuyen.khoa(id));
  if(!CHUYEN_CON_SUA_DUOC.contains(c.getTrangThai()))throw LoiNghiepVuException.xungDot("ht3.chuyendakhoa");
  var dons=kiemTraKeHoach(i,id);
  chiTiet.deleteAll(chiTiet.findByIdMaChuyen(id));em.flush();
  for(String x:dons){var ct=new ChiTietChuyenHang();ct.setId(new ChiTietChuyenHang.Id(id,x));em.persist(ct);}
  c.setMaDonHang(dons.get(0));c.setNgayKhoiHanh(i.ngayKhoiHanh());c.setThoiGianDuKien(i.thoiGianDuKien());
  var route=loTrinh.findByMaChuyen(id).stream().findFirst().orElseGet(LoTrinh::new);ghiLoTrinh(route,id,i);
  if(route.getMaLoTrinh()==null)em.persist(route);em.flush();return view(c);}
 /** Xoá chuyến lập nhầm khi chưa phân công; đơn hàng được trả lại danh sách chờ lập kế hoạch. */
 @Transactional public void xoaChuyen(Integer id){nd(HT3_LAP_KE_HOACH_TUYEN);var c=get(chuyen.khoa(id));
  if(!CHUYEN_CON_SUA_DUOC.contains(c.getTrangThai()))throw LoiNghiepVuException.xungDot("ht3.chuyendakhoa");
  if(!suCo.findByMaChuyen(id).isEmpty())throw LoiNghiepVuException.xungDot("ht3.chuyencosuco");
  mocHanhTrinh.deleteAll(mocHanhTrinh.findByMaChuyenOrderByThoiGianAsc(id));
  loTrinh.deleteAll(loTrinh.findByMaChuyen(id));
  chiTiet.deleteAll(chiTiet.findByIdMaChuyen(id));
  chuyen.delete(c);}
 @Transactional public ChuyenView phanCong(Integer id,PhanCongInput i){var n=nd(HT3_PHAN_CONG_TAI_XE);valid(i);var c=get(chuyen.khoa(id));var tx=get(taiXe.findById(i.maTaiXe()));var pt=get(phuongTien.findById(i.maPhuongTien()));if(!"Hoạt động".equals(tx.getTrangThai())||tx.getSoGPLX()==null||tx.getSoGPLX().isBlank()||tx.getLoaiGPLX()==null||tx.getLoaiGPLX().isBlank())throw LoiNghiepVuException.duLieu("ht3.thieugplx");if(!"Sẵn sàng".equals(pt.getTrangThai()))throw LoiNghiepVuException.xungDot("ht3.phuongtienban");if(c.getNgayKhoiHanh()!=null&&chuyen.findByMaTaiXe(i.maTaiXe()).stream().anyMatch(x->!Objects.equals(x.getMaChuyen(),id)&&x.getNgayKhoiHanh()!=null&&Math.abs(Duration.between(x.getNgayKhoiHanh(),c.getNgayKhoiHanh()).toHours())<4))throw LoiNghiepVuException.xungDot("ht3.trunglich");c.setMaTaiXe(i.maTaiXe());c.setMaPhuongTien(i.maPhuongTien());c.setTrangThai("Đã phân công");return view(c);}
 @Transactional public ChuyenView pheDuyet(Integer id){var n=nd(HT3_DUYET_DIEU_PHOI);var c=get(chuyen.khoa(id));if(!"Đã phân công".equals(c.getTrangThai()))throw LoiNghiepVuException.xungDot("ht3.chuaphancong");c.setTrangThai("Đã phê duyệt");return view(c);}
 @Transactional public ChuyenView yeuCauDieuChinh(Integer id,String lyDo){var n=nd(HT3_DUYET_DIEU_PHOI);if(lyDo==null||lyDo.isBlank())throw LoiNghiepVuException.duLieu("ht3.lydo");var c=get(chuyen.khoa(id));c.setTrangThai("Cần điều chỉnh");return view(c);}
 @Transactional public ChuyenView nhanDon(String id){var n=nd(HT3_XAC_NHAN_NHAN_DON);var c=get(chuyen.khoa(Integer.valueOf(id)));if(!"Đã phê duyệt".equals(c.getTrangThai()))throw LoiNghiepVuException.xungDot("ht3.chuapheduyet");if(!taiXe.findAll().stream().anyMatch(t->t.getMaTaiXe().equals(c.getMaTaiXe())&&t.getMaNV().equals(n.maNV())))throw LoiNghiepVuException.tuChoiQuyen("quyen.tuchoi");
  // Chuyến có thể gom nhiều đơn: nhận chuyến là nhận hết, trước đây chỉ đơn đầu tiên đổi trạng thái.
  var donNhan=donCuaChuyen(c).stream().sorted().toList();
  for(String x:donNhan){var d=get(donHang.khoa(x));if(!"Đã nhập kho".equals(d.getTrangThai())||!phieuXuat.existsByMaDonHangAndTrangThai(x,"Đã xuất"))throw LoiNghiepVuException.xungDot("ht3.chuabangiaokho");}
  for(String x:donNhan)state.doiTrangThai("HT3",x,"Đang giao",n,"Nhận đơn từ chuyến "+id);
  c.setTrangThai("Đang giao");return view(c);}
 @Transactional public ChuyenView nhanDonHang(String orderId){var c=chuyen.findAll().stream().filter(x->orderId.equals(x.getMaDonHang())||chiTiet.findByIdMaChuyen(x.getMaChuyen()).stream().anyMatch(y->orderId.equals(y.getId().getMaDonHang()))).findFirst().orElseThrow(()->LoiNghiepVuException.khongTimThay("loi.khongtimthay"));return nhanDon(String.valueOf(c.getMaChuyen()));}
 @Transactional public SuCoView baoCaoSuCo(SuCoInput i){var n=nd(HT3_BAO_CAO_SU_CO);valid(i);var c=get(chuyen.findById(i.maChuyen()));var s=new SuCoVanTai();s.setMaChuyen(i.maChuyen());s.setMaDonHang(i.maDonHang()==null||i.maDonHang().isBlank()?null:i.maDonHang());s.setMaTaiXe(c.getMaTaiXe());s.setLoaiSuCo(i.loaiSuCo());s.setMoTa(i.moTa());s.setHinhAnh(i.hinhAnh());s.setThoiGian(LocalDateTime.now());s.setTrangThai("Chờ xử lý");em.persist(s);em.flush();return suCoView(s);}
 /**
  * Điều phối cập nhật trạng thái và phương án xử lý vào chính sự cố đã báo (3.3.2) — trước đây
  * muốn đổi trạng thái phải tạo một sự cố mới, khiến sự cố cũ treo mãi ở trạng thái ban đầu.
  */
 @Transactional public SuCoView xuLySuCo(Integer id,XuLySuCoInput i){var n=nd(HT3_BAO_CAO_SU_CO);valid(i);
  if(!quyen.coChucNang(n.vaiTro(),HT3_LAP_KE_HOACH_TUYEN)&&!quyen.coChucNang(n.vaiTro(),HT3_DUYET_DIEU_PHOI))throw LoiNghiepVuException.tuChoiQuyen("ht3.chidieuphoixuly");
  if(!List.of("Chờ xử lý","Đang xử lý","Đã xử lý").contains(i.trangThai()))throw LoiNghiepVuException.duLieu("ht3.trangthaisuco");
  var s=suCo.findById(id).orElseThrow(()->LoiNghiepVuException.khongTimThay("loi.khongtimthay"));
  s.setTrangThai(i.trangThai());s.setHuongXuLy(i.huongXuLy());if(i.hinhAnh()!=null)s.setHinhAnh(i.hinhAnh().isBlank()?null:i.hinhAnh());s.setNguoiXuLy(n.maNV());s.setThoiGianXuLy(LocalDateTime.now());return suCoView(s);}
 /** Tài xế thu hồi sự cố mình vừa báo khi tình huống đã hết, chỉ khi điều phối chưa xử lý. */
 @Transactional public void thuHoiSuCo(Integer id){var n=nd(HT3_BAO_CAO_SU_CO);
  var s=suCo.findById(id).orElseThrow(()->LoiNghiepVuException.khongTimThay("loi.khongtimthay"));
  boolean cuaToi=s.getMaTaiXe()!=null&&taiXe.findById(s.getMaTaiXe()).map(t->n.maNV().equals(t.getMaNV())).orElse(false);
  boolean dieuPhoi=quyen.coChucNang(n.vaiTro(),HT3_LAP_KE_HOACH_TUYEN)||quyen.coChucNang(n.vaiTro(),HT3_DUYET_DIEU_PHOI);
  if(!cuaToi&&!dieuPhoi)throw LoiNghiepVuException.tuChoiQuyen("quyen.tuchoi");
  if(s.getNguoiXuLy()!=null||"Đã xử lý".equals(s.getTrangThai()))throw LoiNghiepVuException.xungDot("ht3.sucodaxuly");
  suCo.delete(s);}
 /** Ghi một mốc hành trình vào {@code MocHanhTrinh} [G20] — trước đây dữ liệu bị bỏ đi không lưu. */
 @Transactional public MocView moc(Integer id,MocInput i){var n=nd(HT3_CAP_NHAT_HANH_TRINH);valid(i);var c=get(chuyen.khoa(id));if(!"Đang giao".equals(c.getTrangThai()))throw LoiNghiepVuException.xungDot("ht3.chuadanggiao");if(!taiXe.findAll().stream().anyMatch(t->t.getMaTaiXe().equals(c.getMaTaiXe())&&t.getMaNV().equals(n.maNV())))throw LoiNghiepVuException.tuChoiQuyen("quyen.tuchoi");  var dons=donCuaChuyen(c);String don=i.maDonHang()==null||i.maDonHang().isBlank()?(dons.size()==1?dons.get(0):c.getMaDonHang()):i.maDonHang();
  if(don!=null&&!dons.contains(don))throw LoiNghiepVuException.duLieu("ht3.donngoaichuyen");
  var m=new MocHanhTrinh();m.setMaChuyen(id);m.setMaDonHang(don);m.setDiem(i.diem());m.setMoTa(i.moTa());m.setThoiGian(LocalDateTime.now());m.setMaNVCapNhat(n.maNV());em.persist(m);em.flush();return mocView(m);}
 private MocView mocView(MocHanhTrinh m){return new MocView(m.getMaMoc(),m.getMaChuyen(),m.getMaDonHang(),m.getDiem(),m.getMoTa(),m.getThoiGian(),m.getMaNVCapNhat());}
 @Transactional(readOnly=true) public List<MocView> mocHanhTrinh(Integer id){nd(HT3_TRA_CUU_LICH_SU_GIAO);return mocHanhTrinh.findByMaChuyenOrderByThoiGianAsc(id).stream().map(this::mocView).toList();}
 @Transactional public ChuyenView hoanTat(String ma,MinhChungInput i){var n=nd(HT3_XAC_NHAN_GIAO_HANG);valid(i);var d=get(donHang.findById(ma));if(!d.getTrangThai().equals("Đang giao"))throw LoiNghiepVuException.xungDot("ht3.chuadanggiao");
  MinhChungGiaoHang mc=new MinhChungGiaoHang();mc.setMaMinhChung(this.ma.capMa(LoaiChungTu.MINH_CHUNG_GIAO));mc.setMaDonHang(ma);mc.setLoaiMinhChung(MinhChungGiaoHang.Loai.valueOf(i.loaiMinhChung()));mc.setDuongDan(i.duongDan());mc.setMaNVTai(n.maNV());mc.setThoiGianTai(LocalDateTime.now());em.persist(mc);
  state.doiTrangThai("HT3",ma,"Đã giao",n,"Đã tải minh chứng "+i.duongDan());
  var chuaDon=chuyen.findAll().stream().filter(c->donCuaChuyen(c).contains(ma)).toList();
  var cv=get(chuaDon.stream().filter(c->"Đang giao".equals(c.getTrangThai())).findFirst().or(()->chuaDon.stream().findFirst()));
  // Giao xong đơn cuối cùng thì chốt luôn chuyến, nếu không chuyến treo mãi ở "Đang giao"
  // dù mọi đơn đã "Đã giao" (lịch sử cá nhân của tài xế hiển thị sai trạng thái).
  boolean conDangGiao=donCuaChuyen(cv).stream().map(id->donHang.findById(id).map(x->x.getTrangThai()).orElse("")).anyMatch(t->!t.equals("Đã giao")&&!t.equals("Đã đối soát")&&!t.equals("Đã hủy"));
  if(!conDangGiao){cv.setTrangThai("Hoàn thành");cv.setThoiGianThucTe(LocalDateTime.now());}
  return view(cv);}
 private SuCoView suCoView(SuCoVanTai s){return new SuCoView(s.getMaSuCo(),s.getMaChuyen(),s.getMaDonHang(),s.getMaTaiXe(),s.getLoaiSuCo(),s.getMoTa(),s.getThoiGian(),s.getTrangThai(),s.getHinhAnh(),s.getHuongXuLy(),s.getNguoiXuLy(),s.getThoiGianXuLy());}
 @Transactional(readOnly=true) public List<SuCoView> suCo(Integer id){nd(HT3_TRA_CUU_LICH_SU_GIAO);return suCo.findByMaChuyen(id).stream().map(this::suCoView).toList();}
 /** Toàn bộ sự cố đã ghi nhận; tài xế chỉ thấy sự cố trên chuyến của mình (3.3.2). */
 @Transactional(readOnly=true) public List<SuCoView> danhSachSuCo(){var n=nd(HT3_BAO_CAO_SU_CO);
  boolean dieuPhoi=quyen.coChucNang(n.vaiTro(),HT3_DUYET_DIEU_PHOI)||quyen.coChucNang(n.vaiTro(),HT3_LAP_KE_HOACH_TUYEN);
  var cuaToi=dieuPhoi?null:chuyenCuaToi(n.maNV()).stream().map(ChuyenVan::getMaChuyen).collect(java.util.stream.Collectors.toSet());
  return suCo.findAll().stream().filter(s->cuaToi==null||cuaToi.contains(s.getMaChuyen())).sorted(java.util.Comparator.comparing(SuCoVanTai::getThoiGian,java.util.Comparator.nullsLast(java.util.Comparator.reverseOrder()))).map(this::suCoView).toList();}
}
