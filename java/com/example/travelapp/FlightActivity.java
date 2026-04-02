package com.example.travelapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import android.app.DatePickerDialog;
import java.util.Calendar;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class FlightActivity extends AppCompatActivity {

    // Declare UI components
    private EditText departureText, destinationEditText, departureDateEditText, arrivalDateEditText;
    private MaterialCardView departureDateCard, arrivalDateCard;
    private Spinner passengersSpinner; // Spinner for passengers count
    private Button createButton;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight);

        // Initialize the views
        departureText = findViewById(R.id.departureText);
        destinationEditText = findViewById(R.id.destinationEditText);
        departureDateEditText = findViewById(R.id.departureDateEditText);
        arrivalDateEditText = findViewById(R.id.arrivalDateEditText);
        departureDateCard = findViewById(R.id.departureDateCard);

        // Initialize the Spinner
        passengersSpinner = findViewById(R.id.passengersSpinner);

        // Set up the Spinner with data
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.passenger_numbers, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        passengersSpinner.setAdapter(adapter);

        // Set a listener to handle item selection
        passengersSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Get the selected passenger count
                String selectedPassengerCount = parentView.getItemAtPosition(position).toString();
                Toast.makeText(FlightActivity.this, "Selected passengers: " + selectedPassengerCount, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Handle the case where no item is selected (optional)
            }
        });

        // Null check for EditText views
        if (departureDateEditText == null) {
            Log.e("FlightActivity", "departureDateEditText is not initialized properly");
        }

        if (arrivalDateEditText == null) {
            Log.e("FlightActivity", "arrivalDateEditText is not initialized properly");
        }

        // Set listeners for date selection
        setUpDatePicker(departureDateEditText, true);  // Departure date

        // Initialize the Create button and set its OnClickListener
        createButton = findViewById(R.id.createButton);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Validate the inputs before proceeding
                if (departureText.getText().toString().isEmpty()) {
                    Toast.makeText(FlightActivity.this, "Please enter departure location", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (destinationEditText.getText().toString().isEmpty()) {
                    Toast.makeText(FlightActivity.this, "Please enter destination location", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (departureDateEditText.getText().toString().isEmpty()) {
                    Toast.makeText(FlightActivity.this, "Please select a departure date", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (passengersSpinner.getSelectedItemPosition() == 0) {
                    Toast.makeText(FlightActivity.this, "Please select the number of passengers", Toast.LENGTH_SHORT).show();
                    return;
                }


                // Get trip details
                String departure = departureText.getText().toString();
                String arrival = destinationEditText.getText().toString();
                String flight_date = departureDateEditText.getText().toString();
                String passengers = passengersSpinner.getSelectedItem().toString();


                // Store trip data in Firebase
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                Map<String, Object> tripData = new HashMap<>();
                tripData.put("departure", departure);
                tripData.put("arrival", arrival);
                tripData.put("flight_date", flight_date);
                tripData.put("passengers", passengers);

                db.collection("trips")
                        .add(tripData)
                        .addOnSuccessListener(documentReference -> {
                            Log.d("Firebase", "Trip saved with ID: " + documentReference.getId());

                            // Move to AvailableFlights after storing data
                            Intent intent = new Intent(FlightActivity.this, availabeFlights.class);
                            intent.putExtra("departure", departure);
                            intent.putExtra("arrival", arrival);
                            intent.putExtra("flight_date", flight_date);
                            startActivity(intent);
                        })
                        .addOnFailureListener(e -> Log.e("Firebase", "Error saving trip", e));





            }
        });
    }

    // Method to show DatePickerDialog when the user clicks on the date EditText
    private void setUpDatePicker(final EditText editText, final boolean isDeparture) {
        editText.setFocusable(false);

        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get current date
                final Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

                // Create a DatePickerDialog
                DatePickerDialog datePickerDialog = new DatePickerDialog(FlightActivity.this,
                        (view, year1, month1, dayOfMonth1) -> {
                            // Format the selected date and set it to the EditText
                            String selectedDate = dayOfMonth1 + "/" + (month1 + 1) + "/" + year1;
                            if (editText != null) {
                                editText.setText(selectedDate);
                            }
                            if (isDeparture) {
                                Toast.makeText(FlightActivity.this, "Departure date selected", Toast.LENGTH_SHORT).show();
                            } else {
                                //Toast.makeText(Flight.this, "Arrival date selected", Toast.LENGTH_SHORT).show();
                            }
                        }, year, month, dayOfMonth);
                datePickerDialog.show();
            }
        });
    }
}
