package com.example.municipalassistant.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.municipalassistant.R;
import com.example.municipalassistant.Retrofit.JsonPlaceHolderApi;
import com.example.municipalassistant.Retrofit.Ticket;
import com.example.municipalassistant.Retrofit.TicketPost;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UpisActivity extends AppCompatActivity{

    private TextInputLayout txtOpis;
    private ImageView imgSlika;
    private ImageButton ibtnKamera;
    private Button btnPrijavi;
    private ProgressBar pb;

    private JsonPlaceHolderApi jsonPlaceHolderApi;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;
    String currentPhotoPath = "";
    byte[] byteArray = null;
    String photoUrl = "";

    private Toolbar toolbar;
    private ActionBar actionBar;

    private static  final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static  final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private Boolean mLocationPermissionsGranted = false;
    String LATITUDE = "";
    String LONGITUDE = "";

    String sOpis = "";
    int nId;

    private FusedLocationProviderClient mFusedLocationProviderClient;

    //Firebase Storage
    private FirebaseStorage storage = FirebaseStorage.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upis);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        //actionBar.setTitle("Tickets");
        actionBar.setDisplayHomeAsUpEnabled(true);

        txtOpis = findViewById(R.id.txt_opis);
        imgSlika = findViewById(R.id.img_slika);
        ibtnKamera = findViewById(R.id.ibtn_kamera);
        ibtnKamera.setBackgroundColor(0xff00e676);
        ibtnKamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
        imgSlika.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentPhotoPath != ""){
                    Intent intent = new Intent(UpisActivity.this, SlikaActivity.class);
                    intent.putExtra("imgPath", currentPhotoPath);
                    startActivityForResult(intent, 2);
                }
            }
        });

        pb = findViewById(R.id.pb);
        btnPrijavi = findViewById(R.id.btn_prijavi);
        btnPrijavi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnPrijavi.setEnabled(false);
                btnPrijavi.setVisibility(View.INVISIBLE);
                pb.setVisibility(View.VISIBLE);
                sOpis = txtOpis.getEditText().getText().toString();

                if (byteArray != null){
                    photoUpload();
                }
                else if(!sOpis.equals("")){
                    getTicketId();
                }
                else{
                    btnPrijavi.setEnabled(true);
                    btnPrijavi.setVisibility(View.VISIBLE);
                    pb.setVisibility(View.INVISIBLE);
                    Toast.makeText(UpisActivity.this, "Nedostaje opis ili slika", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://student.vsmti.hr/jvudrag/PIS/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        jsonPlaceHolderApi = retrofit.create(JsonPlaceHolderApi.class);

    }

    private void createTicket() {
        String date = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        int nDate = Integer.parseInt(date);

        TicketPost ticketPost = new TicketPost(nId,sOpis,photoUrl,1,nDate,LONGITUDE,LATITUDE);
        Call<ResponseBody> call = jsonPlaceHolderApi.createTicket(ticketPost);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(!response.isSuccessful()){
                    return;
                }
                Toast.makeText(UpisActivity.this, "Problem prijavljen", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                btnPrijavi.setEnabled(true);
                btnPrijavi.setVisibility(View.VISIBLE);
                pb.setVisibility(View.INVISIBLE);
                Toast.makeText(UpisActivity.this, "Došlo je do pogreške", Toast.LENGTH_SHORT).show();
            }
        });
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

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            //Bundle extras = data.getExtras();
            Bitmap imageBitmap = BitmapFactory.decodeFile(currentPhotoPath);
            setImage(imageBitmap);
        }
        else if(resultCode == RESULT_OK && requestCode == 2) {
            deleteImg();
        }
    }

    private void setImage(Bitmap image) {
        Bitmap newBitmap = null;
        try {
            ExifInterface exif = new ExifInterface(currentPhotoPath);
            int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int rotationInDegrees = exifToDegrees(rotation);
            Matrix matrix = new Matrix();
            if (rotation != 0) {
                matrix.preRotate(rotationInDegrees);
            }
            newBitmap = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            newBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byteArray = stream.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();
        }
        imgSlika.setImageBitmap(newBitmap);
        imgSlika.setBackgroundColor(Color.WHITE);
    }

    private int exifToDegrees(int rotation) {
        if (rotation == ExifInterface.ORIENTATION_ROTATE_90) { return 90; }
        else if (rotation == ExifInterface.ORIENTATION_ROTATE_180) {  return 180; }
        else if (rotation == ExifInterface.ORIENTATION_ROTATE_270) {  return 270; }
        return 0;
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String imageFileName = UUID.randomUUID().toString();
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();

        return image;
    }

    public void deleteImg() {
        currentPhotoPath = "";
        imgSlika.setImageResource(R.drawable.ic_outline_panorama_24);

        imgSlika.setBackgroundColor(Color.parseColor("#2f000000"));
    }

    private void getLocationPermission(){
        String[] permission = {Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionsGranted = true;
                getDeviceLocation();
            }
            else{
                ActivityCompat.requestPermissions(this, permission,LOCATION_PERMISSION_REQUEST_CODE);
            }
        }
        else{
            ActivityCompat.requestPermissions(this, permission,LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionsGranted = false;

        switch (requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length > 0){
                    for(int i = 0; i < grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionsGranted = false;
                            return;
                        }
                    }
                    mLocationPermissionsGranted = true;
                    getDeviceLocation();
                }
            }
        }
    }

    private void getDeviceLocation(){

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if(mLocationPermissionsGranted){
                Task<Location> location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if(task.isSuccessful()){
                            Location currentLocation = (Location) task.getResult();
                            double latitude = currentLocation.getLatitude();
                            double longitude = currentLocation.getLongitude();
                            LATITUDE = String.valueOf(latitude);
                            LONGITUDE = String.valueOf(longitude);
                            Log.d("latitude", String.valueOf(LATITUDE));
                            Log.d("longitude", String.valueOf(LONGITUDE));
                            createTicket();
                        }
                        else {
                            Toast.makeText(UpisActivity.this, "nemogu dohvatiti lokaciju", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }catch (SecurityException e){
        }
    }

    private void getTicketId(){
        Call<List<Ticket>> call = jsonPlaceHolderApi.getTickets();

        call.enqueue(new Callback<List<Ticket>>() {
            @Override
            public void onResponse(Call<List<Ticket>> call, Response<List<Ticket>> response) {

                if(!response.isSuccessful()){
                    return;
                }
                List<Ticket> tickets = response.body();
                nId = tickets.size() + 1;
                getLocationPermission();
            }

            @Override
            public void onFailure(Call<List<Ticket>> call, Throwable t) {
            }
        });
    }

    private void photoUpload(){
        String path = "prijavljenjeSlike/" + UUID.randomUUID() + ".png";
        final StorageReference prijavljenjeSlikeRef = storage.getReference(path);
        UploadTask uploadTask = prijavljenjeSlikeRef.putBytes((byteArray));

        uploadTask.addOnCompleteListener(UpisActivity.this, new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
            }
        });

        Task<Uri> getDownloadUriTask = uploadTask.continueWithTask(
                new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if(!task.isSuccessful()){
                            throw task.getException();
                        }
                        return prijavljenjeSlikeRef.getDownloadUrl();
                    }
                }
        );

        getDownloadUriTask.addOnCompleteListener(UpisActivity.this, new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if(task.isSuccessful()){
                    Uri downloadUri = task.getResult();
                    Log.d("URL za sliku", String.valueOf(downloadUri));
                    photoUrl = String.valueOf(downloadUri);
                    getTicketId();
                }
            }
        });
    }

}
