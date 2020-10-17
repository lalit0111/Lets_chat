package com.example.letschat.Adapters;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.letschat.R;
import com.example.letschat.models.message;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class GlobalChatAdapter extends RecyclerView.Adapter<GlobalChatAdapter.ViewHolder> {

    List<message> messageList;
    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();


    public GlobalChatAdapter(List<message> messageList) {
        this.messageList = messageList;
    }

    @Override
    public int getItemViewType(int position) {
        message message = messageList.get(position);
        if (message == null) {
            return 0;
        } else if (message.getSender().equals(firebaseUser.getPhoneNumber())) {
            return 0;
        }
        else {
            return  1;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if(viewType==0){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.global_right, parent, false);
        }
        else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.global_left, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d("check5","bind");
        holder.chatText.setText(messageList.get(position).getMessage());
        holder.timeInHrs.setText(messageList.get(position).getTime());
        holder.number.setText(holder.getContactName(messageList.get(position).getSender(),holder.itemView.getContext()));
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }


    public  class ViewHolder extends RecyclerView.ViewHolder {

        MaterialTextView chatText;
        TextView timeInHrs, number;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            chatText = itemView.findViewById(R.id.GlobalchatText);
            timeInHrs = itemView.findViewById(R.id.Globaltime);
            number = itemView.findViewById(R.id.senderNumber);
            Log.d("check5","called");
        }

        public String getContactName(final String phoneNumber, Context context) {
            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));

            String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};

            String contactName = phoneNumber;
            Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    contactName = cursor.getString(0);
                }
                else if(phoneNumber.equals(firebaseUser.getPhoneNumber())){
                    contactName = "Me";
                }
                else {
                    contactName = phoneNumber;
                }
                cursor.close();
            }


            return contactName;
        }
    }
}

