package com.example.letschat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextThemeWrapper;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.letschat.Contacts.PERMISSIONS_REQUEST_READ_CONTACTS;

public class SplashScreen extends AppCompatActivity {

    CircleImageView imageView;
    Boolean permissionGranted;


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    handler();
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(SplashScreen.this, "Permission denied to read your Contact", Toast.LENGTH_SHORT).show();
                    finish();
                    System.exit(0);
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        imageView = findViewById(R.id.imageView);

        ActivityCompat.requestPermissions(SplashScreen.this,
                new String[]{Manifest.permission.READ_CONTACTS},
                1);

        }


//    public Boolean requestContactPermission() {
//        Boolean permissionGranted = false;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
//                if (ActivityCompat.shouldShowRequestPermissionRationale(SplashScreen.this,
//                        android.Manifest.permission.READ_CONTACTS)) {
//
//                    AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AppTheme));
//                    builder.setTitle("Read Contacts permission");
//                    builder.setPositiveButton(android.R.string.ok, null);
//                    builder.setMessage("Please enable access to contacts.");
//                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
//                        @TargetApi(Build.VERSION_CODES.M)
//                        @Override
//                        public void onDismiss(DialogInterface dialog) {
//                            requestPermissions(
//                                    new String[]
//                                            {android.Manifest.permission.READ_CONTACTS}
//                                    , PERMISSIONS_REQUEST_READ_CONTACTS);
//                        }
//                    });
//                    builder.show();
//                } else {
//                    ActivityCompat.requestPermissions(SplashScreen.this,
//                            new String[]{android.Manifest.permission.READ_CONTACTS},
//                            PERMISSIONS_REQUEST_READ_CONTACTS);
//                }
//            } else {
//                permissionGranted = true;
//                handler();
//            }
//        } else {
//            permissionGranted = true;
//            handler();
//        }
//
//        return permissionGranted;
//    }


    public void handler() {

        final String transitionName = "shared";
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(SplashScreen.this, phone.class);
                    ActivityOptionsCompat options =
                            ActivityOptionsCompat.makeSceneTransitionAnimation(
                                    SplashScreen.this, imageView, transitionName);
                    startActivity(intent, options.toBundle());
                    finish();
                }
            }
        };

        new Handler().postDelayed(runnable, 1000);
    }

}