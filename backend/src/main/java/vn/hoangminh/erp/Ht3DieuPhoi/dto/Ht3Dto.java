package vn.hoangminh.erp.Ht3DieuPhoi.dto;
import jakarta.validation.constraints.*; import java.math.BigDecimal; import java.time.*; import java.util.*;
public final class Ht3Dto { private Ht3Dto(){}
 public record TaiXeView(String maTaiXe,String maNV,String hoTen,String soGPLX,String loaiGPLX,String trangThai){}
 public record PhuongTienView(Integer maPhuongTien,String bienSo,String loaiXe,BigDecimal taiTrong,String trangThai){}
 public record ChuyenInput(@NotEmpty List<@NotBlank String> maDonHang,@NotNull LocalDateTime ngayKhoiHanh,@NotNull LocalDateTime thoiGianDuKien,@NotBlank @Size(max=255) String diemXuatPhat,@NotBlank @Size(max=255) String diemKetThuc,@NotNull @DecimalMin(value="0",inclusive=false) BigDecimal khoangCach,@NotNull @Min(1) Integer thoiGianDuKienPhut,@Size(max=500) String moTa){}
 public record PhanCongInput(@NotBlank String maTaiXe,@NotNull Integer maPhuongTien){}
 /** Sự cố phải có mô tả và ảnh hiện trường thì điều phối mới đủ căn cứ xử lý (3.3.2). */
 public record SuCoInput(@NotNull Integer maChuyen,@Size(max=10) String maDonHang,@NotBlank @Size(max=50) String loaiSuCo,@NotBlank @Size(max=2000) String moTa,@NotBlank @Size(max=255) String hinhAnh){}
 public record MinhChungInput(@NotBlank String loaiMinhChung,@NotBlank @Size(max=255) String duongDan){}
 /** Mốc gắn với một đơn cụ thể của chuyến; bỏ trống thì tính cho toàn chuyến. */
 public record MocInput(@NotBlank @Size(max=255) String diem,@Size(max=500) String moTa,@Size(max=10) String maDonHang){}
 /** Chuyến vận chuyển kèm lộ trình, tài xế và xe — đủ dữ liệu cho màn phân công và phê duyệt (3.3.2). */
 public record ChuyenView(Integer maChuyen,String maNguoiLap,String maTaiXe,String hoTenTaiXe,Integer maPhuongTien,String bienSo,String trangThai,LocalDateTime ngayKhoiHanh,LocalDateTime thoiGianDuKien,LocalDateTime thoiGianThucTe,String diemXuatPhat,String diemKetThuc,BigDecimal khoangCach,String moTa,List<String> maDonHang){}
 /** Một đơn trong nhiệm vụ của tài xế: điểm giao/nhận, người nhận, hàng hoá, tiền thu hộ (3.3.2). */
 public record DonNhiemVu(String maDonHang,String nguoiNhan,String sdtNguoiNhan,String diachiLayHang,String diachiGiaoHang,BigDecimal khoiLuong,BigDecimal tienCOD,String trangThai,String hangHoa){}
 public record NhiemVuView(Integer maChuyen,String trangThai,LocalDateTime ngayKhoiHanh,LocalDateTime thoiGianDuKien,LocalDateTime thoiGianThucTe,String diemXuatPhat,String diemKetThuc,BigDecimal khoangCach,String moTa,String bienSo,List<DonNhiemVu> donHang,List<MocView> moc){}
 public record SuCoView(Integer maSuCo,Integer maChuyen,String maDonHang,String maTaiXe,String loaiSuCo,String moTa,LocalDateTime thoiGian,String trangThai,String hinhAnh,String huongXuLy,String nguoiXuLy,LocalDateTime thoiGianXuLy){}
 /** Điều phối cập nhật trạng thái, phương án xử lý và ảnh kèm theo vào sự cố tài xế đã báo (3.3.2). */
 public record XuLySuCoInput(@NotBlank @Size(max=30) String trangThai,@Size(max=500) String huongXuLy,@Size(max=255) String hinhAnh){}
 public record MocView(Integer maMoc,Integer maChuyen,String maDonHang,String diem,String moTa,LocalDateTime thoiGian,String maNVCapNhat){}
 /**
  * Một dòng lịch sử giao hàng cá nhân của NV giao hàng (đặc tả 3.3.2) — tách theo từng đơn và mang
  * cả trạng thái đơn, vì trước đây chỉ hiện trạng thái chuyến nên đơn đã giao vẫn đọc là "Đang giao".
  */
 public record LichSuGiaoView(Integer maChuyen,String maDonHang,String trangThaiChuyen,String trangThaiDon,LocalDateTime ngayKhoiHanh,LocalDateTime thoiGianThucTe,String diemXuatPhat,String diemKetThuc,BigDecimal khoangCach){}
 /** Kết quả tra cứu lịch sử giao hàng theo mã vận đơn, biển số, ngày giờ, khách hàng (3.3.2). */
 public record TraCuuView(Integer maChuyen,String maDonHang,String tenKhachHang,String bienSo,String hoTenTaiXe,String trangThaiChuyen,String trangThaiDon,LocalDateTime ngayKhoiHanh,LocalDateTime thoiGianThucTe,String diemKetThuc,Integer soSuCo,String mocGanNhat,LocalDateTime thoiGianMoc,Integer soMoc){}
}
