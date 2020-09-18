package com.example.letschat;

import android.content.Intent;
import android.content.pm.Signature;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.MenuItem;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.Menu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.annotation.GlideExtension;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.engine.executor.GlideExecutor;
import com.bumptech.glide.module.GlideModule;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.MediaStoreSignature;
import com.bumptech.glide.signature.ObjectKey;
import com.example.letschat.model.Adapter;
import com.example.letschat.ui.home.HomeFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    Adapter adapter;
    RecyclerView recyclerView;
    private static final float END_SCALE = 0.7f;
    View contentView;
    TextView showUsername,showUserStatus;
    CircleImageView drawerImage;
    FirebaseUser user;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    StorageReference imageReference;
    DatabaseReference databaseReference;
    ProgressBar progressBarImage;
    Long time = 0L;
    FloatingActionButton fab;
    userDetails userInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);


        contentView = findViewById(R.id.contentView);
        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        drawerImage = headerView.findViewById(R.id.drawerImage);
        showUsername = headerView.findViewById(R.id.showUsername);
        showUserStatus = headerView.findViewById(R.id.showUserStatus);
        progressBarImage = headerView.findViewById(R.id.progressBarImage);
        fab = findViewById(R.id.fab);


        user = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getPhoneNumber());
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        imageReference = storageReference.child("UsersProfileImages/"+user.getPhoneNumber());



        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userInfo = snapshot.getValue(userDetails.class);
                showUsername.setText(userInfo.getUsername());
                showUserStatus.setText(userInfo.getPhoneNumber());
                Log.d("check",userInfo.getUsername());
                String imageStatus = userInfo.getImageUrl();
                if(!imageStatus.equals("default")){
                    time = Long.valueOf(userInfo.getImageUrl());
                }


                if(!imageStatus.equals("default")) {
                    Glide.with(getApplicationContext() /* context */)
                            .load(imageReference)
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    progressBarImage.setVisibility(View.GONE);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    progressBarImage.setVisibility(View.GONE);
                                    return false;
                                }
                            })
                            .signature(new MediaStoreSignature("image/jpeg", time, getApplicationContext().getResources().getConfiguration().orientation))
                            .into(drawerImage);
                }
                else{
                    drawerImage.setImageResource(R.drawable.download);
                    progressBarImage.setVisibility(View.GONE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("check", error.getMessage());
            }
        });

        setSupportActionBar(toolbar);



        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        drawer.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                // labelView.setVisibility(slideOffset > 0 ? View.VISIBLE : View.GONE);

                // Scale the View based on current slide offset
                final float diffScaledOffset = slideOffset * (1.2f - END_SCALE);
                final float offsetScale = 1 - diffScaledOffset;
                contentView.setScaleX(offsetScale);
                contentView.setScaleY(offsetScale);

                // Translate the View, accounting for the scaled width
                final float xOffset = drawerView.getWidth() * slideOffset;
                final float xOffsetDiff = contentView.getWidth() * diffScaledOffset / 2;
                final float xTranslation = xOffset - xOffsetDiff;
                contentView.setTranslationX(xTranslation);
            }


        });


//        if (intent.getData() != null) {
//            File imageToShow = (File) intent.getExtras().get("image");
//            Bitmap bitmap = BitmapFactory.decodeFile(imageToShow.getAbsolutePath());
//            drawerImage.setImageBitmap(bitmap);
//        }


        drawerImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phone = userInfo.getPhoneNumber();
                Intent intent = new Intent(MainActivity.this, imageViewer.class);
                intent.putExtra("phone", phone);
                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.action_settings){
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(MainActivity.this, phone.class);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }


}