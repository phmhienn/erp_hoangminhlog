package vn.hoangminh.erp.Ht5ThuChiCod.service; import jakarta.persistence.EntityManager; import jakarta.validation.Validator; import java.math.*; import java.time.*; import java.util.*; import java.util.stream.Collectors; import lombok.RequiredArgsConstructor; import org.springframework.context.event.EventListener; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import vn.hoangminh.erp.Ht5ThuChiCod.domain.*; import vn.hoangminh.erp.Ht5ThuChiCod.dto.Ht5Dto.*; import vn.hoangminh.erp.Ht5ThuChiCod.repository.*; import vn.hoangminh.erp.Ht1DonHang.repository.DonHangRepository; import vn.hoangminh.erp.Ht1DonHang.repository.KhachHangRepository; import vn.hoangminh.erp.common.donhang.DonHangDoiTrangThaiEvent; import vn.hoangminh.erp.common.donhang.DonHangStateService; import vn.hoangminh.erp.common.exception.LoiNghiepVuException; import vn.hoangminh.erp.common.permission.*; import vn.hoangminh.erp.common.security.NguoiDungHienTai; import vn.hoangminh.erp.common.sequence.*;
@Service @RequiredArgsConstructor public class Ht5Service {private final GiaoDichCODRepository gd;private final PhieuThuChiRepository pt;private final SoQuyRepository sq;private final DoiSoatCODRepository ds;private final ChiTietDoiSoatRepository ctds;private final CongNoRepository cn;private final DonHangRepository dh;private final KhachHangRepository kh;private final DonHangStateService state;private final MaChungTuService ma;private final QuyenService quyen;private final Validator validator;private final EntityManager em; private NguoiDungHienTai nd(ChucNang c){var n=NguoiDungHienTai.hienTai();quyen.yeuCauChucNang(n.vaiTro(),c);return n;} private NguoiDungHienTai ndMot(ChucNang... cs){var n=NguoiDungHienTai.hienTai();for(ChucNang c:cs)if(quyen.coChucNang(n.vaiTro(),c))return n;throw LoiNghiepVuException.tuChoiQuyen("quyen.tuchoi");} private <T>T get(Optional<T> o){return o.orElseThrow(()->LoiNghiepVuException.khongTimThay("loi.khongtimthay"));} private void valid(Object x){if(!validator.validate(x).isEmpty())throw LoiNghiepVuException.duLieu("loi.dulieu.khonghople");} private static BigDecimal nz(BigDecimal x){return x==null?BigDecimal.ZERO:x;}
 private GiaoDichView view(GiaoDichCOD x){var d=dh.findById(x.getMaDonHang()).orElse(null);
  String ten=d==null||d.getMaKhachHang()==null?null:kh.findById(d.getMaKhachHang()).map(k->k.getTenKhachHang()).orElse(null);
  BigDecimal lech=x.getSoTienThucThu()==null?null:nz(x.getSoTienPhaiThu()).subtract(x.getSoTienThucThu());
  return new GiaoDichView(x.getMaGiaoDich(),x.getMaDonHang(),ten,d==null?null:d.getTrangThai(),x.getSoTienPhaiThu(),x.getSoTienThucThu(),lech,x.getTrangThai(),x.getNgayTao(),x.getNguoiCapNhat(),x.getLyDoSaiLech(),x.getNguoiXuLySaiLech(),x.getThoiGianXuLySaiLech());}
 /**
  * 3.5.1 bước 1: "hệ thống tiếp nhận dữ liệu đơn hàng đã giao và số tiền COD cần thu từ các phân hệ
  * liên quan" — là việc của hệ thống, không phải usecase của actor nào, nên chạy ngầm khi mở màn
  * thay vì để người dùng bấm nút tạo tay.
  */
 private void dongBoTuDonDaGiao(){var daCo=gd.findAll().stream().map(GiaoDichCOD::getMaDonHang).collect(Collectors.toSet());
  for(var d:dh.findAll())if(("Đã giao".equals(d.getTrangThai())||"Đã đối soát".equals(d.getTrangThai()))&&d.getTienCOD()!=null&&d.getTienCOD().signum()>0&&!daCo.contains(d.getMaDonHang()))sinh(d.getMaDonHang(),d.getTienCOD());}
 /** Tra cứu COD theo trạng thái thu/chi hoặc từ khoá mã đơn/mã giao dịch/tên khách hàng (3.5.2). */
 @Transactional public List<GiaoDichView> giaoDich(String tt,String tuKhoa){
  ndMot(ChucNang.HT5_TRA_CUU_COD,ChucNang.HT5_CAP_NHAT_TRANG_THAI_GD,ChucNang.HT5_DOI_SOAT_COD,ChucNang.HT5_QUAN_LY_SAI_LECH,ChucNang.HT5_LICH_SU_GIAO_DICH,ChucNang.HT5_BAO_CAO_THU_CHI);
  dongBoTuDonDaGiao();
  return (tt==null||tt.isBlank()?gd.findAll():gd.findByTrangThai(tt)).stream().map(this::view)
   .filter(v->tuKhoa==null||tuKhoa.isBlank()||v.maDonHang().toLowerCase().contains(tuKhoa.toLowerCase().trim())||v.maGiaoDich().toLowerCase().contains(tuKhoa.toLowerCase().trim())||(v.tenKhachHang()!=null&&v.tenKhachHang().toLowerCase().contains(tuKhoa.toLowerCase().trim())))
   .toList();}
 /** Sinh giao dịch COD cho một đơn; dùng chung cho thao tác tay và luồng tự động khi đơn "Đã giao". */
 private GiaoDichCOD sinh(String order,BigDecimal tienCod){var x=new GiaoDichCOD();x.setMaGiaoDich(ma.capMa(LoaiChungTu.GIAO_DICH_COD));x.setMaDonHang(order);x.setSoTienPhaiThu(tienCod);x.setTrangThai("Chưa thu");x.setNgayTao(LocalDate.now());return gd.save(x);}
 /**
  * Mục 2.4.5 / 3.5.1: COD "tự động tổng hợp khi đơn hàng chuyển trạng thái đã giao thành công".
  * Chạy trong cùng giao dịch của HT3 nên hoặc cùng thành công, hoặc cùng rollback.
  */
 @EventListener public void khiDonDoiTrangThai(DonHangDoiTrangThaiEvent e){if(!"Đã giao".equals(e.trangThaiMoi()))return;var d=dh.findById(e.maDonHang()).orElse(null);if(d==null||d.getTienCOD()==null||d.getTienCOD().signum()<=0)return;if(!gd.findByMaDonHang(e.maDonHang()).isEmpty())return;sinh(e.maDonHang(),d.getTienCOD());}
 /** Usecase "Quản lý sai lệch COD" (3.5.3): danh sách giao dịch thực thu khác phải thu. */
 @Transactional(readOnly=true) public List<GiaoDichView> saiLech(){nd(ChucNang.HT5_QUAN_LY_SAI_LECH);
  return gd.findAll().stream().map(this::view).filter(v->v.chenhLech()!=null&&v.chenhLech().signum()!=0).toList();}
 /** Ghi nhận nguyên nhân sai lệch và, nếu đã xử lý xong, đưa giao dịch về trạng thái phù hợp. */
 @Transactional public GiaoDichView xuLySaiLech(String id,SaiLechInput i){var n=nd(ChucNang.HT5_QUAN_LY_SAI_LECH);valid(i);
  var x=get(gd.findById(id));if("Đã đối soát".equals(x.getTrangThai()))throw LoiNghiepVuException.xungDot("ht5.gd.dadoisoat");
  if(x.getSoTienThucThu()==null||nz(x.getSoTienPhaiThu()).compareTo(x.getSoTienThucThu())==0)throw LoiNghiepVuException.duLieu("ht5.gd.khonglech");
  if(i.trangThai()!=null&&!i.trangThai().isBlank()){if(!List.of("Sai lệch","Đã thu").contains(i.trangThai()))throw LoiNghiepVuException.duLieu("ht5.gd.trangthai");x.setTrangThai(i.trangThai());}
  x.setLyDoSaiLech(i.lyDoSaiLech());x.setNguoiXuLySaiLech(n.maNV());x.setThoiGianXuLySaiLech(LocalDateTime.now());return view(x);}
 @Transactional public GiaoDichView capNhat(String id,TrangThaiInput i){nd(ChucNang.HT5_CAP_NHAT_TRANG_THAI_GD);valid(i);var x=get(gd.findById(id));if("Đã đối soát".equals(x.getTrangThai()))throw LoiNghiepVuException.xungDot("ht5.gd.dadoisoat");x.setTrangThai(i.trangThai());x.setSoTienThucThu(i.soTienThucThu());x.setThoiGianCapNhat(LocalDateTime.now());x.setNguoiCapNhat(NguoiDungHienTai.hienTai().maNV());return view(x);}
 private PhieuView pv(PhieuThuChi x){String don=x.getMaGiaoDich()==null?null:gd.findById(x.getMaGiaoDich()).map(GiaoDichCOD::getMaDonHang).orElse(null);
  return new PhieuView(x.getMaPhieu(),x.getLoaiPhieu(),x.getMaGiaoDich(),don,x.getSoTien(),x.getNgayLap(),x.getNoiDung(),x.getTrangThai(),x.getNguoiLap(),x.getNguoiDuyet(),x.getNguoiXacNhan(),x.getThoiGianXacNhan());}
 @Transactional(readOnly=true) public List<PhieuView> danhSachPhieu(String tt){ndMot(ChucNang.HT5_LAP_PHIEU_THU,ChucNang.HT5_LAP_PHIEU_CHI,ChucNang.HT5_PHE_DUYET_GIAO_DICH,ChucNang.HT5_XAC_NHAN_THU_TIEN,ChucNang.HT5_XAC_NHAN_CHI_TRA,ChucNang.HT5_LICH_SU_GIAO_DICH,ChucNang.HT5_BAO_CAO_THU_CHI,ChucNang.HT5_TRA_CUU_COD);return pt.findAll().stream().filter(x->tt==null||tt.isBlank()||tt.equals(x.getTrangThai())).map(this::pv).toList();}
 @Transactional public PhieuView phieu(PhieuInput i){valid(i);if(!"Thu".equalsIgnoreCase(i.loaiPhieu())&&!"Chi".equalsIgnoreCase(i.loaiPhieu()))throw LoiNghiepVuException.duLieu("ht5.loaiphieu");
  var n=nd("Thu".equalsIgnoreCase(i.loaiPhieu())?ChucNang.HT5_LAP_PHIEU_THU:ChucNang.HT5_LAP_PHIEU_CHI);if(i.maGiaoDich()!=null&&!i.maGiaoDich().isBlank())get(gd.findById(i.maGiaoDich()));var x=new PhieuThuChi();x.setMaPhieu(ma.capMa("Thu".equalsIgnoreCase(i.loaiPhieu())?LoaiChungTu.PHIEU_THU:LoaiChungTu.PHIEU_CHI));x.setLoaiPhieu(i.loaiPhieu());x.setMaGiaoDich(i.maGiaoDich());x.setSoTien(i.soTien());x.setNgayLap(i.ngayLap());x.setNoiDung(i.noiDung());x.setTrangThai("Chờ duyệt");x.setNguoiLap(n.maNV());return pv(pt.save(x));}
 /**
  * Usecase "Phê duyệt giao dịch" của kế toán trưởng (3.5.3) — chỉ chốt phương án, chưa động vào tiền.
  * 3.5.1 bước 4 nói phiếu sau đó "chuyển cho thủ quỹ xác nhận thực tế", bước 5 mới ghi sổ quỹ và
  * công nợ; trước đây bước duyệt ghi sổ luôn nên vai trò Thủ quỹ bị bỏ trống.
  */
 @Transactional public PhieuView duyet(String id){var n=nd(ChucNang.HT5_PHE_DUYET_GIAO_DICH);var x=get(pt.findById(id));if(!"Chờ duyệt".equals(x.getTrangThai()))throw LoiNghiepVuException.xungDot("ht5.phieu.dachotduyet");x.setTrangThai("Đã duyệt");x.setNguoiDuyet(n.maNV());return pv(x);}
 /**
  * Usecase "Xác nhận thu tiền" / "Xác nhận chi trả COD" của thủ quỹ (3.5.3): kiểm tiền thực tế rồi
  * xác nhận, hệ thống ghi sổ quỹ và cập nhật công nợ ngay trong cùng giao dịch (3.5.1 bước 5).
  */
 @Transactional public PhieuView xacNhan(String id){var x=get(pt.findById(id));boolean thu="Thu".equalsIgnoreCase(x.getLoaiPhieu());
  var n=nd(thu?ChucNang.HT5_XAC_NHAN_THU_TIEN:ChucNang.HT5_XAC_NHAN_CHI_TRA);
  if(!"Đã duyệt".equals(x.getTrangThai()))throw LoiNghiepVuException.xungDot("ht5.phieu.chuaduyet");
  x.setTrangThai("Đã thực hiện");x.setNguoiXacNhan(n.maNV());x.setThoiGianXacNhan(LocalDateTime.now());
  var s=new SoQuy();s.setMaSoQuy(ma.capMa(LoaiChungTu.SO_QUY));s.setMaPhieu(x.getMaPhieu());s.setSoTienThu(thu?nz(x.getSoTien()):BigDecimal.ZERO);s.setSoTienChi(thu?BigDecimal.ZERO:nz(x.getSoTien()));s.setNgayGhiSo(LocalDate.now());s.setMaNVGhiSo(n.maNV());sq.save(s);
  capNhatCongNo(x,thu);return pv(x);}
 /**
  * Usecase "Tra cứu lịch sử giao dịch" (3.5.3, thủ quỹ và kế toán trưởng): gộp nhật ký thao tác
  * trên giao dịch COD, phiếu thu/chi và các đợt đối soát, xếp theo thời gian gần nhất.
  */
 @Transactional(readOnly=true) public List<LichSuView> lichSu(){nd(ChucNang.HT5_LICH_SU_GIAO_DICH);
  var ds1=new ArrayList<LichSuView>();
  for(var g:gd.findAll())ds1.add(new LichSuView(g.getThoiGianCapNhat()==null?(g.getNgayTao()==null?null:g.getNgayTao().atStartOfDay()):g.getThoiGianCapNhat(),"Giao dịch COD",g.getMaGiaoDich(),g.getMaDonHang(),g.getSoTienThucThu()==null?g.getSoTienPhaiThu():g.getSoTienThucThu(),g.getTrangThai(),g.getNguoiCapNhat(),g.getLyDoSaiLech()));
  for(var x:pt.findAll()){String nguoi=x.getNguoiXacNhan()!=null?x.getNguoiXacNhan():(x.getNguoiDuyet()!=null?x.getNguoiDuyet():x.getNguoiLap());
   ds1.add(new LichSuView(x.getThoiGianXacNhan()==null?(x.getNgayLap()==null?null:x.getNgayLap().atStartOfDay()):x.getThoiGianXacNhan(),"Phiếu "+x.getLoaiPhieu().toLowerCase(),x.getMaPhieu(),x.getMaGiaoDich()==null?null:gd.findById(x.getMaGiaoDich()).map(GiaoDichCOD::getMaDonHang).orElse(null),x.getSoTien(),x.getTrangThai(),nguoi,x.getNoiDung()));}
  for(var d:ds.findAll())ds1.add(new LichSuView(d.getThoiGian(),"Đợt đối soát",d.getMaDoiSoat(),null,d.getTongTien(),d.getTrangThai(),d.getNguoiDoiSoat(),"Chênh lệch "+nz(d.getChenhLech())));
  ds1.sort(Comparator.comparing(LichSuView::thoiGian,Comparator.nullsLast(Comparator.reverseOrder())));return ds1;}
 private void capNhatCongNo(PhieuThuChi x,boolean thu){if(x.getMaGiaoDich()==null||x.getMaGiaoDich().isBlank())return;var g=gd.findById(x.getMaGiaoDich()).orElse(null);if(g==null)return;var d=dh.findById(g.getMaDonHang()).orElse(null);if(d==null||d.getMaKhachHang()==null)return;var c=cn.findFirstByMaKhachHang(d.getMaKhachHang()).orElseGet(()->{var z=new CongNo();z.setMaCongNo(ma.capMa(LoaiChungTu.CONG_NO));z.setMaKhachHang(d.getMaKhachHang());z.setSoTienPhaiTra(BigDecimal.ZERO);z.setSoTienDaTra(BigDecimal.ZERO);z.setTrangThai("Còn nợ");z.setNgayCapNhat(LocalDateTime.now());em.persist(z);return z;});if(thu)c.setSoTienPhaiTra(nz(c.getSoTienPhaiTra()).add(nz(x.getSoTien())));else c.setSoTienDaTra(nz(c.getSoTienDaTra()).add(nz(x.getSoTien())));c.setTrangThai(nz(c.getSoTienDaTra()).compareTo(nz(c.getSoTienPhaiTra()))>=0?"Đã tất toán":"Còn nợ");c.setNgayCapNhat(LocalDateTime.now());}
 /**
  * Đối soát theo đợt (2.4.5): chênh lệch = Σ phải thu − Σ thực thu của các giao dịch trong đợt.
  * Giao dịch đã đối soát được khoá lại và đơn hàng tương ứng chuyển sang "Đã đối soát" (PHỤ LỤC C).
  */
 @Transactional public DoiSoatView doiSoat(DoiSoatInput i){var n=nd(ChucNang.HT5_DOI_SOAT_COD);valid(i);var list=i.maGiaoDich().stream().distinct().map(id->get(gd.findById(id))).toList();for(var g:list)if("Đã đối soát".equals(g.getTrangThai()))throw LoiNghiepVuException.xungDot("ht5.gd.dadoisoat");BigDecimal phaiThu=list.stream().map(g->nz(g.getSoTienPhaiThu())).reduce(BigDecimal.ZERO,BigDecimal::add);BigDecimal thucThu=list.stream().map(g->nz(g.getSoTienThucThu())).reduce(BigDecimal.ZERO,BigDecimal::add);var x=new DoiSoatCOD();x.setMaDoiSoat(ma.capMa(LoaiChungTu.DOI_SOAT));x.setNgayDoiSoat(i.ngayDoiSoat());x.setTongTien(phaiThu);x.setChenhLech(phaiThu.subtract(thucThu));x.setTrangThai(phaiThu.compareTo(thucThu)==0?"Khớp":"Có chênh lệch");x.setNguoiDoiSoat(n.maNV());x.setThoiGian(LocalDateTime.now());ds.save(x);for(var g:list){var ct=new ChiTietDoiSoat();ct.setId(new ChiTietDoiSoat.Id(x.getMaDoiSoat(),g.getMaGiaoDich()));em.persist(ct);g.setTrangThai("Đã đối soát");g.setThoiGianCapNhat(LocalDateTime.now());g.setNguoiCapNhat(n.maNV());var d=dh.findById(g.getMaDonHang()).orElse(null);if(d!=null&&"Đã giao".equals(d.getTrangThai()))state.doiTrangThai("HT5",d.getMaDonHang(),"Đã đối soát",n,"Đợt đối soát "+x.getMaDoiSoat());}em.flush();return to(x);}
 private DoiSoatView to(DoiSoatCOD x){return new DoiSoatView(x.getMaDoiSoat(),x.getNgayDoiSoat(),x.getTongTien(),x.getChenhLech(),x.getTrangThai(),x.getNguoiDoiSoat(),x.getThoiGian(),ctds.findByIdMaDoiSoat(x.getMaDoiSoat()).stream().map(c->c.getId().getMaGiaoDich()).toList());}
 @Transactional(readOnly=true) public List<DoiSoatView> danhSachDoiSoat(){nd(ChucNang.HT5_DOI_SOAT_COD);return ds.findAll().stream().map(this::to).toList();}
 @Transactional(readOnly=true) public List<SoQuyView> soQuy(){nd(ChucNang.HT5_BAO_CAO_THU_CHI);return sq.findAll().stream().map(x->new SoQuyView(x.getMaSoQuy(),x.getMaPhieu(),x.getSoTienThu(),x.getSoTienChi(),x.getNgayGhiSo())).toList();}
 @Transactional(readOnly=true) public List<CongNoView> congNo(){nd(ChucNang.HT5_BAO_CAO_THU_CHI);return cn.findAll().stream().map(c->new CongNoView(c.getMaCongNo(),c.getMaKhachHang(),kh.findById(c.getMaKhachHang()).map(k->k.getTenKhachHang()).orElse(null),c.getSoTienPhaiTra(),c.getSoTienDaTra(),nz(c.getSoTienPhaiTra()).subtract(nz(c.getSoTienDaTra())),c.getTrangThai(),c.getNgayCapNhat())).toList();}
 /** Báo cáo thu – chi COD theo khoảng ngày (đặc tả 3.5.1 bước 6, 3.5.2 "xuất báo cáo thu-chi"). */
 @Transactional(readOnly=true) public BaoCaoView baoCao(LocalDate a,LocalDate b){nd(ChucNang.HT5_BAO_CAO_THU_CHI);if(a==null)a=LocalDate.now().withDayOfMonth(1);if(b==null)b=LocalDate.now();if(a.isAfter(b))throw LoiNghiepVuException.duLieu("ht5.tieuchi");final LocalDate tu=a,den=b;var list=gd.findAll().stream().filter(g->g.getNgayTao()!=null&&!g.getNgayTao().isBefore(tu)&&!g.getNgayTao().isAfter(den)).toList();BigDecimal phaiThu=list.stream().map(g->nz(g.getSoTienPhaiThu())).reduce(BigDecimal.ZERO,BigDecimal::add);BigDecimal thucThu=list.stream().map(g->nz(g.getSoTienThucThu())).reduce(BigDecimal.ZERO,BigDecimal::add);var phieu=pt.findAll().stream().filter(p->"Đã thực hiện".equals(p.getTrangThai())&&p.getNgayLap()!=null&&!p.getNgayLap().isBefore(tu)&&!p.getNgayLap().isAfter(den)).toList();BigDecimal daChi=phieu.stream().filter(p->"Chi".equalsIgnoreCase(p.getLoaiPhieu())).map(p->nz(p.getSoTien())).reduce(BigDecimal.ZERO,BigDecimal::add);var saiLech=list.stream().filter(g->g.getSoTienThucThu()!=null&&nz(g.getSoTienPhaiThu()).compareTo(g.getSoTienThucThu())!=0).map(this::view).toList();BigDecimal duQuy=sq.findAll().stream().map(s->nz(s.getSoTienThu()).subtract(nz(s.getSoTienChi()))).reduce(BigDecimal.ZERO,BigDecimal::add);return new BaoCaoView(tu,den,list.size(),phaiThu,thucThu,daChi,thucThu.subtract(daChi),saiLech.size(),saiLech,duQuy,list.stream().collect(Collectors.groupingBy(GiaoDichCOD::getTrangThai,TreeMap::new,Collectors.counting())),list.isEmpty()?"Không có giao dịch COD phù hợp với tiêu chí đã chọn":null);}
}
