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
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity
{

private EditText email,password;
private Button LoginBtn;
private  TextView newAccountBtn,forgotPasswdBtn;


private ProgressDialog mDialog;

private FirebaseAuth mauth;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initialize_id();

      newAccountBtn.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              SendUserToRegisterActivity();
          }
      });

      LoginBtn.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view)
          {
              UserLoginInfo();

          }
      });


    }

    private void UserLoginInfo() {
        String UserEmail = email.getText().toString();
        String UserPasswd =password.getText().toString();

        if (TextUtils.isEmpty(UserEmail))
        {
            email.setError("please fill email");

        }

        else
            if(TextUtils.isEmpty(UserPasswd))
        {
            password.setError("please fill password");
        }
            else
            {   mDialog.setTitle("Logging in");
                mDialog.setMessage("Please wait while we are loading your Account...");
                mDialog.setCanceledOnTouchOutside(false);
                mDialog.show();


                mauth.signInWithEmailAndPassword(UserEmail,UserPasswd).
                        addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {
                            SendUserToMainActivity();
                            Toast.makeText(getApplicationContext(),"Logged in successfully",Toast.LENGTH_SHORT).show();
                            mDialog.dismiss();

                        }
                        else
                        {
                            String message = task.getException().getMessage();
                            Toast.makeText(LoginActivity.this, "error" + message, Toast.LENGTH_SHORT).show();
                        mDialog.dismiss();
                        }

                    }
                });
            }

    }







    private void initialize_id()
    {
        email=(EditText)findViewById(R.id.id_email);
        password=(EditText)findViewById(R.id.id_pass);
        LoginBtn=(Button)findViewById(R.id.id_login);
        newAccountBtn=(TextView)findViewById(R.id.id_signup);
        forgotPasswdBtn=(TextView)findViewById(R.id.forget_password);

        mDialog = new ProgressDialog(LoginActivity.this);

        mauth=FirebaseAuth.getInstance();


    }

    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(LoginActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void SendUserToRegisterActivity()
    {
        Intent loginIntent = new Intent(LoginActivity.this,RegisterActivity.class);
        startActivity(loginIntent);
    }

}
