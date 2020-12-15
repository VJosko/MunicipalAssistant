package com.example.municipalassistant.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.example.municipalassistant.R;
import com.example.municipalassistant.Retrofit.JsonPlaceHolderApi;
import com.example.municipalassistant.Retrofit.Ticket;
import com.example.municipalassistant.Adapters.recAdapterTickets;

import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TicketiActivity extends AppCompatActivity implements recAdapterTickets.OnTicketListener{

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private JsonPlaceHolderApi jsonPlaceHolderApi;

    List<Ticket> lTickets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticketi);

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

        getTickets();

        //------------Recycler-view------------------
        recyclerView = findViewById(R.id.rec);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

    }

    private void getTickets(){
        Call<List<Ticket>> call = jsonPlaceHolderApi.getTickets();

        call.enqueue(new Callback<List<Ticket>>() {
            @Override
            public void onResponse(Call<List<Ticket>> call, Response<List<Ticket>> response) {

                if(!response.isSuccessful()){
                    return;
                }

                lTickets = response.body();

//                mAdapter = new recAdapterTickets(lTickets, this);
//                recyclerView.setAdapter(mAdapter);
            }

            @Override
            public void onFailure(Call<List<Ticket>> call, Throwable t) {
            }
        });
    }

    @Override
    public void onTicketClick(int position) {

    }
}
