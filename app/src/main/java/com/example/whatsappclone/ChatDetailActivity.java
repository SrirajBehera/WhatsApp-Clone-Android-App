package com.example.whatsappclone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.whatsappclone.Adapters.ChatAdapter;
import com.example.whatsappclone.Models.MessageModel;
import com.example.whatsappclone.databinding.ActivityChatDetailBinding;
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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class ChatDetailActivity extends AppCompatActivity {

    ActivityChatDetailBinding binding;

    FirebaseDatabase database;
    FirebaseAuth auth;
    FirebaseStorage storage;

    ProgressDialog dialog;

    String senderRoom, receiverRoom;
    String senderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        senderId = auth.getUid(); // used final keyword to make the variable global
        String receiverId = getIntent().getStringExtra("userId");
        String userName = getIntent().getStringExtra("userName");
        String profilePic = getIntent().getStringExtra("profilePic");

//        getSupportActionBar().setTitle(userName);

        binding.userName.setText(userName);
        Picasso.get().load(profilePic).placeholder(R.drawable.ic_avatar).into(binding.profileImage);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading Image");
        dialog.setCancelable(false);

        binding.backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(ChatDetailActivity.this, MainActivity.class);
//                startActivity(intent);
                finish();
            }
        });

        senderRoom = senderId + receiverId;
        receiverRoom = receiverId + senderId;

        database.getReference().child("presence").child(receiverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String status = snapshot.getValue(String.class);
                    if (!status.isEmpty()){
                        binding.presence.setText(status);
                        binding.presence.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        final ArrayList<MessageModel> messageModels = new ArrayList<>();
        final ChatAdapter chatAdapter = new ChatAdapter(messageModels, this, receiverId);
        binding.chatRecyclerView.setAdapter(chatAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.chatRecyclerView.setLayoutManager(layoutManager);
        layoutManager.setStackFromEnd(true); // to set scroll position to bottom always

        database.getReference().child("chats")
                .child(senderRoom)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messageModels.clear();
                        for (DataSnapshot snapshot1 : snapshot.getChildren()){
                            MessageModel model = snapshot1.getValue(MessageModel.class);
                            model.setMessageId(snapshot1.getKey());
                            messageModels.add(model);
                        }
                        chatAdapter.notifyDataSetChanged(); // to update recyclerView in runtime
                        binding.chatRecyclerView.smoothScrollToPosition(binding.chatRecyclerView.getAdapter().getItemCount());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        binding.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = binding.etMessage.getText().toString();
                final MessageModel model = new MessageModel(senderId, message);
                model.setTimestamp(new Date().getTime());
                binding.etMessage.setText("");
                binding.chatRecyclerView.smoothScrollToPosition(binding.chatRecyclerView.getAdapter().getItemCount());
                layoutManager.setStackFromEnd(true);

                String randomKey = database.getReference().push().getKey();

//                HashMap<String, Object> lastMsgObj = new HashMap<>();
//                lastMsgObj.put("lastMsg", model.getMessage());
//                lastMsgObj.put("lastMsgTime", new Date().getTime());
//
//                database.getReference().child("chats")
//                        .child(senderRoom)
//                        .updateChildren(lastMsgObj);
//
//                database.getReference().child("chats")
//                        .child(receiverRoom)
//                        .updateChildren(lastMsgObj);

                database.getReference().child("chats")
                        .child(senderRoom)
                        .child(randomKey)
//                        .push() // coz this was making diff ids for sender and receiver
                        .setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        database.getReference().child("chats")
                                .child(receiverRoom)
                                .child(randomKey)
//                                .push()
                                .setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                            }
                        });
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

        final Handler handler = new Handler();
        binding.etMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                database.getReference().child("presence").child(senderId).setValue("Typing...");
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(userStoppedTyping, 1000);
            }

            Runnable userStoppedTyping = new Runnable() {
                @Override
                public void run() {
                    database.getReference().child("presence").child(senderId).setValue("Online");
                }
            };
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chat_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.setting:
                Toast.makeText(this, "Profile Clicked", Toast.LENGTH_SHORT).show();
                break;
            case R.id.videoCall:
                Toast.makeText(this, "videoCall Clicked", Toast.LENGTH_SHORT).show();
                break;
            case R.id.voiceCall:
                Toast.makeText(this, "voiceCall Clicked", Toast.LENGTH_SHORT).show();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String currentId = FirebaseAuth.getInstance().getUid();
        FirebaseDatabase.getInstance().getReference().child("presence").child(currentId).setValue("Online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        String currentId = FirebaseAuth.getInstance().getUid();
        FirebaseDatabase.getInstance().getReference().child("presence").child(currentId).setValue("Offline");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 25){
            if (data != null){
                if (data.getData() != null){
                    Uri selectedImage = data.getData();
                    Calendar calendar = Calendar.getInstance();
                    StorageReference reference = storage.getReference().child("chats").child(calendar.getTimeInMillis() + "");
                    dialog.show();
                    reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            dialog.dismiss();
                            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String filePath = uri.toString();

                                    String message = binding.etMessage.getText().toString();
                                    final MessageModel model = new MessageModel(senderId, message);
                                    model.setTimestamp(new Date().getTime());
                                    model.setMessage("photo");
                                    model.setImageUrl(filePath);
                                    binding.etMessage.setText("");

                                    String randomKey = database.getReference().push().getKey();

                                    database.getReference().child("chats")
                                            .child(senderRoom)
                                            .child(randomKey)
                                            .setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            database.getReference().child("chats")
                                                    .child(receiverRoom)
                                                    .child(randomKey)
//                                                  .push()
                                                    .setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                }
                                            });
                                        }
                                    });

//                                    Toast.makeText(ChatDetailActivity.this, filePath, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                }
            }
        }
    }

//    @Override
//    public boolean onSupportNavigateUp() {
//        finish();
//        return super.onSupportNavigateUp();
//    }
}