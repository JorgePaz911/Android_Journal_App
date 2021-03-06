package com.example.journal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

import Util.JournalApi;

public class LoginActivity extends AppCompatActivity {

    private Button loginButton;
    private Button registerButton;
    private AutoCompleteTextView email;
    private EditText password;
    private ProgressBar progressBar;


    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    private FirebaseFirestore db  = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        firebaseAuth = FirebaseAuth.getInstance();

        loginButton = findViewById(R.id.email_sign_in_button);
        registerButton = findViewById(R.id.email_register_button);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        progressBar = findViewById(R.id.login_progress);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, CreateAccountActivity.class));
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                loginEmailPasswordUser(email.getText().toString().trim(), password.getText().toString().trim());
            }
        });

    }

    private void loginEmailPasswordUser(String email, String password) {

        progressBar.setVisibility(View.VISIBLE);
        if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){

            //sign in first
            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            String currentUserId = user.getUid();

                            collectionReference
                                    .whereEqualTo("userId", currentUserId)
                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                            if(error != null){

                                            }else{
                                                if(!value.isEmpty()){
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                    for(QueryDocumentSnapshot snap : value){ //is for necessary?
                                                        JournalApi journalApi = JournalApi.getInstance();
                                                        journalApi.setUsername(snap.getString("username"));
                                                        journalApi.setUserId(snap.getString("userId"));

                                                        startActivity(new Intent(LoginActivity.this, journalListActivity.class));
                                                    }
                                                }
                                            }

                                        }
                                    });

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    });



        }else{
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(this, "Please Enter Email and Password", Toast.LENGTH_SHORT).show();
        }
    }
}