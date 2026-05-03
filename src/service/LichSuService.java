package service;

import model.LichSuChanDoan;
import java.util.ArrayList;
import java.util.List;

public class LichSuService {

    // Singleton: chỉ có 1 instance dùng chung toàn app
    private static LichSuService instance;
    private List<LichSuChanDoan> danhSach = new ArrayList<>();

    private LichSuService() {}

    public static LichSuService getInstance() {
        if (instance == null) instance = new LichSuService();
        return instance;
    }

    public void themLichSu(String thoiGian, String trieuChung,
                           String tenBenh, double phanTram,
                           String doTuoi, String thoiGianXuatHien) {
        danhSach.add(new LichSuChanDoan(
            thoiGian, trieuChung, tenBenh,
            phanTram, doTuoi, thoiGianXuatHien));
    }

    public List<LichSuChanDoan> getDanhSach() { return danhSach; }

    // Tìm kiếm theo tên bệnh
    public List<LichSuChanDoan> timKiem(String tuKhoa) {
        List<LichSuChanDoan> ketQua = new ArrayList<>();
        for (LichSuChanDoan ls : danhSach) {
            if (ls.getTenBenh().toLowerCase().contains(tuKhoa.toLowerCase()) ||
                ls.getTrieuChungDaChon().toLowerCase().contains(tuKhoa.toLowerCase())) {
                ketQua.add(ls);
            }
        }
        return ketQua;
    }
}