package com.shop.bagrutproject.screens;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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
import com.google.firebase.database.FirebaseDatabase;
import com.shop.bagrutproject.R;
import com.shop.bagrutproject.models.Cart;
import com.shop.bagrutproject.models.Item;
import com.shop.bagrutproject.models.User;
import com.shop.bagrutproject.services.DatabaseService;

import java.util.ArrayList;
import java.util.UUID;


public class Register extends AppCompatActivity implements View.OnClickListener{

    EditText etFName, etLName, etPhone, etEmail, etPass;
    Button btnReg;
    String fName,lName, phone, email, pass;

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    public static final String MyPREFERENCES = "MyPrefs" ;
    SharedPreferences sharedpreferences;

    DatabaseService databaseService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        databaseService=DatabaseService.getInstance();


        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        init_views();


        // Write a message to the database
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Users");

        mAuth = FirebaseAuth.getInstance();

    }

    private void init_views(){
        btnReg=findViewById(R.id.btnSubmit);
        etFName=findViewById(R.id.etFname);
        etLName=findViewById(R.id.etLname);
        etPhone=findViewById(R.id.etPhone);
        etEmail=findViewById(R.id.etEmail);
        etPass=findViewById(R.id.etPassword);
        btnReg.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        fName=etFName.getText().toString();
        lName=etLName.getText().toString();
        phone=etPhone.getText().toString();
        email=etEmail.getText().toString();
        pass=etPass.getText().toString();

        //check if registration is valid
        Boolean isValid=true;
        if (fName.length()<2){
            Toast.makeText(Register.this,"שם פרטי קצר מדי", Toast.LENGTH_LONG).show();
            isValid = false;
        }
        if (lName.length()<2){
            Toast.makeText(Register.this,"שם משפחה קצר מדי", Toast.LENGTH_LONG).show();
            isValid = false;
        }
        if (phone.length()<9||phone.length()>10){
            Toast.makeText(Register.this,"מספר הטלפון לא תקין", Toast.LENGTH_LONG).show();
            isValid = false;
        }

        if (!email.contains("@")){
            Toast.makeText(Register.this,"כתובת האימייל לא תקינה", Toast.LENGTH_LONG).show();
            isValid = false;
        }
        if(pass.length()<6){
            Toast.makeText(Register.this,"הסיסמה קצרה מדי", Toast.LENGTH_LONG).show();
            isValid = false;
        }
        if(pass.length()>20){
            Toast.makeText(Register.this,"הסיסמה ארוכה מדי", Toast.LENGTH_LONG).show();
            isValid = false;
        }

        if (isValid==true){

            mAuth.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d("TAG", "createUserWithEmail:success");
                                FirebaseUser fireuser = mAuth.getCurrentUser();
                                User newUser=new User(fireuser.getUid(), email, pass, fName, lName, phone);
                                myRef.child(fireuser.getUid()).setValue(newUser);
                                SharedPreferences.Editor editor = sharedpreferences.edit();

                                editor.putString("email", email);
                                editor.putString("password", pass);

                                // מאתחל את העגלה
                             Cart   cart = new Cart(UUID.randomUUID().toString(), new ArrayList<Item>());

                                databaseService.createNewCart(cart,newUser.getUid(), new DatabaseService.DatabaseCallback<Void>() {
                                    @Override
                                    public void onCompleted(Void object) {

                                    }

                                    @Override
                                    public void onFailed(Exception e) {

                                    }
                                });
                                editor.commit();
                                Intent goLog=new Intent(getApplicationContext(), Login.class);
                                startActivity(goLog);


                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w("TAG", "createUserWithEmail:failure", task.getException());
                                Toast.makeText(Register.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();

                            }

                            // ...
                        }
                    });
        }


    }
}
