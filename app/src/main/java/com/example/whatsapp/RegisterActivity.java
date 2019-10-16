package com.example.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity
{
    private EditText email,password;
    private Button CreateAccBtn;
    private  TextView AlreadyExistingAcc;
    private ProgressDialog mDialog;

    private FirebaseAuth mauth;
    private DatabaseReference Rootref;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        initialize_fields();


        AlreadyExistingAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUserToLoginActivity();
            }
        });

        CreateAccBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                createAccountInfo();

            }
        });
    }

    private void createAccountInfo()
    {
        String myemail=email.getText().toString();
        String mypasswd=password.getText().toString();

        if(TextUtils.isEmpty(myemail))
        {
            email.setError("please fill email");
        }
        else if(TextUtils.isEmpty(mypasswd))
        {
            password.setError("please fill password");
        }
        else
        {
            mDialog.setTitle("Saving information..");
            mDialog.setMessage("Please wait while we are creating new Account...");
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.show();

       mauth.createUserWithEmailAndPassword(myemail,mypasswd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
           @Override
           public void onComplete(@NonNull Task<AuthResult> task)
           {

               if (task.isSuccessful())
               {
                   String currentUserid= mauth.getCurrentUser().getUid();

                   Rootref.child("Users").child(currentUserid).setValue("");

                   SendUserToMainActivity();
                   Toast.makeText(RegisterActivity.this, "Account is created Succesfully", Toast.LENGTH_SHORT).show();
                   mDialog.dismiss();
               }
               else
               {
                   String message = task.getException().getMessage();
                   Toast.makeText(RegisterActivity.this, "error"+ message, Toast.LENGTH_SHORT).show();
                   mDialog.dismiss();
               }



           }
       });

        }

    }



    private void SendUserToLoginActivity()
    {

        Intent RegIntent = new Intent(RegisterActivity.this,LoginActivity.class);
        startActivity(RegIntent);

    }
    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(RegisterActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }


    private void initialize_fields()
    {
        email=(EditText)findViewById(R.id.id_email_reg);
        password=(EditText)findViewById(R.id.id_pass_reg);
        CreateAccBtn=(Button)findViewById(R.id.id_create_reg);
        AlreadyExistingAcc=(TextView)findViewById(R.id.already_account);

        mDialog= new ProgressDialog(this);

        mauth=FirebaseAuth.getInstance();
        Rootref= FirebaseDatabase.getInstance().getReference();

    }
}
