package model;

public class Benh {
    // ===== Thuộc tính =====
    private String maBenh;
    private String tenBenh;
    private String moTa;
    private String huongDan;
    private String hinhAnh;      // tên file, VD: "camcum.png"
    private String mucCanhBao;   // "Nhẹ", "Trung bình", "Nặng"

    // ===== Constructor rỗng =====
    public Benh() {}

    // ===== Constructor đầy đủ =====
    public Benh(String maBenh, String tenBenh, String moTa,
                String huongDan, String hinhAnh, String mucCanhBao) {
        this.maBenh = maBenh;
        this.tenBenh = tenBenh;
        this.moTa = moTa;
        this.huongDan = huongDan;
        this.hinhAnh = hinhAnh;
        this.mucCanhBao = mucCanhBao;
    }

    // ===== Getter / Setter =====
    public String getMaBenh()              { return maBenh; }
    public void   setMaBenh(String v)      { this.maBenh = v; }
    public String getTenBenh()             { return tenBenh; }
    public void   setTenBenh(String v)     { this.tenBenh = v; }
    public String getMoTa()                { return moTa; }
    public void   setMoTa(String v)        { this.moTa = v; }
    public String getHuongDan()            { return huongDan; }
    public void   setHuongDan(String v)    { this.huongDan = v; }
    public String getHinhAnh()             { return hinhAnh; }
    public void   setHinhAnh(String v)     { this.hinhAnh = v; }
    public String getMucCanhBao()          { return mucCanhBao; }
    public void   setMucCanhBao(String v)  { this.mucCanhBao = v; }

    @Override
    public String toString() {
        return tenBenh + " [" + mucCanhBao + "]";
    }
}