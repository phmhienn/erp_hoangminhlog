package vn.hoangminh.erp.Ht4NhanSu.dto;
import jakarta.validation.constraints.*; import java.time.LocalDate; import java.time.LocalDateTime;
public final class Ht4Dto { private Ht4Dto(){}
 public record HoSoInput(@NotBlank @Size(max=100) String hoTen, LocalDate ngaySinh, @Size(max=10) String gioiTinh, @Size(max=15) String soDienThoai, @Size(max=255) String diaChi, @Email @Size(max=100) String email, String maPhongBan, String maChucVu, String maTrangThai, LocalDate ngayVaoLam){}
 public record HoSoView(String maNV,String hoTen,LocalDate ngaySinh,String gioiTinh,String soDienThoai,String diaChi,String email,String maPhongBan,String tenPhongBan,String maChucVu,String tenChucVu,String maTrangThai,String tenTrangThai,LocalDate ngayVaoLam){}
 public record TrangThaiInput(@NotBlank String maTrangThai){}
 public record YeuCauInput(@NotBlank @Size(max=500) String noiDung){}
 public record YeuCauView(Integer maYeuCau,String maNV,String hoTen,String noiDung,String trangThai,LocalDateTime thoiGianGui,String nguoiXuLy,LocalDateTime thoiGianXuLy,String phanHoi){ }
 /** Nhân sự xử lý yêu cầu cập nhật: duyệt hoặc từ chối kèm phản hồi (đặc tả 3.4.2). */
 public record XuLyYeuCauInput(@NotBlank String trangThai,@Size(max=300) String phanHoi){}
}
