package vn.hoangminh.erp.Ht2Kho.controller;
import jakarta.validation.Valid; import lombok.RequiredArgsConstructor; import org.springframework.web.bind.annotation.*; import java.time.LocalDate; import java.util.*; import vn.hoangminh.erp.Ht1DonHang.domain.DonHang; import vn.hoangminh.erp.Ht2Kho.dto.Ht2Dto.*; import vn.hoangminh.erp.Ht2Kho.service.Ht2Service;
@RestController @RequestMapping("/api/ht2") @RequiredArgsConstructor public class Ht2Controller {
 private final Ht2Service s;
 @GetMapping("/don-cho-nhap") public List<DonKhoView> choNhap(@RequestParam(required=false) String tuKhoa){return s.donChoNhap(tuKhoa);}
 @GetMapping("/don-cho-xuat") public List<DonKhoView> choXuat(@RequestParam(required=false) String tuKhoa){return s.donChoXuat(tuKhoa);}
 @PostMapping("/phieu-nhap") public PhieuNhapView nhap(@Valid @RequestBody NhapInput i){return s.taoNhap(i);}
 @PutMapping("/phieu-nhap/{ma}") public PhieuNhapView suaNhap(@PathVariable String ma,@Valid @RequestBody NhapInput i){return s.capNhatNhap(ma,i);}
 @GetMapping("/phieu-nhap") public List<PhieuNhapView> dsNhap(@RequestParam(required=false) String trangThai){return s.danhSachNhap(trangThai);}
 @PostMapping("/phieu-nhap/{ma}/gui-duyet") public PhieuNhapView gui(@PathVariable String ma){return s.guiDuyet(ma);}
 @PostMapping("/phieu-nhap/{ma}/duyet") public PhieuNhapView duyet(@PathVariable String ma){return s.duyet(ma);}
 @PostMapping("/phieu-nhap/{ma}/tu-choi") public PhieuNhapView tuChoi(@PathVariable String ma,@Valid @RequestBody vn.hoangminh.erp.Ht1DonHang.dto.Ht1Dto.LyDoInput i){return s.tuChoi(ma,i.lyDo());}
 @GetMapping("/ton-kho") public List<DonKhoView> ton(){return s.tonKho();}
 @PostMapping("/phieu-xuat") public PhieuXuatView xuat(@Valid @RequestBody XuatInput i){return s.taoXuat(i);}
 @PutMapping("/phieu-xuat/{ma}") public PhieuXuatView suaXuat(@PathVariable String ma,@Valid @RequestBody XuatInput i){return s.capNhatXuat(ma,i);}
 @PostMapping("/phieu-xuat/{ma}/gui-duyet") public PhieuXuatView guiDuyetXuat(@PathVariable String ma){return s.guiDuyetXuat(ma);}
 @PostMapping("/phieu-xuat/{ma}/duyet") public PhieuXuatView duyetXuat(@PathVariable String ma){return s.duyetXuat(ma);}
 @PostMapping("/phieu-xuat/{ma}/tu-choi") public PhieuXuatView tuChoiXuat(@PathVariable String ma,@Valid @RequestBody vn.hoangminh.erp.Ht1DonHang.dto.Ht1Dto.LyDoInput i){return s.tuChoiXuat(ma,i.lyDo());}
 @GetMapping("/phieu-xuat") public List<PhieuXuatView> dsXuat(){return s.danhSachXuat();}
 @GetMapping("/tra-cuu") public List<TraCuuView> traCuu(@RequestParam(required=false) String maDonHang,@RequestParam(required=false) String tuKhoa,@RequestParam(required=false) LocalDate tuNgay,@RequestParam(required=false) LocalDate denNgay,@RequestParam(required=false) String maNV,@RequestParam(required=false) String loai){return s.traCuu(maDonHang,tuKhoa,tuNgay,denNgay,maNV,loai);}
 @PostMapping("/phieu-kiem-ke") public KiemKeView kk(@Valid @RequestBody KiemKeInput i){return s.taoKiemKe(i);}
 @GetMapping("/phieu-kiem-ke") public List<KiemKeView> dsKk(){return s.danhSachKiemKe();}
 @PostMapping("/bien-ban") public BienBanView bienBan(@Valid @RequestBody BienBanInput i){return s.lapBienBan(i);}
 @GetMapping("/bien-ban") public List<BienBanView> dsBienBan(){return s.danhSachBienBan();}
 @GetMapping("/dashboard") public Dashboard dash(){return s.dashboard();}
 @GetMapping("/bao-cao-nhap") public BaoCao report(@RequestParam(required=false) LocalDate tuNgay,@RequestParam(required=false) LocalDate denNgay){return s.baoCao(tuNgay,denNgay);}
}
