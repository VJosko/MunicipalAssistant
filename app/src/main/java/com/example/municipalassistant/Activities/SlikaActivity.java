package com.example.municipalassistant.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.municipalassistant.R;

import java.io.IOException;

public class SlikaActivity extends AppCompatActivity{

    private Toolbar toolbar;
    private ActionBar actionBar;
    private ImageView imgSlika;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slika);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        //actionBar.setTitle("Tickets");
        actionBar.setDisplayHomeAsUpEnabled(true);

        String imgPath = getIntent().getStringExtra("imgPath");

        imgSlika = findViewById(R.id.img_slika);
        setImage(imgPath);
    }

    private void setImage(String imgPath) {
        Bitmap image = BitmapFactory.decodeFile(imgPath);

        Bitmap newBitmap = null;
        try {
            ExifInterface exif = new ExifInterface(imgPath);
            int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int rotationInDegrees = exifToDegrees(rotation);
            Matrix matrix = new Matrix();
            if (rotation != 0) {
                matrix.preRotate(rotationInDegrees);
            }
            newBitmap = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        imgSlika.setImageBitmap(newBitmap);
    }

    private int exifToDegrees(int rotation) {
        if (rotation == ExifInterface.ORIENTATION_ROTATE_90) { return 90; }
        else if (rotation == ExifInterface.ORIENTATION_ROTATE_180) {  return 180; }
        else if (rotation == ExifInterface.ORIENTATION_ROTATE_270) {  return 270; }
        return 0;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.delete:
                Intent resultIntent = new Intent();
                resultIntent.putExtra("delete", "delete");
                setResult(RESULT_OK, resultIntent);
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.delete_menu, menu);
        return true;
    }

}
