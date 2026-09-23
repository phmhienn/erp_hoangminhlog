package vn.hoangminh.erp.Ht2Kho.service;
import jakarta.persistence.EntityManager; import jakarta.validation.Validator; import lombok.RequiredArgsConstructor; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import java.time.*; import java.util.*; import java.util.stream.Collectors;
import vn.hoangminh.erp.Ht1DonHang.domain.DonHang; import vn.hoangminh.erp.Ht1DonHang.repository.DonHangRepository; import vn.hoangminh.erp.Ht1DonHang.repository.HangHoaRepository; import vn.hoangminh.erp.common.audit.LichSuDonHangService; import vn.hoangminh.erp.common.donhang.DonHangStateService; import vn.hoangminh.erp.common.exception.LoiNghiepVuException; import vn.hoangminh.erp.common.permission.*; import vn.hoangminh.erp.common.security.NguoiDungHienTai; import vn.hoangminh.erp.common.sequence.*; import vn.hoangminh.erp.Ht2Kho.domain.*; import vn.hoangminh.erp.Ht2Kho.dto.Ht2Dto.*; import vn.hoangminh.erp.Ht2Kho.repository.*; import static vn.hoangminh.erp.common.permission.ChucNang.*;
@Service @RequiredArgsConstructor public class Ht2Service {
 private final PhieuNhapRepository phieuNhap; private final PhieuXuatRepository phieuXuat; private final PhieuKiemKeRepository kiemKe; private final KiemKeDonHangRepository kiemKeDon; private final BienBanSuCoRepository bienBan; private final DonHangRepository donHang; private final HangHoaRepository hangHoa; private final vn.hoangminh.erp.Ht1DonHang.repository.KhachHangRepository khachHang; private final LichSuDonHangService lichSu; private final DonHangStateService state; private final MaChungTuService ma; private final TaoChungTuTransaction tao; private final QuyenService quyen; private final Validator validator; private final EntityManager em;
 private NguoiDungHienTai nd(ChucNang c){var n=NguoiDungHienTai.hienTai();quyen.yeuCauChucNang(n.vaiTro(),c);return n;} private void valid(Object o){if(!validator.validate(o).isEmpty())throw LoiNghiepVuException.duLieu("loi.dulieu.khonghople");} private void exists(boolean ok){if(!ok)throw LoiNghiepVuException.khongTimThay("loi.khongtimthay");}
 /** Cho qua nếu vai trò có ít nhất một chức năng — dùng cho dữ liệu tồn kho mà cả hai vai trò đều cần. */
 private NguoiDungHienTai ndMot(ChucNang... cs){var n=NguoiDungHienTai.hienTai();for(ChucNang c:cs)if(quyen.coChucNang(n.vaiTro(),c))return n;throw LoiNghiepVuException.tuChoiQuyen("quyen.tuchoi");}
/** Quy đổi đơn hàng sang khung nhìn của kho, kèm tên khách hàng và danh sách mặt hàng. */
 private DonKhoView donKho(DonHang d){String ten=d.getMaKhachHang()==null?null:khachHang.findById(d.getMaKhachHang()).map(k->k.getTenKhachHang()).orElse(null);
  String mh=hangHoa.findByMaDonHangOrderByMaHangHoa(d.getMaDonHang()).stream().map(h->h.getLoaiHangHoa()+" ×"+h.getSoLuong()).collect(Collectors.joining(", "));
  return new DonKhoView(d.getMaDonHang(),d.getMaKhachHang(),ten,d.getNguoiNhan(),d.getSdtNguoiNhan(),d.getDiachiGiaoHang(),d.getKhoiLuong(),d.getTienCOD(),d.getTrangThai(),d.getNgayTao()==null?null:d.getNgayTao().toLocalDate(),mh);}
 private boolean khopDon(DonKhoView v,String tuKhoa){if(tuKhoa==null||tuKhoa.isBlank())return true;String k=tuKhoa.toLowerCase().trim();
  return v.maDonHang().toLowerCase().contains(k)||(v.tenKhachHang()!=null&&v.tenKhachHang().toLowerCase().contains(k))||(v.nguoiNhan()!=null&&v.nguoiNhan().toLowerCase().contains(k))||(v.matHang()!=null&&v.matHang().toLowerCase().contains(k));}
 /** Đơn đã được HT1 duyệt, đang chờ nhập kho — nguồn tra cứu đơn hàng của màn lập phiếu nhập (3.2.3). */
 @Transactional(readOnly=true) public List<DonKhoView> donChoNhap(String tuKhoa){ndMot(HT2_TRA_CUU_DON_NHAP_KHO,HT2_LAP_PHIEU_NHAP);
  return donHang.findAll().stream().filter(d->d.getTrangThai().equals("Đã tạo")&&lichSu.kiemDuyet(d.getMaDonHang()).equals("Đã duyệt")).map(this::donKho).filter(v->khopDon(v,tuKhoa)).toList();}
 /** Đơn đã nhập kho, đủ điều kiện lập phiếu xuất — nguồn tra cứu đơn hàng của màn lập phiếu xuất. */
 @Transactional(readOnly=true) public List<DonKhoView> donChoXuat(String tuKhoa){ndMot(HT2_TRA_CUU_DON_NHAP_KHO,HT2_LAP_PHIEU_XUAT);
  return donHang.findAll().stream().filter(d->dangTrongKho(d)).map(this::donKho).filter(v->khopDon(v,tuKhoa)).toList();}
 /** Quản lý kho xem toàn bộ phiếu; nhân viên kho chỉ xem phiếu do chính mình lập. */
 @Transactional(readOnly=true) public List<PhieuNhapView> danhSachNhap(String trangThai){var n=ndMot(HT2_PHE_DUYET_PHIEU_NHAP,HT2_LAP_PHIEU_NHAP);
  boolean quanLy=quyen.coChucNang(n.vaiTro(),HT2_PHE_DUYET_PHIEU_NHAP);
  return phieuNhap.findAll().stream().filter(p->p.getMaDonHang()!=null).filter(p->quanLy||n.maNV().equals(p.getMaNV()))
    .filter(p->trangThai==null||trangThai.isBlank()||p.getTrangThai().equals(trangThai)).map(this::nhapView).toList();}
 private String matHang(String id){return id==null?"":donHang.findById(id).map(this::donKho).map(DonKhoView::matHang).orElse("");}
 private PhieuNhapView nhapView(PhieuNhap p){return new PhieuNhapView(p.getMaPhieuNhap(),p.getMaDonHang(),p.getMaNV(),p.getNgayNhap(),p.getTrangThai(),p.getGhiChu(),matHang(p.getMaDonHang()));}
 private DonHang khoaDon(String id){if(id==null||id.isBlank())throw LoiNghiepVuException.duLieu("ht2.thieumadon");return donHang.khoa(id).orElseThrow(()->LoiNghiepVuException.khongTimThay("loi.khongtimthay"));}
 private void kiemDonNhap(DonHang d){if(!"Đã tạo".equals(d.getTrangThai())||!"Đã duyệt".equals(lichSu.kiemDuyet(d.getMaDonHang())))throw LoiNghiepVuException.xungDot("ht2.donchuaduyet");}
 private void kiemTrungNhap(String don,String boQua){if(phieuNhap.findAll().stream().anyMatch(p->don.equals(p.getMaDonHang())&&!Objects.equals(boQua,p.getMaPhieuNhap())&&!"Từ chối".equals(p.getTrangThai())))throw LoiNghiepVuException.xungDot("ht2.dontrungphieu");}
 private void kiemTrungXuat(String don,String boQua){if(phieuXuat.findAll().stream().anyMatch(p->don.equals(p.getMaDonHang())&&!Objects.equals(boQua,p.getMaPhieuXuat())&&!"Từ chối".equals(p.getTrangThai())))throw LoiNghiepVuException.xungDot("ht2.dontrungphieu");}
 private boolean daXuat(String don){return phieuXuat.existsByMaDonHangAndTrangThai(don,"Đã xuất");}
 private boolean dangTrongKho(DonHang d){return "Đã nhập kho".equals(d.getTrangThai())&&!daXuat(d.getMaDonHang());}
 private void chuPhieu(String nv){var n=NguoiDungHienTai.hienTai();if(!Objects.equals(n.maNV(),nv)&&!quyen.coChucNang(n.vaiTro(),HT2_PHE_DUYET_PHIEU_NHAP))throw LoiNghiepVuException.tuChoiQuyen("quyen.tuchoi");}
 @Transactional public PhieuNhapView taoNhap(NhapInput i){var n=nd(HT2_LAP_PHIEU_NHAP);valid(i);var d=khoaDon(i.maDonHang());kiemDonNhap(d);kiemTrungNhap(d.getMaDonHang(),null);
  return tao.thucHien(()->{var p=new PhieuNhap();p.setMaPhieuNhap(ma.capMa(LoaiChungTu.PHIEU_NHAP));p.setMaDonHang(d.getMaDonHang());p.setMaNV(n.maNV());p.setNgayNhap(i.ngayNhap());p.setTrangThai("Lưu tạm");p.setGhiChu(i.ghiChu());em.persist(p);em.flush();return nhapView(p);});}
 @Transactional public PhieuNhapView capNhatNhap(String id,NhapInput i){nd(HT2_CAP_NHAT_PHIEU);valid(i);var p=phieuNhap.khoa(id).orElseThrow(()->LoiNghiepVuException.khongTimThay("loi.khongtimthay"));chuPhieu(p.getMaNV());
  if(!Set.of("Lưu tạm","Từ chối").contains(p.getTrangThai()))throw LoiNghiepVuException.xungDot("ht2.phieukhoasua");
  if(!Objects.equals(p.getMaDonHang(),i.maDonHang()))throw LoiNghiepVuException.duLieu("ht2.khongdoidon");
  var d=khoaDon(i.maDonHang());kiemDonNhap(d);kiemTrungNhap(i.maDonHang(),id);
  p.setNgayNhap(i.ngayNhap());p.setMaNCC(null);p.setTongTien(null);p.setGhiChu(i.ghiChu());p.setTrangThai("Lưu tạm");return nhapView(p);}
 @Transactional public PhieuNhapView guiDuyet(String id){nd(HT2_LAP_PHIEU_NHAP);var p=phieuNhap.khoa(id).orElseThrow(()->LoiNghiepVuException.khongTimThay("loi.khongtimthay"));chuPhieu(p.getMaNV());kiemDonNhap(khoaDon(p.getMaDonHang()));kiemTrungNhap(p.getMaDonHang(),id);if(!p.getTrangThai().equals("Lưu tạm"))throw LoiNghiepVuException.xungDot("loi.trangthai.xungdot");p.setTrangThai("Chờ duyệt");return nhapView(p);}
 @Transactional public PhieuNhapView duyet(String id){var n=nd(HT2_PHE_DUYET_PHIEU_NHAP);var p=phieuNhap.khoa(id).orElseThrow(()->LoiNghiepVuException.khongTimThay("loi.khongtimthay"));
  if(!"Chờ duyệt".equals(p.getTrangThai()))throw LoiNghiepVuException.xungDot("ht2.chopheduyet");var d=khoaDon(p.getMaDonHang());kiemDonNhap(d);
  state.doiTrangThai("HT2",d.getMaDonHang(),"Đã nhập kho",n,"Tiếp nhận đơn vào kho: "+id);p.setTrangThai("Đã duyệt");p.setNguoiDuyet(n.maNV());return nhapView(p);}
 @Transactional public PhieuNhapView tuChoi(String id,String lyDo){var n=nd(HT2_PHE_DUYET_PHIEU_NHAP);if(lyDo==null||lyDo.isBlank()||lyDo.length()>255)throw LoiNghiepVuException.duLieu("ht2.lydo");var p=phieuNhap.khoa(id).orElseThrow(()->LoiNghiepVuException.khongTimThay("loi.khongtimthay"));if(!p.getTrangThai().equals("Chờ duyệt"))throw LoiNghiepVuException.xungDot("ht2.chopheduyet");p.setTrangThai("Từ chối");p.setGhiChu(lyDo);return nhapView(p);}
 @Transactional(readOnly=true) public List<DonKhoView> tonKho(){ndMot(HT2_DASHBOARD_NHAP_XUAT,HT2_KIEM_KE,HT2_LAP_PHIEU_XUAT);return donHang.findAll().stream().filter(this::dangTrongKho).map(this::donKho).toList();}
 private PhieuXuatView xuatView(PhieuXuat p){return new PhieuXuatView(p.getMaPhieuXuat(),p.getMaDonHang(),p.getMaNV(),p.getNgayXuat(),p.getTrangThai(),p.getLyDoXuat(),p.getGhiChu(),p.getNguoiDuyet(),matHang(p.getMaDonHang()));}
 private void kiemDonXuat(String id){if(!dangTrongKho(khoaDon(id)))throw LoiNghiepVuException.xungDot("ht2.donchuadanhap");}
 @Transactional public PhieuXuatView taoXuat(XuatInput i){var n=nd(HT2_LAP_PHIEU_XUAT);valid(i);kiemDonXuat(i.maDonHang());kiemTrungXuat(i.maDonHang(),null);
  return tao.thucHien(()->{var p=new PhieuXuat();p.setMaPhieuXuat(ma.capMa(LoaiChungTu.PHIEU_XUAT));p.setMaNV(n.maNV());p.setMaDonHang(i.maDonHang());p.setNgayXuat(i.ngayXuat());p.setLyDoXuat(i.lyDoXuat());p.setGhiChu(i.ghiChu());p.setTrangThai("Lưu tạm");em.persist(p);em.flush();return xuatView(p);});}
 @Transactional public PhieuXuatView capNhatXuat(String id,XuatInput i){nd(HT2_CAP_NHAT_PHIEU);valid(i);var p=phieuXuat.khoa(id).orElseThrow(()->LoiNghiepVuException.khongTimThay("loi.khongtimthay"));chuPhieu(p.getMaNV());
  if(!Set.of("Lưu tạm","Từ chối").contains(p.getTrangThai()))throw LoiNghiepVuException.xungDot("ht2.phieukhoasua");
  if(!Objects.equals(p.getMaDonHang(),i.maDonHang()))throw LoiNghiepVuException.duLieu("ht2.khongdoidon");
  kiemDonXuat(i.maDonHang());kiemTrungXuat(i.maDonHang(),id);p.setNgayXuat(i.ngayXuat());p.setLyDoXuat(i.lyDoXuat());p.setGhiChu(i.ghiChu());p.setTongTien(null);p.setTrangThai("Lưu tạm");return xuatView(p);}
 @Transactional public PhieuXuatView guiDuyetXuat(String id){nd(HT2_LAP_PHIEU_XUAT);var p=phieuXuat.khoa(id).orElseThrow(()->LoiNghiepVuException.khongTimThay("loi.khongtimthay"));chuPhieu(p.getMaNV());
  if(!"Lưu tạm".equals(p.getTrangThai()))throw LoiNghiepVuException.xungDot("loi.trangthai.xungdot");kiemDonXuat(p.getMaDonHang());kiemTrungXuat(p.getMaDonHang(),id);p.setTrangThai("Chờ duyệt");return xuatView(p);}
 @Transactional public PhieuXuatView duyetXuat(String id){var n=nd(HT2_PHE_DUYET_PHIEU_NHAP);var p=phieuXuat.khoa(id).orElseThrow(()->LoiNghiepVuException.khongTimThay("loi.khongtimthay"));
  if(!"Chờ duyệt".equals(p.getTrangThai()))throw LoiNghiepVuException.xungDot("ht2.chopheduyet");kiemDonXuat(p.getMaDonHang());
  p.setTrangThai("Đã xuất");p.setNguoiDuyet(n.maNV());lichSu.ghi(p.getMaDonHang(),"XuatKho","Đã xuất",n.maTaiKhoan(),"Bàn giao nguyên đơn theo phiếu "+id);return xuatView(p);}
 @Transactional public PhieuXuatView tuChoiXuat(String id,String lyDo){nd(HT2_PHE_DUYET_PHIEU_NHAP);
  if(lyDo==null||lyDo.isBlank()||lyDo.length()>255)throw LoiNghiepVuException.duLieu("ht2.lydo");
  var p=phieuXuat.khoa(id).orElseThrow(()->LoiNghiepVuException.khongTimThay("loi.khongtimthay"));
  if(!p.getTrangThai().equals("Chờ duyệt"))throw LoiNghiepVuException.xungDot("ht2.chopheduyet");
  p.setTrangThai("Từ chối");p.setGhiChu(lyDo);return xuatView(p);}
 @Transactional public KiemKeView taoKiemKe(KiemKeInput i){var n=nd(HT2_KIEM_KE);valid(i);
  if(i.chiTiet().stream().map(KiemKeChiTiet::maDonHang).distinct().count()!=i.chiTiet().size())throw LoiNghiepVuException.duLieu("loi.dulieu.khonghople");
  return tao.thucHien(()->{var p=new PhieuKiemKe();p.setMaPhieuKiemKe(ma.capMa(LoaiChungTu.PHIEU_KIEM_KE));p.setMaNV(n.maNV());p.setNgayKiemKe(i.ngayKiemKe());p.setKhuVucKiemKe(i.khuVucKiemKe());p.setGhiChu(i.ghiChu());p.setTrangThai("Đã chốt");em.persist(p);
   for(var x:i.chiTiet().stream().sorted(Comparator.comparing(KiemKeChiTiet::maDonHang)).toList()){var d=khoaDon(x.maDonHang());int stock=dangTrongKho(d)?1:0;
    var c=new KiemKeDonHang();c.setId(new KiemKeDonHang.Id(p.getMaPhieuKiemKe(),x.maDonHang()));c.setSoLuongHeThong(stock);c.setSoLuongThucTe(x.soLuongThucTe());c.setChenhLech(x.soLuongThucTe()-stock);c.setGhiChu(x.ghiChu());em.persist(c);}
   em.flush();return kkView(p);});}
 private KiemKeView kkView(PhieuKiemKe p){return new KiemKeView(p.getMaPhieuKiemKe(),p.getNgayKiemKe(),p.getKhuVucKiemKe(),p.getTrangThai(),p.getGhiChu(),kiemKeDon.findByIdMaPhieuKiemKe(p.getMaPhieuKiemKe()).stream().map(c->new KiemKeChiTietView(c.getId().getMaDonHang(),c.getSoLuongHeThong(),c.getSoLuongThucTe(),c.getChenhLech(),c.getGhiChu())).toList());}
 /** Luồng phụ bước 4 của đặc tả 3.2.3: hàng thừa/thiếu/hư hỏng thì lập biên bản thay vì nhập kho. */
 @Transactional public BienBanView lapBienBan(BienBanInput i){var n=nd(HT2_LAP_BIEN_BAN_SU_CO);valid(i);exists(donHang.existsById(i.maDonHang()));if(i.maPhieuNhap()!=null&&!i.maPhieuNhap().isBlank()){var p=phieuNhap.findById(i.maPhieuNhap()).orElseThrow(()->LoiNghiepVuException.khongTimThay("loi.khongtimthay"));if(!Objects.equals(p.getMaDonHang(),i.maDonHang()))throw LoiNghiepVuException.duLieu("ht2.bienbankhacdon");}var b=new BienBanSuCo();b.setMaBienBan(ma.capMa(LoaiChungTu.BIEN_BAN_SU_CO));b.setMaPhieuNhap(i.maPhieuNhap()==null||i.maPhieuNhap().isBlank()?null:i.maPhieuNhap());b.setMaDonHang(i.maDonHang()==null||i.maDonHang().isBlank()?null:i.maDonHang());b.setLoaiSuCo(i.loaiSuCo());b.setMoTa(i.moTa());b.setDuongDanAnh(i.duongDanAnh());b.setMaNVLap(n.maNV());b.setNgayLap(LocalDateTime.now());return bb(bienBan.save(b));}
 private BienBanView bb(BienBanSuCo b){return new BienBanView(b.getMaBienBan(),b.getMaPhieuNhap(),b.getMaDonHang(),b.getLoaiSuCo(),b.getMoTa(),b.getDuongDanAnh(),b.getMaNVLap(),b.getNgayLap());}
 @Transactional(readOnly=true) public List<BienBanView> danhSachBienBan(){nd(HT2_LAP_BIEN_BAN_SU_CO);return bienBan.findByOrderByNgayLapDesc().stream().map(this::bb).toList();}
 @Transactional(readOnly=true) public List<PhieuXuatView> danhSachXuat(){var n=ndMot(HT2_LAP_PHIEU_XUAT,HT2_PHE_DUYET_PHIEU_NHAP);
  boolean quanLy=quyen.coChucNang(n.vaiTro(),HT2_PHE_DUYET_PHIEU_NHAP);
  return phieuXuat.findAll().stream().filter(p->p.getMaDonHang()!=null).filter(p->quanLy||n.maNV().equals(p.getMaNV())).map(this::xuatView).toList();}
 @Transactional(readOnly=true) public List<KiemKeView> danhSachKiemKe(){nd(HT2_KIEM_KE);return kiemKe.findAll().stream().map(this::kkView).filter(p->!p.chiTiet().isEmpty()).toList();}
 @Transactional(readOnly=true) public Dashboard dashboard(){nd(HT2_DASHBOARD_NHAP_XUAT);var ds=donHang.findAll();return new Dashboard(ds.stream().filter(d->"Đã tạo".equals(d.getTrangThai())&&"Đã duyệt".equals(lichSu.kiemDuyet(d.getMaDonHang()))).count(),ds.stream().filter(this::dangTrongKho).count(),ds.stream().filter(d->"Đã nhập kho".equals(d.getTrangThai())&&daXuat(d.getMaDonHang())).count(),phieuNhap.countByTrangThai("Chờ duyệt"),phieuXuat.findAll().stream().filter(p->"Chờ duyệt".equals(p.getTrangThai())).count());}
 @Transactional(readOnly=true) public BaoCao baoCao(LocalDate a,LocalDate b){nd(HT2_BAO_CAO_NHAP_KHO);if(a==null)a=LocalDate.now().withDayOfMonth(1);if(b==null)b=LocalDate.now();if(a.isAfter(b))throw LoiNghiepVuException.duLieu("ht2.tieuchi");
  var pn=phieuNhap.findByNgayNhapBetween(a,b).stream().filter(p->p.getMaDonHang()!=null).toList();var px=phieuXuat.findByNgayXuatBetween(a,b).stream().filter(p->p.getMaDonHang()!=null).toList();
  return new BaoCao(a,b,pn.size(),pn.stream().filter(p->"Đã duyệt".equals(p.getTrangThai())).map(PhieuNhap::getMaDonHang).distinct().count(),px.size(),px.stream().filter(p->"Đã xuất".equals(p.getTrangThai())).map(PhieuXuat::getMaDonHang).distinct().count(),pn.stream().collect(Collectors.groupingBy(PhieuNhap::getTrangThai,TreeMap::new,Collectors.counting())),pn.stream().filter(p->"Đã duyệt".equals(p.getTrangThai())).collect(Collectors.groupingBy(p->p.getNgayNhap().toString(),TreeMap::new,Collectors.counting())),px.stream().collect(Collectors.groupingBy(PhieuXuat::getTrangThai,TreeMap::new,Collectors.counting())),px.stream().filter(p->"Đã xuất".equals(p.getTrangThai())).collect(Collectors.groupingBy(p->p.getNgayXuat().toString(),TreeMap::new,Collectors.counting())));}
 /**
  * Tra cứu gộp đơn hàng chờ nhập, phiếu nhập và phiếu xuất theo mã đơn, tên khách hàng,
  * khoảng ngày hoặc mã nhân viên thực hiện (3.2.2 vai trò nhân viên kho — "Tra cứu lịch sử").
  */
 @Transactional(readOnly=true) public List<TraCuuView> traCuu(String maDonHang,String tuKhoa,LocalDate tuNgay,LocalDate denNgay,String maNV,String loai){nd(HT2_TRA_CUU_DON_NHAP_KHO);
  var khach=new java.util.HashMap<String,String>();donHang.findAll().forEach(d->khach.put(d.getMaDonHang(),d.getMaKhachHang()));
  List<TraCuuView> kq=new java.util.ArrayList<>();
  if(loai==null||loai.isBlank()||loai.equals("Đơn chờ nhập"))
   for(var d:donHang.findAll()) if(d.getTrangThai().equals("Đã tạo")&&lichSu.kiemDuyet(d.getMaDonHang()).equals("Đã duyệt"))
    kq.add(new TraCuuView("Đơn chờ nhập",d.getMaDonHang(),d.getMaDonHang(),d.getMaKhachHang(),d.getNgayTao().toLocalDate(),d.getTrangThai(),null,matHang(d.getMaDonHang())));
  if(loai==null||loai.isBlank()||loai.equals("Phiếu nhập"))
   for(var p:phieuNhap.findAll()) if(p.getMaDonHang()!=null)
    kq.add(new TraCuuView("Phiếu nhập",p.getMaPhieuNhap(),p.getMaDonHang(),khach.get(p.getMaDonHang()),p.getNgayNhap(),p.getTrangThai(),p.getMaNV(),
      donHang.findById(p.getMaDonHang()).map(this::donKho).map(DonKhoView::matHang).orElse("")));
  if(loai==null||loai.isBlank()||loai.equals("Phiếu xuất"))
   for(var p:phieuXuat.findAll()) if(p.getMaDonHang()!=null)
    kq.add(new TraCuuView("Phiếu xuất",p.getMaPhieuXuat(),p.getMaDonHang(),khach.get(p.getMaDonHang()),p.getNgayXuat(),p.getTrangThai(),p.getMaNV(),
      donHang.findById(p.getMaDonHang()).map(this::donKho).map(DonKhoView::matHang).orElse("")));
  var ten=new java.util.HashMap<String,String>();khachHang.findAll().forEach(k->ten.put(k.getMaKhachHang(),k.getTenKhachHang()));
  return kq.stream()
   .map(v->new TraCuuView(v.loai(),v.ma(),v.maDonHang(),v.tenKhachHang()==null?null:ten.getOrDefault(v.tenKhachHang(),v.tenKhachHang()),v.ngay(),v.trangThai(),v.maNV(),v.matHang()))
   .filter(v->maDonHang==null||maDonHang.isBlank()||(v.maDonHang()!=null&&v.maDonHang().toLowerCase().contains(maDonHang.toLowerCase().trim()))||v.ma().toLowerCase().contains(maDonHang.toLowerCase().trim()))
   .filter(v->tuKhoa==null||tuKhoa.isBlank()||(v.tenKhachHang()!=null&&v.tenKhachHang().toLowerCase().contains(tuKhoa.toLowerCase().trim())))
   .filter(v->tuNgay==null||(v.ngay()!=null&&!v.ngay().isBefore(tuNgay)))
   .filter(v->denNgay==null||(v.ngay()!=null&&!v.ngay().isAfter(denNgay)))
   .filter(v->maNV==null||maNV.isBlank()||(v.maNV()!=null&&v.maNV().toLowerCase().contains(maNV.toLowerCase().trim())))
   .sorted(Comparator.comparing(TraCuuView::ngay,Comparator.nullsLast(Comparator.reverseOrder()))).toList();}
}
