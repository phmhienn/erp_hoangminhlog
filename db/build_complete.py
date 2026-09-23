"""Generate a fresh database: exact migration DDL plus coherent scenario seeds."""
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
MIGRATIONS = sorted((ROOT / 'backend/src/main/resources/db/migration').glob('V*__*.sql'), key=lambda p: int(p.name.split('__')[0][1:]))

def statements(text):
    # Split outside strings; strip SQL comments only outside strings.
    out, current, quote, i = [], [], None, 0
    while i < len(text):
        c = text[i]
        if quote:
            current.append(c)
            if c == "\\" and i + 1 < len(text):
                i += 1; current.append(text[i])
            elif c == quote:
                if i + 1 < len(text) and text[i + 1] == quote:
                    i += 1; current.append(text[i])
                else: quote = None
        elif c in "'\"`":
            quote = c; current.append(c)
        elif text[i:i+2] == '--' or c == '#':
            end = text.find('\n', i)
            i = len(text) if end < 0 else end
            current.append('\n')
        elif text[i:i+2] == '/*':
            end = text.find('*/', i+2)
            if end < 0: raise ValueError('Unclosed comment')
            i = end+1; current.append(' ')
        elif c == ';':
            if ''.join(current).strip(): out.append(''.join(current).strip()+';')
            current = []
        else: current.append(c)
        i += 1
    assert not quote, 'Unclosed SQL string'
    assert not ''.join(current).strip(), 'Missing SQL terminator'
    return out

def literal(value):
    if value is None: return 'NULL'
    if isinstance(value, (int,float)): return str(value)
    return "'" + str(value).replace("'", "''") + "'"

seed = []
populated = set()
def insert(table, columns, *rows):
    populated.add(table.lower())
    seed.append('INSERT INTO '+table+' ('+columns+') VALUES\n  '+',\n  '.join('('+','.join(map(literal, row))+')' for row in rows)+';')
def note(text): seed.append('-- '+text)
def code(prefix, n): return prefix+str(n).zfill(7)
def nv(n): return code('NV',n)
def ta(n): return code('TA',n)
def history(n, action, status, actor, time, comment=None):
    insert('LichSuDonHang','maDonHang,hanhDong,trangThai,maTaiKhoan,thoiGian,ghiChu',(code('DH',n),action,status,ta(actor),time,comment))

def generate():
    ddl=[]
    for path in MIGRATIONS:
        for statement in statements(path.read_text(encoding='utf-8-sig')):
            if re.match(r'^(CREATE|ALTER)\s', statement, re.I):
                ddl.append('-- '+path.name+'\n'+statement)
    tables=re.findall(r'CREATE TABLE\s+(\w+)', '\n'.join(ddl), re.I)
    assert len(tables)==36
    note('Danh mục nhân sự, tài khoản, khách hàng, dịch vụ, phương tiện giữ từ V2. Mật khẩu: 123456.')
    for statement in statements((MIGRATIONS[0].parent/'V2__seed.sql').read_text(encoding='utf-8-sig')):
        match=re.match(r'INSERT INTO\s+(\w+)',statement,re.I)
        if match and match[1] != 'SanPham':
            seed.append(statement); populated.add(match[1].lower())
    note('Danh mục mô tả kiện hàng của khách; không có giá mua/bán hoặc nhà cung cấp.')
    goods=[('SP001','Kiện đồ gia dụng','Kiện','Giữ nguyên bao bì, tránh va đập'),('SP002','Kiện quần áo','Kiện','Bảo quản khô ráo'),('SP003','Hồ sơ đóng kiện','Kiện','Tránh ẩm, giữ niêm phong')]
    insert('SanPham','maSP,tenSP,donViTinh,gia,trangThai,dieuKienBaoQuan',*[(a,b,c,None,'Đang sử dụng',d) for a,b,c,d in goods])
    scenarios={1:'Đơn nháp',2:'Chờ kinh doanh duyệt',3:'Cần bổ sung địa chỉ',4:'Đã hủy theo yêu cầu khách',5:'Đã duyệt, chưa tiếp nhận',6:'Phiếu nhập chờ duyệt',7:'Đang lưu kho, chưa lập chuyến',8:'Phiếu xuất chờ duyệt, chuyến nháp',9:'Đã xuất, chuyến được duyệt chờ tài xế nhận',10:'Đang vận chuyển',11:'Đã giao, COD chưa thu',12:'Đã đối soát và trả hết COD',13:'Đã giao, COD thiếu, phiếu thu chờ duyệt',14:'Đã đối soát, mới trả một phần COD'}
    recipients=['Nguyễn Minh Anh','Trần Đức Bình','Lê Ngọc Chi','Phạm Văn Dũng','Võ Thị Hà','Đỗ Thanh Huy','Bùi Thu Lan','Ngô Văn Nam','Hồ Thị Oanh','Mai Tuấn Phong','Lý Minh Quân','Đặng Hoài Sơn','Trịnh Bảo Trâm','Dương Hải Yến']
    cod={11:120000,12:600000,13:300000,14:450000}
    for n, scenario in scenarios.items():
        note(code('DH',n)+' — '+scenario)
        day='2026-09-20' if n>=11 else '2026-09-23'
        status='Đã tạo' if n<=6 else 'Đã nhập kho' if n<=9 else 'Đang giao' if n==10 else 'Đã đối soát' if n in (12,14) else 'Đã giao'
        if n==4: status='Đã hủy'
        customer='KH0000003' if n in (11,12,13) else 'KH0000001'
        sp,goods_name,_,_=goods[(n-1)%3]
        qty=2 if sp=='SP002' else 1
        weight=3 if sp=='SP002' else 2 if sp=='SP001' else 1
        insert('DonHang','maDonHang,maKhachHang,maDichVu,nguoiGui,nguoiNhan,sdtNguoiGui,sdtNguoiNhan,diachiLayHang,diachiGiaoHang,khoiLuong,tienCOD,phiVanChuyen,trangThai,lyDoHuy,ngayTao',
            (code('DH',n),customer,'DV01','Shop MiSa' if customer=='KH0000003' else 'Minh Phát',recipients[n-1],'0908123456' if customer=='KH0000003' else '0908221144','090111'+str(n).zfill(4),'78 Lê Văn Sỹ, TP.HCM' if customer=='KH0000003' else '120 Nguyễn Văn Trỗi, TP.HCM',str(n*10)+' Lê Lợi, TP.HCM',weight,cod.get(n,0),25000,status,'Khách đổi kế hoạch gửi hàng' if n==4 else None,day+' 07:00:00'))
        insert('HangHoa','maHangHoa,maDonHang,maSP,loaiHangHoa,soLuong,trongLuong',(code('HH',n),code('DH',n),sp,goods_name,qty,weight))
        history(n,'Tao','Đã tạo',1,day+' 07:00:00','Tiếp nhận yêu cầu gửi hàng')
        if n!=1: history(n,'GuiDuyet','Đã tạo',1,day+' 07:10:00')
        if n==3: history(n,'TuChoi','Đã tạo',2,day+' 07:20:00','Cần xác nhận số nhà người nhận')
        elif n==4: history(n,'Huy','Đã hủy',1,day+' 07:20:00','Khách đổi kế hoạch gửi hàng')
        elif n>=5: history(n,'Duyet','Đã tạo',2,day+' 07:20:00','Đủ thông tin tiếp nhận')
        if n>=6:
            insert('PHIEU_NHAP','maPhieuNhap,maDonHang,maNV,ngayNhap,trangThai,ghiChu,nguoiDuyet',
                (code('PN',n),code('DH',n),nv(3),day,'Chờ duyệt' if n==6 else 'Đã duyệt','Bao bì móp, chờ kiểm tra' if n==6 else 'Tiếp nhận đủ nguyên đơn',None if n==6 else nv(4)))
        if n>=7: history(n,'DoiTrangThai','Đã nhập kho',4,day+' 08:00:00',code('PN',n))
        if n>=8:
            insert('PHIEU_XUAT','maPhieuXuat,maNV,maDonHang,ngayXuat,lyDoXuat,trangThai,ghiChu,nguoiDuyet',
                (code('PX',n),nv(3),code('DH',n),day,'Bàn giao nguyên đơn cho vận chuyển','Chờ duyệt' if n==8 else 'Đã xuất',None,None if n==8 else nv(4)))
        start='14:00:00' if n==9 else '08:30:00'
        if n>=9: history(n,'XuatKho','Đã xuất',4,day+(' 13:45:00' if n==9 else ' 08:15:00'),code('PX',n))
        if n>=10: history(n,'DoiTrangThai','Đang giao',16 if n>=11 else 7,day+' '+start,'Nhận đủ nguyên đơn từ kho')
        if n>=11: history(n,'DoiTrangThai','Đã giao',16,day+' '+{11:'09:15:00',12:'09:30:00',13:'09:45:00',14:'10:00:00'}[n],'Người nhận đã ký nhận')
        if n in (12,14): history(n,'DoiTrangThai','Đã đối soát',11,day+' 11:00:00','Đối soát COD khớp')
    # Each active trip holds distinct orders; completed trip groups 11–14 in the same area.
    trips=[(1,[8],'Nháp',None,None,'2026-09-23 16:00:00','2026-09-23 18:00:00',None),
           (2,[9],'Đã phê duyệt','TX0000001',1,'2026-09-23 14:00:00','2026-09-23 16:00:00',None),
           (3,[10],'Đang giao','TX0000001',1,'2026-09-23 08:30:00','2026-09-23 11:30:00',None),
           (4,[11,12,13,14],'Hoàn thành','TX0000003',5,'2026-09-20 08:30:00','2026-09-20 10:30:00','2026-09-20 10:00:00')]
    for trip,orders,status,driver,vehicle,start,end,actual in trips:
        insert('ChuyenVan','MaChuyen,MaNguoiLap,MaTaiXe,MaPhuongTien,MaDonHang,NgayKhoiHanh,ThoiGianDuKien,ThoiGianThucTe,TrangThai',(trip,nv(5),driver,vehicle,code('DH',orders[0]),start,end,actual,status))
        insert('ChiTietChuyenHang','MaChuyen,MaDonHang',*[(trip,code('DH',n)) for n in orders])
        insert('LoTrinh','MaChuyen,DiemXuatPhat,DiemKetThuc,KhoangCach,ThoiGianDuKien,MoTa',(trip,'Kho Hoàng Minh','Đường Lê Lợi, TP.HCM',12,180 if trip==3 else 120,'Giao nguyên đơn theo địa chỉ người nhận'))
        if trip>=3:
            for n in orders:
                insert('MocHanhTrinh','maChuyen,maDonHang,diem,moTa,thoiGian,maNVCapNhat',(trip,code('DH',n),'Đã lấy hàng','Đã đối chiếu phiếu xuất',start,nv(7 if trip==3 else 16)))
                if actual:
                    delivered = '2026-09-20 '+{11:'09:15:00',12:'09:30:00',13:'09:45:00',14:'10:00:00'}[n]
                    insert('MocHanhTrinh','maChuyen,maDonHang,diem,moTa,thoiGian,maNVCapNhat',(trip,code('DH',n),'Đến điểm giao','Người nhận ký nhận',delivered,nv(16)))
                    insert('MinhChungGiaoHang','maMinhChung,maDonHang,loaiMinhChung,duongDan,maNVTai,thoiGianTai',(code('MC',n),code('DH',n),'ChuKy','demo/chu-ky-'+code('DH',n)+'.png',nv(16),delivered))
    insert('SuCoVanTai','MaChuyen,MaDonHang,MaTaiXe,LoaiSuCo,MoTa,ThoiGian,TrangThai,HuongXuLy,NguoiXuLy,ThoiGianXuLy',(3,code('DH',10),'TX0000001','Tắc đường','Ùn tắc tại điểm rẽ','2026-09-23 09:00:00','Đang xử lý','Chuyển sang tuyến đường song song',nv(5),'2026-09-23 09:10:00'))
    insert('BienBanSuCo','maBienBan,maPhieuNhap,maDonHang,loaiSuCo,moTa,maNVLap,ngayLap',(code('BB',1),code('PN',6),code('DH',6),'Hư hỏng','Bao bì móp; chờ khách xác nhận trước khi duyệt tiếp nhận',nv(3),'2026-09-23 07:50:00'))
    insert('PHIEU_KIEM_KE','maPhieuKiemKe,ngayKiemKe,khuVucKiemKe,maNV,trangThai,ghiChu',(code('KK',1),'2026-09-23','Khu A',nv(3),'Đã chốt','Đối chiếu hai đơn đang lưu kho'),(code('KK',2),'2026-09-20','Khu bàn giao',nv(3),'Đã chốt','Đối chiếu sau bàn giao'))
    insert('KIEM_KE_DON_HANG','maPhieuKiemKe,maDonHang,soLuongHeThong,soLuongThucTe,chenhLech,ghiChu',(code('KK',1),code('DH',7),1,1,0,'Đủ nguyên đơn'),(code('KK',1),code('DH',8),1,1,0,'Chờ duyệt phiếu xuất'),(code('KK',2),code('DH',12),0,0,0,'Đã bàn giao'))
    note('Bảng chi tiết sản phẩm cũ: chỉ dữ liệu đối chiếu lịch sử của đơn 12, không tạo tồn sản phẩm mới.')
    insert('CHI_TIET_PHIEU_NHAP','maPhieuNhap,maSP,soLuong,tinhTrangHang',(code('PN',12),'SP003',1,'Đạt'))
    insert('CHI_TIET_PHIEU_XUAT','maPhieuXuat,maSP,soLuong',(code('PX',12),'SP003',1))
    insert('TON_KHO','maTonKho,maSP,maViTri,soLuongTon,ngayCapNhat',(code('TK',1),'SP003','Khu bàn giao',0,'2026-09-20 08:15:00'))
    insert('CHI_TIET_KIEM_KE','maPhieuKiemKe,maSP,soLuongHeThong,soLuongThucTe,chenhLech,ghiChu',(code('KK',2),'SP003',0,0,0,'Đối chiếu lịch sử sau xuất nguyên đơn 12'))
    insert('ChiPhiLuuKho','maChiPhi,maPhieuNhap,soTien,ngayGhi,maNVGhi',(code('CP',1),code('PN',7),10000,'2026-09-23 09:00:00',nv(11)))
    insert('YeuCauCapNhatHoSo','maNV,noiDung,trangThai,thoiGianGui,nguoiXuLy,thoiGianXuLy,phanHoi',(nv(14),'Đề nghị cập nhật địa chỉ liên hệ','Chờ xử lý','2026-09-23 08:00:00',None,None,None),(nv(16),'Xác minh lại số điện thoại','Đã xử lý','2026-09-20 15:00:00',nv(9),'2026-09-20 16:00:00','Đã đối chiếu, số điện thoại hiện tại chính xác'))
    for n in (11,12,13,14):
        actual=None if n==11 else 250000 if n==13 else cod[n]
        status='Chưa thu' if n==11 else 'Sai lệch' if n==13 else 'Đã đối soát'
        insert('GiaoDichCOD','maGiaoDich,maDonHang,soTienPhaiThu,soTienThucThu,trangThai,ngayTao,thoiGianCapNhat,nguoiCapNhat,lyDoSaiLech,nguoiXuLySaiLech,thoiGianXuLySaiLech',
            (code('GD',n),code('DH',n),cod[n],actual,status,'2026-09-20',None if n==11 else '2026-09-20 11:00:00',None if n==11 else nv(11),'Thiếu 50.000 đồng, đang xác minh với tài xế' if n==13 else None,nv(11) if n==13 else None,'2026-09-20 11:00:00' if n==13 else None))
    for n in (12,14):
        insert('DoiSoatCOD','maDoiSoat,ngayDoiSoat,tongTien,chenhLech,trangThai,nguoiDoiSoat,thoiGian',(code('DS',n),'2026-09-20',cod[n],0,'Khớp',nv(11),'2026-09-20 11:00:00'))
        insert('ChiTietDoiSoat','maDoiSoat,maGiaoDich',(code('DS',n),code('GD',n)))
        for kind,amount,hour in [('Thu',cod[n],12),('Chi',600000 if n==12 else 200000,13)]:
            voucher=code('PT' if kind=='Thu' else 'PC',n)
            insert('PhieuThuChi','maPhieu,loaiPhieu,maGiaoDich,soTien,ngayLap,noiDung,trangThai,nguoiLap,nguoiDuyet,nguoiXacNhan,thoiGianXacNhan',(voucher,kind,code('GD',n),amount,'2026-09-20',kind+' COD '+code('DH',n),'Đã thực hiện',nv(11),nv(13),nv(12),f'2026-09-20 {hour}:00:00'))
            insert('SoQuy','maSoQuy,maPhieu,soTienThu,soTienChi,ngayGhiSo,maNVGhiSo',(code('SQ',n*2+(kind=='Chi')),voucher,amount if kind=='Thu' else 0,amount if kind=='Chi' else 0,'2026-09-20',nv(12)))
        insert('CongNo','maCongNo,maKhachHang,soTienPhaiTra,soTienDaTra,trangThai,ngayCapNhat',(code('CN',n),'KH0000003' if n==12 else 'KH0000001',cod[n],600000 if n==12 else 200000,'Đã tất toán' if n==12 else 'Còn nợ','2026-09-20 13:00:00'))
    insert('PhieuThuChi','maPhieu,loaiPhieu,maGiaoDich,soTien,ngayLap,noiDung,trangThai,nguoiLap',(code('PT',13),'Thu',code('GD',13),250000,'2026-09-20','Thu COD thực tế; thiếu 50.000 đang xác minh','Chờ duyệt',nv(11)))
    assert all(t.lower() in populated for t in tables), set(tables)-populated
    header="""-- ERP Hoàng Minh: CSDL hoàn chỉnh, dữ liệu demo theo workflow đơn hàng.
-- Cấu trúc: toàn bộ CREATE/ALTER của V1–V12; không sửa migration gốc.
-- Dữ liệu: danh mục V2 và kịch bản mới, không trộn các seed thử nghiệm V3–V5.
-- Chỉ chạy MỘT LẦN trên DB MỚI. Không chạy lên DB cloud đang có dữ liệu.
-- Mật khẩu tài khoản demo: 123456. Ảnh/chữ ký demo chỉ là đường dẫn tham chiếu.
-- Backend đang tắt Flyway. Nếu bật cho DB nhập sẵn này, baseline-on-migrate=true, baseline-version=12.
SET NAMES utf8mb4;
CREATE DATABASE erp_hoangminh_full_demo CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE erp_hoangminh_full_demo;
"""
    checks='\n-- Kiểm đếm đủ 36 bảng; sổ quỹ và công nợ còn 250.000 đồng.\n'
    checks+='\nUNION ALL\n'.join(f"SELECT '{t}' tenBang, COUNT(*) soBanGhi FROM `{t}`" for t in tables)+';\n'
    checks+='SELECT * FROM vw_SoDuQuy;\nSELECT * FROM vw_CongNoConLai;\n'
    output=header+'\n\n'.join(ddl)+'\n\nSTART TRANSACTION;\n'+'\n\n'.join(seed)+'\nCOMMIT;\n'+checks
    (ROOT/'db/ERP_hoangminh_hoan_chinh.sql').write_text(output,encoding='utf-8')
    print(f'Generated {len(tables)} tables, 14 coherent order scenarios.')

if __name__=='__main__': generate()
