package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class DangNhapForm extends JFrame {

    private JTextField txtTaiKhoan;
    private JPasswordField txtMatKhau;
    private JButton btnDangNhap;
    private JLabel lblLogo;

    // Tài khoản mẫu (hardcode cho sinh viên, có thể nâng cấp DB)
    private static final String USER = "admin";
    private static final String PASS = "123456";

    public DangNhapForm() {
        initComponents();
        setTitle("Đăng nhập — Hệ Chuyên Gia Sức Khỏe");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(400, 320);
        setLocationRelativeTo(null);  // Căn giữa màn hình
        setResizable(false);
    }

    private void initComponents() {
        // Panel chính màu xanh y tế
        JPanel panelMain = new JPanel(null);  // null layout để tự đặt toạ độ
        panelMain.setBackground(new Color(0, 123, 167));

        // Logo (nếu có file logo.png trong images/)
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/images/logo.png"));
            Image img = icon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
            lblLogo = new JLabel(new ImageIcon(img));
        } catch (Exception e) {
            lblLogo = new JLabel("🏥");
            lblLogo.setFont(new Font("Arial", Font.PLAIN, 40));
        }
        lblLogo.setForeground(Color.WHITE);
        lblLogo.setBounds(170, 20, 60, 60);
        panelMain.add(lblLogo);

        JLabel lblTitle = new JLabel("HỆ CHUYÊN GIA SỨC KHỎE", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 14));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setBounds(20, 88, 360, 25);
        panelMain.add(lblTitle);

        // Panel form trắng
        JPanel panelForm = new JPanel(null);
        panelForm.setBackground(Color.WHITE);
        panelForm.setBounds(30, 125, 340, 160);
        panelForm.setBorder(BorderFactory.createLineBorder(new Color(200,200,200)));

        JLabel lblUser = new JLabel("Tài khoản:");
        lblUser.setBounds(20, 15, 90, 25);
        panelForm.add(lblUser);

        txtTaiKhoan = new JTextField();
        txtTaiKhoan.setBounds(115, 15, 200, 28);
        panelForm.add(txtTaiKhoan);

        JLabel lblPass = new JLabel("Mật khẩu:");
        lblPass.setBounds(20, 55, 90, 25);
        panelForm.add(lblPass);

        txtMatKhau = new JPasswordField();
        txtMatKhau.setBounds(115, 55, 200, 28);
        panelForm.add(txtMatKhau);

        btnDangNhap = new JButton("ĐĂNG NHẬP");
        btnDangNhap.setBounds(95, 105, 150, 35);
        btnDangNhap.setBackground(new Color(0, 123, 167));
        btnDangNhap.setForeground(Color.WHITE);
        btnDangNhap.setFocusPainted(false);
        btnDangNhap.addActionListener(e -> xuLyDangNhap());
        panelForm.add(btnDangNhap);

        // Enter cũng đăng nhập
        txtMatKhau.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) xuLyDangNhap();
            }
        });

        panelMain.add(panelForm);
        setContentPane(panelMain);
    }

    private void xuLyDangNhap() {
        String user = txtTaiKhoan.getText().trim();
        String pass = new String(txtMatKhau.getPassword()).trim();

        // Bẫy lỗi: để trống
        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng nhập đầy đủ tài khoản và mật khẩu!",
                "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Kiểm tra tài khoản
        if (user.equals(USER) && pass.equals(PASS)) {
            JOptionPane.showMessageDialog(this,
                "Đăng nhập thành công! Chào mừng bạn.", "Thành công",
                JOptionPane.INFORMATION_MESSAGE);
            this.dispose();
            new TrangChuForm().setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this,
                "Tài khoản hoặc mật khẩu không đúng!",
                "Lỗi đăng nhập", JOptionPane.ERROR_MESSAGE);
            txtMatKhau.setText("");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DangNhapForm().setVisible(true));
    }
}