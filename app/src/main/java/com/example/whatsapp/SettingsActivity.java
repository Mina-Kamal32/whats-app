package com.example.whatsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;


public class SettingsActivity extends AppCompatActivity {
    private Button UpdateAccounSettings ;
    private EditText UserName,UserStatus;
    private CircleImageView userProfileIamge ;


    private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef ;
    private StorageReference UserProfileIamgeRef;
    private ProgressDialog loadingBar;
    private StorageTask uploadTask;
    private String downloaedUri="";

    private Toolbar SettingsTollBar ;

    

    private static final int GalleryPick=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth =FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        rootRef= FirebaseDatabase.getInstance().getReference();

        UserProfileIamgeRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        InitializeFields();

        userProfileIamge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent galleryIntent =new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GalleryPick);

            }
        });

        UserName.setVisibility(View.INVISIBLE);

        UpdateAccounSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                UpdateSettings();
            }
        });

        RetrieveUserInfo();
    }




    private void InitializeFields()
    {
        UpdateAccounSettings= (Button)findViewById(R.id.update_settings_buttons);
        UserName =(EditText)findViewById(R.id.set_user_name);
        UserStatus =(EditText)findViewById(R.id.set_profile_status);
        userProfileIamge =(CircleImageView)findViewById(R.id.set_profile_image);
        loadingBar= new ProgressDialog(this);
        SettingsTollBar =(Toolbar)findViewById(R.id.settings_toolbar);
        setSupportActionBar(SettingsTollBar);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Account Settings");

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==GalleryPick && resultCode==RESULT_OK && data!=null )
        {
            Uri ImageUri=data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
                    }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode==RESULT_OK)
            {
                loadingBar.setTitle("set profile Image");
                loadingBar.setMessage("please wait your profile image updating ");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                Uri resultUri = result.getUri();
//                final String srr= result.getUri().toString();
                final StorageReference filePath =UserProfileIamgeRef.child(currentUserID+".png");


                uploadTask=filePath.putFile(resultUri);
                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful()) {
                            loadingBar.dismiss();
                            Toast.makeText(SettingsActivity.this, "Error" +task.getException(), Toast.LENGTH_SHORT).show();
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task <Uri> task) {


                                if (task.isSuccessful())
                                {
//
                                    Uri dUri=task.getResult();
                                    downloaedUri = dUri.toString();

                                    Toast.makeText(SettingsActivity.this,"profile Image uploaded ",Toast.LENGTH_LONG).show();

                                    rootRef.child("Users").child(currentUserID).child("image")
                                            .setValue(downloaedUri).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful())
                                            {
                                                loadingBar.dismiss();
                                                Toast.makeText(SettingsActivity.this," Image save in database ",Toast.LENGTH_SHORT).show();
                                            }else
                                            {
                                                loadingBar.dismiss();
                                                String massage = task.getException().toString();
                                                Toast.makeText(SettingsActivity.this," Error 8888888888 : "+ massage,Toast.LENGTH_SHORT).show();
                                            }

                                        }
                                    });
//
//
//                                    rootRef.child("Users").child(currentUserID).child("image")
//                                            .setValue(downloaedUri).addOnCompleteListener(new OnCompleteListener<Void>() {
//                                        @Override
//                                        public void onComplete(@NonNull Task<Void> task) {
//                                            if (task.isSuccessful())
//                                            {
//                                                loadingBar.dismiss();
//                                                Toast.makeText(SettingsActivity.this," Image save in database ",Toast.LENGTH_SHORT).show();
//                                            }else
//                                            {
//                                                loadingBar.dismiss();
//                                                String massage = task.getException().toString();
//                                                Toast.makeText(SettingsActivity.this," Error : "+ massage,Toast.LENGTH_SHORT).show();
//                                            }
//
//                                        }
//                                    });

                                }else
                                {
                                    loadingBar.dismiss();
                                    String massage = task.getException().toString();
                                    Toast.makeText(SettingsActivity.this," Error : "+ massage,Toast.LENGTH_SHORT).show();
                                }


                    }
                });




            }

        }

    }


    private void UpdateSettings()
    {
        String setUserName = UserName.getText().toString();
        String setStatus = UserStatus.getText().toString();


        if (TextUtils.isEmpty(setUserName))
        {
            Toast.makeText(SettingsActivity.this,"UserName is Empty",Toast.LENGTH_LONG).show();
        }
        if (TextUtils.isEmpty(setStatus))
        {
            Toast.makeText(SettingsActivity.this,"Status is Empty",Toast.LENGTH_LONG).show();
        }
        else
            {
                HashMap<String,Object> proFileMap  = new HashMap<>();
                proFileMap.put("uid",currentUserID);
                proFileMap.put("name",setUserName);
                proFileMap.put("status",setStatus);



                rootRef.child("Users").child(currentUserID).updateChildren(proFileMap)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            sendUserToMainActivity();
                            Toast.makeText(SettingsActivity.this,"profile Updated Successfuly.....",Toast.LENGTH_LONG).show();
                        }
                        else
                            {
                                String massage = task.getException().toString();
                                Toast.makeText(SettingsActivity.this,"Error" + massage ,Toast.LENGTH_LONG).show();

                            }

                    }
                });


        }

    }


    private void RetrieveUserInfo()
    {
        rootRef.child("Users").child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.exists())&&(dataSnapshot.hasChild("name"))&&(dataSnapshot.hasChild("image")))
                {
                    String retrieveUserName =dataSnapshot.child("name").getValue().toString();
                    String retrieveStatus =dataSnapshot.child("status").getValue().toString();
                    String retrieveProfileImage =dataSnapshot.child("image").getValue().toString();
                    Picasso.get().load(retrieveProfileImage).placeholder(R.drawable.signup_photo).into(userProfileIamge);

                    UserName.setText(retrieveUserName);
                    UserStatus.setText(retrieveStatus);
                }
                else if ((dataSnapshot.exists())&&(dataSnapshot.hasChild("name")))
                {
                    String retrieveUserName =dataSnapshot.child("name").getValue().toString();
                    String retrieveStatus =dataSnapshot.child("status").getValue().toString();

                    UserName.setText(retrieveUserName);
                    UserStatus.setText(retrieveStatus);

                }
                else
                {
                    UserName.setVisibility(View.VISIBLE);
                    Toast.makeText(SettingsActivity.this,"please update your profile ....",Toast.LENGTH_LONG).show();

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void sendUserToMainActivity()
    {
        Intent loginIntent = new Intent(SettingsActivity.this,MainActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK );
        startActivity(loginIntent);
        finish();

    }

}
