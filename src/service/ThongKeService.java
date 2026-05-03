package service;

import model.LichSuChanDoan;
import java.util.*;

public class ThongKeService {

    // Đếm số lần xuất hiện của từng bệnh
    public static Map<String, Integer> demTheoBenh() {
        Map<String, Integer> dem = new HashMap<>();
        for (LichSuChanDoan ls : LichSuService.getInstance().getDanhSach()) {
            dem.merge(ls.getTenBenh(), 1, Integer::sum);
        }
        return dem;
    }

    // Đếm số lần xuất hiện của từng triệu chứng
    public static Map<String, Integer> demTheoTrieuChung() {
        Map<String, Integer> dem = new HashMap<>();
        for (LichSuChanDoan ls : LichSuService.getInstance().getDanhSach()) {
            for (String tc : ls.getTrieuChungDaChon().split(",")) {
                String s = tc.trim();
                if (!s.isEmpty()) dem.merge(s, 1, Integer::sum);
            }
        }
        return dem;
    }

    // Lấy bệnh xuất hiện nhiều nhất
    public static String benhPhoNhat() {
        Map<String, Integer> dem = demTheoBenh();
        if (dem.isEmpty()) return "--";
        return Collections.max(dem.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    // Lấy triệu chứng xuất hiện nhiều nhất
    public static String trieuChungPhoNhat() {
        Map<String, Integer> dem = demTheoTrieuChung();
        if (dem.isEmpty()) return "--";
        return Collections.max(dem.entrySet(), Map.Entry.comparingByValue()).getKey();
    }
}