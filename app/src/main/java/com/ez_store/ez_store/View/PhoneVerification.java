package com.ez_store.ez_store.View;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.chaos.view.PinView;
import com.ez_store.ez_store.Internet.Connection;
import com.ez_store.ez_store.Model.LoadingDialog;
import com.ez_store.ez_store.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

public class PhoneVerification extends AppCompatActivity {
    private AppCompatButton sendButton;
    private TextView message , phone_message;
    private PinView pinView;
    private static LoadingDialog loadingDialog;
    private FirebaseAuth firebaseAuth;
    private String phoneNumber ,Login ,update ,email;
    private final static String LOGIN = "login";
    private final static String PERSON = "Person";
    private String codeBySystem;
    private long backPressTime;
    private Connection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_verification);

        initialize();
    }

    private void initialize() {
        connection = new Connection(PhoneVerification.this);
        message = (TextView) findViewById(R.id.message1);
        phone_message = (TextView) findViewById(R.id.user_phone);
        sendButton = (AppCompatButton) findViewById(R.id.Send_Button);
        pinView = (PinView) findViewById(R.id.pinView);
        loadingDialog  = new LoadingDialog(PhoneVerification.this);
        firebaseAuth = FirebaseAuth.getInstance();

        phoneNumber = getIntent().getStringExtra("phone");
        Login = getIntent().getStringExtra("login");
        update = getIntent().getStringExtra("Update");
        email = getIntent().getStringExtra("email");


        phone_message.setText(phoneNumber);
        message.setVisibility(View.GONE);
        phone_message.setVisibility(View.GONE);
    }

    private void sendVerificationCode(String phoneNumber) {
        try {
            if (connection.isConnected) {
                loadingDialog.start();
                PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                        .setPhoneNumber(phoneNumber)                    // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                        // Activity (for callback binding)
                        .setCallbacks(mCallbacks)                 // OnVerificationStateChangedCallbacks
                        .build();
                PhoneAuthProvider.verifyPhoneNumber(options);
            } else {
                Toast.makeText(PhoneVerification.this, getResources().getString(R.string.NoInternet), Toast.LENGTH_SHORT).show();
            }
        }
        catch (Exception ex){
            Toast.makeText(PhoneVerification.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            Toast.makeText(PhoneVerification.this,"Code sent" , Toast.LENGTH_LONG).show();
            codeBySystem = s; // s is the code
            loadingDialog.dismiss();
            phone_message.setVisibility(View.VISIBLE);
            message.setVisibility(View.VISIBLE);
            sendButton.setClickable(false);
            sendButton.setBackgroundResource(R.drawable.loginbuttononclick);
        }

        @Override
        public void onCodeAutoRetrievalTimeOut(@NonNull String s) {
            super.onCodeAutoRetrievalTimeOut(s);
            sendButton.setClickable(true);
            message.setVisibility(View.GONE);
            phone_message.setVisibility(View.GONE);
            sendButton.setBackgroundResource(R.drawable.login_button_animation);
            sendButton.setText("Send Code");
        }

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            String code = phoneAuthCredential.getSmsCode();
            pinView.setText(code);
            whatToDo(code);
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Toast.makeText(PhoneVerification.this,e.getMessage(),Toast.LENGTH_SHORT).show();
        }
    };

    private void whatToDo(String code) {
        try {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codeBySystem, code);
            if (Login!=null && Login.equals(LOGIN)) {
                signInWithPhoneAuthCredential(credential);
            } else if(update!=null){
                updateInfo(credential);
            }else {
                linkWithEmail(credential);
            }
        }catch (Exception ex){
            Toast.makeText(PhoneVerification.this,ex.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    private void updateInfo(PhoneAuthCredential credential) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(PERSON);
        databaseReference.child(firebaseAuth.getCurrentUser().getUid()).child("phone").setValue(phoneNumber);
        firebaseAuth.getCurrentUser().updatePhoneNumber(credential);

        Intent intent = new Intent(PhoneVerification.this, HomeActivity.class);
        finish();
        startActivity(intent);
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
        Toast.makeText(PhoneVerification.this, getResources().getString(R.string.verificationCompleted), Toast.LENGTH_SHORT).show();
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            if (firebaseAuth.getCurrentUser().isEmailVerified()) {
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference(PERSON);
                                reference.child(firebaseAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if(snapshot.exists()) {
                                            loadingDialog.start();
                                            finish();
                                            startActivity(new Intent(PhoneVerification.this, HomeActivity.class));
                                            overridePendingTransition(R.anim.slide_up_in, R.anim.slide_up_out);
                                            Toast.makeText(PhoneVerification.this, getResources().getString(R.string.verificationCompleted), Toast.LENGTH_SHORT).show();
                                            loadingDialog.dismiss();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(PhoneVerification.this,error.getMessage(),Toast.LENGTH_LONG).show();
                                    }
                                });
                            } else {
                                showAlertDialog();
                            }
                        } else {
                            Toast.makeText(PhoneVerification.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(PhoneVerification.this, LoginActivity.class);
                            finish();
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
                        }
                    }
                });
    }

    private void sendVerificationEmail() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(@NonNull Void aVoid) {
                Toast.makeText(PhoneVerification.this, "Verification Email Has been Sent.\n  Please Check Your Email", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(PhoneVerification.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAlertDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(PhoneVerification.this);
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
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(PhoneVerification.this);
        alertDialog.setTitle("New Email");
        final EditText input = new EditText(PhoneVerification.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialog.setView(input);
        alertDialog.setIcon(R.drawable.useemail);

        alertDialog.setPositiveButton("Change Email", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                firebaseAuth.getCurrentUser().updateEmail(input.getText().toString().trim());
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(PERSON);
                databaseReference.child(firebaseAuth.getCurrentUser().getUid()).child("email").setValue(input.getText().toString().trim());
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

    private void linkWithEmail(PhoneAuthCredential credential) {
        try{
            firebaseAuth.getCurrentUser().linkWithCredential(credential).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(@NonNull AuthResult authResult) {
                    Intent intent = new Intent(PhoneVerification.this, LoginActivity.class);
                    finish();
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
                    Toast.makeText(PhoneVerification.this, getResources().getString(R.string.verificationCompleted), Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(PhoneVerification.this,e.getMessage(),Toast.LENGTH_LONG).show();
                }
            });
        }catch (Exception ex) {
            Toast.makeText(PhoneVerification.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void Verify(View view) {
        if(connection.isConnected) {
            String Code = pinView.getText().toString().trim();
            whatToDo(Code);
        }
        else {
            Toast.makeText(PhoneVerification.this,getResources().getString(R.string.NoInternet),Toast.LENGTH_SHORT).show();
        }
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

    public void sendButton(View view) {
        sendButton.setText(R.string.Resend);
        sendButton.setClickable(false);
        sendButton.setBackgroundResource(R.drawable.loginbuttononclick);
        sendVerificationCode(phoneNumber);
    }

    public void wrongPhone(View view) {
        if(connection.isConnected) {
            if(Login != null){
                Intent intent = new Intent(PhoneVerification.this, LoginActivity.class);
                finish();
                startActivity(intent);
                overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
            }else if(update == "update"){
                Intent intent = new Intent(PhoneVerification.this, HomeActivity.class);
                finish();
                startActivity(intent);
                overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
            }else {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Person");
                reference.child(firebaseAuth.getUid()).removeValue();
                firebaseAuth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Intent intent = new Intent(PhoneVerification.this, RegisterActivity.class);
                            finish();
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
                        } else {
                            Toast.makeText(PhoneVerification.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }else {
            Toast.makeText(PhoneVerification.this,getResources().getString(R.string.NoInternet),Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        if(backPressTime + 2000 > System.currentTimeMillis()) {
            if(Login==null && update==null) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Person");
                reference.child(firebaseAuth.getUid()).removeValue();
                firebaseAuth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            finish();
                            PhoneVerification.this.overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
                        } else {
                            Toast.makeText(PhoneVerification.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            super.onBackPressed();
            return;
        }
        else{
            Toast.makeText(PhoneVerification.this,"Press back again to exit", Toast.LENGTH_LONG).show();
        }
        backPressTime = System.currentTimeMillis();
    }
}