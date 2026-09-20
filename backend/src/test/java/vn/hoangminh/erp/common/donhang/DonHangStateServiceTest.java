package vn.hoangminh.erp.common.donhang;

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import vn.hoangminh.erp.common.exception.LoiNghiepVuException;

class DonHangStateServiceTest {
  @Test void vongDoiHopLe() {
    DonHangStateService.kiemTra("HT2","Đã tạo","Đã nhập kho");
    DonHangStateService.kiemTra("HT3","Đã nhập kho","Đang giao");
    DonHangStateService.kiemTra("HT3","Đang giao","Đã giao");
    DonHangStateService.kiemTra("HT5","Đã giao","Đã đối soát");
    DonHangStateService.kiemTra("HT1","Đã tạo","Đã hủy");
  }
  @Test void camNhayCocDiNguocVaHuySauNhapKho() {
    for(var states : new String[][]{{"HT3","Đã tạo","Đã giao"},{"HT2","Đang giao","Đã nhập kho"},{"HT1","Đã nhập kho","Đã hủy"},{"HT3","Đã hủy","Đang giao"},{"HT5","Đã đối soát","Đã đối soát"}})
      assertThatThrownBy(()->DonHangStateService.kiemTra(states[0],states[1],states[2]))
        .isInstanceOfSatisfying(LoiNghiepVuException.class,e->assertThat(e.getStatus().value()).isEqualTo(409));
  }
  @Test void camGhiSaiHeThong() {
    assertThatThrownBy(()->DonHangStateService.kiemTra("HT1","Đang giao","Đã giao"))
      .isInstanceOfSatisfying(LoiNghiepVuException.class,e->assertThat(e.getStatus().value()).isEqualTo(403));
  }
}
