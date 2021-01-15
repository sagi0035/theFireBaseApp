package com.example.thefirebaseapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.arch.core.executor.TaskExecutor;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.security.AccessController;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;


public class loginActivity extends AppCompatActivity {

    private Button logInButton, registerButton;
    private EditText userEmail, userPassword;
    private TextView forgotPassword;
    private RelativeLayout relativeLayout;
    private ImageView googleImageView , facebookImageView , phoneImageView;
    private LoginButton loginButtonForFacebook;

    String codeSent, phoneNumberAsString, code;

    ProgressDialog theProgressDialog;

    FirebaseAuth mAuth;

    private static final int RC_SIGN_IN = 1;
    private static final String TAG = "LoginActivity";
    private GoogleSignInClient mGoogleSignInClient;

    EditText phoneNumber;

    CountryCodePicker countryCodePicker;

    PhoneAuthProvider.ForceResendingToken resendToken;

    private CallbackManager callbackManager;
    private Boolean faceBookLogIn = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        theProgressDialog = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();

        logInButton = (Button) findViewById(R.id.logInButton);
        registerButton  = (Button) findViewById(R.id.registerButton);
        userEmail = (EditText) findViewById(R.id.logInEmail);
        userPassword = (EditText) findViewById(R.id.logInPassword);
        forgotPassword = (TextView) findViewById(R.id.forgotPassword);
        relativeLayout = (RelativeLayout) findViewById(R.id.theRelativeLayout);
        googleImageView = (ImageView) findViewById(R.id.googleclone);
        facebookImageView = (ImageView) findViewById(R.id.facebookclone);
        phoneImageView = (ImageView) findViewById(R.id.phone);




        // so here we will be dealing with an instance were the user has forgottent their password
        // we will provide the user with an opportunity to reset said password!
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText emailForPasswordReset = new EditText(view.getContext());
                AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(view.getContext());
                passwordResetDialog.setTitle("Password Reset");
                passwordResetDialog.setMessage("\nEnter your email so that we may send the email reset link\n");
                passwordResetDialog.setView(emailForPasswordReset);

                passwordResetDialog.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // so here we will go about sending the link
                        String mail = emailForPasswordReset.getText().toString();
                        mAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(loginActivity.this, "A password reset link has been sent to your email!", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(loginActivity.this, "There was some sort of an error here!", Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                });

                passwordResetDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });


                Dialog dialog = passwordResetDialog.create();
                dialog.show();
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.holo_green_dark);

            }
        });


        // now this will register the user
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUserToRegisterActivity();
            }
        });

        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();
            }
        });

        // now this will be to actually log the user in!
        logInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(TextUtils.isEmpty(userEmail.getText().toString()) || TextUtils.isEmpty(userPassword.getText().toString())) {
                    Toast.makeText(loginActivity.this, "Neither the email nor the password can be left empty!", Toast.LENGTH_SHORT).show();
                } else {
                    theProgressDialog.setTitle("Logging In");
                    theProgressDialog.setMessage("Please wait while we log you in!");
                    theProgressDialog.show();
                    theProgressDialog.setCanceledOnTouchOutside(false);
                    mAuth.signInWithEmailAndPassword(userEmail.getText().toString(),userPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            // so if successful we will log rthe user in as promised!
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (!user.isEmailVerified()) {
                                    Toast.makeText(loginActivity.this, "Account not yet verified", Toast.LENGTH_SHORT).show();
                                    theProgressDialog.dismiss();
                                } else {
                                    Toast.makeText(loginActivity.this, "A successful login!", Toast.LENGTH_SHORT).show();
                                    // it is very important to put the intent inside of the oncomplete listener so that we only move to the new intent if we were succesfully logged in when the task is coimpleted!
                                    Intent intenter = new Intent(loginActivity.this,MainActivity.class);
                                    startActivity(intenter);
                                    finish();
                                    theProgressDialog.dismiss();
                                }
                            } else {
                                Toast.makeText(loginActivity.this, "Incorrect Login Information\nPlease re-enter your email or password!", Toast.LENGTH_SHORT).show();
                                theProgressDialog.dismiss();
                            }
                        }
                    });
                }
            }
        });


        // so this will send to the function were the user logs in through their google account
        googleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createTheGoogle();
                signIn();
            }
        });


        // this will be sent to a function for logging through the phone
        phoneImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendVerificationCode(view);
            }
        });

        // so we initialize the sdk and callbackmanager and loginbutton
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        loginButtonForFacebook = (LoginButton) findViewById(R.id.login_button);


        facebookImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    // so if the user is logged in to facebook we will log them out
                    // so that the process of logging in is repeated
                    LoginManager.getInstance().logOut();
                } catch (Exception e) {
                    // do nothing
                }
                // so we will now set the boolean to true so as to signify the activity result
                faceBookLogIn = true;
                // so we will on the click of the textview have the loginButton be preesed
                // because we can only set the permission and registerCallback on the logionbutton
                loginButtonForFacebook.callOnClick();
                loginButtonForFacebook.setReadPermissions(Arrays.asList("email"));

                loginButtonForFacebook.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // we will also create a progressdialog
                        theProgressDialog.setTitle("Facebook sign-in!");
                        theProgressDialog.setMessage("Please wait while we sign you in!");
                        theProgressDialog.setCanceledOnTouchOutside(false);
                        theProgressDialog.show();
                        // and create the function to handle the token and actually sign in
                        handleFacebookToken(loginResult.getAccessToken());;
                    }

                    @Override
                    public void onCancel() {
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Toast.makeText(loginActivity.this, "Error!\nDid not login through facebook!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void handleFacebookToken(AccessToken accessToken) {
        // possible error here with having to provide a new authcredential as per the google one REMOVE IF FINE
        AuthCredential authCredentialTwo = FacebookAuthProvider.getCredential(accessToken.getToken());
        mAuth.signInWithCredential(authCredentialTwo).addOnCompleteListener(this,new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(loginActivity.this, "Login Success!!!", Toast.LENGTH_SHORT).show();
                    sendUserToMainActivity();
                    theProgressDialog.dismiss();
                } else {
                    Log.i("Your porblem is",task.getException().toString());
                    task.getException();
                    Toast.makeText(loginActivity.this, "Failed to login through facebook!!!", Toast.LENGTH_SHORT).show();
                    theProgressDialog.dismiss();
                }
            }
        });

    }


    private void createTheGoogle() {
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        // and so here only if the facebook login was a success i.e the boolean is true do we create the callback activity result
        if (faceBookLogIn) {
            callbackManager.onActivityResult(requestCode,resultCode,data);
        }

        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            // so if we have obtained the email we will set a progress bar
            theProgressDialog.setTitle("Google sign-in!");
            theProgressDialog.setMessage("Please wait while we sign you in!");
            theProgressDialog.setCanceledOnTouchOutside(false);
            theProgressDialog.show();
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                e.printStackTrace();
                Toast.makeText(this, "There was an error! " + e.getMessage(), Toast.LENGTH_SHORT).show();
                theProgressDialog.dismiss();
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            sendUserToMainActivity();
                            theProgressDialog.dismiss();
                            // so this will now be to prevent the sign in form being automatic i.e. you actually have to choose an account to sign/log in to!
                            mGoogleSignInClient.revokeAccess();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(loginActivity.this, "Not authenticated\nPlease try again!", Toast.LENGTH_SHORT).show();
                            theProgressDialog.dismiss();
                        }

                        // ...
                    }
                });
    }






    public void sendUserToMainActivity() {
        Intent intentFive = new Intent(loginActivity.this,MainActivity.class);
        intentFive.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intentFive);
        finish();
    }


    private void sendUserToRegisterActivity() {
        Intent intentTwo = new Intent(loginActivity.this,registerActivity.class);
        //intentTwo.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intentTwo);
    }


    public void hideKeyboard() {

        View view = this.getCurrentFocus();
        if (view!=null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(),0);
        }
    }

    // so we send the verification code
    private void sendVerificationCode(final View view) {


        final AlertDialog.Builder phoneDialog = new AlertDialog.Builder(view.getContext());
        LinearLayout linearLayout = new LinearLayout(view.getContext());
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);


        countryCodePicker = new CountryCodePicker(view.getContext());
        countryCodePicker.setDefaultCountryUsingNameCode("US");
        linearLayout.addView(countryCodePicker);
        phoneNumber = new EditText(view.getContext());
        linearLayout.addView(phoneNumber);
        phoneDialog.setTitle("Put on your phone number");

        phoneDialog.setMessage("\nThis will not be shared with anybody\n");
        phoneDialog.setView(linearLayout);

        //starhere

        phoneDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (TextUtils.isEmpty(phoneNumber.getText().toString())) {
                    Toast.makeText(loginActivity.this, "Phone number cannot be empty!", Toast.LENGTH_SHORT).show();
                } else {

                    theProgressDialog.setTitle("Phone sign-in!");
                    theProgressDialog.setMessage("Please wait while we send the code to your phone!");
                    theProgressDialog.setCanceledOnTouchOutside(false);
                    theProgressDialog.show();


                    phoneNumberAsString = countryCodePicker.getSelectedCountryCodeWithPlus() + phoneNumber.getText().toString();

                    PhoneAuthOptions options =
                            PhoneAuthOptions.newBuilder(mAuth)
                                    .setPhoneNumber(phoneNumberAsString)       // Phone number to verify
                                    .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                                    .setActivity(loginActivity.this)                 // Activity (for callback binding)
                                    .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                                    .build();
                    PhoneAuthProvider.verifyPhoneNumber(options);
                }

                /*
                final EditText theVerificationCode = new EditText(view.getContext());
                AlertDialog.Builder theMore = new AlertDialog.Builder(view.getContext());
                theMore.setTitle("Enter your verification code");
                theMore.setView(theVerificationCode);

                theMore.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        verifySignInCode(theVerificationCode.getText().toString());
                    }
                });

                theMore.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

                Dialog dialog = theMore.create();
                dialog.show();
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.holo_green_dark);
                */
            }



        });



        phoneDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });


        Dialog dialog = phoneDialog.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.holo_green_dark);


    }

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Toast.makeText(loginActivity.this, "Your verification message failed!\nDid you enter a correct number and country code?", Toast.LENGTH_SHORT).show();
            theProgressDialog.dismiss();
        }

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            // so this refers to the codesent to the user
            theProgressDialog.dismiss();
            Toast.makeText(loginActivity.this, "Your code been sent", Toast.LENGTH_SHORT).show();
            codeSent = s;
            resendToken = forceResendingToken;
            verifySignInCode();
        }
    };

    // and here is where we will verify of the code sent in was in fact correctly inputted
    private void verifySignInCode() {

        final EditText theVerificationCode = new EditText(loginActivity.this);
        AlertDialog.Builder theMore = new AlertDialog.Builder(loginActivity.this);
        theMore.setTitle("Enter your verification code");
        theMore.setView(theVerificationCode);

        theMore.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                theProgressDialog.setTitle("Verifying your sign-in code!");
                theProgressDialog.setMessage("Please wait while we sign you in!");
                theProgressDialog.setCanceledOnTouchOutside(false);
                theProgressDialog.show();

                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codeSent, theVerificationCode.getText().toString());
                signInWithPhoneAuthCredential(credential);
            }
        });

        theMore.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        Dialog dialog = theMore.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.holo_green_dark);



    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            theProgressDialog.dismiss();
                            sendUserToMainActivity();
                            Toast.makeText(loginActivity.this, "Success!!!", Toast.LENGTH_SHORT).show();
                        } else {
                            theProgressDialog.dismiss();
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                Toast.makeText(loginActivity.this, "Incorrect verification code!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    @Override
    protected void onStart() {

        // so this will now be for the sake of sending the user straight to the mainactivity should they have already been logged in
        // (though I think this may already be the case so check to make sure!)
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // so if there is no user he will have to gign in thus we send them to the signin
        if (currentUser != null && currentUser.isEmailVerified()) {
            sendUserToMainActivity();
        }

        super.onStart();
    }



    // thse final 2 functions may b unnecessarry and thus may be removed later on!
    public void sendUserToSignInActivity() {
        Intent intentFour = new Intent(loginActivity.this,loginActivity.class);
        intentFour.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intentFour);
        finish();
    }

    public void sendUserTosetUpActivity() {
        Intent intentThree = new Intent(loginActivity.this,setUpActivity.class);
        intentThree.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intentThree);
        finish();
    }


}