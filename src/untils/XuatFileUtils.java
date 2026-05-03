package utils;

import model.Benh;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class XuatFileUtils {

    public static void xuatTXT(Benh benh, String noiDungKetQua, Component parent) {
        // Hộp thoại chọn nơi lưu file
        JFileChooser fc = new JFileChooser();
        String tenFile = "KetQua_" +
            new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".txt";
        fc.setSelectedFile(new File(tenFile));

        int lua = fc.showSaveDialog(parent);
        if (lua != JFileChooser.APPROVE_OPTION) return;

        File file = fc.getSelectedFile();
        try (PrintWriter pw = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(file), "UTF-8"))) {

            pw.println("============================================");
            pw.println("   KẾT QUẢ TƯ VẤN SỨC KHỎE");
            pw.println("============================================");
            pw.println("Ngày: " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()));
            pw.println("Tên bệnh: " + benh.getTenBenh());
            pw.println("Mức cảnh báo: " + benh.getMucCanhBao());
            pw.println("--------------------------------------------");
            pw.println(noiDungKetQua);
            pw.println("============================================");
            pw.println("* Đây là tư vấn tham khảo, không thay thế bác sĩ.");

            JOptionPane.showMessageDialog(parent,
                "Xuất file thành công!\n" + file.getAbsolutePath(),
                "Thành công", JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(parent,
                "Lỗi xuất file: " + ex.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}