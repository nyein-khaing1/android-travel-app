package com.example.travelapp;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.travelapp.Adapter.FlightAdapter;
import com.example.travelapp.Model.Flight;
import com.example.travelapp.Model.Flight;
import com.example.travelapp.databinding.ActivitySearchBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SearchActivity extends Flight1Activity {
private ActivitySearchBinding bind;
private String from, to, date;
public int numPassenger;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(bind.getRoot());

        getIntentExtra();
        initList();
        setVariable();

    }

    private void setVariable() {
        bind.backBtn.setOnClickListener(view -> {
        finish();
        });
    }

    private void initList() {
        DatabaseReference myRef = db.getReference("Flights");
        ArrayList<Flight> list = new ArrayList<>();

        // First database query - filter by origin
        Query query = myRef.orderByChild("from").equalTo(from);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot issue:snapshot.getChildren()){
                        Flight flight = issue.getValue(Flight.class);

                        // Second filter in code - match destination
                        if(flight.getTo().equals(to)){
                            list.add(flight);
                        }

                        // Handle results including empty result set
                        if(!list.isEmpty()){
                            bind.searchView.setLayoutManager(new LinearLayoutManager(SearchActivity.this,LinearLayoutManager.VERTICAL,false));
                            bind.searchView.setAdapter(new FlightAdapter(list));
                        }
                        bind.progressBarSearch.setVisibility(View.GONE);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Missing error handling was a weakness in my implementation

            }
        });

    }

    private void getIntentExtra() {
        from = getIntent().getStringExtra("from");
        to = getIntent().getStringExtra("to");
        date =getIntent().getStringExtra("date");
        numPassenger = getIntent().getIntExtra("numberOfPassenger", 1);
    }

    public int getNumPassenger() {
        return numPassenger;
    }
}