package vn.hoangminh.erp.Ht5ThuChiCod.controller; import jakarta.validation.Valid; import java.time.LocalDate; import java.util.*; import lombok.RequiredArgsConstructor; import org.springframework.web.bind.annotation.*; import vn.hoangminh.erp.Ht5ThuChiCod.dto.Ht5Dto.*; import vn.hoangminh.erp.Ht5ThuChiCod.service.Ht5Service;
@RestController @RequestMapping("/api/ht5") @RequiredArgsConstructor public class Ht5Controller{private final Ht5Service s;
 @GetMapping("/giao-dich") public List<GiaoDichView> gd(@RequestParam(required=false)String trangThai,@RequestParam(required=false)String tuKhoa){return s.giaoDich(trangThai,tuKhoa);}
 @PutMapping("/giao-dich/{ma}") public GiaoDichView cap(@PathVariable String ma,@Valid @RequestBody TrangThaiInput i){return s.capNhat(ma,i);}
 @GetMapping("/phieu-thu-chi") public List<PhieuView> dsPhieu(@RequestParam(required=false)String trangThai){return s.danhSachPhieu(trangThai);}
 @PostMapping("/phieu-thu-chi") public PhieuView phieu(@Valid @RequestBody PhieuInput i){return s.phieu(i);}
 @PostMapping("/phieu-thu-chi/{ma}/phe-duyet") public PhieuView duyet(@PathVariable String ma){return s.duyet(ma);}
 @PostMapping("/phieu-thu-chi/{ma}/xac-nhan") public PhieuView xacNhan(@PathVariable String ma){return s.xacNhan(ma);}
 @GetMapping("/sai-lech") public List<GiaoDichView> saiLech(){return s.saiLech();}
 @PutMapping("/sai-lech/{ma}") public GiaoDichView xuLySaiLech(@PathVariable String ma,@Valid @RequestBody SaiLechInput i){return s.xuLySaiLech(ma,i);}
 @GetMapping("/lich-su") public List<LichSuView> lichSu(){return s.lichSu();}
 @GetMapping("/doi-soat") public List<DoiSoatView> dsDoiSoat(){return s.danhSachDoiSoat();}
 @PostMapping("/doi-soat") public DoiSoatView ds(@Valid @RequestBody DoiSoatInput i){return s.doiSoat(i);}
 @GetMapping("/so-quy") public List<SoQuyView> sq(){return s.soQuy();}
 @GetMapping("/cong-no") public List<CongNoView> cn(){return s.congNo();}
 @GetMapping("/bao-cao") public BaoCaoView baoCao(@RequestParam(required=false) LocalDate tuNgay,@RequestParam(required=false) LocalDate denNgay){return s.baoCao(tuNgay,denNgay);}
}
