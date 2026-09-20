package vn.hoangminh.erp.Ht1DonHang.controller;

import java.time.LocalDate;
import java.util.List;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.hoangminh.erp.Ht1DonHang.dto.Ht1Dto.*;
import vn.hoangminh.erp.Ht1DonHang.service.Ht1Service;

@RestController @RequestMapping("/api/ht1") @RequiredArgsConstructor
public class Ht1Controller {
  private final Ht1Service service;
  @GetMapping("/khach-hang") public List<KhachHangView> khach(@RequestParam(required=false) String tuKhoa) { return service.khachHang(tuKhoa); }
  @PostMapping("/khach-hang") public KhachHangView taoKhach(@Valid @RequestBody KhachHangInput i) { return service.taoKhach(i); }
  @PutMapping("/khach-hang/{ma}") public KhachHangView suaKhach(@PathVariable String ma,@Valid @RequestBody KhachHangInput i) { return service.suaKhach(ma,i); }
  @GetMapping("/dich-vu") public List<DichVuView> dichVu() { return service.dichVu(); }
  @GetMapping("/don-hang") public List<DonHangView> ds(@RequestParam(required=false) String ma,@RequestParam(required=false) String khachHang,@RequestParam(required=false) LocalDate tuNgay,@RequestParam(required=false) LocalDate denNgay) { return service.danhSach(ma,khachHang,tuNgay,denNgay); }
  @GetMapping("/don-hang/{ma}") public DonHangView chiTiet(@PathVariable String ma) { return service.chiTiet(ma); }
  @PostMapping("/don-hang") public DonHangView tao(@Valid @RequestBody DonHangInput i) { return service.taoDon(i); }
  @PutMapping("/don-hang/{ma}") public DonHangView sua(@PathVariable String ma,@Valid @RequestBody DonHangInput i) { return service.suaDon(ma,i); }
  @PostMapping("/don-hang/{ma}/gui-duyet") public DonHangView gui(@PathVariable String ma) { return service.xuLy(ma,"gui-duyet",null); }
  @PostMapping("/don-hang/{ma}/duyet") public DonHangView duyet(@PathVariable String ma) { return service.xuLy(ma,"duyet",null); }
  @PostMapping("/don-hang/{ma}/tu-choi") public DonHangView tuChoi(@PathVariable String ma,@Valid @RequestBody LyDoInput i) { return service.xuLy(ma,"tu-choi",i.lyDo()); }
  @PostMapping("/don-hang/{ma}/huy") public DonHangView huy(@PathVariable String ma,@Valid @RequestBody LyDoInput i) { return service.xuLy(ma,"huy",i.lyDo()); }
  @GetMapping("/don-hang/{ma}/lich-su") public List<LichSuView> lichSu(@PathVariable String ma) { return service.lichSu(ma); }
  @GetMapping("/bao-cao") public BaoCao baoCao(@RequestParam(required=false) LocalDate tuNgay,@RequestParam(required=false) LocalDate denNgay,@RequestParam(required=false) String maKhachHang,@RequestParam(required=false) String maDichVu) { return service.baoCao(tuNgay,denNgay,maKhachHang,maDichVu); }
}
