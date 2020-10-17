package com.example.letschat;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.MediaStoreSignature;
import com.example.letschat.models.message;
import com.example.letschat.models.userDetails;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    EditText messageBox;
    ImageButton send;
    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference2, databaseReference3, databaseReference4, databaseReference5;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    StorageReference imageReference;
    DatabaseReference databaseReference;
    List<message> messagesList;
    String senderPhone;
    String senderPhoneSub;
    String phoneOfReciever;
    RecyclerView chats;
    Toolbar toolbar;
    ImageButton back;
    CircleImageView profilePic;
    TextView usernameToolbar;
    String username;
    Long time = 0L;
    LinearLayout seenLayout;
    userDetails userInfo;
    String ref;
    FirebaseRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        toolbar = findViewById(R.id.chatToolbar);
        back = findViewById(R.id.backButton);
        seenLayout = findViewById(R.id.seenLayout);
        setSupportActionBar(toolbar);

        messagesList = new ArrayList<>();


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        senderPhone = firebaseUser.getPhoneNumber();

        final Intent intent = getIntent();
        phoneOfReciever = intent.getStringExtra("phone");
        username = intent.getStringExtra("name");


        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference3 = firebaseDatabase.getReference("Inbox").child(senderPhone);
        databaseReference4 = firebaseDatabase.getReference("Inbox").child(phoneOfReciever);

        senderPhoneSub = firebaseUser.getPhoneNumber().substring(2);
        if (Long.parseLong(phoneOfReciever) > Long.parseLong(senderPhone)) {
            ref = phoneOfReciever + "_" + senderPhone;
            databaseReference = firebaseDatabase.getReference("Chats").child(phoneOfReciever + "_" + senderPhone);
            databaseReference5 = firebaseDatabase.getReference("seen").child(phoneOfReciever + "_" + senderPhone);

        } else {
            ref = senderPhone + "_" + phoneOfReciever;
            databaseReference = firebaseDatabase.getReference("Chats").child(senderPhone + "_" + phoneOfReciever);
            databaseReference5 = firebaseDatabase.getReference("seen").child(senderPhone + "_" + phoneOfReciever);
        }

        messageBox = findViewById(R.id.messageBox);
        send = findViewById(R.id.sendButton);
        chats = findViewById(R.id.chatRecyclerView);
        profilePic = findViewById(R.id.profilePic);
        usernameToolbar = findViewById(R.id.usernameToolbar);

        usernameToolbar.setText(username);


        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = messageBox.getText().toString().trim();
                if (!message.isEmpty()) {
                   String currentTime = new SimpleDateFormat("hh:mm aa", Locale.getDefault()).format(new Date());

                    addNewUserToDatabase(messageBox.getText().toString().trim(), firebaseUser.getPhoneNumber(),currentTime);
                    messageBox.setText("");

                    databaseReference3.child(phoneOfReciever).setValue(ref);
                    databaseReference4.child(senderPhone).setValue(ref);
                    databaseReference5.child(phoneOfReciever).setValue(false);
                }
            }
        });

        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(ChatActivity.this, ProfileViewActivity.class);
                intent1.putExtra("phone2", phoneOfReciever);
                intent1.putExtra("contact", username);
                startActivity(intent1);
            }
        });

        usernameToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(ChatActivity.this, ProfileViewActivity.class);
                intent1.putExtra("phone2", phoneOfReciever);
                intent1.putExtra("contact", username);
                startActivity(intent1);
            }
        });

        databaseReference2 = firebaseDatabase.getReference().child("Users").child(phoneOfReciever);

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        databaseReference2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                imageReference = storageReference.child("UsersProfileImages/" + phoneOfReciever);
                userInfo = snapshot.getValue(userDetails.class);
                if (!userInfo.getImageUrl().equals("default")) {
                    time = Long.valueOf(userInfo.getImageUrl());
                }
                if (imageReference != null && !userInfo.getImageUrl().equals("default")) {
                    Glide.with(getApplicationContext() /* context */)
                            .load(imageReference)
                            .placeholder(R.drawable.loading_dp)
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    profilePic.setImageResource(R.drawable.dp_default);
                                    Toast.makeText(ChatActivity.this, "Unable to load", Toast.LENGTH_SHORT).show();
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
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        final Query query = databaseReference;

        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                messagesList.add(snapshot.getValue(message.class));
                databaseReference5.child(senderPhone).setValue(true);


                Log.d("check3", String.valueOf(messagesList));
                Log.d("check3", String.valueOf(snapshot.getKey()));

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        databaseReference5.child(phoneOfReciever).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.getValue() != null) {
                    boolean i = (boolean) snapshot.getValue();
                    if (i && messagesList.size() != 0 && messagesList.get(messagesList.size() - 1).getSender().equals(firebaseUser.getPhoneNumber())) {
                        Log.d("check3", "made visible");
                        seenLayout.setVisibility(View.VISIBLE);
                    } else {
                        seenLayout.setVisibility(View.GONE);
                    }
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });


        FirebaseRecyclerOptions<message> options = new FirebaseRecyclerOptions.Builder<message>().setQuery(query, message.class).build();

        adapter = new FirebaseRecyclerAdapter<message, ChatHolder>(options) {

            @Override
            public int getItemViewType(int position) {
                message message = messagesList.get(position);

                if (message == null) {
                    return 0;
                } else if (message.getSender().equals(firebaseUser.getPhoneNumber())) {
                    return 0;
                } else {
                    return 1;
                }
            }

            @Override
            protected void onBindViewHolder(@NonNull ChatHolder chatHolder, int i, @NonNull message message) {
                chatHolder.chatText.setText(message.getMessage());
                chatHolder.timeInHrs.setText(message.getTime());
            }

            @NonNull
            @Override
            public ChatHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view;

                if (viewType == 0) {
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.right_message, parent, false);
                } else {
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.left_message, parent, false);
                }
                return new ChatHolder(view);
            }
        };


        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        chats.setLayoutManager(linearLayoutManager);
        chats.setAdapter(adapter);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                chats.scrollToPosition(adapter.getItemCount() - 1);
            }
        });

    }

    public class ChatHolder extends RecyclerView.ViewHolder {
        MaterialTextView chatText;
        TextView timeInHrs;

        public ChatHolder(@NonNull View itemView) {
            super(itemView);
            chatText = itemView.findViewById(R.id.chatText);
            timeInHrs=itemView.findViewById(R.id.time);
        }
    }


    private void addNewUserToDatabase(String message, String sender, String time) {
        message messageObject = new message(message, sender, time);
        databaseReference.push().setValue(messageObject);
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}