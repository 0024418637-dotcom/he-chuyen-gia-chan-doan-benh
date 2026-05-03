package view;

import model.Benh;
import model.LichSuChanDoan;
import service.ChanDoanService;
import service.LichSuService;
import utils.HinhAnhUtils;
import utils.XuatFileUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class TimKiemForm extends JPanel {

    private JTextField txtTimKiem;
    private JComboBox<String> cboLoaiTimKiem;
    private JButton btnTim, btnXoa, btnXuatFile;

    private JTable tableKetQua;
    private DefaultTableModel tableModel;

    private JTextArea txtChiTiet;
    private JLabel lblHinhAnh;

    public TimKiemForm() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // ===== PANEL TÌM KIẾM (trên) =====
        JPanel panelTim = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        panelTim.setBackground(new Color(235, 245, 255));
        panelTim.setBorder(BorderFactory.createTitledBorder("🔍 Tìm kiếm"));

        panelTim.add(new JLabel("Từ khóa:"));
        txtTimKiem = new JTextField(22);
        txtTimKiem.setFont(new Font("Arial", Font.PLAIN, 13));
        // Nhấn Enter cũng tìm
        txtTimKiem.addActionListener(e -> xuLyTimKiem());
        panelTim.add(txtTimKiem);

        panelTim.add(new JLabel("Loại:"));
        cboLoaiTimKiem = new JComboBox<>(new String[]{
            "Tất cả", "Tìm theo tên bệnh", "Tìm theo triệu chứng"
        });
        cboLoaiTimKiem.setFont(new Font("Arial", Font.PLAIN, 13));
        panelTim.add(cboLoaiTimKiem);

        btnTim   = taoNut("🔍 Tìm",    new Color(0, 123, 167));
        btnXoa   = taoNut("🔄 Xóa",    new Color(100, 100, 100));
        btnXuatFile = taoNut("💾 Xuất .txt", new Color(140, 90, 0));

        btnTim.addActionListener(e -> xuLyTimKiem());
        btnXoa.addActionListener(e -> xuLyXoa());
        btnXuatFile.addActionListener(e -> xuLyXuatFile());

        panelTim.add(btnTim);
        panelTim.add(btnXoa);
        panelTim.add(btnXuatFile);

        add(panelTim, BorderLayout.NORTH);

        // ===== PANEL GIỮA: Bảng kết quả =====
        String[] cot = {"Thời gian", "Triệu chứng", "Bệnh chẩn đoán", "% Phù hợp", "Độ tuổi"};
        tableModel = new DefaultTableModel(cot, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tableKetQua = new JTable(tableModel);
        tableKetQua.setRowHeight(26);
        tableKetQua.setFont(new Font("Arial", Font.PLAIN, 12));
        tableKetQua.getTableHeader().setBackground(new Color(0, 123, 167));
        tableKetQua.getTableHeader().setForeground(Color.WHITE);
        tableKetQua.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        tableKetQua.setSelectionBackground(new Color(200, 230, 250));
        tableKetQua.setGridColor(new Color(220, 230, 240));

        // Click dòng → hiện chi tiết bên dưới
        tableKetQua.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) hienChiTiet();
        });

        add(new JScrollPane(tableKetQua), BorderLayout.CENTER);

        // ===== PANEL DƯỚI: Chi tiết + hình ảnh =====
        JPanel panelDuoi = new JPanel(new BorderLayout(10, 0));
        panelDuoi.setBackground(Color.WHITE);
        panelDuoi.setBorder(BorderFactory.createTitledBorder("Chi tiết kết quả"));
        panelDuoi.setPreferredSize(new Dimension(0, 170));

        // Hình ảnh bên trái
        lblHinhAnh = new JLabel("", SwingConstants.CENTER);
        lblHinhAnh.setPreferredSize(new Dimension(150, 140));
        lblHinhAnh.setBorder(BorderFactory.createLineBorder(new Color(200, 220, 230)));
        lblHinhAnh.setText("--");
        panelDuoi.add(lblHinhAnh, BorderLayout.WEST);

        // Nội dung chi tiết bên phải
        txtChiTiet = new JTextArea();
        txtChiTiet.setEditable(false);
        txtChiTiet.setFont(new Font("Arial", Font.PLAIN, 13));
        txtChiTiet.setLineWrap(true);
        txtChiTiet.setWrapStyleWord(true);
        txtChiTiet.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        txtChiTiet.setBackground(new Color(248, 252, 255));
        txtChiTiet.setText("Chọn một dòng trong bảng để xem chi tiết.");
        panelDuoi.add(new JScrollPane(txtChiTiet), BorderLayout.CENTER);

        add(panelDuoi, BorderLayout.SOUTH);

        // Hiện toàn bộ lịch sử lúc mở form
        hienThiTatCa();
    }

    private JButton taoNut(String text, Color mau) {
        JButton btn = new JButton(text);
        btn.setBackground(mau);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setPreferredSize(new Dimension(110, 32));
        return btn;
    }

    // ===== HIỆN TOÀN BỘ LỊCH SỬ =====
    private void hienThiTatCa() {
        tableModel.setRowCount(0);
        for (LichSuChanDoan ls : LichSuService.getInstance().getDanhSach()) {
            tableModel.addRow(new Object[]{
                ls.getThoiGian(),
                ls.getTrieuChungDaChon(),
                ls.getTenBenh(),
                ls.getPhanTramPhuHop() + "%",
                ls.getDoTuoi()
            });
        }
    }

    // ===== XỬ LÝ TÌM KIẾM =====
    private void xuLyTimKiem() {
        String tuKhoa = txtTimKiem.getText().trim();

        // Bẫy lỗi để trống
        if (tuKhoa.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng nhập từ khóa tìm kiếm!",
                "Chú ý", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String loai = (String) cboLoaiTimKiem.getSelectedItem();
        List<LichSuChanDoan> tat_ca = LichSuService.getInstance().getDanhSach();

        tableModel.setRowCount(0);
        int demKetQua = 0;

        for (LichSuChanDoan ls : tat_ca) {
            boolean khop = false;

            if ("Tìm theo tên bệnh".equals(loai)) {
                khop = ls.getTenBenh().toLowerCase().contains(tuKhoa.toLowerCase());
            } else if ("Tìm theo triệu chứng".equals(loai)) {
                khop = ls.getTrieuChungDaChon().toLowerCase().contains(tuKhoa.toLowerCase());
            } else { // Tất cả
                khop = ls.getTenBenh().toLowerCase().contains(tuKhoa.toLowerCase())
                    || ls.getTrieuChungDaChon().toLowerCase().contains(tuKhoa.toLowerCase());
            }

            if (khop) {
                tableModel.addRow(new Object[]{
                    ls.getThoiGian(),
                    ls.getTrieuChungDaChon(),
                    ls.getTenBenh(),
                    ls.getPhanTramPhuHop() + "%",
                    ls.getDoTuoi()
                });
                demKetQua++;
            }
        }

        // Thông báo kết quả
        if (demKetQua == 0) {
            JOptionPane.showMessageDialog(this,
                "Không tìm thấy kết quả cho: \"" + tuKhoa + "\"",
                "Không có kết quả", JOptionPane.INFORMATION_MESSAGE);
        } else {
            // Hiện số kết quả tìm được trên tiêu đề bảng
            // (có thể thêm JLabel để hiện số kết quả)
        }
    }

    // ===== XỬ LÝ NÚT XÓA / LÀM MỚI =====
    private void xuLyXoa() {
        txtTimKiem.setText("");
        cboLoaiTimKiem.setSelectedIndex(0);
        txtChiTiet.setText("Chọn một dòng trong bảng để xem chi tiết.");
        lblHinhAnh.setIcon(null);
        lblHinhAnh.setText("--");
        hienThiTatCa();
    }

    // ===== HIỆN CHI TIẾT KHI CLICK DÒNG =====
    private void hienChiTiet() {
        int row = tableKetQua.getSelectedRow();
        if (row < 0) return;

        String tenBenh     = tableModel.getValueAt(row, 2).toString();
        String trieuChung  = tableModel.getValueAt(row, 1).toString();
        String phanTram    = tableModel.getValueAt(row, 3).toString();
        String thoiGian    = tableModel.getValueAt(row, 0).toString();
        String doTuoi      = tableModel.getValueAt(row, 4).toString();

        // Hiện thông tin chi tiết
        txtChiTiet.setText(
            "⏰ Thời gian    : " + thoiGian  + "\n" +
            "🤒 Triệu chứng  : " + trieuChung + "\n" +
            "🏥 Bệnh chẩn đoán: " + tenBenh   + "\n" +
            "📊 Độ phù hợp   : " + phanTram   + "\n" +
            "👤 Độ tuổi      : " + doTuoi
        );

        // Tìm hình ảnh của bệnh từ ChanDoanService
        // (vì lịch sử chỉ lưu tên bệnh, cần map sang tên file ảnh)
        String hinhAnh = layTenHinhAnh(tenBenh);
        ImageIcon icon = utils.HinhAnhUtils.layHinhAnh(hinhAnh, 140, 130);
        if (icon != null) {
            lblHinhAnh.setIcon(icon);
            lblHinhAnh.setText("");
        } else {
            lblHinhAnh.setIcon(null);
            lblHinhAnh.setText(tenBenh);
        }
    }

    // Map tên bệnh → tên file ảnh
    private String layTenHinhAnh(String tenBenh) {
        switch (tenBenh) {
            case "Cảm cúm":            return "camcum.png";
            case "Viêm họng":          return "viemhong.png";
            case "Đau dạ dày":         return "daday.png";
            case "Rối loạn tiêu hóa":  return "roiloan.png";
            case "Mất ngủ":            return "matngungcu.png";
            case "Stress tâm lý":      return "stress.png";
            default:                   return "no_image.png";
        }
    }

    // ===== XUẤT FILE TXT KẾT QUẢ TÌM KIẾM =====
    private void xuLyXuatFile() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                "Không có dữ liệu để xuất!",
                "Chú ý", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Tạo nội dung xuất
        StringBuilder sb = new StringBuilder();
        sb.append("KẾT QUẢ TÌM KIẾM — HỆ CHUYÊN GIA SỨC KHỎE\n");
        sb.append("Từ khóa: ").append(txtTimKiem.getText()).append("\n");
        sb.append("=".repeat(50)).append("\n");

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            sb.append(i + 1).append(". ")
              .append(tableModel.getValueAt(i, 2)).append("\n");
            sb.append("   Triệu chứng : ").append(tableModel.getValueAt(i, 1)).append("\n");
            sb.append("   Phù hợp     : ").append(tableModel.getValueAt(i, 3)).append("\n");
            sb.append("   Thời gian   : ").append(tableModel.getValueAt(i, 0)).append("\n");
            sb.append("-".repeat(40)).append("\n");
        }

        // Dùng JFileChooser để chọn nơi lưu
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new java.io.File("KetQuaTimKiem.txt"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (java.io.PrintWriter pw = new java.io.PrintWriter(
                    new java.io.OutputStreamWriter(
                        new java.io.FileOutputStream(fc.getSelectedFile()), "UTF-8"))) {
                pw.print(sb.toString());
                JOptionPane.showMessageDialog(this,
                    "Xuất file thành công!\n" + fc.getSelectedFile().getAbsolutePath(),
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "Lỗi xuất file: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}