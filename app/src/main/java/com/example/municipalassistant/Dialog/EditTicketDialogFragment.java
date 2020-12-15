package com.example.municipalassistant.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

import com.example.municipalassistant.Activities.UpisActivity;
import com.example.municipalassistant.Fragments.TicketListFragment;
import com.example.municipalassistant.R;
import com.example.municipalassistant.Retrofit.JsonPlaceHolderApi;
import com.example.municipalassistant.Retrofit.Ticket;
import com.example.municipalassistant.Retrofit.TicketUpdate;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EditTicketDialogFragment extends DialogFragment {

    private static final String TAG = "EditTicketDialogFragmen";

    private IEditTicket mIEditTicket;
    private ImageButton btnClose;
    private Button btnSave, btnMap;
    private ImageView imgOpis;

    private LinearLayout lin;
    private ConstraintLayout conOpis, conCoords, conDatumPrijave, conStatus, conAddress;
    private TextView tvOpis, tvCoords, tvDatumPrijave, tvAddress;
    private MaterialButtonToggleGroup tgStatus;
    private Button btnOtvoreno, btnRijeseno, btnOdbaceno;
    public TextInputLayout txtKomentar;

    String sStatus;
    public Ticket ticket;
    private JsonPlaceHolderApi jsonPlaceHolderApi;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_edit_ticket, container, false);

        Bundle extras = getArguments();
        ticket = mIEditTicket.getSelectedTicket(extras.getInt("position"));
        sStatus = ticket.getStatus();

        btnClose = view.findViewById(R.id.btn_close);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!txtKomentar.getEditText().getText().toString().equals(ticket.getKomentar()) || !ticket.getStatus().equals(sStatus)){
                    showAlertDialog();
                }
                else{
                    dismiss();
                }
            }
        });

        btnSave = view.findViewById(R.id.btn_save);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txt = txtKomentar.getEditText().getText().toString();
                if(!ticket.getKomentar().equals(txt) || !ticket.getStatus().equals(sStatus)){
                    updateTicket();
                }
            }
        });

        btnMap = view.findViewById(R.id.btn_map);
        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIEditTicket.goToMapFragment(ticket.getId());
                dismiss();
            }
        });

        imgOpis = view.findViewById(R.id.img_opis);
        UrlImageViewHelper.setUrlDrawable(imgOpis, ticket.getSlika());

        lin = view.findViewById(R.id.lin);
        conOpis = view.findViewById(R.id.con_opis);
        tvOpis = view.findViewById(R.id.tv_opis);
        if(!ticket.getOpis().equals("")) {
            tvOpis.setText(ticket.getOpis());
        }
        else{
            lin.removeView(conOpis);
            //conOpis.removeView(conOpis);
            //conOpis.setVisibility(View.INVISIBLE);
        }

        conCoords = view.findViewById(R.id.con_coords);
        tvCoords = view.findViewById(R.id.tv_coords);
        tvCoords.setText(ticket.getLatituda() + ", " + ticket.getLongituda());

        tvAddress = view.findViewById(R.id.tv_address);
        tvAddress.setText(getAddress());

        conDatumPrijave = view.findViewById(R.id.con_datum_prijave);
        tvDatumPrijave = view.findViewById(R.id.tv_datum_prijave);
        tvDatumPrijave.setText(ticket.getDatum_prijave());

        tgStatus = view.findViewById(R.id.tg_status);
        btnOtvoreno = view.findViewById(R.id.btn_otvoreno);
        btnRijeseno = view.findViewById(R.id.btn_rijeseno);
        btnOdbaceno = view.findViewById(R.id.btn_odbaceno);
        setCheckedButton();
        tgStatus.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                if(group.getCheckedButtonId() == -1){
                    group.check(checkedId);
                }
                if(tgStatus.getCheckedButtonId() == btnOtvoreno.getId()){
                    sStatus = "otvoren";
                }
                else if(tgStatus.getCheckedButtonId() == btnRijeseno.getId()){
                    sStatus = "riješen";
                }
                else if(tgStatus.getCheckedButtonId() == btnOdbaceno.getId()){
                    sStatus = "odbačen";
                }
                Log.d(TAG, "onButtonChecked: ____________" + sStatus);
            }
        });

        txtKomentar = view.findViewById(R.id.txt_komentar);
        txtKomentar.getEditText().setText(ticket.getKomentar());

        //Retrofit
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

        return view;
    }


    private void updateTicket(){
        String date = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        int statusId = getStatusId();

        TicketUpdate ticketUpdate = new TicketUpdate(ticket.getId(), txtKomentar.getEditText().getText().toString(),statusId,date);
        Call<ResponseBody> call = jsonPlaceHolderApi.updateStatus(ticketUpdate);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(!response.isSuccessful()){
                    return;
                }
                mIEditTicket.refreshTable(ticket.getId());
                dismiss();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "onFailure: ");
            }
        });
    }

    private int getStatusId(){
        int id = 1;
        switch (sStatus){
            case "otvoren":
                id = 1;
                break;
            case "riješen":
                id = 2;
                break;
            case "odbačen":
                id = 3;
                break;
        }
        return id;
    }

    private void showAlertDialog(){
         new MaterialAlertDialogBuilder(getContext())
                 .setTitle("Odbaci promjene?")
                 .setNegativeButton("natrag", new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                     }
                 })
                 .setPositiveButton("Odbaci", new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                     }
                 })
                .show();
    }

    private String getAddress() {
        String address = "";
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(Double.parseDouble(ticket.getLatituda()),
                    Double.parseDouble(ticket.getLongituda()), 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(addresses != null) {
            address = addresses.get(0).getAddressLine(0);
            String[] addressParts = address.split(",");
            address = addressParts[0] + ", " + addressParts[1] + ", " + addressParts[2];
        }
        return address;
    }

    private void setCheckedButton() {
        switch (ticket.getStatus()){
            case "otvoren":
                tgStatus.check(btnOtvoreno.getId());
                break;
            case "riješen":
                tgStatus.check(btnRijeseno.getId());
                break;
            case "odbačen":
                tgStatus.check(btnOdbaceno.getId());
                break;
        }
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    public interface IEditTicket{
        Ticket getSelectedTicket(int position);
        void goToMapFragment(int id);
        void refreshTable(int id);
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mIEditTicket = (IEditTicket) getActivity();
    }
}