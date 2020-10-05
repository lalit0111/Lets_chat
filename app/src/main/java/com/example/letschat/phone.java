package com.example.letschat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mukesh.OtpView;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

public class phone extends AppCompatActivity {


    MaterialButton verify;
    CardView otpCard;
    CardView signupCard;
    MaterialTextView setupText;
    MaterialButton signupButton;
    Boolean onScreenOne = true;
    TextInputEditText phone, username;
    CircleImageView userImage;
    MaterialButton uploadImageButton;
    String phoneNumber, Username;
    String codeBySystem;
    OtpView pinview;
    ProgressBar progressBar;
    DatabaseReference databaseReference;
    Uri filePath;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    public static Boolean imageSelectedForUpload = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone);

        verify = findViewById(R.id.verify);
        otpCard = findViewById(R.id.otpCard);
        signupCard = findViewById(R.id.signupCard);
        signupButton = findViewById(R.id.signupButton);
        phone = findViewById(R.id.phone);
        username = findViewById(R.id.username);
        pinview = findViewById(R.id.otp_view);
        progressBar = findViewById(R.id.progress_circular);
        uploadImageButton = findViewById(R.id.uploadImageButton);
        userImage = findViewById(R.id.userImage);
        setupText = findViewById(R.id.setupText);

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference("UsersProfileImages");
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");


        progressBar.setVisibility(View.GONE);
        setupText.setVisibility(View.GONE);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (phone.getText().toString().length() == 10 && !username.getText().toString().isEmpty()) {
                    phoneNumber = "+91" + phone.getText().toString();
                    Username = username.getText().toString();

                    sendVerificationCodeToUser(phoneNumber);

                    final Animation aniSlide = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);
                    signupCard.startAnimation(aniSlide);
                    aniSlide.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            signupCard.setTranslationY(2000);
                            onScreenOne = false;
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                } else {
                    Toast.makeText(phone.this, "Enter Valid Phone Number And Username", Toast.LENGTH_SHORT).show();
                }

            }
        });


        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                String code = pinview.getText().toString();

                if (code.length() != 6) {
                    Toast.makeText(phone.this, "Wrong OTP", Toast.LENGTH_SHORT).show();
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    verifyCode(code);
                }
            }
        });


        uploadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });


    }


    private void sendVerificationCodeToUser(String phoneNumber) {

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            codeBySystem = s;
            Toast.makeText(phone.this, "Code Sent", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            String code = "123456";
//             String code = phoneAuthCredential.getSmsCode();
            pinview.setText(code);
            if (code != null) {
                setupText.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
//                verifyCode(code);
                signInUserByCredential(phoneAuthCredential);
            }
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Toast.makeText(phone.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.INVISIBLE);
            setupText.setVisibility(View.INVISIBLE);
            pinview.setText("");
        }
    };

    private void verifyCode(String codeByUser) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codeBySystem, codeByUser);
        signInUserByCredential(credential);
    }

    private void signInUserByCredential(PhoneAuthCredential credential) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(phone.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {

                    if (imageSelectedForUpload) {
                        if (filePath != null) {

                            storageReference.child(phoneNumber).putFile(filePath)
                                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            progressBar.setVisibility(View.INVISIBLE);
                                            setupText.setVisibility(View.INVISIBLE);
                                            Toast.makeText(phone.this, "Image Uploaded", Toast.LENGTH_SHORT).show();
                                            addNewUserToDatabase(Username, phoneNumber, String.valueOf(System.currentTimeMillis()),"No Status.");
                                            Intent intent = new Intent(phone.this, MainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    })
                                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                                            setupText.setVisibility(View.VISIBLE);
                                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                            int currentProgress = (int) progress;
                                            progressBar.setProgress(currentProgress);
                                        }
                                    });
                        }
                    }

                    else {
                        addNewUserToDatabase(Username, phoneNumber, "default","No Status.");
                        Intent intent = new Intent(phone.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }

                }

                else {
                    Toast.makeText(phone.this, "Wrong OTP", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void addNewUserToDatabase(String name, String phone, String imageUrl,String status) {

        userDetails User = new userDetails(name, phone, imageUrl,status);
        databaseReference.child(phone).setValue(User);
    }

    public void chooseImage() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                userImage.setImageBitmap(bitmap);
                imageSelectedForUpload = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onBackPressed() {
        if (!onScreenOne) {
            onScreenOne = true;
            Animation up = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
            signupCard.startAnimation(up);
            up.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    signupCard.setTranslationY(0);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            signupCard.setTranslationY(0);
        } else
            super.onBackPressed();
    }
}