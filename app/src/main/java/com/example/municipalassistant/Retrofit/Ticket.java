package com.example.municipalassistant.Retrofit;

public class Ticket {

    public int id;

    private String opis;

    private String status;

    private String datum_prijave;

    private String slika;

    private String longituda;

    private String latituda;

    private String komentar;

    public Ticket(int id, String opis, String status, String datum_prijave, String slika, String longituda, String latituda, String komentar) {
        this.id = id;
        this.opis = opis;
        this.slika = slika;
        this.status = status;
        this.datum_prijave = datum_prijave;
        this.longituda = longituda;
        this.latituda = latituda;
        this.komentar = komentar;
    }

    public String getKomentar() {
        return komentar;
    }

    public int getId() {
        return id;
    }

    public String getOpis() {
        return opis;
    }

    public String getSlika() {
        return slika;
    }

    public String getStatus() {
        return status;
    }

    public String getDatum_prijave() {
        return datum_prijave;
    }

    public String getLongituda() {
        return longituda;
    }

    public String getLatituda() {
        return latituda;
    }
}
