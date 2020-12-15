package com.example.municipalassistant.Retrofit;

public class TicketUpdate {

    private int id;

    private String komentar;

    private int status;

    private String datum_zavrsetka;

    public TicketUpdate(int id, String komentar, int status, String datum_zavrsetka) {
        this.id = id;
        this.komentar = komentar;
        this.status = status;
        this.datum_zavrsetka = datum_zavrsetka;
    }

    public int getId() {
        return id;
    }

    public String getKomentar() {
        return komentar;
    }

    public int getStatus() {
        return status;
    }

    public String getDatum_zavrsetka() {
        return datum_zavrsetka;
    }
}
