package com.ez_store.ez_store.View;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.ez_store.ez_store.Internet.Connection;
import com.ez_store.ez_store.Model.LoadingDialog;
import com.ez_store.ez_store.Model.Person;
import com.ez_store.ez_store.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hbb20.CountryCodePicker;

import java.util.ArrayList;
import java.util.List;

import static com.ez_store.ez_store.Model.Crypto.encrypt;

public class RegisterActivity extends AppCompatActivity {

    private Connection connection;
    private long backPressTime;
    private EditText UserName,Password,ConfirmPassword,Email;
    private static LoadingDialog loadingDialog;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference reference;
    private CountryCodePicker codePicker ;
    private final List<EditText> EditTextList = new ArrayList<>();
    private RadioButton ownerButton, employeeButton;
    private final static String PERSON = "Person";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        connection = new Connection(this);
        UserName = (EditText) findViewById(R.id.Register_Name);
        Email = (EditText) findViewById(R.id.Register_Email);
        EditText phone = (EditText) findViewById(R.id.Phone_Text);
        ownerButton = (RadioButton)findViewById(R.id.AdminRadioButton);
        employeeButton = (RadioButton)findViewById(R.id.EmployeeRadioButton);
        Password = (EditText) findViewById(R.id.Register_Password_Text);
        ConfirmPassword = (EditText) findViewById(R.id.Register_Confirm_Password_Text);
        codePicker = (CountryCodePicker) findViewById(R.id.CountryCode);
        codePicker.registerCarrierNumberEditText(phone);
        loadingDialog = new LoadingDialog(RegisterActivity.this);
        firebaseAuth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();

        EditTextList.add(UserName);
        EditTextList.add(Email);
        EditTextList.add(phone);
        EditTextList.add(Password);
        EditTextList.add(ConfirmPassword);
    }

    public void Register(View view) {
        if(connection.isConnected) {
            if (!IsEmptyField(EditTextList)) {
                final String username = UserName.getText().toString().trim();
                final String email = Email.getText().toString().trim();
                final String password = Password.getText().toString().trim();
                final String phone = codePicker.getFullNumberWithPlus().replace(" ", "").replace("-","");
                if (IsMatched(Password, ConfirmPassword)) {
                    if(employeeButton.isChecked() || ownerButton.isChecked()) {
                        loadingDialog.start();
                        createEmail(username, password, phone, email);
                    }
                    else {
                        Toast.makeText(this, R.string.SelectRole, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
        else {
            Toast.makeText(RegisterActivity.this, getResources().getString(R.string.NoInternet), Toast.LENGTH_SHORT).show();
        }
    }

    private void createEmail(String username,String  password, String phone,String email){
        firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    sendVerificationEmail();
                    if(employeeButton.isChecked()) {
                        Person employee = new Person(firebaseAuth.getCurrentUser().getUid(), username, encrypt(password.getBytes()), phone, email.replace(".", ","),"Employee",false);
                        reference.child(PERSON).child(firebaseAuth.getCurrentUser().getUid()).setValue(employee);
                        Intent intent = new Intent(RegisterActivity.this, PhoneVerification.class);
                        intent.putExtra("phone", employee.getPhone());
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                    }
                    else if(ownerButton.isChecked()) {
                        Person owner = new Person(firebaseAuth.getCurrentUser().getUid(), username, encrypt(password.getBytes()), phone, email.replace(".", ","), "Owner",false);
                        reference.child(PERSON).child(firebaseAuth.getCurrentUser().getUid()).setValue(owner);
                        Intent intent = new Intent(RegisterActivity.this, PhoneVerification.class);
                        intent.putExtra("phone", owner.getPhone());
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    loadingDialog.dismiss();
                }
            }
        });
    }

    private void sendVerificationEmail() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(@NonNull Void aVoid) {
                Toast.makeText(RegisterActivity.this, "Verification Email Has been Sent.\n  Please Check Your Email", Toast.LENGTH_SHORT).show();
                loadingDialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                loadingDialog.dismiss();
            }
        });
    }

    public void backToSplash(View view) {
        if(connection.isConnected){
            Intent intent = new Intent(RegisterActivity.this, SplashActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
        }
        else {
            Toast.makeText(RegisterActivity.this,getResources().getString(R.string.NoInternet),Toast.LENGTH_LONG).show();
        }
    }

    private boolean IsEmptyField(List<EditText> editTextList){
        boolean bool = false;
        for(EditText item : editTextList) {
            if (TextUtils.isEmpty(item.getText())) {
                item.setError(getResources().getString(R.string.EmptyField));
                bool = true;
            }
        }
        return bool;
    }

    private boolean IsMatched(EditText password1, EditText password2){
        if(password1.getText().toString().trim().equals(password2.getText().toString().trim())){
            return true;
        }
        else {
            Toast.makeText(this, R.string.PasswordDoesntMatch, Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    @Override
    public void onBackPressed() {
        if(backPressTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
            finish();
            this.overridePendingTransition(R.anim.slide_up_out, R.anim.slide_up_in);
            return;
        }
        else{
            Toast.makeText(RegisterActivity.this,"Press back again to exit", Toast.LENGTH_LONG).show();
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