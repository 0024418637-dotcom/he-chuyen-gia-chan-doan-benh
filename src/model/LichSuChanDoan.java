package model;

public class LichSuChanDoan {
    private String thoiGian;
    private String trieuChungDaChon;  // VD: "Sốt, Ho, Đau họng"
    private String tenBenh;
    private double phanTramPhuHop;    // VD: 85.5
    private String doTuoi;
    private String thoiGianXuatHien;

    public LichSuChanDoan() {}

    public LichSuChanDoan(String thoiGian, String trieuChungDaChon,
                          String tenBenh, double phanTramPhuHop,
                          String doTuoi, String thoiGianXuatHien) {
        this.thoiGian = thoiGian;
        this.trieuChungDaChon = trieuChungDaChon;
        this.tenBenh = tenBenh;
        this.phanTramPhuHop = phanTramPhuHop;
        this.doTuoi = doTuoi;
        this.thoiGianXuatHien = thoiGianXuatHien;
    }

    // Getter / Setter (viết tương tự Benh.java)
    public String getThoiGian()                   { return thoiGian; }
    public void   setThoiGian(String v)           { this.thoiGian = v; }
    public String getTrieuChungDaChon()           { return trieuChungDaChon; }
    public void   setTrieuChungDaChon(String v)   { this.trieuChungDaChon = v; }
    public String getTenBenh()                    { return tenBenh; }
    public void   setTenBenh(String v)            { this.tenBenh = v; }
    public double getPhanTramPhuHop()             { return phanTramPhuHop; }
    public void   setPhanTramPhuHop(double v)     { this.phanTramPhuHop = v; }
    public String getDoTuoi()                     { return doTuoi; }
    public void   setDoTuoi(String v)             { this.doTuoi = v; }
    public String getThoiGianXuatHien()           { return thoiGianXuatHien; }
    public void   setThoiGianXuatHien(String v)   { this.thoiGianXuatHien = v; }
}