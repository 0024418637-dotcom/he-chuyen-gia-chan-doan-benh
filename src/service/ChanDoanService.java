package service;

import model.Benh;
import java.util.*;

/**
 * ChanDoanService — Hệ chuyên gia suy luận dựa trên tập luật (Rule-Based).
 *
 * Mỗi luật có dạng:  S_i ∧ S_j ∧ ... ∧ T_k ∧ D_l → B_n
 * Input : danh sách triệu chứng + mã độ tuổi (T1-T4) + mã thời gian (D1-D4)
 * Output: [Benh, double phanTram] hoặc null nếu không khớp
 *
 * Cách tính % phù hợp (partial match):
 *   - Triệu chứng khớp   : đếm số triệu chứng trong rule có mặt trong input
 *   - Độ tuổi khớp (0/1) : 1 nếu T của rule == T của input, else 0
 *   - Thời gian khớp (0/1): 1 nếu D của rule == D của input, else 0
 *   - Tổng điều kiện = số triệu chứng + 2 (T + D)
 *   - % = (triệuChứngKhớp * TRONG_SO_TC + tuoiKhop * W_T + tgKhop * W_D) / tổng_trọng_số * 100
 *
 * Trọng số (có thể điều chỉnh):
 *   TRONG_SO_TC = 1.0 mỗi triệu chứng
 *   W_T (độ tuổi) = 1.5  — quan trọng hơn một chút
 *   W_D (thời gian) = 1.0
 */
public class ChanDoanService {

    // =====================================================================
    // TẬP SỰ KIỆN — ánh xạ tên hiển thị → mã Sx
    // =====================================================================
    private static final Map<String, String> MA_TRIEU_CHUNG = new LinkedHashMap<>();
    static {
        MA_TRIEU_CHUNG.put("Sốt",                  "S1");
        MA_TRIEU_CHUNG.put("Ho",                   "S2");
        MA_TRIEU_CHUNG.put("Đau họng",             "S3");
        MA_TRIEU_CHUNG.put("Sổ mũi",               "S4");
        MA_TRIEU_CHUNG.put("Đau đầu",              "S5");
        MA_TRIEU_CHUNG.put("Mệt mỏi",              "S6");
        MA_TRIEU_CHUNG.put("Khó ngủ",              "S7");
        MA_TRIEU_CHUNG.put("Căng thẳng/Lo âu",     "S8");
        MA_TRIEU_CHUNG.put("Đau bụng",             "S9");
        MA_TRIEU_CHUNG.put("Buồn nôn",             "S10");
        MA_TRIEU_CHUNG.put("Tiêu chảy",            "S11");
        MA_TRIEU_CHUNG.put("Nổi mẩn",              "S12");
        MA_TRIEU_CHUNG.put("Ngứa",                 "S13");
        MA_TRIEU_CHUNG.put("Hắt hơi",              "S14");
        MA_TRIEU_CHUNG.put("Nghẹt mũi",            "S15");
        MA_TRIEU_CHUNG.put("Chóng mặt",            "S16");
        MA_TRIEU_CHUNG.put("Khó tập trung",        "S17");
        MA_TRIEU_CHUNG.put("Chán ăn",              "S18");
        MA_TRIEU_CHUNG.put("Đau cơ",               "S19");
        MA_TRIEU_CHUNG.put("Đau vùng xoang",       "S20");
    }

    // Ánh xạ tên độ tuổi hiển thị → mã Tx
    private static final Map<String, String> MA_DO_TUOI = new LinkedHashMap<>();
    static {
        MA_DO_TUOI.put("Dưới 5 tuổi",         "T1");
        MA_DO_TUOI.put("5 - 12 tuổi",         "T2");
        MA_DO_TUOI.put("18 - 60 tuổi",        "T3");
        MA_DO_TUOI.put("Trên 60 tuổi",        "T4");
        // Alias cho ComboBox cũ
        MA_DO_TUOI.put("Dưới 18",             "T2");
        MA_DO_TUOI.put("18 - 30",             "T3");
        MA_DO_TUOI.put("30 - 50",             "T3");
        MA_DO_TUOI.put("Trên 50",             "T4");
    }

    // Ánh xạ tên thời gian hiển thị → mã Dx
    private static final Map<String, String> MA_THOI_GIAN = new LinkedHashMap<>();
    static {
        MA_THOI_GIAN.put("1 - 2 ngày",        "D1");
        MA_THOI_GIAN.put("3 - 5 ngày",        "D2");
        MA_THOI_GIAN.put("5 - 7 ngày",        "D3");
        MA_THOI_GIAN.put("Trên 7 ngày",       "D4");
        // Alias cho ComboBox cũ
        MA_THOI_GIAN.put("Dưới 1 ngày",       "D1");
        MA_THOI_GIAN.put("1 - 3 ngày",        "D1");
        MA_THOI_GIAN.put("3 - 7 ngày",        "D2");
        MA_THOI_GIAN.put("Hơn 1 tuần",        "D4");
    }
    public String giaiThich(List<String> input, Rule rule) {
    return "Dựa trên triệu chứng: " + input.toString() +
           ", hệ thống suy luận theo luật → " + rule.maBenh;
}
    // =====================================================================
    // CLASS RULE — đại diện cho 1 luật:  S∧S∧...∧T∧D → B
    // =====================================================================
    private static class Rule {
        List<String> trieuChung;  // Danh sách mã Sx
        String doTuoi;            // Mã Tx
        String thoiGian;          // Mã Dx
        String maBenh;            // Mã Bx kết luận

        Rule(List<String> trieuChung, String doTuoi, String thoiGian, String maBenh) {
            this.trieuChung = trieuChung;
            this.doTuoi     = doTuoi;
            this.thoiGian   = thoiGian;
            this.maBenh     = maBenh;
        }
    }

    // Trọng số
    private static final double W_T  = 1.5;  // trọng số độ tuổi
    private static final double W_D  = 1.0;  // trọng số thời gian
    private static final double W_TC = 1.0;  // trọng số mỗi triệu chứng

    // Danh sách tất cả rule
    private List<Rule> tapLuat = new ArrayList<>();

    // Danh sách bệnh (dữ liệu mô tả)
    private List<Benh> danhSachBenh = new ArrayList<>();

    // =====================================================================
    // CONSTRUCTOR — khởi tạo toàn bộ tập luật + dữ liệu bệnh
    // =====================================================================
    // Improve diagnosis: return top 3 possible diseases instead of only one
    public ChanDoanService() {
        khoiTaoDanhSachBenh();
        khoiTaoTapLuat();
    }

    // =====================================================================
    // KHỞI TẠO DỮ LIỆU BỆNH (17 bệnh)
    // =====================================================================
    private void khoiTaoDanhSachBenh() {
        danhSachBenh.add(new Benh("B1",  "Cảm cúm",
            "Bệnh do virus gây ra, lây qua đường hô hấp, thường gặp khi thời tiết thay đổi.",
            "Nghỉ ngơi, uống nhiều nước ấm, dùng thuốc hạ sốt nếu cần. Gặp bác sĩ nếu sốt cao trên 38.5°C.",
            "camcum.png", "Trung bình"));

        danhSachBenh.add(new Benh("B2",  "Viêm họng",
            "Viêm nhiễm vùng họng do vi khuẩn hoặc virus, thường gặp khi thời tiết lạnh.",
            "Súc miệng nước muối ấm, uống nhiều nước, tránh đồ lạnh. Dùng kháng sinh nếu do vi khuẩn theo chỉ định.",
            "viemhong.png", "Nhẹ"));

        danhSachBenh.add(new Benh("B3",  "Đau dạ dày",
            "Viêm loét dạ dày do ăn uống không điều độ hoặc stress kéo dài.",
            "Ăn đúng giờ, tránh đồ cay nóng và rượu bia. Dùng thuốc giảm acid theo chỉ định bác sĩ.",
            "daday.png", "Trung bình"));

        danhSachBenh.add(new Benh("B4",  "Rối loạn tiêu hóa",
            "Hệ tiêu hóa hoạt động không ổn định do ăn uống không vệ sinh hoặc thực phẩm không phù hợp.",
            "Ăn nhẹ, uống oresol nếu mất nước. Tránh đồ ăn lạ. Gặp bác sĩ nếu kéo dài quá 3 ngày.",
            "roiloan.png", "Nhẹ"));

        danhSachBenh.add(new Benh("B5",  "Mất ngủ",
            "Khó đi vào giấc ngủ hoặc ngủ không sâu do áp lực, stress hoặc thói quen sinh hoạt không hợp lý.",
            "Ngủ đúng giờ, tránh caffeine buổi tối, không dùng thiết bị điện tử trước khi ngủ. Tập thể dục nhẹ.",
            "matngungcu.png", "Nhẹ"));

        danhSachBenh.add(new Benh("B6",  "Stress tâm lý",
            "Căng thẳng tâm lý do áp lực học tập, công việc hoặc cuộc sống kéo dài.",
            "Nghỉ ngơi hợp lý, chia sẻ với người thân, tập yoga hoặc thiền định. Gặp chuyên gia tâm lý nếu kéo dài.",
            "stress.png", "Trung bình"));

        danhSachBenh.add(new Benh("B7",  "Dị ứng nhẹ",
            "Phản ứng của cơ thể với thực phẩm, môi trường hoặc thời tiết.",
            "Tránh tiếp xúc tác nhân gây dị ứng, dùng thuốc kháng histamine nhẹ. Theo dõi nếu triệu chứng nặng hơn.",
            "diung.png", "Nhẹ"));

        danhSachBenh.add(new Benh("B8",  "Cảm lạnh",
            "Nhiễm virus nhẹ ở đường hô hấp trên, thường do tiếp xúc lạnh đột ngột.",
            "Giữ ấm cơ thể, uống nước gừng mật ong ấm, nghỉ ngơi đầy đủ. Có thể tự khỏi sau 3-5 ngày.",
            "camlanh.png", "Nhẹ"));

        danhSachBenh.add(new Benh("B9",  "Viêm mũi dị ứng",
            "Viêm niêm mạc mũi do phản ứng với bụi, phấn hoa hoặc thời tiết thay đổi.",
            "Hạn chế tiếp xúc dị nguyên, dùng thuốc xịt mũi theo chỉ định. Đeo khẩu trang khi ra ngoài.",
            "viemmuidiung.png", "Nhẹ"));

        danhSachBenh.add(new Benh("B10", "Đau nửa đầu",
            "Đau đầu một bên liên quan đến rối loạn thần kinh hoặc căng thẳng kéo dài.",
            "Nghỉ ngơi nơi yên tĩnh, tránh ánh sáng mạnh và tiếng ồn. Dùng thuốc giảm đau theo chỉ định bác sĩ.",
            "dauniudau.png", "Trung bình"));

        danhSachBenh.add(new Benh("B11", "Ngộ độc thực phẩm",
            "Rối loạn tiêu hóa do ăn phải thực phẩm nhiễm khuẩn hoặc không đảm bảo vệ sinh.",
            "Bù nước bằng oresol, ăn cháo loãng. Đến cơ sở y tế ngay nếu nôn nhiều hoặc mất nước nặng.",
            "ngodocdoc.png", "Nặng"));

        danhSachBenh.add(new Benh("B12", "Thiếu ngủ",
            "Ngủ không đủ giấc trong thời gian dài do áp lực hoặc sinh hoạt không hợp lý.",
            "Thiết lập giờ ngủ cố định, tránh ngủ ngày quá nhiều. Hạn chế caffeine và điện thoại trước khi ngủ.",
            "thieungu.png", "Nhẹ"));

        danhSachBenh.add(new Benh("B13", "Trầm cảm nhẹ",
            "Rối loạn tâm lý với biểu hiện buồn bã, mất hứng thú và giảm năng lượng.",
            "Tìm kiếm hỗ trợ từ người thân và chuyên gia tâm lý. Duy trì hoạt động thể chất nhẹ hàng ngày.",
            "tramcam.png", "Nặng"));

        danhSachBenh.add(new Benh("B14", "Viêm da",
            "Viêm nhiễm trên da do dị ứng, vi khuẩn hoặc tác nhân từ môi trường.",
            "Giữ vệ sinh da sạch, tránh gãi mạnh. Dùng kem bôi theo chỉ định bác sĩ da liễu.",
            "viemda.png", "Nhẹ"));

        danhSachBenh.add(new Benh("B15", "Táo bón",
            "Khó đi tiêu do chế độ ăn thiếu chất xơ hoặc uống ít nước.",
            "Uống đủ 2 lít nước/ngày, bổ sung rau xanh và trái cây. Tập thể dục nhẹ sau bữa ăn.",
            "taobon.png", "Nhẹ"));

        danhSachBenh.add(new Benh("B16", "Suy nhược cơ thể",
            "Cơ thể mệt mỏi kéo dài do thiếu dinh dưỡng, stress hoặc làm việc quá sức.",
            "Nghỉ ngơi đầy đủ, bổ sung vitamin và khoáng chất. Ăn uống đủ bữa và cân bằng dinh dưỡng.",
            "suynhuoc.png", "Trung bình"));

        danhSachBenh.add(new Benh("B17", "Viêm xoang",
            "Viêm nhiễm các xoang trong vùng mũi do vi khuẩn hoặc dị ứng.",
            "Xông hơi nước muối sinh lý, giữ ẩm không khí. Điều trị sớm để tránh biến chứng viêm xoang mạn tính.",
            "viemxoang.png", "Trung bình"));
    }

    // =====================================================================
    // KHỞI TẠO TẬP LUẬT — 170 luật từ file tập luật
    // Mỗi luật: new Rule([Sx, Sy,...], "Tx", "Dx", "Bx")
    // =====================================================================
    private void khoiTaoTapLuat() {
        // ----- B1: CẢM CÚM (10 luật) -----
        // Triệu chứng chính: S1(Sốt), S2(Ho), S4(Sổ mũi)
        tapLuat.add(new Rule(Arrays.asList("S1","S2","S4"), "T1", "D1", "B1"));
        tapLuat.add(new Rule(Arrays.asList("S1","S2","S4"), "T2", "D1", "B1"));
        tapLuat.add(new Rule(Arrays.asList("S1","S2","S4"), "T3", "D2", "B1"));
        tapLuat.add(new Rule(Arrays.asList("S1","S2","S4"), "T4", "D3", "B1"));
        tapLuat.add(new Rule(Arrays.asList("S1","S2","S4"), "T1", "D4", "B1"));
        tapLuat.add(new Rule(Arrays.asList("S1","S2","S4"), "T2", "D4", "B1"));
        tapLuat.add(new Rule(Arrays.asList("S1","S2","S4"), "T3", "D1", "B1"));
        tapLuat.add(new Rule(Arrays.asList("S1","S2","S4"), "T4", "D2", "B1"));
        tapLuat.add(new Rule(Arrays.asList("S1","S2","S4"), "T1", "D3", "B1"));
        tapLuat.add(new Rule(Arrays.asList("S1","S2","S4"), "T2", "D3", "B1"));

        // ----- B2: VIÊM HỌNG (10 luật) -----
        // Triệu chứng chính: S3(Đau họng), S2(Ho), S5(Đau đầu)
        tapLuat.add(new Rule(Arrays.asList("S3","S2","S5"), "T1", "D1", "B2"));
        tapLuat.add(new Rule(Arrays.asList("S3","S2","S5"), "T2", "D1", "B2"));
        tapLuat.add(new Rule(Arrays.asList("S3","S2","S5"), "T3", "D2", "B2"));
        tapLuat.add(new Rule(Arrays.asList("S3","S2","S5"), "T4", "D3", "B2"));
        tapLuat.add(new Rule(Arrays.asList("S3","S2","S5"), "T1", "D4", "B2"));
        tapLuat.add(new Rule(Arrays.asList("S3","S2","S5"), "T2", "D4", "B2"));
        tapLuat.add(new Rule(Arrays.asList("S3","S2","S5"), "T3", "D1", "B2"));
        tapLuat.add(new Rule(Arrays.asList("S3","S2","S5"), "T4", "D2", "B2"));
        tapLuat.add(new Rule(Arrays.asList("S3","S2","S5"), "T1", "D3", "B2"));
        tapLuat.add(new Rule(Arrays.asList("S3","S2","S5"), "T2", "D3", "B2"));

        // ----- B3: ĐAU DẠ DÀY (10 luật) -----
        // Triệu chứng chính: S9(Đau bụng), S10(Buồn nôn), S6(Mệt mỏi)
        tapLuat.add(new Rule(Arrays.asList("S9","S10","S6"), "T1", "D1", "B3"));
        tapLuat.add(new Rule(Arrays.asList("S9","S10","S6"), "T2", "D1", "B3"));
        tapLuat.add(new Rule(Arrays.asList("S9","S10","S6"), "T3", "D2", "B3"));
        tapLuat.add(new Rule(Arrays.asList("S9","S10","S6"), "T4", "D3", "B3"));
        tapLuat.add(new Rule(Arrays.asList("S9","S10","S6"), "T1", "D4", "B3"));
        tapLuat.add(new Rule(Arrays.asList("S9","S10","S6"), "T2", "D4", "B3"));
        tapLuat.add(new Rule(Arrays.asList("S9","S10","S6"), "T3", "D1", "B3"));
        tapLuat.add(new Rule(Arrays.asList("S9","S10","S6"), "T4", "D2", "B3"));
        tapLuat.add(new Rule(Arrays.asList("S9","S10","S6"), "T1", "D3", "B3"));
        tapLuat.add(new Rule(Arrays.asList("S9","S10","S6"), "T2", "D3", "B3"));

        // ----- B4: RỐI LOẠN TIÊU HÓA (10 luật) -----
        // Triệu chứng chính: S9(Đau bụng), S11(Tiêu chảy), S10(Buồn nôn)
        tapLuat.add(new Rule(Arrays.asList("S9","S11","S10"), "T1", "D1", "B4"));
        tapLuat.add(new Rule(Arrays.asList("S9","S11","S10"), "T2", "D1", "B4"));
        tapLuat.add(new Rule(Arrays.asList("S9","S11","S10"), "T3", "D2", "B4"));
        tapLuat.add(new Rule(Arrays.asList("S9","S11","S10"), "T4", "D3", "B4"));
        tapLuat.add(new Rule(Arrays.asList("S9","S11","S10"), "T1", "D4", "B4"));
        tapLuat.add(new Rule(Arrays.asList("S9","S11","S10"), "T2", "D4", "B4"));
        tapLuat.add(new Rule(Arrays.asList("S9","S11","S10"), "T3", "D1", "B4"));
        tapLuat.add(new Rule(Arrays.asList("S9","S11","S10"), "T4", "D2", "B4"));
        tapLuat.add(new Rule(Arrays.asList("S9","S11","S10"), "T1", "D3", "B4"));
        tapLuat.add(new Rule(Arrays.asList("S9","S11","S10"), "T2", "D3", "B4"));

        // ----- B5: MẤT NGỦ (10 luật) -----
        // Triệu chứng chính: S7(Khó ngủ), S8(Căng thẳng), S6(Mệt mỏi)
        tapLuat.add(new Rule(Arrays.asList("S7","S8","S6"), "T1", "D1", "B5"));
        tapLuat.add(new Rule(Arrays.asList("S7","S8","S6"), "T2", "D1", "B5"));
        tapLuat.add(new Rule(Arrays.asList("S7","S8","S6"), "T3", "D2", "B5"));
        tapLuat.add(new Rule(Arrays.asList("S7","S8","S6"), "T4", "D3", "B5"));
        tapLuat.add(new Rule(Arrays.asList("S7","S8","S6"), "T1", "D4", "B5"));
        tapLuat.add(new Rule(Arrays.asList("S7","S8","S6"), "T2", "D4", "B5"));
        tapLuat.add(new Rule(Arrays.asList("S7","S8","S6"), "T3", "D1", "B5"));
        tapLuat.add(new Rule(Arrays.asList("S7","S8","S6"), "T4", "D2", "B5"));
        tapLuat.add(new Rule(Arrays.asList("S7","S8","S6"), "T1", "D3", "B5"));
        tapLuat.add(new Rule(Arrays.asList("S7","S8","S6"), "T2", "D3", "B5"));

        // ----- B6: STRESS TÂM LÝ (10 luật) -----
        // Triệu chứng chính: S8(Căng thẳng), S6(Mệt mỏi), S5(Đau đầu)
        tapLuat.add(new Rule(Arrays.asList("S8","S6","S5"), "T1", "D1", "B6"));
        tapLuat.add(new Rule(Arrays.asList("S8","S6","S5"), "T2", "D1", "B6"));
        tapLuat.add(new Rule(Arrays.asList("S8","S6","S5"), "T3", "D2", "B6"));
        tapLuat.add(new Rule(Arrays.asList("S8","S6","S5"), "T4", "D3", "B6"));
        tapLuat.add(new Rule(Arrays.asList("S8","S6","S5"), "T1", "D4", "B6"));
        tapLuat.add(new Rule(Arrays.asList("S8","S6","S5"), "T2", "D4", "B6"));
        tapLuat.add(new Rule(Arrays.asList("S8","S6","S5"), "T3", "D1", "B6"));
        tapLuat.add(new Rule(Arrays.asList("S8","S6","S5"), "T4", "D2", "B6"));
        tapLuat.add(new Rule(Arrays.asList("S8","S6","S5"), "T1", "D3", "B6"));
        tapLuat.add(new Rule(Arrays.asList("S8","S6","S5"), "T2", "D3", "B6"));

        // ----- B7: DỊ ỨNG NHẸ (10 luật) -----
        // Triệu chứng chính: S12(Nổi mẩn), S13(Ngứa)
        tapLuat.add(new Rule(Arrays.asList("S12","S13"), "T1", "D1", "B7"));
        tapLuat.add(new Rule(Arrays.asList("S12","S13"), "T2", "D1", "B7"));
        tapLuat.add(new Rule(Arrays.asList("S12","S13"), "T3", "D2", "B7"));
        tapLuat.add(new Rule(Arrays.asList("S12","S13"), "T4", "D3", "B7"));
        tapLuat.add(new Rule(Arrays.asList("S12","S13"), "T1", "D4", "B7"));
        tapLuat.add(new Rule(Arrays.asList("S12","S13"), "T2", "D4", "B7"));
        tapLuat.add(new Rule(Arrays.asList("S12","S13"), "T3", "D1", "B7"));
        tapLuat.add(new Rule(Arrays.asList("S12","S13"), "T4", "D2", "B7"));
        tapLuat.add(new Rule(Arrays.asList("S12","S13"), "T1", "D3", "B7"));
        tapLuat.add(new Rule(Arrays.asList("S12","S13"), "T2", "D3", "B7"));

        // ----- B8: CẢM LẠNH (10 luật) -----
        // Triệu chứng chính: S2(Ho), S4(Sổ mũi), S14(Hắt hơi)
        tapLuat.add(new Rule(Arrays.asList("S2","S4","S14"), "T1", "D1", "B8"));
        tapLuat.add(new Rule(Arrays.asList("S2","S4","S14"), "T2", "D1", "B8"));
        tapLuat.add(new Rule(Arrays.asList("S2","S4","S14"), "T3", "D2", "B8"));
        tapLuat.add(new Rule(Arrays.asList("S2","S4","S14"), "T4", "D3", "B8"));
        tapLuat.add(new Rule(Arrays.asList("S2","S4","S14"), "T1", "D4", "B8"));
        tapLuat.add(new Rule(Arrays.asList("S2","S4","S14"), "T2", "D4", "B8"));
        tapLuat.add(new Rule(Arrays.asList("S2","S4","S14"), "T3", "D1", "B8"));
        tapLuat.add(new Rule(Arrays.asList("S2","S4","S14"), "T4", "D2", "B8"));
        tapLuat.add(new Rule(Arrays.asList("S2","S4","S14"), "T1", "D3", "B8"));
        tapLuat.add(new Rule(Arrays.asList("S2","S4","S14"), "T2", "D3", "B8"));

        // ----- B9: VIÊM MŨI DỊ ỨNG (10 luật) -----
        // Triệu chứng chính: S4(Sổ mũi), S14(Hắt hơi), S13(Ngứa)
        tapLuat.add(new Rule(Arrays.asList("S4","S14","S13"), "T1", "D1", "B9"));
        tapLuat.add(new Rule(Arrays.asList("S4","S14","S13"), "T2", "D1", "B9"));
        tapLuat.add(new Rule(Arrays.asList("S4","S14","S13"), "T3", "D2", "B9"));
        tapLuat.add(new Rule(Arrays.asList("S4","S14","S13"), "T4", "D3", "B9"));
        tapLuat.add(new Rule(Arrays.asList("S4","S14","S13"), "T1", "D4", "B9"));
        tapLuat.add(new Rule(Arrays.asList("S4","S14","S13"), "T2", "D4", "B9"));
        tapLuat.add(new Rule(Arrays.asList("S4","S14","S13"), "T3", "D1", "B9"));
        tapLuat.add(new Rule(Arrays.asList("S4","S14","S13"), "T4", "D2", "B9"));
        tapLuat.add(new Rule(Arrays.asList("S4","S14","S13"), "T1", "D3", "B9"));
        tapLuat.add(new Rule(Arrays.asList("S4","S14","S13"), "T2", "D3", "B9"));

        // ----- B10: ĐAU NỬA ĐẦU (10 luật) -----
        // Triệu chứng chính: S5(Đau đầu), S10(Buồn nôn), S16(Chóng mặt)
        tapLuat.add(new Rule(Arrays.asList("S5","S10","S16"), "T1", "D1", "B10"));
        tapLuat.add(new Rule(Arrays.asList("S5","S10","S16"), "T2", "D1", "B10"));
        tapLuat.add(new Rule(Arrays.asList("S5","S10","S16"), "T3", "D2", "B10"));
        tapLuat.add(new Rule(Arrays.asList("S5","S10","S16"), "T4", "D3", "B10"));
        tapLuat.add(new Rule(Arrays.asList("S5","S10","S16"), "T1", "D4", "B10"));
        tapLuat.add(new Rule(Arrays.asList("S5","S10","S16"), "T2", "D4", "B10"));
        tapLuat.add(new Rule(Arrays.asList("S5","S10","S16"), "T3", "D1", "B10"));
        tapLuat.add(new Rule(Arrays.asList("S5","S10","S16"), "T4", "D2", "B10"));
        tapLuat.add(new Rule(Arrays.asList("S5","S10","S16"), "T1", "D3", "B10"));
        tapLuat.add(new Rule(Arrays.asList("S5","S10","S16"), "T2", "D3", "B10"));

        // ----- B11: NGỘ ĐỘC THỰC PHẨM (10 luật) -----
        // Triệu chứng chính: S9(Đau bụng), S11(Tiêu chảy), S10(Buồn nôn)
        // Lưu ý: cùng triệu chứng B4 nhưng phân biệt bởi độ tuổi/thời gian khác
        tapLuat.add(new Rule(Arrays.asList("S9","S11","S10"), "T1", "D1", "B11"));
        tapLuat.add(new Rule(Arrays.asList("S9","S11","S10"), "T2", "D1", "B11"));
        tapLuat.add(new Rule(Arrays.asList("S9","S11","S10"), "T3", "D2", "B11"));
        tapLuat.add(new Rule(Arrays.asList("S9","S11","S10"), "T4", "D3", "B11"));
        tapLuat.add(new Rule(Arrays.asList("S9","S11","S10"), "T1", "D4", "B11"));
        tapLuat.add(new Rule(Arrays.asList("S9","S11","S10"), "T2", "D4", "B11"));
        tapLuat.add(new Rule(Arrays.asList("S9","S11","S10"), "T3", "D1", "B11"));
        tapLuat.add(new Rule(Arrays.asList("S9","S11","S10"), "T4", "D2", "B11"));
        tapLuat.add(new Rule(Arrays.asList("S9","S11","S10"), "T1", "D3", "B11"));
        tapLuat.add(new Rule(Arrays.asList("S9","S11","S10"), "T2", "D3", "B11"));

        // ----- B12: THIẾU NGỦ (10 luật) -----
        // Triệu chứng chính: S6(Mệt mỏi), S7(Khó ngủ)
        tapLuat.add(new Rule(Arrays.asList("S6","S7"), "T1", "D1", "B12"));
        tapLuat.add(new Rule(Arrays.asList("S6","S7"), "T2", "D1", "B12"));
        tapLuat.add(new Rule(Arrays.asList("S6","S7"), "T3", "D2", "B12"));
        tapLuat.add(new Rule(Arrays.asList("S6","S7"), "T4", "D3", "B12"));
        tapLuat.add(new Rule(Arrays.asList("S6","S7"), "T1", "D4", "B12"));
        tapLuat.add(new Rule(Arrays.asList("S6","S7"), "T2", "D4", "B12"));
        tapLuat.add(new Rule(Arrays.asList("S6","S7"), "T3", "D1", "B12"));
        tapLuat.add(new Rule(Arrays.asList("S6","S7"), "T4", "D2", "B12"));
        tapLuat.add(new Rule(Arrays.asList("S6","S7"), "T1", "D3", "B12"));
        tapLuat.add(new Rule(Arrays.asList("S6","S7"), "T2", "D3", "B12"));

        // ----- B13: TRẦM CẢM NHẸ (10 luật) -----
        // Triệu chứng chính: S8(Căng thẳng), S7(Khó ngủ), S17(Khó tập trung)
        tapLuat.add(new Rule(Arrays.asList("S8","S7","S17"), "T1", "D1", "B13"));
        tapLuat.add(new Rule(Arrays.asList("S8","S7","S17"), "T2", "D1", "B13"));
        tapLuat.add(new Rule(Arrays.asList("S8","S7","S17"), "T3", "D2", "B13"));
        tapLuat.add(new Rule(Arrays.asList("S8","S7","S17"), "T4", "D3", "B13"));
        tapLuat.add(new Rule(Arrays.asList("S8","S7","S17"), "T1", "D4", "B13"));
        tapLuat.add(new Rule(Arrays.asList("S8","S7","S17"), "T2", "D4", "B13"));
        tapLuat.add(new Rule(Arrays.asList("S8","S7","S17"), "T3", "D1", "B13"));
        tapLuat.add(new Rule(Arrays.asList("S8","S7","S17"), "T4", "D2", "B13"));
        tapLuat.add(new Rule(Arrays.asList("S8","S7","S17"), "T1", "D3", "B13"));
        tapLuat.add(new Rule(Arrays.asList("S8","S7","S17"), "T2", "D3", "B13"));

        // ----- B14: VIÊM DA (10 luật) -----
        // Triệu chứng chính: S12(Nổi mẩn), S13(Ngứa)
        // Lưu ý: cùng triệu chứng với B7, phân biệt bởi mức độ nghiêm trọng hơn
        tapLuat.add(new Rule(Arrays.asList("S12","S13"), "T1", "D1", "B14"));
        tapLuat.add(new Rule(Arrays.asList("S12","S13"), "T2", "D1", "B14"));
        tapLuat.add(new Rule(Arrays.asList("S12","S13"), "T3", "D2", "B14"));
        tapLuat.add(new Rule(Arrays.asList("S12","S13"), "T4", "D3", "B14"));
        tapLuat.add(new Rule(Arrays.asList("S12","S13"), "T1", "D4", "B14"));
        tapLuat.add(new Rule(Arrays.asList("S12","S13"), "T2", "D4", "B14"));
        tapLuat.add(new Rule(Arrays.asList("S12","S13"), "T3", "D1", "B14"));
        tapLuat.add(new Rule(Arrays.asList("S12","S13"), "T4", "D2", "B14"));
        tapLuat.add(new Rule(Arrays.asList("S12","S13"), "T1", "D3", "B14"));
        tapLuat.add(new Rule(Arrays.asList("S12","S13"), "T2", "D3", "B14"));

        // ----- B15: TÁO BÓN (10 luật) -----
        // Triệu chứng chính: S9(Đau bụng), S6(Mệt mỏi)
        tapLuat.add(new Rule(Arrays.asList("S9","S6"), "T1", "D1", "B15"));
        tapLuat.add(new Rule(Arrays.asList("S9","S6"), "T2", "D1", "B15"));
        tapLuat.add(new Rule(Arrays.asList("S9","S6"), "T3", "D2", "B15"));
        tapLuat.add(new Rule(Arrays.asList("S9","S6"), "T4", "D3", "B15"));
        tapLuat.add(new Rule(Arrays.asList("S9","S6"), "T1", "D4", "B15"));
        tapLuat.add(new Rule(Arrays.asList("S9","S6"), "T2", "D4", "B15"));
        tapLuat.add(new Rule(Arrays.asList("S9","S6"), "T3", "D1", "B15"));
        tapLuat.add(new Rule(Arrays.asList("S9","S6"), "T4", "D2", "B15"));
        tapLuat.add(new Rule(Arrays.asList("S9","S6"), "T1", "D3", "B15"));
        tapLuat.add(new Rule(Arrays.asList("S9","S6"), "T2", "D3", "B15"));

        // ----- B16: SUY NHƯỢC CƠ THỂ (10 luật) -----
        // Triệu chứng chính: S6(Mệt mỏi), S16(Chóng mặt)
        tapLuat.add(new Rule(Arrays.asList("S6","S16"), "T1", "D1", "B16"));
        tapLuat.add(new Rule(Arrays.asList("S6","S16"), "T2", "D1", "B16"));
        tapLuat.add(new Rule(Arrays.asList("S6","S16"), "T3", "D2", "B16"));
        tapLuat.add(new Rule(Arrays.asList("S6","S16"), "T4", "D3", "B16"));
        tapLuat.add(new Rule(Arrays.asList("S6","S16"), "T1", "D4", "B16"));
        tapLuat.add(new Rule(Arrays.asList("S6","S16"), "T2", "D4", "B16"));
        tapLuat.add(new Rule(Arrays.asList("S6","S16"), "T3", "D1", "B16"));
        tapLuat.add(new Rule(Arrays.asList("S6","S16"), "T4", "D2", "B16"));
        tapLuat.add(new Rule(Arrays.asList("S6","S16"), "T1", "D3", "B16"));
        tapLuat.add(new Rule(Arrays.asList("S6","S16"), "T2", "D3", "B16"));

        // ----- B17: VIÊM XOANG (10 luật) -----
        // Triệu chứng chính: S4(Sổ mũi), S15(Nghẹt mũi), S20(Đau vùng xoang)
        tapLuat.add(new Rule(Arrays.asList("S4","S15","S20"), "T1", "D1", "B17"));
        tapLuat.add(new Rule(Arrays.asList("S4","S15","S20"), "T2", "D1", "B17"));
        tapLuat.add(new Rule(Arrays.asList("S4","S15","S20"), "T3", "D2", "B17"));
        tapLuat.add(new Rule(Arrays.asList("S4","S15","S20"), "T4", "D3", "B17"));
        tapLuat.add(new Rule(Arrays.asList("S4","S15","S20"), "T1", "D4", "B17"));
        tapLuat.add(new Rule(Arrays.asList("S4","S15","S20"), "T2", "D4", "B17"));
        tapLuat.add(new Rule(Arrays.asList("S4","S15","S20"), "T3", "D1", "B17"));
        tapLuat.add(new Rule(Arrays.asList("S4","S15","S20"), "T4", "D2", "B17"));
        tapLuat.add(new Rule(Arrays.asList("S4","S15","S20"), "T1", "D3", "B17"));
        tapLuat.add(new Rule(Arrays.asList("S4","S15","S20"), "T2", "D3", "B17"));
    }

    // =====================================================================
    // PHƯƠNG THỨC CHÍNH: chanDoan()
    // Input : trieuChungChon (tên hiển thị), tenDoTuoi (chuỗi hiển thị), tenThoiGian (chuỗi hiển thị)
    // Output: List<Object[]>{ {Benh, Double phanTram}, ... } — Top 3 bệnh có xác suất cao nhất
    //         Hoặc null nếu không tìm thấy
    // =====================================================================
    public List<Object[]> chanDoan(List<String> trieuChungChon,
                                   String tenDoTuoi,
                                   String tenThoiGian) {

        // 1. Validate đầu vào
        if (trieuChungChon == null || trieuChungChon.isEmpty()) return null;

        // 2. Chuyển tên hiển thị → mã Sx cho triệu chứng
        Set<String> maTrieuChungInput = new HashSet<>();
        for (String ten : trieuChungChon) {
            String ma = MA_TRIEU_CHUNG.get(ten);
            if (ma != null) maTrieuChungInput.add(ma);
        }

        // 3. Chuyển tên hiển thị → mã Tx, Dx
        String maDoTuoi   = MA_DO_TUOI.getOrDefault(tenDoTuoi, "");
        String maThoiGian = MA_THOI_GIAN.getOrDefault(tenThoiGian, "");

        // 4. Duyệt toàn bộ tập luật, tính điểm từng rule
        //    Dùng Map: maBenh → điểm cao nhất của bệnh đó
        Map<String, Double> diemTheoBenh = new LinkedHashMap<>();

        for (Rule rule : tapLuat) {
            // --- Tính điểm triệu chứng ---
            int tcKhop = 0;
            for (String maTC : rule.trieuChung) {
                if (maTrieuChungInput.contains(maTC)) tcKhop++;
            }
            if (tcKhop == 0) continue; // Không có triệu chứng nào khớp → bỏ qua

            double diemTC = tcKhop * W_TC;

            // --- Tính điểm độ tuổi ---
            double diemT = rule.doTuoi.equals(maDoTuoi) ? W_T : 0.0;

            // --- Tính điểm thời gian ---
            double diemD = rule.thoiGian.equals(maThoiGian) ? W_D : 0.0;

            // --- Tổng điểm tối đa của rule này ---
            double tongMax = rule.trieuChung.size() * W_TC + W_T + W_D;

            // --- % phù hợp của rule này ---
            double phanTram = (diemTC + diemT + diemD) / tongMax * 100.0;

            // --- Lưu điểm cao nhất theo từng bệnh ---
            diemTheoBenh.merge(rule.maBenh, phanTram, Math::max);
        }

        if (diemTheoBenh.isEmpty()) return null;

        // 5. Sắp xếp bệnh theo điểm giảm dần và lấy top 3
        List<Map.Entry<String, Double>> topBenhList = diemTheoBenh.entrySet().stream()
            .filter(entry -> entry.getValue() >= 20.0)  // Ngưỡng tối thiểu giảm xuống 20%
            .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))  // Sắp xếp giảm dần
            .limit(3)  // Lấy top 3
            .toList();

        if (topBenhList.isEmpty()) return null;

        // 6. Xây dựng danh sách kết quả với đối tượng Benh
        List<Object[]> ketQua = new ArrayList<>();
        
        for (Map.Entry<String, Double> entry : topBenhList) {
            String maBenhCanTim = entry.getKey();
            double phanTramCanTim = entry.getValue();
            
            // Tìm đối tượng Benh theo mã
            for (Benh b : danhSachBenh) {
                if (b.getMaBenh().equals(maBenhCanTim)) {
                    double phanTramLamTron = Math.round(phanTramCanTim * 10.0) / 10.0;
                    ketQua.add(new Object[]{ b, phanTramLamTron });
                    break;
                }
            }
        }

        return ketQua.isEmpty() ? null : ketQua;
    }

    // =====================================================================
    // GETTER — trả về map triệu chứng để ChanDoanForm có thể build UI động
    // =====================================================================
    public static Map<String, String> getMaTrieuChung() {
        return Collections.unmodifiableMap(MA_TRIEU_CHUNG);
    }

    public static Map<String, String> getMaDoTuoi() {
        return Collections.unmodifiableMap(MA_DO_TUOI);
    }

    public static Map<String, String> getMaThoiGian() {
        return Collections.unmodifiableMap(MA_THOI_GIAN);
    }
}