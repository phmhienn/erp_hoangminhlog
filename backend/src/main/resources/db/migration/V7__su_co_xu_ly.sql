-- =====================================================================
-- V7 — Bổ sung phương án xử lý cho sự cố vận tải (HT3)
-- G22  Đặc tả 3.3.2 vai trò nhân viên điều phối: "Cập nhật và xử lý sự cố vận tải: ghi nhận, xử lý
--      tình huống phát sinh; điều động xe thay thế hoặc đổi lộ trình kịp thời" — điều phối phải cập
--      nhật được trạng thái và phương án xử lý vào chính sự cố tài xế đã báo, để tài xế đọc lại
--      (3.3.2 vai trò tài xế: "yêu cầu hỗ trợ/phê duyệt phương án thay thế").
--      Lược đồ 3.3.4 chỉ có TrangThai nên bổ sung ba cột lưu vết việc xử lý.
-- =====================================================================

ALTER TABLE SuCoVanTai
  ADD COLUMN HuongXuLy    VARCHAR(500) NULL COMMENT 'Phương án xử lý điều phối gửi cho tài xế',
  ADD COLUMN NguoiXuLy    VARCHAR(20)  NULL,
  ADD COLUMN ThoiGianXuLy DATETIME     NULL,
  ADD CONSTRAINT fk_scv_xuly FOREIGN KEY (NguoiXuLy) REFERENCES NhanVien (maNV);
