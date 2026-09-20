package vn.hoangminh.erp.Ht2Kho.service;
import jakarta.persistence.EntityManager; import jakarta.validation.Validator; import lombok.RequiredArgsConstructor; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import java.math.*; import java.time.*; import java.util.*; import java.util.function.Function; import java.util.stream.Collectors;
import vn.hoangminh.erp.Ht1DonHang.domain.DonHang; import vn.hoangminh.erp.Ht1DonHang.repository.DonHangRepository; import vn.hoangminh.erp.Ht1DonHang.repository.HangHoaRepository; import vn.hoangminh.erp.common.audit.LichSuDonHangService; import vn.hoangminh.erp.common.donhang.DonHangStateService; import vn.hoangminh.erp.common.exception.LoiNghiepVuException; import vn.hoangminh.erp.common.permission.*; import vn.hoangminh.erp.common.security.NguoiDungHienTai; import vn.hoangminh.erp.common.sequence.*; import vn.hoangminh.erp.Ht2Kho.domain.*; import vn.hoangminh.erp.Ht2Kho.dto.Ht2Dto.*; import vn.hoangminh.erp.Ht2Kho.repository.*; import static vn.hoangminh.erp.common.permission.ChucNang.*;
@Service @RequiredArgsConstructor public class Ht2Service {
 private final SanPhamRepository sanPham; private final PhieuNhapRepository phieuNhap; private final ChiTietPhieuNhapRepository ctNhap; private final PhieuXuatRepository phieuXuat; private final ChiTietPhieuXuatRepository ctXuat; private final TonKhoRepository tonKho; private final PhieuKiemKeRepository kiemKe; private final ChiTietKiemKeRepository ctKiemKe; private final BienBanSuCoRepository bienBan; private final DonHangRepository donHang; private final HangHoaRepository hangHoa; private final vn.hoangminh.erp.Ht1DonHang.repository.KhachHangRepository khachHang; private final LichSuDonHangService lichSu; private final DonHangStateService state; private final MaChungTuService ma; private final TaoChungTuTransaction tao; private final QuyenService quyen; private final Validator validator; private final EntityManager em;
 private NguoiDungHienTai nd(ChucNang c){var n=NguoiDungHienTai.hienTai();quyen.yeuCauChucNang(n.vaiTro(),c);return n;} private void valid(Object o){if(!validator.validate(o).isEmpty())throw LoiNghiepVuException.duLieu("loi.dulieu.khonghople");} private void exists(boolean ok){if(!ok)throw LoiNghiepVuException.khongTimThay("loi.khongtimthay");}
 /** Cho qua nếu vai trò có ít nhất một chức năng — dùng cho dữ liệu tồn kho mà cả hai vai trò đều cần. */
 private NguoiDungHienTai ndMot(ChucNang... cs){var n=NguoiDungHienTai.hienTai();for(ChucNang c:cs)if(quyen.coChucNang(n.vaiTro(),c))return n;throw LoiNghiepVuException.tuChoiQuyen("quyen.tuchoi");}
 private SanPhamView sp(SanPham p){return new SanPhamView(p.getMaSP(),p.getTenSP(),p.getDonViTinh(),p.getGia(),p.getTrangThai(),p.getDieuKienBaoQuan());}
 @Transactional(readOnly=true) public List<SanPhamView> sanPham(String tuKhoa){nd(HT2_TRA_CUU_DON_NHAP_KHO);return sanPham.findAll().stream().filter(p->tuKhoa==null||tuKhoa.isBlank()||p.getMaSP().contains(tuKhoa)||p.getTenSP().toLowerCase().contains(tuKhoa.toLowerCase())).map(this::sp).toList();}
/** Quy đổi đơn hàng sang khung nhìn của kho, kèm tên khách hàng và danh sách mặt hàng. */
 private DonKhoView donKho(DonHang d){String ten=d.getMaKhachHang()==null?null:khachHang.findById(d.getMaKhachHang()).map(k->k.getTenKhachHang()).orElse(null);
  String mh=hangHoa.findByMaDonHangOrderByMaHangHoa(d.getMaDonHang()).stream().map(h->h.getLoaiHangHoa()+" ×"+h.getSoLuong()).collect(Collectors.joining(", "));
  return new DonKhoView(d.getMaDonHang(),d.getMaKhachHang(),ten,d.getNguoiNhan(),d.getSdtNguoiNhan(),d.getDiachiGiaoHang(),d.getKhoiLuong(),d.getTienCOD(),d.getTrangThai(),d.getNgayTao()==null?null:d.getNgayTao().toLocalDate(),mh);}
 private boolean khopDon(DonKhoView v,String tuKhoa){if(tuKhoa==null||tuKhoa.isBlank())return true;String k=tuKhoa.toLowerCase().trim();
  return v.maDonHang().toLowerCase().contains(k)||(v.tenKhachHang()!=null&&v.tenKhachHang().toLowerCase().contains(k))||(v.nguoiNhan()!=null&&v.nguoiNhan().toLowerCase().contains(k))||(v.matHang()!=null&&v.matHang().toLowerCase().contains(k));}
 /** Đơn đã được HT1 duyệt, đang chờ nhập kho — nguồn tra cứu mã PO của màn lập phiếu nhập (3.2.3). */
 @Transactional(readOnly=true) public List<DonKhoView> donChoNhap(String tuKhoa){ndMot(HT2_TRA_CUU_DON_NHAP_KHO,HT2_LAP_PHIEU_NHAP);
  return donHang.findAll().stream().filter(d->d.getTrangThai().equals("Đã tạo")&&lichSu.kiemDuyet(d.getMaDonHang()).equals("Đã duyệt")).map(this::donKho).filter(v->khopDon(v,tuKhoa)).toList();}
 /** Đơn đã nhập kho, đủ điều kiện lập phiếu xuất — nguồn tra cứu mã PO của màn lập phiếu xuất. */
 @Transactional(readOnly=true) public List<DonKhoView> donChoXuat(String tuKhoa){ndMot(HT2_TRA_CUU_DON_NHAP_KHO,HT2_LAP_PHIEU_XUAT);
  return donHang.findAll().stream().filter(d->d.getTrangThai().equals("Đã nhập kho")).map(this::donKho).filter(v->khopDon(v,tuKhoa)).toList();}
 /** Quản lý kho xem toàn bộ phiếu; nhân viên kho chỉ xem phiếu do chính mình lập. */
 @Transactional(readOnly=true) public List<PhieuNhapView> danhSachNhap(String trangThai){var n=ndMot(HT2_PHE_DUYET_PHIEU_NHAP,HT2_LAP_PHIEU_NHAP);
  boolean quanLy=quyen.coChucNang(n.vaiTro(),HT2_PHE_DUYET_PHIEU_NHAP);
  return phieuNhap.findAll().stream().filter(p->quanLy||n.maNV().equals(p.getMaNV()))
    .filter(p->trangThai==null||trangThai.isBlank()||p.getTrangThai().equals(trangThai)).map(this::nhapView).toList();}
 private PhieuNhapView nhapView(PhieuNhap p){var lines=ctNhap.findByIdMaPhieuNhap(p.getMaPhieuNhap()).stream().map(x->new NhapChiTiet(x.getId().getMaSP(),x.getSoLuong(),x.getDonGia(),x.getTinhTrangHang())).toList();return new PhieuNhapView(p.getMaPhieuNhap(),p.getMaDonHang(),p.getMaNV(),p.getNgayNhap(),p.getTrangThai(),p.getTongTien(),p.getGhiChu(),lines);}
 @Transactional public PhieuNhapView taoNhap(NhapInput i){var n=nd(HT2_LAP_PHIEU_NHAP);valid(i);var d=donHang.findById(i.maDonHang()).orElseThrow(()->LoiNghiepVuException.khongTimThay("loi.khongtimthay"));if(!d.getTrangThai().equals("Đã tạo")||!lichSu.kiemDuyet(d.getMaDonHang()).equals("Đã duyệt"))throw LoiNghiepVuException.xungDot("ht2.donchuaduyet");return tao.thucHien(()->{var p=new PhieuNhap();p.setMaPhieuNhap(ma.capMa(LoaiChungTu.PHIEU_NHAP));p.setMaDonHang(i.maDonHang());p.setMaNCC(i.maNCC());p.setMaNV(n.maNV());p.setNgayNhap(i.ngayNhap());p.setTrangThai("Lưu tạm");p.setGhiChu(i.ghiChu());p.setTongTien(i.chiTiet().stream().map(x->x.donGia()==null?BigDecimal.ZERO:x.donGia().multiply(BigDecimal.valueOf(x.soLuong()))).reduce(BigDecimal.ZERO,BigDecimal::add));em.persist(p);for(var x:i.chiTiet()){exists(sanPham.existsById(x.maSP()));var c=new ChiTietPhieuNhap();c.setId(new ChiTietPhieuNhap.Id(p.getMaPhieuNhap(),x.maSP()));c.setSoLuong(x.soLuong());c.setDonGia(x.donGia());c.setTinhTrangHang(x.tinhTrangHang());c.setThanhTien(x.donGia()==null?BigDecimal.ZERO:x.donGia().multiply(BigDecimal.valueOf(x.soLuong())));em.persist(c);}em.flush();return nhapView(p);});}
 /** Cập nhật phiếu nhập khi còn sửa được — usecase "Lập – cập nhật phiếu nhập_xuất kho" (3.2.3). */
 @Transactional public PhieuNhapView capNhatNhap(String id,NhapInput i){nd(HT2_CAP_NHAT_PHIEU);valid(i);var p=phieuNhap.findById(id).orElseThrow(()->LoiNghiepVuException.khongTimThay("loi.khongtimthay"));
  if(!p.getTrangThai().equals("Lưu tạm")&&!p.getTrangThai().equals("Từ chối"))throw LoiNghiepVuException.xungDot("ht2.phieukhoasua");
  p.setNgayNhap(i.ngayNhap());p.setMaNCC(i.maNCC());p.setGhiChu(i.ghiChu());p.setTrangThai("Lưu tạm");
  ctNhap.deleteAll(ctNhap.findByIdMaPhieuNhap(id));ctNhap.flush();
  for(var x:i.chiTiet()){exists(sanPham.existsById(x.maSP()));var c=new ChiTietPhieuNhap();c.setId(new ChiTietPhieuNhap.Id(id,x.maSP()));c.setSoLuong(x.soLuong());c.setDonGia(x.donGia());c.setTinhTrangHang(x.tinhTrangHang());c.setThanhTien(x.donGia()==null?BigDecimal.ZERO:x.donGia().multiply(BigDecimal.valueOf(x.soLuong())));em.persist(c);}
  em.flush();p.setTongTien(i.chiTiet().stream().map(x->x.donGia()==null?BigDecimal.ZERO:x.donGia().multiply(BigDecimal.valueOf(x.soLuong()))).reduce(BigDecimal.ZERO,BigDecimal::add));return nhapView(p);}
 @Transactional public PhieuNhapView guiDuyet(String id){nd(HT2_LAP_PHIEU_NHAP);var p=phieuNhap.findById(id).orElseThrow(()->LoiNghiepVuException.khongTimThay("loi.khongtimthay"));if(!p.getTrangThai().equals("Lưu tạm"))throw LoiNghiepVuException.xungDot("loi.trangthai.xungdot");p.setTrangThai("Chờ duyệt");return nhapView(p);}
 @Transactional public PhieuNhapView duyet(String id){var n=nd(HT2_PHE_DUYET_PHIEU_NHAP);var p=phieuNhap.findById(id).orElseThrow(()->LoiNghiepVuException.khongTimThay("loi.khongtimthay"));if(!p.getTrangThai().equals("Chờ duyệt"))throw LoiNghiepVuException.xungDot("ht2.chopheduyet");for(var c:ctNhap.findByIdMaPhieuNhap(id)){var existing=tonKho.khoa(c.getId().getMaSP(),"");var t=existing.orElseGet(()->{var z=new TonKho();z.setMaTonKho(ma.capMa(LoaiChungTu.TON_KHO));z.setMaSP(c.getId().getMaSP());z.setMaViTri("");z.setSoLuongTon(0);return z;});t.setSoLuongTon(t.getSoLuongTon()+c.getSoLuong());t.setNgayCapNhat(LocalDateTime.now());if(existing.isEmpty())em.persist(t);em.flush();}p.setTrangThai("Đã duyệt");p.setNguoiDuyet(n.maNV());state.doiTrangThai("HT2",p.getMaDonHang(),"Đã nhập kho",n,"Duyệt phiếu nhập "+id);return nhapView(p);}
 @Transactional public PhieuNhapView tuChoi(String id,String lyDo){var n=nd(HT2_PHE_DUYET_PHIEU_NHAP);if(lyDo==null||lyDo.isBlank()||lyDo.length()>300)throw LoiNghiepVuException.duLieu("ht2.lydo");var p=phieuNhap.findById(id).orElseThrow(()->LoiNghiepVuException.khongTimThay("loi.khongtimthay"));if(!p.getTrangThai().equals("Chờ duyệt"))throw LoiNghiepVuException.xungDot("ht2.chopheduyet");p.setTrangThai("Từ chối");p.setGhiChu(lyDo);return nhapView(p);}
 private TonKhoView tk(TonKho t){var p=sanPham.findById(t.getMaSP()).orElse(null);return new TonKhoView(t.getMaTonKho(),t.getMaSP(),p==null?null:p.getTenSP(),t.getMaViTri(),t.getSoLuongTon(),t.getNgayCapNhat());}
 @Transactional(readOnly=true) public List<TonKhoView> tonKho(){ndMot(HT2_DASHBOARD_NHAP_XUAT,HT2_KIEM_KE,HT2_LAP_PHIEU_XUAT);return tonKho.findAll().stream().map(this::tk).toList();}
 private PhieuXuatView xuatView(PhieuXuat p){var lines=ctXuat.findByIdMaPhieuXuat(p.getMaPhieuXuat()).stream().map(x->new XuatChiTiet(x.getId().getMaSP(),x.getSoLuong(),x.getDonGia())).toList();return new PhieuXuatView(p.getMaPhieuXuat(),p.getMaDonHang(),p.getMaNV(),p.getNgayXuat(),p.getTrangThai(),p.getTongTien(),p.getLyDoXuat(),p.getGhiChu(),p.getNguoiDuyet(),lines);}
 /**
  * Lập phiếu xuất ở trạng thái "Lưu tạm" theo đúng quy trình xuất kho 3.2.1: soạn hàng và đóng gói
  * trước, chỉ khi xác nhận xuất mới trừ tồn kho. Nhờ vậy phiếu còn sửa được trước lúc xuất thật.
  */
 @Transactional public PhieuXuatView taoXuat(XuatInput i){var n=nd(HT2_LAP_PHIEU_XUAT);valid(i);kiemDonXuat(i.maDonHang());
  return tao.thucHien(()->{for(var x:i.chiTiet())exists(sanPham.existsById(x.maSP()));
   var p=new PhieuXuat();p.setMaPhieuXuat(ma.capMa(LoaiChungTu.PHIEU_XUAT));p.setMaNV(n.maNV());p.setMaDonHang(i.maDonHang());p.setNgayXuat(i.ngayXuat());p.setLyDoXuat(i.lyDoXuat());p.setGhiChu(i.ghiChu());p.setTrangThai("Lưu tạm");
   p.setTongTien(tong(i));em.persist(p);em.flush();luuDongXuat(p.getMaPhieuXuat(),i);em.flush();return xuatView(p);});}
 private void kiemDonXuat(String maDon){if(maDon!=null&&!maDon.isBlank()){var d=donHang.findById(maDon).orElseThrow(()->LoiNghiepVuException.khongTimThay("loi.khongtimthay"));if(!d.getTrangThai().equals("Đã nhập kho"))throw LoiNghiepVuException.xungDot("ht2.donchuadanhap");}}
 private BigDecimal tong(XuatInput i){return i.chiTiet().stream().map(x->x.donGia()==null?BigDecimal.ZERO:x.donGia().multiply(BigDecimal.valueOf(x.soLuong()))).reduce(BigDecimal.ZERO,BigDecimal::add);}
 private void luuDongXuat(String id,XuatInput i){for(var x:i.chiTiet()){var c=new ChiTietPhieuXuat();c.setId(new ChiTietPhieuXuat.Id(id,x.maSP()));c.setSoLuong(x.soLuong());c.setDonGia(x.donGia());c.setThanhTien(x.donGia()==null?BigDecimal.ZERO:x.donGia().multiply(BigDecimal.valueOf(x.soLuong())));em.persist(c);}}
 @Transactional public PhieuXuatView capNhatXuat(String id,XuatInput i){nd(HT2_CAP_NHAT_PHIEU);valid(i);var p=phieuXuat.findById(id).orElseThrow(()->LoiNghiepVuException.khongTimThay("loi.khongtimthay"));
  if(!p.getTrangThai().equals("Lưu tạm")&&!p.getTrangThai().equals("Từ chối"))throw LoiNghiepVuException.xungDot("ht2.phieukhoasua");kiemDonXuat(i.maDonHang());
  for(var x:i.chiTiet())exists(sanPham.existsById(x.maSP()));
  p.setMaDonHang(i.maDonHang());p.setNgayXuat(i.ngayXuat());p.setLyDoXuat(i.lyDoXuat());p.setGhiChu(i.ghiChu());p.setTrangThai("Lưu tạm");p.setTongTien(tong(i));
  ctXuat.deleteAll(ctXuat.findByIdMaPhieuXuat(id));ctXuat.flush();luuDongXuat(id,i);em.flush();return xuatView(p);}
 /** Nhân viên kho gửi phiếu xuất lên quản lý kho duyệt — đối xứng với luồng phiếu nhập (3.2.2). */
 @Transactional public PhieuXuatView guiDuyetXuat(String id){nd(HT2_LAP_PHIEU_XUAT);var p=phieuXuat.findById(id).orElseThrow(()->LoiNghiepVuException.khongTimThay("loi.khongtimthay"));
  if(!p.getTrangThai().equals("Lưu tạm"))throw LoiNghiepVuException.xungDot("loi.trangthai.xungdot");
  if(ctXuat.findByIdMaPhieuXuat(id).isEmpty())throw LoiNghiepVuException.duLieu("loi.dulieu.khonghople");
  p.setTrangThai("Chờ duyệt");return xuatView(p);}
 /**
  * Quản lý kho duyệt phiếu xuất: kiểm tra tồn rồi trừ tồn trong cùng transaction (3.2.1 bước 4 luồng
  * xuất). Trước đây nhân viên kho tự xác nhận và trừ tồn nên usecase "Quản lí phiếu nhập_xuất" của
  * 3.2.3 mới chỉ làm được một nửa.
  */
 @Transactional public PhieuXuatView duyetXuat(String id){var n=nd(HT2_PHE_DUYET_PHIEU_NHAP);var p=phieuXuat.findById(id).orElseThrow(()->LoiNghiepVuException.khongTimThay("loi.khongtimthay"));
  if(!p.getTrangThai().equals("Chờ duyệt"))throw LoiNghiepVuException.xungDot("ht2.chopheduyet");
  var dong=ctXuat.findByIdMaPhieuXuat(id);if(dong.isEmpty())throw LoiNghiepVuException.duLieu("loi.dulieu.khonghople");
  for(var c:dong){var t=tonKho.khoa(c.getId().getMaSP(),"").orElseThrow(()->LoiNghiepVuException.duLieu("ht2.thieuton"));if(t.getSoLuongTon()<c.getSoLuong())throw LoiNghiepVuException.duLieu("ht2.thieuton");}
  for(var c:dong){var t=tonKho.khoa(c.getId().getMaSP(),"").orElseThrow();t.setSoLuongTon(t.getSoLuongTon()-c.getSoLuong());t.setNgayCapNhat(LocalDateTime.now());}
  p.setTrangThai("Đã xuất");p.setNguoiDuyet(n.maNV());em.flush();return xuatView(p);}
 @Transactional public PhieuXuatView tuChoiXuat(String id,String lyDo){nd(HT2_PHE_DUYET_PHIEU_NHAP);
  if(lyDo==null||lyDo.isBlank()||lyDo.length()>255)throw LoiNghiepVuException.duLieu("ht2.lydo");
  var p=phieuXuat.findById(id).orElseThrow(()->LoiNghiepVuException.khongTimThay("loi.khongtimthay"));
  if(!p.getTrangThai().equals("Chờ duyệt"))throw LoiNghiepVuException.xungDot("ht2.chopheduyet");
  p.setTrangThai("Từ chối");p.setGhiChu(lyDo);return xuatView(p);}
 @Transactional public KiemKeView taoKiemKe(KiemKeInput i){var n=nd(HT2_KIEM_KE);valid(i);return tao.thucHien(()->{var p=new PhieuKiemKe();p.setMaPhieuKiemKe(ma.capMa(LoaiChungTu.PHIEU_KIEM_KE));p.setMaNV(n.maNV());p.setNgayKiemKe(i.ngayKiemKe());p.setKhuVucKiemKe(i.khuVucKiemKe());p.setGhiChu(i.ghiChu());p.setTrangThai("Đã chốt");em.persist(p);for(var x:i.chiTiet()){var stock=tonKho.findByMaSP(x.maSP()).stream().mapToInt(TonKho::getSoLuongTon).sum();var c=new ChiTietKiemKe();c.setId(new ChiTietKiemKe.Id(p.getMaPhieuKiemKe(),x.maSP()));c.setSoLuongHeThong(stock);c.setSoLuongThucTe(x.soLuongThucTe());c.setChenhLech(x.soLuongThucTe()-stock);c.setGhiChu(x.ghiChu());em.persist(c);}em.flush();return new KiemKeView(p.getMaPhieuKiemKe(),p.getNgayKiemKe(),p.getKhuVucKiemKe(),p.getTrangThai(),p.getGhiChu(),ctKiemKe.findByIdMaPhieuKiemKe(p.getMaPhieuKiemKe()).stream().map(c->new KiemKeChiTietView(c.getId().getMaSP(),c.getSoLuongHeThong(),c.getSoLuongThucTe(),c.getChenhLech(),c.getGhiChu())).toList());});}
 /** Luồng phụ bước 4 của đặc tả 3.2.3: hàng thừa/thiếu/hư hỏng thì lập biên bản thay vì nhập kho. */
 @Transactional public BienBanView lapBienBan(BienBanInput i){var n=nd(HT2_LAP_BIEN_BAN_SU_CO);valid(i);if(i.maPhieuNhap()!=null&&!i.maPhieuNhap().isBlank())exists(phieuNhap.existsById(i.maPhieuNhap()));if(i.maDonHang()!=null&&!i.maDonHang().isBlank())exists(donHang.existsById(i.maDonHang()));var b=new BienBanSuCo();b.setMaBienBan(ma.capMa(LoaiChungTu.BIEN_BAN_SU_CO));b.setMaPhieuNhap(i.maPhieuNhap()==null||i.maPhieuNhap().isBlank()?null:i.maPhieuNhap());b.setMaDonHang(i.maDonHang()==null||i.maDonHang().isBlank()?null:i.maDonHang());b.setLoaiSuCo(i.loaiSuCo());b.setMoTa(i.moTa());b.setDuongDanAnh(i.duongDanAnh());b.setMaNVLap(n.maNV());b.setNgayLap(LocalDateTime.now());return bb(bienBan.save(b));}
 private BienBanView bb(BienBanSuCo b){return new BienBanView(b.getMaBienBan(),b.getMaPhieuNhap(),b.getMaDonHang(),b.getLoaiSuCo(),b.getMoTa(),b.getDuongDanAnh(),b.getMaNVLap(),b.getNgayLap());}
 @Transactional(readOnly=true) public List<BienBanView> danhSachBienBan(){nd(HT2_LAP_BIEN_BAN_SU_CO);return bienBan.findByOrderByNgayLapDesc().stream().map(this::bb).toList();}
 @Transactional(readOnly=true) public List<PhieuXuatView> danhSachXuat(){var n=ndMot(HT2_LAP_PHIEU_XUAT,HT2_PHE_DUYET_PHIEU_NHAP);
  boolean quanLy=quyen.coChucNang(n.vaiTro(),HT2_PHE_DUYET_PHIEU_NHAP);
  return phieuXuat.findAll().stream().filter(p->quanLy||n.maNV().equals(p.getMaNV())).map(this::xuatView).toList();}
 @Transactional(readOnly=true) public List<KiemKeView> danhSachKiemKe(){nd(HT2_KIEM_KE);return kiemKe.findAll().stream().map(p->new KiemKeView(p.getMaPhieuKiemKe(),p.getNgayKiemKe(),p.getKhuVucKiemKe(),p.getTrangThai(),p.getGhiChu(),ctKiemKe.findByIdMaPhieuKiemKe(p.getMaPhieuKiemKe()).stream().map(c->new KiemKeChiTietView(c.getId().getMaSP(),c.getSoLuongHeThong(),c.getSoLuongThucTe(),c.getChenhLech(),c.getGhiChu())).toList())).toList();}
 @Transactional(readOnly=true) public Dashboard dashboard(){nd(HT2_DASHBOARD_NHAP_XUAT);var all=tonKho.findAll();var sap=all.stream().filter(x->x.getSoLuongTon()<=5).map(this::tk).toList();return new Dashboard(sanPham.count(),all.stream().mapToLong(x->x.getSoLuongTon()).sum(),all.stream().map(x->sanPham.findById(x.getMaSP()).map(SanPham::getGia).orElse(BigDecimal.ZERO).multiply(BigDecimal.valueOf(x.getSoLuongTon()))).reduce(BigDecimal.ZERO,BigDecimal::add),phieuNhap.countByTrangThai("Chờ duyệt"),phieuNhap.countByTrangThai("Đã duyệt"),phieuXuat.count(),sap.size(),sap);}
 /** Báo cáo cả hai chiều nhập và xuất — usecase "Báo cáo nhập_xuất kho" của quản lý kho (3.2.3). */
 @Transactional(readOnly=true) public BaoCao baoCao(LocalDate a,LocalDate b){nd(HT2_BAO_CAO_NHAP_KHO);if(a==null)a=LocalDate.now().withDayOfMonth(1);if(b==null)b=LocalDate.now();if(a.isAfter(b))throw LoiNghiepVuException.duLieu("ht2.tieuchi");
  var pn=phieuNhap.findByNgayNhapBetween(a,b);var px=phieuXuat.findByNgayXuatBetween(a,b);
  Map<String,Long> slNhap=new TreeMap<>(),slXuat=new TreeMap<>();
  for(var p:pn)for(var c:ctNhap.findByIdMaPhieuNhap(p.getMaPhieuNhap()))slNhap.merge(c.getId().getMaSP(),(long)c.getSoLuong(),Long::sum);
  for(var p:px)for(var c:ctXuat.findByIdMaPhieuXuat(p.getMaPhieuXuat()))slXuat.merge(c.getId().getMaSP(),(long)c.getSoLuong(),Long::sum);
  return new BaoCao(a,b,pn.size(),pn.stream().map(x->x.getTongTien()==null?BigDecimal.ZERO:x.getTongTien()).reduce(BigDecimal.ZERO,BigDecimal::add),
   px.size(),px.stream().map(x->x.getTongTien()==null?BigDecimal.ZERO:x.getTongTien()).reduce(BigDecimal.ZERO,BigDecimal::add),
   pn.stream().collect(Collectors.groupingBy(PhieuNhap::getTrangThai,TreeMap::new,Collectors.counting())),
   pn.stream().collect(Collectors.groupingBy(x->x.getNgayNhap().toString(),TreeMap::new,Collectors.counting())),
   px.stream().collect(Collectors.groupingBy(PhieuXuat::getTrangThai,TreeMap::new,Collectors.counting())),
   px.stream().collect(Collectors.groupingBy(x->x.getNgayXuat().toString(),TreeMap::new,Collectors.counting())),
   slNhap,slXuat,pn.isEmpty()&&px.isEmpty()?"Không có phiếu nhập/xuất phù hợp với tiêu chí đã chọn":null);}
 /**
  * Tra cứu gộp đơn hàng chờ nhập, phiếu nhập và phiếu xuất theo mã đơn, tên khách hàng,
  * khoảng ngày hoặc mã nhân viên thực hiện (3.2.2 vai trò nhân viên kho — "Tra cứu lịch sử").
  */
 @Transactional(readOnly=true) public List<TraCuuView> traCuu(String maDonHang,String tuKhoa,LocalDate tuNgay,LocalDate denNgay,String maNV,String loai){nd(HT2_TRA_CUU_DON_NHAP_KHO);
  var khach=new java.util.HashMap<String,String>();donHang.findAll().forEach(d->khach.put(d.getMaDonHang(),d.getMaKhachHang()));
  List<TraCuuView> kq=new java.util.ArrayList<>();
  if(loai==null||loai.isBlank()||loai.equals("Đơn chờ nhập"))
   for(var d:donHang.findAll()) if(d.getTrangThai().equals("Đã tạo")&&lichSu.kiemDuyet(d.getMaDonHang()).equals("Đã duyệt"))
    kq.add(new TraCuuView("Đơn chờ nhập",d.getMaDonHang(),d.getMaDonHang(),d.getMaKhachHang(),d.getNgayTao().toLocalDate(),d.getTrangThai(),null,d.getPhiVanChuyen(),null));
  if(loai==null||loai.isBlank()||loai.equals("Phiếu nhập"))
   for(var p:phieuNhap.findAll())
    kq.add(new TraCuuView("Phiếu nhập",p.getMaPhieuNhap(),p.getMaDonHang(),khach.get(p.getMaDonHang()),p.getNgayNhap(),p.getTrangThai(),p.getMaNV(),p.getTongTien(),
      ctNhap.findByIdMaPhieuNhap(p.getMaPhieuNhap()).stream().map(c->c.getId().getMaSP()+"×"+c.getSoLuong()).collect(Collectors.joining(", "))));
  if(loai==null||loai.isBlank()||loai.equals("Phiếu xuất"))
   for(var p:phieuXuat.findAll())
    kq.add(new TraCuuView("Phiếu xuất",p.getMaPhieuXuat(),p.getMaDonHang(),khach.get(p.getMaDonHang()),p.getNgayXuat(),p.getTrangThai(),p.getMaNV(),p.getTongTien(),
      ctXuat.findByIdMaPhieuXuat(p.getMaPhieuXuat()).stream().map(c->c.getId().getMaSP()+"×"+c.getSoLuong()).collect(Collectors.joining(", "))));
  var ten=new java.util.HashMap<String,String>();khachHang.findAll().forEach(k->ten.put(k.getMaKhachHang(),k.getTenKhachHang()));
  return kq.stream()
   .map(v->new TraCuuView(v.loai(),v.ma(),v.maDonHang(),v.tenKhachHang()==null?null:ten.getOrDefault(v.tenKhachHang(),v.tenKhachHang()),v.ngay(),v.trangThai(),v.maNV(),v.tongTien(),v.matHang()))
   .filter(v->maDonHang==null||maDonHang.isBlank()||(v.maDonHang()!=null&&v.maDonHang().toLowerCase().contains(maDonHang.toLowerCase().trim()))||v.ma().toLowerCase().contains(maDonHang.toLowerCase().trim()))
   .filter(v->tuKhoa==null||tuKhoa.isBlank()||(v.tenKhachHang()!=null&&v.tenKhachHang().toLowerCase().contains(tuKhoa.toLowerCase().trim())))
   .filter(v->tuNgay==null||(v.ngay()!=null&&!v.ngay().isBefore(tuNgay)))
   .filter(v->denNgay==null||(v.ngay()!=null&&!v.ngay().isAfter(denNgay)))
   .filter(v->maNV==null||maNV.isBlank()||(v.maNV()!=null&&v.maNV().toLowerCase().contains(maNV.toLowerCase().trim())))
   .sorted(Comparator.comparing(TraCuuView::ngay,Comparator.nullsLast(Comparator.reverseOrder()))).toList();}
}
