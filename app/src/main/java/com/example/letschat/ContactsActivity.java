package com.example.letschat;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.letschat.Adapters.ContactsAdapter;
import com.example.letschat.models.contactDetails;
import com.example.letschat.models.userDetails;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ContactsActivity extends AppCompatActivity {

    Map<String, String> namePhoneMap = new HashMap<String, String>();
    SearchView searchView;
    CardView contactsCard;
    RecyclerView contactsRecycler;
    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference = firebaseDatabase.getReference("Users");
    HashMap<String, userDetails> databaseContacts = new HashMap<>();
    List<String> contactsInFirebase = new ArrayList<String>();
    Set<String> keySet;
    public static final int PERMISSIONS_REQUEST_READ_CONTACTS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        searchView = findViewById(R.id.searchView);
        contactsCard = findViewById(R.id.contactsCard);
        contactsRecycler = findViewById(R.id.ContactsRecycler);

        firebaseUser=FirebaseAuth.getInstance().getCurrentUser();

        searchView.setQueryHint("Search Contacts...");


        Animation up = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
        contactsCard.setAnimation(up);
        up.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                contactsCard.setTranslationY(0);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });



        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                databaseContacts = (HashMap<String, userDetails>) snapshot.getValue();
                keySet = databaseContacts.keySet();
                contactsInFirebase.clear();
                contactsInFirebase.addAll(keySet);
                final List<contactDetails> contactOfUser = getPhoneNumbers();
                Log.d("check", String.valueOf(contactsInFirebase));
                final ContactsAdapter contactsAdapter = new ContactsAdapter(contactOfUser);
                contactsRecycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                contactsRecycler.setAdapter(contactsAdapter);

                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String s) {
                        Log.d("check1","querySubmit");
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String s) {
                        Log.d("check1","querychange");
                        Log.d("check2", contactOfUser.toString());
                        contactsAdapter.getFilter().filter(s);
                        return false;
                    }
                });


                int resId = R.anim.layout_fall;
                LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getApplicationContext(), resId);
                contactsRecycler.setLayoutAnimation(animation);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }


    private List<contactDetails> getPhoneNumbers() {

        List<contactDetails> contactsDetailsList = new ArrayList<contactDetails>();
        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

        while (phones.moveToNext()) {

            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));


            if (phoneNumber.length() == 10) {
                phoneNumber = "+91" + phoneNumber;
            }
            if(phoneNumber.length()==12){
                phoneNumber = "+" + phoneNumber;
            }

            if(phoneNumber.equals(firebaseUser.getPhoneNumber())){
                continue;
            }

            // Cleanup the phone number
            phoneNumber = phoneNumber.replaceAll("[()\\s-]+", "");

            // Enter Into Hash Map
            namePhoneMap.put(phoneNumber, name);

        }

        // Get The Contents of Hash Map in Log
        for (Map.Entry<String, String> entry : namePhoneMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            Log.d("check", key);
            if (contactsInFirebase.contains(key)) {
                contactDetails contactsDetails = new contactDetails(value, key);
                contactsDetailsList.add(contactsDetails);
                Log.d("check", "added");
            }

        }

        phones.close();
        return contactsDetailsList;

    }

}