package com.ez_store.ez_store.View.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.ez_store.ez_store.Internet.Connection;
import com.ez_store.ez_store.Model.LoadingDialog;
import com.ez_store.ez_store.Model.SessionManager;
import com.ez_store.ez_store.R;
import com.ez_store.ez_store.View.HomeActivity;
import com.ez_store.ez_store.View.LoginActivity;
import com.ez_store.ez_store.View.PhoneVerification;
import com.ez_store.ez_store.View.SellActivity;
import com.ez_store.ez_store.databinding.FragmentMyProfileBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import static com.ez_store.ez_store.Model.Crypto.decrypt;
import static com.ez_store.ez_store.Model.Crypto.encrypt;

public class MyProfileFragment extends Fragment implements View.OnClickListener {

    private FragmentMyProfileBinding binding;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private Connection connection;
    private static LoadingDialog loadingDialog;
    private static final String PERSON = "Person";
    private static final String EMPLOYEE = "Employee";
    private static final String OWNER = "Owner";
    private static final int PICK_IMAGE = 1;
    private Uri ImgUri;
    private String username,email,id,myPhone,image,role,password;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_my_profile, container, false);

        binding = DataBindingUtil.setContentView(getActivity(),R.layout.fragment_my_profile);
        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference(PERSON);
        firebaseAuth = FirebaseAuth.getInstance();
        connection = new Connection(getActivity());
        loadingDialog = new LoadingDialog(getActivity());
        binding.CountryCode.registerCarrierNumberEditText(binding.phoneEditText);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(PERSON);
        reference.child(firebaseAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                username = snapshot.child("userName").getValue(String.class);
                image = snapshot.child("imgUri").getValue(String.class);
                if(image!=null){
                    image = image.replace(",",".");
                }
                role = snapshot.child("role").getValue(String.class);
                password = decrypt(snapshot.child("password").getValue(String.class).getBytes());
                email = snapshot.child("email").getValue(String.class);
                id = snapshot.child("id").getValue(String.class);
                myPhone = snapshot.child("phone").getValue(String.class);

                fillWithInfo();

                binding.editEmail.setOnClickListener(MyProfileFragment.this);
                binding.editPassword.setOnClickListener(MyProfileFragment.this);
                binding.editPhone.setOnClickListener(MyProfileFragment.this);
                binding.editRole.setOnClickListener(MyProfileFragment.this);
                binding.editUsername.setOnClickListener(MyProfileFragment.this);
                binding.choosePic.setOnClickListener(MyProfileFragment.this);
                binding.back.setOnClickListener(MyProfileFragment.this);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(getActivity(),error.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
        return root;
    }

    @Override
    public void onClick(View v) {
        if(connection.isConnected) {
            if (v.getId() == binding.editEmail.getId()) {editEmail();}
            else if (v.getId() == binding.editPhone.getId()) {editPhone();}
            else if (v.getId() == binding.editUsername.getId()) {editUsername();}
            else if (v.getId() == binding.editPassword.getId()) {editPassword();}
            else if (v.getId() == binding.editRole.getId()) {editRole();}
            else if (v.getId() == binding.choosePic.getId()) {
                loadingDialog.start();
                openGallery();
            } else if (v.getId() == binding.back.getId()) {backToHomeActivity();}
        }else {
            Toast.makeText(getContext(), R.string.fui_no_internet, Toast.LENGTH_LONG).show();
        }
    }

    private void backToHomeActivity() {
        getActivity().finish();
        Intent intent = new Intent(getActivity(), HomeActivity.class);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    private void fillWithInfo() {
        try {
            if (!(image == null)) {
                Picasso.get().load(image).into(binding.profilePic);
            }

            binding.usernameText.setText(username);
            binding.emailText.setText(email.replace(",","."));
            binding.phoneText.setText(myPhone.substring(myPhone.length() - 9));
            binding.passwordText.setText(getPasswordWithHiddenParts(password));

            if (role.equals(EMPLOYEE)) {
                binding.roleText.setText(EMPLOYEE);
            } else if (role.equals(OWNER)) {
                binding.roleText.setText(OWNER);
            }
        }catch (Exception ex){
            Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void editRole() {
        if(binding.radioGroup.getVisibility() != View.VISIBLE) {
            binding.roleText.setVisibility(View.GONE);
            binding.radioGroup.setVisibility(View.VISIBLE);
            binding.editRole.setImageResource(R.drawable.save_icon);
        }else {
            if (binding.employeeRadioButton.isChecked()) {
                showAlert(databaseReference);
            } else {
                databaseReference.child(id).child("role").setValue(OWNER);
            }

            binding.roleText.setVisibility(View.VISIBLE);
            binding.radioGroup.setVisibility(View.GONE);
            binding.editRole.setImageResource(R.drawable.edit_stick);
        }
    }

    private void showAlert(DatabaseReference databaseReference) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Are You Sure That You Want To Be Employee ? \n You Will Lose All Data (Products / Employee / Bills (Dept + Cash))");

        builder.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int ID) {
                        databaseReference.child(id).child("role").setValue(EMPLOYEE);
                        databaseReference.child(id).child("Employee").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                if(snapshot.exists()) {
                                    for (DataSnapshot snap : snapshot.getChildren()) {
                                        databaseReference.child(snap.child("id").getValue(String.class)).child("Permissions").removeValue();
                                        databaseReference.child(snap.child("id").getValue(String.class)).child("employed").setValue(false);
                                    }
                                    databaseReference.child(id).child("Employee").removeValue();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                                Toast.makeText(getActivity(),error.getMessage(),Toast.LENGTH_LONG).show();
                            }
                        });
                        databaseReference.child(id).child("Products").removeValue();
                        databaseReference.child(id).child("Cash").removeValue();
                        databaseReference.child(id).child("Dept").removeValue();
                        databaseReference.child(id).child("Payment").removeValue();
                    }
                });

        builder.setNegativeButton("No",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener( new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.GREEN);
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED);
            }
        });
        alertDialog.show();
    }

    private void editPassword() {
        if(binding.passwordLayout.getVisibility() != View.VISIBLE) {
            binding.passwordText.setVisibility(View.GONE);
            binding.passwordLayout.setVisibility(View.VISIBLE);
            binding.passwordEditText.setHint(getPasswordWithHiddenParts(password));
            binding.editPassword.setImageResource(R.drawable.save_icon);
        }else {
            final String new_password = binding.passwordEditText.getText().toString().trim();
            final String confirm_password = binding.confirmPasswordEditText.getText().toString().trim();

            if (!new_password.isEmpty()) {
                if (new_password.equals(confirm_password)) {
                    databaseReference.child(id).child("password").setValue(encrypt(new_password.getBytes()));
                    firebaseAuth.getCurrentUser().updatePassword(new_password);
                } else {
                    Toast.makeText(getActivity(), R.string.PasswordDoesntMatch, Toast.LENGTH_LONG).show();
                }
            }

            binding.passwordText.setVisibility(View.VISIBLE);
            binding.passwordLayout.setVisibility(View.GONE);
            binding.editPassword.setImageResource(R.drawable.edit_stick);
        }
    }

    private void editUsername() {
        if(binding.usernameText.getVisibility() == View.VISIBLE) {
            binding.usernameText.setVisibility(View.GONE);
            binding.usernameEditText.setVisibility(View.VISIBLE);
            binding.usernameEditText.setHint(username);
            binding.editUsername.setImageResource(R.drawable.save_icon);
        }else {
            final String new_name = binding.usernameEditText.getText().toString().trim();
            if(!new_name.isEmpty()){
                databaseReference.child(id).child("userName").setValue(new_name);
            }

            binding.usernameText.setVisibility(View.VISIBLE);
            binding.usernameEditText.setVisibility(View.GONE);
            binding.editUsername.setImageResource(R.drawable.edit_stick);
        }
    }

    private void editPhone() {
        if(binding.phoneLayout.getVisibility() != View.VISIBLE) {
            binding.phoneText.setVisibility(View.GONE);
            binding.phoneLayout.setVisibility(View.VISIBLE);
            binding.phoneEditText.setHint(myPhone.substring(myPhone.length() - 9));
            binding.editPhone.setImageResource(R.drawable.save_icon);
        }else {
            final String new_phone = binding.phoneEditText.getText().toString().trim();
            if(!new_phone.isEmpty()){
                final String fullNumber = binding.CountryCode.getFullNumberWithPlus().replace(" ", "").replace("-", "");

                Intent intent = new Intent(getActivity(), PhoneVerification.class);
                intent.putExtra("Update","update");
                intent.putExtra("phone",fullNumber);
                startActivity(intent);
            }
            binding.phoneText.setVisibility(View.VISIBLE);
            binding.phoneLayout.setVisibility(View.GONE);
            binding.editPhone.setImageResource(R.drawable.edit_stick);
        }
    }

    private void editEmail() {
        if(binding.emailText.getVisibility() == View.VISIBLE) {
            binding.emailText.setVisibility(View.GONE);
            binding.emailEditText.setVisibility(View.VISIBLE);
            binding.emailEditText.setHint(email.replace(",","."));
            binding.editEmail.setImageResource(R.drawable.save_icon);
        }else {
            final String e_mail = binding.emailEditText.getText().toString().trim();
            if (!e_mail.isEmpty()) {
                firebaseAuth.getCurrentUser().updateEmail(e_mail);
                databaseReference.child(firebaseAuth.getCurrentUser().getUid()).child("email").setValue(e_mail.replace(".",","));
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
            }
            binding.emailText.setVisibility(View.VISIBLE);
            binding.emailEditText.setVisibility(View.GONE);
            binding.editEmail.setImageResource(R.drawable.edit_stick);
        }
    }

    private String getPasswordWithHiddenParts(String password) {
        String passwordWithHiddenParts = "";
        passwordWithHiddenParts += password.charAt(0);
        for (int i = 1 ; i<password.length()-1 ; i++){
            passwordWithHiddenParts += "*";
        }
        passwordWithHiddenParts += password.charAt(password.length()-1);
        return passwordWithHiddenParts;
    }

    private void openGallery() {
        if (connection.isConnected) {
            Intent gallery = new Intent();
            gallery.setType("image/*");
            gallery.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(gallery, PICK_IMAGE);
        } else {
            Toast.makeText(getActivity(), getResources().getString(R.string.NoInternet), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            ImgUri = data.getData();
            binding.profilePic.setImageURI(ImgUri);
            loadingDialog.dismiss();
            setImage(ImgUri);
        }
    }

    private void setImage(Uri imgUri) {
        ContentResolver cr = getContext().getContentResolver();
        MimeTypeMap map = MimeTypeMap.getSingleton();
        StorageReference ref = storageReference.child(System.currentTimeMillis() + "." + map.getExtensionFromMimeType(cr.getType(imgUri)));

        ref.putFile(imgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        try {
                            if(image != null) {
                                StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(image.replace(",", "."));
                                storageReference.delete();
                            }
                            databaseReference.child(id).child("imgUri").setValue(uri.toString().replace(".",","));
                            loadingDialog.dismiss();
                        } catch (Exception ex) {
                            Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_SHORT).show();
                            loadingDialog.dismiss();
                        }
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), "Failed", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        connection.registerNetworkCallback();
    }

    @Override
    public void onPause() {
        connection.unregisterNetworkCallback();
        super.onPause();
    }
}