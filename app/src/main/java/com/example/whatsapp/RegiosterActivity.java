package com.example.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.TextUtilsCompat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegiosterActivity extends AppCompatActivity {

    private Button CreateAccountButton;
    private EditText UserEmail,UserPassword;
    private TextView AlreadyHaveAccountLink ;

    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regioster);

        mAuth=FirebaseAuth.getInstance();
        rootRef= FirebaseDatabase.getInstance().getReference();

        InitializeFields();
        AlreadyHaveAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToLoginActivity();
            }
        });

        CreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CreateNewAccount();

            }
        });

    }

    private void CreateNewAccount()
    {


     String email = UserEmail.getText().toString();
     String password = UserPassword.getText().toString();
    if (TextUtils.isEmpty(email))
    {
        Toast.makeText(this,"please enter email",Toast.LENGTH_SHORT).show();
    }
    if (TextUtils.isEmpty(password))
    {
            Toast.makeText(this,"please enter password",Toast.LENGTH_SHORT).show();
    }
    else
    {

        loadingBar.setTitle("Creating New Account");
        loadingBar.setMessage("please wait , while ew creating new account for you....");
        loadingBar.setCanceledOnTouchOutside(true);
        loadingBar.show();

        mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){

                            String deviceToken= FirebaseInstanceId.getInstance().getToken();

                            String currentUser =mAuth.getCurrentUser().getUid();
                            rootRef.child("Users").child(currentUser).setValue("");

                            rootRef.child("Users").child(currentUser).child("device_token")
                                    .setValue(deviceToken);

                            sendUserToMainActivity();
                            Toast.makeText(RegiosterActivity.this,"Account created Successfully ",Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                        else {
                            String massage = task.getException().toString();
                            Toast.makeText(RegiosterActivity.this,"Error : "+ massage,Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                });
    }

    }

    private void InitializeFields() {
        CreateAccountButton=(Button)findViewById(R.id.regisoter_button);
        UserEmail=(EditText)findViewById(R.id.regisoter_email);
        UserPassword=(EditText)findViewById(R.id.regisoter_password);
        AlreadyHaveAccountLink=(TextView) findViewById(R.id.alread_have_account_link);
        loadingBar=new ProgressDialog(this);
    }
    private void sendUserToLoginActivity()
    {
        Intent register = new Intent(RegiosterActivity.this,LoginActivity.class);
        startActivity(register);
    }
    private void sendUserToMainActivity()
    {
        Intent mainIntent = new Intent(RegiosterActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
