package com.example.tabianconsulting;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tabianconsulting.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;


public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private static final String DOMAIN_NAME = "gmail.com";
    private FirebaseAuth mAuth;


    //widgets
    private EditText mEmail, mPassword, mConfirmPassword;
    private Button mRegister;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mEmail = (EditText) findViewById(R.id.input_email);
        mPassword = (EditText) findViewById(R.id.input_password);
        mConfirmPassword = (EditText) findViewById(R.id.input_confirm_password);
        mRegister = (Button) findViewById(R.id.btn_register);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: attempting to register.");

                //check for null valued EditText fields
                if(!isEmpty(mEmail.getText().toString())
                        && !isEmpty(mPassword.getText().toString())
                        && !isEmpty(mConfirmPassword.getText().toString())) {

                    //check if the user password length is <= 6
                    if (!(mPassword.getText().toString().length() <= 6)) {


                        //check if user has a company email address
                        if (isValidDomain(mEmail.getText().toString())) {

                            //check if passwords match
                            if (doStringsMatch(mPassword.getText().toString(), mConfirmPassword.getText().toString())) {
                                registerNewEmail(mEmail.getText().toString(), mPassword.getText().toString());

                            } else {
                                Toast.makeText(RegisterActivity.this, "Passwords do not Match", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(RegisterActivity.this, "Please Register with Company Email", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Toast.makeText(RegisterActivity.this, "You must have a password length grater than 6", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(RegisterActivity.this, "You must fill out all the fields", Toast.LENGTH_SHORT).show();


                }
            }
        });

        hideSoftKeyboard();

    }

    private void registerNewEmail(final String email,String password){
        showDialog();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success " + mAuth.getCurrentUser().getUid() );
                            sendVerificationEmail();

                            User user = new User();
                            user.setName(email.substring(0,email.indexOf("@")));
                            user.setPhone("0916675432");
                            user.setProfile_image("");
                            user.setSecurity_level("1");
                            user.setUser_id(mAuth.getCurrentUser().getUid());

                            FirebaseDatabase.getInstance().getReference()
                                    .child(getString(R.string.dbnode_users))
                                    .child(mAuth.getCurrentUser().getUid())
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    //we want email verification so by default as soon as user register it will let them in so for the email verification
                                    //purpose we signOut user as soon as they get registered
                                    mAuth.signOut();
                                    redirectLoginScreen();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    //we want email verification so by default as soon as user register it will let them in so for the email verification
                                    //purpose we signOut user as soon as they get registered
                                    mAuth.signOut();
                                    redirectLoginScreen();
                                    Toast.makeText(RegisterActivity.this,"something went wrong",Toast.LENGTH_SHORT).show();
                                }
                            });

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Unable to register please try again.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        hideDialog();

                        // ...
                    }
                });
    }
    private void sendVerificationEmail(){
        final FirebaseUser user = mAuth.getCurrentUser();
        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        //I must always make sure if the user object is not null before calling any method on it
                        if (user != null){
                            if (task.isSuccessful()){
                                Toast.makeText(RegisterActivity.this, "Email Verification Sent",
                                        Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(RegisterActivity.this, "Could't Send Email Verification",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }

                    }
                });
    }

    /**
     * Returns True if the user's email contains '@tabian.ca'
     * @param email
     * @return
     */
    private boolean isValidDomain(String email){
        Log.d(TAG, "isValidDomain: verifying email has correct domain: " + email);
        String domain = email.substring(email.indexOf("@") + 1).toLowerCase();
        Log.d(TAG, "isValidDomain: users domain: " + domain);
        return domain.equals(DOMAIN_NAME);
    }

    /**
     * Redirects the user to the login screen
     */
    private void redirectLoginScreen(){
        Log.d(TAG, "redirectLoginScreen: redirecting to login screen.");

        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
    /**
     * Return true if @param 's1' matches @param 's2'
     * @param s1
     * @param s2
     * @return
     */
    private boolean doStringsMatch(String s1, String s2){
        return s1.equals(s2);
    }

    /**
     * Return true if the @param is null
     * @param string
     * @return
     */
    private boolean isEmpty(String string){
        return string.equals("");
    }


    private void showDialog(){
        mProgressBar.setVisibility(View.VISIBLE);

    }

    private void hideDialog(){
        if(mProgressBar.getVisibility() == View.VISIBLE){
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void hideSoftKeyboard(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }


}
















