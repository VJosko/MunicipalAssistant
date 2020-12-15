package com.example.municipalassistant.Retrofit;

public class Komentar {
    public int id;
    public int ID_Ticket;
    public int ID_Korisnik;
    public int Datum;
    public String Komentar;

    public Komentar(int id, int ID_Ticket, int ID_Korisnik, int Datum, String Komentar) {
        this.id = id;
        this.ID_Ticket = ID_Ticket;
        this.ID_Korisnik = ID_Korisnik;
        this.Datum = Datum;
        this.Komentar = Komentar;
    }
}
