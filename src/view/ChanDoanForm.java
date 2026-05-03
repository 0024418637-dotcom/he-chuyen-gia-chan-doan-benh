package view;

import model.Benh;
import service.ChanDoanService;
import service.LichSuService;
import utils.HinhAnhUtils;
import utils.XuatFileUtils;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * ChanDoanForm — Form chẩn đoán bệnh.
 *
 * Thay đổi so với phiên bản cũ:
 *   - Gọi chanDoan(trieuChung, doTuoi, thoiGian) thay vì chanDoan(trieuChung)
 *   - ComboBox độ tuổi và thời gian khớp đúng với tập luật (T1-T4, D1-D4)
 *   - Triệu chứng đủ 20 loại từ tập sự kiện (S1-S20)
 */
public class ChanDoanForm extends JPanel {

    private JCheckBox[] arrCheckBox;
    private JComboBox<String> cboDoTuoi;
    private JComboBox<String> cboThoiGian;
    private JButton btnChanDoan, btnLamMoi, btnXuatFile;
    private JTextArea txtKetQua;
    private JLabel lblHinhAnh;
    private JLabel lblTenBenh;

    private ChanDoanService chanDoanService = new ChanDoanService();
    private Benh benhHienTai = null;

    // =====================================================================
    // 20 triệu chứng đầy đủ từ tập sự kiện S1-S20
    // =====================================================================
    private String[] dsTrieuChung = {
        "Sốt",              // S1
        "Ho",               // S2
        "Đau họng",         // S3
        "Sổ mũi",           // S4
        "Đau đầu",          // S5
        "Mệt mỏi",          // S6
        "Khó ngủ",          // S7
        "Căng thẳng/Lo âu", // S8
        "Đau bụng",         // S9
        "Buồn nôn",         // S10
        "Tiêu chảy",        // S11
        "Nổi mẩn",          // S12
        "Ngứa",             // S13
        "Hắt hơi",          // S14
        "Nghẹt mũi",        // S15
        "Chóng mặt",        // S16
        "Khó tập trung",    // S17
        "Chán ăn",          // S18
        "Đau cơ",           // S19
        "Đau vùng xoang"    // S20
    };

    // Độ tuổi khớp với mã T1-T4 trong tập luật
    private String[] dsDoTuoi = {
        "Dưới 5 tuổi",         // T1
        "5 - 12 tuổi",         // T2
        "18 - 60 tuổi",        // T3
        "Trên 60 tuổi"         // T4
    };

    // Thời gian khớp với mã D1-D4 trong tập luật
    private String[] dsThoiGian = {
        "1 - 2 ngày",          // D1
        "3 - 5 ngày",          // D2
        "5 - 7 ngày",          // D3
        "Trên 7 ngày"          // D4
    };

    public ChanDoanForm() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // ===== PANEL TRÁI: Nhập triệu chứng =====
        JPanel panelTrai = new JPanel(new BorderLayout(0, 8));
        panelTrai.setBackground(Color.WHITE);
        panelTrai.setPreferredSize(new Dimension(360, 0));

        JLabel lblTieuDe = new JLabel("  Chọn triệu chứng của bạn:");
        lblTieuDe.setFont(new Font("Arial", Font.BOLD, 14));
        lblTieuDe.setForeground(new Color(0, 100, 150));
        panelTrai.add(lblTieuDe, BorderLayout.NORTH);

        // Panel checkbox — 20 triệu chứng, 2 cột
        JPanel panelTC = new JPanel(new GridLayout(0, 2, 6, 4));
        panelTC.setBackground(new Color(245, 250, 255));
        panelTC.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(180, 210, 230)),
            "Triệu chứng (S1-S20)",
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            new Font("Arial", Font.PLAIN, 11),
            new Color(0, 100, 150)
        ));

        arrCheckBox = new JCheckBox[dsTrieuChung.length];
        for (int i = 0; i < dsTrieuChung.length; i++) {
            arrCheckBox[i] = new JCheckBox(dsTrieuChung[i]);
            arrCheckBox[i].setBackground(new Color(245, 250, 255));
            arrCheckBox[i].setFont(new Font("Arial", Font.PLAIN, 12));
            panelTC.add(arrCheckBox[i]);
        }
        panelTrai.add(new JScrollPane(panelTC), BorderLayout.CENTER);

        // ComboBox độ tuổi và thời gian (khớp tập luật)
        JPanel panelCbo = new JPanel(new GridLayout(2, 2, 8, 8));
        panelCbo.setBackground(Color.WHITE);
        panelCbo.setBorder(BorderFactory.createEmptyBorder(8, 2, 4, 2));

        JLabel lblTuoi = new JLabel("Độ tuổi (T):");
        lblTuoi.setFont(new Font("Arial", Font.BOLD, 12));
        panelCbo.add(lblTuoi);

        cboDoTuoi = new JComboBox<>(dsDoTuoi);
        cboDoTuoi.setFont(new Font("Arial", Font.PLAIN, 12));
        panelCbo.add(cboDoTuoi);

        JLabel lblTG = new JLabel("Thời gian (D):");
        lblTG.setFont(new Font("Arial", Font.BOLD, 12));
        panelCbo.add(lblTG);

        cboThoiGian = new JComboBox<>(dsThoiGian);
        cboThoiGian.setFont(new Font("Arial", Font.PLAIN, 12));
        panelCbo.add(cboThoiGian);

        panelTrai.add(panelCbo, BorderLayout.SOUTH);

        // ===== PANEL PHẢI: Kết quả =====
        JPanel panelPhai = new JPanel(new BorderLayout(6, 8));
        panelPhai.setBackground(Color.WHITE);
        panelPhai.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(0, 123, 167)),
            "Kết quả chẩn đoán",
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 12),
            new Color(0, 100, 150)
        ));

        // Hình ảnh bệnh
        lblHinhAnh = new JLabel("", SwingConstants.CENTER);
        lblHinhAnh.setPreferredSize(new Dimension(210, 160));
        lblHinhAnh.setBorder(BorderFactory.createLineBorder(new Color(200, 220, 230)));
        lblHinhAnh.setText("<html><center><i>Chưa có kết quả</i></center></html>");
        lblHinhAnh.setFont(new Font("Arial", Font.ITALIC, 12));
        lblHinhAnh.setForeground(Color.GRAY);
        panelPhai.add(lblHinhAnh, BorderLayout.NORTH);

        // Tên bệnh + % phù hợp
        lblTenBenh = new JLabel("", SwingConstants.CENTER);
        lblTenBenh.setFont(new Font("Arial", Font.BOLD, 15));
        lblTenBenh.setForeground(new Color(180, 0, 0));
        panelPhai.add(lblTenBenh, BorderLayout.CENTER);

        // Nội dung kết quả
        txtKetQua = new JTextArea(9, 0);
        txtKetQua.setEditable(false);
        txtKetQua.setFont(new Font("Arial", Font.PLAIN, 13));
        txtKetQua.setLineWrap(true);
        txtKetQua.setWrapStyleWord(true);
        txtKetQua.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        txtKetQua.setBackground(new Color(250, 253, 255));
        panelPhai.add(new JScrollPane(txtKetQua), BorderLayout.SOUTH);

        // ===== PANEL NÚT =====
        JPanel panelNut = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 8));
        panelNut.setBackground(new Color(240, 248, 255));
        panelNut.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 220, 235)));

        btnChanDoan = taoNut("  Chan doan", new Color(0, 123, 167));
        btnLamMoi   = taoNut("  Lam moi",   new Color(80, 150, 80));
        btnXuatFile = taoNut("  Xuat file",  new Color(140, 90, 0));

        btnChanDoan.addActionListener(e -> xuLyChanDoan());
        btnLamMoi.addActionListener(e -> xuLyLamMoi());
        btnXuatFile.addActionListener(e -> xuLyXuatFile());

        panelNut.add(btnChanDoan);
        panelNut.add(btnLamMoi);
        panelNut.add(btnXuatFile);

        add(panelTrai,  BorderLayout.WEST);
        add(panelPhai,  BorderLayout.CENTER);
        add(panelNut,   BorderLayout.SOUTH);
    }

    private JButton taoNut(String text, Color mau) {
        JButton btn = new JButton(text);
        btn.setBackground(mau);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(140, 38));
        btn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        return btn;
    }

    // =====================================================================
    // XỬ LÝ NÚT CHẨN ĐOÁN — Gửi input vào Rule-Based Engine
    // =====================================================================
    private void xuLyChanDoan() {
        // 1. Lấy danh sách triệu chứng được tick
        List<String> dsChon = new ArrayList<>();
        for (JCheckBox cb : arrCheckBox) {
            if (cb.isSelected()) dsChon.add(cb.getText());
        }

        // 2. Validate: phải chọn ít nhất 1 triệu chứng
        if (dsChon.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng chọn ít nhất 1 triệu chứng!",
                "Chua chon trieu chung", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 3. Lấy độ tuổi và thời gian đã chọn
        String doTuoi   = (String) cboDoTuoi.getSelectedItem();
        String thoiGian = (String) cboThoiGian.getSelectedItem();

        // 4. Gọi Rule-Based Engine — truyền đủ 3 tham số
        Object[] ketQua = chanDoanService.chanDoan(dsChon, doTuoi, thoiGian);

        // 5. Xử lý kết quả
        if (ketQua == null) {
            // Không khớp bất kỳ luật nào hoặc % < 30%
            lblTenBenh.setText("Khong tim thay benh phu hop");
            lblTenBenh.setForeground(new Color(150, 80, 0));
            txtKetQua.setText(
                    "\n\n⚠️ Đây chỉ là tư vấn tham khảo, không thay thế bác sĩ.\n\n" +
                    "Chúng tôi không tìm thấy bất kỳ bệnh lý nào phù hợp\n" +
                    "với các triệu chứng bạn đã nhập.\n\n" +
                    "Có thể do:\n" +
                    "  - Số lượng triệu chứng quá ít\n" +
                    "  - Tổ hợp triệu chứng không khớp với tập luật\n\n" +
                    "Vui lòng gặp bác sĩ để được tư vấn chính xác hơn."
                );
            lblHinhAnh.setIcon(null);
            lblHinhAnh.setText("<html><center><i>Khong co ket qua</i></center></html>");
            benhHienTai = null;
            return;
        }

        // 6. Hiển thị kết quả thành công
        benhHienTai     = (Benh) ketQua[0];
        double phanTram = (double) ketQua[1];

        // Màu cảnh báo theo mức độ bệnh
        Color mauCanhBao = switch (benhHienTai.getMucCanhBao()) {
            case "Nang" -> new Color(180, 0, 0);
            case "Trung binh" -> new Color(170, 90, 0);
            default -> new Color(0, 120, 60);
        };
        lblTenBenh.setText(benhHienTai.getTenBenh() + "   (" + phanTram + "% phu hop)");
        lblTenBenh.setForeground(mauCanhBao);

        // Nội dung chi tiết
        String noiDung =
            "MUC CANH BAO : " + benhHienTai.getMucCanhBao() + "\n\n" +
            "MO TA BENH:\n" + benhHienTai.getMoTa() + "\n\n" +
            "LOI KHUYEN:\n" + benhHienTai.getHuongDan() + "\n\n" +
            "------------------------------------\n" +
            "Trieu chung da chon : " + String.join(", ", dsChon) + "\n" +
            "Do tuoi             : " + doTuoi + "\n" +
            "Thoi gian           : " + thoiGian;
        txtKetQua.setText(noiDung);
        txtKetQua.setCaretPosition(0);

        // 7. Hình ảnh bệnh (dùng HinhAnhUtils — fallback no_image.png)
        ImageIcon icon = HinhAnhUtils.layHinhAnh(benhHienTai.getHinhAnh(), 200, 155);
        if (icon != null) {
            lblHinhAnh.setIcon(icon);
            lblHinhAnh.setText("");
        } else {
            lblHinhAnh.setIcon(null);
            lblHinhAnh.setText("<html><center>" + benhHienTai.getTenBenh() + "</center></html>");
        }

        // 8. Lưu lịch sử qua LichSuService
        String thoiGianHienTai = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm")
            .format(new java.util.Date());
        LichSuService.getInstance().themLichSu(
            thoiGianHienTai,
            String.join(", ", dsChon),
            benhHienTai.getTenBenh(),
            phanTram,
            doTuoi,
            thoiGian
        );
    }

    // =====================================================================
    // XỬ LÝ NÚT LÀM MỚI
    // =====================================================================
    private void xuLyLamMoi() {
        for (JCheckBox cb : arrCheckBox) cb.setSelected(false);
        cboDoTuoi.setSelectedIndex(0);
        cboThoiGian.setSelectedIndex(0);
        txtKetQua.setText("");
        lblTenBenh.setText("");
        lblTenBenh.setForeground(new Color(180, 0, 0));
        lblHinhAnh.setIcon(null);
        lblHinhAnh.setText("<html><center><i>Chua co ket qua</i></center></html>");
        benhHienTai = null;
    }

    // =====================================================================
    // XỬ LÝ NÚT XUẤT FILE TXT
    // =====================================================================
    private void xuLyXuatFile() {
        if (benhHienTai == null) {
            JOptionPane.showMessageDialog(this,
                "Chua co ket qua chan doan de xuat!",
                "Chu y", JOptionPane.WARNING_MESSAGE);
            return;
        }
        XuatFileUtils.xuatTXT(benhHienTai, txtKetQua.getText(), this);
    }
}