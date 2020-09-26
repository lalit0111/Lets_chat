package com.example.letschat.ui.slideshow;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.MediaStoreSignature;
import com.example.letschat.MainActivity;
import com.example.letschat.R;
import com.example.letschat.imageViewer;
import com.example.letschat.phone;
import com.example.letschat.userDetails;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;
import static androidx.core.content.ContextCompat.getSystemService;
import static androidx.core.content.ContextCompat.getSystemServiceName;

public class SlideshowFragment extends Fragment {

    private SlideshowViewModel slideshowViewModel;
    Boolean imageSelectedForUpload;
    Uri filePath;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference, imageReference;
    Long time = 0L;
    userDetails userInfo;
    MaterialButton uploadButton;
    CircleImageView userImage;
    FirebaseUser firebaseUser;
    String phone;
    ImageButton editName, editStatus;
    Boolean nameEdit, statusEdit;
    EditText myName, myStatus,myPhone;
    String Name,Status;
    int count = 0;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        slideshowViewModel =
                ViewModelProviders.of(this).get(SlideshowViewModel.class);
        final View root = inflater.inflate(R.layout.fragment_slideshow, container, false);

//        TextView toolbarText = getActivity().findViewById(R.id.title_text);
//        toolbarText.setText("Settings");

        uploadButton = root.findViewById(R.id.uploadImageButton2);
        userImage = root.findViewById(R.id.userImage2);
        editName = root.findViewById(R.id.nameEdit);
        editStatus = root.findViewById(R.id.statusEdit);
        myName = root.findViewById(R.id.myName);
        myStatus = root.findViewById(R.id.myStatus);
        myPhone=root.findViewById(R.id.senderPhone);
        nameEdit = false;
        statusEdit = false;
        myStatus.setEnabled(false);
        myStatus.setClickable(false);
        myName.setEnabled(false);
        myName.setClickable(false);
        myPhone.setEnabled(false);
        myPhone.setClickable(false);


        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        phone = firebaseUser.getPhoneNumber();
        imageSelectedForUpload = false;


        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference().child("Users").child(phone);

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                imageReference = storageReference.child("UsersProfileImages/" + phone);
                userInfo = snapshot.getValue(userDetails.class);
                myName.setText(userInfo.getUsername());
                myStatus.setText(userInfo.getStatus());
                if (!userInfo.getImageUrl().equals("default")) {
                    time = Long.valueOf(userInfo.getImageUrl());
                }
                if (imageReference != null && !userInfo.getImageUrl().equals("default")) {
                    Glide.with(root.getContext() /* context */)
                            .load(imageReference)
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    return false;
                                }
                            })
                            .signature(new MediaStoreSignature("image/jpeg", time, root.getContext().getResources().getConfiguration().orientation))
                            .into(userImage);

                } else {
                    userImage.setImageResource(R.drawable.download);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });


        editName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (nameEdit) {
                    nameEdit = false;
                    Drawable drawable = getResources().getDrawable(R.drawable.ic_baseline_edit_24);
                    editName.setImageDrawable(drawable);
                    Log.d("check3", "1");
                    myName.clearFocus();
                    closeKeyboard();
                    Name=myName.getText().toString();
                    UpdateDatabase(Name,phone,userInfo.getImageUrl(),userInfo.getStatus());
                    myName.setEnabled(false);
                    myName.setClickable(false);

                } else {
                    nameEdit = true;
                    Drawable drawable = getResources().getDrawable(R.drawable.ic_baseline_check_24);
                    editName.setImageDrawable(drawable);
                    myName.setEnabled(true);
                    myName.setClickable(true);
                    myName.requestFocus();
                    myName.setSelection(myName.getText().toString().length());
                    showKeyboard();
                    Log.d("check3", "2");
                }
            }
        });


        editStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (statusEdit) {
                    statusEdit = false;
                    Drawable drawable = getResources().getDrawable(R.drawable.ic_baseline_edit_24);
                    editStatus.setImageDrawable(drawable);
                    Log.d("check3", "1");
                    myStatus.clearFocus();
                    closeKeyboard();
                    Status = myStatus.getText().toString();
                    UpdateDatabase(userInfo.getUsername(),phone,userInfo.getImageUrl(),Status);
                    myStatus.setEnabled(false);
                    myStatus.setClickable(false);

                } else {
                    statusEdit = true;
                    Drawable drawable = getResources().getDrawable(R.drawable.ic_baseline_check_24);
                    editStatus.setImageDrawable(drawable);
                    myStatus.setEnabled(true);
                    myStatus.setClickable(true);
                    myStatus.requestFocus();
                    myStatus.setSelection(myStatus.getText().toString().length());
                    showKeyboard();
                    Log.d("check3", "2");
                }
            }
        });


        return root;
    }

    public void chooseImage() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), 2);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 2 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            imageSelectedForUpload = true;


                imageReference.putFile(filePath)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                UpdateDatabase(userInfo.getUsername(), phone, String.valueOf(System.currentTimeMillis()),userInfo.getStatus());
                                Toast.makeText(getContext(), "Image Uploaded", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                int currentProgress = (int) progress;
                            }
                        });

        }
    }

    public void showKeyboard(){
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public void closeKeyboard(){
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    private void UpdateDatabase(String name, String phone, String imageUrl,String status) {

        userDetails User = new userDetails(name, phone, imageUrl,status);
        databaseReference.setValue(User);
    }
}