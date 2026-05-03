package model;

public class TrieuChung {
    private String maTrieuChung;
    private String tenTrieuChung;

    public TrieuChung() {}

    public TrieuChung(String maTrieuChung, String tenTrieuChung) {
        this.maTrieuChung = maTrieuChung;
        this.tenTrieuChung = tenTrieuChung;
    }

    public String getMaTrieuChung()              { return maTrieuChung; }
    public void   setMaTrieuChung(String v)      { this.maTrieuChung = v; }
    public String getTenTrieuChung()             { return tenTrieuChung; }
    public void   setTenTrieuChung(String v)     { this.tenTrieuChung = v; }

    @Override
    public String toString() { return tenTrieuChung; }
}