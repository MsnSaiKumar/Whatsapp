package com.example.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity
{
    private String recieverUserId,current_state,senderUserId;
    private TextView userProfileName,userProfileStatus;
    private Button SendmessageReqButton,Declinemessagebutton;
    private CircleImageView userProfileimage;

    private DatabaseReference UsersRef,ChatReqRef,ContactsRef;
    private FirebaseAuth mauth;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mauth=FirebaseAuth.getInstance();
        UsersRef= FirebaseDatabase.getInstance().getReference().child("Users");
        ChatReqRef= FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        ContactsRef= FirebaseDatabase.getInstance().getReference().child("Contacts");


        recieverUserId = getIntent().getExtras().get("Visit_user_id").toString(); // refrd from FindFriends Actiivty 76 & 79//

        senderUserId=mauth.getCurrentUser().getUid();




        userProfileimage=(CircleImageView)findViewById(R.id.person_profile_profiPic);
        userProfileName=(TextView)findViewById(R.id.person_profile_userName);
        userProfileStatus=(TextView)findViewById(R.id.person_profile_status);
        SendmessageReqButton=(Button)findViewById(R.id.profile_send_message_button);
        Declinemessagebutton=(Button)findViewById(R.id.profile_decline_message_button);
        current_state="new";

        RetrieveUserInfo();
    }

    private void RetrieveUserInfo()
    {
        UsersRef.child(recieverUserId).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists() && dataSnapshot.hasChild("image"))

                {
                    String profilePic = dataSnapshot.child("image").getValue().toString();
                    String profileName = dataSnapshot.child("name").getValue().toString();
                    String profileStatus = dataSnapshot.child("status").getValue().toString();

                    Picasso.with(ProfileActivity.this).load(profilePic).placeholder(R.drawable.profile).into(userProfileimage);
                    userProfileName.setText(profileName);
                    userProfileStatus.setText(profileStatus);

                    ManageChatRequests();
                }
                else
                {
                    String profileName = dataSnapshot.child("name").getValue().toString();
                    String profileStatus = dataSnapshot.child("status").getValue().toString();

                    userProfileName.setText(profileName);
                    userProfileStatus.setText(profileStatus);

                    ManageChatRequests();



                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void ManageChatRequests()
    {

        // retrieves the "request_type" from firebase //
        ChatReqRef.child(senderUserId)
                .addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.hasChild(recieverUserId))
                        {
                            // "request_type present in firebase is "sent", will be stored in this 'request_type' variable below//

                            String request_type=dataSnapshot.child(recieverUserId).child("request_type").getValue().toString();

                            if (request_type.equals("Sent"))
                            {
                                current_state ="request_sent";
                                SendmessageReqButton.setText("Cancel chat Request");
                            }
                            if(request_type.equals("Recieved"))
                            {
                                current_state="request_recieved";
                                SendmessageReqButton.setText("Accept Chat Request");

                                Declinemessagebutton.setEnabled(true);
                                Declinemessagebutton.setVisibility(View.VISIBLE);

                                Declinemessagebutton.setOnClickListener(new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View view)
                                    {
                                        CancelChatReq();

                                    }
                                });

                            }

                        }
                        else
                        {
                            ContactsRef.child(senderUserId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                        {
                                            if (dataSnapshot.hasChild(recieverUserId))
                                            {
                                                current_state="friends";
                                                SendmessageReqButton.setText("Remove this Contact");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
        //till here//



        if (!senderUserId.equals(recieverUserId))
        {
            SendmessageReqButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    SendmessageReqButton.setEnabled(false);

                    if (current_state.equals("new"))
                    {
                        SendChatReq();
                    }
                    if (current_state.equals("request_sent"))
                    {
                        CancelChatReq();
                    }
                    if (current_state.equals("request_recieved"))
                    {
                        AcceptChatReq();
                    }
                    if (current_state.equals("friends"))
                    {
                        RemoveSpecificContact();
                    }

                }
            });
        }
        else
        {
            // if senderId and recieverId is same then btn vil be invisible i.e for user own account it hides the btn//

            SendmessageReqButton.setVisibility(View.INVISIBLE);
        }
    }




    private void SendChatReq()
    {

        ChatReqRef.child(senderUserId).child(recieverUserId).child("request_type")
                .setValue("Sent").addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if (task.isSuccessful())
                {
                    ChatReqRef.child(recieverUserId).child(senderUserId).child("request_type")
                            .setValue("Recieved").addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                           if (task.isSuccessful())
                           {
                               SendmessageReqButton.setEnabled(true);
                               current_state="request_sent";
                               SendmessageReqButton.setText("Cancel Chat Request");
                           }
                        }
                    });
                }

            }
        });
    }


    private void CancelChatReq()
    {
        ChatReqRef.child(senderUserId).child(recieverUserId)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if (task.isSuccessful())
                {
                    ChatReqRef.child(recieverUserId).child(senderUserId)
                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if (task.isSuccessful())
                            {
                                SendmessageReqButton.setEnabled(true);
                                current_state="new";
                                SendmessageReqButton.setText("Send Message");


                                Declinemessagebutton.setEnabled(false);
                                Declinemessagebutton.setVisibility(View.INVISIBLE);

                            }

                        }
                    }) ;
                }
            }
        });
    }

    private void AcceptChatReq()
    {

        ContactsRef.child(senderUserId).child(recieverUserId).child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            ContactsRef.child(recieverUserId).child(senderUserId).child("Contacts").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                ChatReqRef.child(senderUserId).child(recieverUserId).removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>()
                                                        {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                if (task.isSuccessful())
                                                                {
                                                                    ChatReqRef.child(recieverUserId).child(senderUserId).removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>()
                                                                            {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task)
                                                                                {
                                                                                    SendmessageReqButton.setEnabled(true);
                                                                                    current_state="friends";
                                                                                    SendmessageReqButton.setText("Remove this Contact");

                                                                                    Declinemessagebutton.setVisibility(View.INVISIBLE);
                                                                                    Declinemessagebutton.setEnabled(false);
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }

                                        }
                                    });


                        }
                    }
                });
    }

    private void RemoveSpecificContact() {
        ContactsRef.child(senderUserId).child(recieverUserId)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    ContactsRef.child(recieverUserId).child(senderUserId)
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                SendmessageReqButton.setEnabled(true);
                                current_state = "new";
                                SendmessageReqButton.setText("Send Message");


                                Declinemessagebutton.setEnabled(false);
                                Declinemessagebutton.setVisibility(View.INVISIBLE);

                            }

                        }
                    });
                }
            }
        });
    }
}
