package com.example.letschat.ui.home;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.letschat.ContactsActivity;
import com.example.letschat.R;
import com.example.letschat.models.message;
import com.example.letschat.Adapters.HomeAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment {

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference, databaseReference2;
    FirebaseUser firebaseUser;
    List<String> titles = new ArrayList<>();
    List<String> contents = new ArrayList<>();
    List<String> phones = new ArrayList<>();
    List<String> times = new ArrayList<>();
    ChildEventListener childEventListener;

    private HomeViewModel homeViewModel;
    RecyclerView recyclerView;
    HomeAdapter adapter;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        final View root = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = root.findViewById(R.id.recycler);


        int resId = R.anim.layout_fall;
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getContext(), resId);
        recyclerView.setLayoutAnimation(animation);



        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Inbox").child(firebaseUser.getPhoneNumber());


        adapter = new HomeAdapter(titles, contents, phones,times);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);


        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull final DataSnapshot snapshot0, @Nullable String previousChildName) {
                String Content= snapshot0.getValue().toString();
                Log.d("check3" , Content);
                DatabaseReference dr = FirebaseDatabase.getInstance().getReference("Chats");
                final Query q = dr.child(Content).orderByKey().limitToLast(1);
                q.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        message msg= snapshot.getValue(message.class);
                        String name = getContactName(snapshot0.getKey(),getContext());
                        assert msg != null;
                        String number =snapshot0.getKey();
                        String content = msg.getMessage();
                        String currentTime = msg.getTime();

                        if(!phones.contains(number)){
                            titles.add(name);
                            contents.add(content);
                            phones.add(number);
                            times.add(currentTime);
                            adapter.notifyDataSetChanged();
                        }
                        else {
                            int index = phones.indexOf(number);
                            contents.remove(index);
                            contents.add(index,content);
                            times.remove(index);
                            times.add(index,currentTime);
                            adapter.notifyDataSetChanged();
                        }

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
        };

        databaseReference.addChildEventListener(childEventListener);





        FloatingActionButton fab = root.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                    Intent intent = new Intent(getActivity(), ContactsActivity.class);
                    startActivity(intent);
            }
        });



        return root;
    }


    public String getContactName(final String phoneNumber, Context context) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));

        String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};

        String contactName = "";
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                contactName = cursor.getString(0);
            }
            else {
                contactName = phoneNumber;
            }
            cursor.close();
        }

        return contactName;
    }

    @Override
    public void onStart() {
        super.onStart();

    }
}