package vn.hoangminh.erp.Ht1DonHang.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public final class Ht1Dto {
  private Ht1Dto() {}
  public record KhachHangInput(
      @NotBlank @Size(max=100) String tenKhachHang,
      @Size(max=10) String soDienThoai, @Size(max=255) String diachi,
      @Email @Size(max=100) String email, @Size(max=30) String loaiKH) {}
  public record KhachHangView(String maKhachHang, String tenKhachHang, String soDienThoai,
                             String diachi, String email, String loaiKH) {}
  public record DichVuView(String maDichVu, String tenDichVu, BigDecimal donGia, String moTa) {}
  public record HangHoaInput(@NotBlank @Size(max=50) String loaiHangHoa,
      @NotNull @Min(1) Integer soLuong, @NotNull @Positive Double trongLuong) {}
  public record DonHangInput(
      @NotBlank @Size(max=20) String maKhachHang, @NotBlank @Size(max=20) String maDichVu,
      @NotBlank @Size(max=100) String nguoiGui, @NotBlank @Size(max=100) String nguoiNhan,
      @NotBlank @Size(max=15) String sdtNguoiGui, @NotBlank @Size(max=15) String sdtNguoiNhan,
      @NotBlank @Size(max=255) String diachiLayHang, @NotBlank @Size(max=255) String diachiGiaoHang,
      @NotNull @DecimalMin(value="0", inclusive=false) @Digits(integer=8,fraction=2) BigDecimal khoiLuong,
      @NotNull @DecimalMin("0") @Digits(integer=13,fraction=2) BigDecimal tienCOD,
      @NotNull @DecimalMin("0") @Digits(integer=13,fraction=2) BigDecimal phiVanChuyen,
      @NotEmpty @Size(max=100) List<@NotNull @Valid HangHoaInput> hangHoa) {}
  public record LyDoInput(@NotBlank @Size(max=300) String lyDo) {}
  public record HangHoaView(String maHangHoa, String maSP, String loaiHangHoa, Integer soLuong, Double trongLuong) {}
  public record DonHangView(String maDonHang, String maKhachHang, String maDichVu,
      String nguoiGui, String nguoiNhan, String sdtNguoiGui, String sdtNguoiNhan,
      String diachiLayHang, String diachiGiaoHang, BigDecimal khoiLuong, BigDecimal tienCOD,
      BigDecimal phiVanChuyen, String trangThai, String kiemDuyet, String lyDoHuy,
      LocalDateTime ngayTao, List<HangHoaView> hangHoa) {}
  public record LichSuView(Integer maLichSu, String hanhDong, String trangThai,
      String maTaiKhoan, LocalDateTime thoiGian, String ghiChu) {}
  public record BaoCao(long tongDon, Map<String,Long> theoTrangThai, Map<String,Long> theoKiemDuyet,
      Map<String,Long> theoNgay, Map<String,Long> theoTuan, Map<String,Long> theoThang,
      Map<String,Long> theoKhachHang, Map<String,Long> theoDichVu, String thongBao) {}
}
