package com.example.travelapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.travelapp.Model.Flight;
import com.example.travelapp.databinding.ActivityTicketDetailBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class TicketDetailActivity extends AppCompatActivity {
    private ActivityTicketDetailBinding bind;
    private Flight flight;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private String userId;
    private int numberOfPassengers = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = ActivityTicketDetailBinding.inflate(getLayoutInflater());
        setContentView(bind.getRoot());

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("UserTickets");

        // Get current user ID
        if (firebaseAuth.getCurrentUser() != null) {
            userId = firebaseAuth.getCurrentUser().getUid();
        } else {
            // Handle case when user is not logged in
            Toast.makeText(this, "Please log in to save tickets", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        getIntentExtra();
        if (flight != null) {
            setVar();
            saveFlightToFirebase(); // Save the flight to Firebase
        } else {
            Log.e("TicketDetailActivity", "Flight object is null");
            Toast.makeText(this, "No ticket information available", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void getIntentExtra() {
        flight = (Flight) getIntent().getSerializableExtra("flight");
        numberOfPassengers = getIntent().getIntExtra("numberOfPassenger", 1);
        if (flight == null) {
            Log.e("TicketDetailActivity", "Flight object is null in getIntentExtra()");
        } else {
            Log.d("TicketDetailActivity", "Flight object retrieved: " + flight.getFromShort());
            Log.d("TicketDetailActivity", "Number of passengers: " + numberOfPassengers);
        }
    }

    private void setVar() {
        bind.backBtn.setOnClickListener(view -> finish());

        // Set data from the Flight object
        bind.fromTxt.setText(flight.getFrom());
        bind.fromShortTxt.setText(flight.getFromShort());
        bind.toTxt.setText(flight.getTo());
        bind.toShortTxt.setText(flight.getToShort());
        bind.fromSmallTxt.setText(flight.getFrom());
        bind.toSmallTxt.setText(flight.getTo());
        bind.dateTxt.setText(flight.getDate());
        bind.timeTxt.setText(flight.getTime());
        bind.arrivalTxt.setText(flight.getArrivalTime());
        bind.classTxt.setText(flight.getClassSeat());

        // Get number of passengers from the flight object
        int numPassengers = flight.getNumberOfPassengers() != null ? flight.getNumberOfPassengers() : 1;

        // Calculate the total price based on number of passengers
        double unitPrice = flight.getPrice();
        double totalPrice = unitPrice * numPassengers;

        // Format the price with 2 decimal places and show number of passengers
        if (numPassengers > 1) {
            bind.priceTxt.setText(String.format("£%.2f (£%.2f × %d passengers)",
                    totalPrice, unitPrice, numPassengers));
        } else {
            bind.priceTxt.setText(String.format("£%.2f", unitPrice));
        }


        // Load airline logo using Glide
        Glide.with(TicketDetailActivity.this)
                .load(flight.getairlineLogo())
                .into(bind.logo);
    }

    private void saveFlightToFirebase() {
        if (flight != null && userId != null) {
            // Making sure the flight has the number of passengers before saving
            if (flight.getNumberOfPassengers() == null) {
                flight.setNumberOfPassengers(1); // Default to 1 if not set
            }

            // Create a unique key for this ticket
            String ticketId = databaseReference.child(userId).push().getKey();

            if (ticketId != null) {
                // Save the flight under this user's ID
                databaseReference.child(userId).child(ticketId).setValue(flight)
                        .addOnSuccessListener(aVoid -> {
                            Log.d("TicketDetailActivity", "Ticket saved successfully");
                            Toast.makeText(TicketDetailActivity.this, "Ticket saved successfully", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Log.e("TicketDetailActivity", "Failed to save ticket", e);
                            Toast.makeText(TicketDetailActivity.this, "Failed to save ticket: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        }
    }
}