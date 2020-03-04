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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    private Button SendVerificationCodeButton, VerifyButton;
    private EditText InputPhoneNumber, InputVerificationCode;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private FirebaseAuth mAuth;

    private ProgressDialog LodingBar;

    private String mVerificationID;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        mAuth =FirebaseAuth.getInstance();

        SendVerificationCodeButton =(Button)findViewById(R.id.send_ver_code_button);
        VerifyButton=(Button)findViewById(R.id.verify_button);
        InputPhoneNumber =(EditText)findViewById(R.id.phone_number_input);
        InputVerificationCode=(EditText)findViewById(R.id.verification_code_input);
        LodingBar =new ProgressDialog(this);

        SendVerificationCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String phoneNumber =InputPhoneNumber.getText().toString();
                if (TextUtils.isEmpty(phoneNumber))
                {
                    Toast.makeText(PhoneLoginActivity.this,"please enter  phone number first",Toast.LENGTH_SHORT);
                    InputPhoneNumber.setText("mon");
                }
                else
                    {
                        LodingBar.setTitle("Phone Verification");
                        LodingBar.setMessage("please wait ,while we are authenticating your phone ");
                        LodingBar.setCanceledOnTouchOutside(false);
                        LodingBar.show();
                        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber,
                                60, TimeUnit.SECONDS,
                                PhoneLoginActivity.this,callbacks);
                    }


            }
        });

        VerifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                SendVerificationCodeButton.setVisibility(View.INVISIBLE);
                InputPhoneNumber.setVisibility(View.INVISIBLE);

                String verificationCode =InputVerificationCode.getText().toString();
                if (TextUtils.isEmpty(verificationCode))
                {
                    Toast.makeText(PhoneLoginActivity.this,"please write Verification code first",Toast.LENGTH_SHORT);
                }
                else
                {

                    LodingBar.setTitle("Verification code ");
                    LodingBar.setMessage("please wait ,while we are Verification code...");
                    LodingBar.setCanceledOnTouchOutside(false);
                    LodingBar.show();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationID, verificationCode);
                    signInWithPhoneAuthCredential(credential);

                }

            }
        });


        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential)
            {

                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e)
            {
                LodingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this,
                        "Invalid phone Number, please enter correct phone numbr with your country code... ",
                        Toast.LENGTH_SHORT);

                SendVerificationCodeButton.setVisibility(View.VISIBLE);
                InputPhoneNumber.setVisibility(View.VISIBLE);
                VerifyButton.setVisibility(View.INVISIBLE);
                InputVerificationCode.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onCodeSent(String s,
                                   PhoneAuthProvider.ForceResendingToken forceResendingToken)
            {

                mVerificationID = s;
                mResendToken = forceResendingToken;

                LodingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this,"code has been sent , please check and verify... ",Toast.LENGTH_SHORT);

                SendVerificationCodeButton.setVisibility(View.INVISIBLE);
                InputPhoneNumber.setVisibility(View.INVISIBLE);

                VerifyButton.setVisibility(View.VISIBLE);
                InputVerificationCode.setVisibility(View.VISIBLE);




            }
        };

    }



    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            LodingBar.dismiss();
                            Toast.makeText(PhoneLoginActivity.this,"Logged in successfully...",Toast.LENGTH_SHORT);
                            sendUserToMainActivity();

                        }
                        else
                        {
                            String message =task.getException().toString();
                            Toast.makeText(PhoneLoginActivity.this,"Error : "+message,Toast.LENGTH_SHORT);



                        }
                    }
                });
    }
    private void sendUserToMainActivity()
    {
        Intent mainIntent = new Intent(PhoneLoginActivity.this,MainActivity.class);
//        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK );
        startActivity(mainIntent);
        finish();
    }

}
