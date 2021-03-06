package com.example.journal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import Util.JournalApi;

public class CreateAccountActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener; //Listens to events firebase is firing
    private FirebaseUser currentUser; //fetches current user logged in

    //Firestore connection
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private CollectionReference collectionReference = db.collection("Users");

    private EditText emailEditText;
    private EditText passwordEditText;
    private ProgressBar progressBar;
    private Button createAccountButton;
    private EditText usernameEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        firebaseAuth = FirebaseAuth.getInstance();

        createAccountButton = findViewById(R.id.email_register_button);
        progressBar = findViewById(R.id.create_act_progress);
        emailEditText = findViewById(R.id.email_account);
        passwordEditText = findViewById(R.id.password_account);
        usernameEditText = findViewById(R.id.username_account);

       authStateListener = new FirebaseAuth.AuthStateListener() {
           @Override
           public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
               currentUser = firebaseAuth.getCurrentUser();

               if(currentUser != null){
                   //user is already logged in

               }else{
                   //no user yet

               }
           }
       };

       createAccountButton.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               if(!TextUtils.isEmpty(emailEditText.getText().toString()) &&
                       !TextUtils.isEmpty(passwordEditText.getText().toString()) &&
                       !TextUtils.isEmpty(usernameEditText.getText().toString())){

                   String email = emailEditText.getText().toString().trim();
                   String password = passwordEditText.getText().toString().trim();
                   String username = usernameEditText.getText().toString().trim();

                   createUserEmailAccount(email, password, username);
               }else{
                   Toast.makeText(CreateAccountActivity.this, "Empty Fields Not Allowed", Toast.LENGTH_SHORT).show();
               }
           }
       });

    }

    private void createUserEmailAccount(String email, String password, String username){
        if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(username)){

            progressBar.setVisibility(View.VISIBLE);

            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                //we take user to the AddJournalActivity
                                currentUser = firebaseAuth.getCurrentUser();
                                String currentUserId = currentUser.getUid();

                                //Create a user map so we can create a user in user collection
                                Map<String, String> userObject = new HashMap<>();
                                userObject.put("userId", currentUserId);
                                userObject.put("username", username);

                                //save to our firestore db
                                collectionReference.add(userObject)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        documentReference.get()
                                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                        if(task.getResult().exists()){
                                                            progressBar.setVisibility(View.INVISIBLE);
                                                            String name = task.getResult()
                                                                    .getString("username");

                                                            JournalApi journalApi = JournalApi.getInstance();
                                                            journalApi.setUserId(currentUserId);
                                                            journalApi.setUsername(username);

                                                            Intent intent = new Intent(CreateAccountActivity.this, PostJournalActivity.class);
                                                            intent.putExtra("username", name);
                                                            intent.putExtra("userId", currentUserId);
                                                            startActivity(intent);
                                                        }else{
                                                            progressBar.setVisibility(View.INVISIBLE);
                                                        }
                                                    }
                                                });
                                    }
                                })
                                        .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                    }
                                });

                            }else{
                                //Something went wrong
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
        }else{

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }
}