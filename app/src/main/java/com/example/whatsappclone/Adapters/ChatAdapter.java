package com.example.whatsappclone.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.whatsappclone.Models.MessageModel;
import com.example.whatsappclone.Models.Users;
import com.example.whatsappclone.R;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter {

    ArrayList<MessageModel> messageModels;
    Context context;
    String recId;

    String senderRoom, receiverRoom;

    public ChatAdapter(ArrayList<MessageModel> messageModels, Context context, String recId, String senderRoom, String receiverRoom) {
        this.messageModels = messageModels;
        this.context = context;
        this.recId = recId;
        this.senderRoom = senderRoom;
        this.receiverRoom = receiverRoom;
    }

    public ChatAdapter(ArrayList<MessageModel> messageModels, Context context) {
        this.messageModels = messageModels;
        this.context = context;
    }

    public ChatAdapter(ArrayList<MessageModel> messageModels, Context context, String recId) {
        this.messageModels = messageModels;
        this.context = context;
        this.recId = recId;
    }

    int SENDER_VIEW_TYPE = 1;
    int RECEIVER_VIEW_TYPE = 2;

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == SENDER_VIEW_TYPE){
            View view = LayoutInflater.from(context).inflate(R.layout.sample_sender, parent, false);
            return new SenderViewHolder(view);
        }
        else {
            View view = LayoutInflater.from(context).inflate(R.layout.sample_reciever, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (messageModels.get(position).getuId().equals(FirebaseAuth.getInstance().getUid())){
            return SENDER_VIEW_TYPE;
        }
        else {
            return RECEIVER_VIEW_TYPE;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessageModel messageModel = messageModels.get(position);
//        String senderId = FirebaseAuth.getInstance().getUid();
//        Users users = new Users();
//        String receiverId = users.getUserId();
//        senderRoom = senderId + receiverId;
//        receiverRoom = receiverId + senderId;
        senderRoom = FirebaseAuth.getInstance().getUid() + recId;
        receiverRoom = recId + FirebaseAuth.getInstance().getUid();

        int reactions[] = new int[]{
                R.drawable.ic_like,
                R.drawable.ic_love,
                R.drawable.ic_care,
                R.drawable.ic_haha,
                R.drawable.ic_wow,
                R.drawable.ic_sad,
                R.drawable.ic_angry};

        ReactionsConfig config = new ReactionsConfigBuilder(context)
                .withReactions(reactions)
                .build();

        ReactionPopup popup = new ReactionPopup(context, config, (pos) -> {
            if (holder.getClass() == SenderViewHolder.class) {
                ((SenderViewHolder)holder).senderFeeling.setImageResource(reactions[pos]);
                ((SenderViewHolder)holder).senderFeeling.setVisibility(View.VISIBLE);
            }
            else {
                ((ReceiverViewHolder)holder).receiverFeeling.setImageResource(reactions[pos]);
                ((ReceiverViewHolder)holder).receiverFeeling.setVisibility(View.VISIBLE);
            }

            messageModel.setFeeling(pos);

            FirebaseDatabase.getInstance().getReference()
                    .child("chats")
                    .child(senderRoom)
                    .child(messageModel.getMessageId())
                    .setValue(messageModel);

            FirebaseDatabase.getInstance().getReference()
                    .child("chats")
                    .child(receiverRoom)
                    .child(messageModel.getMessageId())
                    .setValue(messageModel);

            return true; // true is closing popup, false is requesting a new selection
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                new AlertDialog.Builder(context)
                        .setTitle("Delete")
                        .setMessage("Are you sure you want to delete the message?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                String senderRoom = FirebaseAuth.getInstance().getUid() + recId;
                                database.getReference().child("chats")
                                        .child(senderRoom)
                                        .child(messageModel.getMessageId())
                                        .setValue(null);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
                return false;
            }
        });

        if (holder.getClass() == SenderViewHolder.class){
            ((SenderViewHolder)holder).senderMsg.setText(messageModel.getMessage());

            if (messageModel.getMessage().equals("photo")){
                ((SenderViewHolder)holder).senderImage.setVisibility(View.VISIBLE);
                ((SenderViewHolder)holder).senderMsg.setVisibility(View.GONE);
                Picasso.get().load(messageModel.getImageUrl()).placeholder(R.drawable.image_placeholder).into(((SenderViewHolder)holder).senderImage);
            }

//            if (messageModel.getFeeling() >= 0){
//                messageModel.setFeeling(reactions[messageModel.getFeeling()]);
//                ((SenderViewHolder)holder).senderFeeling.setImageResource(messageModel.getFeeling());
//                ((SenderViewHolder)holder).senderFeeling.setVisibility(View.VISIBLE);
//            }
//            else {
//                ((SenderViewHolder)holder).senderFeeling.setVisibility(View.GONE);
//            }
//
//            ((SenderViewHolder)holder).senderMsg.setOnTouchListener(new View.OnTouchListener() {
//                @Override
//                public boolean onTouch(View v, MotionEvent event) {
//                    popup.onTouch(v, event);
//                    return true;
//                }
//            });
        }
        else {
            ((ReceiverViewHolder)holder).receiverMsg.setText(messageModel.getMessage());

            if (messageModel.getMessage().equals("photo")){
                ((ReceiverViewHolder)holder).receiverImage.setVisibility(View.VISIBLE);
                ((ReceiverViewHolder)holder).receiverMsg.setVisibility(View.GONE);
                Picasso.get().load(messageModel.getImageUrl()).placeholder(R.drawable.image_placeholder).into(((ReceiverViewHolder)holder).receiverImage);
            }

//            if (messageModel.getFeeling() >= 0){
//                messageModel.setFeeling(reactions[messageModel.getFeeling()]);
//                ((ReceiverViewHolder)holder).receiverFeeling.setImageResource(messageModel.getFeeling());
//                ((ReceiverViewHolder)holder).receiverFeeling.setVisibility(View.VISIBLE);
//            }
//            else {
//                ((ReceiverViewHolder)holder).receiverFeeling.setVisibility(View.GONE);
//            }
//
//            ((ReceiverViewHolder)holder).receiverMsg.setOnTouchListener(new View.OnTouchListener() {
//                @Override
//                public boolean onTouch(View v, MotionEvent event) {
//                    popup.onTouch(v, event);
//                    return true;
//                }
//            });
        }
    }

    @Override
    public int getItemCount() {
        return messageModels.size();
    }

    public class ReceiverViewHolder extends RecyclerView.ViewHolder {

        TextView receiverMsg, receiverTime;
        ImageView receiverFeeling, receiverImage;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            receiverMsg = itemView.findViewById(R.id.receiverText);
            receiverTime = itemView.findViewById(R.id.receiverTime);
            receiverFeeling = itemView.findViewById(R.id.receiverFeeling);
            receiverImage = itemView.findViewById(R.id.receiverImage);
        }
    }

    public class SenderViewHolder extends RecyclerView.ViewHolder {

        TextView senderMsg, senderTime;
        ImageView senderFeeling, senderImage;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            senderMsg = itemView.findViewById(R.id.senderText);
            senderTime = itemView.findViewById(R.id.senderTime);
            senderFeeling = itemView.findViewById(R.id.senderFeeling);
            senderImage = itemView.findViewById(R.id.senderImage);
        }
    }
}
