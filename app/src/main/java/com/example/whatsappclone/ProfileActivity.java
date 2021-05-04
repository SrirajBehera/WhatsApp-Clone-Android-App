package com.example.whatsappclone;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.whatsappclone.Models.Users;
import com.example.whatsappclone.databinding.ActivityProfileBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class ProfileActivity extends AppCompatActivity {

    ActivityProfileBinding binding;

    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    Uri sFile;

    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        dialog = new ProgressDialog(this);
        dialog.setMessage("Setting up your profile");
        dialog.setCancelable(false);

        binding.profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 33);
            }
        });

        binding.btnProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = binding.etProfileName.getText().toString();
                if (name.isEmpty()){
                    binding.etProfileName.setError("Please provide a name!");
                    return;
                }
                dialog.show();
                if (sFile != null){
                    final StorageReference reference = storage.getReference().child("profile_pictures").child(auth.getUid());
                    reference.putFile(sFile).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imageUrl = uri.toString();
                                    String phone = auth.getCurrentUser().getPhoneNumber();
                                    String uId = auth.getUid();

                                    FirebaseUser user = auth.getCurrentUser();

                                    Users users = new Users();
                                    users.setProfilePic(imageUrl);
                                    users.setUserName(name);
                                    users.setPhoneNumber("+911234567892");
                                    users.setUserId(uId);

                                    database.getReference().child("Users").child(uId)
                                            .setValue(users)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    dialog.dismiss();
                                                    Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            });
//                                    Toast.makeText(ProfileActivity.this, "Profile Picture Updated ", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                }
                else {
                    String phone = auth.getCurrentUser().getPhoneNumber();
                    String uId = auth.getUid();

                    Users users = new Users();
                    users.setUserName(name);
                    users.setProfilePic("Default Image");
                    users.setPhoneNumber(phone);
                    users.setUserId(uId);

                    database.getReference().child("Users").child(uId)
                            .setValue(users)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    dialog.dismiss();
                                    Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            });
//                                    Toast.makeText(ProfileActivity.this, "Profile Picture Updated ", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data.getData() != null) {
            sFile = data.getData();
            binding.profilePic.setImageURI(sFile);
        }
    }
}