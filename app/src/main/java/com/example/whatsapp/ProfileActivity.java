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

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;


public class ProfileActivity extends AppCompatActivity {
    String receverUserId,Current_state, senderUsersId;

    private CircleImageView userProfileImage;
    private TextView userProfileName, userProfileStatus;
    private Button sendMessageRequestButton,DeclineMessageRequestButton;

    private DatabaseReference userRef ,ChatRequesRef ,ContactsRef,NotificationRef;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        mAuth =FirebaseAuth.getInstance();
        userRef= FirebaseDatabase.getInstance().getReference().child("Users");
        ChatRequesRef=FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        ContactsRef=FirebaseDatabase.getInstance().getReference().child("Contacts");
        NotificationRef=FirebaseDatabase.getInstance().getReference().child("Notifications");


        receverUserId = getIntent().getExtras().get("visit_user_id").toString();
        senderUsersId = mAuth.getCurrentUser().getUid().toString();

        Toast.makeText(this,receverUserId,Toast.LENGTH_SHORT).show();

        userProfileImage =(CircleImageView)findViewById(R.id.visit_profile_image);
        userProfileName =(TextView) findViewById(R.id.visit_user_name);
        userProfileStatus =(TextView) findViewById(R.id.visit_user_status);
        sendMessageRequestButton =(Button) findViewById(R.id.send_message_request_button);
        DeclineMessageRequestButton =(Button) findViewById(R.id.decline_message_request_button);
        Current_state ="new";

        RetrieveUserInfo();


    }

    private void RetrieveUserInfo()
    {
        userRef.child(receverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists() && (dataSnapshot.hasChild("image")))
                {

                    String userImage=dataSnapshot.child("image").getValue().toString();
                    String userName=dataSnapshot.child("name").getValue().toString();
                    String userStatus=dataSnapshot.child("status").getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage);
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);


                    ManageChatRequests();


                }
                else {

                    String userName=dataSnapshot.child("name").getValue().toString();
                    String userStatus=dataSnapshot.child("status").getValue().toString();

                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

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
        ChatRequesRef.child(senderUsersId)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.hasChild(receverUserId))
                {
                    String request_type=dataSnapshot.child(receverUserId).child("request_type").getValue().toString();

                    if (request_type.equals("sent"))
                    {
                        Current_state="request_sent";
                        sendMessageRequestButton.setText("Cancel Chat Request");
                    }else if (request_type.equals("received"))
                    {
                        Current_state="request_received";
                        sendMessageRequestButton.setText("Accept Chat Request");
                        DeclineMessageRequestButton.setVisibility(View.VISIBLE);
                        DeclineMessageRequestButton.setEnabled(true);
                        DeclineMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v)
                            {
                             CancelChatRequest();
                            }
                        });

                    }

                }else
                    {
                        ContactsRef.child(senderUsersId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                    {
                                        if (dataSnapshot.hasChild(receverUserId))
                                        {
                                         Current_state="friends";
                                         sendMessageRequestButton.setText("Remove this Contact");
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

        if (!senderUsersId.equals(receverUserId))
        {

            sendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendMessageRequestButton.setEnabled(false);
                    if (Current_state.equals("new"))
                    {
                        sendChatRequest();
                    }
                    if (Current_state.equals("request_sent"))
                    {
                        CancelChatRequest();
                    }
                    if (Current_state.equals("request_received"))
                    {
                        AcceptChatRequest();
                    }
                    if (Current_state.equals("friends"))
                    {
                        RemoveSpecificContact();
                    }

                }
            });

        }else
            {
                sendMessageRequestButton.setVisibility(View.INVISIBLE);
            }
    }

    private void RemoveSpecificContact()
    {

        ContactsRef.child(senderUsersId).child(receverUserId).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            ContactsRef.child(receverUserId).child(senderUsersId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                sendMessageRequestButton.setEnabled(true);
                                                sendMessageRequestButton.setText("send message");
                                                Current_state="new";

                                                DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                DeclineMessageRequestButton.setEnabled(false);

                                            }

                                        }
                                    });
                        }

                    }
                });
    }

    private void AcceptChatRequest()
    {
        ContactsRef.child(senderUsersId).child(receverUserId)
                .child("Contacts").setValue("saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            ContactsRef.child(receverUserId).child(senderUsersId)
                                    .child("Contacts").setValue("saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {

                                                ChatRequesRef.child(senderUsersId).child(receverUserId).removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                if (task.isSuccessful())
                                                                {
                                                                    ChatRequesRef.child(receverUserId).child(senderUsersId)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task)
                                                                                {
                                                                                    if (task.isSuccessful())
                                                                                    {
                                                                                        sendMessageRequestButton.setEnabled(true);
                                                                                        sendMessageRequestButton.setText("Remove this Contact");
                                                                                        Current_state="frinds";

                                                                                        DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                                                        DeclineMessageRequestButton.setEnabled(false);

                                                                                        DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                                                        DeclineMessageRequestButton.setEnabled(false);

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


                    }
                });

    }

    private void CancelChatRequest()
    {
        ChatRequesRef.child(senderUsersId).child(receverUserId).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if (task.isSuccessful())
                {
                    ChatRequesRef.child(receverUserId).child(senderUsersId)
                            .removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    if (task.isSuccessful())
                                    {
                                        sendMessageRequestButton.setEnabled(true);
                                        sendMessageRequestButton.setText("send message");
                                        Current_state="new";

                                        DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                        DeclineMessageRequestButton.setEnabled(false);

                                    }

                                }
                            });
                }

            }
        });
    }

    private void sendChatRequest()
    {
        ChatRequesRef.child(senderUsersId).child(receverUserId)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if (task.isSuccessful())
                {
                    ChatRequesRef.child(receverUserId).child(senderUsersId)
                            .child("request_type").setValue("received")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if (task.isSuccessful())
                            {

                                HashMap<String,String> chatNotificationMap=new HashMap<>();
                                chatNotificationMap.put("from",senderUsersId);
                                chatNotificationMap.put("type","request");

                                NotificationRef.child(receverUserId).push()
                                        .setValue(chatNotificationMap)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task)
                                            {
                                                if (task.isSuccessful())
                                                {
                                                    sendMessageRequestButton.setEnabled(true);
                                                    sendMessageRequestButton.setText("Cancel Chat Request");
                                                    Current_state="request_sent";
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
}
