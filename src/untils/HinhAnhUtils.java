package utils;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class HinhAnhUtils {

    /**
     * Tải hình ảnh từ thư mục images/, scale theo kích thước cho trước.
     * Nếu không tìm thấy → trả về ảnh no_image.png
     */
    public static ImageIcon layHinhAnh(String tenFile, int width, int height) {
        try {
            URL url = HinhAnhUtils.class.getResource("/images/" + tenFile);
            if (url != null) {
                ImageIcon icon = new ImageIcon(url);
                Image img = icon.getImage()
                    .getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(img);
            }
        } catch (Exception e) {
            // Không tìm thấy ảnh, dùng ảnh mặc định
        }

        // Ảnh mặc định nếu không có
        try {
            URL urlDefault = HinhAnhUtils.class.getResource("/images/no_image.png");
            if (urlDefault != null) {
                ImageIcon icon = new ImageIcon(urlDefault);
                Image img = icon.getImage()
                    .getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(img);
            }
        } catch (Exception ignored) {}

        return null;
    }
}