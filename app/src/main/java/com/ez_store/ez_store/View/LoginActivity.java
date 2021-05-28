package com.ez_store.ez_store.View;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.ez_store.ez_store.Internet.Connection;
import com.ez_store.ez_store.Model.LoadingDialog;
import com.ez_store.ez_store.Model.SessionManager;
import com.ez_store.ez_store.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hbb20.CountryCodePicker;

import java.util.HashMap;

import static com.ez_store.ez_store.Model.Crypto.decrypt;
import static com.ez_store.ez_store.Model.Crypto.encrypt;

public class LoginActivity extends AppCompatActivity {

    private Connection connection;
    private long backPressTime;
    private EditText Phone, Password, Email;
    private CountryCodePicker codePicker;
    private TextInputLayout passwordWithEye;
    private LinearLayout phoneWithCodePicker;
    private CheckBox RememberMe;
    private AppCompatButton LoginByButton;
    private static LoadingDialog loadingDialog;
    private SessionManager session;
    private FirebaseAuth auth;
    private final static String LOGIN = "login";
    private final static String PERSON = "Person";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        connection = new Connection(this);
        Phone = (EditText) findViewById(R.id.Phone_Text);
        passwordWithEye = (TextInputLayout) findViewById(R.id.Login_Password);
        Email = (EditText) findViewById(R.id.Login_Email);
        Password = (EditText) findViewById(R.id.Login_Password_Text);
        codePicker = (CountryCodePicker) findViewById(R.id.CountryCode);
        codePicker.registerCarrierNumberEditText(Phone);
        phoneWithCodePicker = (LinearLayout) findViewById(R.id.Phone_Number);
        RememberMe = (CheckBox) findViewById(R.id.RememberMe);
        LoginByButton = (AppCompatButton) findViewById(R.id.LoginByIcon);
        loadingDialog = new LoadingDialog(LoginActivity.this);
        auth = FirebaseAuth.getInstance();
        session = new SessionManager(LoginActivity.this, SessionManager.SESSION_REMEMBER_ME);

        fillWithData(session);
    }

    public void login(View view) {
        final String phone = codePicker.getFullNumberWithPlus().replace(" ", "").replace("-", "");
        final String password = Password.getText().toString().trim();
        final String email = Email.getText().toString().trim();
        if (connection.isConnected) {
            if (Email.getVisibility() == View.VISIBLE) {
                if (!IsEmptyField(email,Email) && !IsEmptyField(password,Password)) {
                    loadingDialog.start();
                    RememberME();
                    openAccountByEmail(email, password);
                }
            } else {
                if (!IsEmptyField(phone,Phone)) {
                    loadingDialog.start();
                    openAccountByPhone(phone);
                }
            }
        } else {
            Toast.makeText(LoginActivity.this, getResources().getString(R.string.NoInternet), Toast.LENGTH_LONG).show();
        }
    }

    private void openAccountByPhone(String phone) {
        Intent intent = new Intent(LoginActivity.this, PhoneVerification.class);
        intent.putExtra("login" ,LOGIN);
        intent.putExtra("phone" ,phone);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
        loadingDialog.dismiss();
    }

    private void openAccountByEmail(String email, String password) {
        auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    if (task.isSuccessful()) {
                        if (auth.getCurrentUser().isEmailVerified()) {
                            String userID = auth.getCurrentUser().getUid();
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference(PERSON);
                            reference.child(userID).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    Toast.makeText(LoginActivity.this, "Logged in Successfully", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                                    overridePendingTransition(R.anim.slide_up_in, R.anim.slide_up_out);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(LoginActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                        } else {
                            showAlertDialog();
                        }
                    }
                } else {
                    Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
                loadingDialog.dismiss();
            }
        });
    }

    private void sendVerificationEmail() {
        FirebaseUser user = auth.getCurrentUser();
        user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(@NonNull Void aVoid) {
                Toast.makeText(LoginActivity.this, "Verification Email Has been Sent.\n  Please Check Your Email", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean IsEmptyField(String input , EditText editText){
        if(input.isEmpty()) {
            editText.setError(getResources().getString(R.string.EmptyField));
            editText.setFocusable(true);
            return true;
        }
        return false;
    }

    private void RememberME() {
        if(RememberMe.isChecked()) {
            String email = Email.getText().toString().trim();
            String password = Password.getText().toString().trim();
            SessionManager session = new SessionManager(LoginActivity.this, SessionManager.SESSION_REMEMBER_ME);
            session.createRememberMeSession(email,password);
        }
    }

    private void fillWithData(SessionManager session){
        if (session.checkRememberMe()) {
            HashMap<String, String> map = session.getRememberMeSession();
            Email.setText(map.get(SessionManager.KEY__SESSION_EMAIL));
            Password.setText(map.get(SessionManager.KEY_SESSION_PASSWORD));
        }
    }

    private void showAlertDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(LoginActivity.this);
        alertDialog.setTitle("Email Verification");
        alertDialog.setMessage("Your email doesn't verified , You should verify your email before login.");
        alertDialog.setPositiveButton("Resend Email", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendVerificationEmail();
            }
        });
        alertDialog.setNegativeButton("Change Email", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
               changeEmailAlertDialog();
                dialog.dismiss();
            }
        });
        loadingDialog.dismiss();
        AlertDialog dialog = alertDialog.create();
        dialog.show();
    }

    private void changeEmailAlertDialog(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(LoginActivity.this);
        alertDialog.setTitle("New Email");
        final EditText input = new EditText(LoginActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialog.setView(input);
        alertDialog.setIcon(R.drawable.useemail);

        alertDialog.setPositiveButton("Change Email", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                auth.getCurrentUser().updateEmail(input.getText().toString().trim());
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(PERSON);
                databaseReference.child(auth.getCurrentUser().getUid()).child("email").setValue(input.getText().toString().trim());
                sendVerificationEmail();
            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        loadingDialog.dismiss();
        AlertDialog dialog = alertDialog.create();
        dialog.show();
    }

    public void backToSplash(View view) {
        if(connection.isConnected){
            Intent intent = new Intent(LoginActivity.this, SplashActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
        }
        else {
            Toast.makeText(LoginActivity.this,getResources().getString(R.string.NoInternet),Toast.LENGTH_LONG).show();
        }
    }

    public void loginBy(View view) {
        if(connection.isConnected){
            if(Email.getVisibility()==View.VISIBLE) {
                phoneWithCodePicker.setVisibility(View.VISIBLE);
                Email.setVisibility(View.GONE);
                RememberMe.setVisibility(View.GONE);
                passwordWithEye.setVisibility(View.GONE);
                LoginByButton.setBackgroundResource(R.drawable.use_email_animation);
            }else {
                phoneWithCodePicker.setVisibility(View.GONE);
                Email.setVisibility(View.VISIBLE);
                RememberMe.setVisibility(View.VISIBLE);
                passwordWithEye.setVisibility(View.VISIBLE);
                LoginByButton.setBackgroundResource(R.drawable.use_phone_animation);
            }
        }
        else {
            Toast.makeText(LoginActivity.this,getResources().getString(R.string.NoInternet),Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        if(backPressTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
            finish();
            this.overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
            return;
        }
        else{
            Toast.makeText(LoginActivity.this,"Press back again to exit", Toast.LENGTH_LONG).show();
        }
        backPressTime = System.currentTimeMillis();
    }

    @Override
    protected void onResume() {
        super.onResume();
        connection.registerNetworkCallback();
    }

    @Override
    protected void onPause() {
        connection.unregisterNetworkCallback();
        super.onPause();
    }
}