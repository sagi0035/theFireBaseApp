package com.example.thefirebaseapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class registerActivity extends AppCompatActivity {

    EditText userEmail , userPassword , userConfirmPassword;
    Button createAccount;
    RelativeLayout relativeLayout;

    // this will be to create a progress dialog
    ProgressDialog loadingBar;

    FirebaseAuth mAuth;
    boolean allowCreation = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        // now we get the instance of firebase
        mAuth = FirebaseAuth.getInstance();

        userEmail = (EditText) findViewById(R.id.registerEmail);
        userPassword = (EditText) findViewById(R.id.registerPassword);
        userConfirmPassword = (EditText) findViewById(R.id.registerConfirmPassword);
        createAccount = (Button) findViewById(R.id.registerButton);
        relativeLayout = (RelativeLayout) findViewById(R.id.theRelLay);

        loadingBar = new ProgressDialog(this);

        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();
            }
        });

        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreateNewAccount();
            }
        });
    }

    @Override
    protected void onStart() {

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser!= null) {
            allowCreation = false;
        }

        // so if there is no user he will have to sign in thus we send them to the signin
        if (currentUser != null && currentUser.isEmailVerified()) {
            Intent intenter = new Intent(registerActivity.this,MainActivity.class);
            startActivity(intenter);
            finish();
        }

        super.onStart();
    }

    // we will try create an an account
    private void CreateNewAccount() {
        String email = userEmail.getText().toString();
        String password = userPassword.getText().toString();
        String confirmPassword = userConfirmPassword.getText().toString();

        // if anything is left empty we will toast an error message
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Email cannot be empty!", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Password cannot be empty!", Toast.LENGTH_SHORT).show();
        } else if(password.length() < 5) {
            Toast.makeText(this, "Password must be at least 5 characters long!", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Please confirm your password!", Toast.LENGTH_SHORT).show();
        } else if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Your passwords do not match! Please try again!", Toast.LENGTH_SHORT).show();
        } else {
            // so here we will now place the loadingbar to signify that we are loading the registration of the user
            loadingBar.setTitle("Creating new account...");
            loadingBar.setMessage("Please wait while we create your account!");
            loadingBar.show();
            // now this will set the basis the loadingbar to only be removed after everything has finished loading!
            loadingBar.setCanceledOnTouchOutside(false);
            if (allowCreation) {
                mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // now in this case we will simply go about registering the user!
                        final FirebaseUser user = mAuth.getCurrentUser();
                        // so here we will first check if the task of creating a user was succesful
                        if (task.isSuccessful()) {

                            if (task.isSuccessful()) {
                                // and we will get email verification as an extra measure
                                user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(registerActivity.this, "Verification email has been sent!\nPlease confirm!", Toast.LENGTH_SHORT).show();
                                        sendUserToLogInActivity();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(registerActivity.this, "Email not sent!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }


                            Toast.makeText(registerActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        } else {
                            // this will on the other hand be in the case of a failure!
                            String message = task.getException().getMessage();
                            Toast.makeText(registerActivity.this, "Error Occurred: " + message, Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                });

                allowCreation = false;
            } else {
                final FirebaseUser user = mAuth.getCurrentUser();
                if (user.isEmailVerified()) {
                    // so now what we also want to do is send the user to setup once the task is successful!
                    Toast.makeText(this, "Please sign in to complete your account!", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                    sendUserToLogInActivity();
                } else {
                    if (!user.isEmailVerified()) {
                        Toast.makeText(this, "Your account is still not verified!\nPlease confirm through your email and then sign-in to complete", Toast.LENGTH_SHORT).show();
                    }
                    sendUserToLogInActivity();
                    loadingBar.dismiss();
                }
            }

        }



    }

    private void sendUserToLogInActivity() {
        Intent setUpIntent = new Intent(registerActivity.this,loginActivity.class);

        startActivity(setUpIntent);
        finish();
    }

    private void sendUserToSetUpActivity() {
        Intent setUpIntent = new Intent(registerActivity.this,setUpActivity.class);
        setUpIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setUpIntent);
        finish();
    }


    public void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view!=null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(),0);
        }
    }
}