package com.example.letschat.model;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
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
import com.example.letschat.Chat;
import com.example.letschat.MainActivity;
import com.example.letschat.R;
import com.example.letschat.contactsDetails;
import com.example.letschat.imageViewer;
import com.example.letschat.userDetails;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> implements Filterable {


    List<contactsDetails> contactDetailsList;
    List<contactsDetails> contactsDetailsAll;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    StorageReference imageReference;
    Long time = 0L;
    userDetails userInfo;


    public ContactsAdapter(List<contactsDetails> contactDetailsList) {
        this.contactDetailsList = contactDetailsList;
        contactsDetailsAll = new ArrayList<>(contactDetailsList);
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contacts_cardview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final contactsDetails contactsDetails = contactDetailsList.get(position);
        holder.contactsName.setText(contactsDetails.getName());
        holder.contactsStatus.setText(contactsDetails.getPhoneNumber());

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference().child("Users").child(contactsDetails.getPhoneNumber());

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                imageReference = storageReference.child("UsersProfileImages/" + contactsDetails.getPhoneNumber());
                userInfo = snapshot.getValue(userDetails.class);
                if (!userInfo.getImageUrl().equals("default")) {
                    time = Long.valueOf(userInfo.getImageUrl());
                }
                if (imageReference != null && !userInfo.getImageUrl().equals("default")) {
                    Glide.with(holder.itemView.getContext() /* context */)
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
                            .signature(new MediaStoreSignature("image/jpeg", time, holder.itemView.getContext().getResources().getConfiguration().orientation))
                            .into(holder.contactsImage);

                } else {
                    holder.contactsImage.setImageResource(R.drawable.new_profile);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.contactsImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phone = contactsDetails.getPhoneNumber();
                Intent intent = new Intent(holder.itemView.getContext(), imageViewer.class);
                intent.putExtra("phone", phone);
                holder.itemView.getContext().startActivity(intent);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(),Chat.class);
                intent.putExtra("name",contactsDetails.getName());
                intent.putExtra("phone",contactsDetails.getPhoneNumber());
                holder.itemView.getContext().startActivity(intent);

            }
        });
    }

    @Override
    public int getItemCount() {
        return contactDetailsList.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {

                List<contactsDetails> filteredList = new ArrayList<>();

                if (charSequence.toString().isEmpty()) {
                    filteredList.addAll(contactsDetailsAll);
                    Log.d("check1", "char is empty");
                    Log.d("check1", contactsDetailsAll.toString());
                } else {
                    for (contactsDetails var : contactsDetailsAll) {
                        if (var.getName().toLowerCase().contains(charSequence) || var.getPhoneNumber().toLowerCase().contains(charSequence)) {
                            filteredList.add(var);
                            Log.d("check1", "added in list");
                        }
                    }
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredList;

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                contactDetailsList.clear();
                contactDetailsList.addAll((Collection<? extends contactsDetails>) filterResults.values);
                Log.d("check1", "publish");
                notifyDataSetChanged();

            }
        };
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        View v;
        TextView contactsName, contactsStatus;
        CircleImageView contactsImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            contactsName = itemView.findViewById(R.id.ContactsName);
            contactsStatus = itemView.findViewById(R.id.ContactsStatus);
            contactsImage = itemView.findViewById(R.id.contactsImage);
            v = itemView;
        }
    }


}

