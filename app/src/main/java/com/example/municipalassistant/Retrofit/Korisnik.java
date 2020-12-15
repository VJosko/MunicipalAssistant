package com.example.municipalassistant.Retrofit;

public class Korisnik {
    public int id;
    public String username;
    public String password;
    public String ImePrezime;

    public Korisnik(int id, String username, String password, String imePrezime) {
        this.id = id;
        this.username = username;
        this.password = password;
        ImePrezime = imePrezime;
    }
}
