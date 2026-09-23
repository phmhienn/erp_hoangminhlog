package vn.hoangminh.erp.Ht2Kho.dto;
import jakarta.validation.Valid; import jakarta.validation.constraints.*; import java.math.BigDecimal; import java.time.*; import java.util.*;
public final class Ht2Dto { private Ht2Dto(){}
 public record NhapInput(@NotBlank @Size(max=10) String maDonHang,@NotNull LocalDate ngayNhap,@Size(max=255) String ghiChu){}
 public record PhieuNhapView(String maPhieuNhap,String maDonHang,String maNV,LocalDate ngayNhap,String trangThai,String ghiChu,String matHang){}
 public record XuatInput(@NotBlank @Size(max=10) String maDonHang,@NotNull LocalDate ngayXuat,@Size(max=255) String lyDoXuat,@Size(max=255) String ghiChu){}
 public record PhieuXuatView(String maPhieuXuat,String maDonHang,String maNV,LocalDate ngayXuat,String trangThai,String lyDoXuat,String ghiChu,String nguoiDuyet,String matHang){}
 public record DonKhoView(String maDonHang,String maKhachHang,String tenKhachHang,String nguoiNhan,String sdtNguoiNhan,String diachiGiaoHang,BigDecimal khoiLuong,BigDecimal tienCOD,String trangThai,LocalDate ngayTao,String matHang){}
 public record KiemKeChiTiet(@NotBlank @Size(max=10) String maDonHang,@NotNull @Min(0) @Max(1) Integer soLuongThucTe,@Size(max=255) String ghiChu){}
 public record KiemKeInput(@NotNull LocalDate ngayKiemKe,@Size(max=100) String khuVucKiemKe,@Size(max=255) String ghiChu,@NotEmpty List<@Valid KiemKeChiTiet> chiTiet){}
 public record KiemKeView(String maPhieuKiemKe,LocalDate ngayKiemKe,String khuVucKiemKe,String trangThai,String ghiChu,List<KiemKeChiTietView> chiTiet){}
 public record KiemKeChiTietView(String maDonHang,Integer soLuongHeThong,Integer soLuongThucTe,Integer chenhLech,String ghiChu){}
 /** Biên bản sự cố hàng không đạt — luồng phụ bước 4 của đặc tả 3.2.3. */
 public record BienBanInput(@Size(max=20) String maPhieuNhap,@NotBlank @Size(max=10) String maDonHang,@NotBlank @Size(max=50) String loaiSuCo,@Size(max=500) String moTa,@Size(max=255) String duongDanAnh){}
 public record BienBanView(String maBienBan,String maPhieuNhap,String maDonHang,String loaiSuCo,String moTa,String duongDanAnh,String maNVLap,LocalDateTime ngayLap){}
 public record Dashboard(long donChoNhap,long donTrongKho,long donChoNhanVanChuyen,long phieuNhapChoDuyet,long phieuXuatChoDuyet){}
 public record BaoCao(LocalDate tuNgay,LocalDate denNgay,long soPhieuNhap,long donDaNhap,long soPhieuXuat,long donDaXuat,Map<String,Long> theoTrangThaiNhap,Map<String,Long> theoNgayNhap,Map<String,Long> theoTrangThaiXuat,Map<String,Long> theoNgayXuat){}
 /**
  * Một dòng kết quả của usecase "Báo cáo phiếu nhập/xuất" (3.2.3): gom đơn chờ nhập,
  * phiếu nhập và phiếu xuất về cùng một bảng để nhân viên kho đối soát (3.2.2 "tra cứu lịch sử").
  */
 public record TraCuuView(String loai,String ma,String maDonHang,String tenKhachHang,LocalDate ngay,String trangThai,String maNV,String matHang){}
}
