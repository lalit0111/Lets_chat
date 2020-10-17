package com.example.letschat.Adapters;

import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.MediaStoreSignature;
import com.example.letschat.ChatActivity;
import com.example.letschat.ImageViewerActivity;
import com.example.letschat.R;
import com.example.letschat.models.userDetails;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder> {


    List<String> titles = new ArrayList<String>();
    List<String> content = new ArrayList<String>();
    List<String> phones = new ArrayList<>();
    List<String> times = new ArrayList<>();
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference, databaseReference2;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    StorageReference imageReference;
    FirebaseUser firebaseUser;
    Long time = 0L;
    userDetails userInfo;
    String phoneOfReciever, senderPhone;


    public HomeAdapter() {

    }

    public HomeAdapter(List<String> titles, List<String> content, List<String> phones, List<String> times) {
        this.titles = titles;
        this.content = content;
        this.phones = phones;
        this.times = times;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_cardview, parent, false);
        Log.d("item", "card created");
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        holder.name.setText(titles.get(position));
        holder.chat.setText(content.get(position));
        holder.lastTime.setText(times.get(position));

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();

        senderPhone = firebaseUser.getPhoneNumber();
        phoneOfReciever = phones.get(position);

        if (Long.parseLong(phoneOfReciever) > Long.parseLong(senderPhone)) {
            databaseReference2 = firebaseDatabase.getReference("seen").child(phoneOfReciever + "_" + senderPhone);

        } else {
            databaseReference2 = firebaseDatabase.getReference("seen").child(senderPhone + "_" + phoneOfReciever);
        }


        databaseReference = firebaseDatabase.getReference().child("Users").child(phones.get(position));


        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();


        databaseReference2.child(firebaseUser.getPhoneNumber()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean i = (boolean) snapshot.getValue();
                Log.d("check3", String.valueOf(i));
                if (!i) {
                    holder.chat.setTypeface(Typeface.DEFAULT_BOLD);
                    holder.name.setTypeface(Typeface.DEFAULT_BOLD);
                    holder.lastTime.setTypeface(Typeface.DEFAULT_BOLD);
                    holder.contactsImage.setBorderColor(holder.itemView.getResources().getColor(R.color.colorPrimary));
                    holder.contactsImage.setBorderWidth(6);
                }
                else {
                    holder.chat.setTypeface(Typeface.DEFAULT);
                    holder.name.setTypeface(Typeface.DEFAULT);
                    holder.lastTime.setTypeface(Typeface.DEFAULT);
                    holder.contactsImage.setBorderWidth(0);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                imageReference = storageReference.child("UsersProfileImages/" + phones.get(position));
                userInfo = snapshot.getValue(userDetails.class);
                if (!userInfo.getImageUrl().equals("default")) {
                    time = Long.valueOf(userInfo.getImageUrl());
                }
                if (imageReference != null && !userInfo.getImageUrl().equals("default")) {
                    Glide.with(holder.itemView.getContext() /* context */)
                            .load(imageReference)
                            .placeholder(R.drawable.loading_dp)
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    holder.contactsImage.setImageResource(R.drawable.dp_default);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    return false;
                                }
                            })
                            .signature(new MediaStoreSignature("image/jpeg", time, holder.itemView.getContext().getResources().getConfiguration().orientation))
                            .into(holder.contactsImage);

                } else {
                    holder.contactsImage.setImageResource(R.drawable.dp_default);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.contactsImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phone = phones.get(position);
                Intent intent = new Intent(holder.itemView.getContext(), ImageViewerActivity.class);
                intent.putExtra("phone", phone);
                holder.itemView.getContext().startActivity(intent);
            }
        });


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ChatActivity.class);
                intent.putExtra("name", titles.get(position));
                intent.putExtra("phone", phones.get(position));
                holder.itemView.getContext().startActivity(intent);

            }
        });
    }

    @Override
    public int getItemCount() {
        return titles.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        View v;
        TextView name;
        TextView chat;
        CircleImageView contactsImage;
        TextView lastTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            contactsImage = itemView.findViewById(R.id.mainCircular);
            name = itemView.findViewById(R.id.name);
            chat = itemView.findViewById(R.id.chat);
            lastTime = itemView.findViewById(R.id.lastTime);
            v = itemView;
        }
    }
}

