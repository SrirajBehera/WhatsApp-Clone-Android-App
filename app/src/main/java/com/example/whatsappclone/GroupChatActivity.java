package com.example.whatsappclone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.example.whatsappclone.Adapters.ChatAdapter;
import com.example.whatsappclone.Adapters.GroupChatAdapter;
import com.example.whatsappclone.Models.MessageModel;
import com.example.whatsappclone.databinding.ActivityGroupChat2Binding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class GroupChatActivity extends AppCompatActivity {

    ActivityGroupChat2Binding binding;

    FirebaseDatabase database;
    FirebaseAuth auth;
    FirebaseStorage storage;

    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupChat2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        binding.backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading Image");
        dialog.setCancelable(false);

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final ArrayList<MessageModel> messageModels = new ArrayList<>();

        final String senderId = FirebaseAuth.getInstance().getUid();
        binding.userName.setText("Friends Group");

        final GroupChatAdapter adapter = new GroupChatAdapter(messageModels, this);
        binding.chatRecyclerView.setAdapter(adapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.chatRecyclerView.setLayoutManager(layoutManager);
        layoutManager.setStackFromEnd(true);

        database.getReference().child("Group chat")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messageModels.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                            MessageModel model = dataSnapshot.getValue(MessageModel.class);
                            model.setMessageId(snapshot.getKey());
                            messageModels.add(model);
                        }
                        adapter.notifyDataSetChanged();
                        binding.chatRecyclerView.smoothScrollToPosition(binding.chatRecyclerView.getAdapter().getItemCount());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        binding.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String message = binding.etMessage.getText().toString();
                final MessageModel model = new MessageModel(senderId, message);
                model.setTimestamp(new Date().getTime());

                binding.etMessage.setText("");
                binding.chatRecyclerView.smoothScrollToPosition(binding.chatRecyclerView.getAdapter().getItemCount());

                database.getReference().child("Group chat")
                        .push()
                        .setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                });
            }
        });

        binding.attachmentIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 25);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 25){
            if (data != null){
                if (data.getData() != null){
                    Uri selectedImage = data.getData();
                    Calendar calendar = Calendar.getInstance();
                    StorageReference referenceGrp = storage.getReference().child("public").child(calendar.getTimeInMillis() + "");
                    dialog.show();
                    referenceGrp.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            dialog.dismiss();
                            referenceGrp.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String filePath = uri.toString();

                                    String message = binding.etMessage.getText().toString();
                                    String senderId = auth.getUid();
                                    final MessageModel model = new MessageModel(senderId, message);
                                    model.setTimestamp(new Date().getTime());
                                    model.setMessage("photo");
                                    model.setImageUrl(filePath);
                                    binding.etMessage.setText("");

                                    database.getReference().child("Group chat")
                                            .push()
                                            .setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                        }
                                    });
                                }
                            });
                        }
                    });
                }
            }
        }
    }
}