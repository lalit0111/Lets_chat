package com.example.letschat.model;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
    MaterialTextView chatText;
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
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.right, parent, false);
        }
        else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.left_message, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        chatText.setText(messageList.get(position).getMessage());
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

   public class ViewHolder extends RecyclerView.ViewHolder {
       public ViewHolder(@NonNull View itemView) {
           super(itemView);
            chatText = itemView.findViewById(R.id.chatText);
       }
   }
}

