package com.example.municipalassistant.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.municipalassistant.Dialog.EditTicketDialogFragment;
import com.example.municipalassistant.R;
import com.example.municipalassistant.Retrofit.Ticket;
import com.example.municipalassistant.Adapters.recAdapterTickets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TicketListFragment extends Fragment implements recAdapterTickets.OnTicketListener {

    private static final String TAG = "TicketListFragment-----";

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private List<Ticket> lTickets;
    private List<String> lAddresses;

    private ITicketListFragment mITicketListFragment;

    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    int nSelected;

    //SharedPreferences Listener-----------------------------
    SharedPreferences.OnSharedPreferenceChangeListener spListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if(key.equals("nSelected")){
                updateTickets();
            }
            if(key.equals("ItemChanged") && sharedPref.getInt("ItemChanged", 0) == 1){
                updateTickets();
                editor.putInt("ItemChanged", 0);
                editor.commit();
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(
                R.layout.fragment_tickets_list, container, false);

        //SharedPreferences----------------------------------
        sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        nSelected = sharedPref.getInt("nSelected", 0);

        //SharedPreferences Listener-----------------------------
        sharedPref.registerOnSharedPreferenceChangeListener(spListener);
//

        //------------Recycler-view------------------
        lTickets = mITicketListFragment.getTicketsList();
        lAddresses = getAdresses();
        recyclerView = view.findViewById(R.id.rec);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new recAdapterTickets(lTickets, lAddresses, this, getContext());
        recyclerView.setAdapter(mAdapter);


        return view;
    }

    private void updateTickets(){
        lTickets = mITicketListFragment.getTicketsList();
        lAddresses = getAdresses();
        mAdapter.notifyDataSetChanged();
    }


    private List<String> getAdresses(){
        String address = "";

        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        List<Address> addresses = null;
        List<String> sAddresses = null;
        sAddresses = new ArrayList<>();

        for(Ticket ticket: lTickets){
            try {
                addresses = geocoder.getFromLocation(Double.parseDouble(ticket.getLatituda()),Double.parseDouble(ticket.getLongituda()), 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(addresses != null) {
                address = addresses.get(0).getAddressLine(0);
                Log.d(TAG, "getAddress: ____________" + address);
                String[] addressParts = address.split(",");
                address = addressParts[0] + ", " + addressParts[2];
                Log.d(TAG, "getAddress: ____________" + address);
                sAddresses.add(address);
            }
        }

        return sAddresses;
    }




    @Override
    public void onTicketClick(int position) {

        Bundle bundle = new Bundle();
        bundle.putInt("position", position);

        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        EditTicketDialogFragment newFragment = new EditTicketDialogFragment();
        newFragment.setArguments(bundle);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(android.R.id.content, newFragment, "editDialog").addToBackStack(null).commit();

        EditTicketDialogFragment editTicketDialogFragment = (EditTicketDialogFragment) getActivity().getSupportFragmentManager().findFragmentByTag("editDialog");
    }

    public interface ITicketListFragment{
        List<Ticket> getTicketsList();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mITicketListFragment = (ITicketListFragment) context;
    }

}
