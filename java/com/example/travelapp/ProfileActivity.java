package com.example.travelapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // UI Elements
    private TextView profileName, profileEmail, profileUsername, profilePassword;
    private Button editButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI elements
        profileName = findViewById(R.id.profileName);
        profileEmail = findViewById(R.id.profileEmail);
        profileUsername = findViewById(R.id.profileUsername);
        profilePassword = findViewById(R.id.profilePassword);
        editButton = findViewById(R.id.editButton);

        // Load user data
        loadUserData();

        // Set up edit button click listener
        editButton.setOnClickListener(v -> {
            // Create an intent to start the Sign_up activity
            Intent intent = new Intent(ProfileActivity.this, Sign_up.class);

            // Add extra data to indicate this is for editing profile
            intent.putExtra("isEditMode", true);

            // pass the current user's data if needed
            intent.putExtra("userId", mAuth.getCurrentUser().getUid());

            // Start the activity
            startActivity(intent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void loadUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            db.collection("users")
                    .document(userId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // Get user data
                                String firstName = document.getString("firstName");
                                String lastName = document.getString("lastName");
                                String email = document.getString("email");
                                String phoneNumber = document.getString("phoneNumber");
                                String country = document.getString("country");
                                String password = document.getString("password");

                                // Display user data
                                profileName.setText(firstName + " " + lastName);
                                profileEmail.setText(email);
                                profileUsername.setText(email);
                                profilePassword.setText(password);

                                Log.d(TAG, "User data loaded successfully");
                            } else {
                                Log.d(TAG, "No such document");
                                Toast.makeText(ProfileActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e(TAG, "Failed to get user data", task.getException());
                            Toast.makeText(ProfileActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // User is not logged in
            Toast.makeText(ProfileActivity.this, "No user logged in", Toast.LENGTH_SHORT).show();
            finish(); // Close this activity and return to previous one
        }
    }
}