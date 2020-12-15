package com.example.municipalassistant.Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import android.util.Log;

import com.example.municipalassistant.Fragments.TicketListFragment;
import com.example.municipalassistant.Fragments.TicketMapFragment;

import java.util.ArrayList;


public class TicketsPagerAdapter extends FragmentStateAdapter {
    public TicketsPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }



    @NonNull
    @Override
    public Fragment createFragment(int position) {
        ArrayList<Fragment> fragments = new ArrayList<>();
        fragments.add(new TicketListFragment());
        fragments.add(new TicketMapFragment());
        switch (position){
            case 0:
                return new TicketListFragment();
            case 1:
                return new TicketMapFragment();
        }
        Log.d("FRAGMENT", String.valueOf(position));
        return new TicketListFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}