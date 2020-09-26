package com.example.letschat.ui.home;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.letschat.Contacts;
import com.example.letschat.MainActivity;
import com.example.letschat.R;
import com.example.letschat.message;
import com.example.letschat.model.Adapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.example.letschat.Contacts.PERMISSIONS_REQUEST_READ_CONTACTS;


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
    Adapter adapter;

    TextView toolbarText;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        final View root = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = root.findViewById(R.id.recycler);

//        toolbarText = getActivity().findViewById(R.id.title_text);
//        toolbarText.setText("CHATS");


        int resId = R.anim.layout_fall;
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getContext(), resId);
        recyclerView.setLayoutAnimation(animation);



        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Inbox").child(firebaseUser.getPhoneNumber());


        adapter = new Adapter(titles, contents, phones,times);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);


//        databaseReference.addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(@NonNull DataSnapshot snapshot0, @Nullable String previousChildName) {
//
//                String key = snapshot0.getKey().toString();
//                phones.add(key);
//                titles.add(getContactName(key, getContext()));
//
//
//                Log.d("check3", String.valueOf(titles));
//
//
//                String contentRef = snapshot0.getValue(String.class);
//
//                databaseReference2 = firebaseDatabase.getReference("Chats");
//
//                assert contentRef != null;
//                Query q = databaseReference2.child(contentRef).orderByKey().limitToLast(1);
//
//                q.addChildEventListener(new ChildEventListener() {
//                    @Override
//                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                        Log.d("check3", "ecx");
//                        message message = snapshot.getValue(com.example.letschat.message.class);
//                        assert message != null;
//                        String Content = message.getMessage();
//                        contents.add(Content);
//
//
//                    }
//
//                    @Override
//                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//
//                    }
//
//                    @Override
//                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {
//
//                    }
//
//                    @Override
//                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//                        Log.d("check3", error.getMessage());
//                    }
//                });
//            }
//
//            @Override
//            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//
//            }
//
//            @Override
//            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//
//        });



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

//                        int i=contains(chatArrayList,number);
//                        if(i==-1){
//                            chatArrayList.add(0,chat);
//                        }else{
//                            chatArrayList.remove(i);
//                            chatArrayList.add(0,chat);
//                        }
//
//                        index.put(snapshot0.getKey(),i);
//                        myAdapter.notifyDataSetChanged();
//                        Log.d("snumber",number);


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

//                Boolean permissionGranted = requestContactPermission();
//                if (permissionGranted) {

                    Intent intent = new Intent(getActivity(), Contacts.class);
                    startActivity(intent);
//                }
            }
        });



        return root;
    }

//    public Boolean requestContactPermission() {
//        Boolean permissionGranted = false;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
//                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
//                        android.Manifest.permission.READ_CONTACTS)) {
//                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//                    builder.setTitle("Read Contacts permission");
//                    builder.setPositiveButton(android.R.string.ok, null);
//                    builder.setMessage("Please enable access to contacts.");
//                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
//                        @TargetApi(Build.VERSION_CODES.M)
//                        @Override
//                        public void onDismiss(DialogInterface dialog) {
//                            requestPermissions(
//                                    new String[]
//                                            {android.Manifest.permission.READ_CONTACTS}
//                                    , PERMISSIONS_REQUEST_READ_CONTACTS);
//                        }
//                    });
//                    builder.show();
//                } else {
//                    ActivityCompat.requestPermissions(getActivity(),
//                            new String[]{android.Manifest.permission.READ_CONTACTS},
//                            PERMISSIONS_REQUEST_READ_CONTACTS);
//                }
//            } else {
//                permissionGranted = true;
//            }
//        } else {
//            permissionGranted = true;
//        }
//        return permissionGranted;
//    }

    public String getContactName(final String phoneNumber, Context context) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));

        String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};

        String contactName = "";
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                contactName = cursor.getString(0);
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