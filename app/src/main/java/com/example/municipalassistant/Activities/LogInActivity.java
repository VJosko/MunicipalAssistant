package com.example.municipalassistant.Activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.municipalassistant.R;
import com.example.municipalassistant.Retrofit.JsonPlaceHolderApi;
import com.example.municipalassistant.Retrofit.Korisnik;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LogInActivity extends AppCompatActivity {

    private TextInputLayout txtUsername, txtPassword;
    private TextView tvUpozorenje;
    private Button btnPrijava;

    private JsonPlaceHolderApi jsonPlaceHolderApi;

    List<Korisnik> lKorisnik;

    private Toolbar toolbar;
    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("Prijava");
        actionBar.setDisplayHomeAsUpEnabled(true);

        txtUsername = findViewById(R.id.txt_username);
        txtPassword = findViewById(R.id.txt_password);
        tvUpozorenje = findViewById(R.id.tv_upozorenje);
        btnPrijava = findViewById(R.id.btn_prijava);
        btnPrijava.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateLogin();
            }
        });

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://student.vsmti.hr/jvudrag/PIS/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient)
                .build();

        jsonPlaceHolderApi = retrofit.create(JsonPlaceHolderApi.class);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void validateLogin() {
        Korisnik korisnik = new Korisnik(1,txtUsername.getEditText().getText().toString().toString(),txtPassword.getEditText().getText().toString(),"ime");
        Call<ResponseBody> call = jsonPlaceHolderApi.validateLogin(korisnik);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful()){
                    try {
                        String x = response.body().string();
                        Log.e("Response body", x);
                        if(x.length() < 3){
                            incorrect();
                        }
                        else {
                            correct();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
            }
        });
    }

    private void correct(){
        Log.e("CORRECT", "    TAJ COVJEK POSTOJI");
        Intent intent = new Intent(LogInActivity.this, AdminActivity.class);
        startActivity(intent);
    }
    private void incorrect(){
        Log.e("INCORRECT", "    TAJ COVJEK NE POSTOJI");
        if(txtUsername.getEditText().getText().toString().isEmpty()){
            txtUsername.setError("Upišite korisničko ime");
        }
        else{
            txtUsername.setError("");
        }
        if(txtPassword.getEditText().getText().toString().isEmpty()){
            txtPassword.setError("Upišite lozinku");
        }
        else{
            txtPassword.setError("");
        }
        if(!txtUsername.getEditText().getText().toString().isEmpty() && !txtPassword.getEditText().getText().toString().isEmpty()){
            tvUpozorenje.setVisibility(View.VISIBLE);
        }
        else{
            tvUpozorenje.setVisibility(View.INVISIBLE);
        }
    }
}