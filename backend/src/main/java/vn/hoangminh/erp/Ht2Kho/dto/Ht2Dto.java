package vn.hoangminh.erp.Ht2Kho.dto;
import jakarta.validation.Valid; import jakarta.validation.constraints.*; import java.math.BigDecimal; import java.time.*; import java.util.*;
public final class Ht2Dto { private Ht2Dto(){}
 public record SanPhamView(String maSP,String tenSP,String donViTinh,BigDecimal gia,String trangThai,String dieuKienBaoQuan){}
 public record NhapChiTiet(@NotBlank String maSP,@NotNull @Min(1) Integer soLuong,@DecimalMin("0") BigDecimal donGia,@Size(max=50) String tinhTrangHang){}
 public record NhapInput(@NotBlank String maDonHang,@NotNull LocalDate ngayNhap,@Size(max=20) String maNCC,@Size(max=255) String ghiChu,@NotEmpty List<@Valid NhapChiTiet> chiTiet){}
 public record PhieuNhapView(String maPhieuNhap,String maDonHang,String maNV,LocalDate ngayNhap,String trangThai,BigDecimal tongTien,String ghiChu,List<NhapChiTiet> chiTiet){}
 public record XuatChiTiet(@NotBlank String maSP,@NotNull @Min(1) Integer soLuong,@DecimalMin("0") BigDecimal donGia){}
 public record XuatInput(@Size(max=10) String maDonHang,@NotNull LocalDate ngayXuat,@Size(max=255) String lyDoXuat,@Size(max=255) String ghiChu,@NotEmpty List<@Valid XuatChiTiet> chiTiet){}
 public record PhieuXuatView(String maPhieuXuat,String maDonHang,String maNV,LocalDate ngayXuat,String trangThai,BigDecimal tongTien,String lyDoXuat,String ghiChu,String nguoiDuyet,List<XuatChiTiet> chiTiet){}
 /**
  * Thông tin đơn hàng hiển thị khi nhân viên kho tra cứu theo mã PO — đặc tả 3.2.3 bước 8:
  * "hệ thống truy vấn dữ liệu từ CSDL và hiển thị thông tin chi tiết đơn hàng lên màn hình".
  */
 public record DonKhoView(String maDonHang,String maKhachHang,String tenKhachHang,String nguoiNhan,String sdtNguoiNhan,String diachiGiaoHang,BigDecimal khoiLuong,BigDecimal tienCOD,String trangThai,LocalDate ngayTao,String matHang){}
 public record TonKhoView(String maTonKho,String maSP,String tenSP,String maViTri,Integer soLuongTon,LocalDateTime ngayCapNhat){}
 public record KiemKeChiTiet(@NotBlank String maSP,@NotNull @Min(0) Integer soLuongThucTe,@Size(max=255) String ghiChu){}
 public record KiemKeInput(@NotNull LocalDate ngayKiemKe,@Size(max=100) String khuVucKiemKe,@Size(max=255) String ghiChu,@NotEmpty List<@Valid KiemKeChiTiet> chiTiet){}
 public record KiemKeView(String maPhieuKiemKe,LocalDate ngayKiemKe,String khuVucKiemKe,String trangThai,String ghiChu,List<KiemKeChiTietView> chiTiet){}
 public record KiemKeChiTietView(String maSP,Integer soLuongHeThong,Integer soLuongThucTe,Integer chenhLech,String ghiChu){}
 /** Biên bản sự cố hàng không đạt — luồng phụ bước 4 của đặc tả 3.2.3. */
 public record BienBanInput(@Size(max=20) String maPhieuNhap,@Size(max=10) String maDonHang,@NotBlank @Size(max=50) String loaiSuCo,@Size(max=500) String moTa,@Size(max=255) String duongDanAnh){}
 public record BienBanView(String maBienBan,String maPhieuNhap,String maDonHang,String loaiSuCo,String moTa,String duongDanAnh,String maNVLap,LocalDateTime ngayLap){}
 public record Dashboard(long tongSanPham,long tongSoLuong,BigDecimal tongGiaTri,long phieuNhapChoDuyet,long phieuNhapDaDuyet,long phieuXuat,long sanPhamSapHet,List<TonKhoView> sapHet){}
 public record BaoCao(LocalDate tuNgay,LocalDate denNgay,long soPhieuNhap,BigDecimal tongNhap,long soPhieuXuat,BigDecimal tongXuat,Map<String,Long> theoTrangThaiNhap,Map<String,Long> theoNgayNhap,Map<String,Long> theoTrangThaiXuat,Map<String,Long> theoNgayXuat,Map<String,Long> nhapTheoSanPham,Map<String,Long> xuatTheoSanPham,String thongBao){}
 /**
  * Một dòng kết quả của usecase "Báo cáo phiếu nhập/xuất" (3.2.3): gom đơn chờ nhập,
  * phiếu nhập và phiếu xuất về cùng một bảng để nhân viên kho đối soát (3.2.2 "tra cứu lịch sử").
  */
 public record TraCuuView(String loai,String ma,String maDonHang,String tenKhachHang,LocalDate ngay,String trangThai,String maNV,BigDecimal tongTien,String matHang){}
}
