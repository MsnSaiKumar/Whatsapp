package com.example.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

//import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

public class MainActivity extends AppCompatActivity
{
private Toolbar mtoolbar;
private TabLayout mytablayout;
private ViewPager myViewPager;
private TabsAcessioriesAdapter myTabsAccessorAdapter;

private FirebaseUser currentUser;
private FirebaseAuth mauth;
private DatabaseReference RootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mauth=FirebaseAuth.getInstance();
        currentUser=mauth.getCurrentUser();
        RootRef= FirebaseDatabase.getInstance().getReference();

        mtoolbar = (Toolbar)findViewById(R.id.toolbar_main);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("Whatsapp");


        myViewPager = (ViewPager) findViewById(R.id.main_tabs_pager);
        myTabsAccessorAdapter = new TabsAcessioriesAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(myTabsAccessorAdapter);

        mytablayout = (TabLayout) findViewById(R.id.main_tabs);
        mytablayout.setupWithViewPager(myViewPager);
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        if (currentUser ==null)// if user doesnt sign in sends to loginActivity
        {
            sendUserToLoginActivity();
        }
        else
        {
            verifyUserExistence();
        }
    }


    private void verifyUserExistence()
    {
        String currentUserId=mauth.getCurrentUser().getUid();
        RootRef.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
            if(dataSnapshot.child("name").exists())
            {

                Toast.makeText(MainActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
            }
            else
            {
                // when user creates account it sends to settings activity//
                SendUserToSettingsActivity();

            }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
         super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
         super.onOptionsItemSelected(item);

        if (item.getItemId()==R.id.menu_logout)
        {
            mauth.signOut();
            sendUserToLoginActivity();
        }
        if (item.getItemId()==R.id.menu_settings)
        {
            SendUserToSettingsActivity();

        }
        if (item.getItemId()==R.id.menu_create_group)
        {
            RequestNewGroup();
        }
        if (item.getItemId()==R.id.menu_find_friends)
        {

            SendUserToFindFriendsActivity();
        }

        return  true;

    }

    private void RequestNewGroup()
    {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this,R.style.AlertDialog);
        builder.setTitle("Enter Group Name :");

        final EditText groupNameField = new EditText(MainActivity.this);
        groupNameField.setHint("e.g coding");

        builder.setView(groupNameField);

        builder.setPositiveButton("create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                String groupName = groupNameField.getText().toString();

                if(TextUtils.isEmpty(groupName))
                {
                    Toast.makeText(MainActivity.this, "Please Write Group Name", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    createNewGroup(groupName);
                }

            }
        });


        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                dialogInterface.cancel();

            }
        });

        builder.show();

    }

    private void createNewGroup(final String groupName)
    {

        RootRef.child("Groups").child(groupName).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
            if (task.isSuccessful())
            {
                Toast.makeText(MainActivity.this, groupName + "group is created Sucessfully", Toast.LENGTH_SHORT).show();
            }

            }
        });
    }


    private void sendUserToLoginActivity()
    {
        Intent loginIntent = new Intent(MainActivity.this,LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);

    }

    private void SendUserToSettingsActivity()
    {

        Intent SettingsIntent = new Intent(MainActivity.this,SettingsActivity.class);
       
        startActivity(SettingsIntent);


    }
    private void SendUserToFindFriendsActivity()
    {

        Intent FindFriendIntent = new Intent(MainActivity.this,FindFriendsActvity.class);
        startActivity(FindFriendIntent);


    }



}
