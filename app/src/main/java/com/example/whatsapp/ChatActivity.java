package com.example.whatsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.Inflater;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String messageReceiverID,messageSenderID, messageReceiverName, messageReceiverImage;

    private TextView userName, userLastSeen;
    private CircleImageView userImage;

    private Toolbar ChatToolbar;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    private ImageButton SendMessageButton,SendFilesButton;
    private EditText MessageInputText;

    private final List<Messages> messagesList =new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private messageAdapter messageAdapter;
    private RecyclerView userMessageList;
    private String saveCurrentTime,saveCurrentDate;
    private String checker="",myUri="";
    private StorageTask uploadTask;
    private Uri fileUri;
    private ProgressDialog loadingBar;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        mAuth=FirebaseAuth.getInstance();
        messageSenderID=mAuth.getCurrentUser().getUid();
        RootRef= FirebaseDatabase.getInstance().getReference();

        messageReceiverName = getIntent().getExtras().getString("visit_user_name");
        messageReceiverID = getIntent().getExtras().getString("visit_user_id");
        messageReceiverImage = getIntent().getExtras().getString("visit_image");



        IntializeControllers();

        userName.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.profile_image).into(userImage);

        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                SendMessage();

            }
        });

        DisplayLastSeen();

        SendFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CharSequence options[]=new CharSequence[]
                        {
                                "Image",
                                "PDF Files",
                                "Ms Word Files"
                        };
                AlertDialog.Builder builder=new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Select the File");

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which==0)
                        {
                            checker="image";
                            Intent intent =new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent,"select image"),1);
                        }
                        if (which==1)
                        {
                            checker="pdf";

                            Intent intent =new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/pdf");
                            startActivityForResult(intent.createChooser(intent,"select pdf file"),1);
                        }
                        if (which==2)
                        {
                            checker="docx";

                            Intent intent =new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/msword");
                            startActivityForResult(intent.createChooser(intent,"select ms word file"),1);

                        }

                    }
                });

                builder.show();

            }
        });




    }


    private void IntializeControllers()
    {

        ChatToolbar=(Toolbar)findViewById(R.id.chat_toolbar);
        setSupportActionBar(ChatToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater=(LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView =layoutInflater.inflate(R.layout.custom_chat_bar,null);

        actionBar.setCustomView(actionBarView);


        userName=(TextView)findViewById(R.id.custom_profile_name);
        userLastSeen=(TextView)findViewById(R.id.custom_profile_last_seen);
        userImage=(CircleImageView) findViewById(R.id.custom_profile_IMAGE);

        SendMessageButton=(ImageButton)findViewById(R.id.send_message_btn);
        SendFilesButton=(ImageButton)findViewById(R.id.send_file_btn);
        MessageInputText=(EditText)findViewById(R.id.input_message);

        messageAdapter=new messageAdapter(messagesList);
        userMessageList=(RecyclerView)findViewById(R.id.private_messages_list_of_users);
        linearLayoutManager=new LinearLayoutManager(this);
        userMessageList.setLayoutManager(linearLayoutManager);
        userMessageList.setAdapter(messageAdapter);
        loadingBar=new ProgressDialog(this);


        Calendar calendar =Calendar.getInstance();
        SimpleDateFormat CurrentDate=new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = CurrentDate.format(calendar.getTime());
        SimpleDateFormat CurrentTime=new SimpleDateFormat("hh:mm a");
        saveCurrentTime = CurrentTime.format(calendar.getTime());


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==1 && resultCode==RESULT_OK && data!=null && data.getData()!=null)
        {
            loadingBar.setTitle("sending file");
            loadingBar.setMessage("please wait we are sending that file...");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            fileUri=data.getData();

            if (!checker.equals("image"))
            {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Document Files");

                final String messageSenderRef ="Messages/"+messageSenderID+"/"+messageReceiverID;
                final String messageReceiverRef ="Messages/"+messageReceiverID+"/"+messageSenderID;


                DatabaseReference userMessageKeyRef=RootRef.child("Messages")
                        .child(messageSenderID).child(messageReceiverID).push();

                final String messagePushID =userMessageKeyRef.getKey();

                final StorageReference filePath =storageReference.child(messagePushID+"."+checker);

                final String[] uri = {""};
                filePath.putFile(fileUri)

                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                uri[0] = taskSnapshot.getStorage().getDownloadUrl().toString();

                            }
                        })
                        .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                    {
                        Map messageTextBody = new HashMap();
                        messageTextBody.put("message",uri[0]);
                        messageTextBody.put("name",fileUri.getLastPathSegment());
                        messageTextBody.put("type",checker);
                        messageTextBody.put("from",messageSenderID);
                        messageTextBody.put("to",messageReceiverID);
                        messageTextBody.put("messageID",messagePushID);
                        messageTextBody.put("time",saveCurrentTime);
                        messageTextBody.put("date",saveCurrentDate);

                        Map messageBodyDetails = new HashMap();
                        messageBodyDetails.put(messageSenderRef+"/"+messagePushID,messageTextBody);
                        messageBodyDetails.put(messageReceiverRef+"/"+messagePushID,messageTextBody);

                        RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                if (task.isSuccessful())
                                {
                                    loadingBar.dismiss();
                                    Toast.makeText(ChatActivity.this,"Message Sent Successfully...",Toast.LENGTH_LONG).show();


                                }else
                                {
                                    loadingBar.dismiss();
                                    Toast.makeText(ChatActivity.this,"Erorr  ",Toast.LENGTH_LONG).show();

                                }
                                MessageInputText.setText("");

                            }
                        });





                        }



                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        loadingBar.dismiss();
                        Toast.makeText(ChatActivity.this,"Erorr  ",Toast.LENGTH_LONG).show();


                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                    }
                });


            }
            else if (checker.equals("image"))
            {
//
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");

                final String messageSenderRef ="Messages/"+messageSenderID+"/"+messageReceiverID;
                final String messageReceiverRef ="Messages/"+messageReceiverID+"/"+messageSenderID;


                DatabaseReference userMessageKeyRef=RootRef.child("Messages")
                        .child(messageSenderID).child(messageReceiverID).push();
                final String messagePushID =userMessageKeyRef.getKey();

                 final StorageReference filePath =storageReference.child(messagePushID+"."+"jpg");

                uploadTask=filePath.putFile(fileUri);


                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful()) {
                            loadingBar.dismiss();
                            Toast.makeText(ChatActivity.this, "Error" +task.getException(), Toast.LENGTH_SHORT).show();
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {

                        if (task.isSuccessful()) {
                            Uri downloadUri=task.getResult();
                            myUri=downloadUri.toString();

                            Map messageTextBody = new HashMap();
                            messageTextBody.put("message",myUri);
                            messageTextBody.put("name",fileUri.getLastPathSegment());
                            messageTextBody.put("type",checker);
                            messageTextBody.put("from",messageSenderID);
                            messageTextBody.put("to",messageReceiverID);
                            messageTextBody.put("messageID",messagePushID);
                            messageTextBody.put("time",saveCurrentTime);
                            messageTextBody.put("date",saveCurrentDate);

                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(messageSenderRef+"/"+messagePushID,messageTextBody);
                            messageBodyDetails.put(messageReceiverRef+"/"+messagePushID,messageTextBody);

                            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful())
                                    {
                                        loadingBar.dismiss();
                                        Toast.makeText(ChatActivity.this,"Message Sent Successfully...",Toast.LENGTH_LONG).show();


                                    }else
                                    {
                                        loadingBar.dismiss();
                                        Toast.makeText(ChatActivity.this,"Erorr  ",Toast.LENGTH_LONG).show();


                                    }
                                    MessageInputText.setText("");

                                }
                            });

                        }

                    }
                });


            }
            else
                {
                    loadingBar.dismiss();
                    Toast.makeText(this, "Nothing Selected, Error.", Toast.LENGTH_SHORT).show();


                }

        }
    }

    private void DisplayLastSeen()
    {
        RootRef.child("Users").child(messageReceiverID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.child("userState").hasChild("state"))
                        {
                            String state =dataSnapshot.child("userState").child("state").getValue().toString();
                            String date =dataSnapshot.child("userState").child("date").getValue().toString();
                            String time =dataSnapshot.child("userState").child("time").getValue().toString();

                            if (state.equals("online"))
                            {
                                userLastSeen.setText("online");
                            }else if (state.equals("offline"))
                            {
                                userLastSeen.setText("Last Seen: " + date + " " + time);

                            }

                        }else {
                            userLastSeen.setText("offline");

                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    @Override
    protected void onStart()
    {
        super.onStart();
        RootRef.child("Messages").child(messageSenderID).child(messageReceiverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
                    {
                        Messages messages=dataSnapshot.getValue(Messages.class);
                        messagesList.add(messages);
                        messageAdapter.notifyDataSetChanged();
                        userMessageList.smoothScrollToPosition(userMessageList.getAdapter().getItemCount());
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

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

    private void SendMessage()
        {

            String messageText =MessageInputText.getText().toString();

            if (TextUtils.isEmpty(messageText))
            {
                Toast.makeText(ChatActivity.this,"empty",Toast.LENGTH_LONG).show();

            }else {

                String messageSenderRef ="Messages/"+messageSenderID+"/"+messageReceiverID;
                String messageReceiverRef ="Messages/"+messageReceiverID+"/"+messageSenderID;


                DatabaseReference userMessageKeyRef=RootRef.child("Messages")
                        .child(messageSenderID).child(messageReceiverID).push();
                String messagePushID =userMessageKeyRef.getKey();

                Map messageTextBody = new HashMap();
                messageTextBody.put("message",messageText);
                messageTextBody.put("type","text");
                messageTextBody.put("from",messageSenderID);
                messageTextBody.put("to",messageReceiverID);
                messageTextBody.put("messageID",messagePushID);
                messageTextBody.put("time",saveCurrentTime);
                messageTextBody.put("date",saveCurrentDate);

                Map messageBodyDetails = new HashMap();
                messageBodyDetails.put(messageSenderRef+"/"+messagePushID,messageTextBody);
                messageBodyDetails.put(messageReceiverRef+"/"+messagePushID,messageTextBody);

                RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful())
                        {
                            Toast.makeText(ChatActivity.this,"Message Sent Successfully...",Toast.LENGTH_LONG).show();

                        }else
                        {
                            Toast.makeText(ChatActivity.this,"Erorr",Toast.LENGTH_LONG).show();

                        }
                        MessageInputText.setText("");

                    }
                });

            }
        }
}
