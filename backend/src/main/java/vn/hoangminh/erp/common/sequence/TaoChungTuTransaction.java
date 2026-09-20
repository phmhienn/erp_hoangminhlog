package vn.hoangminh.erp.common.sequence;

import java.util.function.Supplier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/** Thử lại trong transaction mới: transaction cũ đã rollback hoàn toàn khi trùng mã. */
@Component
public class TaoChungTuTransaction {
  private final TransactionTemplate transaction;
  public TaoChungTuTransaction(PlatformTransactionManager manager) { transaction = new TransactionTemplate(manager); }
  public <T> T thucHien(Supplier<T> work) {
    for (int lan=0; ; lan++) {
      try { return transaction.execute(status -> work.get()); }
      catch (RuntimeException ex) {
        Throwable cause=ex;
        boolean collision=false;
        while(cause!=null) {
          if(cause instanceof DataIntegrityViolationException
              || cause instanceof org.hibernate.exception.ConstraintViolationException
              || cause instanceof jakarta.persistence.EntityExistsException) collision=true;
          cause=cause.getCause();
        }
        if(!collision) throw ex;
        if(lan>=2) throw new DataIntegrityViolationException("Khong the cap ma chung tu",ex);
      }
    }
  }
}
