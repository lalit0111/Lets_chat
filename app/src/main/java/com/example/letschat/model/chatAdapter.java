package com.example.letschat.model;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.letschat.R;
import com.example.letschat.message;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class chatAdapter extends RecyclerView.Adapter<chatAdapter.ViewHolder> {

    List<message> messageList;
    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();


    public chatAdapter(List<message> messageList){

        this.messageList=messageList;
    }

    @Override
    public int getItemViewType(int position) {
        message message = messageList.get(position);
        if(message==null){
            return 0;
        }
        else if (message.getSender().equals(firebaseUser.getPhoneNumber())){
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
        holder.number.setText(messageList.get(position).getSender());
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
    }
}

