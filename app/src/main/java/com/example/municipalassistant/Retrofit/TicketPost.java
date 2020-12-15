package com.example.municipalassistant.Retrofit;

public class TicketPost {

    private int id;

    private String opis;

    private String slika;

    private int status;

    private int datum_prijave;

    private String longituda;

    private String latituda;


    public TicketPost(int id, String opis, String slika, int status, int datum_prijave, String longituda, String latituda) {
        this.id = id;
        this.opis = opis;
        this.slika = slika;
        this.status = status;
        this.datum_prijave = datum_prijave;
        this.longituda = longituda;
        this.latituda = latituda;
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

    public int getStatus() {
        return status;
    }

    public int getDatum_prijave() {
        return datum_prijave;
    }

    public String getLongituda() {
        return longituda;
    }

    public String getLatituda() {
        return latituda;
    }
}
