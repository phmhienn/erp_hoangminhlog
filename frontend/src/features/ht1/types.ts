export interface KhachHang { maKhachHang: string; tenKhachHang: string; soDienThoai: string; diachi: string; email: string; loaiKH: string }
export interface DichVu { maDichVu: string; tenDichVu: string; donGia: number }
export interface HangHoa { loaiHangHoa: string; soLuong: number; trongLuong: number }
export interface DonInput {
  maKhachHang: string; maDichVu: string; nguoiGui: string; nguoiNhan: string;
  sdtNguoiGui: string; sdtNguoiNhan: string; diachiLayHang: string; diachiGiaoHang: string;
  khoiLuong: number; tienCOD: number; phiVanChuyen: number; hangHoa: HangHoa[];
}
export interface DonHang extends DonInput { maDonHang: string; trangThai: string; kiemDuyet: string; lyDoHuy: string | null; ngayTao: string }
export interface LichSu { maLichSu: number; hanhDong: string; trangThai: string; maTaiKhoan: string; thoiGian: string; ghiChu: string | null }
export interface BaoCao { tongDon: number; theoTrangThai: Record<string,number>; theoKiemDuyet: Record<string,number>; theoNgay: Record<string,number>; theoTuan: Record<string,number>; theoThang: Record<string,number>; theoKhachHang: Record<string,number>; theoDichVu: Record<string,number>; thongBao: string | null }
