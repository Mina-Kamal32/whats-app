package com.example.whatsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.Layout;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.internal.Util;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton sendMessageButton;
    private EditText userMassageInput;
    private ScrollView mScrollView;
    private TextView displayTextMassages;

    private DatabaseReference UsersRef, GrupNameRef , GroupMessageKeyRef;
    private FirebaseAuth mAuth;

    String currentGroupName ,currentUserID,currentUserName ,currentDate,currentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        currentGroupName = getIntent().getExtras().getString("groupName");

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        GrupNameRef=FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);

        InitializeFields();

        GetUserInfo();

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                saveMessageInfoToDatabase();
                userMassageInput.setText("");
                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);

            }
        });



    }


    @Override
    protected void onStart()
    {
        super.onStart();
        GrupNameRef.addChildEventListener(new ChildEventListener() {
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



    private void InitializeFields()
    {
        mToolbar =(Toolbar) findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(currentGroupName);
        sendMessageButton =(ImageButton)findViewById(R.id.send_message_button);
        userMassageInput =(EditText)findViewById(R.id.input_group_message);
        displayTextMassages =(TextView)findViewById(R.id.group_chat_text_display);
        mScrollView =(ScrollView)findViewById(R.id.my_scroll_view);

    }

    private void GetUserInfo()
    {
        UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    currentUserName =dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void saveMessageInfoToDatabase()
    {
        String message =userMassageInput.getText().toString();
        String messageKEY =GrupNameRef.push().getKey();

        if (TextUtils.isEmpty(message))
        {

            Toast.makeText(this,"please write message first..",Toast.LENGTH_SHORT).show();
        }
        else
            {

                Calendar calForDate = Calendar.getInstance();
                SimpleDateFormat simpleDateFormat= new SimpleDateFormat("MMM dd, yyyy");
                currentDate =simpleDateFormat.format(calForDate.getTime());

                Calendar calForTime = Calendar.getInstance();
                SimpleDateFormat simpleDateTime= new SimpleDateFormat("hh:mm a");
                currentTime =simpleDateTime.format(calForTime.getTime());

                HashMap<String ,Object> groupMessageKey = new HashMap<>();
                GrupNameRef.updateChildren(groupMessageKey);

                GroupMessageKeyRef=GrupNameRef.child(messageKEY);

                HashMap<String ,Object> MessageInfoMap = new HashMap<>();
                MessageInfoMap.put("name",currentUserName);
                MessageInfoMap.put("message",message);
                MessageInfoMap.put("date",currentDate);
                MessageInfoMap.put("time",currentTime);

                GroupMessageKeyRef.updateChildren(MessageInfoMap);




            }

    }

    private void DisplayMessages(DataSnapshot dataSnapshot)
    {

        Iterator iterator =dataSnapshot.getChildren().iterator();

        while (iterator.hasNext())
        {
            String chatDate = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatMessage = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatName = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatTime = (String) ((DataSnapshot)iterator.next()).getValue();

            displayTextMassages.append(chatName +" \n"+ chatMessage +"\n"+ chatTime +"    " +chatDate +"\n\n\n" );

            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }


}
