package com.tutsplus.tesstwoimport;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;

import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class MainActivity extends AppCompatActivity {

    private static String logtag = "CameraTest";
    private static int TAKE_PICTURE = 1;
    protected ImageView mImageView;
    private TextView mTextView;
    private Button cameraButton;
    private View.OnClickListener cameraListener = new View.OnClickListener() {
        public void onClick(View v) {
            takePhoto(v);
        }
    };

    // Path of trained data for tesseract OCR
    // Can also set language here, jpn.traineddata, etc.
    private String datapath = "";
    private String TESSDATA = "tessdata/eng.traineddata";
    private TessBaseAPI mTess;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set variables to respective views
        cameraButton = (Button) findViewById(R.id.btn_camera);
        cameraButton.setOnClickListener(cameraListener);

        mImageView = (ImageView)findViewById(R.id.img_camera);
        mTextView = (TextView) findViewById(R.id.textView);


        // Initiate Tesseract language trained data
        datapath = getFilesDir() + "/tesseract/";

        // Make sure trained data is copied
        checkFile(new File(datapath + "tessdata/"));

        // Initialize Tesseract API
        String language = "eng";

        mTess = new TessBaseAPI();
        mTess.init(datapath, language);

    }

    private void takePhoto(View v) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, TAKE_PICTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == TAKE_PICTURE && resultCode == RESULT_OK) {
            Bundle extras = intent.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            mImageView.setImageBitmap(imageBitmap);


        } else {
            Toast.makeText(getApplicationContext(), "Error getting image", Toast.LENGTH_LONG).show();
        }
    }

    // Special handling is needed to open a file from the assets folder.
    // It must be copied from external storage first, and then uncompressed into
    // a resources folder.
    // Used in: checkFile()
    // Code used from:
    // http://imperialsoup.com/2016/04/29/simple-ocr-android-app-using-tesseract-tutorial/
    private void copyFiles() {
        try {
            // Location of trained data, based on the assets folder
            String filepath = datapath + "/" + TESSDATA;

            // Use the AssetManager class to access assets
            AssetManager assetManager = getAssets();

            // Use streams for reading the trained data
            InputStream instream = assetManager.open(TESSDATA);
            OutputStream outstream = new FileOutputStream(filepath);

            // Copy the file
            byte[] buffer = new byte[1024];
            int read;
            while ((read = instream.read(buffer)) != -1) {
                outstream.write(buffer, 0, read);
            }
            outstream.flush();
            outstream.close();
            instream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // checkFile() is used to check if the directory exists
    // before even doing the copyFiles.
    // It's basically a wrapper for copyFiles() so that it doesn't
    // hit any snags.
    // Used in: onCreate()
    private void checkFile(File dir) {
        // case: directory not existing
        // do: create new directory
        // Notes: It can also report back failed directory creation
        if (!dir.exists() && dir.mkdirs()) {
            copyFiles();
        }

        // case: directory exists, no file inside
        if (dir.exists()) {
            String datafilepath = datapath+"/" + TESSDATA;
            File datafile = new File(datafilepath);
            if (!datafile.exists()) {
                copyFiles();
            }
        }
    }
}


