package vn.hoangminh.erp.Ht4NhanSu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.hoangminh.erp.Ht4NhanSu.domain.TaiXe;

/** Repository nội bộ của module HT4 (bảng dùng chung {@code TaiXe}). */
public interface TaiXeRepository extends JpaRepository<TaiXe, String> {}
