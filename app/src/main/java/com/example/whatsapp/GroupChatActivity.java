package com.example.whatsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity
{

    private Toolbar mtoolbar;
    private ImageButton sendMessageButton;
    private EditText userMessageInput;
    private ScrollView mScrollView;
    private TextView displayTextMessages;

    private FirebaseAuth mauth;
    private DatabaseReference UsersRef,GroupNameRef,GroupMessageKeyRef;

    private String CurrentUserId,currentUserName,currentDate,currentTime;
    private String currentGroupname;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        currentGroupname=getIntent().getExtras().get("groupName").toString();
        Toast.makeText(GroupChatActivity.this,currentGroupname, Toast.LENGTH_SHORT).show();


        Initialize_fields();

        RetrieveUserInfo();

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {

                SaveInfoToDatabase();

                // after sending message in the editText it should be empty to write new message//
                userMessageInput.setText("");

                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });



    }

    @Override
    protected void onStart()
    {
        super.onStart();

        GroupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {

                if (dataSnapshot.exists())
                {
                    DisplayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {

                if (dataSnapshot.exists())
                {
                    DisplayMessages(dataSnapshot);
                }
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


    private void Initialize_fields()
    {

        mtoolbar=(Toolbar)findViewById(R.id.group_chat_bar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle(currentGroupname);



        sendMessageButton=(ImageButton)findViewById(R.id.send_message_btn);
        userMessageInput=(EditText)findViewById(R.id.input_text);
        displayTextMessages=(TextView)findViewById(R.id.group_chat_text_display);
        mScrollView=(ScrollView)findViewById(R.id.scroll_view);

        mauth=FirebaseAuth.getInstance();
        CurrentUserId=mauth.getCurrentUser().getUid();
        UsersRef= FirebaseDatabase.getInstance().getReference();
        GroupNameRef=FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupname);

    }



    private void RetrieveUserInfo()
    {
        UsersRef.child("Users").child(CurrentUserId).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {

                if(dataSnapshot.exists())
                {
                    currentUserName = dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void SaveInfoToDatabase()
    {
        String message = userMessageInput.getText().toString();
        String messageKEY=GroupNameRef.push().getKey();

        if (TextUtils.isEmpty(message))
        {
            Toast.makeText(this, "Please write mesage", Toast.LENGTH_SHORT).show();
        }

        else
        {
            Calendar calFordate =  Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM,dd,yyyy");
            currentDate=currentDateFormat.format(calFordate.getTime());


            Calendar calForTime =  Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
            currentTime=currentTimeFormat.format(calForTime.getTime());


            // this hashmap is used to get unique key/id via groupMessageKey//

            HashMap groupMessageKey = new HashMap();
            GroupNameRef.updateChildren(groupMessageKey);

            GroupMessageKeyRef = GroupNameRef.child(messageKEY);


         //  GroupMessageKeyRef=FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupname).child(messageKey);




            // this Hashmap is used to store userInfo in firebase Database//

            HashMap messageInfoMap = new HashMap();
            messageInfoMap.put("name",currentUserName);
            messageInfoMap.put("date",currentDate);
            messageInfoMap.put("time",currentTime);
            messageInfoMap.put("message",message);

            GroupMessageKeyRef.updateChildren(messageInfoMap);

        }
    }

    private void DisplayMessages(DataSnapshot dataSnapshot)
    {

        Iterator iterator = dataSnapshot.getChildren().iterator();

        while (iterator.hasNext())
        {
            String chatDate = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatMessage = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatName = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatTime = (String) ((DataSnapshot)iterator.next()).getValue();

            displayTextMessages.append(chatName + ":\n" + chatMessage + "\n" + chatTime + "    " + chatDate + "\n\n\n" );
            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);


        }
    }


}
