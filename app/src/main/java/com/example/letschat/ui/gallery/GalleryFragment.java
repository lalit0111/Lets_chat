package com.example.letschat.ui.gallery;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.letschat.R;
import com.example.letschat.message;
import com.example.letschat.model.chatAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GalleryFragment extends Fragment {


    EditText messageBox;
    ImageButton send;
    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    List<message> messagesList;
    RecyclerView chats;
    chatAdapter adapter;


    private GalleryViewModel galleryViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        galleryViewModel =
                ViewModelProviders.of(this).get(GalleryViewModel.class);
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);


        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        messageBox = root.findViewById(R.id.GlobalmessageBox);
        send = root.findViewById(R.id.GlobalsendButton);
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Global");
        chats = root.findViewById(R.id.GlobalchatRecyclerView);
        messagesList = new ArrayList<message>();


        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = messageBox.getText().toString().trim();
                if (!message.isEmpty()) {

                    String currentTime = new SimpleDateFormat("hh:mm aa", Locale.getDefault()).format(new Date());
                    Log.d("check4", String.valueOf(adapter.getItemCount()));
                    addNewUserToDatabase(messageBox.getText().toString().trim(), firebaseUser.getPhoneNumber(), currentTime);
                    messageBox.setText("");
                }
            }
        });


        final Query query = databaseReference.orderByKey().limitToLast(50);

        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                messagesList.add(snapshot.getValue(message.class));

                Log.d("check4", String.valueOf(messagesList));
                Log.d("check4", String.valueOf(snapshot.getKey()));
                adapter.notifyDataSetChanged();

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


//        FirebaseRecyclerOptions<message> options = new FirebaseRecyclerOptions.Builder<message>().setQuery(query, message.class).build();
//
//        adapter = new FirebaseRecyclerAdapter<message, ChatHolder>(options) {
//
//            @Override
//            public int getItemViewType(int position) {
//                message message = messagesList.get(position);
//
//                if (message == null) {
//                    return 0;
//                } else if (message.getSender().equals(firebaseUser.getPhoneNumber())) {
//                    return 0;
//                } else {
//                    return 1;
//                }
//            }
//
//            @NonNull
//            @Override
//            public ChatHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//                View view;
//
//                if (viewType == 0) {
//                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.global_right, parent, false);
//                } else {
//                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.global_left, parent, false);
//                }
//                return new ChatHolder(view);
//            }
//
//            @Override
//            protected void onBindViewHolder(@NonNull ChatHolder chatHolder, int i, @NonNull message message) {
//                chatHolder.chatText.setText(message.getMessage());
//                chatHolder.timeInHrs.setText(message.getTime());
//                chatHolder.number.setText(message.getSender());
//            }
//        };
//
//        Log.d("check5", String.valueOf(adapter));
        adapter = new chatAdapter(messagesList);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(root.getContext());
        chats.setLayoutManager(linearLayoutManager);
        chats.setAdapter(adapter);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                chats.scrollToPosition(messagesList.size()- 1);
            }
        });

        return root;
    }

//    public class ChatHolder extends RecyclerView.ViewHolder {
//        MaterialTextView chatText;
//        TextView timeInHrs, number;
//
//        public ChatHolder(@NonNull View itemView) {
//            super(itemView);
//            Log.d("check5","called");
//            chatText = itemView.findViewById(R.id.GlobalchatText);
//            timeInHrs = itemView.findViewById(R.id.Globaltime);
//            number = itemView.findViewById(R.id.senderNumber);
//        }
//    }

    private void addNewUserToDatabase(String message, String sender, String time) {
        message messageObject = new message(message, sender, time);
        databaseReference.push().setValue(messageObject);
    }
}