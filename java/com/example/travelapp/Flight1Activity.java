package com.example.travelapp;

import com.example.travelapp.Model.Flight;
import com.example.travelapp.Model.Location;
import com.example.travelapp.databinding.ActivityFlight1Binding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.ismaeldivita.chipnavigation.ChipNavigationBar;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class Flight1Activity extends Flight2Activity {
    private ActivityFlight1Binding bind;
    private int adultSeat = 1;
    private SimpleDateFormat date = new SimpleDateFormat("d MMM, yyyy", Locale.ENGLISH);
    private Calendar calender = Calendar.getInstance();

    private ChipNavigationBar chipNavigationBar;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = ActivityFlight1Binding.inflate(getLayoutInflater());
        setContentView(bind.getRoot());

        Locations();
        Passengers();
        ClassSeat();
        DatePickup();
        Variable();

        // Initialized the ChipNavigationBar
        chipNavigationBar = findViewById(R.id.bottom_nav_bar);
        if (chipNavigationBar == null) {
            Log.e("Navigation", "ChipNavigationBar is null");
        } else {
            Log.d("Navigation", "ChipNavigationBar initialized successfully");
        }


        //chipNavigationBar.setItemSelected(R.id.nav_ticket, true);

        chipNavigationBar.setOnItemSelectedListener(id -> {
            Log.d("Navigation", "Selected item ID: " + id);
            if (id == R.id.nav_home) {
                Log.d("Navigation", "Home button clicked");
                Intent homeIntent = new Intent(Flight1Activity.this, Home.class);
                startActivity(homeIntent);
            } else if (id == R.id.nav_ticket) {
                Log.d("Navigation", "Ticket button clicked");

                // Get the current userID
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

                if (userId != null) {
                    // Reference to the user's ticket
                    DatabaseReference userTicketsRef = FirebaseDatabase.getInstance().getReference("UserTickets")
                            .child(userId);

                    // Show a loading indicator


                    userTicketsRef.limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            // Hiding loading indicator if you added one

                            if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                                for (DataSnapshot ticketSnapshot : snapshot.getChildren()) {
                                    Flight savedFlight = ticketSnapshot.getValue(Flight.class);
                                    if (savedFlight != null) {
                                        // Navigate to TicketDetailActivity with the flight
                                        Intent ticketDetailIntent = new Intent(Flight1Activity.this, TicketDetailActivity.class);
                                        ticketDetailIntent.putExtra("flight", savedFlight);
                                        startActivity(ticketDetailIntent);
                                        return;
                                    }
                                }
                            } else {
                                // No saved tickets found - show an informative message
                                Toast.makeText(Flight1Activity.this, "You don't have any saved tickets yet. Book a flight first!", Toast.LENGTH_LONG).show();

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Hide loading indicator if you added one

                            Log.e("Navigation", "Failed to load user tickets", error.toException());
                            Toast.makeText(Flight1Activity.this, "Unable to access your tickets. Please try again later.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(Flight1Activity.this, "Please log in to view your tickets", Toast.LENGTH_SHORT).show();

                }

            } else if (id == R.id.nav_profile) {
                Log.d("Navigation", "Profile button clicked");
                // Directly navigate to TicketDetailActivity without passing any data
                Intent profileIntent = new Intent(Flight1Activity.this, ProfileActivity.class);
                startActivity(profileIntent);}
            else {
                Log.d("Navigation", "Unknown item clicked");
            }
        });
    }






    private void Variable() {
        bind.searchBtn.setOnClickListener(view -> {
            // This casting could fail if spinner adapter isn't properly set up
            Location fromLocation = (Location) bind.fromSp.getSelectedItem();
            Location toLocation = (Location) bind.toSp.getSelectedItem();
            Intent intent = new Intent(Flight1Activity.this,SearchActivity.class);
            // validation to prevent app crashes

            if (fromLocation != null && toLocation != null) {
                intent.putExtra("from", fromLocation.getName());
                intent.putExtra("to", toLocation.getName());
                intent.putExtra("date", bind.departureDateText.getText().toString());
                intent.putExtra("numberOfPassenger", adultSeat);
                startActivity(intent);
            } else {
                Toast.makeText(Flight1Activity.this, "Please select valid locations", Toast.LENGTH_SHORT).show();
            }

        });
    }

    private void DatePickup() {
        Calendar calendarToday = Calendar.getInstance();
        String currentDay = date.format(calendarToday.getTime());
        bind.departureDateText.setText(currentDay);

        Calendar calendarTmr= Calendar.getInstance();
        calendarTmr.add(Calendar.DAY_OF_YEAR, 1);
        String tomorrowDay = date.format(calendarTmr.getTime());
        bind.returnDateText.setText(tomorrowDay);

        bind.departureDateText.setOnClickListener(view -> showDatePickerDialog(bind.departureDateText));
        bind.returnDateText.setOnClickListener(view -> showDatePickerDialog(bind.returnDateText));

    }

    private void ClassSeat() {
        bind.progressBarClass.setVisibility(View.VISIBLE);
        ArrayList<String> bList = new ArrayList<>();
        bList.add("Business Class");
        bList.add ("First Class");
        bList.add("Economy Class");

        ArrayAdapter<String> a = new ArrayAdapter<>(Flight1Activity.this,R.layout.sp_item,bList);
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bind.classSp.setAdapter(a);
        bind.progressBarClass.setVisibility(View.GONE);


    }

    private void Passengers() {
        bind.plusAdultBtn.setOnClickListener(view -> {
            adultSeat++;
            bind.adultTxt.setText(adultSeat + "Adult");

        });
        bind.minusAdultbtn.setOnClickListener(view -> {
            if(adultSeat>1){
                adultSeat--;
                bind.adultTxt.setText(adultSeat + "Adult");
            }
        });
    }

    private void Locations(){

        bind.progressBarFrom.setVisibility(View.VISIBLE);
        bind.progressBarTo.setVisibility(View.VISIBLE);
        DatabaseReference dRef = db.getReference("Locations");
        ArrayList<Location> aList = new ArrayList<>();
        dRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot issue:snapshot.getChildren()){
                        aList.add(issue.getValue(Location.class));
                    }
                    // Ensure the correct constructor is used
                    ArrayAdapter<Location> abc = new ArrayAdapter<>(Flight1Activity.this, R.layout.sp_item,aList);
                    abc.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    bind.fromSp.setAdapter(abc);
                    bind.toSp.setAdapter(abc);
                    bind.fromSp.setSelection(1);
                    bind.progressBarFrom.setVisibility(View.GONE);
                    bind.progressBarTo.setVisibility(View.GONE);




                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }

    private void showDatePickerDialog (TextView textView){
        int year = calender.get(Calendar.YEAR);
        int month = calender.get(Calendar.MONTH);
        int day = calender.get(Calendar.DAY_OF_MONTH);


        DatePickerDialog datePickerDialog = new DatePickerDialog(this,(view,selectedYear,selectedMonth,selectedDay)->{
            calender.set(selectedYear, selectedMonth,selectedDay);
            String formattedDate = date.format(calender.getTime());
            textView.setText(formattedDate);

        },year,month,day);
        datePickerDialog.show();
    }
}