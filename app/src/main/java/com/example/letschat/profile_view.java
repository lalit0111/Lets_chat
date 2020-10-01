package com.example.letschat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.MediaStoreSignature;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import de.hdodenhof.circleimageview.CircleImageView;


public class profile_view extends AppCompatActivity {

    ImageView profilePic;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    StorageReference imageReference;
    Long time = 0L;
    userDetails userInfo;
    String phoneOfReciever2;
    EditText status;
    TextView phone;
    String username;
    CircleImageView editStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final CollapsingToolbarLayout toolBarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);



        profilePic = findViewById(R.id.profilePicScroll);
        status = findViewById(R.id.status);
//        editStatus = findViewById(R.id.editStatus);
        phone = findViewById(R.id.phoneOfReceiver3);


        Intent intent = getIntent();
        username = intent.getStringExtra("contact");
        phoneOfReciever2=intent.getStringExtra("phone2");

        Log.e("check",phoneOfReciever2);

        phone.setText(phoneOfReciever2.toString());

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference().child("Users").child(phoneOfReciever2);

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                imageReference = storageReference.child("UsersProfileImages/" + phoneOfReciever2);
                userInfo = snapshot.getValue(userDetails.class);
                toolBarLayout.setExpandedTitleTypeface(Typeface.DEFAULT_BOLD);
                toolBarLayout.setTitle(username);
                assert userInfo != null;
                if (!userInfo.getImageUrl().equals("default")) {
                    time = Long.valueOf(userInfo.getImageUrl());
                }
                if (imageReference != null && !userInfo.getImageUrl().equals("default")) {
                    Glide.with(getApplicationContext() /* context */)
                            .load(imageReference)
                            .fitCenter()
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    profilePic.setImageResource(R.drawable.dp_default);
                                    Toast.makeText(profile_view.this, "Unable to load", Toast.LENGTH_SHORT).show();
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    return false;
                                }
                            })
                            .signature(new MediaStoreSignature("image/jpeg", time, getApplicationContext().getResources().getConfiguration().orientation))
                            .into(profilePic);

                } else {
                    profilePic.setImageResource(R.drawable.dp_default);
                    toolbar.setTitleTextColor(ContextCompat.getColor(getApplicationContext(),R.color.Black));
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("check",error.getMessage());
            }
        });




        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.saveStatus);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent2 = new Intent(profile_view.this, imageViewer.class);
                intent2.putExtra("phone",phoneOfReciever2);
                startActivity(intent2);
            }
        });

//        editStatus.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                status.requestFocus();
//                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
//                imm.showSoftInput(status, InputMethodManager.SHOW_IMPLICIT);
//                status.setSelection(status.getText().toString().length());
//            }
//        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return  true;
    }
}