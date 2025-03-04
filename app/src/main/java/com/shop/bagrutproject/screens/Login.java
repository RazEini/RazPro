package com.shop.bagrutproject.screens;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.shop.bagrutproject.R;
import com.shop.bagrutproject.models.Cart;
import com.shop.bagrutproject.models.User;
import com.shop.bagrutproject.services.AuthenticationService;
import com.shop.bagrutproject.services.DatabaseService;
import com.shop.bagrutproject.utils.SharedPreferencesUtil;

public class Login extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "LoginActivity";
    private AuthenticationService authenticationService;
    private DatabaseService databaseService;

    EditText etEmail, etPassword;
    Button btnLog;
    String email, pass;
    FirebaseAuth mAuth;
    private User user=null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        /// get the instance of the authentication service
        authenticationService = AuthenticationService.getInstance();
        /// get the instance of the database service
        databaseService = DatabaseService.getInstance();


        initViews();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLog = findViewById(R.id.btnSubmit);
        btnLog.setOnClickListener(this);
        mAuth = FirebaseAuth.getInstance();
    }

    public void btnBack2(View view) {
        Intent intent = new Intent(Login.this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        email = etEmail.getText().toString();
        pass = etPassword.getText().toString();
        loginUser(email,pass);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // השהייה קטנה כדי לוודא ש-Firebase מספיק להתעדכן
        new Handler().postDelayed(() -> {
            if (mAuth.getCurrentUser() != null) {
                Log.d("LoginCheck", "User is already logged in, redirecting...");
                Intent go = new Intent(getApplicationContext(), UserAfterLoginPage.class);
                startActivity(go);
                finish();
            }
        }, 1000);
    }

    // פונקציה להתנתקות
    public void logout() {
        // מנקה את ה-SharedPreferences כאשר המשתמש מתנתק
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear(); // מנקה את כל הנתונים
        editor.apply();

        // מבצע יציאה מהחשבון ב-Firebase
        mAuth.signOut();

        // מעביר את המשתמש למסך ההתחברות
        Intent go = new Intent(getApplicationContext(), Login.class);
        startActivity(go);
    }


    private void loginUser(String email, String password) {
        authenticationService.signIn(email, password, new AuthenticationService.AuthCallback<String>() {
            /// Callback method called when the operation is completed
            /// @param uid the user ID of the user that is logged in
            @Override
            public void onCompleted(String uid) {
                Log.d(TAG, "onCompleted: User logged in successfully");
                /// get the user data from the database


                databaseService.getUser(uid, new DatabaseService.DatabaseCallback<User>() {
                    @Override
                    public void onCompleted(User user) {
                        Log.d(TAG, "onCompleted: User data retrieved successfully");
                        /// save the user data to shared preferences
                        SharedPreferencesUtil.saveUser(Login.this, user);
                        /// Redirect to main activity and clear back stack to prevent user from going back to login screen


                        Intent mainIntent = new Intent(Login.this, UserAfterLoginPage.class);
                        /// Clear the back stack (clear history) and start the MainActivity
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(mainIntent);

                    }

                    @Override
                    public void onFailed(Exception e) {
                        Log.e(TAG, "onFailed: Failed to retrieve user data", e);
                        /// Show error message to user
                        etPassword.setError("Invalid email or password");
                        etPassword.requestFocus();
                        /// Sign out the user if failed to retrieve user data
                        /// This is to prevent the user from being logged in again
                        authenticationService.signOut();

                    }
                });


            }



            @Override
            public void onFailed(Exception e) {
                Log.e(TAG, "onFailed: Failed to log in user", e);
                /// Show error message to user
                etPassword.setError("Invalid email or password");
                etPassword.requestFocus();

            }
        });
    }

}
