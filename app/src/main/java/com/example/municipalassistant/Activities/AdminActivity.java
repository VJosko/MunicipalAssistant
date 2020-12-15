package com.example.municipalassistant.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.municipalassistant.Adapters.TicketsPagerAdapter;
import com.example.municipalassistant.Dialog.EditTicketDialogFragment;
import com.example.municipalassistant.Fragments.TicketListFragment;
import com.example.municipalassistant.Fragments.TicketMapFragment;
import com.example.municipalassistant.R;
import com.example.municipalassistant.Retrofit.JsonPlaceHolderApi;
import com.example.municipalassistant.Retrofit.Ticket;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static java.lang.Integer.parseInt;

public class AdminActivity extends AppCompatActivity implements TicketListFragment.ITicketListFragment,
        TicketMapFragment.ITicketMapFragment, EditTicketDialogFragment.IEditTicket {

    private static final String TAG = "AdminActivity";

    private ViewPager2 viewPager;
    private FragmentStateAdapter pagerAdapter;
    private TabLayout tabLayout;
    private Toolbar toolbar;

    private ActionBar actionBar;

    private JsonPlaceHolderApi jsonPlaceHolderApi;
    private List<Ticket> lTickets;
    private List<Ticket> tickets;
    private int nSelected;

    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    public SharedPreferences.OnSharedPreferenceChangeListener spChange;

    List<Fragment> fragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        sharedPref = getPreferences(Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        editor.putInt("nSelected", 0);
        editor.putInt("ItemChanged", 0);
        editor.putInt("SelectedTicketId", -1);
        editor.commit();

        tickets = new ArrayList<>();
        fragments = new ArrayList<>();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        //actionBar.setTitle("Tickets");
        actionBar.setDisplayHomeAsUpEnabled(true);

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
    }

    private void loadFragments(){
        //Inicijalizacija ViewPager2, PagerAdapter i tab
        viewPager = findViewById(R.id.pager);
        tabLayout = findViewById(R.id.tab_layout);

        pagerAdapter = new TicketsPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setUserInputEnabled(false);

        new TabLayoutMediator(tabLayout, viewPager,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        switch (position){
                            case 0:
                                tab.setText("Lista");
                                break;
                            case 1:
                                tab.setText("Karta");
                                break;
                        }
                    }
                }).attach();

        fragments.add(getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + viewPager.getCurrentItem()));


    }

    private void getTickets(){
        Call<List<Ticket>> call = jsonPlaceHolderApi.getTickets();
        call.enqueue(new Callback<List<Ticket>>() {
            @Override
            public void onResponse(Call<List<Ticket>> call, Response<List<Ticket>> response) {

                if(!response.isSuccessful()){
                    return;
                }

                lTickets = sortTickets(response.body());
                loadFragments();
            }

            @Override
            public void onFailure(Call<List<Ticket>> call, Throwable t) {
                loadFragments();
            }
        });
    }

    private void getUpdatedTickets(final int id){
        Call<List<Ticket>> call = jsonPlaceHolderApi.getTickets();

        call.enqueue(new Callback<List<Ticket>>() {
            @Override
            public void onResponse(Call<List<Ticket>> call, Response<List<Ticket>> response) {

                if(!response.isSuccessful()){
                    return;
                }

                lTickets = sortTickets(response.body());
                editor.putInt("ItemChanged", 1);
                editor.putInt("SelectedTicketId", id);
                editor.commit();
            }

            @Override
            public void onFailure(Call<List<Ticket>> call, Throwable t) {
                Log.d(TAG, "onFailure: ________________________nema");
            }
        });
    }

    private List<Ticket> sortTickets(List<Ticket> unsorted) {
        List<Ticket> sorted;
        sorted = new ArrayList<>();
        for (int i = unsorted.size() - 1; i > 0; i--) {
            sorted.add(unsorted.get(i));
        }
        return  sorted;
    }

    @Override
    public void onBackPressed() {
        boolean edit = false;
        EditTicketDialogFragment editTicketDialogFragment = (EditTicketDialogFragment) getSupportFragmentManager().findFragmentByTag("editDialog");
        if(editTicketDialogFragment != null && editTicketDialogFragment.isVisible()){
            edit = true;
        }
        if(viewPager.getCurrentItem() == 0 && !edit){
            super.onBackPressed();
        }
        else if(!edit){
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        }
        else if(edit){
            String text = editTicketDialogFragment.txtKomentar.getEditText().getText().toString();
            String komentar = editTicketDialogFragment.ticket.getKomentar();
            if(!komentar.equals(text)) {
                showAlertDialog(editTicketDialogFragment.getContext());
            }
            else{
                super.onBackPressed();
            }
        }
    }

    private void showAlertDialog(Context context){
        new MaterialAlertDialogBuilder(context)
                .setTitle("Odbaci promjene?")
                //.setMessage("poruka")
                .setNegativeButton("natrag", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Odbaci", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AdminActivity.super.onBackPressed();
                    }
                })
                .show();
    }

    private List<Ticket> filteredList(){
        tickets.clear();
        for(int i = 0; i < lTickets.size(); i++){
            if(getStatusId(lTickets.get(i).getStatus()) == nSelected){
                tickets.add(lTickets.get(i));
            }
        }
        if(nSelected == 0) {
            for(int i = 0; i < lTickets.size(); i++){
                tickets.add(lTickets.get(i));
            }
        }
        return tickets;
    }

    @Override
    public List<Ticket> getTicketsList() {
        return filteredList();
    }

    private int getStatusId(String status){
        switch(status){
            case "odbačen":
                return 3;
            case "otvoren":
                return 1;
            case "riješen":
                return 2;
            default:
                return 0;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.filter_action_button, menu);
        Drawable drawable = ContextCompat.getDrawable(getApplicationContext(),R.drawable.ic_baseline_filter_list_24);
        toolbar.setOverflowIcon(drawable);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(!item.isChecked()){
            item.setChecked(true);
        }
        switch (item.getItemId()){
            case R.id.filter_sve:
                nSelected = 0;
                editor.putInt("nSelected", 0);
                editor.commit();
                break;
            case R.id.filter_otvoreni:
                nSelected = 1;
                editor.putInt("nSelected", 1);
                editor.commit();
                break;
            case R.id.filter_rijeseni:
                nSelected = 2;
                editor.putInt("nSelected", 2);
                editor.commit();
                break;
            case R.id.filter_odbaceni:
                nSelected = 3;
                editor.putInt("nSelected", 3);
                editor.commit();
                break;
            case android.R.id.home:
                if(viewPager.getCurrentItem() == 0){
                    super.onBackPressed();
                    if(viewPager.getCurrentItem() == 0){
                        super.onBackPressed();
                    }
                }
                else{
                    viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
                }
        }
        return true;
    }

    @Override
    public Ticket getSelectedTicket(int position) {
        return filteredList().get(position);
    }

    @Override
    public void goToMapFragment(int id) {
        editor.putInt("SelectedTicketId", id);
        editor.commit();
        viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
    }

    @Override
    public void refreshTable(int id) {
        getUpdatedTickets(id);
    }

}