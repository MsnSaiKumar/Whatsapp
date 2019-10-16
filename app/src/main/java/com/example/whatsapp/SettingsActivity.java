package com.example.whatsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity
{

    private EditText username,userStatus;
    private CircleImageView profImage;
    private Button updateSettingsBtn;
    private ProgressDialog mDialog;
    private Toolbar mtoolbar;

    private FirebaseAuth mauth;
    private DatabaseReference RootRef;
    private StorageReference UserProfileImageRef;

    private String currentUserId;
    private static  final int Gallery_pic=1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mtoolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        initialize_fields();

        username.setVisibility(View.INVISIBLE);

        updateSettingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                updateSettins();

            }
        });

        RetrievingInfo();

        profImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
            Intent galleryIntent = new Intent();
            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
            galleryIntent.setType("image/*");
            startActivityForResult(galleryIntent,Gallery_pic);
            }
        });


    }




    private void updateSettins()
    {
        String name = username.getText().toString();
        String status = userStatus.getText().toString();

        if (TextUtils.isEmpty(name))
        {
            username.setError("please fill Username");
        }

        else if (TextUtils.isEmpty(status))
        {
            username.setError("please write your status");
        }
        else
        {




            HashMap profileMap = new HashMap();
            profileMap.put("uid",currentUserId);
            profileMap.put("name",name);
            profileMap.put("status",status);

            RootRef.child("Users").child(currentUserId).updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {

                            if (task.isSuccessful())
                            {
                                SendUserToMainActivity();
                                Toast.makeText(SettingsActivity.this, "Profile updated Sucessfully", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                String Message =task.getException().getMessage();
                                Toast.makeText(SettingsActivity.this, "Error" + Message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

        }

    }

    private void RetrievingInfo()
    {
        RootRef.child("Users").child(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {

                        if (dataSnapshot.exists() && (dataSnapshot.hasChild("name")) && (dataSnapshot.hasChild("image")))
                        {
                            String retrieveName = dataSnapshot.child("name").getValue().toString();
                            String retrieveStatus = dataSnapshot.child("status").getValue().toString();
                            String retrieveProfileImage = dataSnapshot.child("image").getValue().toString();


                            username.setText(retrieveName);
                            userStatus.setText(retrieveStatus);
                            Picasso.with(SettingsActivity.this).load(retrieveProfileImage).into(profImage);
                        }

                        else if (dataSnapshot.exists() && (dataSnapshot.hasChild("name")))
                        {
                            String retrieveName = dataSnapshot.child("name").getValue().toString();
                            String retrieveStatus = dataSnapshot.child("status").getValue().toString();

                            username.setText(retrieveName);
                            userStatus.setText(retrieveStatus);
                        }
                        else
                        {
                            username.setVisibility(View.VISIBLE);
                            Toast.makeText(SettingsActivity.this, "please set & update your profile", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    private void initialize_fields()
    {

        username=(EditText)findViewById(R.id.settings_username);
        userStatus=(EditText)findViewById(R.id.settings_status);
        profImage=(CircleImageView)findViewById(R.id.settings_profile_img);
        updateSettingsBtn=(Button)findViewById(R.id.settings_update_button);


        mDialog=new ProgressDialog(this);

        mauth=FirebaseAuth.getInstance();
        currentUserId=mauth.getCurrentUser().getUid();
        RootRef= FirebaseDatabase.getInstance().getReference();

        UserProfileImageRef= FirebaseStorage.getInstance().getReference().child("Profile Images");

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==Gallery_pic && resultCode==RESULT_OK && data!=null)

            {
                Uri imageuri=data.getData();
                CropImage.activity()   // crop image//
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)//x axisRatio=1,y axisRatio=1//
                        .start(this);


            }
        if (requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode==RESULT_OK)
            {
                mDialog.setTitle("Profile Image");
                mDialog.setMessage("please wait your profile image is uploading...");
                mDialog.setCanceledOnTouchOutside(false);
                mDialog.show();

                Uri resultUri = result.getUri();

                final StorageReference filepath= UserProfileImageRef.child(currentUserId + ".jpg");

                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>()

                {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                    {

                        if (task.isSuccessful())
                        {
                            Toast.makeText(SettingsActivity.this, "profile image is uploaded succesfully", Toast.LENGTH_SHORT).show();

                           filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                               @Override
                               public void onSuccess(Uri uri)
                               {

                                   final String downloadUrl = uri.toString();

                                   RootRef.child("Users").child(currentUserId).child("image").setValue(downloadUrl)
                                           .addOnCompleteListener(new OnCompleteListener<Void>() {
                                               @Override
                                               public void onComplete(@NonNull Task<Void> task) {
                                                   if (task.isSuccessful())
                                                   {
                                                       Toast.makeText(SettingsActivity.this, "image saved in database", Toast.LENGTH_SHORT).show();
                                                  mDialog.dismiss();

                                                   }
                                                   else
                                                   {
                                                       String message = task.getException().getMessage();
                                                       Toast.makeText(SettingsActivity.this, "Error" + message, Toast.LENGTH_SHORT).show();

                                                       mDialog.dismiss();
                                                   }
                                               }
                                           });
                               }
                           }) ;
                        }

                        
                        else
                        {
                            String message = task.getException().getMessage();
                            Toast.makeText(SettingsActivity.this, "Error" + message, Toast.LENGTH_SHORT).show();
                            mDialog.dismiss();
                        }
                    }
                });
            }
        }



    }

    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(SettingsActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

}
