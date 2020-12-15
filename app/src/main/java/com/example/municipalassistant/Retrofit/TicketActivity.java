package com.example.municipalassistant.Retrofit;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.municipalassistant.R;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TicketActivity extends AppCompatActivity {

    private TextView result;

    private JsonPlaceHolderApi jsonPlaceHolderApi;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        result = findViewById(R.id.opis);

        Gson gson = new GsonBuilder().setLenient().create();

        /*OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();*/

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://student.vsmti.hr/jvudrag/PIS/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        jsonPlaceHolderApi = retrofit.create(JsonPlaceHolderApi.class);

        //getTickets();
        createTicket();
        //post();
    }

    /*private void createTicket() {
        Call<TicketPost> call = jsonPlaceHolderApi.createTicket(20,"probni opis","nema slike",1,20200603,"123","456");
    }*/

    public void post() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("http://student.vsmti.hr/jvudrag/PIS/json.php?action=add_ticket");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    conn.setRequestProperty("Accept","application/json");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);

                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("id", 10);
                    jsonParam.put("opis", "probni opis");
                    jsonParam.put("slika", "nema");
                    jsonParam.put("status", 1);
                    jsonParam.put("datum_prijave", 1);
                    jsonParam.put("longituda", "1");
                    jsonParam.put("latituda", "1");
                    jsonParam.put("komentar", "1");

                    Log.i("JSON", jsonParam.toString());
                    DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                    //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
                    os.writeBytes(jsonParam.toString());

                    os.flush();
                    os.close();

                    Log.i("STATUS", String.valueOf(conn.getResponseCode()));
                    Log.i("MSG" , conn.getResponseMessage());

                    conn.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }

    private void createTicket() {
        TicketPost ticketPost = new TicketPost(17,"probni opis","nema slike",1,20200603,"123","456");
        Call<ResponseBody> call = jsonPlaceHolderApi.createTicket(ticketPost);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(!response.isSuccessful()){
                    result.setText("Code: " + response.code());
                    return;
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                result.setText(t.getMessage());
            }
        });
    }

    private void getTickets(){
        Call<List<Ticket>> call = jsonPlaceHolderApi.getTickets();

        call.enqueue(new Callback<List<Ticket>>() {
            @Override
            public void onResponse(Call<List<Ticket>> call, Response<List<Ticket>> response) {

                if(!response.isSuccessful()){
                    result.setText("Code: " + response.code());
                    return;
                }
                List<Ticket> tickets = response.body();

                for(Ticket ticket : tickets){
                    String content = "";
                    content += "ID: " + ticket.getId() + "\n";
                    content += "Opis: " + ticket.getOpis() + "\n";
                    content += "Status: " + ticket.getStatus() + "\n\n";
                    content += "Datum prijave: " + ticket.getDatum_prijave() + "\n";
                    content += "Slika: " + ticket.getSlika() + "\n";
                    content += "Longituda: " + ticket.getLongituda() + "\n";
                    content += "Latituda: " + ticket.getLatituda() + "\n";

                    result.append(content);
                }
            }

            @Override
            public void onFailure(Call<List<Ticket>> call, Throwable t) {
                result.setText(t.getMessage());
            }
        });
    }
}
