package com.example.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.zip.Inflater;
//import android.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    private Toolbar myToolbar;
    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private TabsAccessorAdapter myTabsAccessorAdapter;
//    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth =FirebaseAuth.getInstance();

        RootRef = FirebaseDatabase.getInstance().getReference();


        myToolbar=(Toolbar)findViewById(R.id.main_bage_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("WhatsApp");

        myViewPager = (ViewPager)findViewById(R.id.main_tabs_pager);
        myTabsAccessorAdapter = new
                TabsAccessorAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(myTabsAccessorAdapter);
        myTabLayout = (TabLayout)findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewPager);

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser==null){
            sendUserToLoginActivity();
        }
        else
        {

            VerifyUserExistance();
            updateUserStatus("online");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser!=null)
        {
            updateUserStatus("offline");
        }
    }



        @Override
    protected void onDestroy() {
        super.onDestroy();
//        super.onStop();
            FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser!=null)
        {
            updateUserStatus("offline");
        }
    }

    private void VerifyUserExistance()
    {
       String currentUserID= mAuth.getCurrentUser().getUid();
       RootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot dataSnapshot)
           {
               if (dataSnapshot.child("name").exists())
               {
                   Toast.makeText(MainActivity.this,"welcome",Toast.LENGTH_SHORT).show();
               }
               else
                {
                    sendUserToSettingsActivity();
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId()== R.id.main_find_friends_option)
        {
            sendUserToFindFriendsActivity();
        }
        if (item.getItemId()== R.id.main_create_group_option)
        {

            final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this,R.style.AlertDialog);
            builder.setTitle("Enter Group Name");
            final EditText grouupNameField = new EditText(MainActivity.this);
            grouupNameField.setHint("mima kamal");
            builder.setView(grouupNameField);

            builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    String groupName =grouupNameField.getText().toString();
                    if (TextUtils.isEmpty(groupName))
                    {
                    Toast.makeText(MainActivity.this,"Please Write Group Name",Toast.LENGTH_SHORT).show();
                    }
                    else
                        {
                            CreateNewGroup(groupName);
                        }

                }
            });

            builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {

                    dialog.cancel();
                }
            });

            builder.show();
        }

        if (item.getItemId()== R.id.main_settings_option)
        {
            sendUserToSettingsActivity();

        }

        if (item.getItemId()== R.id.main_logout_option)
        {
            updateUserStatus("offline");
            mAuth.signOut();
            sendUserToLoginActivity();


        }
        return true;

    }

    private void CreateNewGroup(final String groupName)
    {
        RootRef.child("Groups").child(groupName).setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    Toast.makeText(MainActivity.this,groupName+"group is Created Successfully...",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendUserToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this,LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    private void sendUserToSettingsActivity() {
        Intent settingsintent = new Intent(MainActivity.this,SettingsActivity.class);
        startActivity(settingsintent);
    }

    private void sendUserToFindFriendsActivity() {
        Intent findFriendsIntent = new Intent(MainActivity.this,FindFriendsActivity.class);
        startActivity(findFriendsIntent);
    }

    private void updateUserStatus (String state)
    {
        String saveCurrentTime,saveCurrentDate;
        Calendar calendar =Calendar.getInstance();
        SimpleDateFormat CurrentDate=new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = CurrentDate.format(calendar.getTime());
        SimpleDateFormat CurrentTime=new SimpleDateFormat("hh:mm a");
        saveCurrentTime = CurrentTime.format(calendar.getTime());

        HashMap<String,Object> onlineState =new HashMap<>();
        onlineState.put("time",saveCurrentTime);
        onlineState.put("date",saveCurrentDate);
        onlineState.put("state",state);

        currentUserID=mAuth.getCurrentUser().getUid();
        RootRef.child("Users").child(currentUserID).child("userState").updateChildren(onlineState);
    }
}
