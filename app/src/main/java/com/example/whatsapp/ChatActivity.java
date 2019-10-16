package com.example.whatsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity
{
private String messageRecieverId,messageRecieverName,messageRecieverImage,messageSenderId;

private TextView userLastSeen1,userName1;
private CircleImageView userImage1;
private EditText MessageInputText;
private ImageButton SendMessageButton;

private FirebaseAuth mauth;
private DatabaseReference RootRef;

private Toolbar chatToolbar;
private RecyclerView  userMessagesList;

private final List<Messages> messagesList = new ArrayList<>();
private LinearLayoutManager linearLayoutManager;
private MessagesAdapter messagesAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messageRecieverId=getIntent().getExtras().get("visit_users_id").toString();
        messageRecieverName=getIntent().getExtras().get("visit_users_name").toString();
        messageRecieverImage=getIntent().getExtras().get("visit_image").toString();



        InitializeControllers();

      userName1.setText(messageRecieverName);
        Picasso.with(ChatActivity.this).load(messageRecieverImage).placeholder(R.drawable.profile).into(userImage1);


        SendMessageButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                SendMessages();

            }
        });


    }




    private void InitializeControllers()
    {


        chatToolbar=(Toolbar)findViewById(R.id.chat_bar);
        setSupportActionBar(chatToolbar);

        ActionBar actionBar =getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);



        LayoutInflater layoutInflater =(LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView  = layoutInflater.inflate(R.layout.custom_chat_bar1,null);
        actionBar.setCustomView(actionBarView);

        userImage1=(CircleImageView)findViewById(R.id.custom_profile_image);
        userName1=(TextView)findViewById(R.id.custom_profile_name);
        userLastSeen1=(TextView)findViewById(R.id.custom_profile_lastSeen);
        MessageInputText=(EditText)findViewById(R.id.chat_input_text);
        SendMessageButton=(ImageButton)findViewById(R.id.chat_send_message_btn);


        mauth=FirebaseAuth.getInstance();
        messageSenderId=mauth.getCurrentUser().getUid();
        RootRef= FirebaseDatabase.getInstance().getReference();


       messagesAdapter =new MessagesAdapter(messagesList);
       userMessagesList=(RecyclerView) findViewById(R.id.private_chat_list);
       linearLayoutManager = new LinearLayoutManager(this);
       userMessagesList.setLayoutManager(linearLayoutManager);
       userMessagesList.setAdapter(messagesAdapter);










    }

//    @Override
//    protected void onStart()
//    {
//        super.onStart();
//
//        RootRef.child("Messages").child(messageSenderId).child(messageRecieverId)
//                .addChildEventListener(new ChildEventListener()
//                {
//                    @Override
//                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
//                    {
//                        Messages messages = dataSnapshot.getValue(Messages.class);
//                        messagesList.add(messages);
//
//
//                        messageAdapter.notifyDataSetChanged();
//
//                    }
//
//                    @Override
//                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//
//                    }
//
//                    @Override
//                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
//
//                    }
//
//                    @Override
//                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                    }
//                });
//    }


    @Override
    protected void onStart()
    {
        super.onStart();

        RootRef.child("Messages").child(messageSenderId).child(messageRecieverId)
                .addChildEventListener(new ChildEventListener()
                {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
                    {
                        Messages messages = dataSnapshot.getValue(Messages.class);

                        messagesList.add(messages);

                        messagesAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
                    {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    private void SendMessages()
    {
        String messagesText = MessageInputText.getText().toString();
        if (TextUtils.isEmpty(messagesText))
        {
            Toast.makeText(this, "Please fill the message", Toast.LENGTH_SHORT).show();
        }
        else
        {
            String messageSenderRef = "Messages/" +  messageSenderId + "/" + messageRecieverId;

            String messageRecieverRef = "Messages/" +  messageRecieverId + "/" + messageSenderId;

            DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                    .child(messageSenderId).child(messageRecieverId).push();

            String messagePushId = userMessageKeyRef.getKey();
            HashMap messageTextBody = new HashMap();
            messageTextBody.put("message",messagesText);
            messageTextBody.put("type","text");
            messageTextBody.put("from",messageSenderId);

            HashMap messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef + "/" + messagePushId,messageTextBody);
            messageBodyDetails.put(messageRecieverRef +"/" + messagePushId,messageTextBody);

            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener()
            {
                @Override
                public void onComplete(@NonNull Task task)
                {
                    if (task.isSuccessful())
                    {
                        Toast.makeText(ChatActivity.this, "message Sent Successfully...", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                    MessageInputText.setText(" ");
                }
            });
        }
    }
}
