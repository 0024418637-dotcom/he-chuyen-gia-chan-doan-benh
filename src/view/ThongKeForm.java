package view;

import model.LichSuChanDoan;
import service.LichSuService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

public class ThongKeForm extends JPanel {

    // Các label thống kê tổng quan
    private JLabel lblTongLuot;
    private JLabel lblBenhPhoNhat;
    private JLabel lblTrieuChungPhoNhat;

    // Panel chứa biểu đồ cột (vẽ tay bằng Swing)
    private BieuDoPanel bieuDoPanel;

    // Bảng lịch sử gần đây
    private DefaultTableModel tableModel;
    private JTable table;

    public ThongKeForm() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // ===== PANEL TRÊN: 3 thẻ thống kê tổng quan =====
        JPanel panelTop = new JPanel(new GridLayout(1, 3, 10, 0));
        panelTop.setBackground(Color.WHITE);
        panelTop.setPreferredSize(new Dimension(0, 90));

        lblTongLuot          = new JLabel("0", SwingConstants.CENTER);
        lblBenhPhoNhat       = new JLabel("--", SwingConstants.CENTER);
        lblTrieuChungPhoNhat = new JLabel("--", SwingConstants.CENTER);

        panelTop.add(taoTheThongKe("Tổng lượt chẩn đoán", lblTongLuot, new Color(0,123,167)));
        panelTop.add(taoTheThongKe("Bệnh phổ biến nhất",  lblBenhPhoNhat, new Color(0,160,90)));
        panelTop.add(taoTheThongKe("Triệu chứng hay gặp", lblTrieuChungPhoNhat, new Color(180,80,0)));

        add(panelTop, BorderLayout.NORTH);

        // ===== PANEL GIỮA: Biểu đồ cột =====
        JPanel panelGiua = new JPanel(new BorderLayout(0, 6));
        panelGiua.setBackground(Color.WHITE);

        JLabel lblTieuDeBD = new JLabel("  📊 Thống kê số lần chẩn đoán theo bệnh:");
        lblTieuDeBD.setFont(new Font("Arial", Font.BOLD, 13));
        lblTieuDeBD.setForeground(new Color(0,100,150));
        panelGiua.add(lblTieuDeBD, BorderLayout.NORTH);

        bieuDoPanel = new BieuDoPanel();
        bieuDoPanel.setPreferredSize(new Dimension(0, 180));
        panelGiua.add(bieuDoPanel, BorderLayout.CENTER);

        add(panelGiua, BorderLayout.CENTER);

        // ===== PANEL DƯỚI: Bảng lịch sử gần đây =====
        JPanel panelDuoi = new JPanel(new BorderLayout(0, 4));
        panelDuoi.setBackground(Color.WHITE);

        JLabel lblTieuDeBang = new JLabel("  📋 5 lượt chẩn đoán gần nhất:");
        lblTieuDeBang.setFont(new Font("Arial", Font.BOLD, 13));
        lblTieuDeBang.setForeground(new Color(0,100,150));
        panelDuoi.add(lblTieuDeBang, BorderLayout.NORTH);

        String[] cot = {"Thời gian", "Triệu chứng", "Bệnh chẩn đoán", "% Phù hợp"};
        tableModel = new DefaultTableModel(cot, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(25);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.getTableHeader().setBackground(new Color(0, 123, 167));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.setGridColor(new Color(220,230,240));
        panelDuoi.add(new JScrollPane(table), BorderLayout.CENTER);

        add(panelDuoi, BorderLayout.SOUTH);
    }

    // Tạo thẻ thống kê nhỏ (card)
    private JPanel taoTheThongKe(String tieuDe, JLabel lblGiaTri, Color mau) {
        JPanel card = new JPanel(new BorderLayout(4, 4));
        card.setBackground(new Color(240, 248, 255));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(mau, 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        JLabel lblTieuDe = new JLabel(tieuDe, SwingConstants.CENTER);
        lblTieuDe.setFont(new Font("Arial", Font.PLAIN, 11));
        lblTieuDe.setForeground(new Color(80, 100, 120));
        card.add(lblTieuDe, BorderLayout.NORTH);

        lblGiaTri.setFont(new Font("Arial", Font.BOLD, 22));
        lblGiaTri.setForeground(mau);
        card.add(lblGiaTri, BorderLayout.CENTER);

        return card;
    }

    /**
     * Gọi phương thức này mỗi khi chuyển sang tab Thống kê
     * để dữ liệu luôn được cập nhật mới nhất.
     */
    public void capNhatThongKe() {
        List<LichSuChanDoan> ds = LichSuService.getInstance().getDanhSach();

        // 1. Tổng lượt
        lblTongLuot.setText(String.valueOf(ds.size()));

        if (ds.isEmpty()) {
            lblBenhPhoNhat.setText("--");
            lblTrieuChungPhoNhat.setText("--");
            tableModel.setRowCount(0);
            bieuDoPanel.capNhat(new LinkedHashMap<>());
            return;
        }

        // 2. Đếm bệnh
        Map<String, Integer> demBenh = new HashMap<>();
        for (LichSuChanDoan ls : ds) {
            demBenh.merge(ls.getTenBenh(), 1, Integer::sum);
        }
        String benhPhoNhat = Collections.max(demBenh.entrySet(),
            Map.Entry.comparingByValue()).getKey();
        lblBenhPhoNhat.setText(
            "<html><center>" + benhPhoNhat +
            "<br><font size='2'>(" + demBenh.get(benhPhoNhat) + " lần)</font></center></html>");

        // 3. Đếm triệu chứng
        Map<String, Integer> demTC = new HashMap<>();
        for (LichSuChanDoan ls : ds) {
            String[] dsTC = ls.getTrieuChungDaChon().split(",");
            for (String tc : dsTC) {
                String tenTC = tc.trim();
                if (!tenTC.isEmpty()) demTC.merge(tenTC, 1, Integer::sum);
            }
        }
        if (!demTC.isEmpty()) {
            String tcPhoNhat = Collections.max(demTC.entrySet(),
                Map.Entry.comparingByValue()).getKey();
            lblTrieuChungPhoNhat.setText(
                "<html><center>" + tcPhoNhat +
                "<br><font size='2'>(" + demTC.get(tcPhoNhat) + " lần)</font></center></html>");
        }

        // 4. Cập nhật biểu đồ cột
        // Sắp xếp bệnh giảm dần theo số lần
        Map<String, Integer> benhSapXep = new LinkedHashMap<>();
        demBenh.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .forEach(e -> benhSapXep.put(e.getKey(), e.getValue()));
        bieuDoPanel.capNhat(benhSapXep);

        // 5. Bảng 5 lượt gần nhất (lấy từ cuối danh sách)
        tableModel.setRowCount(0);
        int start = Math.max(0, ds.size() - 5);
        for (int i = ds.size() - 1; i >= start; i--) {
            LichSuChanDoan ls = ds.get(i);
            tableModel.addRow(new Object[]{
                ls.getThoiGian(),
                ls.getTrieuChungDaChon(),
                ls.getTenBenh(),
                ls.getPhanTramPhuHop() + "%"
            });
        }
    }

    // =====================================================
    // Inner class: Biểu đồ cột vẽ bằng Graphics (không cần thư viện ngoài)
    // =====================================================
    static class BieuDoPanel extends JPanel {

        private Map<String, Integer> data = new LinkedHashMap<>();

        // Màu cho từng cột
        private Color[] mauCot = {
            new Color(0, 123, 167),
            new Color(0, 160, 90),
            new Color(180, 80, 0),
            new Color(120, 60, 180),
            new Color(200, 50, 50),
            new Color(0, 150, 150)
        };

        public BieuDoPanel() {
            setBackground(new Color(248, 252, 255));
            setBorder(BorderFactory.createLineBorder(new Color(200, 220, 235)));
        }

        public void capNhat(Map<String, Integer> data) {
            this.data = data;
            repaint(); // Vẽ lại biểu đồ
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (data == null || data.isEmpty()) {
                g.setColor(Color.GRAY);
                g.setFont(new Font("Arial", Font.PLAIN, 13));
                g.drawString("Chưa có dữ liệu thống kê.", 20, getHeight() / 2);
                return;
            }

            Graphics2D g2 = (Graphics2D) g;
            // Làm mịn chữ
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int paddingLeft  = 50;  // Để ghi nhãn trục Y
            int paddingBottom = 40; // Để ghi nhãn trục X
            int paddingTop   = 20;
            int w = getWidth()  - paddingLeft - 20;
            int h = getHeight() - paddingBottom - paddingTop;

            // Giá trị lớn nhất
            int maxVal = Collections.max(data.values());
            if (maxVal == 0) return;

            int n = data.size();
            int cotWidth  = Math.min(80, w / n - 10);
            int khoangCach = w / n;

            int idx = 0;
            for (Map.Entry<String, Integer> entry : data.entrySet()) {
                String tenBenh = entry.getKey();
                int soLan = entry.getValue();

                // Tính chiều cao cột theo tỉ lệ
                int chieuCaoCot = (int) ((double) soLan / maxVal * h);
                int x = paddingLeft + idx * khoangCach + (khoangCach - cotWidth) / 2;
                int y = paddingTop + h - chieuCaoCot;

                // Vẽ cột
                Color mau = mauCot[idx % mauCot.length];
                g2.setColor(mau);
                g2.fillRoundRect(x, y, cotWidth, chieuCaoCot, 6, 6);

                // Viền cột
                g2.setColor(mau.darker());
                g2.drawRoundRect(x, y, cotWidth, chieuCaoCot, 6, 6);

                // Số liệu trên đỉnh cột
                g2.setColor(new Color(50, 50, 80));
                g2.setFont(new Font("Arial", Font.BOLD, 12));
                String soLanStr = String.valueOf(soLan);
                int swNum = g2.getFontMetrics().stringWidth(soLanStr);
                g2.drawString(soLanStr, x + (cotWidth - swNum) / 2, y - 4);

                // Tên bệnh dưới trục X (xoay nếu dài)
                g2.setColor(new Color(60, 80, 100));
                g2.setFont(new Font("Arial", Font.PLAIN, 11));
                // Rút gọn tên nếu quá dài
                String nhan = tenBenh.length() > 10 ? tenBenh.substring(0, 8) + ".." : tenBenh;
                int swLbl = g2.getFontMetrics().stringWidth(nhan);
                g2.drawString(nhan, x + (cotWidth - swLbl) / 2,
                    paddingTop + h + 18);

                idx++;
            }

            // Vẽ đường trục Y
            g2.setColor(new Color(180, 200, 220));
            g2.drawLine(paddingLeft - 2, paddingTop, paddingLeft - 2, paddingTop + h);
            g2.drawLine(paddingLeft - 2, paddingTop + h, paddingLeft + w, paddingTop + h);

            // Nhãn trục Y (0 và max)
            g2.setColor(Color.GRAY);
            g2.setFont(new Font("Arial", Font.PLAIN, 10));
            g2.drawString("0", paddingLeft - 18, paddingTop + h + 4);
            g2.drawString(String.valueOf(maxVal), paddingLeft - 25, paddingTop + 8);
        }
    }
}