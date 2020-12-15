package com.example.municipalassistant.Adapters;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.municipalassistant.R;
import com.example.municipalassistant.Retrofit.Ticket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class recAdapterTickets extends RecyclerView.Adapter<recAdapterTickets.MyViewHolder>{

    List<Ticket> igre;
    List<String> addresses;
    Context context;
    private OnTicketListener mOnTicketListener;

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView id;
        public TextView opis;
        public TextView datum;
        public LinearLayout lin;
        OnTicketListener onTicketListener;


        public MyViewHolder(View v, OnTicketListener onTicketListener) {
            super(v);
            id = v.findViewById(R.id.id);
            opis = v.findViewById(R.id.opis);
            datum = v.findViewById(R.id.datum);
            lin = v.findViewById(R.id.lin);
            this.onTicketListener = onTicketListener;

            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onTicketListener.onTicketClick(getAdapterPosition());
        }
    }

    public interface OnTicketListener{
        void onTicketClick(int position);
    }


    public recAdapterTickets(List<Ticket> igra, List<String> address, OnTicketListener onTicketListener, Context contex){
        igre = igra;
        addresses = address;
        context = contex;
        this.mOnTicketListener = onTicketListener;
    }



    @NonNull
    @Override
    public recAdapterTickets.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rec_ticketi, parent, false);
        MyViewHolder vh = new MyViewHolder(v, mOnTicketListener);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull recAdapterTickets.MyViewHolder holder, int position) {
        List<String> adress = getAdresses();
        Log.i("Status", igre.get(position).getStatus());
        if(igre.get(position).getStatus().equals("odbačen")){
            holder.lin.setBackgroundColor(0xbb00b2ca);
        }
        else if(igre.get(position).getStatus().equals("otvoren")){
            holder.lin.setBackgroundColor(0xbbfa4441);
        }
        else if(igre.get(position).getStatus().equals("riješen")){
            holder.lin.setBackgroundColor(0xbb04e762);
        }
        holder.opis.setText(adress.get(position));
        holder.datum.setText(igre.get(position).getDatum_prijave());
    }

    @Override
    public int getItemCount() {
        return igre.size();
    }

    private List<String> getAdresses(){
        String address = "";

        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses = null;
        List<String> sAddresses = null;
        sAddresses = new ArrayList<>();

        for(Ticket ticket: igre){
            try {
                addresses = geocoder.getFromLocation(Double.parseDouble(ticket.getLatituda()),Double.parseDouble(ticket.getLongituda()), 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(addresses != null) {
                address = addresses.get(0).getAddressLine(0);
                String[] addressParts = address.split(",");
                address = addressParts[0] + ", " + addressParts[2];
                sAddresses.add(address);
            }
        }

        return sAddresses;
    }

}
