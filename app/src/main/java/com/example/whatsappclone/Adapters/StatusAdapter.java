package com.example.whatsappclone.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.whatsappclone.Fragments.StatusFragment;
import com.example.whatsappclone.Models.UserStatus;
import com.example.whatsappclone.R;

import java.util.ArrayList;

public class StatusAdapter extends RecyclerView.Adapter<StatusAdapter.StatusViewHolder>{

    Context context;
    ArrayList<UserStatus> userStatuses;

    public StatusAdapter(Context context, ArrayList<UserStatus> userStatuses) {
        this.context = context;
        this.userStatuses = userStatuses;
    }

    @NonNull
    @Override
    public StatusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_status, parent,false);
        return new StatusViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StatusViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return userStatuses.size();
    }

    public class StatusViewHolder extends RecyclerView.ViewHolder {



        public StatusViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
