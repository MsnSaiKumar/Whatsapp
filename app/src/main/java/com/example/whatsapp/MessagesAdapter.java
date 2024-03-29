package com.example.whatsapp;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder>
{
    private List<Messages> userMessagesList;
    private FirebaseAuth mauth;
    private DatabaseReference usersRef;

    public MessagesAdapter(List<Messages> userMessagesList) // sai >??
    {
        this.userMessagesList=userMessagesList;
    }

    public class MessageViewHolder extends  RecyclerView.ViewHolder
    {
        public TextView senderMessageText,recieverMessageText;
        public CircleImageView recieverProfileImage;

        public MessageViewHolder(@NonNull View itemView)
        {
            super(itemView);

            senderMessageText=(TextView)itemView.findViewById(R.id.sender_message_text);
            recieverMessageText=(TextView) itemView.findViewById(R.id.reciever_message_text);
            recieverProfileImage=(CircleImageView) itemView.findViewById(R.id.message_profile_image);

        }
    }


    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i )
    {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.custom_message_layout,viewGroup,false);

        mauth=FirebaseAuth.getInstance();

        return  new MessageViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position)
    {
        String messageSenderId = mauth.getCurrentUser().getUid();
        Messages messages= userMessagesList.get(position);

        String fromUserId = messages.getFrom();
        String fromMessageType = messages.getType();

        usersRef= FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserId);

        usersRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.hasChild("image"))
                {
                    String recieverImage= dataSnapshot.child("image").getValue().toString();
                    Picasso.with(holder.recieverProfileImage.getContext()).load(recieverImage).into(holder.recieverProfileImage);
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if (fromMessageType.equals("text"))
        {
            holder.recieverMessageText.setVisibility(View.INVISIBLE);
            holder.recieverProfileImage.setVisibility(View.INVISIBLE);

            if (fromUserId.equals(messageSenderId))
            {
                holder.senderMessageText.setBackgroundResource(R.drawable.sender_message_layout);
                holder.senderMessageText.setTextColor(Color.BLACK);
                holder.senderMessageText.setText(messages.getMessage());
            }

            else
            {

                holder.senderMessageText.setVisibility(View.INVISIBLE);

                holder.recieverProfileImage.setVisibility(View.VISIBLE);
                holder.recieverMessageText.setVisibility(View.VISIBLE);

                holder.recieverMessageText.setBackgroundResource(R.drawable.reciever_messsage_layout);
                holder.recieverMessageText.setTextColor(Color.BLACK);
                holder.recieverMessageText.setText(messages.getMessage());


            }
        }


    }

    @Override
    public int getItemCount()
    {
        return userMessagesList.size();
    }


}
