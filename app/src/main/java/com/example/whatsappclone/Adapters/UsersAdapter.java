package com.example.whatsappclone.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.whatsappclone.ChatDetailActivity;
import com.example.whatsappclone.Models.Users;
import com.example.whatsappclone.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> implements Filterable {

    ArrayList<Users> list;
    Context context;

    public UsersAdapter(ArrayList<Users> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.sample_show_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Users users = list.get(position);
        Picasso.get().load(users.getProfilePic()).placeholder(R.drawable.ic_avatar).into(holder.image);
        holder.userName.setText(users.getUserName());

        holder.image.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_transition_animation));
        holder.constraintLayout.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_scale_animation));

        String senderId = FirebaseAuth.getInstance().getUid();
        String senderRoom = senderId + users.getUserId();

//        FirebaseDatabase.getInstance().getReference()
//                .child("chats")
//                .child(senderRoom)
//                .addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        if (snapshot.exists()) {
//                            String lastMsg = snapshot.child("lastMsg").getValue(String.class);
//                            long time = snapshot.child("lastMsgTime").getValue(Long.class);
//                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm a");
//                            holder.timeStamp.setText(simpleDateFormat.format(new Date(time)));
//                            holder.lastMessage.setText(lastMsg);
//                        }
//                        else {
//                            holder.lastMessage.setText("Tap to chat");
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });

        FirebaseDatabase.getInstance().getReference().child("chats")
                .child(FirebaseAuth.getInstance().getUid() + users.getUserId()) // senderRoom = senderId + receiverId
                .orderByChild("timestamp") // by default firebase stores data in ascending order and we made it to descending order according to timestamp
                .limitToLast(1) // we want only 1 message
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChildren()){
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                                holder.lastMessage.setText(dataSnapshot.child("message").getValue().toString());
                                long time = dataSnapshot.child("timestamp").getValue(Long.class);
                                SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
                                holder.timeStamp.setText(dateFormat.format(new Date(time)));
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatDetailActivity.class);
                intent.putExtra("userId", users.getUserId());
                intent.putExtra("profilePic", users.getProfilePic());
                intent.putExtra("userName", users.getUserName());
                context.startActivity(intent);
            }
        });

        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Image clicked", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    Filter filter = new Filter() {
        // Runs on a background thread
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Users> filteredList = new ArrayList<>();
            if (constraint.toString().isEmpty()){
                filteredList.addAll(list);
            }
            else {
                for (Users user : list){
                    if (user.getUserName().toLowerCase().contains(constraint.toString().toLowerCase())){
                        filteredList.add(user);
                    }
                }
            }

            FilterResults filterResults = new FilterResults();
            filterResults.values = filteredList;
            return filterResults;
        }

        // Runs on a UI thread
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            list.clear();
            list.addAll((Collection<? extends Users>) results);
            notifyDataSetChanged();
        }
    };

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        TextView userName, lastMessage, timeStamp;
        ConstraintLayout constraintLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.profile_image);
            userName = itemView.findViewById(R.id.userNameList);
            lastMessage = itemView.findViewById(R.id.lastMessage);
            timeStamp = itemView.findViewById(R.id.msgTime);
            constraintLayout = itemView.findViewById(R.id.constraintLayoutUser);
        }
    }
}
