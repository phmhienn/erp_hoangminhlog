-- [G24] Chuẩn hoá trạng thái chuyến bị lệch chữ giữa dữ liệu mẫu và mã nguồn.
-- V5 gieo 'Chờ điều chỉnh' trong khi Ht3Service.yeuCauDieuChinh ghi 'Cần điều chỉnh', nên chuyến
-- CV0000001 không lọt vào bất kỳ bộ lọc nào: không sửa, không xoá, không phân công, không duyệt được
-- mà vẫn hiện ở "Nhiệm vụ của tôi" (Phòng vận tải – giao nhận, mục 2.3).
UPDATE ChuyenVan SET TrangThai = 'Cần điều chỉnh' WHERE TrangThai = 'Chờ điều chỉnh';

-- Chạy lại bước dọn của V8 cho những dòng vừa được chuẩn hoá: chuyến chưa khởi hành mà đơn đã được
-- chuyến khác giao xong là chuyến lập thừa.
UPDATE ChuyenVan c
SET c.TrangThai = 'Đã hủy'
WHERE c.TrangThai IN ('Nháp', 'Cần điều chỉnh', 'Đã phân công', 'Đã phê duyệt')
  AND NOT EXISTS (
    SELECT 1 FROM ChiTietChuyenHang ct JOIN DonHang d ON d.MaDonHang = ct.MaDonHang
    WHERE ct.MaChuyen = c.MaChuyen AND d.TrangThai NOT IN ('Đã giao', 'Đã đối soát', 'Đã hủy'))
  AND EXISTS (SELECT 1 FROM ChiTietChuyenHang ct WHERE ct.MaChuyen = c.MaChuyen);
