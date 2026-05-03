package view;

import model.LichSuChanDoan;
import service.LichSuService;
import utils.XuatFileUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class LichSuForm extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtTimKiem;
    private JButton btnTimKiem, btnLamMoi;

    public LichSuForm() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(8, 8));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel tìm kiếm
        JPanel panelTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelTop.setBackground(Color.WHITE);
        panelTop.add(new JLabel("🔍 Tìm kiếm:"));
        txtTimKiem = new JTextField(20);
        panelTop.add(txtTimKiem);
        btnTimKiem = new JButton("Tìm");
        btnTimKiem.addActionListener(e -> timKiem());
        panelTop.add(btnTimKiem);
        btnLamMoi = new JButton("Tất cả");
        btnLamMoi.addActionListener(e -> hienThiTatCa());
        panelTop.add(btnLamMoi);
        add(panelTop, BorderLayout.NORTH);

        // Bảng lịch sử
        String[] cot = {"Thời gian","Triệu chứng","Tên bệnh","% Phù hợp","Độ tuổi"};
        tableModel = new DefaultTableModel(cot, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(26);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(0, 123, 167));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(200, 230, 250));

        // Click dòng → hiện chi tiết
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0) hienChiTiet(row);
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);
        hienThiTatCa();
    }

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

    private void timKiem() {
        String kw = txtTimKiem.getText().trim();
        if (kw.isEmpty()) { hienThiTatCa(); return; }
        tableModel.setRowCount(0);
        List<LichSuChanDoan> ds = LichSuService.getInstance().timKiem(kw);
        for (LichSuChanDoan ls : ds) {
            tableModel.addRow(new Object[]{
                ls.getThoiGian(), ls.getTrieuChungDaChon(),
                ls.getTenBenh(), ls.getPhanTramPhuHop() + "%", ls.getDoTuoi()
            });
        }
        if (ds.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy kết quả.",
                "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void hienChiTiet(int row) {
        String msg =
            "Thời gian: "  + tableModel.getValueAt(row, 0) + "\n" +
            "Triệu chứng: " + tableModel.getValueAt(row, 1) + "\n" +
            "Bệnh: "        + tableModel.getValueAt(row, 2) + "\n" +
            "Phù hợp: "     + tableModel.getValueAt(row, 3) + "\n" +
            "Độ tuổi: "     + tableModel.getValueAt(row, 4);
        JOptionPane.showMessageDialog(this, msg, "Chi tiết lịch sử",
            JOptionPane.INFORMATION_MESSAGE);
    }
}