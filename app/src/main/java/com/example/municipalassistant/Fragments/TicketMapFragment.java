package com.example.municipalassistant.Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.municipalassistant.Dialog.EditTicketDialogFragment;
import com.example.municipalassistant.R;
import com.example.municipalassistant.Retrofit.Ticket;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.lang.Double.parseDouble;

public class TicketMapFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "TicketMapFragment";

    private MapView mapView;
    private GoogleMap mGoogleMap;

    private List<Ticket> lTickets;

    private ITicketMapFragment mITicketMapFragment;

    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    int nSelected;

    //SharedPreferences Listener-----------------------------
    SharedPreferences.OnSharedPreferenceChangeListener spListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if(key.equals("nSelected")){
                nSelected = sharedPref.getInt("nSelected", 0);
                setMarkers();
            }
            Log.d(TAG, "onSharedPreferenceChanged: __________"+ sharedPref.getInt("SelectedTicketId", -1));
            if(key.equals("SelectedTicketId")){
                if(sharedPref.getInt("SelectedTicketId", -1) != -1){
                setMarkers();
                editor.putInt("SelectedTicketId", -1);
                editor.commit();
            }}
//            if(key.equals("ItemChanged")){
//                if(sharedPref.getInt("ItemChanged", 0) == 1){
//                    setMarkers();
//                }
//            }
        }
    };

    @SuppressLint("CommitPrefEdits")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(
                R.layout.fragment_tickets_map, container, false);

        //SharedPreferences----------------------------------
        sharedPref = Objects.requireNonNull(getActivity()).getPreferences(Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        nSelected = sharedPref.getInt("nSelected", 0);

        //SharedPreferences Listener-----------------------------
        sharedPref.registerOnSharedPreferenceChangeListener(spListener);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mapView = view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        
        lTickets = mITicketMapFragment.getTicketsList();
        setMarkers();

        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                openDialog(Integer.parseInt(marker.getSnippet()));
                return false;
            }
        });
    }

    private void openDialog(int id) {
        int position = -1;
        for (int i = 0; i < lTickets.size(); i++) {
            if(lTickets.get(i).getId() == id){
                position = i;
            }
        }
        Bundle bundle = new Bundle();
        bundle.putInt("position", position);

        FragmentManager fragmentManager = Objects.requireNonNull(getActivity()).getSupportFragmentManager();
        EditTicketDialogFragment newFragment = new EditTicketDialogFragment();
        newFragment.setArguments(bundle);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(android.R.id.content, newFragment, "editDialog").addToBackStack(null).commit();
    }

    private void setMarkers(){
        int nSelectedId = sharedPref.getInt("SelectedTicketId", -1);
        LatLng selectedTicket = null;
        updateTickets();
        mGoogleMap.clear();
        for (Ticket ticket: lTickets) {
            int color = 360;
            switch (ticket.getStatus()) {
                case "odbačen":
                    color = 200;
                    break;
                case "otvoren":
                    color = 0;
                    break;
                case "riješen":
                    color = 120;
                    break;
            }
            LatLng marker = new LatLng(parseDouble(ticket.getLatituda()), parseDouble(ticket.getLongituda()));
            mGoogleMap.addMarker(new MarkerOptions()
                    .position(marker)
                    .snippet(String.valueOf(ticket.getId()))
                    .icon(BitmapDescriptorFactory.defaultMarker(color)));

            if(ticket.getId() == nSelectedId){
                selectedTicket = marker;
            }
        }
        if(selectedTicket != null){
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedTicket, 18));
        }
        else{
            LatLng Croatia = new LatLng(44.4737849, 16.4688717);
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Croatia, 7));
        }
    }

    private void updateTickets(){
        lTickets = mITicketMapFragment.getTicketsList();
    }


    public interface ITicketMapFragment{
        List<Ticket> getTicketsList();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mITicketMapFragment = (ITicketMapFragment) context;
    }

    @Override
    public void onPause() {
        super.onPause();
        editor.putInt("SelectedTicketId", -1);
        editor.commit();
    }
}
