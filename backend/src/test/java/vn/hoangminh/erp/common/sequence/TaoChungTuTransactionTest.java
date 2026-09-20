package vn.hoangminh.erp.common.sequence;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

class TaoChungTuTransactionTest {
  @Test void rollbackTruocKhiThuLai() {
    var manager=mock(PlatformTransactionManager.class);
    when(manager.getTransaction(any())).thenAnswer(i->mock(TransactionStatus.class));
    var service=new TaoChungTuTransaction(manager); var count=new AtomicInteger();
    assertThat(service.thucHien(()->{
      if(count.getAndIncrement()==0) throw new jakarta.persistence.EntityExistsException("duplicate");
      return "DH0000002";
    })).isEqualTo("DH0000002");
    var order=inOrder(manager);
    order.verify(manager).getTransaction(any());order.verify(manager).rollback(any());
    order.verify(manager).getTransaction(any());order.verify(manager).commit(any());
  }
  @Test void gioiHanBaLanKhongLapVoHan() {
    var manager=mock(PlatformTransactionManager.class);when(manager.getTransaction(any())).thenReturn(mock(TransactionStatus.class));
    var service=new TaoChungTuTransaction(manager);
    assertThatThrownBy(()->service.thucHien(()->{throw new DataIntegrityViolationException("duplicate");})).isInstanceOf(DataIntegrityViolationException.class);
    verify(manager,times(3)).rollback(any());verify(manager,never()).commit(any());
  }
}
