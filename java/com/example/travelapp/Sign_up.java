package com.example.travelapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;


public class Sign_up extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    DatabaseReference reference;
    private boolean isEditMode = false;

    // Views
    private EditText firstNameEdit, lastNameEdit, emailEdit, phoneEdit, countryEdit, passwordEdit, confirmPasswordEdit;
    private CheckBox termsCheckBox;
    private Button createAccountButton;
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        firstNameEdit = findViewById(R.id.firstNameEditText);
        lastNameEdit = findViewById(R.id.lastNameEditText);
        emailEdit = findViewById(R.id.emailEditText);
        phoneEdit = findViewById(R.id.phoneEditText);
        countryEdit= findViewById(R.id.countryEditText);
        passwordEdit = findViewById(R.id.passwordEditText);
        confirmPasswordEdit = findViewById(R.id.confirmPasswordEditText);
        termsCheckBox = findViewById(R.id.termsCheckBox);
        createAccountButton = findViewById(R.id.createAccountButton);

        // Check if its  in edit mode
        isEditMode = getIntent().getBooleanExtra("isEditMode", false);

        if (isEditMode) {
            // Change the button text
            createAccountButton.setText("Save Changes");

            // Hide terms checkbox since user already agreed
            termsCheckBox.setVisibility(View.GONE);

            // Load existing user data
            loadUserData();
        }

        // Listen for button click
        createAccountButton.setOnClickListener(v -> {
            String email = emailEdit.getText().toString().trim();
            String password = passwordEdit.getText().toString().trim();
            String confirmPassword = confirmPasswordEdit.getText().toString().trim();

            if (isEditMode) {
                // Update existing user
                if (password.equals(confirmPassword)) {
                    updateUserData();
                } else {
                    Toast.makeText(Sign_up.this, "Passwords don't match", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Create new user
                if (password.equals(confirmPassword) && termsCheckBox.isChecked()) {
                    signup(email, password);
                } else {
                    Toast.makeText(Sign_up.this, "Passwords don't match or terms not accepted", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null && getIntent().getBooleanExtra("fromMain", false)) {
            Intent intent = new Intent(Sign_up.this, Home.class);
            startActivity(intent);
        }
    }

    // Load user data for editing
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

                                // Fill the form with existing data
                                firstNameEdit.setText(firstName);
                                lastNameEdit.setText(lastName);
                                emailEdit.setText(email);
                                phoneEdit.setText(phoneNumber);
                                countryEdit.setText(country);
                                passwordEdit.setText(password);
                                confirmPasswordEdit.setText(password);

                                // Disable email field since it's linked to Firebase Auth
                                emailEdit.setEnabled(false);

                                Log.d("Sign_up", "User data loaded for editing");
                            }
                        } else {
                            Log.e("Sign_up", "Failed to get user data for editing", task.getException());
                            Toast.makeText(Sign_up.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    // Update user data
    private void updateUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Collect user details from the input fields
            String firstName = firstNameEdit.getText().toString().trim();
            String lastName = lastNameEdit.getText().toString().trim();
            String phoneNumber = phoneEdit.getText().toString().trim();
            String country = countryEdit.getText().toString().trim();
            String email = emailEdit.getText().toString().trim();
            String password = passwordEdit.getText().toString().trim();

            // Create updated user object
            User user = new User(firstName, lastName, email, phoneNumber, country, password);

            // Update user data in Firestore
            db.collection("users")
                    .document(userId)
                    .set(user, SetOptions.merge())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Update password in Firebase Auth if changed
                            String newPassword = passwordEdit.getText().toString().trim();
                            if (!newPassword.isEmpty()) {
                                currentUser.updatePassword(newPassword)
                                        .addOnCompleteListener(passwordTask -> {
                                            if (passwordTask.isSuccessful()) {
                                                Log.d("Sign_up", "Password updated");
                                            } else {
                                                Log.e("Sign_up", "Error updating password", passwordTask.getException());
                                                Toast.makeText(Sign_up.this, "Failed to update password", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }

                            Toast.makeText(Sign_up.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();

                            // Return to ProfileActivity
                            Intent intent = new Intent(Sign_up.this, ProfileActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Log.e("Sign_up", "Failed to update user data", task.getException());
                            Toast.makeText(Sign_up.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    public void signup(String email, String password) {
        Log.d("Sign_up", "Signup method called");
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign-up success
                        Log.d("Sign_up", "Signup successful");
                        FirebaseUser user = mAuth.getCurrentUser();
                        if(user != null){
                            saveUserData(user.getUid());
                            Log.d("Sign_up", "User ID: " + user.getUid());
                        }

                    } else {
                        // Sign-up failure
                        Log.e("Sign_up", "Signup failed", task.getException());
                        Toast.makeText(Sign_up.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserData(String userId) {

        // Collect user details from the input fields
        String firstName = firstNameEdit.getText().toString().trim();
        String lastName = lastNameEdit.getText().toString().trim();
        String phoneNumber = phoneEdit.getText().toString().trim();
        String country = countryEdit.getText().toString().trim();
        String email = emailEdit.getText().toString().trim();
        String password = passwordEdit.getText().toString().trim();

        User user = new User(firstName, lastName, email, phoneNumber, country, password);



        // Save user data in firestore
        db.collection("users")
                .document(userId)
                .set(user, SetOptions.merge())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(Sign_up.this, "User registered successfully", Toast.LENGTH_SHORT).show();
                        //  add a transition to another activity here, e.g., using Intent

                        // Move to HomeActivity after successful sign-up
                        Intent intent = new Intent(Sign_up.this, Home.class);
                        startActivity(intent);
                        finish();

                        System.out.println(intent);

                    }else{
                        Log.e("Sign_up", "Failed to save user data", task.getException());
                        Toast.makeText(Sign_up.this, "Failed to save user data", Toast.LENGTH_SHORT).show();
                    }

                });

    }
    // User class to map user details
    public static class User {
        private String firstName;
        private String lastName;
        private String email;
        private String phoneNumber;
        private String country;
        private String password;

        public User() {

        }

        public User(String firstName, String lastName, String email, String phoneNumber, String country, String password) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.phoneNumber = phoneNumber;
            this.country = country;
            this.password = password;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public String getEmail() {
            return email;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public String getCountry() {
            return country;
        }

        public String getPassword() {
            return password;
        }
    }
}