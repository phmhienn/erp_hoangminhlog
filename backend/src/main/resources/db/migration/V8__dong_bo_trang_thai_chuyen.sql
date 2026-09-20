-- [G23] Dọn hệ quả của lỗi "một đơn lập được nhiều chuyến" (Phòng vận tải – giao nhận, mục 1.1).
-- Trước bản vá, lập kế hoạch không đánh dấu đơn đã có chuyến nên cùng một đơn sinh ra nhiều chuyến
-- với nhiều tài xế; chuyến thừa treo ở "Nháp"/"Cần điều chỉnh"/"Đang giao" dù đơn đã giao xong,
-- khiến "Nhiệm vụ của tôi" và "Lịch sử cá nhân" hiển thị sai trạng thái.

-- 1. Chuyến đang giao mà mọi đơn đã giao/đối soát thì chốt "Hoàn thành" và ghi thời gian thực tế.
UPDATE ChuyenVan c
SET c.TrangThai = 'Hoàn thành',
    c.ThoiGianThucTe = COALESCE(c.ThoiGianThucTe, NOW())
WHERE c.TrangThai = 'Đang giao'
  AND NOT EXISTS (
    SELECT 1 FROM ChiTietChuyenHang ct JOIN DonHang d ON d.MaDonHang = ct.MaDonHang
    WHERE ct.MaChuyen = c.MaChuyen AND d.TrangThai NOT IN ('Đã giao', 'Đã đối soát', 'Đã hủy'))
  AND EXISTS (SELECT 1 FROM ChiTietChuyenHang ct WHERE ct.MaChuyen = c.MaChuyen);

-- 2. Chuyến chưa khởi hành mà đơn đã được chuyến khác giao xong là chuyến lập thừa -> huỷ.
UPDATE ChuyenVan c
SET c.TrangThai = 'Đã hủy'
WHERE c.TrangThai IN ('Nháp', 'Cần điều chỉnh', 'Đã phân công', 'Đã phê duyệt')
  AND NOT EXISTS (
    SELECT 1 FROM ChiTietChuyenHang ct JOIN DonHang d ON d.MaDonHang = ct.MaDonHang
    WHERE ct.MaChuyen = c.MaChuyen AND d.TrangThai NOT IN ('Đã giao', 'Đã đối soát', 'Đã hủy'))
  AND EXISTS (SELECT 1 FROM ChiTietChuyenHang ct WHERE ct.MaChuyen = c.MaChuyen);

-- 3. Cùng một đơn còn nằm ở nhiều chuyến còn hiệu lực: giữ chuyến tiến xa nhất (hoà thì giữ mã nhỏ
--    nhất), huỷ các chuyến còn lại để từ nay mỗi đơn chỉ còn đúng một kế hoạch.
UPDATE ChuyenVan c
JOIN ChiTietChuyenHang ct ON ct.MaChuyen = c.MaChuyen
JOIN (
  SELECT ct2.MaDonHang,
         CAST(SUBSTRING_INDEX(GROUP_CONCAT(c2.MaChuyen ORDER BY
           FIELD(c2.TrangThai, 'Đang giao', 'Đã phê duyệt', 'Đã phân công', 'Cần điều chỉnh', 'Nháp'),
           c2.MaChuyen), ',', 1) AS UNSIGNED) AS GiuLai
  FROM ChiTietChuyenHang ct2 JOIN ChuyenVan c2 ON c2.MaChuyen = ct2.MaChuyen
  WHERE c2.TrangThai IN ('Nháp', 'Cần điều chỉnh', 'Đã phân công', 'Đã phê duyệt', 'Đang giao')
  GROUP BY ct2.MaDonHang
  HAVING COUNT(DISTINCT c2.MaChuyen) > 1
) k ON k.MaDonHang = ct.MaDonHang
SET c.TrangThai = 'Đã hủy'
WHERE c.TrangThai IN ('Nháp', 'Cần điều chỉnh', 'Đã phân công', 'Đã phê duyệt', 'Đang giao')
  AND c.MaChuyen <> k.GiuLai;
