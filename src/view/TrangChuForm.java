package view;

import javax.swing.*;
import java.awt.*;

public class TrangChuForm extends JFrame {

    private JPanel panelSidebar;
    private JPanel panelNoidung;
    private CardLayout cardLayout;  // Dùng CardLayout để chuyển form

    // Các form con
    private ChanDoanForm chanDoanForm;
    private LichSuForm   lichSuForm;
    private ThongKeForm  thongKeForm;
    private TimKiemForm  timKiemForm;

    public TrangChuForm() {
        initComponents();
        setTitle("Hệ Chuyên Gia Tư Vấn Sức Khỏe");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // ===== BANNER đầu trang =====
        JPanel panelBanner = new JPanel(new BorderLayout());
        panelBanner.setBackground(new Color(0, 123, 167));
        panelBanner.setPreferredSize(new Dimension(1000, 70));

        JLabel lblTieuDe = new JLabel(
            "  🏥  HỆ CHUYÊN GIA TƯ VẤN SỨC KHỎE THƯỜNG GẶP", SwingConstants.LEFT);
        lblTieuDe.setFont(new Font("Arial", Font.BOLD, 18));
        lblTieuDe.setForeground(Color.WHITE);
        panelBanner.add(lblTieuDe, BorderLayout.CENTER);

        // Logo bên phải banner
        JLabel lblLogo = new JLabel("  v1.0  ", SwingConstants.RIGHT);
        lblLogo.setFont(new Font("Arial", Font.PLAIN, 12));
        lblLogo.setForeground(new Color(200,240,255));
        panelBanner.add(lblLogo, BorderLayout.EAST);

        add(panelBanner, BorderLayout.NORTH);

        // ===== SIDEBAR bên trái =====
        panelSidebar = new JPanel();
        panelSidebar.setLayout(new BoxLayout(panelSidebar, BoxLayout.Y_AXIS));
        panelSidebar.setBackground(new Color(30, 90, 120));
        panelSidebar.setPreferredSize(new Dimension(180, 580));
        panelSidebar.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));

        // Các nút sidebar
        String[][] menuItems = {
            {"🩺  Chẩn đoán", "CHANDOAN"},
            {"📋  Lịch sử",   "LICHSU"},
            {"📊  Thống kê",  "THONGKE"},
            {"🔍  Tìm kiếm",  "TIMKIEM"}
        };
        for (String[] item : menuItems) {
            JButton btn = taoNutSidebar(item[0], item[1]);
            panelSidebar.add(btn);
            panelSidebar.add(Box.createVerticalStrut(6));
        }

        add(panelSidebar, BorderLayout.WEST);

        // ===== PANEL NỘI DUNG bên phải (CardLayout) =====
        cardLayout = new CardLayout();
        panelNoidung = new JPanel(cardLayout);

        chanDoanForm = new ChanDoanForm();
        lichSuForm   = new LichSuForm();
        thongKeForm  = new ThongKeForm();
        timKiemForm  = new TimKiemForm();

        panelNoidung.add(chanDoanForm, "CHANDOAN");
        panelNoidung.add(lichSuForm,   "LICHSU");
        panelNoidung.add(thongKeForm,  "THONGKE");
        panelNoidung.add(timKiemForm,  "TIMKIEM");

        add(panelNoidung, BorderLayout.CENTER);

        // Hiện mặc định tab Chẩn đoán
        cardLayout.show(panelNoidung, "CHANDOAN");
    }

    // Tạo nút sidebar đẹp
    private JButton taoNutSidebar(String text, String card) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(170, 45));
        btn.setPreferredSize(new Dimension(170, 45));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setBackground(new Color(50, 130, 160));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            cardLayout.show(panelNoidung, card);
            // Nếu chuyển sang ThongKe thì cập nhật dữ liệu
            if ("THONGKE".equals(card)) thongKeForm.capNhatThongKe();
        });
        return btn;
    }
}